package com.scapeandrun.frostbite.client.model;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.item.ItemScoutWeapon;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public final class ScoutWeaponModel extends AnimatedGeoModel<ItemScoutWeapon> {
    private static final String[] GROUPS = {"pincer", "railgun", "smasher", "aurora"};
    private static final String[] ENERGY = {
        "pincerEnergy", "railcore", "smasherEnergy", "auroraEnergy"
    };

    @Override
    public ResourceLocation getModelLocation(ItemScoutWeapon object) {
        return new ResourceLocation(Frostbite.MODID, "geo/scout_weapons_refined.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(ItemScoutWeapon object) {
        return new ResourceLocation(Frostbite.MODID, "textures/models/armor/kx20_refined_100.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(ItemScoutWeapon object) {
        return new ResourceLocation(Frostbite.MODID, "animations/scout_weapons.animation.json");
    }

    @Override
    public void setLivingAnimations(ItemScoutWeapon item, Integer id, AnimationEvent event) {
        super.setLivingAnimations(item, id, event);
        for (int i = 0; i < GROUPS.length; i++) {
            IBone bone = getBone(GROUPS[i]);
            if (bone != null) bone.setHidden(i != item.getType().ordinal());
            IBone energy = getBone(ENERGY[i]);
            if (energy != null) energy.setHidden(i != item.getType().ordinal());
        }
    }
}
