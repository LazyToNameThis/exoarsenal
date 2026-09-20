package com.exoarsenal.block;

import com.exoarsenal.config.ModConfig;
import com.exoarsenal.registry.ModContent;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import java.util.Random;

public class BlockRecoverySoil extends Block {
    public static final PropertyInteger STAGE = PropertyInteger.create("stage", 0, 6);
    public static final String[] NAMES = {
        "frostbitten", "barren", "poor", "recovering", "dirt", "fertile", "rich"
    };

    public BlockRecoverySoil() {
        super(Material.GROUND);
        setHardness(0.9F);
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
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == ModContent.TAB || tab == CreativeTabs.SEARCH)
            for (int i = 0; i < 7; i++) items.add(new ItemStack(this, 1, i));
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
        else drops.add(new ItemStack(this, 1, stage));
    }

    @Override
    public void randomTick(World world, BlockPos pos, IBlockState state, Random random) {
        int stage = state.getValue(STAGE);
        if (random.nextInt(ModConfig.soilTransitionRoll) != 0) return;
        if (stage == 0 && hasHeat(world, pos))
            world.setBlockState(pos, state.withProperty(STAGE, 1), 3);
        else if (stage == 1 && hasWater(world, pos))
            world.setBlockState(pos, state.withProperty(STAGE, 2), 3);
    }

    public static boolean hasHeat(World world, BlockPos pos) {
        for (BlockPos check : BlockPos.getAllInBoxMutable(pos.add(-1, -1, -1), pos.add(1, 1, 1))) {
            if (!world.isBlockLoaded(check, false)) continue;
            Block b = world.getBlockState(check).getBlock();
            if (b == Blocks.LIT_FURNACE
                    || b == Blocks.FIRE
                    || b == Blocks.FLOWING_LAVA
                    || b == Blocks.LAVA) return true;
        }
        return false;
    }

    public static boolean hasWater(World world, BlockPos pos) {
        for (BlockPos check : BlockPos.getAllInBoxMutable(pos.add(-4, -1, -4), pos.add(4, 1, 4))) {
            if (!world.isBlockLoaded(check, false)) continue;
            Material material = world.getBlockState(check).getMaterial();
            if (material == Material.WATER) return true;
        }
        return false;
    }
}
