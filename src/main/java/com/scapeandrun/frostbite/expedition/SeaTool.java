package com.scapeandrun.frostbite.expedition;

import net.minecraft.item.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import java.util.Collections;

public final class SeaTool extends ItemTool {
    private final boolean hamaxe;

    public SeaTool(boolean hamaxe) {
        super(hamaxe ? 5 : 3, -2.8F, ToolMaterial.IRON, Collections.emptySet());
        this.hamaxe = hamaxe;
        setMaxDamage(450);
        setHarvestLevel(hamaxe ? "axe" : "pickaxe", 2);
        if (hamaxe) setHarvestLevel("shovel", 2);
    }

    private boolean effective(IBlockState state) {
        Material m = state.getMaterial();
        return hamaxe
                ? m == Material.WOOD
                        || m == Material.PLANTS
                        || m == Material.VINE
                        || m == Material.GROUND
                        || m == Material.SAND
                : m == Material.ROCK || m == Material.IRON || m == Material.ANVIL;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        return effective(state) ? 7 : super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean canHarvestBlock(IBlockState state) {
        return effective(state) && state.getBlock().getHarvestLevel(state) <= 2;
    }

    @Override
    public boolean getIsRepairable(ItemStack tool, ItemStack ingredient) {
        return ingredient.getItem() == SeaContent.REMAINS;
    }
}
