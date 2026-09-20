package com.exoarsenal.tile;

import com.exoarsenal.block.BlockPotteryKiln;
import com.exoarsenal.registry.ModContent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import slimeknights.tconstruct.library.tools.Pattern;

import java.util.ArrayList;
import java.util.List;

public class TileEntityPotteryKiln extends TileEntity implements ITickable, IInventory {
    public static final int FIRING_TIME = 2400;
    private static final List<MetalRecipe> LOW_TEMPERATURE_METALS = new ArrayList<>();
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(5, ItemStack.EMPTY);
    private int burnTime;
    private int firingTime;
    private boolean wasLit;

    public static void registerLowTemperatureMetalRecipe(ItemStack input, ItemStack output) {
        if (input.isEmpty() || output.isEmpty()) return;
        LOW_TEMPERATURE_METALS.add(new MetalRecipe(input.copy(), output.copy()));
    }

    @Override
    public void update() {
        if (world.isRemote) return;
        boolean canFire = hasCookableInput();
        if (burnTime <= 0 && canFire && isCharcoal(inventory.get(4))) {
            burnTime = 1600;
            inventory.get(4).shrink(1);
            if (inventory.get(4).isEmpty()) inventory.set(4, ItemStack.EMPTY);
            markDirty();
        }
        if (burnTime > 0) burnTime--;
        boolean lit = burnTime > 0 && canFire;
        if (lit) {
            firingTime++;
            if (firingTime >= FIRING_TIME) {
                fireBatch();
                firingTime = 0;
            }
        } else if (!canFire) firingTime = 0;
        if (lit != wasLit) {
            wasLit = lit;
            world.setBlockState(
                    pos, world.getBlockState(pos).withProperty(BlockPotteryKiln.LIT, lit), 3);
        }
    }

    private boolean hasCookableInput() {
        for (int slot = 0; slot < 4; slot++)
            if (!getFiringResult(inventory.get(slot)).isEmpty()) return true;
        return false;
    }

    private void fireBatch() {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack input = inventory.get(slot);
            ItemStack result = getFiringResult(input);
            if (result.isEmpty()) continue;
            result.setCount(input.getCount());
            inventory.set(slot, result);
        }
        markDirty();
    }

    public boolean insertOne(ItemStack source) {
        if (source.isEmpty()) return false;
        if (isCharcoal(source)) {
            ItemStack fuel = inventory.get(4);
            if (fuel.isEmpty())
                inventory.set(4, new ItemStack(source.getItem(), 1, source.getMetadata()));
            else if (ItemStack.areItemsEqual(fuel, source)
                    && fuel.getCount() < fuel.getMaxStackSize()) fuel.grow(1);
            else return false;
            markDirty();
            return true;
        }
        if (getFiringResult(source).isEmpty()) return false;
        boolean large = isLarge(source);
        if (large) {
            for (int i = 0; i < 4; i++) if (!inventory.get(i).isEmpty()) return false;
        } else if (isLarge(inventory.get(0))) return false;
        int maxSlot = large ? 1 : 4;
        for (int slot = 0; slot < maxSlot; slot++) {
            if (inventory.get(slot).isEmpty()) {
                ItemStack placed = source.copy();
                placed.setCount(1);
                inventory.set(slot, placed);
                markDirty();
                return true;
            }
        }
        return false;
    }

    public ItemStack extractOne() {
        for (int slot = 3; slot >= 0; slot--) {
            if (!inventory.get(slot).isEmpty()) {
                ItemStack result = inventory.get(slot);
                inventory.set(slot, ItemStack.EMPTY);
                markDirty();
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean isCharcoal(ItemStack stack) {
        return stack.getItem() == Items.COAL && stack.getMetadata() == 1;
    }

    private static boolean isLarge(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = stack.getItem().getRegistryName();
        String path = id == null ? "" : id.getResourcePath();
        return stack.getItem() instanceof ItemBlock
                || path.contains("vessel")
                || path.contains("jar")
                || path.contains("large")
                || path.contains("crucible");
    }

    public static ItemStack getFiringResult(ItemStack input) {
        if (input.isEmpty()) return ItemStack.EMPTY;
        if (input.getItem() == ModContent.UNFIRED_CLAY_CAST) {
            ItemStack result = new ItemStack(ModContent.CLAY_CAST);
            return Pattern.setTagForPart(result, Pattern.getPartFromTag(input));
        }
        if (input.getItem() == ModContent.UNFIRED_FIRECLAY_CAST) {
            ItemStack result = new ItemStack(ModContent.FIRECLAY_CAST);
            return Pattern.setTagForPart(result, Pattern.getPartFromTag(input));
        }
        for (MetalRecipe recipe : LOW_TEMPERATURE_METALS) {
            if (recipe.matches(input)) return recipe.output.copy();
        }
        ItemStack furnace = FurnaceRecipes.instance().getSmeltingResult(input);
        ResourceLocation id = input.getItem().getRegistryName();
        String domain = id == null ? "" : id.getResourceDomain();
        String path = id == null ? "" : id.getResourcePath();
        if (!furnace.isEmpty()
                && (input.getItem() == Items.CLAY_BALL
                        || "ceramics".equals(domain)
                        || "rustic".equals(domain)
                        || path.contains("unfired")
                        || path.contains("raw_clay"))) return furnace.copy();
        return ItemStack.EMPTY;
    }

    private static final class MetalRecipe {
        private final ItemStack input;
        private final ItemStack output;

        private MetalRecipe(ItemStack input, ItemStack output) {
            this.input = input;
            this.output = output;
        }

        private boolean matches(ItemStack candidate) {
            return input.getItem() == candidate.getItem()
                    && (input.getMetadata()
                                    == net.minecraftforge.oredict.OreDictionary.WILDCARD_VALUE
                            || input.getMetadata() == candidate.getMetadata())
                    && ItemStack.areItemStackTagsEqual(input, candidate);
        }
    }

    @Override
    public String getName() {
        return "container.exoarsenal.pottery_kiln";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation(getName());
    }

    @Override
    public int getSizeInventory() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : inventory) if (!stack.isEmpty()) return false;
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inventory.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return ItemStackHelper.getAndSplit(inventory, index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return ItemStackHelper.getAndRemove(inventory, index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventory.set(index, stack);
        markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return world.getTileEntity(pos) == this && player.getDistanceSq(pos) <= 64D;
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index == 4 ? isCharcoal(stack) : !getFiringResult(stack).isEmpty();
    }

    @Override
    public int getField(int id) {
        return id == 0 ? burnTime : firingTime;
    }

    @Override
    public void setField(int id, int value) {
        if (id == 0) burnTime = value;
        else firingTime = value;
    }

    @Override
    public int getFieldCount() {
        return 2;
    }

    @Override
    public void clear() {
        inventory.clear();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        ItemStackHelper.saveAllItems(tag, inventory);
        tag.setInteger("Burn", burnTime);
        tag.setInteger("Firing", firingTime);
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        ItemStackHelper.loadAllItems(tag, inventory);
        burnTime = tag.getInteger("Burn");
        firingTime = tag.getInteger("Firing");
    }
}
