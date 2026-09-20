package com.exoarsenal.event;

import com.exoarsenal.item.EnergyUtil;
import com.exoarsenal.item.ItemEnergyShield;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class EnergyShieldHandler {
    @SubscribeEvent
    public void block(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer) || event.getSource().isUnblockable())
            return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if (!player.isHandActive()) return;
        ItemStack stack = player.getActiveItemStack();
        if (!(stack.getItem() instanceof ItemEnergyShield)) return;

        Entity source = event.getSource().getImmediateSource();
        if (source == null) source = event.getSource().getTrueSource();
        if (source == null) return;
        Vec3d incoming =
                source.getPositionVector()
                        .addVector(0.0D, source.height * 0.5D, 0.0D)
                        .subtract(player.getPositionEyes(1.0F));
        if (incoming.lengthSquared() > 0.001D
                && player.getLookVec().normalize().dotProduct(incoming.normalize()) < 0.10D) return;

        ItemEnergyShield shield = (ItemEnergyShield) stack.getItem();
        int cost = shield.blockCost(event.getAmount());
        if (!EnergyUtil.drain(stack, cost, false)) {
            player.stopActiveHand();
            return;
        }

        event.setAmount(Math.max(0.0F, event.getAmount() * (1.0F - shield.reduction())));
        ItemEnergyShield.impact(stack, player.world.getTotalWorldTime());
        source.addVelocity(-incoming.x * 0.018D, 0.08D, -incoming.z * 0.018D);
        player.world.playSound(
                null,
                player.posX,
                player.posY + 1.0D,
                player.posZ,
                net.minecraft.init.SoundEvents.BLOCK_ANVIL_LAND,
                net.minecraft.util.SoundCategory.PLAYERS,
                0.28F,
                shield.isX10() ? 1.9F : 1.55F);
    }
}
