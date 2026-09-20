package com.exoarsenal.expedition;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.*;
import net.minecraft.world.IBlockAccess;

public final class ScourgeDisplayBlock extends Block {
    private final boolean relic;

    public ScourgeDisplayBlock(boolean relic) {
        super(Material.ROCK);
        this.relic = relic;
        setHardness(2);
        setResistance(6);
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
        return new AxisAlignedBB(.1, 0, .1, .9, relic ? 1 : .75, .9);
    }
}
