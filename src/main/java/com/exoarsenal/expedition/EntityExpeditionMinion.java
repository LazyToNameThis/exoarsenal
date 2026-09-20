package com.exoarsenal.expedition;

import net.minecraft.entity.*;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import java.util.UUID;

public final class EntityExpeditionMinion extends Entity {
    private static final DataParameter<Integer> TYPE =
            EntityDataManager.createKey(EntityExpeditionMinion.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> GUARD =
            EntityDataManager.createKey(EntityExpeditionMinion.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> FLYING =
            EntityDataManager.createKey(EntityExpeditionMinion.class, DataSerializers.BOOLEAN);
    private UUID owner;
    private int slot, missing;

    public EntityExpeditionMinion(World w) {
        super(w);
        setSize(.6F, .6F);
        setNoGravity(true);
    }

    public EntityExpeditionMinion(World w, EntityPlayer p, int type, int slot) {
        this(w);
        owner = p.getUniqueID();
        this.slot = slot;
        dataManager.set(TYPE, type);
        setPosition(p.posX, p.posY + 1.5, p.posZ);
    }

    public UUID owner() {
        return owner;
    }

    public int variant() {
        return dataManager.get(TYPE);
    }

    public boolean guard() {
        return dataManager.get(GUARD);
    }

    public void toggleGuard() {
        dataManager.set(GUARD, !guard());
    }

    public boolean flying() {
        return dataManager.get(FLYING);
    }

    @Override
    protected void entityInit() {
        dataManager.register(TYPE, 0);
        dataManager.register(GUARD, false);
        dataManager.register(FLYING, false);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (world.isRemote) return;
        EntityPlayer p = owner == null ? null : world.getPlayerEntityByUUID(owner);
        if (p == null || !p.isEntityAlive()) {
            if (++missing > 100) setDead();
            return;
        }
        missing = 0;
        if (variant() == 3 && !VictideArmor.summoner(p)) {
            setDead();
            return;
        }
        if (variant() >= 4) {
            groundCompanion(p);
            return;
        }
        double angle = ticksExisted * .055 + slot * Math.PI;
        Vec3d goal =
                p.getPositionVector()
                        .addVector(
                                Math.cos(angle) * 1.7,
                                variant() == 3 ? .45 : 1.3,
                                Math.sin(angle) * 1.7);
        EntityLivingBase target = null;
        double distance = 18 * 18;
        if (!guard())
            for (EntityLivingBase e :
                    world.getEntitiesWithinAABB(
                            EntityLivingBase.class, p.getEntityBoundingBox().grow(18)))
                if ((e instanceof IMob
                                || e instanceof EntitySeaCreature
                                        && ((EntitySeaCreature) e).angry())
                        && e.isEntityAlive()
                        && p.canEntityBeSeen(e)
                        && e.getDistanceSq(p) < distance) {
                    target = e;
                    distance = e.getDistanceSq(p);
                }
        if (target != null) {
            if (variant() == 0)
                goal = target.getPositionVector().addVector(0, target.height * .5, 0);
            else
                goal =
                        p.getPositionVector()
                                .addVector(Math.cos(angle) * 2, 2.2, Math.sin(angle) * 2);
            if (ticksExisted % 20 == 0) {
                float damage = (WulfrumArmor.full(p) ? 1.1F : 1) * 4;
                if (variant() == 0 && getDistanceSq(target) < 2.5)
                    target.attackEntityFrom(DamageSource.causeIndirectDamage(this, p), damage);
                else if (variant() >= 1) {
                    EntityExpeditionShot shot =
                            new EntityExpeditionShot(
                                    world,
                                    p,
                                    variant() == 1
                                            ? EntityExpeditionShot.PELLET
                                            : EntityExpeditionShot.CORAL,
                                    damage);
                    shot.setDamageClass(2);
                    shot.setPosition(posX, posY, posZ);
                    Vec3d d = target.getPositionEyes(1).subtract(getPositionVector()).normalize();
                    shot.shoot(d.x, d.y, d.z, 1.1F, .5F);
                    world.spawnEntity(shot);
                }
            }
        }
        if (getDistanceSq(p) > 40 * 40) setPosition(p.posX, p.posY + 1.5, p.posZ);
        Vec3d step = goal.subtract(getPositionVector()).scale(.22);
        if (step.lengthVector() > .55) step = step.normalize().scale(.55);
        setPosition(posX + step.x, posY + step.y, posZ + step.z);
        rotationYaw = (float) Math.toDegrees(Math.atan2(step.x, step.z));
    }

    private void groundCompanion(EntityPlayer player) {
        EntityLivingBase target = null;
        double range = 18 * 18;
        for (EntityLivingBase candidate :
                world.getEntitiesWithinAABB(
                        EntityLivingBase.class, player.getEntityBoundingBox().grow(18)))
            if ((candidate instanceof IMob
                            || candidate instanceof EntitySeaCreature
                                    && ((EntitySeaCreature) candidate).angry())
                    && candidate.isEntityAlive()
                    && player.canEntityBeSeen(candidate)
                    && candidate.getDistanceSq(player) < range) {
                target = candidate;
                range = candidate.getDistanceSq(player);
            }
        boolean flying = getDistanceSq(player) > 18 * 18 || Math.abs(player.posY - posY) > 5;
        dataManager.set(FLYING, flying);
        stepHeight = .6F;
        Vec3d goal = (target == null || flying ? player : target).getPositionVector();
        if (flying) {
            goal = player.getPositionVector().addVector(0, 1.2, 0);
            Vec3d movement = goal.subtract(getPositionVector()).normalize().scale(.55);
            setPosition(posX + movement.x, posY + movement.y, posZ + movement.z);
            motionY = 0;
            return;
        }
        Vec3d delta = goal.subtract(getPositionVector());
        double length = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        double speed = target == null ? .16 : .25;
        motionX = length > 1 ? delta.x / length * speed : 0;
        motionZ = length > 1 ? delta.z / length * speed : 0;
        if (onGround
                && (collidedHorizontally || variant() == 5 && length > 1 && ticksExisted % 14 == 0))
            motionY = .34;
        else motionY = Math.max(-.7, motionY - .06);
        move(MoverType.SELF, motionX, motionY, motionZ);
        rotationYaw = (float) -Math.toDegrees(Math.atan2(delta.x, delta.z));
        if (target != null && getDistanceSq(target) < 2.8 && ticksExisted % 20 == 0) {
            float damage = 4 * (WulfrumArmor.full(player) ? 1.1F : 1);
            if (variant() == 5)
                target.attackEntityFrom(DamageSource.causeIndirectDamage(this, player), damage);
            else {
                EntityPrebossShot spark =
                        new EntityPrebossShot(world, player, PrebossItem.Kind.STORM_SPEAR);
                spark.setSummonDamage(damage);
                spark.setPosition(posX, posY + .35, posZ);
                Vec3d d = target.getPositionEyes(1).subtract(spark.getPositionVector());
                spark.shoot(d.x, d.y, d.z, .25F, 2);
                world.spawnEntity(spark);
            }
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound n) {
        owner = n.hasUniqueId("Owner") ? n.getUniqueId("Owner") : null;
        dataManager.set(TYPE, n.getInteger("Type"));
        dataManager.set(GUARD, n.getBoolean("Guard"));
        slot = n.getInteger("Slot");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound n) {
        if (owner != null) n.setUniqueId("Owner", owner);
        n.setInteger("Type", variant());
        n.setBoolean("Guard", guard());
        n.setInteger("Slot", slot);
    }
}
