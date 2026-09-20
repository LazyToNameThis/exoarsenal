package com.scapeandrun.frostbite.item;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;

public class ItemKXMultitool extends ItemX10Multitool {
    public static final int CAPACITY = 4000000;

    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        if (getForm(stack) == DRILL) return 48.0F;
        if (getForm(stack) == SAW && state.getMaterial() == Material.WOOD) return 36.0F;
        if (getForm(stack) == SCYTHE
                && (state.getMaterial() == Material.PLANTS || state.getMaterial() == Material.VINE))
            return 60.0F;
        return 1.0F;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return EnergyUtil.provider(CAPACITY, 96000);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return EnergyUtil.stored(stack) < CAPACITY;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1.0D - EnergyUtil.stored(stack) / (double) CAPACITY;
    }
}
