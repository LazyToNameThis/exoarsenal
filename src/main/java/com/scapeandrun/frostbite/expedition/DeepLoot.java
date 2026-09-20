package com.scapeandrun.frostbite.expedition;

import java.util.*;
import net.minecraft.item.*;
import net.minecraft.init.Items;

public final class DeepLoot {
    public static List<ItemStack> cache(boolean shadow, Random random) {
        List<ItemStack> loot = new ArrayList<>();
        Item[] choices =
                shadow
                        ? new Item[] {
                            DeepContent.FLAMELASH,
                            DeepContent.DEMON_SCYTHE,
                            PrebossContent.OBSIDIAN_SKULL
                        }
                        : new Item[] {
                            DeepContent.MURAMASA, DeepContent.WATER_BOLT, PrebossContent.REGEN_BAND
                        };
        loot.add(new ItemStack(choices[random.nextInt(choices.length)]));
        if (!shadow) loot.add(new ItemStack(DeepContent.SHADOW_KEY));
        else loot.add(new ItemStack(DeepContent.HELLSTONE, 3 + random.nextInt(5)));
        loot.add(new ItemStack(ExpeditionContent.HEALING_POTION, 2));
        loot.add(new ItemStack(PrebossContent.RECALL_POTION, 2));
        loot.add(new ItemStack(Items.GOLD_NUGGET, 4 + random.nextInt(6)));
        return loot;
    }
}
