package com.exoarsenal.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class ItemEnergyShield extends Item implements IAnimatable {
    private final boolean x10;
    private final int capacity;
    private final AnimationFactory factory = new AnimationFactory(this);
    private final Map<AnimationController<?>, Integer> seenImpacts = new IdentityHashMap<>();

    public ItemEnergyShield(boolean x10) {
        this.x10 = x10;
        this.capacity = x10 ? 1000000 : 300000;
        setMaxStackSize(1);
    }

    public boolean isX10() {
        return x10;
    }

    public float reduction() {
        return x10 ? 0.90F : 0.70F;
    }

    private static NBTTagCompound tag(ItemStack stack) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        return stack.getTagCompound();
    }

    public int blockCost(float incomingDamage) {
        return (x10 ? 520 : 300) + (int) Math.ceil(incomingDamage * (x10 ? 75.0D : 55.0D));
    }

    public static void impact(ItemStack stack, long tick) {
        NBTTagCompound nbt = tag(stack);
        nbt.setLong("ShieldImpactTick", tick);
        nbt.setInteger("ShieldImpactSerial", nbt.getInteger("ShieldImpactSerial") + 1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(
            World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (EnergyUtil.stored(stack) < blockCost(1.0F))
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        tag(stack).setBoolean("ShieldActive", true);
        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase living, int count) {
        tag(stack).setBoolean("ShieldActive", true);
        if (!living.world.isRemote
                && count % 20 == 0
                && !EnergyUtil.drain(stack, x10 ? 18 : 12, false)) {
            living.stopActiveHand();
        }
    }

    @Override
    public void onPlayerStoppedUsing(
            ItemStack stack, World world, EntityLivingBase living, int timeLeft) {
        tag(stack).setBoolean("ShieldActive", false);
    }

    private <P extends IAnimatable> PlayState predicate(AnimationEvent<P> event) {
        ItemStack stack =
                event.getExtraDataOfType(ItemStack.class).isEmpty()
                        ? ItemStack.EMPTY
                        : event.getExtraDataOfType(ItemStack.class).get(0);
        NBTTagCompound nbt = stack.hasTagCompound() ? stack.getTagCompound() : null;
        if (nbt != null
                && Minecraft.getMinecraft().world != null
                && Minecraft.getMinecraft().world.getTotalWorldTime()
                                - nbt.getLong("ShieldImpactTick")
                        <= 6L) {
            int serial = nbt.getInteger("ShieldImpactSerial");
            Integer seen = seenImpacts.get(event.getController());
            if (seen == null || seen != serial) {
                event.getController().markNeedsReload();
                seenImpacts.put(event.getController(), serial);
            }
            event.getController()
                    .setAnimation(
                            new AnimationBuilder()
                                    .addAnimation("animation.energy_shield.impact", false));
            return PlayState.CONTINUE;
        }
        boolean active = nbt != null && nbt.getBoolean("ShieldActive");
        event.getController()
                .setAnimation(
                        new AnimationBuilder()
                                .addAnimation(
                                        active
                                                ? "animation.energy_shield.active"
                                                : "animation.energy_shield.inactive",
                                        true));
        return PlayState.CONTINUE;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BLOCK;
    }

    @Override
    public boolean isFull3D() {
        return true;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(
                new AnimationController<ItemEnergyShield>(
                        this, "energy_shield", 2, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return EnergyUtil.provider(capacity, x10 ? 32000 : 12000);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return EnergyUtil.stored(stack) < capacity;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1.0D - EnergyUtil.stored(stack) / (double) capacity;
    }

    @Override
    public void addInformation(
            ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        EquipmentTooltip.appendLegacy(stack, tooltip);
    }
}
