package com.exoarsenal.event;

import com.exoarsenal.block.BlockRecoveryFarmland;
import com.exoarsenal.block.BlockRecoverySoil;
import com.exoarsenal.registry.ModContent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.CropGrowEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SoilEventHandler {
    private static final float[] GROWTH_CHANCE = {0.04F, 0.08F, 0.22F, 0.48F, 1.0F, 1.0F, 1.0F};

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void till(PlayerInteractEvent.RightClickBlock event) {
        ItemStack held = event.getItemStack();
        if (!(held.getItem() instanceof ItemHoe) || event.getWorld().isRemote) return;
        IBlockState state = event.getWorld().getBlockState(event.getPos());
        int stage;
        if (state.getBlock() == ModContent.FROSTBITTEN_SOIL)
            stage = state.getValue(BlockRecoverySoil.STAGE);
        else if (state.getBlock() == Blocks.DIRT) stage = 4;
        else return;
        if (!event.getWorld().isAirBlock(event.getPos().up())) return;
        event.getWorld()
                .setBlockState(
                        event.getPos(),
                        ModContent.RECOVERY_FARMLAND
                                .getDefaultState()
                                .withProperty(BlockRecoveryFarmland.STAGE, stage),
                        3);
        event.getWorld()
                .playSound(
                        null,
                        event.getPos(),
                        SoundEvents.ITEM_HOE_TILL,
                        SoundCategory.BLOCKS,
                        1F,
                        1F);
        EntityPlayer player = event.getEntityPlayer();
        if (!player.capabilities.isCreativeMode) held.damageItem(1, player);
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
    }

    @SubscribeEvent
    public void slowCropGrowth(CropGrowEvent.Pre event) {
        BlockPos soilPos = event.getPos().down();
        IBlockState soil = event.getWorld().getBlockState(soilPos);
        if (soil.getBlock() != ModContent.RECOVERY_FARMLAND) return;
        int stage = soil.getValue(BlockRecoveryFarmland.STAGE);
        if (event.getWorld().rand.nextFloat() > GROWTH_CHANCE[stage])
            event.setResult(Event.Result.DENY);
    }
}
