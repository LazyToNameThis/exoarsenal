package com.scapeandrun.frostbite.recipe;

import com.scapeandrun.frostbite.registry.ModContent;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RecipeKnifePattern extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        int hewn = 0, knives = 0;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() == ModContent.HEWN_STICKS) hewn++;
            else if (isKnife(stack)) knives++;
            else return false;
        }
        return hewn == 1 && knives == 1 && patternItem() != null;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        Item item = patternItem();
        return item == null ? ItemStack.EMPTY : new ItemStack(item, 4);
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        Item item = patternItem();
        return item == null ? ItemStack.EMPTY : new ItemStack(item, 4);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(Ingredient.fromItem(ModContent.HEWN_STICKS));
        ingredients.add(Ingredient.fromItem(ModContent.FLINT_KNIFE));
        return ingredients;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        NonNullList<ItemStack> result =
                NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty() && isKnife(stack)) {
                ItemStack knife = stack.copy();
                knife.setCount(1);
                if (knife.isItemStackDamageable()) {
                    knife.setItemDamage(knife.getItemDamage() + 1);
                    if (knife.getItemDamage() < knife.getMaxDamage()) result.set(i, knife);
                } else result.set(i, knife);
            }
        }
        return result;
    }

    private static Item patternItem() {
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation("tconstruct", "pattern"));
    }

    public static boolean isKnife(ItemStack stack) {
        if (stack.getItem() == ModContent.FLINT_KNIFE) return true;
        for (int id : OreDictionary.getOreIDs(stack))
            if (OreDictionary.getOreName(id).equals("toolKnife")) return true;
        ResourceLocation name = stack.getItem().getRegistryName();
        return name != null && name.getResourcePath().contains("knife");
    }
}
