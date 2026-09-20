package com.exoarsenal.expedition;

import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import java.util.*;

public final class EntityExpeditionShot extends EntityThrowable {
    public static final int SLICER = 0,
            BARINADE = 1,
            SAND = 2,
            SPEAR = 3,
            SCREW = 4,
            PELLET = 5,
            WATER = 6,
            BUBBLE = 7,
            CORAL = 8,
            URCHIN = 9,
            FISHBONE = 10,
            SHELL = 11;
    public static final int PG_DAGGER = 12, PG_LASER = 13;
    private static final DataParameter<Integer> TYPE =
            EntityDataManager.createKey(EntityExpeditionShot.class, DataSerializers.VARINT);
    private float damage = 4;
    private int hits, damageClass;
    private boolean falling;
    private final Set<UUID> struck = new HashSet<>();

    public void setDamageClass(int value) {
        damageClass = MathHelper.clamp(value, 0, 2);
    }

    public int damageClass() {
        return damageClass;
    }

    public EntityExpeditionShot(World w) {
        super(w);
        setSize(.3F, .3F);
    }

    public EntityExpeditionShot(World w, EntityLivingBase owner, int type, float damage) {
        super(w, owner);
        dataManager.set(TYPE, type);
        this.damage = damage;
        setSize(type == SAND ? .5F : .3F, type == SAND ? .5F : .3F);
    }

    @Override
    protected void entityInit() {
        dataManager.register(TYPE, PELLET);
    }

    public int type() {
        return dataManager.get(TYPE);
    }

    @Override
    protected float getGravityVelocity() {
        return type() == SPEAR
                ? .012F
                : type() == CORAL || type() == URCHIN
                        ? .025F
                        : type() == SAND && falling ? .04F : 0;
    }

    private boolean returning() {
        return type() == PG_DAGGER
                || type() == SLICER
                || type() == SCREW
                || type() == FISHBONE
                || type() == SHELL;
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote) {
            EntityLivingBase owner = getThrower();
            if (owner == null || !owner.isEntityAlive()) {
                setDead();
                return;
            }
            if (returning() && ticksExisted > (type() == SCREW ? 7 : 17)) {
                Vec3d d = owner.getPositionEyes(1).subtract(getPositionVector());
                if (d.lengthSquared() < 1) {
                    setDead();
                    return;
                }
                d = d.normalize().scale(1.1);
                motionX = d.x;
                motionY = d.y;
                motionZ = d.z;
            }
            int max =
                    type() == CORAL
                            ? 9
                            : type() == PG_LASER
                                    ? 11
                                    : type() == PELLET ? 24 : type() == SAND ? 38 : 65;
            if (ticksExisted > max) {
                if (type() == SAND) burst();
                if (type() == CORAL) coralBurst();
                setDead();
                return;
            }
        }
        super.onUpdate();
        if (world.isRemote && ticksExisted % 2 == 0)
            world.spawnParticle(
                    type() == BARINADE || type() == PELLET
                            ? EnumParticleTypes.END_ROD
                            : EnumParticleTypes.CRIT,
                    posX,
                    posY,
                    posZ,
                    0,
                    0,
                    0);
    }

    @Override
    protected void onImpact(RayTraceResult hit) {
        if (world.isRemote) return;
        EntityLivingBase owner = getThrower();
        if (owner == null) return;
        if (hit.entityHit != null) {
            Entity e = hit.entityHit;
            if (e == owner || !(e instanceof EntityLivingBase) || struck.contains(e.getUniqueID()))
                return;
            if (owner instanceof EntityPlayer
                    && e instanceof EntityPlayer
                    && !((EntityPlayer) owner).canAttackPlayer((EntityPlayer) e)) return;
            if (e instanceof EntityExpeditionMinion) return;
            struck.add(e.getUniqueID());
            e.attackEntityFrom(
                    DamageSource.causeIndirectDamage(this, owner).setProjectile(),
                    damage * (type() == SPEAR ? Math.min(2, 1 + hits * .2F) : 1));
            hits++;
            if (type() == URCHIN)
                ((EntityLivingBase) e)
                        .addPotionEffect(
                                new net.minecraft.potion.PotionEffect(
                                        net.minecraft.init.MobEffects.POISON, 60, 0));
            if (type() == CORAL) coralBurst();
            if (type() == WATER || type() == BUBBLE) {
                e.extinguish();
                ((EntityLivingBase) e)
                        .addPotionEffect(
                                new net.minecraft.potion.PotionEffect(
                                        net.minecraft.init.MobEffects.SLOWNESS, 40, 0));
            }
            if (type() == SAND) {
                falling = true;
                motionX *= .12;
                motionZ *= .12;
                motionY = -.05;
            } else if (type() == PG_LASER
                    || type() == PELLET
                    || type() == CORAL
                    || type() == URCHIN
                    || type() == BUBBLE && hits >= 2
                    || type() == WATER && hits >= 3
                    || type() == BARINADE && hits >= 3
                    || type() == SPEAR && hits >= 10) setDead();
        } else if (type() == SAND) {
            burst();
            setDead();
        } else if (returning()) {
            ticksExisted = Math.max(ticksExisted, 18);
        } else {
            if (type() == CORAL) coralBurst();
            setDead();
        }
    }

    private void coralBurst() {
        EntityLivingBase owner = getThrower();
        if (owner == null) return;
        for (EntityLivingBase e :
                world.getEntitiesWithinAABB(
                        EntityLivingBase.class, getEntityBoundingBox().grow(1.25)))
            if (e != owner
                    && !struck.contains(e.getUniqueID())
                    && !owner.isOnSameTeam(e)
                    && (!(e instanceof EntityPlayer)
                            || !(owner instanceof EntityPlayer)
                            || ((EntityPlayer) owner).canAttackPlayer((EntityPlayer) e))) {
                struck.add(e.getUniqueID());
                e.attackEntityFrom(
                        DamageSource.causeIndirectDamage(this, owner).setProjectile(), damage);
            }
        if (world instanceof WorldServer)
            ((WorldServer) world)
                    .spawnParticle(
                            EnumParticleTypes.WATER_SPLASH,
                            posX,
                            posY,
                            posZ,
                            12,
                            .35,
                            .35,
                            .35,
                            .08);
    }

    private void burst() {
        EntityLivingBase owner = getThrower();
        if (owner == null) return;
        for (EntityLivingBase e :
                world.getEntitiesWithinAABB(EntityLivingBase.class, getEntityBoundingBox().grow(2)))
            if (e != owner
                    && !struck.contains(e.getUniqueID())
                    && (!(e instanceof EntityPlayer)
                            || !(owner instanceof EntityPlayer)
                            || ((EntityPlayer) owner).canAttackPlayer((EntityPlayer) e)))
                e.attackEntityFrom(
                        DamageSource.causeIndirectDamage(this, owner).setProjectile(),
                        damage * .65F);
        if (world instanceof WorldServer)
            ((WorldServer) world)
                    .spawnParticle(
                            EnumParticleTypes.BLOCK_DUST,
                            posX,
                            posY,
                            posZ,
                            28,
                            .5,
                            .3,
                            .5,
                            .08,
                            net.minecraft.block.Block.getStateId(
                                    net.minecraft.init.Blocks.SAND.getDefaultState()));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound n) {
        super.writeEntityToNBT(n);
        n.setInteger("Type", type());
        n.setFloat("Damage", damage);
        n.setInteger("Hits", hits);
        n.setInteger("Age", ticksExisted);
        n.setBoolean("Falling", falling);
        n.setInteger("DamageClass", damageClass);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound n) {
        super.readEntityFromNBT(n);
        dataManager.set(TYPE, MathHelper.clamp(n.getInteger("Type"), 0, PG_LASER));
        damage = n.getFloat("Damage");
        hits = n.getInteger("Hits");
        ticksExisted = n.getInteger("Age");
        falling = n.getBoolean("Falling");
        setDamageClass(n.getInteger("DamageClass"));
    }
}
