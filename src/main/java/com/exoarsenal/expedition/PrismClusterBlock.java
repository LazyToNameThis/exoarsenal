package com.exoarsenal.expedition;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.math.*;
import net.minecraft.world.IBlockAccess;
import java.util.Random;

public final class PrismClusterBlock extends Block {
    public PrismClusterBlock() {
        super(Material.GLASS);
        setHardness(.8F);
        setLightLevel(.8F);
    }

    @Override
    public boolean isOpaqueCube(IBlockState s) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState s) {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState s, IBlockAccess w, BlockPos p) {
        return new AxisAlignedBB(.15, 0, .15, .85, .9, .85);
    }

    @Override
    public Item getItemDropped(IBlockState s, Random r, int fortune) {
        return ExpeditionContent.PRISM_SHARD;
    }

    @Override
    public int quantityDropped(Random r) {
        return 1 + r.nextInt(3);
    }
}
