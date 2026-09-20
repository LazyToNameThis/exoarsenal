package com.exoarsenal.item;

import com.exoarsenal.entity.EntityX20Scout;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public final class ItemSuspiciousController extends Item {
    public ItemSuspiciousController() {
        setMaxStackSize(1);
    }

    @Override
    public EnumActionResult onItemUse(
            EntityPlayer player,
            World world,
            BlockPos pos,
            EnumHand hand,
            EnumFacing face,
            float x,
            float y,
            float z) {
        if (world.isRemote) return EnumActionResult.SUCCESS;
        if (!world.getEntitiesWithinAABB(
                        EntityX20Scout.class, player.getEntityBoundingBox().grow(192))
                .isEmpty()) {
            player.sendStatusMessage(new TextComponentString("Scout is already nearby."), true);
            return EnumActionResult.FAIL;
        }
        BlockPos ground = pos;
        while (ground.getY() > 0
                && world.getBlockState(ground).getBlock() == net.minecraft.init.Blocks.SNOW_LAYER)
            ground = ground.down();
        BlockPos place = ground.up();
        EntityX20Scout scout = new EntityX20Scout(world);
        scout.setPosition(place.getX() + .5, place.getY(), place.getZ() + .5);
        java.util.List<net.minecraft.util.math.AxisAlignedBB> obstacles =
                new java.util.ArrayList<>();
        net.minecraft.util.math.AxisAlignedBB bounds = scout.getEntityBoundingBox();
        for (BlockPos cell :
                BlockPos.getAllInBox(
                        new BlockPos(bounds.minX, bounds.minY, bounds.minZ),
                        new BlockPos(bounds.maxX, bounds.maxY, bounds.maxZ))) {
            net.minecraft.block.state.IBlockState state = world.getBlockState(cell);
            if (state.getBlock() != net.minecraft.init.Blocks.SNOW_LAYER)
                state.addCollisionBoxToList(world, cell, bounds, obstacles, scout, false);
        }
        if (!obstacles.isEmpty()
                || !world.checkNoEntityCollision(bounds, scout)
                || !world.getBlockState(ground).isSideSolid(world, ground, EnumFacing.UP)) {
            player.sendStatusMessage(
                    new TextComponentString("Scout needs a clear landing area on solid ground."),
                    true);
            return EnumActionResult.FAIL;
        }
        scout.setAttackTarget(player);
        scout.prepareEntrance();
        if (!world.spawnEntity(scout)) return EnumActionResult.FAIL;
        if (!player.isCreative()) player.getHeldItem(hand).shrink(1);
        return EnumActionResult.SUCCESS;
    }
}
