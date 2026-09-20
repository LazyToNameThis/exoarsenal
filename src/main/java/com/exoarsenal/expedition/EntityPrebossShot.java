package com.exoarsenal.expedition;

import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import java.util.*;

public final class EntityPrebossShot extends EntityThrowable {
    private static final DataParameter<Integer> TYPE =
            EntityDataManager.createKey(EntityPrebossShot.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> GENERATION =
            EntityDataManager.createKey(EntityPrebossShot.class, DataSerializers.VARINT);
    private final Set<UUID> struck = new HashSet<>();
    private float damage = 4;
    private int bounces, hits;
    private boolean empowered, split, returning, summonSource;

    public void setSummonDamage(float value) {
        summonSource = true;
        damage = value;
    }

    public boolean summonSource() {
        return summonSource;
    }

    public EntityPrebossShot(World world) {
        super(world);
        setSize(.25F, .25F);
    }

    public EntityPrebossShot(World world, EntityLivingBase owner, PrebossItem.Kind kind) {
        super(world, owner);
        setSize(.25F, .25F);
        dataManager.set(TYPE, kind.ordinal());
        damage =
                kind == PrebossItem.Kind.DIAMOND
                        ? 8
                        : kind == PrebossItem.Kind.FROST_BOLT
                                ? 8
                                : kind == PrebossItem.Kind.WOOD_BOOMERANG
                                        ? 3
                                        : kind == PrebossItem.Kind.KNIFE ? 3 : 6;
        if (owner instanceof EntityPlayer
                && magic()
                && PrebossProgress.data((EntityPlayer) owner).getInteger("ManaSickness") > 0)
            damage *= .75F;
    }

    @Override
    protected void entityInit() {
        dataManager.register(TYPE, PrebossItem.Kind.KNIFE.ordinal());
        dataManager.register(GENERATION, 0);
    }

    public PrebossItem.Kind kind() {
        return PrebossItem.Kind.values()[
                MathHelper.clamp(dataManager.get(TYPE), 0, PrebossItem.Kind.values().length - 1)];
    }

    public int generation() {
        return dataManager.get(GENERATION);
    }

    public void setEmpowered(boolean value) {
        empowered = value;
        if (value) damage *= 1.7F;
    }

    public boolean magic() {
        switch (kind()) {
            case SPARKING:
            case FROSTING:
            case FROST_BOLT:
            case THUNDER_ZAPPER:
            case DIAMOND:
            case EMERALD:
                return true;
            default:
                return false;
        }
    }

    private boolean boomerang() {
        return kind() == PrebossItem.Kind.WOOD_BOOMERANG
                || kind() == PrebossItem.Kind.ENCHANTED_BOOMERANG;
    }

    @Override
    protected float getGravityVelocity() {
        if (boomerang()
                || kind() == PrebossItem.Kind.DIAMOND
                || kind() == PrebossItem.Kind.EMERALD
                || kind() == PrebossItem.Kind.THUNDER_ZAPPER) return 0;
        if (kind() == PrebossItem.Kind.FROST_BOLT) return ticksExisted > 55 ? .035F : 0;
        if (kind() == PrebossItem.Kind.CRYSTALLINE)
            return generation() > 0 ? 0 : ticksExisted > 12 ? .025F : 0;
        return .018F;
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote) {
            EntityLivingBase owner = getThrower();
            if (owner == null || !owner.isEntityAlive()) {
                setDead();
                return;
            }
            int max =
                    PrebossRules.maximumAge(
                            boomerang()
                                    ? PrebossRules.EntityType.BOOMERANG
                                    : kind() == PrebossItem.Kind.FROST_BOLT
                                            ? PrebossRules.EntityType.FROST
                                            : generation() > 0 ? PrebossRules.EntityType.SPLIT : 0);
            if (ticksExisted >= max) {
                setDead();
                return;
            }
            if (boomerang() && ticksExisted >= 14) {
                if (!returning) {
                    returning = true;
                    struck.clear();
                }
                Vec3d delta = owner.getPositionEyes(1).subtract(getPositionVector());
                if (delta.lengthSquared() < 1) {
                    setDead();
                    return;
                }
                delta = delta.normalize().scale(1.35);
                motionX = delta.x;
                motionY = delta.y;
                motionZ = delta.z;
            }
            if (kind() == PrebossItem.Kind.CRYSTALLINE && !split && ticksExisted >= 8) {
                split = true;
                int count = PrebossRules.splitChildren(generation(), empowered);
                for (int i = 0; i < count; i++)
                    child(owner, (i - 1) * 13, generation() + 1, damage * .55F);
            }
            if (kind() == PrebossItem.Kind.THUNDER_ZAPPER && ticksExisted % 3 == 0) {
                double angle = (ticksExisted / 3 % 2 == 0 ? 1 : -1) * .18;
                double x = motionX, z = motionZ;
                motionX = x * Math.cos(angle) - z * Math.sin(angle);
                motionZ = x * Math.sin(angle) + z * Math.cos(angle);
            }
        }
        super.onUpdate();
        if (world.isRemote && kind() != PrebossItem.Kind.JAVELIN && ticksExisted % 2 == 0)
            world.spawnParticle(
                    kind() == PrebossItem.Kind.SPARKING
                            ? EnumParticleTypes.FLAME
                            : kind() == PrebossItem.Kind.FROST_BOLT
                                            || kind() == PrebossItem.Kind.FROSTING
                                    ? EnumParticleTypes.SNOW_SHOVEL
                                    : EnumParticleTypes.END_ROD,
                    posX,
                    posY,
                    posZ,
                    0,
                    0,
                    0);
    }

    private void child(EntityLivingBase owner, float yaw, int generation, float power) {
        EntityPrebossShot child = new EntityPrebossShot(world, owner, PrebossItem.Kind.CRYSTALLINE);
        child.setPosition(posX, posY, posZ);
        child.damage = power;
        child.empowered = empowered;
        child.dataManager.set(GENERATION, generation);
        Vec3d direction =
                new Vec3d(motionX, motionY, motionZ).rotateYaw((float) Math.toRadians(yaw));
        child.shoot(direction.x, direction.y, direction.z, generation >= 3 ? .5F : .9F, 0);
        world.spawnEntity(child);
    }

    private boolean valid(EntityLivingBase owner, Entity entity) {
        return entity instanceof EntityLivingBase
                && entity != owner
                && !(owner instanceof EntityPlayer
                        && entity instanceof EntityPlayer
                        && !((EntityPlayer) owner).canAttackPlayer((EntityPlayer) entity));
    }

    @Override
    protected void onImpact(RayTraceResult hit) {
        if (world.isRemote) return;
        EntityLivingBase owner = getThrower();
        if (owner == null) return;
        if (hit.entityHit != null) {
            if (!valid(owner, hit.entityHit) || !struck.add(hit.entityHit.getUniqueID())) return;
            EntityLivingBase target = (EntityLivingBase) hit.entityHit;
            DamageSource source = DamageSource.causeIndirectDamage(this, owner).setProjectile();
            if (magic()) source.setMagicDamage();
            target.attackEntityFrom(source, damage);
            hits++;
            if (kind() == PrebossItem.Kind.SPARKING && rand.nextBoolean())
                target.setFire(1 + rand.nextInt(3));
            if (kind() == PrebossItem.Kind.FROSTING || kind() == PrebossItem.Kind.FROST_BOLT)
                target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 1));
            if (kind() == PrebossItem.Kind.THUNDER_ZAPPER || kind() == PrebossItem.Kind.STORM_SPEAR)
                target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 40, 0));
            if (boomerang()) return;
            int pierces =
                    kind() == PrebossItem.Kind.DIAMOND
                                    || kind() == PrebossItem.Kind.EMERALD
                                    || kind() == PrebossItem.Kind.SPARKING
                                    || kind() == PrebossItem.Kind.FROSTING
                            ? 2
                            : 1;
            if (hits < pierces) return;
        } else if (boomerang()) {
            ticksExisted = Math.max(ticksExisted, 14);
            return;
        } else if (kind() == PrebossItem.Kind.FROST_BOLT && hit.sideHit != null) {
            frostBurst(owner, hit.hitVec);
            if (bounces++ < 2) {
                Vec3i normal = hit.sideHit.getDirectionVec();
                if (normal.getX() != 0) motionX = -motionX * .8;
                if (normal.getY() != 0) motionY = -motionY * .8;
                if (normal.getZ() != 0) motionZ = -motionZ * .8;
                setPosition(
                        hit.hitVec.x + normal.getX() * .1,
                        hit.hitVec.y + normal.getY() * .1,
                        hit.hitVec.z + normal.getZ() * .1);
                return;
            }
        }
        if (kind() == PrebossItem.Kind.CRYSTALLINE && empowered && generation() < 3)
            for (int i = -1; i <= 1; i++) child(owner, i * 50, 3, damage * .3F);
        setDead();
    }

    private void frostBurst(EntityLivingBase owner, Vec3d at) {
        for (EntityLivingBase target :
                world.getEntitiesWithinAABB(
                        EntityLivingBase.class,
                        new AxisAlignedBB(
                                at.x - 1.5,
                                at.y - 1.5,
                                at.z - 1.5,
                                at.x + 1.5,
                                at.y + 1.5,
                                at.z + 1.5)))
            if (valid(owner, target) && struck.add(target.getUniqueID())) {
                target.attackEntityFrom(
                        DamageSource.causeIndirectDamage(this, owner).setMagicDamage(),
                        damage * .4F);
                target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 1));
            }
        if (world instanceof WorldServer)
            ((WorldServer) world)
                    .spawnParticle(
                            EnumParticleTypes.SNOW_SHOVEL, at.x, at.y, at.z, 12, .5, .4, .5, .04);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setInteger("Kind", kind().ordinal());
        tag.setInteger("Generation", generation());
        tag.setInteger("Age", ticksExisted);
        tag.setInteger("Bounces", bounces);
        tag.setInteger("Hits", hits);
        tag.setFloat("Damage", damage);
        tag.setBoolean("Empowered", empowered);
        tag.setBoolean("Split", split);
        tag.setBoolean("Returning", returning);
        tag.setBoolean("Summon", summonSource);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        dataManager.set(
                TYPE,
                MathHelper.clamp(tag.getInteger("Kind"), 0, PrebossItem.Kind.values().length - 1));
        dataManager.set(GENERATION, MathHelper.clamp(tag.getInteger("Generation"), 0, 3));
        ticksExisted = tag.getInteger("Age");
        bounces = tag.getInteger("Bounces");
        hits = tag.getInteger("Hits");
        damage = tag.getFloat("Damage");
        empowered = tag.getBoolean("Empowered");
        split = tag.getBoolean("Split");
        returning = tag.getBoolean("Returning");
        summonSource = tag.getBoolean("Summon");
    }
}
