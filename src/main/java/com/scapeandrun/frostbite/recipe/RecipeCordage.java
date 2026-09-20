package com.scapeandrun.frostbite.recipe;

import com.scapeandrun.frostbite.registry.ModContent;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RecipeCordage extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        int grass = 0, sticks = 0;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() == ModContent.DRYGRASS) grass++;
            else if (stack.getItem() == Items.STICK) sticks++;
            else return false;
        }
        return grass == 3 && sticks == 1;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        return new ItemStack(ModContent.CRUDE_CORDAGE);
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 4;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(ModContent.CRUDE_CORDAGE);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(Ingredient.fromItem(ModContent.DRYGRASS));
        ingredients.add(Ingredient.fromItem(ModContent.DRYGRASS));
        ingredients.add(Ingredient.fromItem(ModContent.DRYGRASS));
        ingredients.add(Ingredient.fromItem(Items.STICK));
        return ingredients;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        NonNullList<ItemStack> result =
                NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        for (int i = 0; i < inv.getSizeInventory(); i++)
            if (inv.getStackInSlot(i).getItem() == Items.STICK)
                result.set(i, new ItemStack(Items.STICK));
        return result;
    }
}
