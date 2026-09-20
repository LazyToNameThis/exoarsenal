package com.exoarsenal.entity;

import net.minecraft.entity.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class EntityExcavatorSegment extends EntityLiving {
    private static final DataParameter<Integer>
            OWNER =
                    EntityDataManager.createKey(
                            EntityExcavatorSegment.class, DataSerializers.VARINT),
            INDEX =
                    EntityDataManager.createKey(
                            EntityExcavatorSegment.class, DataSerializers.VARINT);
    private static final DataParameter<Float>
            AIM_YAW =
                    EntityDataManager.createKey(
                            EntityExcavatorSegment.class, DataSerializers.FLOAT),
            AIM_PITCH =
                    EntityDataManager.createKey(
                            EntityExcavatorSegment.class, DataSerializers.FLOAT);
    private static final DataParameter<Integer> FIRE_AT =
            EntityDataManager.createKey(EntityExcavatorSegment.class, DataSerializers.VARINT);

    public EntityExcavatorSegment(World w) {
        super(w);
        setSize(2.2F, 2.2F);
        setNoAI(true);
        noClip = true;
        setNoGravity(true);
        isImmuneToFire = true;
        ignoreFrustumCheck = true;
    }

    public EntityExcavatorSegment(EntityExcavator boss, int index) {
        this(boss.world);
        dataManager.set(OWNER, boss.getEntityId());
        dataManager.set(INDEX, index);
        Vec3d p = boss.segment(index, 1);
        setPosition(p.x, p.y - 1, p.z);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(OWNER, -1);
        dataManager.register(INDEX, 1);
        dataManager.register(AIM_YAW, 0F);
        dataManager.register(AIM_PITCH, 0F);
        dataManager.register(FIRE_AT, -100000);
    }

    private final java.util.PriorityQueue<Integer> scheduledShots = new java.util.PriorityQueue<>();

    public void firingIn(int ticks) {
        if (scheduledShots.size() < 32) scheduledShots.add((int) world.getTotalWorldTime() + ticks);
    }

    public float shotAge(float partial) {
        return (int) world.getTotalWorldTime() - dataManager.get(FIRE_AT) + partial;
    }

    public float aimYaw() {
        return dataManager.get(AIM_YAW);
    }

    public float aimPitch() {
        return dataManager.get(AIM_PITCH);
    }

    public Vec3d cannonBase() {
        return ExcavatorGeometry.turretSocket(
                getPositionVector().addVector(0, 1, 0),
                Math.toRadians(rotationYaw),
                Math.toRadians(rotationPitch));
    }

    public Vec3d muzzle() {
        return ExcavatorGeometry.turretMuzzle(
                cannonBase(), Math.toRadians(aimYaw()), Math.toRadians(aimPitch()));
    }

    public void aim(Vec3d target) {
        Vec3d d = target.subtract(cannonBase());
        dataManager.set(AIM_YAW, (float) Math.toDegrees(Math.atan2(d.x, d.z)));
        double pitch =
                -Math.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z))
                        + Math.asin(Math.min(1, .95 / Math.max(.95, d.lengthVector())));
        dataManager.set(AIM_PITCH, (float) Math.toDegrees(pitch));
    }

    public int index() {
        return dataManager.get(INDEX);
    }

    public EntityExcavator boss() {
        Entity e = world.getEntityByID(dataManager.get(OWNER));
        return e instanceof EntityExcavator ? (EntityExcavator) e : null;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        motionX = motionY = motionZ = 0;
        if (world.isRemote) return;
        while (!scheduledShots.isEmpty()
                && scheduledShots.peek() <= (int) world.getTotalWorldTime())
            dataManager.set(FIRE_AT, scheduledShots.remove());
        EntityExcavator b = boss();
        if (b == null || !b.isEntityAlive()) {
            setDead();
            return;
        }
        Vec3d p = b.segment(index(), 1), front = b.segment(index() - 1, 1), d = front.subtract(p);
        setPosition(p.x, p.y - 1, p.z);
        rotationYaw = (float) Math.toDegrees(Math.atan2(d.x, d.z));
        rotationPitch = (float) -Math.toDegrees(Math.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z)));
        renderYawOffset = rotationYaw;
        velocityChanged = true;
    }

    @Override
    public boolean attackEntityFrom(DamageSource s, float amount) {
        EntityExcavator b = boss();
        return b != null && b.attackEntityFrom(s, amount * .7F);
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public void fall(float d, float m) {}

    @Override
    public void readEntityFromNBT(NBTTagCompound n) {
        super.readEntityFromNBT(n);
        setDead();
    }
}
