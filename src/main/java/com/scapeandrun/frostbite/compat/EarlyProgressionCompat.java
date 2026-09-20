package com.scapeandrun.frostbite.compat;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.registry.ModContent;
import com.scapeandrun.frostbite.tile.TileEntityPotteryKiln;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public final class EarlyProgressionCompat {
    private EarlyProgressionCompat() {}

    public static void registerKilnMetals() {
        register(ModContent.SMALL_FROZEN_COPPER, "nuggetCopper");
        register(ModContent.SMALL_FROZEN_TIN, "nuggetTin");
    }

    private static void register(net.minecraft.item.Item input, String oreName) {
        java.util.List<ItemStack> outputs = OreDictionary.getOres(oreName, false);
        if (outputs.isEmpty()) {
            Frostbite.LOGGER.warn("No {} entry was available for frozen river panning", oreName);
            return;
        }
        ItemStack output = outputs.get(0).copy();
        output.setCount(1);
        TileEntityPotteryKiln.registerLowTemperatureMetalRecipe(new ItemStack(input), output);
    }
}
