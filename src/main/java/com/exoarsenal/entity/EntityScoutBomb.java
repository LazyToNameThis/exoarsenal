package com.exoarsenal.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public final class EntityScoutBomb extends EntityThrowable {
    public EntityScoutBomb(World world) {
        super(world);
    }

    public EntityScoutBomb(World world, EntityLivingBase thrower) {
        super(world, thrower);
    }

    @Override
    protected float getGravityVelocity() {
        return 0.035F;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!world.isRemote && ticksExisted > 120) setDead();
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (world.isRemote) return;
        EntityLivingBase owner = getThrower();
        if (result.entityHit == owner || result.entityHit instanceof EntityScoutHardpoint) return;
        boolean hot = owner instanceof EntityX20Scout && ((EntityX20Scout) owner).isOverheating();
        int struck = 0;
        for (EntityLivingBase target :
                world.getEntitiesWithinAABB(
                        EntityLivingBase.class, getEntityBoundingBox().grow(3.4D))) {
            if (target == owner
                    || target instanceof EntityScoutHardpoint
                    || target instanceof EntityScoutShard
                    || !target.isEntityAlive()) continue;
            double distance = target.getDistance(this);
            if (distance > 3.4D) continue;
            float damage = (float) Math.max(4.0D, 14.0D * (1.0D - distance / 4.5D));
            target.attackEntityFrom(
                    owner == null ? DamageSource.GENERIC : DamageSource.causeMobDamage(owner),
                    damage);
            if (hot) target.setFire(3);
            target.addVelocity((target.posX - posX) * 0.12D, 0.35D, (target.posZ - posZ) * 0.12D);
            if (++struck >= 12) break;
        }
        world.playSound(
                null,
                posX,
                posY,
                posZ,
                SoundEvents.BLOCK_GLASS_BREAK,
                SoundCategory.HOSTILE,
                1.2F,
                0.55F);
        if (world instanceof WorldServer) {
            WorldServer server = (WorldServer) world;
            if (hot) {
                server.spawnParticle(
                        EnumParticleTypes.FLAME, posX, posY, posZ, 24, 1.2, .6, 1.2, .08);
                server.spawnParticle(
                        EnumParticleTypes.EXPLOSION_LARGE, posX, posY, posZ, 1, 0D, 0D, 0D, 0D);
                world.playSound(
                        null,
                        posX,
                        posY,
                        posZ,
                        SoundEvents.ENTITY_GENERIC_EXPLODE,
                        SoundCategory.HOSTILE,
                        1,
                        .8F);
            }
            server.spawnParticle(
                    EnumParticleTypes.SNOW_SHOVEL, posX, posY, posZ, 42, 1.0D, 0.55D, 1.0D, 0.12D);
            server.spawnParticle(
                    EnumParticleTypes.FIREWORKS_SPARK,
                    posX,
                    posY,
                    posZ,
                    12,
                    0.65D,
                    0.35D,
                    0.65D,
                    0.08D);
        }
        setDead();
    }
}
