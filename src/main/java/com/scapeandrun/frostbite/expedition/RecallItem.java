package com.scapeandrun.frostbite.expedition;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.*;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.*;
import java.util.List;

public final class RecallItem extends Item {
    private final boolean consumable;

    public RecallItem(boolean consumable) {
        this.consumable = consumable;
        setMaxStackSize(consumable ? 16 : 1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return consumable ? 4 : 15;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return consumable ? EnumAction.DRINK : EnumAction.BOW;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(
            World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (world.provider.getDimension() != 0
                || player.isRiding()
                || player.isSpectator()
                || player.getCooldownTracker().hasCooldown(this))
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase entity, int count) {
        if (entity.hurtTime > 0) {
            entity.stopActiveHand();
            return;
        }
        if (!entity.world.isRemote && count % 4 == 0)
            ((WorldServer) entity.world)
                    .spawnParticle(
                            EnumParticleTypes.SPELL_INSTANT,
                            entity.posX,
                            entity.posY + 1,
                            entity.posZ,
                            3,
                            .3,
                            .5,
                            .3,
                            .015);
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entity) {
        if (world.isRemote
                || !(entity instanceof EntityPlayerMP)
                || world.provider.getDimension() != 0
                || !entity.isEntityAlive()
                || entity.isRiding()
                || entity.hurtTime > 0) return stack;
        EntityPlayerMP player = (EntityPlayerMP) entity;
        BlockPos bed = player.getBedLocation(0), target = null;
        if (bed != null) {
            BlockPos spawn = EntityPlayer.getBedSpawnLocation(world, bed, player.isSpawnForced(0));
            if (spawn != null && safe(world, player, spawn)) target = spawn;
        }
        if (target == null) {
            BlockPos origin = world.getTopSolidOrLiquidBlock(world.getSpawnPoint());
            for (int radius = 0; radius <= 4 && target == null; radius++)
                for (int dx = -radius; dx <= radius && target == null; dx++)
                    for (int dz = -radius; dz <= radius && target == null; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                        for (int dy = 0; dy <= 8; dy++) {
                            BlockPos at = origin.add(dx, dy, dz);
                            if (safe(world, player, at)) {
                                target = at;
                                break;
                            }
                        }
                    }
        }
        if (target == null) {
            player.sendStatusMessage(
                    new TextComponentString(
                            "No safe place to recall. Clear your bed or world spawn."),
                    true);
            return stack;
        }
        net.minecraftforge.event.entity.living.EnderTeleportEvent event =
                new net.minecraftforge.event.entity.living.EnderTeleportEvent(
                        player, target.getX() + .5, target.getY(), target.getZ() + .5, 0);
        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event)) return stack;
        if (!Double.isFinite(event.getTargetX())
                || !Double.isFinite(event.getTargetY())
                || !Double.isFinite(event.getTargetZ())) return stack;

        BlockPos redirected =
                new BlockPos(event.getTargetX(), event.getTargetY(), event.getTargetZ());
        if (!safe(world, player, redirected)) return stack;
        player.dismountRidingEntity();
        player.setPositionAndUpdate(
                redirected.getX() + .5, redirected.getY(), redirected.getZ() + .5);
        player.motionX = player.motionY = player.motionZ = 0;
        player.fallDistance = 0;
        player.connection.sendPacket(
                new net.minecraft.network.play.server.SPacketEntityVelocity(player));
        player.getCooldownTracker().setCooldown(this, 40);
        ((WorldServer) world)
                .spawnParticle(
                        EnumParticleTypes.SPELL_INSTANT,
                        player.posX,
                        player.posY + 1,
                        player.posZ,
                        24,
                        .4,
                        .7,
                        .4,
                        .03);
        world.playSound(
                null,
                player.posX,
                player.posY,
                player.posZ,
                net.minecraft.init.SoundEvents.ENTITY_ENDERMEN_TELEPORT,
                SoundCategory.PLAYERS,
                .6F,
                1.4F);
        if (consumable && !player.isCreative()) stack.shrink(1);
        return stack;
    }

    private static boolean safe(World world, EntityPlayer player, BlockPos at) {
        if (at.getY() < 1 || at.getY() > 253 || !world.getWorldBorder().contains(at)) return false;
        Block floor = world.getBlockState(at.down()).getBlock();
        if (floor == Blocks.MAGMA
                || floor == Blocks.CACTUS
                || !world.getBlockState(at.down()).isSideSolid(world, at.down(), EnumFacing.UP))
            return false;
        AxisAlignedBB box =
                player.getEntityBoundingBox()
                        .offset(
                                at.getX() + .5 - player.posX,
                                at.getY() - player.posY,
                                at.getZ() + .5 - player.posZ);
        return world.getCollisionBoxes(player, box).isEmpty()
                && !world.containsAnyLiquid(box)
                && world.getBlockState(at).getBlock() != Blocks.FIRE
                && world.getBlockState(at.up()).getBlock() != Blocks.FIRE;
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> text, ITooltipFlag flag) {
        text.add("Hold use to return to your bed, or world spawn.");
        text.add(
                consumable
                        ? "Channels for 0.2 seconds. Consumed on successful recall."
                        : "Channels for 0.75 seconds. Unlimited uses.");
        text.add("Overworld only. Taking damage interrupts recall.");
    }
}
