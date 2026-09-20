package com.exoarsenal.expedition;

import net.minecraft.block.BlockBush;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import java.util.Random;

public final class ShiverthornBlock extends BlockBush {
    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 2);

    public ShiverthornBlock() {
        setDefaultState(blockState.getBaseState().withProperty(AGE, 0));
        setTickRandomly(true);
        setHardness(0);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AGE);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(AGE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(AGE, Math.max(0, Math.min(2, meta)));
    }

    @Override
    protected boolean canSustainBush(IBlockState state) {
        Material material = state.getMaterial();
        return material == Material.GRASS
                || material == Material.GROUND
                || material == Material.CRAFTED_SNOW;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        super.updateTick(world, pos, state, random);
        if (world.getBlockState(pos).getBlock() == this
                && state.getValue(AGE) < 2
                && random.nextInt(8) == 0)
            world.setBlockState(pos, state.withProperty(AGE, state.getValue(AGE) + 1), 2);
    }

    @Override
    public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        return canSustainBush(world.getBlockState(pos.down()));
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return pos.getY() > 0
                && pos.getY() < 256
                && world.getBlockState(pos).getBlock().isReplaceable(world, pos)
                && canBlockStay(world, pos, getDefaultState());
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new HerbTile();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return new AxisAlignedBB(.2, 0, .2, .8, .35 + state.getValue(AGE) * .25, .8);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return PrebossContent.SHIVERTHORN;
    }

    @Override
    public void getDrops(
            NonNullList<ItemStack> drops,
            IBlockAccess world,
            BlockPos pos,
            IBlockState state,
            int fortune) {
        drops.add(new ItemStack(PrebossContent.SHIVERTHORN, state.getValue(AGE) == 2 ? 2 : 1));
    }

    @Override
    public boolean onBlockActivated(
            World world,
            BlockPos pos,
            IBlockState state,
            EntityPlayer player,
            EnumHand hand,
            EnumFacing face,
            float hitX,
            float hitY,
            float hitZ) {
        if (state.getValue(AGE) != 2 || !player.canPlayerEdit(pos, face, player.getHeldItem(hand)))
            return false;
        if (!world.isRemote) {
            world.setBlockState(pos, state.withProperty(AGE, 0), 2);
            spawnAsEntity(
                    world,
                    pos.up(),
                    new ItemStack(PrebossContent.SHIVERTHORN, 1 + world.rand.nextInt(2)));
        }
        return true;
    }

    public static final class HerbTile extends TileEntity {}
}
