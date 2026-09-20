package com.exoarsenal.compat;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.registry.ModContent;
import com.exoarsenal.tile.TileEntityPotteryKiln;
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
            ExoArsenal.LOGGER.warn("No {} entry was available for frozen river panning", oreName);
            return;
        }
        ItemStack output = outputs.get(0).copy();
        output.setCount(1);
        TileEntityPotteryKiln.registerLowTemperatureMetalRecipe(new ItemStack(input), output);
    }
}
