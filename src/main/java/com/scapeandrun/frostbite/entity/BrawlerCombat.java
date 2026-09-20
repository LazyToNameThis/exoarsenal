package com.scapeandrun.frostbite.entity;

import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import static com.scapeandrun.frostbite.entity.EntityBrawlerEffect.*;
import static com.scapeandrun.frostbite.entity.BrawlerScore.Pattern;

final class BrawlerCombat {
    private final EntityBrawler b;
    private Vec3d center = Vec3d.ZERO, locked = Vec3d.ZERO, start = Vec3d.ZERO;
    private final List<EntityBrawlerEffect> objects = new ArrayList<>();
    private EntityPlayer grabbed;
    private int recallCount, counterAt = -1;
    private boolean dodgePassed;
    private final EntityBrawlerEffect[] manufactured = new EntityBrawlerEffect[6];
    private EntityBrawlerEffect weapon, weaponChain;
    private Vec3d weaponFrom = Vec3d.ZERO, weaponTo = Vec3d.ZERO;
    private final List<EntityBrawlerEffect> field = new ArrayList<>();
    private final Set<Integer> activated = new HashSet<>();
    private EntityBrawlerEffect terrain;

    BrawlerCombat(EntityBrawler b) {
        this.b = b;
    }

    void reset() {
        for (EntityBrawlerEffect e : objects) e.setDead();
        objects.clear();
        Arrays.fill(manufactured, null);
        field.clear();
        activated.clear();
        terrain = null;
        weapon = null;
        weaponChain = null;
        release();
        recallCount = 0;
        counterAt = -1;
        b.counterContact(-1);
        dodgePassed = true;
        if (!b.world.isRemote)
            com.scapeandrun.frostbite.world.ExcavatorTerrain.get(b.world).release(b.getUniqueID());
    }

    void recalled() {
        recallCount++;
    }

    boolean counter() {
        if (b.pattern() != Pattern.COUNTER || b.clock() < 20 || b.clock() > 90 || counterAt >= 0)
            return false;
        counterAt = b.clock() + 14;
        b.counterContact(counterAt);
        return true;
    }

    private EntityBrawlerEffect effect(
            int kind, Vec3d from, Vec3d to, int wait, int life, float radius, float damage) {
        EntityBrawlerEffect e =
                new EntityBrawlerEffect(b, kind, from, to, wait, life, radius, damage);
        b.world.spawnEntity(e);
        objects.add(e);
        return e;
    }

    private Vec3d ground(Vec3d p) {
        BlockPos at = new BlockPos(p);
        for (int n = 0; n < 20 && !b.world.getBlockState(at.down()).getMaterial().isSolid(); n++)
            at = at.down();
        for (int n = 0; n < 12 && b.world.getBlockState(at).getMaterial().isSolid(); n++)
            at = at.up();
        return new Vec3d(p.x, at.getY() + .15, p.z);
    }

    private Vec3d ringPoint(int n, int count, double radius, double y) {
        double a = n * Math.PI * 2 / count;
        return center.addVector(Math.cos(a) * radius, y, Math.sin(a) * radius);
    }

    private void lock(EntityLivingBase target) {
        locked = target.getPositionVector().addVector(0, .8, 0);
        start = b.getPositionVector();
    }

    private void line(Vec3d from, Vec3d to, int delay, float damage) {
        effect(FAULT, ground(from), ground(to), delay, delay + 9, .5F, damage);
    }

    private void wave(Vec3d at, float radius, float damage) {
        if (b.clashTick() == 0) effect(RING, ground(at), Vec3d.ZERO, 4, 24, radius, damage);
    }

    private void shard(Vec3d from, Vec3d to, double speed) {
        effect(SHARD, from, to, 8, 80, .4F, 6).velocity(to.subtract(from).normalize().scale(speed));
    }

    private void volley(Vec3d from, Vec3d to, int count) {
        Vec3d d = to.subtract(from).normalize();
        for (int i = 0; i < count; i++)
            shard(
                    from,
                    from.add(d.rotateYaw((float) ((i - (count - 1) * .5) * .12)).scale(20)),
                    .8);
    }

    private void dash(Vec3d destination, double speed, float damage, boolean parry) {
        if (b.clashTick() > 0) return;
        Vec3d d = destination.subtract(b.getPositionVector());
        if (d.lengthVector() > .2)
            b.combatMove(d.normalize().scale(Math.min(speed, d.lengthVector())));
        if (damage <= 0) return;
        if (b.phase() == 3) b.strike(0, damage, 2.4, true);
        else b.ram(damage, parry);
    }

    private void approach(EntityLivingBase target, double speed, double range) {
        Vec3d d = target.getPositionVector().subtract(b.getPositionVector());
        b.steer(
                target.getPositionVector()
                        .subtract(new Vec3d(d.x, 0, d.z).normalize().scale(range))
                        .addVector(0, b.phase() == 1 ? 2.5 : 0, 0),
                speed);
    }

    private void orbit(int t, double period, double radius, double y) {
        double a = t * Math.PI * 2 / period;
        b.steer(
                center.addVector(
                        Math.cos(a) * radius, y + (b.phase() == 1 ? 2.5 : 0), Math.sin(a) * radius),
                1.1);
    }

    private void hit(int arm, float damage, boolean parry) {
        b.strike(arm, damage, 1.8, parry);
    }

    private void anchor(int count, int life) {
        for (int i = 0; i < count; i++)
            effect(
                            CHAIN,
                            b.localToWorld(b.hand(2, 0)),
                            ground(ringPoint(i, count, 12, 0)),
                            0,
                            life,
                            .12F,
                            0)
                    .tether();
    }

    private void breakAnchors() {
        for (EntityBrawlerEffect e : objects) if (e.kind() == CHAIN) e.setDead();
    }

    private void grab(EntityLivingBase target) {
        if (!(target instanceof EntityPlayer)
                || target.getDistanceSq(b) > 30
                || com.scapeandrun.frostbite.combat.WeaponCombat.dodgeReady((EntityPlayer) target))
            return;
        EntityPlayer p = (EntityPlayer) target;
        if (p.isCreative() || p.isSpectator()) return;
        grabbed = p;
    }

    private void hold(Vec3d at) {
        if (grabbed == null) return;
        if (!grabbed.isEntityAlive()
                || grabbed.dimension != b.dimension
                || grabbed.getDistanceSq(b) > 400) {
            release();
            return;
        }
        AxisAlignedBB box =
                grabbed.getEntityBoundingBox().offset(at.subtract(grabbed.getPositionVector()));
        if (!b.world.getCollisionBoxes(grabbed, box).isEmpty()) {
            release();
            return;
        }
        grabbed.fallDistance = 0;
        grabbed.motionX = grabbed.motionY = grabbed.motionZ = 0;
        if (grabbed instanceof EntityPlayerMP)
            ((EntityPlayerMP) grabbed)
                    .connection.setPlayerLocation(
                            at.x, at.y, at.z, grabbed.rotationYaw, grabbed.rotationPitch);
        else grabbed.setPosition(at.x, at.y, at.z);
    }

    private void release() {
        if (grabbed != null) {
            grabbed.fallDistance = 0;
            grabbed.velocityChanged = true;
        }
        grabbed = null;
    }

    private void holdBetweenHands() {
        Vec3d a = b.localToWorld(b.hand(0, 0)), c = b.localToWorld(b.hand(1, 0));
        hold(a.add(c).scale(.5).addVector(0, -.7, 0));
    }

    private void throwHeldUp() {
        if (grabbed == null) return;
        EntityPlayer player = grabbed;
        release();
        player.addVelocity(0, 1.25, 0);
        player.velocityChanged = true;
    }

    private void slamHeld(float damage) {
        if (grabbed == null) return;
        Vec3d at = ground(grabbed.getPositionVector());
        hold(at);
        if (grabbed != null) {
            if (b.pattern() == Pattern.GROUND_POUND) b.punchHeld(grabbed, 0, damage);
            else grabbed.attackEntityFrom(DamageSource.causeMobDamage(b), damage);
        }
        wave(at, 7, 5);
    }

    private void debris(Vec3d from, Vec3d to) {
        for (int i = 0; i < 9; i++) {
            double a = i * Math.PI * 2 / 9;
            effect(
                            TERRAIN,
                            from.addVector(Math.cos(a), i % 3 * .45, Math.sin(a)),
                            to,
                            0,
                            35,
                            .42F,
                            3)
                    .velocity(
                            to.subtract(from)
                                    .normalize()
                                    .scale(.65)
                                    .addVector(Math.cos(a) * .18, .15, Math.sin(a) * .18));
        }
    }

    private static Vec3d between(Vec3d a, Vec3d c, double t) {
        return a.add(c.subtract(a).scale(BrawlerScore.smooth(t)));
    }

    private void deployWeapon(Vec3d at, float size) {
        if (weapon != null) weapon.setDead();
        weapon = effect(CUT, at, at, 0, 220, size, 0);
        weaponFrom = at;
        weaponTo = at;
    }

    private void weaponLeg(Vec3d to) {
        if (weapon == null) return;
        weaponFrom = weapon.getPositionVector();
        weaponTo = to;
        weapon.rearm();
    }

    private void guideWeapon(Vec3d at, float damage) {
        if (weapon == null || weapon.isDead) return;
        weapon.guide(at, damage);
        if (weaponChain != null && !weaponChain.isDead)
            weaponChain.endpoints(b.localToWorld(b.hand(2, 0)), at);
    }

    private void catchChain() {
        if (weapon == null) return;
        if (weaponChain != null) weaponChain.setDead();
        weaponChain =
                effect(
                        CHAIN,
                        b.localToWorld(b.hand(2, 0)),
                        weapon.getPositionVector(),
                        0,
                        180,
                        .075F,
                        0);
    }

    private void catchWeapon() {
        boolean caught = weapon != null;
        if (weapon != null) weapon.setDead();
        if (weaponChain != null) weaponChain.setDead();
        weapon = null;
        weaponChain = null;
        if (caught)
            b.world.playSound(
                    null,
                    b.getPosition(),
                    SoundEvents.BLOCK_ANVIL_PLACE,
                    SoundCategory.HOSTILE,
                    .7F,
                    1.3F);
    }

    private EntityBrawlerEffect liftTerrain(Vec3d at, float size, int life) {
        Vec3d surface = ground(at);
        BlockPos root = new BlockPos(surface).down();
        net.minecraft.block.state.IBlockState state = b.world.getBlockState(root);
        com.scapeandrun.frostbite.world.ExcavatorTerrain journal =
                com.scapeandrun.frostbite.world.ExcavatorTerrain.get(b.world);
        for (int x = -1; x <= 1; x++)
            for (int z = -1; z <= 1; z++) journal.cut(b, root.add(x, 0, z));
        return effect(TERRAIN, surface, Vec3d.ZERO, 0, life, size, 0)
                .terrainState(
                        state.getMaterial().isSolid()
                                ? state
                                : net.minecraft.init.Blocks.STONE.getDefaultState());
    }

    void tick(EntityLivingBase target) {
        int t = b.clock();
        Pattern p = b.pattern();
        if (t == 1) {
            reset();
            center = target.getPositionVector();
            lock(target);
        }
        objects.removeIf(e -> e.isDead);
        if (objects.size() > 90) {
            objects.get(0).setDead();
            objects.remove(0);
        }
        switch (p) {
            case FLAIL_SIEGE:
                approach(target, .28, 4.5);
                if (t >= 30 && t <= 86 && t % 7 == 2) b.strike(3, 11, 1.7, false);
                if (t == 88) wave(b.localToWorld(b.hand(3, 0)), 8, 9);
                if (t == 124 || t == 140 || t == 156)
                    volley(b.localToWorld(b.hand(3, 0)), target.getPositionEyes(1), 4);
                break;
            case PISTON:
                approach(target, .35, 3.6);
                if (t == 28 || t == 46) hit(0, t == 28 ? 7 : 9, false);
                if (t == 66) hit(1, 9, false);
                if (t == 90) {
                    boolean contact = b.strike(0, 10, 1.8, false);
                    if (contact) {
                        target.motionY = .85;
                        target.velocityChanged = true;
                    }
                    lock(target);
                    effect(
                            CHAIN,
                            b.localToWorld(b.hand(2, 0)),
                            locked.addVector(0, 7, 0),
                            0,
                            36,
                            .1F,
                            0);
                }
                if (t > 96 && t < 122 && b.armHealth(2) > 0)
                    dash(locked.addVector(0, 5, 0), .65, 0, false);
                if (t == 128) lock(target);
                if (t > 128 && t < 139) dash(locked, .8, 0, false);
                if (t == 138) {
                    if (!b.strike(0, 13, 2, true)) {
                        Vec3d d = locked.subtract(b.getPositionVector()).normalize();
                        for (int i = -2; i <= 2; i++)
                            line(
                                    b.getPositionVector(),
                                    b.getPositionVector().add(d.rotateYaw(i * .17F).scale(12)),
                                    8,
                                    7);
                    }
                }
                break;
            case WHEEL:
                if (t < 66) approach(target, .2, 7);
                if (t == 28) {
                    for (int i = -1; i <= 1; i++)
                        effect(
                                        BLADE,
                                        b.localToWorld(b.hand(1, 0)).addVector(0, i * .8, 0),
                                        target.getPositionEyes(1),
                                        4,
                                        65,
                                        1,
                                        8)
                                .velocity(
                                        target.getPositionEyes(1)
                                                .subtract(b.getPositionVector())
                                                .normalize()
                                                .scale(.7));
                }
                if (t == 66) {
                    lock(target);
                    deployWeapon(ground(b.localToWorld(new Vec3d(0, 0, 3))), 1.35F);
                    line(weaponFrom, ground(locked), 28, 10);
                }
                if (t == 94) {
                    weaponLeg(ground(target.getPositionVector()));
                    hit(0, 8, false);
                }
                if (t >= 94 && t < 118)
                    guideWeapon(
                            between(weaponFrom, weaponTo, (t - 94) / 24D).addVector(0, -.35, 0),
                            10);
                if (t >= 118 && t < 132)
                    guideWeapon(
                            weaponTo.addVector(0, Math.sin((t - 118) * Math.PI / 28) * 2.5, 0), 12);
                if (t == 132) {
                    catchChain();
                    weaponLeg(b.localToWorld(b.hand(1, 0)));
                }
                if (t >= 132 && t < 160)
                    guideWeapon(
                            between(weaponFrom, b.localToWorld(b.hand(1, 0)), (t - 132) / 28D), 6);
                if (t == 160) catchWeapon();
                break;
            case CRUCIFIX:
                if (t == 12)
                    for (int n = 0; n < 4; n++) {
                        EntityBrawlerEffect chunk = liftTerrain(ringPoint(n, 4, 12, 0), 1.4F, 145);
                        field.add(chunk);
                        manufactured[n] =
                                effect(
                                        CHAIN,
                                        b.localToWorld(b.hand(2, 0)),
                                        chunk.getPositionVector(),
                                        0,
                                        145,
                                        .12F,
                                        4);
                    }
                if (t >= 12 && t < 142)
                    for (int n = 0; n < field.size(); n++) {
                        Vec3d at =
                                ground(
                                                ringPoint(
                                                        n,
                                                        4,
                                                        12
                                                                - 6
                                                                        * BrawlerScore.smooth(
                                                                                (t - 24) / 70D),
                                                        0))
                                        .addVector(0, .6, 0);
                        field.get(n).guide(at, t >= 30 ? 7 : 0);
                        if (manufactured[n] != null && !manufactured[n].isDead)
                            manufactured[n].endpoints(b.localToWorld(b.hand(2, 0)), at);
                    }
                if (t >= 30 && t <= 94 && t % 16 == 14)
                    volley(b.localToWorld(b.hand(3, 0)), target.getPositionEyes(1), 3);
                if (t == 112) {
                    for (int n = 0; n < 2; n++)
                        if (manufactured[n] != null) manufactured[n].setDead();
                    lock(target);
                    line(b.getPositionVector(), locked, 14, 0);
                }
                if (t >= 126 && t < 141) dash(locked, 1.3, 12, true);
                if (t == 142) breakAnchors();
                break;
            case FACTORY:
                approach(target, .2, 9);
                if (t >= 20 && t <= 70 && t % 10 == 0) {
                    int n = (t - 20) / 10;
                    Vec3d mouth = b.localToWorld(b.hand(3, 0));
                    manufactured[n] =
                            effect(BLADE, mouth, ringPoint(n, 6, 7, 1.5), 180, 190, 1.1F, 0);
                }
                for (int n = 0; n < 6; n++)
                    if (manufactured[n] != null && !manufactured[n].isDead && t < 90 + n * 10) {
                        Vec3d mouth = b.localToWorld(b.hand(3, 0)), slot = ringPoint(n, 6, 7, 1.5);
                        double travel = BrawlerScore.smooth((t - (20 + n * 10)) / 16D);
                        manufactured[n].setPosition(
                                mouth.x + (slot.x - mouth.x) * travel,
                                mouth.y + (slot.y - mouth.y) * travel + Math.sin(travel * Math.PI),
                                mouth.z + (slot.z - mouth.z) * travel);
                    }
                if (t >= 90 && t <= 140 && t % 10 == 0) {
                    int n = (t - 90) / 10;
                    Vec3d from = ringPoint(n, 6, 7, 1.5),
                            to = n == 5 ? target.getPositionEyes(1) : ringPoint(n + 1, 6, 7, 1.5);
                    if (manufactured[n] != null) manufactured[n].setDead();
                    effect(BLADE, from, to, 0, n == 5 ? 30 : 10, 1.1F, 8)
                            .velocity(to.subtract(from).scale(n == 5 ? .12 : .1));
                    b.world.playSound(
                            null,
                            new BlockPos(from),
                            SoundEvents.BLOCK_ANVIL_LAND,
                            SoundCategory.HOSTILE,
                            .8F,
                            1.25F + n * .08F);
                }
                break;
            case PARRY_CHECK:
                approach(target, .5, 3);
                if (t == 24) hit(0, 9, true);
                if (t == 42) hit(1, 10, true);
                if (t == 51) {
                    lock(target);
                    effect(CHAIN, b.localToWorld(b.hand(2, 0)), locked, 10, 16, .25F, 9);
                }
                if (t == 67)
                    dodgePassed = target.getDistanceSq(locked.x, locked.y - .8, locked.z) > 3;
                if (t == 80) {
                    hit(0, 12, true);
                    if (b.parryCount() >= 3 && dodgePassed) b.stagger(45);
                }
                break;
            case RECOIL:
                if (t == 20 || t == 55) {
                    lock(target);
                    volley(
                            b.getPositionVector(),
                            b.getPositionVector().subtract(locked.subtract(b.getPositionVector())),
                            7);
                }
                if (t >= 22 && t < 34 || t >= 57 && t < 69) dash(locked, 1.1, 9, false);
                if (t == 34) hit(0, 10, false);
                if (t == 69) hit(1, 10, false);
                if (t >= 78 && t < 98) b.combatMove(new Vec3d(0, .5, 0));
                if (t == 98) {
                    anchor(2, 32);
                    lock(target);
                    volley(b.getPositionVector(), b.getPositionVector().addVector(0, 20, 0), 5);
                }
                if (t > 100 && t < 121) dash(ground(locked), 1.1, 8, false);
                if (t == 121) wave(b.getPositionVector(), 10, 12);
                break;
            case CHAIN_BOXING:
                if (t == 10) anchor(2, 120);
                if (t >= 25 && t < 124) {
                    int leg = t < 65 ? 0 : t < 99 ? 1 : 2;
                    Vec3d goal = center.addVector((leg % 2 == 0 ? 1 : -1) * 8, 1, 0);
                    dash(goal, .55 + leg * .25, 8, false);
                    if (t == 49 || t == 85 || t == 113) hit(leg == 1 ? 1 : 0, 10, false);
                }
                if (t == 124) {
                    breakAnchors();
                    lock(target);
                }
                if (t >= 136 && t < 150) dash(locked, 1.5, 15, true);
                break;
            case GUILLOTINE:
                if (t == 40 || t == 85 || t == 130) {
                    lock(target);
                    int n = (t - 40) / 45;
                    Vec3d axis =
                            (n == 0
                                            ? new Vec3d(1, 0, 0)
                                            : n == 1 ? new Vec3d(.7, .7, 0) : new Vec3d(0, 1, 0))
                                    .rotateYaw((float) Math.toRadians(-b.rotationYaw));
                    Vec3d left = locked.subtract(axis.scale(7)), right = locked.add(axis.scale(7));
                    deployWeapon(left, 1.8F);
                    weaponTo = right;
                    effect(
                            CHAIN,
                            left.addVector(0, 0, -.5),
                            right.addVector(0, 0, -.5),
                            0,
                            25,
                            .08F,
                            0);
                    effect(
                            CHAIN,
                            left.addVector(0, 0, .5),
                            right.addVector(0, 0, .5),
                            0,
                            25,
                            .08F,
                            0);
                }
                if (t >= 40 && t < 166) {
                    int leg = (t - 40) % 45;
                    if (leg >= 14 && leg <= 22)
                        guideWeapon(between(weaponFrom, weaponTo, (leg - 14) / 8D), 12);
                    if (leg == 23) {
                        catchChain();
                        weaponLeg(b.localToWorld(b.hand(1, 0)));
                    }
                    if (leg > 23 && leg < 35)
                        guideWeapon(
                                between(weaponFrom, b.localToWorld(b.hand(1, 0)), (leg - 23) / 12D),
                                0);
                    if (leg == 35) catchWeapon();
                }
                if (t == 165) catchWeapon();
                if (t == 158) volley(b.localToWorld(b.hand(3, 0)), target.getPositionEyes(1), 5);
                break;
            case OVERDRIVE:
                if (t == 8) anchor(2, 156);
                approach(target, .4, 4);
                for (int cycle = 0; cycle < 3; cycle++) {
                    int at = 24 + cycle * 42;
                    if (t == at) volley(b.localToWorld(b.hand(3, 0)), target.getPositionEyes(1), 3);
                    if (t == at + 12) hit(1, 9, false);
                    if (t == at + 24) hit(0, 10, false);
                    if (t > at + 28 && t < at + 36) orbit(t, 60, 6, 1);
                }
                if (t == 166) {
                    lock(target);
                    line(b.getPositionVector(), locked, 16, 0);
                }
                if (t >= 182 && t < 188) dash(locked, 1.4, 16, true);
                break;
            case BALLISTA:
                if (t == 12) anchor(2, 150);
                if (t == 50) {
                    lock(target);
                    deployWeapon(b.localToWorld(new Vec3d(0, 0, -1.5)), 1.5F);
                    weaponTo = locked;
                    catchChain();
                }
                if (t >= 50 && t < 70)
                    guideWeapon(
                            weaponFrom.addVector(0, 0, -BrawlerScore.smooth((t - 50) / 20D)), 0);
                if (t == 70) {
                    weaponLeg(locked);
                    if (weaponChain != null) weaponChain.setDead();
                }
                if (t >= 70 && t < 82)
                    guideWeapon(between(weaponFrom, weaponTo, (t - 70) / 12D), 11);
                if (t >= 74 && t < 96) dash(locked, 1, 0, false);
                if (t == 96) {
                    lock(target);
                    weaponLeg(locked);
                    hit(0, 8, false);
                    line(weaponFrom, locked, 18, 12);
                }
                if (t >= 96 && t < 114)
                    guideWeapon(between(weaponFrom, weaponTo, (t - 96) / 18D), 12);
                if (t == 114) wave(weaponTo, 7, 8);
                if (t == 138) {
                    catchChain();
                    weaponLeg(b.localToWorld(b.hand(1, 0)));
                }
                if (t >= 138 && t < 162)
                    guideWeapon(
                            between(weaponFrom, b.localToWorld(b.hand(1, 0)), (t - 138) / 24D), 0);
                if (t == 162) catchWeapon();
                break;
            case WAR_MACHINE:
                if (t == 14) anchor(4, 176);
                if (t >= 35 && t < 175) {
                    int leg = (t - 35) / 28;
                    Vec3d goal = leg == 3 ? center.addVector(0, 9, 0) : ringPoint(leg, 5, 10, 1);
                    dash(goal, 1.15, 7, false);
                    if ((t - 35) % 28 == 20) {
                        volley(b.localToWorld(b.hand(3, 0)), target.getPositionEyes(1), 5);
                        wave(b.getPositionVector(), 6, 7);
                    }
                }
                if (t == 180) {
                    breakAnchors();
                    lock(target);
                }
                if (t >= 196 && t < 210) dash(locked, 1.35, 18, true);
                if (t == 211 && b.parryCount() > 0) b.stagger(70);
                break;
            case RELAY:
                if (t == 20) {
                    lock(target);
                    deployWeapon(b.localToWorld(b.hand(1, 0)), 1.2F);
                    weaponTo = b.localToWorld(new Vec3d(-1, 1, 4));
                }
                if (t >= 20 && t < 40)
                    guideWeapon(between(weaponFrom, weaponTo, (t - 20) / 20D), 8);
                if (t == 40) {
                    hit(0, 8, false);
                    weaponLeg(center.addVector(5, 2, 0));
                }
                if (t >= 40 && t < 54)
                    guideWeapon(between(weaponFrom, weaponTo, (t - 40) / 14D), 10);
                if (t == 54) catchChain();
                if (t >= 54 && t < 110) {
                    double angle = (t - 54) * Math.PI * 2 / 28;
                    guideWeapon(center.addVector(Math.cos(angle) * 5, 2, Math.sin(angle) * 5), 9);
                    if (t >= 64 && t % 10 == 4 && weapon != null) {
                        effect(
                                        SHARD,
                                        b.localToWorld(b.hand(3, 0)),
                                        weapon.getPositionVector(),
                                        0,
                                        10,
                                        .3F,
                                        0)
                                .velocity(
                                        weapon.getPositionVector()
                                                .subtract(b.localToWorld(b.hand(3, 0)))
                                                .scale(.1));
                        volley(weapon.getPositionVector(), target.getPositionEyes(1), 2);
                    }
                }
                if (t == 110) {
                    lock(target);
                    weaponLeg(locked);
                    if (weaponChain != null) weaponChain.setDead();
                }
                if (t >= 110 && t < 124)
                    guideWeapon(between(weaponFrom, weaponTo, (t - 110) / 14D), 12);
                if (t == 124) weaponLeg(b.localToWorld(b.hand(1, 0)));
                if (t >= 124 && t < 138)
                    guideWeapon(
                            between(weaponFrom, b.localToWorld(b.hand(1, 0)), (t - 124) / 14D), 0);
                if (t == 138) catchWeapon();
                if (t >= 120 && t < 140) dash(locked, 1, 7, false);
                if (t == 142) hit(0, 13, true);
                break;
            case NO_NEUTRAL:
                if (t < 138) approach(target, .4, 3.5);
                int[] order = {0, 1, 2, 3, 0, 2, 1, 3};
                for (int n = 0; n < 8; n++)
                    if (t == 24 + n * 16) {
                        int arm = order[n];
                        if (arm < 2) hit(arm, 9, false);
                        else if (arm == 2)
                            effect(
                                    CHAIN,
                                    b.localToWorld(b.hand(2, 0)),
                                    target.getPositionEyes(1),
                                    7,
                                    13,
                                    .3F,
                                    8);
                        else volley(b.localToWorld(b.hand(3, 0)), target.getPositionEyes(1), 3);
                    }
                if (t == 138) b.stagger(30);
                break;
            case REMAINING_WEAPON:
                approach(target, .35, 4);
                if (t == 30 || t == 62)
                    for (int i = 0; i < 4; i++)
                        if (b.armHealth(i) > 0) {
                            if (i < 2) hit(i, 9, true);
                            else if (i == 2)
                                effect(
                                        CHAIN,
                                        b.localToWorld(b.hand(i, 0)),
                                        target.getPositionEyes(1),
                                        12,
                                        20,
                                        .3F,
                                        8);
                            else volley(b.localToWorld(b.hand(i, 0)), target.getPositionEyes(1), 4);
                        }
                break;
            case RAM:
                int beat = t % 40;
                if (beat == 12) {
                    lock(target);
                    effect(FAULT, b.getPositionVector(), locked, 8, 10, .04F, 0);
                }
                if (beat >= 20 && beat < 32)
                    dash(locked.add(locked.subtract(start).normalize().scale(4)), 1.3, 9, t >= 140);
                else orbit(t, 140, 9, 1);
                break;
            case HEXFIELD:
                if (t == 10)
                    for (int n = 0; n < 18; n++)
                        effect(HEX, ringPoint(n, 18, 9, 1), Vec3d.ZERO, 200, 205, 1, 0);
                if (t >= 32 && t < 176) {
                    int n = (t - 32) / 8;
                    Vec3d goal = ringPoint(n, 18, 9, 1);
                    dash(goal, 1.4, 7, false);
                    if ((t - 32) % 8 == 0)
                        effect(CHAIN, goal, ringPoint(n + 1, 18, 9, 1), 10, 30, .2F, 8);
                }
                if (t == 184)
                    for (int n = 0; n < 18; n++)
                        effect(
                                CHAIN,
                                ringPoint(n, 18, 9, 1),
                                ringPoint(n + 5, 18, 9, 1),
                                10,
                                17,
                                .18F,
                                10);
                break;
            case PHANTOMS:
                approach(target, .55, 3.5);
                if (t == 30) hit(0, 10, false);
                if (t == 64) hit(1, 10, false);
                if (t == 98) {
                    grab(target);
                    if (grabbed != null) {
                        hold(b.localToWorld(new Vec3d(0, 1, 3)));
                        release();
                    }
                }
                if (t == 132) wave(b.getPositionVector(), 8, 10);
                break;
            case RAIL:
                if (t == 12) {
                    lock(target);
                    Vec3d path = locked.subtract(start);
                    for (int n = 1; n <= 6; n++)
                        effect(HEX, start.add(path.scale(n / 6D)), Vec3d.ZERO, n * 5, 118, 1.8F, 0);
                }
                if (t >= 48 && t < 65)
                    dash(locked.add(locked.subtract(start).normalize().scale(6)), 1.65, 11, false);
                if (t >= 86 && t < 108) dash(start, 1.65, 14, true);
                break;
            case MINES:
                if (t == 14)
                    for (int n = 0; n < 12; n++)
                        field.add(
                                effect(
                                        HEX,
                                        ground(ringPoint(n, 12, 8, 0)),
                                        Vec3d.ZERO,
                                        202,
                                        205,
                                        .7F,
                                        0));
                if (t >= 36 && t < 180) {
                    int n = (t - 36) / 12;
                    Vec3d node = ringPoint(n, 12, 8, 0);
                    dash(node.addVector(0, 1, 0), 1, 5, false);
                    for (int i = 0; i < field.size(); i++) {
                        EntityBrawlerEffect mine = field.get(i);
                        if (!activated.contains(i) && b.getDistanceSq(mine) < 9) {
                            activated.add(i);
                            mine.setDead();
                            Vec3d at = mine.getPositionVector();
                            effect(HEX, at, Vec3d.ZERO, 14, 24, .9F, 5);
                            effect(CHAIN, at, target.getPositionEyes(1), 14, 22, .18F, 8);
                        }
                    }
                }
                break;
            case HEADBUTT:
                if (t == 18 || t == 44 || t == 68 || t == 92 || t == 116 || t == 140) lock(target);
                if (t >= 18 && t < 148) {
                    int beat2 = (t - 18) % 24;
                    if (beat2 < 10)
                        dash(locked.addVector(0, t >= 92 && t < 116 ? 4 : 0, 0), 1.1, 9, t >= 140);
                    else if (t < 116) orbit(t, 75, 5, 1);
                }
                if (t == 126) wave(b.getPositionVector(), 9, 10);
                break;
            case TESLA_GRAPPLE:
                if (t == 12) anchor(2, 140);
                if (t >= 28 && t < 132) {
                    orbit(t, 52, 8, 1.5);
                    if (t % 26 == 0) wave(b.getPositionVector(), 7, 7);
                }
                if (t == 136) {
                    lock(target);
                    breakAnchors();
                    effect(CHAIN, b.getPositionVector(), locked.addVector(3, 0, 3), 0, 28, .1F, 0);
                    effect(CHAIN, b.getPositionVector(), locked.addVector(-3, 0, 3), 0, 28, .1F, 0);
                }
                if (t >= 154 && t < 170) dash(locked, 1.5, 14, true);
                break;
            case RECALL:
                if (t == 10)
                    for (int n = 0; n < 12; n++)
                        effect(
                                RECALL,
                                ringPoint(n, 12, 14, 1),
                                b.getPositionVector(),
                                18,
                                116,
                                .5F,
                                5);
                if (t == 132) {
                    if (recallCount >= 7) b.reformShield();
                    else b.stagger(35);
                }
                break;
            case COMPRESSION:
                if (t == 10) center = target.getPositionVector().addVector(0, 1, 0);
                if (t == 20)
                    for (int row = -2; row <= 2; row++)
                        for (int n = 0; n < 10; n++) {
                            double latitude = row * Math.PI / 6,
                                    longitude = (n + (row % 2 == 0 ? 0 : .5)) * Math.PI / 5;
                            Vec3d radial =
                                    new Vec3d(
                                            Math.cos(latitude) * Math.cos(longitude),
                                            Math.sin(latitude),
                                            Math.cos(latitude) * Math.sin(longitude));
                            field.add(
                                    effect(
                                            SHELL,
                                            center.add(radial.scale(13)),
                                            center,
                                            15,
                                            176,
                                            1.3F,
                                            0));
                        }
                if (t >= 20 && t < 192)
                    for (int i = 0; i < field.size(); i++) {
                        int row = i / 10 - 2, n = i % 10;
                        double latitude = row * Math.PI / 6,
                                longitude = (n + (row % 2 == 0 ? 0 : .5)) * Math.PI / 5;
                        double r = 13 - 9 * BrawlerScore.smooth((t - 20) / 120D);
                        Vec3d radial =
                                new Vec3d(
                                        Math.cos(latitude) * Math.cos(longitude),
                                        Math.sin(latitude),
                                        Math.cos(latitude) * Math.sin(longitude));
                        EntityBrawlerEffect cell = field.get(i);
                        Vec3d at = center.add(radial.scale(r));
                        cell.endpoints(cell.getPositionVector(), center);
                        cell.guide(at, t >= 35 ? 8 : 0);
                    }
                if (t < 158) {
                    if (t % 30 == 16) lock(target);
                    if (t % 30 >= 20 && t % 30 < 28) dash(locked, .95, 8, false);
                }
                if (t == 162) lock(target);
                if (t >= 176 && t < 190) dash(locked, 1.2, 15, true);
                break;
            case MISSING_ARMS:
                approach(target, .65, 3.5);
                for (int n = 0; n < 4; n++) {
                    int at = 30 + n * 36;
                    if (t == at) {
                        if (n < 2) hit(n, 10, false);
                        if (n == 2)
                            effect(
                                    CHAIN,
                                    b.getPositionVector(),
                                    target.getPositionEyes(1),
                                    6,
                                    14,
                                    .3F,
                                    8);
                        if (n == 3) volley(b.getPositionVector(), target.getPositionEyes(1), 5);
                    }
                    if (t == at + 8) lock(target);
                    if (t > at + 8 && t < at + 17) dash(locked, .8, 6, false);
                }
                if (t == 166) hit(0, 10, false);
                if (t == 180) hit(1, 10, false);
                if (t == 194) {
                    wave(b.getPositionVector(), 11, 14);
                    b.stagger(28);
                }
                break;
            case BOXING:
                approach(target, .45, 3);
                if (t == 24 || t == 56) hit(0, 8, false);
                if (t == 40 || t == 72 || t == 90) hit(1, 10, false);
                if (t == 124) {
                    hit(0, 14, true);
                    hit(1, 14, true);
                }
                break;
            case CATCH_HANDS:
                approach(target, .65, 3);
                int[] beats = {26, 44, 60, 74, 86, 96, 105, 114};
                for (int n = 0; n < beats.length; n++)
                    if (t == beats[n]) {
                        hit(n % 2, 9, false);
                        b.combatMove(
                                new Vec3d(n % 2 == 0 ? .7 : -.7, 0, .3)
                                        .rotateYaw((float) Math.toRadians(-b.rotationYaw)));
                    }
                if (t == 122) b.stagger(42);
                break;
            case HAYMAKER:
                if (t < 40) approach(target, .18, 4);
                if (t == 46 || t == 70) lock(target);
                if (t >= 50 && t < 59 || t >= 74 && t < 83) dash(locked, .85, 0, false);
                if (t == 58) hit(0, 16, true);
                if (t == 82) {
                    hit(1, 18, true);
                    if (b.parryCount() >= 2) b.stagger(56);
                }
                break;
            case GRAPPLER:
                if (t < 34) approach(target, .4, 2.7);
                if (t == 34) grab(target);
                if (t > 34 && t < 96) holdBetweenHands();
                if (t == 64 || t == 90) slamHeld(5);
                if (t == 96) throwHeldUp();
                if (t > 98 && t < 124)
                    b.steer(target.getPositionVector().addVector(0, -1, -1), .85);
                if (t == 124) grab(target);
                if (t >= 124 && t < 148) {
                    if (t >= 130) b.combatMove(new Vec3d(0, -.55, 0));
                    holdBetweenHands();
                }
                if (t == 148) {
                    slamHeld(8);
                    release();
                    wave(b.getPositionVector(), 12, 10);
                }
                break;
            case GROUND_POUND:
                if (t == 18) lock(target);
                if (t >= 24 && t < 36) dash(locked, 1, 0, false);
                if (t == 34) grab(target);
                if (t > 34 && t < 112) hold(ground(b.getPositionVector().addVector(0, 0, 2)));
                if (t == 52 || t == 72 || t == 92) {
                    slamHeld(4);
                    wave(b.getPositionVector(), 4 + (t - 52) / 10, 5);
                }
                if (t == 106) {
                    release();
                    b.announce("DODGE OR PARRY");
                }
                if (t == 132) {
                    hit(0, 17, true);
                    wave(b.getPositionVector(), 10, 10);
                }
                break;
            case KNUCKLEQUAKE:
                if (t == 28 || t == 50 || t == 70 || t == 86 || t == 100 || t == 112) {
                    hit(t == 28 || t == 70 || t == 100 ? 0 : 1, 9, true);
                    if (b.clashTick() > 0) break;
                    Vec3d direction =
                            target.getPositionVector()
                                    .subtract(b.getPositionVector())
                                    .normalize()
                                    .rotateYaw((float) Math.sin(t) * .45F);
                    line(
                            b.getPositionVector(),
                            b.getPositionVector().add(direction.scale(15)),
                            136 - t,
                            11);
                }
                if (t == 136) wave(b.getPositionVector(), 6, 9);
                break;
            case CLAP:
                approach(target, .4, 3);
                if (t == 46) {
                    hit(0, 13, false);
                    hit(1, 13, false);
                    wave(b.localToWorld(new Vec3d(0, 0, 3)), 9, 7);
                }
                if (t > 62 && t < 82 && target.getDistanceSq(b) < 144) {
                    Vec3d pull =
                            b.getPositionVector()
                                    .subtract(target.getPositionVector())
                                    .normalize()
                                    .scale(.09);
                    target.addVelocity(pull.x, 0, pull.z);
                    target.velocityChanged = true;
                }
                if (t == 102) hit(1, 12, true);
                break;
            case LARIAT:
                if (t < 150) {
                    orbit(t, t < 60 ? 60 : t < 110 ? 45 : 32, 8, 1);
                    if (t % 8 == 0) hit(0, 8, false);
                    if (t == 132) wave(center, 10, 9);
                }
                if (t == 150) lock(target);
                if (t >= 164 && t < 181) dash(locked, 1.5, 16, true);
                break;
            case TWO_MAN:
                approach(target, .2, 3);
                if (t == 26 || t == 66 || t == 106) hit(0, 9, false);
                if (t == 46 || t == 86 || t == 126) hit(1, 10, false);
                if (t == 146) {
                    boolean a = b.strike(0, 14, 1.1, false), c = b.strike(1, 14, 1.1, false);
                    if (!a && !c) b.stagger(26);
                }
                break;
            case THROW_WORLD:
                if (t == 20) terrain = liftTerrain(b.localToWorld(new Vec3d(0, 0, 3)), 2.6F, 100);
                if (t >= 20 && t < 64 && terrain != null)
                    terrain.guide(
                            b.localToWorld(
                                    new Vec3d(
                                            0,
                                            -1.4 + 4.4 * BrawlerScore.smooth((t - 20) / 44D),
                                            2.5)),
                            0);
                if (t == 64) {
                    lock(target);
                    weaponFrom =
                            terrain == null
                                    ? b.localToWorld(new Vec3d(0, 3, 2))
                                    : terrain.getPositionVector();
                    weaponTo = locked;
                }
                if (t >= 64 && t < 86 && terrain != null)
                    terrain.guide(between(weaponFrom, weaponTo, (t - 64) / 30D), 8);
                if (t == 86) {
                    Vec3d impact =
                            terrain == null
                                    ? b.localToWorld(new Vec3d(0, 2, 5))
                                    : terrain.getPositionVector();
                    if (terrain != null) terrain.setDead();
                    debris(impact, target.getPositionEyes(1));
                    lock(target);
                }
                if (t >= 94 && t < 116) dash(locked, 1.1, 13, true);
                break;
            case COUNTER:
                if (counterAt >= 0) {
                    if (t == counterAt - 6) lock(target);
                    if (t > counterAt - 6 && t < counterAt) dash(locked, .8, 0, false);
                    if (t == counterAt) hit(1, 15, true);
                } else approach(target, .16, 4);
                break;
            case ONE_TWO:
                approach(target, .5, 3);
                int[] punches = {28, 48, 76, 92, 106, 118};
                for (int n = 0; n < 6; n++) if (t == punches[n]) hit(n % 2, 10, true);
                if (t == 132) {
                    b.strike(0, 15, 1, false);
                    b.strike(1, 15, 1, false);
                }
                break;
            case PRIMITIVE:
                if (t < 104 || t >= 112 && t < 216) approach(target, .5, 3);
                if (t == 24 || t == 60) hit(0, 9, false);
                if (t == 42 || t == 122) hit(1, 10, false);
                if (t == 76) lock(target);
                if (t >= 80 && t < 91) dash(locked, .9, 8, false);
                if (t == 104) grab(target);
                if (t == 110) release();
                if (t == 146) line(b.getPositionVector(), target.getPositionVector(), 12, 10);
                if (t >= 164 && t < 184) orbit(t, 40, 5, 1);
                if (t == 188) hit(0, 14, true);
                if (t == 208) hit(1, 16, true);
                if (t == 220) terrain = liftTerrain(b.localToWorld(new Vec3d(0, 0, 3)), 2.5F, 70);
                if (t >= 220 && t < 236 && terrain != null)
                    terrain.guide(
                            b.localToWorld(
                                    new Vec3d(0, -1 + 4 * BrawlerScore.smooth((t - 220) / 16D), 3)),
                            0);
                if (t == 236) {
                    weaponFrom =
                            terrain == null
                                    ? b.localToWorld(new Vec3d(0, 3, 2))
                                    : terrain.getPositionVector();
                    weaponTo = target.getPositionEyes(1);
                }
                if (t >= 236 && t < 260 && terrain != null)
                    terrain.guide(between(weaponFrom, weaponTo, (t - 236) / 30D), 8);
                if (t == 260) {
                    Vec3d impact =
                            terrain == null
                                    ? b.localToWorld(new Vec3d(0, 2, 4))
                                    : terrain.getPositionVector();
                    if (terrain != null) terrain.setDead();
                    debris(impact, target.getPositionEyes(1));
                }
                if (t == 276) grab(target);
                if (t >= 277 && t < 290) holdBetweenHands();
                if (t == 290) throwHeldUp();
                if (t > 290 && t < 310)
                    b.steer(target.getPositionVector().addVector(0, -1, -1), .95);
                if (t == 310) grab(target);
                if (t >= 310 && t < 336) {
                    if (t >= 312) b.combatMove(new Vec3d(0, -.4, 0));
                    holdBetweenHands();
                }
                if (t == 340) {
                    slamHeld(9);
                    release();
                    wave(b.getPositionVector(), 13, 12);
                    b.stagger(50);
                }
                break;
        }
        if (t >= p.duration) {
            reset();
            b.advancePattern();
        }
    }
}
