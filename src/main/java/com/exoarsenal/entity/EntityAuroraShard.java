package com.exoarsenal.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

public final class EntityAuroraShard extends Entity {
    private static final DataParameter<Integer> TARGET =
            EntityDataManager.createKey(EntityAuroraShard.class, DataSerializers.VARINT);
    private UUID ownerId;
    private int life;

    public EntityAuroraShard(World world) {
        super(world);
        setSize(0.35F, 0.35F);
        noClip = true;
    }

    public EntityAuroraShard(
            World world, EntityPlayer owner, EntityLivingBase target, Vec3d start) {
        this(world);
        ownerId = owner.getUniqueID();
        dataManager.set(TARGET, target.getEntityId());
        setPosition(start.x, start.y, start.z);
        Vec3d initial =
                target.getPositionVector()
                        .addVector(0, target.height * 0.5D, 0)
                        .subtract(start)
                        .normalize();
        motionX = initial.x * 0.45D;
        motionY = initial.y * 0.45D + 0.12D;
        motionZ = initial.z * 0.45D;
    }

    @Override
    protected void entityInit() {
        dataManager.register(TARGET, -1);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        life++;
        if (world.isRemote) {
            world.spawnParticle(EnumParticleTypes.SPELL_MOB, posX, posY, posZ, 0.15D, 0.85D, 1.0D);
            if ((life & 1) == 0)
                world.spawnParticle(EnumParticleTypes.SNOW_SHOVEL, posX, posY, posZ, 0, 0, 0);
            return;
        }
        Entity targetEntity = world.getEntityByID(dataManager.get(TARGET));
        if (!(targetEntity instanceof EntityLivingBase)
                || !targetEntity.isEntityAlive()
                || life > 90) {
            setDead();
            return;
        }
        EntityLivingBase target = (EntityLivingBase) targetEntity;
        Vec3d desired =
                target.getPositionVector()
                        .addVector(0, target.height * 0.5D, 0)
                        .subtract(getPositionVector())
                        .normalize();
        motionX = motionX * 0.72D + desired.x * 0.34D;
        motionY = motionY * 0.72D + desired.y * 0.34D;
        motionZ = motionZ * 0.72D + desired.z * 0.34D;
        setPosition(posX + motionX, posY + motionY, posZ + motionZ);
        if (getEntityBoundingBox().grow(0.45D).intersects(target.getEntityBoundingBox())) {
            EntityPlayer owner = ownerId == null ? null : world.getPlayerEntityByUUID(ownerId);
            target.attackEntityFrom(
                    owner == null
                            ? DamageSource.MAGIC
                            : DamageSource.causeIndirectMagicDamage(this, owner),
                    12.0F);
            target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 120, 4));
            setDead();
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        if (tag.hasUniqueId("Owner")) ownerId = tag.getUniqueId("Owner");
        life = tag.getInteger("Life");
        dataManager.set(TARGET, tag.getInteger("Target"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        if (ownerId != null) tag.setUniqueId("Owner", ownerId);
        tag.setInteger("Life", life);
        tag.setInteger("Target", dataManager.get(TARGET));
    }
}
