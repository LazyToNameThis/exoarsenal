package com.exoarsenal.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import java.util.UUID;

public final class EntityScoutCut extends Entity {
    private static final DataParameter<Float>
            X = EntityDataManager.createKey(EntityScoutCut.class, DataSerializers.FLOAT),
            Y = EntityDataManager.createKey(EntityScoutCut.class, DataSerializers.FLOAT),
            Z = EntityDataManager.createKey(EntityScoutCut.class, DataSerializers.FLOAT);
    private static final DataParameter<Integer>
            AGE = EntityDataManager.createKey(EntityScoutCut.class, DataSerializers.VARINT),
            DELAY = EntityDataManager.createKey(EntityScoutCut.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> WALL =
            EntityDataManager.createKey(EntityScoutCut.class, DataSerializers.VARINT);
    private UUID owner;

    public EntityScoutCut(World world) {
        super(world);
        setSize(.1F, .1F);
        setNoGravity(true);
        noClip = true;
        ignoreFrustumCheck = true;
    }

    public EntityScoutCut(World world, EntityX20Scout boss, Vec3d from, Vec3d to, int delay) {
        this(world);
        owner = boss.getUniqueID();
        setPosition(from.x, from.y, from.z);
        Vec3d d = to.subtract(from);
        dataManager.set(X, (float) d.x);
        dataManager.set(Y, (float) d.y);
        dataManager.set(Z, (float) d.z);
        dataManager.set(DELAY, Math.max(12, delay));
    }

    public static EntityScoutCut crushingWalls(
            World world, EntityX20Scout boss, Vec3d center, int orientation, int delay) {
        EntityScoutCut cut = new EntityScoutCut(world, boss, center, center, delay);
        cut.dataManager.set(WALL, 1 + Math.floorMod(orientation, 4));
        return cut;
    }

    @Override
    protected void entityInit() {
        dataManager.register(X, 0F);
        dataManager.register(Y, 0F);
        dataManager.register(Z, 0F);
        dataManager.register(AGE, 0);
        dataManager.register(DELAY, 20);
        dataManager.register(WALL, 0);
    }

    public int wall() {
        return dataManager.get(WALL);
    }

    public Vec3d wallNormal() {
        switch (wall()) {
            case 1:
                return new Vec3d(1, 0, 0);
            case 2:
                return new Vec3d(0, 1, 0);
            case 3:
                return new Vec3d(.707, .707, 0);
            default:
                return new Vec3d(0, .707, .707);
        }
    }

    public double wallGap(float partial) {
        double t = Math.max(0, Math.min(1, (age() + partial - (delay() - 10)) / 10D));
        return 6 * (1 - t * t * (3 - 2 * t));
    }

    public Vec3d offset() {
        return new Vec3d(dataManager.get(X), dataManager.get(Y), dataManager.get(Z));
    }

    public int age() {
        return dataManager.get(AGE);
    }

    public int delay() {
        return dataManager.get(DELAY);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (world.isRemote) return;
        Entity e = owner == null ? null : ((WorldServer) world).getEntityFromUuid(owner);
        if (!(e instanceof EntityX20Scout)
                || !e.isEntityAlive()
                || ((EntityX20Scout) e).getScene() != 0) {
            setDead();
            return;
        }
        int age = age() + 1;
        dataManager.set(AGE, age);
        if (age == delay()) {
            Vec3d from = getPositionVector(), to = from.add(offset());
            if (wall() != 0) {
                for (EntityLivingBase victim :
                        world.getEntitiesWithinAABB(
                                EntityLivingBase.class, getEntityBoundingBox().grow(5))) {
                    if (victim == e
                            || victim instanceof EntityFrigidRobot
                            || victim instanceof EntityScoutShard
                            || victim instanceof EntityScoutHardpoint
                            || victim instanceof EntityX20Pilot) continue;
                    if (victim instanceof EntityPlayer
                            && (((EntityPlayer) victim).isCreative()
                                    || ((EntityPlayer) victim).isSpectator())) continue;
                    Vec3d delta =
                            victim.getPositionVector()
                                    .addVector(0, victim.height * .5, 0)
                                    .subtract(from);
                    if (Math.abs(delta.dotProduct(wallNormal())) < 1.2
                            && delta.lengthSquared() < 25)
                        victim.attackEntityFrom(
                                DamageSource.causeMobDamage((EntityX20Scout) e), 20);
                }
            } else
                for (EntityLivingBase victim :
                        world.getEntitiesWithinAABB(
                                EntityLivingBase.class,
                                new AxisAlignedBB(from, to).grow(.65, 6, .65))) {
                    if (victim == e
                            || victim instanceof EntityFrigidRobot
                            || victim instanceof EntityScoutShard
                            || victim instanceof EntityScoutHardpoint
                            || victim instanceof EntityX20Pilot) continue;
                    if (victim instanceof EntityPlayer
                            && (((EntityPlayer) victim).isCreative()
                                    || ((EntityPlayer) victim).isSpectator())) continue;
                    AxisAlignedBB body = victim.getEntityBoundingBox().grow(.65, 6, .65);
                    if (body.contains(from) || body.calculateIntercept(from, to) != null)
                        victim.attackEntityFrom(
                                DamageSource.causeMobDamage((EntityX20Scout) e), 22);
                }
            world.playSound(
                    null,
                    posX,
                    posY,
                    posZ,
                    net.minecraft.init.SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                    net.minecraft.util.SoundCategory.HOSTILE,
                    1.2F,
                    .65F);
        }
        if (age > delay() + 12) setDead();
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        setDead();
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {}

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }
}
