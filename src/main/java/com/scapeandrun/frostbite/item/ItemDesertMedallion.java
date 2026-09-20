package com.scapeandrun.frostbite.item;

import com.scapeandrun.frostbite.entity.EntityDesertScourge;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public final class ItemDesertMedallion extends Item {
    public ItemDesertMedallion() {
        setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(
            World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (world.isRemote) return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        if (world.getDifficulty() == net.minecraft.world.EnumDifficulty.PEACEFUL
                || !EntityDesertScourge.desert(world, player.getPosition())) {
            player.sendStatusMessage(
                    new TextComponentString(
                            "Use this in a warm desert outside Peaceful difficulty."),
                    true);
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }
        if (!world.getEntitiesWithinAABB(
                        EntityDesertScourge.class, player.getEntityBoundingBox().grow(192))
                .isEmpty()) {
            player.sendStatusMessage(
                    new TextComponentString("A Desert Scourge is already nearby."), true);
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }
        EntityDesertScourge boss = new EntityDesertScourge(world);
        boss.setPosition(player.posX, player.posY - 8, player.posZ);
        boss.setAttackTarget(player);
        if (!world.spawnEntity(boss)) return new ActionResult<>(EnumActionResult.FAIL, stack);
        player.getCooldownTracker().setCooldown(this, 100);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void addInformation(
            ItemStack stack,
            World world,
            java.util.List<String> lines,
            net.minecraft.client.util.ITooltipFlag flag) {
        lines.add("\u00a76Right click \u00a77in a desert to summon the Desert Scourge.");
        lines.add("\u00a78Not consumed. Expert worlds use a different moveset.");
    }
}
