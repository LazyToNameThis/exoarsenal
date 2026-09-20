package com.scapeandrun.frostbite.compat;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Map;
import java.lang.reflect.Method;

public final class MekanismCompat {
    private static final ResourceLocation SAWDUST_ID = new ResourceLocation("mekanism", "sawdust");

    private MekanismCompat() {}

    public static ItemStack sawdust(int count) {
        Item sawdust = ForgeRegistries.ITEMS.getValue(SAWDUST_ID);

        if (sawdust == null) {
            for (Map.Entry<ResourceLocation, Item> entry : ForgeRegistries.ITEMS.getEntries()) {
                ResourceLocation id = entry.getKey();
                if ("mekanism".equals(id.getResourceDomain())
                        && "sawdust".equalsIgnoreCase(id.getResourcePath())) {
                    sawdust = entry.getValue();
                    break;
                }
            }
        }

        if (sawdust == null) {
            throw new IllegalStateException(
                    "Mekanism Community Edition sawdust is required but was not registered");
        }
        return new ItemStack(sawdust, count);
    }

    public static void registerEnergyCoreRecipe() {
        ItemStack alloy = firstOre("itemEnrichedAlloy", "alloyAdvanced");
        if (alloy.isEmpty()) {
            Item item =
                    ForgeRegistries.ITEMS.getValue(
                            new ResourceLocation("mekanism", "enrichedalloy"));
            if (item != null) alloy = new ItemStack(item);
        }
        if (alloy.isEmpty())
            throw new IllegalStateException("Mekanism Infused Alloy was not registered");
        try {
            Class<?> registry = Class.forName("mekanism.api.infuse.InfuseRegistry");
            Object diamond = registry.getMethod("get", String.class).invoke(null, "DIAMOND");
            Class<?> type = Class.forName("mekanism.api.infuse.InfuseType");

            Class<?> helper = Class.forName("mekanism.common.recipe.RecipeHandler");
            Method add =
                    helper.getMethod(
                            "addMetallurgicInfuserRecipe",
                            type,
                            int.class,
                            ItemStack.class,
                            ItemStack.class);
            add.invoke(
                    null,
                    diamond,
                    40,
                    alloy,
                    new ItemStack(
                            com.scapeandrun.frostbite.registry.ModContent.PROTOTYPE_ENERGY_CORE));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Unable to register the Mekanism Community Edition energy-core infuser recipe",
                    e);
        }
    }

    private static ItemStack firstOre(String... names) {
        for (String name : names) {
            java.util.List<ItemStack> ores = net.minecraftforge.oredict.OreDictionary.getOres(name);
            if (!ores.isEmpty()) {
                ItemStack result = ores.get(0).copy();
                result.setCount(1);
                return result;
            }
        }
        return ItemStack.EMPTY;
    }
}
