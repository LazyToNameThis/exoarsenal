package com.scapeandrun.frostbite.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import java.util.Random;

public class BlockFrozenStone extends Block {
    public BlockFrozenStone() {
        super(Material.ROCK);
        setHardness(2.2F);
        setResistance(8F);
        setSoundType(SoundType.STONE);
        setHarvestLevel("pickaxe", 0);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Item.getItemFromBlock(Blocks.COBBLESTONE);
    }
}
