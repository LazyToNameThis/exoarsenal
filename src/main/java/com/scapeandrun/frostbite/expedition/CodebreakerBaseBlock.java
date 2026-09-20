package com.scapeandrun.frostbite.expedition;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.entity.EntityDraedon;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class CodebreakerBaseBlock extends Block {
    public CodebreakerBaseBlock() {
        super(Material.IRON);
        setHardness(4);
        setResistance(30);
        setHarvestLevel("pickaxe", 1);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public net.minecraft.util.math.AxisAlignedBB getBoundingBox(
            IBlockState state, net.minecraft.world.IBlockAccess world, BlockPos pos) {
        return new net.minecraft.util.math.AxisAlignedBB(0, 0, 0, 1, .72, 1);
    }

    @Override
    public boolean onBlockActivated(
            World world,
            BlockPos pos,
            IBlockState state,
            EntityPlayer player,
            EnumHand hand,
            EnumFacing side,
            float x,
            float y,
            float z) {
        if (!world.isRemote) EntityDraedon.contact(world, pos, player);
        return true;
    }
}
