package com.scapeandrun.frostbite.client.render;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.item.ItemScoutTreasureBag;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public final class ScoutTreasureBagRenderer extends GeoItemRenderer<ItemScoutTreasureBag> {
    public ScoutTreasureBagRenderer() {
        super(new BagModel());
    }

    private static final class BagModel extends AnimatedGeoModel<ItemScoutTreasureBag> {
        public ResourceLocation getModelLocation(ItemScoutTreasureBag i) {
            return new ResourceLocation(Frostbite.MODID, "geo/scout_treasure_bag.geo.json");
        }

        public ResourceLocation getTextureLocation(ItemScoutTreasureBag i) {
            return new ResourceLocation(Frostbite.MODID, "textures/entity/x20_scout_v2.png");
        }

        public ResourceLocation getAnimationFileLocation(ItemScoutTreasureBag i) {
            return new ResourceLocation(Frostbite.MODID, "animations/frigid_robots.animation.json");
        }
    }
}
