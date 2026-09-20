package com.exoarsenal.expedition;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public final class EntitySnowFlinx extends EntityMob {
    public EntitySnowFlinx(World world) {
        super(world);
        setSize(.65F, .75F);
        experienceValue = 3;
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(2, new EntityAIAttackMelee(this, 1, true));
        tasks.addTask(5, new EntityAIWanderAvoidWater(this, .6));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        targetTasks.addTask(
                2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(14);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.23);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!world.isRemote && onGround && getAttackTarget() != null && ticksExisted % 18 == 0) {
            motionY = .32;
            isAirBorne = true;
        }
    }

    @Override
    public boolean getCanSpawnHere() {
        return world.provider.getDimension() == 0
                && posY < 60
                && com.exoarsenal.world.FrigidSpawnBiomes.isCold(world.getBiome(getPosition()))
                && super.getCanSpawnHere();
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        if (rand.nextBoolean()
                || com.exoarsenal.world.ExoArsenalWorldSettings.get(world).isExpert())
            dropItem(PrebossContent.FLINX_FUR, 1 + rand.nextInt(3));
    }
}
