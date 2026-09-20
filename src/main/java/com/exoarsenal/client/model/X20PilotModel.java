package com.exoarsenal.client.model;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.entity.EntityX20Pilot;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public final class X20PilotModel extends AnimatedGeoModel<EntityX20Pilot> {
    @Override
    public ResourceLocation getModelLocation(EntityX20Pilot object) {
        return new ResourceLocation(ExoArsenal.MODID, "geo/x20_pilot_refined.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(EntityX20Pilot object) {
        return new ResourceLocation(ExoArsenal.MODID, "textures/models/armor/kx20_refined_100.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(EntityX20Pilot object) {
        return new ResourceLocation(ExoArsenal.MODID, "animations/x20_pilot.animation.json");
    }
}
