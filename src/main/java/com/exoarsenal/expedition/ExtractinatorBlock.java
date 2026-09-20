package com.exoarsenal.expedition;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class ExtractinatorBlock extends Block {
    public ExtractinatorBlock() {
        super(Material.IRON);
        setHardness(3);
        setHarvestLevel("pickaxe", 1);
    }

    @Override
    public boolean onBlockActivated(
            World world,
            BlockPos pos,
            IBlockState state,
            EntityPlayer player,
            EnumHand hand,
            EnumFacing face,
            float x,
            float y,
            float z) {
        ItemStack held = player.getHeldItem(hand);
        Block input = Block.getBlockFromItem(held.getItem());
        if (input != GeologyContent.SILT
                && input != GeologyContent.SLUSH
                && input != GeologyContent.FOSSIL) return false;
        if (!player.canPlayerEdit(pos, face, held)) return false;
        if (world.isRemote) return true;
        long now = world.getTotalWorldTime();
        if (now < player.getEntityData().getLong("ExoArsenalExtractReady")) return true;
        player.getEntityData().setLong("ExoArsenalExtractReady", now + 5);
        if (!player.isCreative()) held.shrink(1);
        int roll = world.rand.nextInt(100);
        ItemStack result;
        if (input == GeologyContent.FOSSIL && roll < 20) result = new ItemStack(SeaContent.FOSSIL);
        else if (roll < 48) {
            switch (world.rand.nextInt(8)) {
                case 0:
                    result =
                            new ItemStack(
                                    GeologyContent.ORE, 1, GeologyContent.Ore.COPPER.ordinal());
                    break;
                case 1:
                    result = new ItemStack(GeologyContent.ORE, 1, GeologyContent.Ore.TIN.ordinal());
                    break;
                case 2:
                    result = new ItemStack(Blocks.IRON_ORE);
                    break;
                case 3:
                    result =
                            new ItemStack(GeologyContent.ORE, 1, GeologyContent.Ore.LEAD.ordinal());
                    break;
                case 4:
                    result =
                            new ItemStack(
                                    GeologyContent.ORE, 1, GeologyContent.Ore.SILVER.ordinal());
                    break;
                case 5:
                    result =
                            new ItemStack(
                                    GeologyContent.ORE, 1, GeologyContent.Ore.TUNGSTEN.ordinal());
                    break;
                case 6:
                    result = new ItemStack(Blocks.GOLD_ORE);
                    break;
                default:
                    result =
                            new ItemStack(
                                    GeologyContent.ORE, 1, GeologyContent.Ore.PLATINUM.ordinal());
                    break;
            }
        } else if (roll < 72) {
            switch (world.rand.nextInt(7)) {
                case 0:
                    result = GeologyContent.preferred(GeologyContent.Ore.AMETHYST);
                    break;
                case 1:
                    result = GeologyContent.preferred(GeologyContent.Ore.TOPAZ);
                    break;
                case 2:
                    result = GeologyContent.preferred(GeologyContent.Ore.SAPPHIRE);
                    break;
                case 3:
                    result = new ItemStack(Items.EMERALD);
                    break;
                case 4:
                    result = GeologyContent.preferred(GeologyContent.Ore.RUBY);
                    break;
                case 5:
                    result = new ItemStack(Items.DIAMOND);
                    break;
                default:
                    result = GeologyContent.preferred(GeologyContent.Ore.AMBER);
                    break;
            }
        } else result = new ItemStack(Items.GOLD_NUGGET, 1 + world.rand.nextInt(3));
        if (!player.inventory.addItemStackToInventory(result)) player.dropItem(result, false);
        world.playSound(
                null, pos, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, .4F, 1.2F);
        return true;
    }
}
