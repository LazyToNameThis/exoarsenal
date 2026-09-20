package com.scapeandrun.frostbite.expedition;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;

public final class EntityStormlion extends EntityMob {
    public EntityStormlion(World world) {
        super(world);
        setSize(1.15F, .65F);
        experienceValue = 5;
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(2, new EntityAIAttackMelee(this, 1, true));
        tasks.addTask(5, new EntityAIWanderAvoidWater(this, .65));
        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 12));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        targetTasks.addTask(
                2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(24);
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(8);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.18);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4);
        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(.8);
    }

    @Override
    public boolean attackEntityAsMob(Entity target) {
        boolean hit = super.attackEntityAsMob(target);
        if (hit && target instanceof EntityLivingBase)
            ((EntityLivingBase) target)
                    .addPotionEffect(
                            new net.minecraft.potion.PotionEffect(
                                    net.minecraft.init.MobEffects.WEAKNESS, 40, 0));
        return hit;
    }

    @Override
    public boolean getCanSpawnHere() {
        return world.provider.getDimension() == 0
                && BiomeDictionary.hasType(
                        world.getBiome(getPosition()), BiomeDictionary.Type.SANDY)
                && (!world.canSeeSky(getPosition()) || world.isThundering())
                && world.getCollisionBoxes(this, getEntityBoundingBox()).isEmpty()
                && world.checkNoEntityCollision(getEntityBoundingBox(), this)
                && !world.containsAnyLiquid(getEntityBoundingBox());
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        dropItem(PrebossContent.MANDIBLE, 1);
        if (rand.nextInt(5) == 0) dropItem(PrebossContent.STORMJAW, 1);
        if (rand.nextInt(25) == 0) dropItem(PrebossContent.STORM_SPEAR, 1);
        if (rand.nextInt(25) == 0) dropItem(PrebossContent.THUNDER_ZAPPER, 1);
    }
}
