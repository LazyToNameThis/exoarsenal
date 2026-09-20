package com.exoarsenal.entity;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.*;
import java.util.UUID;

public abstract class EntityFrigidRobot extends EntityMob implements IAnimatable {
    public enum Kind {
        DRONE,
        AMPLIFIER,
        SHIELDER,
        ROVER,
        ARTHROPOD
    }

    private static final DataParameter<Integer>
            ATTACK = EntityDataManager.createKey(EntityFrigidRobot.class, DataSerializers.VARINT),
            TICK = EntityDataManager.createKey(EntityFrigidRobot.class, DataSerializers.VARINT);
    private static final DataParameter<Float> SHIELD =
            EntityDataManager.createKey(EntityFrigidRobot.class, DataSerializers.FLOAT);
    private static final DataParameter<Boolean> BOOST =
            EntityDataManager.createKey(EntityFrigidRobot.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float>
            AIM_X = EntityDataManager.createKey(EntityFrigidRobot.class, DataSerializers.FLOAT),
            AIM_Y = EntityDataManager.createKey(EntityFrigidRobot.class, DataSerializers.FLOAT),
            AIM_Z = EntityDataManager.createKey(EntityFrigidRobot.class, DataSerializers.FLOAT);
    private static final UUID BOOST_ID = UUID.fromString("18d76f34-62cb-4902-a0d5-e746f87cb775");
    private final AnimationFactory factory = new AnimationFactory(this);
    private int cooldown = 35, cursor, lastHurt;
    private boolean expert, modeInitialized;
    private boolean leapImpacted;

    public boolean isExpert() {
        return expert;
    }

    private long boostUntil;
    private Vec3d aim = Vec3d.ZERO;

    public abstract Kind kind();

    public EntityFrigidRobot(World world) {
        super(world);
        setSize(
                kind() == Kind.ARTHROPOD ? 2.8F : kind() == Kind.DRONE ? 1.3F : 1.15F,
                kind() == Kind.ARTHROPOD ? 2.5F : kind() == Kind.DRONE ? 1.1F : 2.2F);
        experienceValue = kind() == Kind.ARTHROPOD ? 35 : 8;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(ATTACK, 0);
        dataManager.register(TICK, 0);
        dataManager.register(SHIELD, kind() == Kind.ROVER ? 20F : 0F);
        dataManager.register(BOOST, false);
        dataManager.register(AIM_X, 0F);
        dataManager.register(AIM_Y, 0F);
        dataManager.register(AIM_Z, 0F);
    }

    public Vec3d visualAim() {
        return new Vec3d(dataManager.get(AIM_X), dataManager.get(AIM_Y), dataManager.get(AIM_Z));
    }

    private void syncAim() {
        dataManager.set(AIM_X, (float) aim.x);
        dataManager.set(AIM_Y, (float) aim.y);
        dataManager.set(AIM_Z, (float) aim.z);
    }

    public int attack() {
        return dataManager.get(ATTACK);
    }

    public int attackTick() {
        return dataManager.get(TICK);
    }

    public float shield() {
        return dataManager.get(SHIELD);
    }

    public boolean amplified() {
        return dataManager.get(BOOST);
    }

    public boolean phaseTwo() {
        return kind() == Kind.ARTHROPOD && getHealth() <= getMaxHealth() * .5F;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        double hp =
                kind() == Kind.SHIELDER
                        ? 50
                        : kind() == Kind.ARTHROPOD
                                ? 160
                                : kind() == Kind.ROVER ? 36 : kind() == Kind.AMPLIFIER ? 30 : 22;
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(hp);
        getEntityAttribute(SharedMonsterAttributes.ARMOR)
                .setBaseValue(kind() == Kind.SHIELDER ? 12 : kind() == Kind.ARTHROPOD ? 8 : 4);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
                .setBaseValue(kind() == Kind.SHIELDER ? .18 : .25);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(36);
        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE)
                .setBaseValue(kind() == Kind.SHIELDER ? .8 : .35);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(5, new EntityAIWanderAvoidWater(this, .6));
        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 24));
        targetTasks.addTask(0, new EntityAIHurtByTarget(this, false));
        targetTasks.addTask(
                1, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (world.isRemote || !isEntityAlive()) return;
        if (getEntityData().hasUniqueId("ScoutWaveOwner")
                && com.exoarsenal.world.ScoutEncounterLedger.get(world)
                        .isClosed(getEntityData().getUniqueId("ScoutWaveOwner"))) {
            setDead();
            return;
        }
        if (!modeInitialized) {
            modeInitialized = true;
            expert = com.exoarsenal.world.ExoArsenalWorldSettings.get(world).isExpert();
            if (expert) {
                float fraction = getHealth() / getMaxHealth();
                getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)
                        .setBaseValue(
                                getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)
                                                .getBaseValue()
                                        * 1.5);
                getEntityAttribute(SharedMonsterAttributes.ARMOR)
                        .setBaseValue(
                                getEntityAttribute(SharedMonsterAttributes.ARMOR).getBaseValue()
                                        * 1.15);
                setHealth(getMaxHealth() * fraction);
            }
        }
        if (kind() == Kind.DRONE) setNoGravity(true);
        if (kind() == Kind.ROVER && ticksExisted - lastHurt > 100 && ticksExisted % 20 == 0)
            dataManager.set(SHIELD, Math.min(20, shield() + 2));
        if (amplified() && world.getTotalWorldTime() > boostUntil) {
            dataManager.set(BOOST, false);
            getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).removeModifier(BOOST_ID);
            setHealth(Math.min(getHealth(), getMaxHealth()));
        }
        if (amplified() && ticksExisted % 40 == 0) heal(1);
        if (kind() == Kind.AMPLIFIER && ticksExisted % 30 == 0) {
            for (EntityFrigidRobot robot :
                    world.getEntitiesWithinAABB(
                            EntityFrigidRobot.class, getEntityBoundingBox().grow(14)))
                if (robot != this && robot.isEntityAlive()) robot.boost();
            for (EntityX20Scout scout :
                    world.getEntitiesWithinAABB(
                            EntityX20Scout.class, getEntityBoundingBox().grow(14)))
                if (scout.isEntityAlive()) scout.amplify();
            ring(14, EnumParticleTypes.END_ROD);
        }
        EntityLivingBase target = getAttackTarget();
        if (target != null
                && getEntityData().hasUniqueId("ScoutWaveOwner")
                && !isWithinHomeDistanceFromPosition(target.getPosition())) {
            setAttackTarget(null);
            getNavigator()
                    .tryMoveToXYZ(
                            getHomePosition().getX(),
                            getHomePosition().getY(),
                            getHomePosition().getZ(),
                            1);
            target = null;
        }
        if (target == null || !target.isEntityAlive()) {
            if (kind() == Kind.DRONE) motionY *= .7;
            return;
        }
        getLookHelper().setLookPositionWithEntity(target, 20, 20);
        if (kind() == Kind.DRONE) {
            setNoGravity(true);
            double desired = target.posY + 3.5;
            motionY = MathHelper.clamp((desired - posY) * .05, -.2, .2);
            Vec3d delta = target.getPositionVector().subtract(getPositionVector());
            double side = Math.sin(ticksExisted * .035);
            Vec3d travel = new Vec3d(delta.x, 0, delta.z).normalize();
            double approach = delta.lengthVector() > 12 ? .15 : delta.lengthVector() < 7 ? -.12 : 0;
            motionX = travel.x * approach - travel.z * side * .13;
            motionZ = travel.z * approach + travel.x * side * .13;
        }
        if (attack() == 0) {
            if (kind() != Kind.DRONE)
                getNavigator().tryMoveToEntityLiving(target, kind() == Kind.AMPLIFIER ? .65 : 1);
            if (--cooldown <= 0
                    && canEntityBeSeen(target)
                    && getDistanceSq(target)
                            < (kind() == Kind.DRONE ? 400 : kind() == Kind.AMPLIFIER ? 100 : 100)) {
                int count =
                        kind() == Kind.SHIELDER
                                ? 3
                                : kind() == Kind.ARTHROPOD
                                        ? (phaseTwo() ? 6 : 4)
                                        : kind() == Kind.AMPLIFIER ? 1 : 2;
                dataManager.set(
                        ATTACK,
                        kind() == Kind.ARTHROPOD
                                ? ArthropodPattern.at(phaseTwo(), cursor++)
                                : 1 + Math.floorMod(cursor++, count));
                dataManager.set(TICK, 0);
                aim = target.getPositionVector().addVector(0, target.height * .5, 0);
                getNavigator().clearPath();
                leapImpacted = false;
                syncAim();
                world.playSound(
                        null,
                        posX,
                        posY,
                        posZ,
                        SoundEvents.BLOCK_PISTON_EXTEND,
                        SoundCategory.HOSTILE,
                        .65F,
                        .7F);
            }
            return;
        }
        int t = attackTick() + 1;
        dataManager.set(TICK, t);
        if (kind() != Kind.DRONE) {
            motionX *= .5;
            motionZ *= .5;
        }
        int a = attack();
        if (t < 12) aim = target.getPositionVector().addVector(0, target.height * .5, 0);
        syncAim();
        if (kind() == Kind.DRONE) {
            if (t == 18 || a == 2 && (t == 24 || t == 30)) beam(5, 24);
            if (amplified() && t == 36) beam(4, 24);
        } else if (kind() == Kind.AMPLIFIER) {
            if (t == 24) {
                strike(5, 3, .45);
                ring(5, EnumParticleTypes.SNOWBALL);
            }
        } else if (kind() == Kind.SHIELDER) {
            if (a == 1 && t == 20) strike(3.2, 4, .5);
            if (a == 2 && t >= 18 && t <= 28) {
                dash(.4);
                if (t == 22) strike(3.5, 5, .8);
            }
            if (a == 3 && t == 28) {
                strike(5, 3, .35);
                ring(5, EnumParticleTypes.SNOWBALL);
            }
            if (amplified() && t == 36) beam(4, 14);
        } else if (kind() == Kind.ROVER) {
            if (a == 1 && (t == 18 || t == 26)) beam(6, 20);
            if (a == 2 && t >= 16 && t <= 26) {
                dash(.55);
                if (t == 22) strike(3.5, 7, .5);
            }
            if (amplified() && t == 34) ringDamage(5, 4);
        } else {
            if (a == 1 && (t == 22 || t == 38 || phaseTwo() && t == 54))
                pincerStrike(5, 10, t == 38 ? -1 : 1, .6);
            if (a == 2 && (t == 28 || phaseTwo() && (t == 40 || t == 52)))
                ringDamage(t == 28 ? 5 : t == 40 ? 7 : 9, t == 28 ? 12 : 6);
            if (a == 3 && t >= 24 && t <= 38) {
                dash(.7);
                if (t == 30) pincerStrike(5, 14, 0, 1);
            }
            if (a == 4 && (t == 22 || t == 30 || t == 38)) {
                Vec3d saved = aim,
                        delta = aim.subtract(getPositionVector()),
                        side = new Vec3d(-delta.z, 0, delta.x).normalize();
                aim = aim.add(side.scale((t - 30) * .35));
                beam(8, 28);
                aim = saved;
            }
            if (a == 5) {
                if (t == 12) {
                    motionY = .8;
                    dash(.45);
                    velocityChanged = true;
                }
                if (t > 12 && t < 22 && !onGround) dash(.35);
                if (t > 16 && onGround && !leapImpacted) {
                    leapImpacted = true;
                    ringDamage(9, 15);
                    t = 28;
                    dataManager.set(TICK, t);
                }
            }
            if (a == 6 && t >= 20 && t <= 56) {
                if (t % 8 == 4) aim = target.getPositionVector().addVector(0, 1, 0);
                if (t % 8 == 0) beam(6, 28);
            }
            if (a == 7 && t == 32) {
                pincerStrike(6, 18, 0, .9);
                ring(3, EnumParticleTypes.SNOWBALL);
            }
            if (a == 8 && t >= 24 && t <= 56) {
                renderYawOffset += 18;
                rotationYaw = renderYawOffset;
                if (t % 8 == 0) {
                    pincerStrike(5.5, 7, t % 16 == 0 ? 1 : -1, .55);
                    ring(5, EnumParticleTypes.SNOWBALL);
                }
            }
            if (a == 9) {
                if (t == 28) pincerStrike(6, 8, 0, -.7);
                if (t == 48) {
                    pincerStrike(4.5, 20, 0, .9);
                    ring(4, EnumParticleTypes.SNOWBALL);
                }
            }
        }
        if (t >= (kind() == Kind.ARTHROPOD ? ArthropodPattern.duration(a) : 48)
                && (kind() != Kind.ARTHROPOD || a != 5 || leapImpacted || t >= 120)) {
            dataManager.set(ATTACK, 0);
            dataManager.set(TICK, 0);
            cooldown = phaseTwo() ? 18 : amplified() ? 18 : 32;
        }
    }

    private void pincerStrike(double range, float damage, int side, double knock) {
        Vec3d forward = aim.subtract(getPositionVector());
        forward = new Vec3d(forward.x, 0, forward.z).normalize();
        if (kind() == Kind.ARTHROPOD && attack() == 8) {
            double yaw = Math.toRadians(renderYawOffset);
            forward = new Vec3d(-Math.sin(yaw), 0, Math.cos(yaw));
        }
        for (EntityPlayer p :
                world.getEntitiesWithinAABB(
                        EntityPlayer.class, getEntityBoundingBox().grow(range, 2, range))) {
            Vec3d delta = p.getPositionVector().subtract(getPositionVector()),
                    flat = new Vec3d(delta.x, 0, delta.z);
            if (p.isCreative()
                    || p.isSpectator()
                    || flat.lengthSquared() > range * range
                    || flat.normalize().dotProduct(forward) < .35
                    || !canEntityBeSeen(p)) continue;
            double lateral = flat.x * forward.z - flat.z * forward.x;
            if (side != 0 && lateral * side < -.7) continue;
            if (p.attackEntityFrom(DamageSource.causeMobDamage(this), output(damage))) {
                Vec3d push = flat.normalize();
                p.addVelocity(push.x * knock, .15, push.z * knock);
            }
        }
        world.playSound(
                null,
                posX,
                posY,
                posZ,
                SoundEvents.ENTITY_IRONGOLEM_ATTACK,
                SoundCategory.HOSTILE,
                .8F,
                .7F);
    }

    private void boost() {
        boostUntil = world.getTotalWorldTime() + 50;
        dataManager.set(BOOST, true);
        if (getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getModifier(BOOST_ID) == null)
            getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)
                    .applyModifier(new AttributeModifier(BOOST_ID, "Frigid amplifier", .25, 1));
    }

    private void dash(double speed) {
        Vec3d d = aim.subtract(getPositionVector()).normalize();
        motionX = d.x * speed;
        motionZ = d.z * speed;
        velocityChanged = true;
    }

    private float output(float damage) {
        return damage * (amplified() ? 1.25F : 1);
    }

    private void strike(double radius, float damage, double knock) {
        for (EntityPlayer p :
                world.getEntitiesWithinAABB(
                        EntityPlayer.class, getEntityBoundingBox().grow(radius, 2, radius)))
            if (!p.isCreative()
                    && !p.isSpectator()
                    && getDistanceSq(p) <= radius * radius
                    && canEntityBeSeen(p)) {
                p.attackEntityFrom(DamageSource.causeMobDamage(this), output(damage));
                Vec3d d = p.getPositionVector().subtract(getPositionVector()).normalize();
                p.addVelocity(d.x * knock, .2, d.z * knock);
            }
    }

    private void ringDamage(double radius, float damage) {
        ring(radius, EnumParticleTypes.SNOWBALL);
        strike(radius, damage, .5);
        com.exoarsenal.network.PacketScoutImpact.send(
                this, getPositionVector().addVector(0, .2, 0), 1);
    }

    private void ring(double radius, EnumParticleTypes particle) {
        if (world instanceof WorldServer)
            for (int i = 0; i < 32; i++) {
                double a = i * Math.PI / 16;
                ((WorldServer) world)
                        .spawnParticle(
                                particle,
                                posX + Math.cos(a) * radius,
                                posY + .3,
                                posZ + Math.sin(a) * radius,
                                1,
                                0D,
                                .08D,
                                0D,
                                0D,
                                new int[0]);
            }
    }

    private void beam(float damage, double range) {
        Vec3d from = getPositionVector().addVector(0, height * .7, 0),
                to = from.add(aim.subtract(from).normalize().scale(range));
        RayTraceResult wall = world.rayTraceBlocks(from, to, false, true, false);
        if (wall != null) to = wall.hitVec;
        EntityPlayer closest = null;
        double distance = from.distanceTo(to);
        for (EntityPlayer p :
                world.getEntitiesWithinAABB(
                        EntityPlayer.class, new AxisAlignedBB(from, to).grow(.3)))
            if (!p.isCreative() && !p.isSpectator()) {
                RayTraceResult hit =
                        p.getEntityBoundingBox().grow(.15).calculateIntercept(from, to);
                if (hit != null && from.distanceTo(hit.hitVec) < distance) {
                    distance = from.distanceTo(hit.hitVec);
                    closest = p;
                }
            }
        if (closest != null)
            closest.attackEntityFrom(
                    DamageSource.causeMobDamage(this).setProjectile(), output(damage));
        com.exoarsenal.network.PacketScoutImpact.send(
                this, from.add(aim.subtract(from).normalize().scale(distance)), 2);
        if (world instanceof WorldServer)
            for (int i = 0; i < 20; i++) {
                Vec3d p = from.add(to.subtract(from).scale(i / 19D));
                ((WorldServer) world)
                        .spawnParticle(
                                EnumParticleTypes.END_ROD,
                                p.x,
                                p.y,
                                p.z,
                                1,
                                0D,
                                0D,
                                0D,
                                0D,
                                new int[0]);
            }
        world.playSound(
                null,
                posX,
                posY,
                posZ,
                SoundEvents.ENTITY_BLAZE_SHOOT,
                SoundCategory.HOSTILE,
                .7F,
                1.5F);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float damage) {
        if (source.getTrueSource() instanceof EntityFrigidRobot) return false;
        if (!world.isRemote) {
            lastHurt = ticksExisted;
            if (shield() > 0) {
                float absorbed = Math.min(shield(), damage);
                dataManager.set(SHIELD, shield() - absorbed);
                damage -= absorbed;
                world.playSound(
                        null,
                        posX,
                        posY,
                        posZ,
                        SoundEvents.ITEM_SHIELD_BLOCK,
                        SoundCategory.HOSTILE,
                        .6F,
                        1.2F);
                if (damage <= 0) return true;
            }
        }
        return super.attackEntityFrom(source, damage * (amplified() ? .8F : 1));
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        if (kind() == Kind.SHIELDER && rand.nextFloat() < .2F)
            dropItem(com.exoarsenal.registry.ModContent.SUSPICIOUS_CONTROLLER, 1);
        int metal = kind() == Kind.ARTHROPOD ? 3 + rand.nextInt(3) : rand.nextInt(2);
        if (metal > 0) dropItem(com.exoarsenal.registry.ModContent.FRIGID_METAL, metal);
    }

    @Override
    public void fall(float distance, float multiplier) {
        if (kind() != Kind.DRONE) super.fall(distance, multiplier);
    }

    @Override
    public boolean getCanSpawnHere() {
        return com.exoarsenal.world.FrigidSpawnBiomes.isCold(world.getBiome(getPosition()))
                && super.getCanSpawnHere();
    }

    @Override
    public boolean isNonBoss() {
        return kind() != Kind.ARTHROPOD;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setFloat("FrigidShield", shield());
        tag.setBoolean("FrigidExpert", expert);
        if (hasHome()) {
            tag.setLong("FrigidHome", getHomePosition().toLong());
            tag.setFloat("FrigidHomeRadius", getMaximumHomeDistance());
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        dataManager.set(SHIELD, MathHelper.clamp(tag.getFloat("FrigidShield"), 0, 20));
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).removeModifier(BOOST_ID);
        expert = tag.getBoolean("FrigidExpert");
        modeInitialized = tag.hasKey("FrigidExpert");
        if (tag.hasKey("FrigidHome"))
            setHomePosAndDistance(
                    BlockPos.fromLong(tag.getLong("FrigidHome")),
                    Math.max(8, (int) tag.getFloat("FrigidHomeRadius")));
    }

    @Override
    public void registerControllers(AnimationData data) {}

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    public static final class Drone extends EntityFrigidRobot {
        public Drone(World w) {
            super(w);
        }

        public Kind kind() {
            return Kind.DRONE;
        }
    }

    public static final class Amplifier extends EntityFrigidRobot {
        public Amplifier(World w) {
            super(w);
        }

        public Kind kind() {
            return Kind.AMPLIFIER;
        }
    }

    public static final class Shielder extends EntityFrigidRobot {
        public Shielder(World w) {
            super(w);
        }

        public Kind kind() {
            return Kind.SHIELDER;
        }
    }

    public static final class Rover extends EntityFrigidRobot {
        public Rover(World w) {
            super(w);
        }

        public Kind kind() {
            return Kind.ROVER;
        }
    }

    public static final class Arthropod extends EntityFrigidRobot {
        public Arthropod(World w) {
            super(w);
        }

        public Kind kind() {
            return Kind.ARTHROPOD;
        }
    }
}
