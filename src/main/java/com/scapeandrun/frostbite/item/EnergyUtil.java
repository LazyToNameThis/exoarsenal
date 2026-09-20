package com.scapeandrun.frostbite.item;

import com.scapeandrun.frostbite.registry.ModContent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class EnergyUtil {
    private EnergyUtil() {}

    public static ICapabilitySerializable<NBTTagCompound> provider(int capacity, int transfer) {
        return new Provider(capacity, transfer);
    }

    @Nullable
    public static IEnergyStorage get(ItemStack stack) {
        return stack.isEmpty() ? null : stack.getCapability(CapabilityEnergy.ENERGY, null);
    }

    public static int stored(ItemStack stack) {
        IEnergyStorage energy = get(stack);
        return energy == null ? 0 : energy.getEnergyStored();
    }

    public static int capacity(ItemStack stack) {
        IEnergyStorage energy = get(stack);
        return energy == null ? 0 : energy.getMaxEnergyStored();
    }

    public static boolean drain(ItemStack stack, int amount, boolean simulate) {
        IEnergyStorage energy = get(stack);
        return energy != null && energy.extractEnergy(amount, simulate) == amount;
    }

    public static boolean drainArmor(EntityPlayer player, int amount, boolean simulate) {
        ItemStack[] order = {
            player.getItemStackFromSlot(EntityEquipmentSlot.CHEST),
            player.getItemStackFromSlot(EntityEquipmentSlot.HEAD),
            player.getItemStackFromSlot(EntityEquipmentSlot.LEGS),
            player.getItemStackFromSlot(EntityEquipmentSlot.FEET)
        };
        int remaining = amount;
        for (ItemStack stack : order) {
            if (!(stack.getItem() instanceof ItemRmorArmor)) continue;
            IEnergyStorage energy = get(stack);
            if (energy != null) remaining -= energy.extractEnergy(remaining, true);
        }
        if (remaining > 0) return false;
        if (!simulate) {
            remaining = amount;
            for (ItemStack stack : order) {
                if (!(stack.getItem() instanceof ItemRmorArmor)) continue;
                IEnergyStorage energy = get(stack);
                if (energy != null) remaining -= energy.extractEnergy(remaining, false);
                if (remaining <= 0) break;
            }
        }
        return true;
    }

    public static int chargeArmor(EntityPlayer player, int amount) {
        int remaining = amount;
        for (ItemStack stack :
                new ItemStack[] {
                    player.getItemStackFromSlot(EntityEquipmentSlot.CHEST),
                    player.getItemStackFromSlot(EntityEquipmentSlot.HEAD),
                    player.getItemStackFromSlot(EntityEquipmentSlot.LEGS),
                    player.getItemStackFromSlot(EntityEquipmentSlot.FEET)
                }) {
            if (!(stack.getItem() instanceof ItemRmorArmor)) continue;
            IEnergyStorage energy = get(stack);
            if (energy != null) remaining -= energy.receiveEnergy(remaining, false);
            if (remaining <= 0) break;
        }
        return amount - remaining;
    }

    public static boolean isRmor(ItemStack stack) {
        return stack.getItem() instanceof ItemRmorArmor;
    }

    private static final class Provider implements ICapabilitySerializable<NBTTagCompound> {
        private final Storage storage;

        private Provider(int capacity, int transfer) {
            storage = new Storage(capacity, transfer);
        }

        @Override
        public boolean hasCapability(
                @Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == CapabilityEnergy.ENERGY;
        }

        @Nullable
        @Override
        @SuppressWarnings("unchecked")
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            return capability == CapabilityEnergy.ENERGY ? (T) storage : null;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("Energy", storage.getEnergyStored());
            return tag;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            storage.setEnergy(nbt.getInteger("Energy"));
        }
    }

    private static final class Storage extends EnergyStorage {
        private Storage(int capacity, int transfer) {
            super(capacity, transfer, transfer);
        }

        private void setEnergy(int value) {
            energy = Math.max(0, Math.min(capacity, value));
        }
    }
}
