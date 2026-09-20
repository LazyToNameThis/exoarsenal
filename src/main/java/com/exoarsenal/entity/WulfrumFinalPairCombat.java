package com.exoarsenal.entity;

import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.WorldServer;
import static com.exoarsenal.entity.WulfrumFinalPairScore.Pattern;
import static com.exoarsenal.entity.BrawlerMartialScore.Attack;

abstract class WulfrumFinalPairCombat implements WulfrumCoordinationCombat {
    protected final Pattern pattern;
    protected final EntityBrawler brawler;
    protected final EntityExcavator excavator;
    protected final EntityWulfrumEye eye;
    protected EntityPlayer player, capturedPlayer;
    protected final Vec3d center;
    protected Vec3d aim, launch = Vec3d.ZERO, velocity = Vec3d.ZERO;
    protected final Vec3d[] marks = new Vec3d[24], tips = new Vec3d[6];
    protected final EntityWulfrumAppendage[] sockets = new EntityWulfrumAppendage[6];
    protected final EntityWulfrumRay[] cannonRays = new EntityWulfrumRay[6];
    protected final Vec3d[] cannonTargets = new Vec3d[6];
    protected EntityWulfrumRay reflectedRay;
    protected final EntityWulfrumEcho[] echoes = new EntityWulfrumEcho[6];
    protected final Vec3d[] historicalTargets = new Vec3d[6];
    protected final Vec3d[] probeRecoil = {
        Vec3d.ZERO, Vec3d.ZERO, Vec3d.ZERO, Vec3d.ZERO, Vec3d.ZERO, Vec3d.ZERO
    };
    protected final List<Entity> hazards = new ArrayList<>();
    protected final List<EntityExcavatorProbe> probes = new ArrayList<>();
    protected final List<Vec3d> history = new ArrayList<>();
    protected final List<EntityWulfrumRay> cuts = new ArrayList<>();
    protected final List<Vec3d[]> cutLines = new ArrayList<>();
    protected final List<EntityExcavatorPayload> platforms = new ArrayList<>();
    protected EntityExcavatorPayload platform;
    protected int elapsedTicks,
            hitstop,
            parryCount,
            lastParryTick = -100,
            replyStartTick = -1,
            stage,
            stageTick,
            punishStartTick = -1,
            disabledCannonCount;
    protected boolean complete;

    protected WulfrumFinalPairCombat(
            List<EntityLivingBase> actors, EntityPlayer target, Pattern chosen) {
        EntityBrawler selectedBrawler = null;
        EntityExcavator selectedExcavator = null;
        EntityWulfrumEye selectedEye = null;
        for (EntityLivingBase actor : actors) {
            if (actor instanceof EntityBrawler) selectedBrawler = (EntityBrawler) actor;
            if (actor instanceof EntityExcavator) selectedExcavator = (EntityExcavator) actor;
            if (actor instanceof EntityWulfrumEye && ((EntityWulfrumEye) actor).overclocked())
                selectedEye = (EntityWulfrumEye) actor;
        }
        brawler = selectedBrawler;
        excavator = selectedExcavator;
        eye = selectedEye;
        player = target;
        pattern = chosen;
        center = target.getPositionVector();
        aim = target.getPositionEyes(1);
        Arrays.fill(marks, aim);
        Arrays.fill(tips, aim);
        if (brawler != null) brawler.beginMartialCoordination();
        if (eye != null) {
            eye.beginSurvivorCoordination();
            eye.coordinationChain(false);
            for (int i = 0; i < 6; i++) sockets[i] = eye.coordinationSocket(i);
        }
    }

    public boolean tick(EntityPlayer target) {
        player = target;
        if (complete) return true;
        for (Entity hazard : hazards)
            if (hazard instanceof EntityWulfrumRay)
                ((EntityWulfrumRay) hazard).coordinationPaused(hitstop > 0);
        if (hitstop > 0) {
            hitstop--;
            return false;
        }
        if (++elapsedTicks > pattern.duration && punishStartTick < 0) return true;
        hazards.removeIf(e -> e.isDead);
        if (brawler != null) {
            brawler.clearCoordinationHands();
            brawler.martialReaction(0, 0);
            brawler.coordinationMartial(Attack.FOOTWORK, 0);
            face(brawler, player.getPositionEyes(1));
        }
        if (excavator != null) excavator.coordinationParry(false);
        if (eye != null) {
            eye.coordinationPose(eye.seer() ? 35 : 45, elapsedTicks);
            eye.look(player.getPositionEyes(1));
        }
        if (punishStartTick >= 0) {
            recover();
            return complete;
        }
        executePattern();
        if (eye != null) {
            history.add(eye.pupil());
            if (history.size() > 240) history.remove(0);
        }
        return complete;
    }

    public void finish() {
        release();
        for (Entity hazard : hazards) if (!hazard.isDead) hazard.setDead();
        for (EntityExcavatorProbe p : probes)
            if (p.isEntityAlive()) {
                p.releaseCoordination();
                p.recall();
            }
        if (brawler != null) brawler.endMartialCoordination();
        if (excavator != null) {
            excavator.coordinationParry(false);
            excavator.coordinationStall(false);
            excavator.coordinationPose(-1, 0);
            com.exoarsenal.world.ExcavatorTerrain.get(player.world)
                    .release(excavator.getUniqueID());
        }
        if (eye != null) {
            for (EntityWulfrumAppendage socket : sockets)
                if (socket != null && !socket.isDead) {
                    socket.tether(eye);
                    socket.pose(eye.pupil(), eye.pupil().addVector(0, 0, 1), false);
                }
            eye.coordinationPose(-1, 0);
        }
    }

    public void parried(EntityLivingBase actor, EntityPlayer p) {
        if (elapsedTicks - lastParryTick < 8) return;
        lastParryTick = elapsedTicks;
        parryCount++;
        replyStartTick = elapsedTicks;
        hitstop = 8;
        release();
        impact(actor.getPositionVector().addVector(0, 2, 0));
        if (pattern == Pattern.attack_10 && !cuts.isEmpty()) {
            int i = Math.min(parryCount - 1, cuts.size() - 1);
            cuts.get(i).setDead();
            Vec3d[] line = cutLines.get(i);
            ray(line[0], line[1], 0, 8, 6);
            if (brawler != null)
                move(brawler, line[0].add(line[1]).scale(.5).addVector(0, -1, 0), 2);
        }
        if (pattern == Pattern.attack_20)
            disabledCannonCount = Math.min(6, disabledCannonCount + 1);
        if (actor == excavator) {
            stage = 2;
            stageTick = 0;
            launch = brawler == null ? center : brawler.getPositionVector().addVector(0, 3, 0);
        }
        if (actor == brawler
                && (pattern == Pattern.attack_14
                        || pattern == Pattern.attack_15
                        || pattern == Pattern.attack_47
                        || pattern == Pattern.attack_50)) {
            stage = 8;
            stageTick = 0;
            launch = excavator != null ? excavator.getPositionVector() : eye.pupil();
        }
        if (actor == brawler
                && (pattern == Pattern.attack_2
                        || pattern == Pattern.attack_7
                        || pattern == Pattern.attack_13))
            velocity =
                    p.getPositionVector()
                            .subtract(brawler.getPositionVector())
                            .normalize()
                            .scale(-2);
    }

    protected Vec3d ring(int i, double radius, double y) {
        return circle(i * Math.PI / 3, radius, y);
    }

    protected Vec3d circle(double angle, double radius, double y) {
        return center.addVector(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
    }

    protected Vec3d mix(Vec3d a, Vec3d z, double f) {
        return a.add(z.subtract(a).scale(MathHelper.clamp(f, 0, 1)));
    }

    protected Vec3d floor(Vec3d p) {
        BlockPos at = new BlockPos(p);
        for (int i = 0;
                i < 80 && !player.world.getBlockState(at.down()).getMaterial().isSolid();
                i++) at = at.down();
        for (int i = 0; i < 12 && player.world.getBlockState(at).getMaterial().isSolid(); i++)
            at = at.up();
        return new Vec3d(p.x, at.getY() + .05, p.z);
    }

    protected void face(EntityLivingBase actor, Vec3d point) {
        Vec3d d = point.subtract(actor.getPositionVector());
        actor.rotationYaw =
                (float) Math.toDegrees(Math.atan2(actor == excavator ? d.x : -d.x, d.z));
        actor.renderYawOffset = actor.rotationYaw;
    }

    protected void move(EntityLivingBase actor, Vec3d destination, double speed) {
        Vec3d d = destination.subtract(actor.getPositionVector());
        if (d.lengthVector() > speed) d = d.normalize().scale(speed);
        actor.motionX = actor.motionY = actor.motionZ = 0;
        if (actor == brawler) actor.move(MoverType.SELF, d.x, d.y, d.z);
        else actor.setPosition(actor.posX + d.x, actor.posY + d.y, actor.posZ + d.z);
        actor.velocityChanged = true;
        if (actor == excavator && d.lengthSquared() > .01) {
            face(actor, destination);
            actor.rotationPitch = (float) -Math.toDegrees(Math.atan2(d.y, Math.hypot(d.x, d.z)));
        }
    }

    protected void approach(double radius, double side, double speed) {
        Vec3d forward = player.getPositionVector().subtract(brawler.getPositionVector());
        forward = new Vec3d(forward.x, 0, forward.z).normalize();
        Vec3d lateral = new Vec3d(forward.z, 0, -forward.x);
        move(
                brawler,
                floor(
                        player.getPositionVector()
                                .subtract(forward.scale(radius))
                                .add(lateral.scale(side))),
                speed);
    }

    protected void spawn(Entity e) {
        hazards.add(e);
        e.world.spawnEntity(e);
    }

    protected void impact(Vec3d p) {
        ((WorldServer) player.world)
                .spawnParticle(
                        EnumParticleTypes.FIREWORKS_SPARK, p.x, p.y, p.z, 16, .35, .4, .35, .18);
        player.world.playSound(
                null,
                p.x,
                p.y,
                p.z,
                net.minecraft.init.SoundEvents.BLOCK_ANVIL_LAND,
                SoundCategory.HOSTILE,
                .8F,
                .8F);
    }

    protected EntityWulfrumRay ray(Vec3d from, Vec3d to, int warning, int life, float damage) {
        if (eye == null) {
            excavator.beam(from, to, warning, life, damage);
            return null;
        }
        EntityWulfrumRay ray =
                new EntityWulfrumRay(
                        eye.world,
                        eye,
                        from,
                        to,
                        warning * WulfrumCombatClock.STEPS,
                        life * WulfrumCombatClock.STEPS,
                        damage);
        spawn(ray);
        return ray;
    }

    protected void cannon(int i, Vec3d to, int warning, float damage) {
        if (i < disabledCannonCount || sockets[i] == null || sockets[i].isDead) return;
        sockets[i].pose(tips[i], to, false);
        sockets[i].charge(warning * WulfrumCombatClock.STEPS, 10 * WulfrumCombatClock.STEPS);
        cannonTargets[i] = to;
        cannonRays[i] =
                ray(
                                tips[i].add(to.subtract(tips[i]).normalize().scale(.6)),
                                to,
                                warning,
                                10,
                                damage)
                        .followOrigin(sockets[i], to);
    }

    protected void socket(int i, Vec3d at, boolean hot) {
        tips[i] = at;
        if (sockets[i] == null || sockets[i].isDead) return;
        Vec3d target =
                cannonRays[i] != null && !cannonRays[i].isDead
                        ? cannonTargets[i]
                        : player.getPositionEyes(1);
        sockets[i].pose(at, target, hot);
    }

    protected void stroke(int i, Vec3d from, Vec3d to, int local, int duration) {
        double f = BrawlerScore.smooth(local / (double) duration);
        socket(i, mix(from, to, f), local >= 0 && local <= duration);
    }

    protected void bladeReplay(Vec3d a, Vec3d z, int delay) {
        spawn(EntityWulfrumAppendage.replay(eye, a, z, delay * WulfrumCombatClock.STEPS));
    }

    protected void slash(Vec3d at, double speed) {
        move(eye, at, speed);
        eye.look(at.addVector(0, 1, 0));
        eye.slash(9);
    }

    protected void combo(Attack attack, int local, boolean damage) {
        brawler.coordinationMartial(attack, local);
        if (!damage || elapsedTicks - lastParryTick < 9) return;
        for (BrawlerMartialScore.Beat beat : BrawlerMartialScore.beats(attack))
            if (local == beat.tick) {
                if (beat.limb == 5) {
                    brawler.martialStrike(0, 12, 1.7, beat.parry);
                    if (elapsedTicks != lastParryTick)
                        brawler.martialStrike(1, 12, 1.7, beat.parry);
                } else
                    brawler.martialStrike(
                            beat.limb,
                            beat.motion == BrawlerMartialScore.Motion.JAB ? 8 : 11,
                            1.7,
                            beat.parry);
            }
    }

    protected void hand(int side, Vec3d at, float grip) {
        brawler.coordinationHand(side, at, grip);
    }

    protected void chargeDrill(Vec3d goal, double speed, boolean parry) {
        Vec3d from = excavator.drillTip();
        move(excavator, goal, speed);
        Vec3d to = excavator.drillTip();
        excavator.coordinationParry(parry);
        if (elapsedTicks - lastParryTick < 12) return;
        for (EntityPlayer p :
                player.world.getEntitiesWithinAABB(
                        EntityPlayer.class, new AxisAlignedBB(from, to).grow(2)))
            if (!p.isCreative()
                    && !p.isSpectator()
                    && (p.getEntityBoundingBox().grow(1.8).contains(to)
                            || p.getEntityBoundingBox().grow(1.8).calculateIntercept(from, to)
                                    != null)) {
                p.attackEntityFrom(DamageSource.causeMobDamage(excavator), 12);
                if (elapsedTicks == lastParryTick) break;
            }
    }

    protected void warning(Vec3d a, Vec3d z, int delay) {
        ray(a, z, delay, 2, 0);
    }

    protected void shock(Vec3d p, float radius) {
        if (excavator != null) {
            excavator.shock(floor(p), Math.round(radius));
            excavator.debris(floor(p), 10);
        } else
            spawn(
                    new EntityBrawlerEffect(
                            brawler,
                            EntityBrawlerEffect.RING,
                            floor(p),
                            Vec3d.ZERO,
                            6,
                            30,
                            radius,
                            9));
    }

    protected void deploy(int count) {
        if (!probes.isEmpty()) return;
        for (int i = 0; i < count; i++) probes.add(excavator.coordinationProbe(i));
    }

    protected boolean probeAlive(int i) {
        return i < probes.size() && probes.get(i).isEntityAlive();
    }

    protected void probe(int i, Vec3d at, boolean blades) {
        if (probeAlive(i)) probes.get(i).coordinate(at, blades);
    }

    protected void probeShot(int i, Vec3d to, int warning) {
        if (!probeAlive(i)) return;
        probes.get(i).coordinatedShot(warning);
        ray(probes.get(i).getPositionVector(), to, warning, 10, 7);
    }

    protected void platform(int radius) {
        if (platform != null) return;
        platform =
                new EntityExcavatorPayload(
                        excavator,
                        EntityExcavatorPayload.CHUNK,
                        floor(center).addVector(0, -1.2, 0),
                        Vec3d.ZERO,
                        0,
                        pattern.duration + 30,
                        radius,
                        0);
        spawn(platform);
    }

    protected void release() {
        if (capturedPlayer != null) {
            capturedPlayer.fallDistance = 0;
            capturedPlayer.velocityChanged = true;
        }
        capturedPlayer = null;
        if (brawler != null) brawler.martialVictim(-1);
    }

    protected void grab() {
        if (player.isCreative()
                || player.isSpectator()
                || com.exoarsenal.combat.WeaponCombat.dodgeReady(player)
                || player.getDistanceSq(brawler) > 36) return;
        capturedPlayer = player;
        brawler.martialVictim(player.getEntityId());
    }

    protected void holdPlayer() {
        if (capturedPlayer == null) return;
        if (!capturedPlayer.isEntityAlive()
                || capturedPlayer.isSpectator()
                || capturedPlayer != player
                || capturedPlayer.getDistanceSq(brawler) > 1600) {
            release();
            return;
        }
        Vec3d
                at =
                        brawler.localToWorld(brawler.hand(0, 0).add(brawler.hand(1, 0)).scale(.5))
                                .addVector(0, -.7, 0),
                delta = at.subtract(capturedPlayer.getPositionVector());
        int count = Math.max(1, (int) Math.ceil(delta.lengthVector() / .4));
        for (int i = 1; i <= count; i++)
            if (!player.world
                    .getCollisionBoxes(
                            capturedPlayer,
                            capturedPlayer
                                    .getEntityBoundingBox()
                                    .offset(delta.scale(i / (double) count)))
                    .isEmpty()) {
                release();
                return;
            }
        capturedPlayer.motionX = capturedPlayer.motionY = capturedPlayer.motionZ = 0;
        capturedPlayer.fallDistance = 0;
        if (capturedPlayer instanceof EntityPlayerMP)
            ((EntityPlayerMP) capturedPlayer)
                    .connection.setPlayerLocation(
                            at.x,
                            at.y,
                            at.z,
                            capturedPlayer.rotationYaw,
                            capturedPlayer.rotationPitch);
        else capturedPlayer.setPosition(at.x, at.y, at.z);
    }

    protected void throwPlayer(Vec3d motion) {
        if (capturedPlayer == null) return;
        EntityPlayer p = capturedPlayer;
        release();
        p.addVelocity(motion.x, motion.y, motion.z);
        p.velocityChanged = true;
    }

    protected void recover() {
        int age = elapsedTicks - punishStartTick;
        if (brawler != null) {
            brawler.martialReaction(3, Math.min(100, 35 + age));
            move(brawler, floor(brawler.getPositionVector()), .4);
        }
        if (excavator != null) excavator.coordinationStall(true);
        if (eye != null) move(eye, eye.getPositionVector().addVector(0, -.03, 0), .03);
        if (age >= 75) complete = true;
    }

    protected void storedCuts(int count, int resolve) {
        if (!cuts.isEmpty()) return;
        for (int i = 0; i < count; i++) {
            double a = i * Math.PI / count;
            Vec3d from = circle(a, 18, 1 + (i % 3)), to = circle(a + Math.PI, 18, 1 + (i % 3));
            cutLines.add(new Vec3d[] {from, to});
            cuts.add(ray(from, to, resolve - elapsedTicks, 12, 8));
        }
    }

    protected abstract void executePattern();

    static WulfrumFinalPairCombat create(
            List<EntityLivingBase> actors, EntityPlayer player, Pattern pattern) {
        switch (pattern.pair) {
            case 0:
                return new SeerBrawlerFinalCombat(actors, player, pattern);
            case 1:
                return new ObserverBrawlerFinalCombat(actors, player, pattern);
            case 2:
                return new SeerExcavatorFinalCombat(actors, player, pattern);
            case 3:
                return new ObserverExcavatorFinalCombat(actors, player, pattern);
            case 4:
                return new ExcavatorBrawlerFinalCombat(actors, player, pattern);
            default:
                throw new IllegalStateException("Unimplemented final pairing");
        }
    }
}
