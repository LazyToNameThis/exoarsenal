package com.exoarsenal.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

public final class EntityIceFragment extends Entity {
    private UUID ownerId;
    private int life;

    public EntityIceFragment(World world) {
        super(world);
        setSize(0.3F, 0.3F);
        noClip = true;
    }

    public EntityIceFragment(World world, EntityPlayer owner, Vec3d start, Vec3d direction) {
        this(world);
        ownerId = owner.getUniqueID();
        setPosition(start.x, start.y, start.z);
        Vec3d velocity = direction.normalize().scale(1.05D);
        motionX = velocity.x;
        motionY = velocity.y;
        motionZ = velocity.z;
    }

    @Override
    protected void entityInit() {}

    @Override
    public void onUpdate() {
        super.onUpdate();
        life++;
        if (world.isRemote) {
            world.spawnParticle(
                    EnumParticleTypes.SNOW_SHOVEL,
                    posX,
                    posY,
                    posZ,
                    -motionX * 0.04D,
                    -motionY * 0.04D,
                    -motionZ * 0.04D);
            return;
        }
        Vec3d start = getPositionVector();
        Vec3d end = start.addVector(motionX, motionY, motionZ);
        RayTraceResult block = world.rayTraceBlocks(start, end, false, true, false);
        if (block != null || life > 42) {
            setDead();
            return;
        }
        setPosition(end.x, end.y, end.z);
        EntityPlayer owner = ownerId == null ? null : world.getPlayerEntityByUUID(ownerId);
        for (EntityLivingBase target :
                world.getEntitiesWithinAABB(
                        EntityLivingBase.class, getEntityBoundingBox().grow(0.45D))) {
            if (target == owner
                    || !target.isEntityAlive()
                    || (owner != null && owner.isOnSameTeam(target))) continue;
            target.attackEntityFrom(
                    owner == null ? DamageSource.GENERIC : DamageSource.causePlayerDamage(owner),
                    6.0F);
            target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 70, 2));
            setDead();
            return;
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        if (tag.hasUniqueId("Owner")) ownerId = tag.getUniqueId("Owner");
        life = tag.getInteger("Life");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        if (ownerId != null) tag.setUniqueId("Owner", ownerId);
        tag.setInteger("Life", life);
    }
}
