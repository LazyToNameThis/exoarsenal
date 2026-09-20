package com.exoarsenal.entity;

import java.util.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import static com.exoarsenal.entity.EntityBrawlerEffect.*;
import static com.exoarsenal.entity.BrawlerLimiterScore.Attack;

final class BrawlerLimiterCombat {
    private final EntityBrawler b;
    private final List<EntityBrawlerEffect> objects = new ArrayList<>(),
            cells = new ArrayList<>(),
            tethers = new ArrayList<>(),
            echoes = new ArrayList<>();
    private final List<Vec3d> history = new ArrayList<>(), route = new ArrayList<>();
    private Vec3d center = Vec3d.ZERO, locked = Vec3d.ZERO, recoil = Vec3d.ZERO;
    private int pause, recovery, lastParry = -100;
    private boolean crashed;

    BrawlerLimiterCombat(EntityBrawler boss) {
        b = boss;
    }

    void reset() {
        for (EntityBrawlerEffect e : objects) e.setDead();
        objects.clear();
        cells.clear();
        tethers.clear();
        echoes.clear();
        history.clear();
        route.clear();
        pause = recovery = 0;
        lastParry = -100;
        crashed = false;
        b.aerialRecovery(0);
    }

    private Attack attack() {
        return BrawlerLimiterScore.decode(b.limiterAttack());
    }

    private Vec3d point(double angle, double r, double y) {
        return center.addVector(Math.cos(angle) * r, y, Math.sin(angle) * r);
    }

    private Vec3d ground(Vec3d p) {
        BlockPos at = new BlockPos(p);
        for (int n = 0; n < 100 && !b.world.getBlockState(at.down()).getMaterial().isSolid(); n++)
            at = at.down();
        for (int n = 0; n < 24 && b.world.getBlockState(at).getMaterial().isSolid(); n++)
            at = at.up();
        return new Vec3d(p.x, at.getY() + .1, p.z);
    }

    private EntityBrawlerEffect fx(
            int type, Vec3d at, Vec3d end, int wait, int life, float size, float damage) {
        EntityBrawlerEffect e = new EntityBrawlerEffect(b, type, at, end, wait, life, size, damage);
        objects.add(e);
        b.world.spawnEntity(e);
        return e;
    }

    private void fly(Vec3d at, double speed) {
        b.steer(at, speed);
    }

    private void lock(EntityLivingBase p, int warning) {
        locked = p.getPositionEyes(1);
        fx(CHAIN, b.getPositionVector(), locked, warning, warning + 1, .025F, 0);
    }

    private void ram(boolean parry, float damage) {
        if (b.clock() > lastParry + 7) b.ram(damage, parry);
    }

    private void pass(int t, int begin, int hit, EntityLivingBase p, boolean parry, double speed) {
        if (t == begin) lock(p, hit - begin);
        if (t >= begin && t <= hit + 4) {
            fly(locked, speed);
            if (t >= hit - 3) ram(parry, parry ? 17 : 11);
        }
    }

    private void wave(Vec3d at, float r, float damage) {
        fx(RING, ground(at), Vec3d.ZERO, 4, 24, r, damage);
        for (int n = 0; n < 6; n++)
            fx(
                    HEX,
                    ground(at)
                            .addVector(
                                    Math.cos(n * Math.PI / 3) * r * .6,
                                    .1,
                                    Math.sin(n * Math.PI / 3) * r * .6),
                    Vec3d.ZERO,
                    7 + n,
                    20 + n,
                    1.7F,
                    damage * .5F);
    }

    private void shards(Vec3d from, Vec3d to, int count) {
        Vec3d d = to.subtract(from).normalize();
        for (int n = 0; n < count; n++)
            fx(SHARD, from, to, 5, 55, .4F, 7)
                    .launch(d.rotateYaw((float) ((n - (count - 1) * .5) * .17)).scale(1.3), 7);
    }

    private void cell(Vec3d at, int wait, int life) {
        cells.add(fx(SHELL, at, center, wait, life, 1.6F, 8));
    }

    private void trace(int t, int start, int end, int trigger) {
        if (t >= start && t <= end && (t - start) % 4 == 0)
            cell(b.getPositionVector(), Math.max(6, trigger - t), trigger - t + 22);
    }

    private void anchors() {
        for (EntityBrawlerEffect e : tethers) e.setDead();
        tethers.clear();
        for (int s : new int[] {-1, 1}) {
            Vec3d at = center.addVector(s * 7, 9, 0);
            fx(HEX, at, Vec3d.ZERO, 0, 40, 1.6F, 0);
            tethers.add(fx(CHAIN, b.getPositionVector(), at, 0, 40, .14F, 0));
        }
    }

    boolean paused() {
        return pause > 0 || recovery > 0;
    }

    void parried(EntityPlayer player) {
        lastParry = b.clock();
        Attack a = attack();
        pause = a == Attack.REACTOR_RAM || a == Attack.FIVE_POINT_IMPACT ? 12 : 6;
        recoil = b.getPositionVector().subtract(player.getPositionVector()).normalize().scale(1.25);
        if (a == Attack.TESLA_METEOR)
            recoil = player.getLookVec().crossProduct(new Vec3d(0, 1, 0)).normalize().scale(2);
        if (a == Attack.FIVE_POINT_IMPACT && b.clock() < 222) return;
        recovery =
                a == Attack.FIVE_POINT_IMPACT
                        ? 90
                        : a == Attack.REACTOR_RAM ? 70 : a == Attack.TERMINAL_CHASE ? 60 : 32;
        crashed = a == Attack.FIVE_POINT_IMPACT || a == Attack.CONTROLLED_CRASH;
        b.aerialRecovery(recovery);
        if (a == Attack.TESLA_SLINGSHOT)
            for (EntityBrawlerEffect tether : tethers)
                fx(CHAIN, tether.end(), b.getPositionVector(), 0, 16, .3F, 0);
    }

    void recover() {
        if (pause > 0) {
            pause--;
            b.motionX = b.motionY = b.motionZ = 0;
            return;
        }
        b.aerialRecovery(recovery);
        if (recovery > 0) {
            int left = --recovery;
            if (crashed) {
                Vec3d floor = ground(b.getPositionVector());
                if (b.posY > floor.y + .3)
                    b.combatMove(new Vec3d(recoil.x * .15, -1.2, recoil.z * .15));
                else if (left % 20 == 0) wave(floor, 10, 0);
            } else
                b.combatMove(
                        recoil.rotateYaw((float) ((32 - left) * .08))
                                .scale(Math.min(1, left / 20D)));
            if (left == 0) {
                reset();
                b.advanceLimiter();
            }
        }
    }

    void tick(EntityLivingBase player) {
        int t = b.clock();
        Attack a = attack();
        if (t == 1) {
            reset();
            center = player.getPositionVector();
            locked = player.getPositionEyes(1);
        }
        objects.removeIf(e -> e.isDead);
        history.add(b.getPositionVector());
        if (history.size() > 120) history.remove(0);
        b.engineMask(BrawlerLimiterScore.engines(a, t));
        switch (a) {
            case SUPERSONIC_INTERCEPT:
                for (int n = 0; n < 4; n++) {
                    int base = n * 34;
                    if (t >= base && t < base + 15)
                        fly(
                                n == 2
                                        ? center.addVector(0, 32, 0)
                                        : point(n * Math.PI * .7, 32, 7 + n * 2),
                                3);
                    pass(t, base + 16, base + 26, player, n == 3, 3.6 + n * .2);
                }
                break;
            case HEX_AFTERBURNER:
                for (int n = 0; n < 4; n++) {
                    int base = n * 26;
                    Vec3d at =
                            n == 0
                                    ? center.addVector(22, 4, 0)
                                    : n == 1
                                            ? center.addVector(-18, 16, 8)
                                            : n == 2
                                                    ? center.addVector(0, 28, 0)
                                                    : center.addVector(19, 3, -12);
                    if (t >= base && t < base + 24) fly(at, 3.5);
                    trace(t, base, base + 24, 112 + n * 8);
                }
                if (t >= 108) fly(point(t * .15, 14, 9 + Math.sin(t * .2) * 6), 3.6);
                break;
            case FIVE_ENGINE_VECTORING:
                if (t < 24) fly(center.addVector(22, 4, 0), 3);
                else if (t < 46) fly(center.addVector(22, 26, 0), 3.5);
                else if (t < 68) fly(center.addVector(-18, 3, 12), 3.6);
                else if (t < 94) fly(center.addVector(0, 8, -24), 3.8);
                pass(t, 98, 112, player, true, 4);
                break;
            case TESLA_METEOR:
                for (int n = 0; n < 3; n++) {
                    int base = n * 52;
                    if (t >= base && t < base + 20)
                        fly(center.addVector(n % 2 == 0 ? 0 : 7, 36, 0), 3);
                    if (t == base + 20)
                        fx(RING, ground(player.getPositionVector()), Vec3d.ZERO, 18, 24, 7, 0);
                    pass(t, base + 22, base + 38, player, n == 2, 4.1);
                    if (t == base + 42 && !paused()) wave(b.getPositionVector(), 13 + n * 3, 12);
                }
                break;
            case SHIELD_SHRAPNEL:
                if (t == 10)
                    for (int n = 0; n < 24; n++)
                        cell(
                                b.getPositionVector()
                                        .addVector(
                                                Math.cos(n) * 3,
                                                Math.sin(n * .7) * 3,
                                                Math.sin(n) * 3),
                                120,
                                150);
                if (t >= 12 && t < 92) {
                    fly(point(t * .1, 17, 7 + Math.sin(t * .07) * 9), 3.1);
                    int peel = (t - 12) / 3;
                    for (int n = Math.max(0, peel); n < cells.size(); n++)
                        cells.get(n)
                                .guide(
                                        b.getPositionVector()
                                                .addVector(
                                                        Math.cos(n + t * .1) * 3,
                                                        Math.sin(n * .7) * 3,
                                                        Math.sin(n + t * .1) * 3),
                                        0);
                }
                if (t == 100)
                    for (EntityBrawlerEffect c : cells) {
                        EntityBrawlerEffect nearest = null;
                        double distance = Double.MAX_VALUE;
                        for (EntityBrawlerEffect other : cells)
                            if (other != c && c.getDistanceSq(other) < distance) {
                                distance = c.getDistanceSq(other);
                                nearest = other;
                            }
                        if (nearest != null)
                            fx(
                                    CHAIN,
                                    c.getPositionVector(),
                                    nearest.getPositionVector(),
                                    10,
                                    34,
                                    .2F,
                                    11);
                    }
                if (t >= 102) fly(point(-t * .13, 12, 8), 3.5);
                break;
            case JETWASH:
                for (int n = 0; n < 3; n++) {
                    int base = n * 30;
                    if (t == base + 1) {
                        locked = player.getPositionVector().add(player.getLookVec().scale(6));
                    }
                    if (t >= base && t < base + 25)
                        fly(locked.addVector(n % 2 == 0 ? 3 : -3, 1, 0), 3);
                    if (t == base + 20 && player.getDistanceSq(b) < 100) {
                        Vec3d push =
                                player.getPositionVector()
                                        .subtract(b.getPositionVector())
                                        .normalize();
                        player.addVelocity(push.x * 1.15, .18, push.z * 1.15);
                        player.velocityChanged = true;
                        wave(b.getPositionVector(), 7, 4);
                    }
                }
                if (t >= 94 && t < 134) {
                    fly(
                            player.getPositionVector()
                                    .subtract(player.getLookVec().scale(6))
                                    .addVector(0, 1, 0),
                            2.7);
                    Vec3d to = player.getPositionEyes(1).subtract(b.getPositionVector());
                    if (t % 6 == 0)
                        fx(
                                JETWASH,
                                b.getPositionVector(),
                                b.getPositionVector().add(to.normalize().scale(11)),
                                0,
                                7,
                                2.5F,
                                7);
                }
                break;
            case ORBITAL_DECAY:
                if (t < 112) {
                    double radius = 22 - 18 * Math.min(1, t / 112D);
                    fly(point(t * Math.PI / 14, radius, 4), 3.7);
                    if (t > 0 && t % 28 == 0)
                        fx(RING, center, Vec3d.ZERO, 40, 80, (float) radius, 7);
                }
                if (t == 112) {
                    for (int n = 0; n < 12; n++)
                        shards(point(n * Math.PI / 6, 18, 4), center.addVector(0, 1, 0), 2);
                }
                pass(t, 124, 140, player, false, 4);
                break;
            case THRUSTER_FEINT:
                if (t < 24) fly(point(0, 24, 6), 2.8);
                if (t == 24) lock(player, 12);
                if (t >= 24 && t < 43) fly(locked.addVector(8, 1, 0), 3.7);
                if (t >= 43 && t < 66)
                    fly(
                            player.getPositionVector()
                                    .subtract(player.getLookVec().scale(14))
                                    .addVector(0, 5, 0),
                            3.5);
                pass(t, 70, 88, player, true, 4.2);
                break;
            case TESLA_SLINGSHOT:
                for (int n = 0; n < 3; n++) {
                    int base = n * 52;
                    if (t == base + 4) {
                        center = player.getPositionVector();
                        anchors();
                    }
                    if (t >= base + 4 && t < base + 23) {
                        fly(center.addVector(0, 8, -26), 2.5);
                        for (EntityBrawlerEffect tether : tethers)
                            tether.endpoints(b.getPositionVector(), tether.end());
                    }
                    pass(t, base + 24, base + 38, player, n == 2, 4.5);
                    if (t == base + 42) for (EntityBrawlerEffect tether : tethers) tether.setDead();
                }
                break;
            case TERMINAL_CHASE:
                if (t < 140) {
                    Vec3d lead =
                            player.getPositionEyes(1)
                                    .addVector(player.motionX * 8, 0, player.motionZ * 8);
                    fly(lead, .85 + t * .017);
                    ram(false, 8);
                }
                pass(t, 140, 154, player, true, 4.2);
                break;
            case HEX_SINGULARITY:
                if (t == 12)
                    for (int row = -2; row <= 2; row++)
                        for (int n = 0; n < 8; n++) {
                            double lat = row * Math.PI / 6, lon = n * Math.PI / 4;
                            cell(
                                    center.addVector(
                                            Math.cos(lat) * Math.cos(lon) * 12,
                                            3 + Math.sin(lat) * 12,
                                            Math.cos(lat) * Math.sin(lon) * 12),
                                    20,
                                    150);
                        }
                if (t >= 12 && t < 130) {
                    double radius = 12 - 7 * (t - 12) / 118D;
                    for (EntityBrawlerEffect cell : cells)
                        if (!cell.isDead) {
                            Vec3d direction =
                                    cell.getPositionVector()
                                            .subtract(center.addVector(0, 3, 0))
                                            .normalize();
                            cell.guide(center.addVector(0, 3, 0).add(direction.scale(radius)), 7);
                            if (cell.getDistanceSq(b) < 16) cell.setDead();
                        }
                    fly(point(t * .1, 14, 4 + Math.sin(t * .08) * 10), 3);
                    if (t % 28 >= 18) fly(center.addVector(0, 3, 0), 3.8);
                }
                if (t == 130) lock(player, 10);
                if (t >= 130 && t < 151) fly(locked.addVector(0, 0, 20), 4);
                if (t == 151) for (EntityBrawlerEffect cell : cells) cell.setDead();
                break;
            case CONTROLLED_CRASH:
                for (int n = 0; n < 3; n++) {
                    int base = n * 30;
                    if (t == base + 1) locked = ground(point(n * 2.1, 17, 0));
                    if (t >= base + 1 && t < base + 20) fly(locked, 3.5);
                    if (t == base + 20) {
                        wave(locked, 8, 8);
                        shards(locked, player.getPositionEyes(1), 7);
                    }
                    if (t >= base + 20 && t < base + 30) fly(locked.addVector(0, 13, 0), 3.8);
                }
                pass(t, 104, 122, player, true, 4);
                break;
            case PHANTOM_BRAWLER:
                if (t == 1)
                    for (int n = 0; n < 4; n++)
                        echoes.add(fx(HEAD_ECHO, b.getPositionVector(), Vec3d.ZERO, 0, 145, 1, 0));
                if (t < 98) {
                    fly(point((t / 20) * 1.8, 19, 4 + (t / 20 % 3) * 7), 3.6);
                    ram(false, 10);
                }
                for (int n = 0; n < echoes.size(); n++) {
                    int sample = Math.max(0, history.size() - 1 - (n + 1) * 7);
                    echoes.get(n).guide(history.get(sample), 0);
                }
                if (t == 112)
                    for (EntityBrawlerEffect echo : echoes) {
                        wave(echo.getPositionVector(), 8, 10);
                        shards(echo.getPositionVector(), player.getPositionEyes(1), 5);
                    }
                break;
            case REACTOR_RAM:
                for (int n = 0; n < 3; n++) {
                    int base = n * 30;
                    if (t >= base && t < base + 12) fly(point(n * 2.1, 25, 7 + n * 3), 3);
                    pass(t, base + 12, base + 24, player, false, 3.2 + n * .3);
                    if (t == base + 26) wave(b.getPositionVector(), 7 + n * 3, 7);
                }
                if (t >= 92 && t < 120) fly(center.addVector(0, 32, 0), 3.2);
                if (t >= 120 && t < 132) {
                    b.motionX = b.motionY = b.motionZ = 0;
                    b.engineMask(0);
                }
                pass(t, 132, 144, player, false, 4);
                pass(t, 145, 156, player, true, 4.5);
                break;
            case FIVE_POINT_IMPACT:
                if (t < 18) fly(center.addVector(0, 20, 0), 2.5);
                if (t >= 18 && t < 100) {
                    int leg = (t - 18) / 20;
                    Vec3d destination =
                            leg == 0
                                    ? center.addVector(22, 8, 0)
                                    : leg == 1
                                            ? center.addVector(22, 30, 0)
                                            : leg == 2
                                                    ? center.addVector(-20, 3, 12)
                                                    : point((t - 78) * .18, 14, 10);
                    fly(destination, 3.5);
                    if (t % 4 == 0) {
                        route.add(b.getPositionVector());
                        cell(b.getPositionVector(), 112 - t, 160 - t);
                    }
                }
                if (t >= 108 && t < 152 && !route.isEmpty()) {
                    int n = Math.min(route.size() - 1, (t - 108) * route.size() / 44);
                    fly(route.get(n), 4.4);
                }
                pass(t, 154, 168, player, true, 4.4);
                pass(t, 174, 186, player, true, 4.4);
                pass(t, 192, 204, player, true, 4.4);
                pass(t, 212, 226, player, true, 4.7);
                break;
        }
        if (!paused() && t >= a.duration) {
            reset();
            b.advanceLimiter();
        }
    }
}
