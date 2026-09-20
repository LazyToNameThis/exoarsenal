package com.scapeandrun.frostbite.expedition;

import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.network.datasync.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.nbt.NBTTagCompound;
import java.util.*;

public final class EntityRelicShot extends EntityThrowable {
    private static final DataParameter<Integer> TYPE =
            EntityDataManager.createKey(EntityRelicShot.class, DataSerializers.VARINT);
    private int bounces, hits;
    private final Set<UUID> struck = new HashSet<>();

    public EntityRelicShot(World world) {
        super(world);
        setSize(.35F, .35F);
    }

    public EntityRelicShot(World world, EntityLivingBase owner, DeepWeapon.Kind kind) {
        super(world, owner);
        dataManager.set(TYPE, kind.ordinal());
        setSize(.35F, .35F);
    }

    @Override
    protected void entityInit() {
        dataManager.register(TYPE, 1);
    }

    public int type() {
        return dataManager.get(TYPE);
    }

    @Override
    protected float getGravityVelocity() {
        return 0;
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote) {
            EntityLivingBase owner = getThrower();
            if (owner == null || !owner.isEntityAlive() || ticksExisted > 120) {
                setDead();
                return;
            }
            if (type() == 2 && ticksExisted < 35) {
                motionX *= 1.06;
                motionY *= 1.06;
                motionZ *= 1.06;
            }
            if (type() == 3
                    && owner instanceof EntityPlayer
                    && owner.isHandActive()
                    && owner.getActiveItemStack().getItem() == DeepContent.FLAMELASH
                    && ticksExisted < 100) {
                Vec3d goal = owner.getPositionEyes(1).add(owner.getLookVec().scale(12));
                Vec3d d = goal.subtract(getPositionVector()).normalize().scale(.9);
                motionX = motionX * .6 + d.x * .4;
                motionY = motionY * .6 + d.y * .4;
                motionZ = motionZ * .6 + d.z * .4;
            }
        }
        super.onUpdate();
    }

    @Override
    protected void onImpact(RayTraceResult hit) {
        if (world.isRemote) return;
        EntityLivingBase owner = getThrower();
        if (owner == null) return;
        if (hit.entityHit != null) {
            if (!(hit.entityHit instanceof EntityLivingBase)
                    || hit.entityHit == owner
                    || hit.entityHit instanceof EntityPlayer
                            && owner instanceof EntityPlayer
                            && !((EntityPlayer) owner).canAttackPlayer((EntityPlayer) hit.entityHit)
                    || !struck.add(hit.entityHit.getUniqueID())) return;
            if (hit.entityHit.attackEntityFrom(
                            DamageSource.causeIndirectDamage(this, owner)
                                    .setMagicDamage()
                                    .setProjectile(),
                            type() == 2 ? 9 : 7)
                    && type() == 3) hit.entityHit.setFire(3);
            if (++hits >= (type() == 3 ? 1 : 4)) setDead();
        } else if (type() == 1 && hit.sideHit != null && bounces++ < 3) {
            switch (hit.sideHit.getAxis()) {
                case X:
                    motionX = -motionX;
                    break;
                case Y:
                    motionY = -motionY;
                    break;
                case Z:
                    motionZ = -motionZ;
                    break;
            }
            setPosition(
                    hit.hitVec.x + hit.sideHit.getFrontOffsetX() * .2,
                    hit.hitVec.y + hit.sideHit.getFrontOffsetY() * .2,
                    hit.hitVec.z + hit.sideHit.getFrontOffsetZ() * .2);
        } else setDead();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setInteger("RelicType", type());
        tag.setInteger("Bounces", bounces);
        tag.setInteger("Hits", hits);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        dataManager.set(TYPE, MathHelper.clamp(tag.getInteger("RelicType"), 1, 3));
        bounces = tag.getInteger("Bounces");
        hits = tag.getInteger("Hits");
    }
}
