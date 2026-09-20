package com.exoarsenal.entity;

import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.*;
import static com.exoarsenal.entity.WulfrumCoordinationScore.Pattern;

abstract class WulfrumPairCombat implements WulfrumCoordinationCombat {
    protected final Pattern pattern;
    protected final WulfrumCoordinationAnimator animator;
    protected final boolean expert;
    protected int elapsedTicks, hitstopTicks, parryCount, lastParryTick = -100;
    protected EntityExcavator excavator;
    protected EntityBrawler brawler;
    protected EntityWulfrumEye seer, observer;
    protected EntityPlayer player;
    protected final Vec3d center;
    protected Vec3d aim;
    protected final Vec3d[] marks = new Vec3d[12];
    protected final List<Entity> effects = new ArrayList<>();
    protected final List<EntityWulfrumShard> shards = new ArrayList<>();
    protected final List<EntityExcavatorProbe> probes = new ArrayList<>();
    protected EntityExcavatorPayload platform;
    protected int rallyStage, rallyTick;
    protected Vec3d rallyGoal = Vec3d.ZERO, rallyDirection = new Vec3d(0, 0, 1);
    protected boolean completed;
    protected final List<EntityBrawlerEffect> artillery = new ArrayList<>();
    protected final List<EntityExcavatorPayload> faultWarnings = new ArrayList<>();
    protected final List<Vec3d[]> faults = new ArrayList<>();
    protected final Set<Integer> clipped = new HashSet<>(), redirected = new HashSet<>();
    protected WulfrumTetherPhysics pendulum;
    protected final double[] probeCableLengths = new double[3];
    protected boolean collisionResolved, formationBroken;
    protected int formationBreakTick;
    protected Vec3d safeAxis = new Vec3d(0, 0, 1), safeCenter = Vec3d.ZERO, gap = Vec3d.ZERO;
    protected int exchangeStage, exchangeTick, exchangeLeg;
    protected Vec3d launchDirection = new Vec3d(0, 0, 1), catchPoint = Vec3d.ZERO, handPosition;
    protected final int[] probeStage = new int[20], probeAge = new int[20];
    protected int replyTick = -1, punishTick = -1;
    protected EntityLivingBase reflected;
    protected final Vec3d[] probeTargets = new Vec3d[20];
    protected final List<EntityBrawlerEffect> cagePanels = new ArrayList<>();
    protected Vec3d cageVelocity = new Vec3d(1, 0, 0), cageDent = new Vec3d(1, 0, 0);
    protected double cageStrength;

    protected WulfrumPairCombat(
            List<EntityLivingBase> actors, EntityPlayer p, boolean expert, Pattern pattern) {
        this.pattern = pattern;
        this.expert = expert;
        player = p;
        center = p.getPositionVector();
        aim = p.getPositionEyes(1);
        Arrays.fill(marks, aim);
        for (EntityLivingBase e : actors) {
            if (e instanceof EntityExcavator) excavator = (EntityExcavator) e;
            else if (e instanceof EntityBrawler) brawler = (EntityBrawler) e;
            else if (e instanceof EntityWulfrumEye) {
                if (((EntityWulfrumEye) e).seer()) seer = (EntityWulfrumEye) e;
                else observer = (EntityWulfrumEye) e;
            }
        }
        animator = new WulfrumCoordinationAnimator(excavator, brawler, seer, observer);
    }

    public boolean tick(EntityPlayer p) {
        player = p;
        if (completed) return true;
        if (hitstopTicks > 0) {
            hitstopTicks--;
            return false;
        }
        if (++elapsedTicks > pattern.duration) return true;
        if (elapsedTicks % 40 == 1) aim = p.getPositionEyes(1);
        if (excavator != null) excavator.coordinationParry(false);
        if (brawler != null
                && (brawler.liveArms() & WulfrumCoordinationScore.arms(pattern))
                        != WulfrumCoordinationScore.arms(pattern)) return true;
        animator.apply(pattern, elapsedTicks, aim, exchangeLeg, exchangeTick);
        executePattern();
        return false;
    }

    public void finish() {
        for (Entity e : effects) if (!e.isDead) e.setDead();
        for (EntityExcavatorProbe p : probes)
            if (!p.isDead) {
                p.releaseCoordination();
                p.recall();
            }
        if (brawler != null) {
            brawler.clearCoordinationHands();
            if (brawler.phase() == 2) brawler.engineMask(1);
        }
        if (excavator != null) {
            excavator.coordinationParry(false);
            excavator.coordinationStall(false);
            excavator.coordinationPose(-1, 0);
        }
        if (seer != null) seer.coordinationPose(-1, 0);
        if (observer != null) observer.coordinationPose(-1, 0);
    }

    public void parried(EntityLivingBase actor, EntityPlayer p) {
        if (elapsedTicks - lastParryTick < 8) return;
        lastParryTick = elapsedTicks;
        parryCount++;
        hitstopTicks = 8;
        aim = p.getPositionEyes(1);
        if (pattern.phaseTwo) {
            reflected = actor;
            replyTick = elapsedTicks;
            launchDirection = actor.getPositionVector().subtract(p.getPositionEyes(1)).normalize();
            impact(actor.getPositionVector());
        }
        if (actor == excavator
                && (pattern == Pattern.attack_7
                        || pattern == Pattern.attack_8
                        || pattern == Pattern.attack_12)) {
            rallyStage = 2;
            rallyTick = 0;
        }
        if (excavator != null) excavator.debris(actor.getPositionVector(), 8);
    }

    protected Vec3d orbit(double a, double r, double y) {
        return center.addVector(Math.cos(a) * r, y, Math.sin(a) * r);
    }

    static double segmentDistance(Vec3d p, Vec3d a, Vec3d z) {
        Vec3d d = z.subtract(a);
        double f =
                MathHelper.clamp(
                        p.subtract(a).dotProduct(d) / Math.max(.000001, d.lengthSquared()), 0, 1);
        return p.distanceTo(a.add(d.scale(f)));
    }

    protected void move(EntityLivingBase e, Vec3d at, double speed) {
        if (e == null) return;
        Vec3d d = at.subtract(e.getPositionVector());
        double cap = speed * (expert ? 1.15 : 1);
        if (d.lengthVector() > cap) d = d.normalize().scale(cap);
        e.motionX = e.motionY = e.motionZ = 0;
        e.setPosition(e.posX + d.x, e.posY + d.y, e.posZ + d.z);
        e.velocityChanged = true;
        if (d.lengthSquared() > .01) {
            e.rotationYaw =
                    (float)
                            Math.toDegrees(
                                    Math.atan2(e instanceof EntityExcavator ? d.x : -d.x, d.z));
            e.rotationPitch = (float) -Math.toDegrees(Math.atan2(d.y, Math.hypot(d.x, d.z)));
            e.renderYawOffset = e.rotationYaw;
        }
    }

    protected void spawn(Entity e) {
        effects.add(e);
        e.world.spawnEntity(e);
    }

    protected void ray(Vec3d from, Vec3d to, int warning, float damage) {
        for (EntityExcavatorProbe probe : probes)
            if (!probe.isDead && probe.getPositionVector().squareDistanceTo(from) < 1)
                probe.coordinatedShot(warning);
        if (observer != null)
            spawn(new EntityWulfrumRay(observer.world, observer, from, to, warning, 10, damage));
        else excavator.beam(from, to, warning, 10, damage);
    }

    protected void impact(Vec3d point) {
        net.minecraft.world.WorldServer world = (net.minecraft.world.WorldServer) player.world;
        world.spawnParticle(
                net.minecraft.util.EnumParticleTypes.FIREWORKS_SPARK,
                point.x,
                point.y,
                point.z,
                18,
                .35,
                .35,
                .35,
                .18);
        world.spawnParticle(
                net.minecraft.util.EnumParticleTypes.CRIT_MAGIC,
                point.x,
                point.y,
                point.z,
                12,
                .6,
                .6,
                .6,
                .1);
        world.playSound(
                null,
                point.x,
                point.y,
                point.z,
                net.minecraft.init.SoundEvents.BLOCK_ANVIL_LAND,
                net.minecraft.util.SoundCategory.HOSTILE,
                .7F,
                1.35F);
    }

    protected void chain(Vec3d a, Vec3d z) {
        if (observer != null
                && seer != null
                && a.squareDistanceTo(observer.pupil()) < 1
                && z.squareDistanceTo(seer.pupil()) < 1) {
            observer.coordinationChain(true);
            return;
        }
        if (elapsedTicks % 3 != 0) return;
        if (brawler != null)
            spawn(new EntityBrawlerEffect(brawler, EntityBrawlerEffect.CHAIN, a, z, 0, 3, .12F, 0));
        else
            spawn(
                    new EntityExcavatorPayload(
                            excavator, EntityExcavatorPayload.TETHER, a, z, 0, 3, .12F, 0));
    }

    protected EntityWulfrumShard shard(Vec3d at, int delay) {
        EntityWulfrumShard x = new EntityWulfrumShard(seer, at, Vec3d.ZERO, 0, delay, player, 6);
        spawn(x);
        shards.add(x);
        return x;
    }

    protected void burst(Vec3d at, int count) {
        for (int n = 0; n < count; n++) {
            double a = n * Math.PI * 2 / count;
            shard(at.addVector(Math.cos(a), Math.sin(a * 2), Math.sin(a)), 15 + n / 3 * 8);
        }
    }

    protected void bore(Vec3d goal) {
        Vec3d from = excavator.getPositionVector();
        double speed =
                pattern == Pattern.attack_46 ? 4.2 + parryCount * .9 : pattern.phaseTwo ? 4.8 : 3.7;
        move(excavator, goal, speed);
        excavator.coordinationParry(true);
        if (elapsedTicks - lastParryTick < 9) return;
        Vec3d to = excavator.getPositionVector();
        for (EntityPlayer p :
                excavator.world.getEntitiesWithinAABB(
                        EntityPlayer.class, new AxisAlignedBB(from, to).grow(2)))
            if (!p.isCreative() && !p.isSpectator()) {
                AxisAlignedBB box = p.getEntityBoundingBox().grow(1.2);
                if (box.contains(to) || box.calculateIntercept(from, to) != null)
                    p.attackEntityFrom(DamageSource.causeMobDamage(excavator), 12);
            }
    }

    protected void deploy(int count) {
        if (!probes.isEmpty()) return;
        for (int n = 0; n < count; n++) {
            EntityExcavatorProbe p = new EntityExcavatorProbe(excavator, n);
            Vec3d at = excavator.segment(n % 18, 1);
            p.setPosition(at.x, at.y, at.z);
            excavator.world.spawnEntity(p);
            probes.add(p);
        }
    }

    protected void place(int n, Vec3d at, boolean blades) {
        if (n < probes.size() && !probes.get(n).isDead) probes.get(n).coordinate(at, blades);
    }

    protected boolean alive(int n) {
        return n < probes.size() && !probes.get(n).isDead;
    }

    protected abstract void executePattern();

    static WulfrumPairCombat create(
            List<EntityLivingBase> actors, EntityPlayer player, boolean expert, Pattern pattern) {
        return pattern.phaseTwo
                ? new WulfrumPhaseTwoPairCombat(actors, player, expert, pattern)
                : new WulfrumPhaseOnePairCombat(actors, player, expert, pattern);
    }
}
