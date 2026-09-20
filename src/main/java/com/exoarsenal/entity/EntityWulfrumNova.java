package com.exoarsenal.entity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public final class EntityWulfrumNova extends Entity {
    public EntityWulfrumNova(World world) {
        super(world);
        setSize(.1F, .1F);
        noClip = true;
        setNoGravity(true);
        ignoreFrustumCheck = true;
    }

    public EntityWulfrumNova(EntityWulfrumEye eye) {
        this(eye.world);
        setPosition(eye.posX, eye.posY + 1.5, eye.posZ);
    }

    @Override
    protected void entityInit() {}

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!world.isRemote) {
            if (ticksExisted == 1)
                world.playSound(
                        null,
                        posX,
                        posY,
                        posZ,
                        SoundEvents.ENTITY_FIREWORK_LAUNCH,
                        SoundCategory.HOSTILE,
                        1.4F,
                        .6F);
            if (ticksExisted == WulfrumNova.BURST)
                world.playSound(
                        null,
                        posX,
                        posY,
                        posZ,
                        SoundEvents.ENTITY_GENERIC_EXPLODE,
                        SoundCategory.HOSTILE,
                        3,
                        .65F);
            if (ticksExisted >= WulfrumNova.END) setDead();
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound n) {
        setDead();
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound n) {}
}
