package com.scapeandrun.frostbite.block;

import com.scapeandrun.frostbite.registry.ModContent;
import com.scapeandrun.frostbite.compat.MekanismCompat;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.storage.loot.LootContext;
import java.util.Random;

public class BlockFrozenLog extends Block {
    public BlockFrozenLog() {
        super(Material.WOOD);
        setHardness(2.5F);
        setResistance(5F);
        setSoundType(SoundType.WOOD);
    }

    @Override
    public void getDrops(
            NonNullList<ItemStack> drops,
            IBlockAccess world,
            BlockPos pos,
            IBlockState state,
            int fortune) {
        Random r =
                world instanceof net.minecraft.world.World
                        ? ((net.minecraft.world.World) world).rand
                        : new Random();
        drops.add(new ItemStack(ModContent.FROZEN_PLANKS, 2 + r.nextInt(2)));
        drops.add(new ItemStack(net.minecraft.init.Items.STICK, 1 + r.nextInt(3)));
        drops.add(MekanismCompat.sawdust(1 + r.nextInt(2)));
    }
}
