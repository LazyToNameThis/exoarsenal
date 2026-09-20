package com.scapeandrun.frostbite.block;

import com.scapeandrun.frostbite.registry.ModContent;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.Random;

public class BlockClimateStabilizer extends Block {
    public BlockClimateStabilizer() {
        super(Material.IRON);
        setHardness(5F);
        setResistance(16F);
        setSoundType(SoundType.METAL);
        setCreativeTab(ModContent.TAB);
        setTickRandomly(true);
    }

    @Override
    public void randomTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (!world.isBlockPowered(pos)) return;
        for (int i = 0; i < 24; i++) {
            BlockPos target =
                    pos.add(random.nextInt(17) - 8, random.nextInt(7) - 3, random.nextInt(17) - 8);
            if (!world.isBlockLoaded(target, false)) continue;
            Block block = world.getBlockState(target).getBlock();
            if (block == ModContent.FROSTBITTEN_SOIL
                    && world.getBlockState(target).getValue(BlockRecoverySoil.STAGE) == 6)
                world.setBlockState(target, Blocks.GRASS.getDefaultState(), 2);
            else if (block == Blocks.SNOW_LAYER || block == Blocks.SNOW)
                world.setBlockToAir(target);
        }
    }
}
