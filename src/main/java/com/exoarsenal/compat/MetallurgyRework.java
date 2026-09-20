package com.exoarsenal.compat;

import com.exoarsenal.ExoArsenal;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Iterator;
import java.util.Map;

public final class MetallurgyRework {
    private MetallurgyRework() {}

    public static void apply() {
        int removed = 0;
        int movedToKiln = 0;
        Iterator<Map.Entry<ItemStack, ItemStack>> iterator =
                FurnaceRecipes.instance().getSmeltingList().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ItemStack, ItemStack> entry = iterator.next();
            if (isMetalIngot(entry.getValue())) {
                if (isKilnMetal(entry.getValue())) {
                    com.exoarsenal.tile.TileEntityPotteryKiln.registerLowTemperatureMetalRecipe(
                            entry.getKey(), entry.getValue());
                    movedToKiln++;
                }
                iterator.remove();
                removed++;
            }
        }
        ExoArsenal.LOGGER.info(
                "Removed {} vanilla-style furnace metallurgy recipes; moved {} low-temperature routes to the charcoal kiln",
                removed,
                movedToKiln);
        TConstructCompat.registerCastRecipes();
    }

    private static boolean isMetalIngot(ItemStack stack) {
        for (int id : OreDictionary.getOreIDs(stack)) {
            String name = OreDictionary.getOreName(id);
            if (name.startsWith("ingot")
                    && !name.equals("ingotBrick")
                    && !name.equals("ingotBrickNether")) return true;
        }
        return false;
    }

    private static boolean isKilnMetal(ItemStack stack) {
        for (int id : OreDictionary.getOreIDs(stack)) {
            String name = OreDictionary.getOreName(id);
            if (name.equals("ingotCopper")
                    || name.equals("ingotTin")
                    || name.equals("ingotLead")
                    || name.equals("ingotZinc")
                    || name.equals("ingotSilver")
                    || name.equals("ingotGold")
                    || name.equals("ingotAluminum")
                    || name.equals("ingotAluminium")
                    || name.equals("ingotBronze")
                    || name.equals("ingotBrass")
                    || name.equals("ingotElectrum")
                    || name.equals("ingotAluminumBrass")
                    || name.equals("ingotAlubrass")) return true;
        }
        return false;
    }
}
