package com.exoarsenal.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

public class EntityRBladeSlash extends Entity {
    private static final DataParameter<Integer> PATTERN =
            EntityDataManager.createKey(EntityRBladeSlash.class, DataSerializers.VARINT);

    public EntityRBladeSlash(World world) {
        super(world);
        setSize(0.1F, 0.1F);
        noClip = true;
        ignoreFrustumCheck = true;
    }

    public EntityRBladeSlash(World world, EntityLivingBase attacker, int pattern) {
        this(world);
        dataManager.set(PATTERN, pattern);
        setPosition(attacker.posX, attacker.posY + attacker.getEyeHeight() * 0.72D, attacker.posZ);
        rotationYaw = attacker.rotationYaw;
        prevRotationYaw = rotationYaw;
        rotationPitch = attacker.rotationPitch * 0.35F;
        prevRotationPitch = rotationPitch;
    }

    @Override
    protected void entityInit() {
        dataManager.register(PATTERN, 0);
    }

    public int getPattern() {
        return dataManager.get(PATTERN);
    }

    public int getLifetime() {
        int pattern = getPattern();
        return pattern >= 7 ? 17 : pattern >= 5 ? 17 : pattern >= 3 ? 14 : 12;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!world.isRemote && ticksExisted >= getLifetime()) setDead();
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        dataManager.set(PATTERN, tag.getInteger("Pattern"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        tag.setInteger("Pattern", getPattern());
    }
}
