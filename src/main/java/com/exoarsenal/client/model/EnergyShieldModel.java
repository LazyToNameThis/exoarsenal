package com.exoarsenal.client.model;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.item.ItemEnergyShield;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class EnergyShieldModel extends AnimatedGeoModel<ItemEnergyShield> {
    @Override
    public ResourceLocation getModelLocation(ItemEnergyShield object) {
        return new ResourceLocation(ExoArsenal.MODID, "geo/energy_shields_refined.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(ItemEnergyShield object) {
        return new ResourceLocation(
                ExoArsenal.MODID,
                object.isX10()
                        ? "textures/models/armor/x10_refined_100.png"
                        : "textures/models/armor/rmor_refined_100.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(ItemEnergyShield object) {
        return new ResourceLocation(ExoArsenal.MODID, "animations/energy_shield.animation.json");
    }

    @Override
    public void setLivingAnimations(ItemEnergyShield shield, Integer id, AnimationEvent event) {
        super.setLivingAnimations(shield, id, event);
        IBone prototype = getBone("prototypeShield");
        IBone prototypeField = getBone("prototypeField");
        IBone x10 = getBone("x10Shield");
        IBone x10Field = getBone("x10Field");
        if (prototype != null) prototype.setHidden(shield.isX10());
        if (prototypeField != null) prototypeField.setHidden(shield.isX10());
        if (x10 != null) x10.setHidden(!shield.isX10());
        if (x10Field != null) x10Field.setHidden(!shield.isX10());
    }
}
