package com.exoarsenal.expedition;

import net.minecraft.entity.*;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.network.datasync.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.potion.PotionEffect;
import net.minecraft.init.MobEffects;

public final class EntityWildBolt extends EntityThrowable {
    private static final DataParameter<Integer> TYPE =
            EntityDataManager.createKey(EntityWildBolt.class, DataSerializers.VARINT);

    public EntityWildBolt(World world) {
        super(world);
        setSize(.2F, .2F);
    }

    public EntityWildBolt(World world, EntityLivingBase owner, int type) {
        super(world, owner);
        dataManager.set(TYPE, type);
        setSize(.2F, .2F);
    }

    @Override
    protected void entityInit() {
        dataManager.register(TYPE, 0);
    }

    public int type() {
        return MathHelper.clamp(dataManager.get(TYPE), 0, 4);
    }

    @Override
    protected float getGravityVelocity() {
        return type() == 2 ? 0 : .018F;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!world.isRemote
                && (ticksExisted > 80 || getThrower() == null || !getThrower().isEntityAlive()))
            setDead();
    }

    @Override
    protected void onImpact(RayTraceResult hit) {
        if (world.isRemote) return;
        if (hit.entityHit == getThrower() || hit.entityHit instanceof EntityWildMob) return;
        if (type() == 4 && hit.entityHit == null) return;
        if (hit.entityHit instanceof EntityLivingBase) {
            EntityLivingBase target = (EntityLivingBase) hit.entityHit;
            DamageSource source =
                    DamageSource.causeIndirectDamage(this, getThrower()).setProjectile();
            if (type() == 2) source.setMagicDamage();
            if (target.attackEntityFrom(source, type() == 2 ? 5 : 4)) {
                if (type() == 0) target.addPotionEffect(new PotionEffect(MobEffects.POISON, 60));
                if (type() == 1) target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 50));
            }
        }
        if (type() == 4 && hit.entityHit != null) hit.entityHit.setFire(3);
        setDead();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setInteger("WildBoltType", type());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        dataManager.set(TYPE, MathHelper.clamp(tag.getInteger("WildBoltType"), 0, 4));
    }
}
