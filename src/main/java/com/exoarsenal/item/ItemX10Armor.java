package com.exoarsenal.item;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.EntityLivingBase;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class ItemX10Armor extends ItemRmorArmor {
    public ItemX10Armor(ArmorMaterial material, EntityEquipmentSlot slot, int capacity) {
        super(material, slot, capacity);
    }

    private <P extends IAnimatable> PlayState x10Animation(AnimationEvent<P> event) {
        EntityLivingBase wearer =
                event.getExtraDataOfType(EntityLivingBase.class).isEmpty()
                        ? null
                        : event.getExtraDataOfType(EntityLivingBase.class).get(0);
        boolean flying = wearer != null && !wearer.onGround && wearer.motionY > -0.45D;
        event.getController()
                .setAnimation(
                        new AnimationBuilder()
                                .addAnimation(
                                        flying
                                                ? "animation.rmor.x10_wings_flight"
                                                : "animation.rmor.x10_wings_idle",
                                        true));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        super.registerControllers(data);
        data.addAnimationController(
                new AnimationController<ItemX10Armor>(this, "x10_wings", 4, this::x10Animation));
    }
}
