package com.scapeandrun.frostbite.item;

import com.scapeandrun.frostbite.event.RmorEventHandler;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.item.GeoArmorItem;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class ItemRmorArmor extends GeoArmorItem implements IAnimatable {
    private final AnimationFactory factory = new AnimationFactory(this);
    private final int capacity;
    private final Map<AnimationController<?>, Integer> seenAnimations = new IdentityHashMap<>();

    public ItemRmorArmor(ArmorMaterial material, EntityEquipmentSlot slot, int capacity) {
        super(material, 0, slot);
        this.capacity = capacity;
        setMaxStackSize(1);
    }

    private <P extends IAnimatable> PlayState predicate(AnimationEvent<P> event) {
        EntityLivingBase wearer =
                event.getExtraDataOfType(EntityLivingBase.class).isEmpty()
                        ? null
                        : event.getExtraDataOfType(EntityLivingBase.class).get(0);
        if (wearer != null) {
            ItemStack main = wearer.getHeldItemMainhand();
            ItemStack off = wearer.getHeldItemOffhand();
            if (main.getItem() instanceof ItemRBlade && ItemRBlade.isSpinning(main)) {
                event.getController()
                        .setAnimation(
                                new AnimationBuilder()
                                        .addAnimation("animation.rmor.rblade_spin_right", true));
                return PlayState.CONTINUE;
            }
            if (off.getItem() instanceof ItemRBlade && ItemRBlade.isSpinning(off)) {
                event.getController()
                        .setAnimation(
                                new AnimationBuilder()
                                        .addAnimation("animation.rmor.rblade_spin_left", true));
                return PlayState.CONTINUE;
            }
        }
        String action = RmorEventHandler.getAnimation(wearer);
        if (!action.isEmpty()
                && wearer != null
                && wearer.world.getTotalWorldTime() - RmorEventHandler.getAnimationTick(wearer)
                        <= RmorEventHandler.getAnimationDuration(action)) {
            int serial = RmorEventHandler.getAnimationSerial(wearer);
            Integer seen = seenAnimations.get(event.getController());
            if (seen == null || seen != serial) {
                event.getController().markNeedsReload();
                seenAnimations.put(event.getController(), serial);
            }
            event.getController()
                    .setAnimation(
                            new AnimationBuilder().addAnimation("animation.rmor." + action, false));
            return PlayState.CONTINUE;
        }
        if (RmorEventHandler.isDefenseForm(wearer)) {
            event.getController()
                    .setAnimation(
                            new AnimationBuilder()
                                    .addAnimation("animation.rmor.defense_idle", true));
            return PlayState.CONTINUE;
        }
        String mode = RmorEventHandler.getModeName(wearer);
        event.getController()
                .setAnimation(
                        new AnimationBuilder()
                                .addAnimation(
                                        RmorEventHandler.isBladeDeployed(wearer)
                                                ? "animation.rmor." + mode
                                                : "animation.rmor.stowed",
                                        true));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(
                new AnimationController<ItemRmorArmor>(this, "rmor", 4, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return EnergyUtil.provider(capacity, 12000);
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
