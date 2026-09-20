package com.scapeandrun.frostbite.item;

import net.minecraft.item.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public final class ItemScoutEnergyCore extends Item
        implements software.bernie.geckolib3.core.IAnimatable {
    private final software.bernie.geckolib3.core.manager.AnimationFactory factory =
            new software.bernie.geckolib3.core.manager.AnimationFactory(this);

    @Override
    public software.bernie.geckolib3.core.manager.AnimationFactory getFactory() {
        return factory;
    }

    @Override
    public void registerControllers(software.bernie.geckolib3.core.manager.AnimationData data) {}

    public ItemScoutEnergyCore() {
        setMaxStackSize(1);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound tag) {
        return EnergyUtil.provider(500000, 16000);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(
            World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (world.isRemote) return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        for (com.scapeandrun.frostbite.entity.EntityScoutCoreRay ray :
                world.getEntitiesWithinAABB(
                        com.scapeandrun.frostbite.entity.EntityScoutCoreRay.class,
                        player.getEntityBoundingBox().grow(8)))
            if (ray.belongs(player)) {
                ray.setDead();
                return new ActionResult<>(EnumActionResult.SUCCESS, stack);
            }
        if (!EnergyUtil.drain(stack, 600, false))
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        world.spawnEntity(new com.scapeandrun.frostbite.entity.EntityScoutCoreRay(player, hand));
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return EnergyUtil.stored(stack) < 500000;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1 - EnergyUtil.stored(stack) / 500000D;
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return 0x63DFFF;
    }

    @Override
    public void addInformation(
            ItemStack stack,
            World world,
            java.util.List<String> lines,
            net.minecraft.client.util.ITooltipFlag flag) {
        lines.add("\u00a7bRight click \u00a77to summon six focusing orbs.");
        lines.add("\u00a77They converge into a piercing frost deathray.");
        lines.add("\u00a77Aim while charging. Right click again to cancel.");
        lines.add("\u00a73" + EnergyUtil.stored(stack) + " / 500,000 RF");
    }
}
