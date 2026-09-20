package com.scapeandrun.frostbite.entity;

import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public final class EntityWulfrumShard extends Entity {
    private static final DataParameter<Integer>
            OWNER = EntityDataManager.createKey(EntityWulfrumShard.class, DataSerializers.VARINT),
            WAIT = EntityDataManager.createKey(EntityWulfrumShard.class, DataSerializers.VARINT);
    private int mode, delay, flight, target;
    private float damage;
    private boolean counted;
    private Vec3d gathering;
    private boolean held;
    private Vec3d lockedLaunch;

    public EntityWulfrumShard launchAfter(Vec3d velocity) {
        lockedLaunch = velocity;
        return this;
    }

    public EntityWulfrumShard hold() {
        held = true;
        delay = 1;
        dataManager.set(WAIT, 1);
        return this;
    }

    public boolean held() {
        return held;
    }

    public void releaseIn(int ticks) {
        held = false;
        delay = Math.max(1, ticks);
        dataManager.set(WAIT, delay);
    }

    public void launch(Vec3d velocity) {
        held = false;
        gathering = null;
        delay = 0;
        dataManager.set(WAIT, 0);
        motionX = velocity.x;
        motionY = velocity.y;
        motionZ = velocity.z;
        velocityChanged = true;
    }

    public EntityWulfrumShard gather(Vec3d ring) {
        gathering = ring;
        return this;
    }

    public EntityWulfrumShard(World w) {
        super(w);
        setSize(.35F, .35F);
        setNoGravity(true);
    }

    public EntityWulfrumShard(
            EntityWulfrumEye owner,
            Vec3d from,
            Vec3d velocity,
            int mode,
            int delay,
            EntityLivingBase target,
            float damage) {
        this(owner.world);
        dataManager.set(OWNER, owner.getEntityId());
        setPosition(from.x, from.y, from.z);
        motionX = velocity.x * 1.5;
        motionY = velocity.y * 1.5;
        motionZ = velocity.z * 1.5;
        this.mode = mode;
        this.delay = WulfrumCombatClock.ticks(delay);
        this.target = target.getEntityId();
        this.damage = damage;
        dataManager.set(WAIT, this.delay);
    }

    @Override
    protected void entityInit() {
        dataManager.register(OWNER, -1);
        dataManager.register(WAIT, 0);
    }

    public boolean charging() {
        return dataManager.get(WAIT) > 0;
    }

    public int waitTicks() {
        return dataManager.get(WAIT);
    }

    public void counted() {
        counted = true;
    }

    public boolean belongs(EntityWulfrumEye eye) {
        return dataManager.get(OWNER) == eye.getEntityId();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (world.isRemote) {
            if (!charging()) setPosition(posX + motionX, posY + motionY, posZ + motionZ);
            return;
        }
        Entity owner = world.getEntityByID(dataManager.get(OWNER));
        if (!(owner instanceof EntityWulfrumEye)
                || !owner.isEntityAlive()
                || ((EntityWulfrumEye) owner).changing()
                || ticksExisted > 300) {
            setDead();
            return;
        }
        if (delay > 0) {
            if (gathering != null) {
                Vec3d next =
                        getPositionVector().add(gathering.subtract(getPositionVector()).scale(.3));
                setPosition(next.x, next.y, next.z);
                velocityChanged = true;
            }
            if (held) return;
            delay--;
            dataManager.set(WAIT, delay);
            if (delay == 0) {
                if (lockedLaunch != null) {
                    launch(lockedLaunch);
                    return;
                }
                Entity victim = world.getEntityByID(target);
                if (victim == null) {
                    setDead();
                    return;
                }
                Vec3d d =
                        victim.getPositionVector()
                                .addVector(victim.motionX * 3, 1, victim.motionZ * 3)
                                .subtract(getPositionVector())
                                .normalize()
                                .scale(1.6);
                motionX = d.x;
                motionY = d.y;
                motionZ = d.z;
                velocityChanged = true;
            }
            return;
        }
        if (++flight > 90) {
            setDead();
            return;
        }
        if (mode == 1) motionY -= .035;
        Vec3d from = getPositionVector(), to = from.addVector(motionX, motionY, motionZ);
        RayTraceResult wall = world.rayTraceBlocks(from, to, false, true, false);
        if (wall != null) to = wall.hitVec;
        boolean hit = wall != null;
        for (EntityPlayer p :
                world.getEntitiesWithinAABB(
                        EntityPlayer.class, new AxisAlignedBB(from, to).grow(.35))) {
            if (p.isCreative() || p.isSpectator()) continue;
            AxisAlignedBB box = p.getEntityBoundingBox().grow(.25);
            if (box.contains(from) || box.calculateIntercept(from, to) != null) {
                hit = true;
                break;
            }
        }
        if (wall != null) to = to.add(from.subtract(to).normalize().scale(.05));
        setPosition(to.x, to.y, to.z);
        velocityChanged = true;
        if (hit) burst((EntityWulfrumEye) owner);
    }

    private void burst(EntityWulfrumEye owner) {
        for (EntityPlayer p :
                world.getEntitiesWithinAABB(EntityPlayer.class, getEntityBoundingBox().grow(1.6)))
            if (!p.isCreative()
                    && !p.isSpectator()
                    && p.getDistanceSq(this) < 2.56
                    && world.rayTraceBlocks(
                                    getPositionVector(), p.getPositionEyes(1), false, true, false)
                            == null)
                p.attackEntityFrom(owner.projectileDamage(this), owner.balancedDamage(damage));
        ((WorldServer) world)
                .spawnParticle(
                        EnumParticleTypes.EXPLOSION_NORMAL,
                        posX,
                        posY,
                        posZ,
                        7,
                        .25,
                        .25,
                        .25,
                        .025);
        ((WorldServer) world)
                .spawnParticle(
                        EnumParticleTypes.VILLAGER_HAPPY, posX, posY, posZ, 5, .35, .35, .35, .02);
        world.playSound(
                null,
                posX,
                posY,
                posZ,
                net.minecraft.init.SoundEvents.ENTITY_GENERIC_EXPLODE,
                SoundCategory.HOSTILE,
                .22F,
                1.7F);
        setDead();
    }

    @Override
    public void setDead() {
        if (!world.isRemote && counted) {
            Entity e = world.getEntityByID(dataManager.get(OWNER));
            if (e instanceof EntityWulfrumEye) ((EntityWulfrumEye) e).releaseShard();
            counted = false;
        }
        super.setDead();
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound n) {
        setDead();
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound n) {}
}
