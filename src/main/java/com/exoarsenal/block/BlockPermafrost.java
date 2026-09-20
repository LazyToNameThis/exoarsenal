package com.exoarsenal.block;

import com.exoarsenal.registry.ModContent;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import java.util.Random;

public class BlockPermafrost extends Block {
    public BlockPermafrost() {
        super(Material.GROUND);
        setHardness(0.8F);
        setResistance(3F);
        setSoundType(SoundType.SNOW);
        setHarvestLevel("shovel", 0);
    }

    @Override
    public void getDrops(
            NonNullList<ItemStack> drops,
            IBlockAccess access,
            BlockPos pos,
            IBlockState state,
            int fortune) {
        Random random = access instanceof World ? ((World) access).rand : new Random();
        if (random.nextFloat() < 0.50F) drops.add(new ItemStack(Items.DYE, 1, 15));
        if (random.nextFloat() < 0.25F) drops.add(new ItemStack(Items.CLAY_BALL));
        if (random.nextFloat() < 0.25F) drops.add(new ItemStack(ModContent.DRYGRASS));
        if (random.nextFloat() < 0.125F) drops.add(new ItemStack(ModContent.FLINT_SHARD));
        if (random.nextFloat() < 0.125F) drops.add(new ItemStack(Items.STICK));
    }
}
