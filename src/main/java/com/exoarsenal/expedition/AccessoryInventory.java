package com.exoarsenal.expedition;

import com.exoarsenal.ExoArsenal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.util.*;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.ItemStackHandler;

@Mod.EventBusSubscriber(modid = ExoArsenal.MODID)
public final class AccessoryInventory extends ItemStackHandler {
    @CapabilityInject(AccessoryInventory.class)
    public static Capability<AccessoryInventory> CAPABILITY;

    public AccessoryInventory() {
        super(5);
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(
                AccessoryInventory.class,
                new Capability.IStorage<AccessoryInventory>() {
                    @Override
                    public NBTBase writeNBT(
                            Capability<AccessoryInventory> capability,
                            AccessoryInventory inventory,
                            EnumFacing facing) {
                        return inventory.serializeNBT();
                    }

                    @Override
                    public void readNBT(
                            Capability<AccessoryInventory> capability,
                            AccessoryInventory inventory,
                            EnumFacing facing,
                            NBTBase nbt) {
                        if (nbt instanceof NBTTagCompound)
                            inventory.deserializeNBT((NBTTagCompound) nbt);
                    }
                },
                AccessoryInventory::new);
    }

    public static boolean accepts(Item item) {
        return item == WulfrumArsenal.HEART
                || item instanceof ExplorationAccessory
                || item == PrebossContent.REGEN_BAND
                || item == PrebossContent.STARPOWER_BAND
                || item == SeaContent.SHIELD
                || item == SeaContent.PENDANT
                || item == SeaContent.GIANT_PEARL
                || item == ExpeditionContent.SAND_CLOAK
                || item == ExpeditionContent.OCEAN_CREST
                || item == ExpeditionContent.BATTERY
                || item == ExpeditionContent.ROVER_DRIVE
                || item == ExpeditionContent.ACROBATICS;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (stack.isEmpty() || !accepts(stack.getItem())) return false;
        for (int i = 0; i < getSlots(); i++)
            if (i != slot && getStackInSlot(i).getItem() == stack.getItem()) return false;
        return true;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {

        NBTTagCompound bounded = tag.copy();
        bounded.setInteger("Size", 5);
        super.deserializeNBT(bounded);
    }

    public static AccessoryInventory get(EntityPlayer player) {
        return player.getCapability(CAPABILITY, null);
    }

    public static ItemStack equipped(EntityPlayer player, Item item) {
        if (player.getHeldItemOffhand().getItem() == item) return player.getHeldItemOffhand();
        AccessoryInventory inventory = get(player);
        if (inventory != null)
            for (int i = 0; i < inventory.getSlots(); i++)
                if (inventory.getStackInSlot(i).getItem() == item)
                    return inventory.getStackInSlot(i);
        return ItemStack.EMPTY;
    }

    public static boolean has(EntityPlayer player, Item item) {
        return !equipped(player, item).isEmpty();
    }

    public static int bottleTier(EntityPlayer player) {
        return has(player, PrebossContent.SANDSTORM_BOTTLE)
                ? 3
                : has(player, PrebossContent.BLIZZARD_BOTTLE)
                        ? 2
                        : has(player, PrebossContent.CLOUD_BOTTLE) ? 1 : 0;
    }

    public static int bottleMask(EntityPlayer player) {
        return (has(player, PrebossContent.CLOUD_BOTTLE) ? 1 : 0)
                | (has(player, PrebossContent.BLIZZARD_BOTTLE) ? 2 : 0)
                | (has(player, PrebossContent.SANDSTORM_BOTTLE) ? 4 : 0);
    }

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer)
            event.addCapability(
                    new ResourceLocation(ExoArsenal.MODID, "accessories"), new Provider());
    }

    private static final class Provider
            implements ICapabilityProvider, INBTSerializable<NBTTagCompound> {
        private final AccessoryInventory inventory = new AccessoryInventory();

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
            return capability == CAPABILITY;
        }

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
            return capability == CAPABILITY ? CAPABILITY.cast(inventory) : null;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            return inventory.serializeNBT();
        }

        @Override
        public void deserializeNBT(NBTTagCompound tag) {
            inventory.deserializeNBT(tag);
        }
    }
}
