package com.exoarsenal.client.model;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.item.ItemRmorArmor;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;

public class KXArmorModel extends X10ArmorModel {
    @Override
    public ResourceLocation getModelLocation(ItemRmorArmor object) {
        return new ResourceLocation(ExoArsenal.MODID, "geo/x10_armor_refined.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(ItemRmorArmor object) {
        return new ResourceLocation(
                ExoArsenal.MODID, "textures/models/armor/kx20_refined_" + chargeName() + ".png");
    }

    @Override
    public void setLivingAnimations(ItemRmorArmor armor, Integer id, AnimationEvent event) {
        super.setLivingAnimations(armor, id, event);
        setKxBones(true);
    }
}
