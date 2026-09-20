package com.scapeandrun.frostbite.expedition;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public abstract class EntityWulfrum extends EntityMob {
    public enum Kind {
        AMPLIFIER,
        DRONE,
        GYRATOR,
        HOVERCRAFT,
        ROVER,
        MINE,
        SLIME
    }

    private static final DataParameter<Boolean>
            CHARGED = EntityDataManager.createKey(EntityWulfrum.class, DataSerializers.BOOLEAN),
            AWAKE = EntityDataManager.createKey(EntityWulfrum.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> FUSE =
            EntityDataManager.createKey(EntityWulfrum.class, DataSerializers.VARINT);
    private int cycle;
    private boolean wasGround;
    private Vec3d dive = Vec3d.ZERO;

    public abstract Kind kind();

    public EntityWulfrum(World w) {
        super(w);
        setSize(kind() == Kind.MINE ? 1.7F : 1, kind() == Kind.AMPLIFIER ? 1.3F : .9F);
        experienceValue = kind() == Kind.MINE ? 12 : 3;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(CHARGED, false);
        dataManager.register(AWAKE, false);
        dataManager.register(FUSE, 0);
    }

    public boolean charged() {
        return dataManager.get(CHARGED);
    }

    public boolean awake() {
        return dataManager.get(AWAKE);
    }

    public int fuse() {
        return dataManager.get(FUSE);
    }

    public void supercharge() {
        dataManager.set(CHARGED, true);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)
                .setBaseValue(
                        kind() == Kind.MINE
                                ? 80
                                : kind() == Kind.AMPLIFIER ? 24 : kind() == Kind.ROVER ? 20 : 14);
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(kind() == Kind.MINE ? 6 : 2);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.23);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(24);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3);
    }

    @Override
    protected void initEntityAI() {
        targetTasks.addTask(0, new EntityAIHurtByTarget(this, false));
        targetTasks.addTask(
                1, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
        if (kind() == Kind.ROVER) {
            tasks.addTask(1, new EntityAIAttackMelee(this, 1.1, true));
            tasks.addTask(4, new EntityAIWanderAvoidWater(this, .8));
        }
        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 16));
    }

    @Override
    public boolean getCanSpawnHere() {
        if (world.provider.getDimension() != 0 || world.getDifficulty() == EnumDifficulty.PEACEFUL)
            return false;
        if (kind() == Kind.MINE) {
            if (posY >= 55 || world.canSeeSky(getPosition())) return false;
        } else if (!world.isDaytime()
                || !world.canSeeSky(getPosition())
                || !world.getBlockState(getPosition().down()).getMaterial().isSolid()) return false;
        return world.checkNoEntityCollision(getEntityBoundingBox())
                && world.getCollisionBoxes(this, getEntityBoundingBox()).isEmpty()
                && !world.containsAnyLiquid(getEntityBoundingBox());
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (world.isRemote || !isEntityAlive()) return;
        cycle++;
        if (kind() == Kind.AMPLIFIER) {
            motionX = motionZ = 0;
            getNavigator().clearPath();
            if (cycle % 20 == 0) {
                boolean near = world.getNearestAttackablePlayer(this, 12, 12) != null;
                if (near) {
                    dataManager.set(AWAKE, true);
                    for (EntityWulfrum e :
                            world.getEntitiesWithinAABB(
                                    EntityWulfrum.class, getEntityBoundingBox().grow(10)))
                        e.supercharge();
                    ring(10, EnumParticleTypes.VILLAGER_HAPPY);
                }
            }
            return;
        }
        EntityLivingBase p = getAttackTarget();
        if (p == null || !p.isEntityAlive()) return;
        getLookHelper().setLookPositionWithEntity(p, 25, 25);
        Vec3d d = p.getPositionVector().subtract(getPositionVector());
        switch (kind()) {
            case DRONE:
            case HOVERCRAFT:
                {
                    setNoGravity(true);
                    int t = cycle % (charged() ? 65 : 100);
                    if (t == 45) {
                        dive =
                                p.getPositionEyes(1)
                                        .subtract(getPositionVector())
                                        .normalize()
                                        .scale(charged() ? .55 : .4);
                        world.playSound(
                                null,
                                posX,
                                posY,
                                posZ,
                                SoundEvents.BLOCK_NOTE_HAT,
                                SoundCategory.HOSTILE,
                                .6F,
                                .6F);
                    }
                    if (t >= 45 && t < 64) {
                        motionX = dive.x;
                        motionY = dive.y;
                        motionZ = dive.z;
                        touch(p, charged() ? 5 : 3);
                    } else {
                        double height = kind() == Kind.DRONE ? 3.5 : 2;
                        motionY = MathHelper.clamp((p.posY + height - posY) * .06, -.2, .2);
                        Vec3d h = new Vec3d(d.x, 0, d.z).normalize();
                        double close = d.lengthVector() > 8 ? .12 : -.06;
                        motionX = h.x * close - h.z * .08;
                        motionZ = h.z * close + h.x * .08;
                        if (charged() && t == 25) laser(p.getPositionEyes(1), 4);
                    }
                    break;
                }
            case GYRATOR:
            case SLIME:
                {
                    if (kind() == Kind.SLIME && !awake()) {
                        motionX = motionZ = 0;
                        if (d.lengthSquared() < 36 || getHealth() < getMaxHealth())
                            dataManager.set(AWAKE, true);
                        else break;
                    }
                    if (onGround && cycle % (charged() ? 18 : 32) == 0) {
                        Vec3d h = new Vec3d(d.x, 0, d.z).normalize();
                        motionX = h.x * (kind() == Kind.GYRATOR ? .45 : .3);
                        motionZ = h.z * (kind() == Kind.GYRATOR ? .45 : .3);
                        motionY = kind() == Kind.GYRATOR ? .42 : .6;
                        velocityChanged = true;
                    }
                    if (onGround && !wasGround && charged() && kind() == Kind.SLIME) {
                        ring(3, EnumParticleTypes.SLIME);
                        for (EntityPlayer q :
                                world.getEntitiesWithinAABB(
                                        EntityPlayer.class, getEntityBoundingBox().grow(3, 1, 3)))
                            hurt(q, 4);
                    }
                    touch(p, charged() ? 5 : 3);
                    wasGround = onGround;
                    break;
                }
            case MINE:
                {
                    setNoGravity(true);
                    motionX = motionY = motionZ = 0;
                    if (getHealth() <= getMaxHealth() * .5F) {
                        supercharge();
                        dataManager.set(FUSE, fuse() + 1);
                    }
                    int period = charged() ? 18 : 40;
                    if (cycle % period == 0) {
                        double a = Math.floorDiv(cycle, period) * Math.PI / 8;
                        for (int i = 0; i < 4; i++) {
                            double angle = a + i * Math.PI / 2;
                            laser(
                                    getPositionVector()
                                            .addVector(
                                                    Math.cos(angle) * 18, .5, Math.sin(angle) * 18),
                                    charged() ? 7 : 5);
                        }
                    }
                    if (fuse() >= 160) {
                        boolean grief =
                                net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(
                                        world, this);
                        world.createExplosion(this, posX, posY, posZ, grief ? 5.5F : 3.5F, grief);
                        setDead();
                    }
                    break;
                }
            default:
                break;
        }
    }

    private void touch(EntityLivingBase p, float amount) {
        if (getEntityBoundingBox().grow(.2).intersects(p.getEntityBoundingBox())) hurt(p, amount);
    }

    private void hurt(EntityLivingBase p, float amount) {
        if (p instanceof EntityPlayer
                && (((EntityPlayer) p).isCreative() || ((EntityPlayer) p).isSpectator())) return;
        p.attackEntityFrom(DamageSource.causeMobDamage(this), amount);
    }

    private void laser(Vec3d target, float damage) {
        Vec3d from = getPositionVector().addVector(0, .5, 0),
                to = from.add(target.subtract(from).normalize().scale(18));
        RayTraceResult wall = world.rayTraceBlocks(from, to, false, true, false);
        if (wall != null) to = wall.hitVec;
        for (EntityPlayer p :
                world.getEntitiesWithinAABB(
                        EntityPlayer.class, new AxisAlignedBB(from, to).grow(.2)))
            if (p.getEntityBoundingBox().grow(.15).calculateIntercept(from, to) != null)
                hurt(p, damage);
        if (world instanceof WorldServer)
            for (int i = 0; i < 18; i++) {
                Vec3d v = from.add(to.subtract(from).scale(i / 17D));
                ((WorldServer) world)
                        .spawnParticle(EnumParticleTypes.REDSTONE, v.x, v.y, v.z, 0, .4, 1, .15, 1);
            }
    }

    private void ring(double r, EnumParticleTypes particle) {
        if (world instanceof WorldServer)
            for (int i = 0; i < 20; i++) {
                double a = i * Math.PI / 10;
                ((WorldServer) world)
                        .spawnParticle(
                                particle,
                                posX + Math.cos(a) * r,
                                posY + .2,
                                posZ + Math.sin(a) * r,
                                1,
                                0D,
                                0D,
                                0D,
                                0D);
            }
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        dropItem(
                ExpeditionContent.SCRAP,
                (kind() == Kind.AMPLIFIER || kind() == Kind.HOVERCRAFT ? 2 : 1) + rand.nextInt(2));
        if (charged()) dropItem(ExpeditionContent.CORE, 1);
        if (kind() == Kind.ROVER && rand.nextInt(10) == 0)
            dropItem(ExpeditionContent.ROVER_DRIVE, 1);
        if (rand.nextInt(20) == 0) dropItem(ExpeditionContent.BATTERY, 1);
    }

    @Override
    public void fall(float d, float m) {
        if (kind() != Kind.DRONE && kind() != Kind.HOVERCRAFT && kind() != Kind.MINE)
            super.fall(d, m);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound n) {
        super.writeEntityToNBT(n);
        n.setBoolean("Supercharged", charged());
        n.setBoolean("Awake", awake());
        n.setInteger("Fuse", fuse());
        n.setInteger("Cycle", cycle);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound n) {
        super.readEntityFromNBT(n);
        dataManager.set(CHARGED, n.getBoolean("Supercharged"));
        dataManager.set(AWAKE, n.getBoolean("Awake"));
        dataManager.set(FUSE, n.getInteger("Fuse"));
        cycle = n.getInteger("Cycle");
    }

    public static final class Amplifier extends EntityWulfrum {
        public Amplifier(World w) {
            super(w);
        }

        public Kind kind() {
            return Kind.AMPLIFIER;
        }
    }

    public static final class Drone extends EntityWulfrum {
        public Drone(World w) {
            super(w);
        }

        public Kind kind() {
            return Kind.DRONE;
        }
    }

    public static final class Gyrator extends EntityWulfrum {
        public Gyrator(World w) {
            super(w);
        }

        public Kind kind() {
            return Kind.GYRATOR;
        }
    }

    public static final class Hovercraft extends EntityWulfrum {
        public Hovercraft(World w) {
            super(w);
        }

        public Kind kind() {
            return Kind.HOVERCRAFT;
        }
    }

    public static final class Rover extends EntityWulfrum {
        public Rover(World w) {
            super(w);
        }

        public Kind kind() {
            return Kind.ROVER;
        }
    }

    public static final class Mine extends EntityWulfrum {
        public Mine(World w) {
            super(w);
        }

        public Kind kind() {
            return Kind.MINE;
        }
    }

    public static final class Slime extends EntityWulfrum {
        public Slime(World w) {
            super(w);
        }

        public Kind kind() {
            return Kind.SLIME;
        }
    }
}
