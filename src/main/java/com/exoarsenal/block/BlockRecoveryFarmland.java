package com.exoarsenal.block;

import com.exoarsenal.config.ModConfig;
import com.exoarsenal.registry.ModContent;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import java.util.Random;

public class BlockRecoveryFarmland extends Block {
    public static final PropertyInteger STAGE = PropertyInteger.create("stage", 0, 6);

    public BlockRecoveryFarmland() {
        super(Material.GROUND);
        setHardness(0.8F);
        setResistance(3F);
        setSoundType(SoundType.GROUND);
        setHarvestLevel("shovel", 0);
        setTickRandomly(true);
        setDefaultState(blockState.getBaseState().withProperty(STAGE, 0));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(STAGE, Math.max(0, Math.min(6, meta)));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(STAGE);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, STAGE);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(STAGE);
    }

    @Override
    public boolean canSustainPlant(
            IBlockState state,
            IBlockAccess world,
            BlockPos pos,
            EnumFacing direction,
            IPlantable plantable) {
        return plantable.getPlantType(world, pos.up()) == EnumPlantType.Crop
                || super.canSustainPlant(state, world, pos, direction, plantable);
    }

    @Override
    public void getDrops(
            NonNullList<ItemStack> drops,
            IBlockAccess world,
            BlockPos pos,
            IBlockState state,
            int fortune) {
        int stage = state.getValue(STAGE);
        if (stage == 4) drops.add(new ItemStack(Blocks.DIRT));
        else drops.add(new ItemStack(ModContent.FROSTBITTEN_SOIL, 1, stage));
    }

    @Override
    public void randomTick(World world, BlockPos pos, IBlockState state, Random random) {
        int stage = state.getValue(STAGE);
        if (random.nextInt(ModConfig.soilTransitionRoll) != 0) return;
        if (stage == 0 && BlockRecoverySoil.hasHeat(world, pos)) advance(world, pos, state, 1);
        else if (stage == 1 && BlockRecoverySoil.hasWater(world, pos))
            advance(world, pos, state, 2);
        else if (stage >= 2 && stage < 6 && isMatureCrop(world, pos.up()))
            advance(world, pos, state, stage + 1);
    }

    private void advance(World world, BlockPos pos, IBlockState state, int stage) {
        world.setBlockState(pos, state.withProperty(STAGE, stage), 3);
    }

    public static boolean isMatureCrop(World world, BlockPos pos) {
        IBlockState crop = world.getBlockState(pos);
        if (!(crop.getBlock() instanceof IGrowable)) return false;
        try {
            return !((IGrowable) crop.getBlock()).canGrow(world, pos, crop, false);
        } catch (RuntimeException ignored) {
            return false;
        }
    }
}
