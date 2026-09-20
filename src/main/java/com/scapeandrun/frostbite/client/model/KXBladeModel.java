package com.scapeandrun.frostbite.client.model;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.item.ItemRBlade;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;

public class KXBladeModel extends X10BladeModel {
    @Override
    public ResourceLocation getTextureLocation(ItemRBlade object) {
        return new ResourceLocation(Frostbite.MODID, "textures/models/armor/kx20_refined_100.png");
    }

    @Override
    public void setLivingAnimations(ItemRBlade blade, Integer id, AnimationEvent event) {
        super.setLivingAnimations(blade, id, event);
        setKxUpgrades(true);
    }
}
