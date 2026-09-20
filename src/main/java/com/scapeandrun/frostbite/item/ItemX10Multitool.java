package com.scapeandrun.frostbite.item;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;

public class ItemX10Multitool extends ItemRTool {
    public static final int X10_CAPACITY = 1500000;

    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        return getForm(stack) == DRILL ? 32.0F : getForm(stack) == SAW ? 22.0F : 1.0F;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return EnergyUtil.provider(X10_CAPACITY, 48000);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return EnergyUtil.stored(stack) < X10_CAPACITY;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1.0D - EnergyUtil.stored(stack) / (double) X10_CAPACITY;
    }
}
