package com.exoarsenal.item;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import javax.annotation.Nullable;

public class ItemKXArmor extends ItemX10Armor {
    private final int kxCapacity;

    public ItemKXArmor(ArmorMaterial material, EntityEquipmentSlot slot, int capacity) {
        super(material, slot, capacity);
        kxCapacity = capacity;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return EnergyUtil.provider(kxCapacity, 128000);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return EnergyUtil.stored(stack) < kxCapacity;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1.0D - EnergyUtil.stored(stack) / (double) kxCapacity;
    }
}
