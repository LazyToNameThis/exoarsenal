package com.exoarsenal.entity;

import com.exoarsenal.item.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import java.util.*;

public final class EntityScoutCoreRay extends Entity {
    private static final DataParameter<Integer>
            AGE = EntityDataManager.createKey(EntityScoutCoreRay.class, DataSerializers.VARINT),
            OWNER = EntityDataManager.createKey(EntityScoutCoreRay.class, DataSerializers.VARINT);
    private static final DataParameter<Float>
            X = EntityDataManager.createKey(EntityScoutCoreRay.class, DataSerializers.FLOAT),
            Y = EntityDataManager.createKey(EntityScoutCoreRay.class, DataSerializers.FLOAT),
            Z = EntityDataManager.createKey(EntityScoutCoreRay.class, DataSerializers.FLOAT),
            LENGTH = EntityDataManager.createKey(EntityScoutCoreRay.class, DataSerializers.FLOAT);
    private UUID owner;
    private EnumHand hand = EnumHand.MAIN_HAND;

    public EntityScoutCoreRay(World w) {
        super(w);
        setSize(.1F, .1F);
        setNoGravity(true);
        noClip = true;
        ignoreFrustumCheck = true;
    }

    public EntityScoutCoreRay(EntityPlayer p, EnumHand hand) {
        this(p.world);
        owner = p.getUniqueID();
        this.hand = hand;
        dataManager.set(OWNER, p.getEntityId());
        Vec3d eye = p.getPositionEyes(1);
        setPosition(eye.x, eye.y, eye.z);
        aim(p.getLookVec());
    }

    @Override
    protected void entityInit() {
        dataManager.register(AGE, 0);
        dataManager.register(OWNER, -1);
        dataManager.register(X, 0F);
        dataManager.register(Y, 0F);
        dataManager.register(Z, 1F);
        dataManager.register(LENGTH, 48F);
    }

    public int age() {
        return dataManager.get(AGE);
    }

    public float length() {
        return dataManager.get(LENGTH);
    }

    public Vec3d direction() {
        return new Vec3d(dataManager.get(X), dataManager.get(Y), dataManager.get(Z));
    }

    public boolean belongs(EntityPlayer p) {
        return dataManager.get(OWNER) == p.getEntityId()
                || owner != null && owner.equals(p.getUniqueID());
    }

    private void aim(Vec3d d) {
        dataManager.set(X, (float) d.x);
        dataManager.set(Y, (float) d.y);
        dataManager.set(Z, (float) d.z);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        Entity e = world.getEntityByID(dataManager.get(OWNER));
        if (!world.isRemote && !(e instanceof EntityPlayer) && owner != null)
            e = ((WorldServer) world).getEntityFromUuid(owner);
        if (!(e instanceof EntityPlayer)) {
            if (!world.isRemote) setDead();
            return;
        }
        EntityPlayer p = (EntityPlayer) e;
        Vec3d eye = p.getPositionEyes(1);
        setPosition(eye.x, eye.y, eye.z);
        if (world.isRemote) return;
        dataManager.set(OWNER, p.getEntityId());
        int age = age() + 1;
        dataManager.set(AGE, age);
        if (!p.isEntityAlive()
                || age > ScoutCoreMotion.END
                || !(p.getHeldItem(hand).getItem() instanceof ItemScoutEnergyCore)) {
            setDead();
            return;
        }
        if (age < ScoutCoreMotion.CHARGE) aim(p.getLookVec());
        Vec3d d = direction(), end = eye.add(d.scale(64));
        RayTraceResult wall = world.rayTraceBlocks(eye, end, false, true, false);
        float length = wall == null ? 64 : (float) eye.distanceTo(wall.hitVec);
        dataManager.set(LENGTH, length);
        if (age == 1 || age == ScoutCoreMotion.CHARGE)
            world.playSound(
                    null,
                    posX,
                    posY,
                    posZ,
                    net.minecraft.init.SoundEvents.ENTITY_GUARDIAN_ATTACK,
                    SoundCategory.PLAYERS,
                    1.0F,
                    age == 1 ? 1.4F : .55F);
        if (age < ScoutCoreMotion.CHARGE || age % 5 != 0) return;
        float power = ScoutCoreMotion.power(age);
        if (!EnergyUtil.drain(p.getHeldItem(hand), 500 + (int) (1500 * power), false)) {
            setDead();
            return;
        }
        if (length <= 4) return;
        Vec3d from = eye.add(d.scale(4)), to = eye.add(d.scale(length));
        double radius = ScoutCoreMotion.beamRadius(age);
        List<EntityLivingBase> targets =
                world.getEntitiesWithinAABB(
                        EntityLivingBase.class, new AxisAlignedBB(from, to).grow(radius));
        targets.sort(Comparator.comparingDouble(p::getDistanceSq));
        int hits = 0;
        for (EntityLivingBase target : targets) {
            if (target == p
                    || !target.isEntityAlive()
                    || p.isOnSameTeam(target)
                    || target instanceof EntityPlayer && !p.canAttackPlayer((EntityPlayer) target))
                continue;
            if (target.getEntityBoundingBox().grow(radius).calculateIntercept(from, to) == null
                    && !target.getEntityBoundingBox().grow(radius).contains(from)) continue;
            if (target.attackEntityFrom(
                    DamageSource.causePlayerDamage(p).setProjectile(), 14 + 38 * power))
                target.addPotionEffect(
                        new net.minecraft.potion.PotionEffect(
                                net.minecraft.init.MobEffects.SLOWNESS, 35, 1));
            if (++hits >= 8) break;
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound n) {
        if (owner != null) n.setUniqueId("Owner", owner);
        n.setInteger("Age", age());
        n.setInteger("Hand", hand.ordinal());
        n.setFloat("X", dataManager.get(X));
        n.setFloat("Y", dataManager.get(Y));
        n.setFloat("Z", dataManager.get(Z));
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound n) {
        owner = n.hasUniqueId("Owner") ? n.getUniqueId("Owner") : null;
        dataManager.set(AGE, n.getInteger("Age"));
        hand = n.getInteger("Hand") == 1 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
        aim(new Vec3d(n.getFloat("X"), n.getFloat("Y"), n.getFloat("Z")).normalize());
    }
}
