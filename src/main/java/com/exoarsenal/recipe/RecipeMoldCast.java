package com.exoarsenal.recipe;

import com.exoarsenal.registry.ModContent;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;
import slimeknights.tconstruct.library.tools.IToolPart;
import slimeknights.tconstruct.library.tools.Pattern;

public class RecipeMoldCast extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
    private final boolean fireclay;

    public RecipeMoldCast(boolean fireclay) {
        this.fireclay = fireclay;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        int clay = 0, parts = 0;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() == (fireclay ? ModContent.FIRECLAY_BALL : Items.CLAY_BALL))
                clay += stack.getCount();
            else if (stack.getItem() instanceof IToolPart
                    && ((IToolPart) stack.getItem()).canBeCasted()) parts++;
            else return false;
        }
        return clay == 2 && parts == 1;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        Item part = findPart(inv);
        if (part == null) return ItemStack.EMPTY;
        ItemStack result =
                new ItemStack(
                        fireclay ? ModContent.UNFIRED_FIRECLAY_CAST : ModContent.UNFIRED_CLAY_CAST);
        return Pattern.setTagForPart(result, part);
    }

    private Item findPart(InventoryCrafting inv) {
        for (int i = 0; i < inv.getSizeInventory(); i++)
            if (inv.getStackInSlot(i).getItem() instanceof IToolPart)
                return inv.getStackInSlot(i).getItem();
        return null;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 3;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(
                fireclay ? ModContent.UNFIRED_FIRECLAY_CAST : ModContent.UNFIRED_CLAY_CAST);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        Item clay = fireclay ? ModContent.FIRECLAY_BALL : Items.CLAY_BALL;
        ingredients.add(Ingredient.fromItem(clay));
        ingredients.add(Ingredient.fromItem(clay));
        java.util.List<ItemStack> parts = new java.util.ArrayList<>();
        for (Item item : net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS) {
            if (item instanceof IToolPart && ((IToolPart) item).canBeCasted())
                parts.add(new ItemStack(item));
        }
        if (!parts.isEmpty())
            ingredients.add(Ingredient.fromStacks(parts.toArray(new ItemStack[0])));
        return ingredients;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        NonNullList<ItemStack> remains =
                NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.getItem() instanceof IToolPart) {
                ItemStack copy = stack.copy();
                copy.setCount(1);
                remains.set(i, copy);
            }
        }
        return remains;
    }
}
