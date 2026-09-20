package com.scapeandrun.frostbite.entity;

import net.minecraft.entity.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.*;
import java.util.UUID;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.*;

public final class EntityDesertVulture extends EntityMob implements IAnimatable {
    private UUID owner;
    private Vec3d dive = Vec3d.ZERO;
    private final AnimationFactory factory = new AnimationFactory(this);

    public EntityDesertVulture(World w) {
        super(w);
        setSize(1.2F, 1);
        setNoAI(true);
        setNoGravity(true);
        experienceValue = 0;
    }

    public void setOwner(UUID id) {
        owner = id;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(18);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        setNoGravity(true);
        fallDistance = 0;
        if (world.isRemote) return;
        Entity parent = owner == null ? null : ((WorldServer) world).getEntityFromUuid(owner);
        if (parent == null || !parent.isEntityAlive()) {
            setDead();
            return;
        }
        EntityLivingBase p = getAttackTarget();
        if (p == null || !p.isEntityAlive()) {
            p = world.getNearestAttackablePlayer(this, 64, 48);
            setAttackTarget(p);
        }
        if (p == null) return;
        int t = ticksExisted % 100;
        Vec3d target;
        if (t == 55)
            dive = p.getPositionEyes(1).subtract(getPositionVector()).normalize().scale(.65);
        if (t >= 55 && t < 78) {
            motionX = dive.x;
            motionY = dive.y;
            motionZ = dive.z;
        } else {
            double angle = ticksExisted * .035 + getEntityId();
            target = p.getPositionVector().addVector(Math.cos(angle) * 9, 7, Math.sin(angle) * 9);
            Vec3d d = target.subtract(getPositionVector()).normalize().scale(.28);
            motionX = motionX * .8 + d.x * .2;
            motionY = motionY * .8 + d.y * .2;
            motionZ = motionZ * .8 + d.z * .2;
        }
        rotationYaw = (float) Math.toDegrees(Math.atan2(-motionX, motionZ));
        renderYawOffset = rotationYaw;
        velocityChanged = true;
        if (t >= 55
                && t < 78
                && getEntityBoundingBox().grow(.25).intersects(p.getEntityBoundingBox()))
            p.attackEntityFrom(DamageSource.causeMobDamage(this), 5);
    }

    @Override
    public void fall(float d, float m) {}

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {}

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    @Override
    public void registerControllers(AnimationData d) {}

    @Override
    public void writeEntityToNBT(NBTTagCompound n) {
        super.writeEntityToNBT(n);
        if (owner != null) n.setUniqueId("Owner", owner);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound n) {
        super.readEntityFromNBT(n);
        owner = n.hasUniqueId("Owner") ? n.getUniqueId("Owner") : null;
    }
}
