package com.exoarsenal.item;

import com.exoarsenal.registry.ModContent;
import com.exoarsenal.world.ModBiomes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.items.ItemHandlerHelper;

public final class ItemClayRiverPan extends Item {
    public ItemClayRiverPan() {
        setMaxStackSize(1);
        setMaxDamage(48);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(
            World world, EntityPlayer player, EnumHand hand) {
        ItemStack pan = player.getHeldItem(hand);
        RayTraceResult hit = rayTrace(world, player, true);
        if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            return new ActionResult<>(EnumActionResult.PASS, pan);
        }
        BlockPos water = hit.getBlockPos();
        if (!world.getBlockState(water).getMaterial().isLiquid()
                || world.getBiome(water) != ModBiomes.FROZEN_RIVER) {
            if (!world.isRemote)
                player.sendStatusMessage(
                        new TextComponentTranslation("status.exoarsenal.pan_river"), true);
            return new ActionResult<>(EnumActionResult.FAIL, pan);
        }
        player.getCooldownTracker().setCooldown(this, 28);
        player.swingArm(hand);
        if (!world.isRemote) {
            int roll = world.rand.nextInt(100);
            ItemStack found =
                    roll < 28
                            ? new ItemStack(ModContent.SMALL_FROZEN_COPPER)
                            : roll < 46
                                    ? new ItemStack(ModContent.SMALL_FROZEN_TIN)
                                    : roll < 57
                                            ? new ItemStack(Items.CLAY_BALL)
                                            : roll < 66
                                                    ? new ItemStack(ModContent.FLINT_SHARD)
                                                    : ItemStack.EMPTY;
            if (!found.isEmpty()) {
                ItemHandlerHelper.giveItemToPlayer(player, found);
                player.sendStatusMessage(
                        new TextComponentTranslation(
                                "status.exoarsenal.pan_found", found.getDisplayName()),
                        true);
            } else
                player.sendStatusMessage(
                        new TextComponentTranslation("status.exoarsenal.pan_empty"), true);
            if (!player.capabilities.isCreativeMode) pan.damageItem(1, player);
            world.playSound(
                    null,
                    water,
                    SoundEvents.ENTITY_GENERIC_SWIM,
                    SoundCategory.PLAYERS,
                    0.75F,
                    0.85F);
            if (world instanceof WorldServer)
                ((WorldServer) world)
                        .spawnParticle(
                                EnumParticleTypes.WATER_SPLASH,
                                water.getX() + 0.5D,
                                water.getY() + 1.0D,
                                water.getZ() + 0.5D,
                                18,
                                0.45D,
                                0.15D,
                                0.45D,
                                0.08D);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, pan);
    }
}
