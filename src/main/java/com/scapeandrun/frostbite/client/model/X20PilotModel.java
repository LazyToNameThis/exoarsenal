package com.scapeandrun.frostbite.client.model;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.entity.EntityX20Pilot;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public final class X20PilotModel extends AnimatedGeoModel<EntityX20Pilot> {
    @Override
    public ResourceLocation getModelLocation(EntityX20Pilot object) {
        return new ResourceLocation(Frostbite.MODID, "geo/x20_pilot_refined.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(EntityX20Pilot object) {
        return new ResourceLocation(Frostbite.MODID, "textures/models/armor/kx20_refined_100.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(EntityX20Pilot object) {
        return new ResourceLocation(Frostbite.MODID, "animations/x20_pilot.animation.json");
    }
}
