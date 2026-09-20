package com.scapeandrun.frostbite.item;

import com.scapeandrun.frostbite.block.BlockRecoverySoil;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockRecoverySoil extends ItemBlock {
    public ItemBlockRecoverySoil(Block block) {
        super(block);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return "tile.exoarsenal.soil."
                + BlockRecoverySoil.NAMES[Math.max(0, Math.min(6, stack.getMetadata()))];
    }
}
