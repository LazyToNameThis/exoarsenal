package com.scapeandrun.frostbite.expedition;

import net.minecraft.entity.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;

public final class EntityCnidrion extends EntityMob {
    private static final DataParameter<Integer> CAST =
            EntityDataManager.createKey(EntityCnidrion.class, DataSerializers.VARINT);
    private int cooldown = 60, remaining;

    public EntityCnidrion(World world) {
        super(world);
        setSize(1.1F, 2.2F);
        setNoGravity(true);
        noClip = true;
        experienceValue = 8;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(CAST, 0);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(56);
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(6);
        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(.95);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(0);
    }

    public int cast() {
        return dataManager.get(CAST);
    }

    @Override
    public boolean attackEntityAsMob(Entity target) {
        return false;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (world.isRemote) return;
        EntityPlayer target = world.getNearestAttackablePlayer(this, 28, 18);
        if (target == null) {
            motionX *= .8;
            motionY *= .8;
            motionZ *= .8;
            dataManager.set(CAST, 0);
            return;
        }
        Vec3d delta =
                target.getPositionVector()
                        .addVector(0, 1 + Math.sin(ticksExisted * .06) * .4, 0)
                        .subtract(getPositionVector());
        boolean moving =
                remaining == 0
                        || SeaCombatRules.cnidrionMovesWhileCasting(getHealth() / getMaxHealth());
        Vec3d velocity =
                moving && delta.lengthSquared() > 36 ? delta.normalize().scale(.13) : Vec3d.ZERO;
        motionX = velocity.x;
        motionY = velocity.y;
        motionZ = velocity.z;
        rotationYaw = renderYawOffset = (float) -Math.toDegrees(Math.atan2(delta.x, delta.z));
        if (remaining > 0) {
            dataManager.set(CAST, remaining);
            if (ticksExisted % 3 == 0) {
                EntityExpeditionShot shot =
                        new EntityExpeditionShot(world, this, EntityExpeditionShot.WATER, 4);
                Vec3d muzzle =
                        getPositionEyes(1)
                                .addVector(
                                        -Math.sin(Math.toRadians(rotationYaw)) * .65,
                                        0,
                                        Math.cos(Math.toRadians(rotationYaw)) * .65);
                shot.setPosition(muzzle.x, muzzle.y, muzzle.z);
                Vec3d direction = target.getPositionEyes(1).subtract(muzzle);
                shot.shoot(direction.x, direction.y, direction.z, .85F, remaining > 4 ? 4 : 1);
                world.spawnEntity(shot);
                remaining--;
            }
        } else if (--cooldown <= 0) {
            remaining = rand.nextBoolean() ? 4 : 12;
            cooldown = SeaCombatRules.cnidrionInterval(getHealth() / getMaxHealth());
        } else dataManager.set(CAST, 0);
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        move(MoverType.SELF, motionX, motionY, motionZ);
    }

    @Override
    public boolean getCanSpawnHere() {
        return world.provider.getDimension() == 0
                && world.canSeeSky(getPosition())
                && BiomeDictionary.hasType(
                        world.getBiome(getPosition()), BiomeDictionary.Type.SANDY)
                && world.checkNoEntityCollision(getEntityBoundingBox(), this);
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        entityDropItem(new net.minecraft.item.ItemStack(SeaContent.FOSSIL, 4 + rand.nextInt(2)), 0);
        if (rand.nextInt(4) == 0) dropItem(SeaContent.ILMERIS_SPARK, 1);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setInteger("WaterCooldown", cooldown);
        tag.setInteger("BurstRemaining", remaining);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        cooldown = MathHelper.clamp(tag.getInteger("WaterCooldown"), 0, 100);
        remaining = MathHelper.clamp(tag.getInteger("BurstRemaining"), 0, 12);
    }
}
