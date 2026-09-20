package com.exoarsenal.expedition;

import net.minecraft.item.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.world.World;

public final class CaveJavelin extends Item {
    @Override
    public ActionResult<ItemStack> onItemRightClick(
            World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.getCooldownTracker().hasCooldown(this))
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        if (!world.isRemote) {
            EntityPrebossShot shot = new EntityPrebossShot(world, player, PrebossItem.Kind.JAVELIN);
            shot.shoot(player, player.rotationPitch, player.rotationYaw, 0, 1.3F, .5F);
            world.spawnEntity(shot);
            if (!player.isCreative()) stack.shrink(1);
            player.getCooldownTracker().setCooldown(this, 18);
        }
        player.swingArm(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }
}
