package com.exoarsenal.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.UUID;

public final class EntityAuroraField extends Entity {
    private UUID ownerId;
    private int life;

    public EntityAuroraField(World world) {
        super(world);
        setSize(11.0F, 5.0F);
        noClip = true;
        ignoreFrustumCheck = true;
    }

    public EntityAuroraField(World world, EntityPlayer owner) {
        this(world);
        ownerId = owner.getUniqueID();
        setPosition(owner.posX, owner.posY + 0.2D, owner.posZ);
    }

    @Override
    protected void entityInit() {}

    @Override
    public void onUpdate() {
        super.onUpdate();
        life++;
        if (world.isRemote) {
            if ((ticksExisted & 1) == 0) {
                for (int i = 0; i < 4; i++) {
                    double angle = ticksExisted * 0.06D + i * Math.PI * 0.5D;
                    double radius = 1.8D + (i & 1) * 2.1D;
                    world.spawnParticle(
                            EnumParticleTypes.SPELL_MOB,
                            posX + Math.cos(angle) * radius,
                            posY + 0.4D + (i % 3) * 0.9D,
                            posZ + Math.sin(angle) * radius,
                            0.15D + i * 0.12D,
                            0.75D,
                            0.95D);
                }
            }
            return;
        }
        if (life >= 1200) {
            setDead();
            return;
        }
        if (life % 10 != 0) return;
        EntityPlayer owner = owner();
        int affected = 0;
        for (EntityLivingBase target :
                world.getEntitiesWithinAABB(EntityLivingBase.class, getEntityBoundingBox())) {
            if (target == owner
                    || !target.isEntityAlive()
                    || (owner != null && owner.isOnSameTeam(target))) continue;
            target.attackEntityFrom(
                    owner == null
                            ? DamageSource.MAGIC
                            : DamageSource.causeIndirectMagicDamage(this, owner),
                    4.0F);
            target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 35, 1));
            if (++affected >= 16) break;
        }
    }

    public boolean belongsTo(EntityPlayer player) {
        return ownerId != null && ownerId.equals(player.getUniqueID());
    }

    public void detonate(EntityPlayer caster) {
        if (world.isRemote) return;
        int fragments = 0;
        for (EntityLivingBase target :
                world.getEntitiesWithinAABB(
                        EntityLivingBase.class, getEntityBoundingBox().grow(6.0D))) {
            if (target == caster || !target.isEntityAlive() || caster.isOnSameTeam(target))
                continue;
            double angle = fragments * 2.399963229728653D;
            Vec3d start =
                    getPositionVector()
                            .addVector(
                                    Math.cos(angle) * 2.2D,
                                    2.1D + (fragments % 3) * 0.4D,
                                    Math.sin(angle) * 2.2D);
            world.spawnEntity(new EntityAuroraShard(world, caster, target, start));
            if (++fragments >= 12) break;
        }
        if (world instanceof WorldServer) {
            ((WorldServer) world)
                    .spawnParticle(
                            EnumParticleTypes.SNOW_SHOVEL,
                            posX,
                            posY + 1.5D,
                            posZ,
                            90,
                            5.0D,
                            2.2D,
                            5.0D,
                            0.16D);
            ((WorldServer) world)
                    .spawnParticle(
                            EnumParticleTypes.FIREWORKS_SPARK,
                            posX,
                            posY + 1.5D,
                            posZ,
                            34,
                            3.0D,
                            1.5D,
                            3.0D,
                            0.12D);
        }
        setDead();
    }

    private EntityPlayer owner() {
        return ownerId == null ? null : world.getPlayerEntityByUUID(ownerId);
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
