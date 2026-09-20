package com.exoarsenal.expedition;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public final class AccessoryContainer extends Container {
    private final EntityPlayer owner;

    public AccessoryContainer(EntityPlayer player) {
        owner = player;
        AccessoryInventory accessories = AccessoryInventory.get(player);
        if (accessories == null) throw new IllegalStateException("Missing accessory capability");
        for (int i = 0; i < 5; i++)
            addSlotToContainer(new SlotItemHandler(accessories, i, 44 + i * 18, 33));
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlotToContainer(
                        new Slot(player.inventory, 9 + row * 9 + col, 8 + col * 18, 82 + row * 18));
        for (int col = 0; col < 9; col++)
            addSlotToContainer(new Slot(player.inventory, col, 8 + col * 18, 140));
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return player == owner && player.isEntityAlive() && !player.isSpectator();
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        if (index < 0 || index >= inventorySlots.size()) return ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);
        if (!slot.getHasStack()) return ItemStack.EMPTY;
        ItemStack stack = slot.getStack(), original = stack.copy();
        if (index < 5) {
            if (!mergeItemStack(stack, 5, 41, true)) return ItemStack.EMPTY;
        } else if (AccessoryInventory.accepts(stack.getItem())) {
            if (!mergeItemStack(stack, 0, 5, false)) return ItemStack.EMPTY;
        } else if (index < 32) {
            if (!mergeItemStack(stack, 32, 41, false)) return ItemStack.EMPTY;
        } else if (!mergeItemStack(stack, 5, 32, false)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.putStack(ItemStack.EMPTY);
        else slot.onSlotChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }
}
