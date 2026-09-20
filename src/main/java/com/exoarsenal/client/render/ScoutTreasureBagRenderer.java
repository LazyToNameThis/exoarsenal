package com.exoarsenal.client.render;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.item.ItemScoutTreasureBag;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public final class ScoutTreasureBagRenderer extends GeoItemRenderer<ItemScoutTreasureBag> {
    public ScoutTreasureBagRenderer() {
        super(new BagModel());
    }

    private static final class BagModel extends AnimatedGeoModel<ItemScoutTreasureBag> {
        public ResourceLocation getModelLocation(ItemScoutTreasureBag i) {
            return new ResourceLocation(ExoArsenal.MODID, "geo/scout_treasure_bag.geo.json");
        }

        public ResourceLocation getTextureLocation(ItemScoutTreasureBag i) {
            return new ResourceLocation(ExoArsenal.MODID, "textures/entity/x20_scout_v2.png");
        }

        public ResourceLocation getAnimationFileLocation(ItemScoutTreasureBag i) {
            return new ResourceLocation(
                    ExoArsenal.MODID, "animations/frigid_robots.animation.json");
        }
    }
}
