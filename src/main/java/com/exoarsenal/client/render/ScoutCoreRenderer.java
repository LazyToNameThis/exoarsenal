package com.exoarsenal.client.render;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.item.ItemScoutEnergyCore;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public final class ScoutCoreRenderer extends GeoItemRenderer<ItemScoutEnergyCore> {
    public ScoutCoreRenderer() {
        super(new CoreModel());
    }

    private static final class CoreModel extends AnimatedGeoModel<ItemScoutEnergyCore> {
        public ResourceLocation getModelLocation(ItemScoutEnergyCore i) {
            return new ResourceLocation(ExoArsenal.MODID, "geo/scout_energy_core.geo.json");
        }

        public ResourceLocation getTextureLocation(ItemScoutEnergyCore i) {
            return new ResourceLocation(ExoArsenal.MODID, "textures/entity/x20_scout_v2.png");
        }

        public ResourceLocation getAnimationFileLocation(ItemScoutEnergyCore i) {
            return new ResourceLocation(
                    ExoArsenal.MODID, "animations/frigid_robots.animation.json");
        }

        @Override
        public void setLivingAnimations(
                ItemScoutEnergyCore item, Integer id, AnimationEvent event) {
            super.setLivingAnimations(item, id, event);
            Minecraft mc = Minecraft.getMinecraft();
            float
                    age =
                            mc.world == null
                                    ? 0
                                    : mc.world.getTotalWorldTime() + event.getPartialTick(),
                    charge = 0;
            if (mc.player != null)
                for (com.exoarsenal.entity.EntityScoutCoreRay ray :
                        mc.world.getEntitiesWithinAABB(
                                com.exoarsenal.entity.EntityScoutCoreRay.class,
                                mc.player.getEntityBoundingBox().grow(4)))
                    if (ray.belongs(mc.player))
                        charge =
                                com.exoarsenal.entity.ScoutCoreMotion.charge(
                                        ray.age() + event.getPartialTick());
            getBone("outerRing").setRotationZ(age * .012F);
            getBone("innerCoil").setRotationZ(-age * (.025F + charge * .08F));
            getBone("heartIce").setRotationY(age * .016F);
            float pulse = 1 + (float) Math.sin(age * .12F) * .04F + charge * .15F;
            getBone("heartIce").setScaleX(pulse);
            getBone("heartIce").setScaleY(pulse);
            getBone("heartIce").setScaleZ(pulse);
            getBone("leftJaw").setPositionX(charge * 2);
            getBone("rightJaw").setPositionX(-charge * 2);
            getBone("topJaw").setPositionY(charge * 2);
            getBone("bottomJaw").setPositionY(-charge * 2);
        }
    }
}
