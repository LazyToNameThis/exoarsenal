package com.exoarsenal.client.render;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.entity.EntityDesertVulture;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public final class DesertVultureRenderer extends GeoEntityRenderer<EntityDesertVulture> {
    public DesertVultureRenderer(RenderManager m) {
        super(m, new Model());
        shadowSize = .45F;
    }

    @Override
    public void renderRecursively(
            BufferBuilder b, GeoBone bone, float r, float g, float blue, float a) {
        String n = bone.getName();
        int c =
                n.equals("ruff")
                        ? 0xD6C6A5
                        : n.equals("neck") || n.equals("head")
                                ? 0xAB6E54
                                : n.equals("beak") || n.equals("talons")
                                        ? 0xC59E58
                                        : n.equals("eyes")
                                                ? 0x231C17
                                                : n.contains("Feathers") ? 0x3C322C : 0x67513C;
        super.renderRecursively(
                b, bone, ((c >> 16) & 255) / 255F, ((c >> 8) & 255) / 255F, (c & 255) / 255F, a);
    }

    private static final class Model extends AnimatedGeoModel<EntityDesertVulture> {
        public ResourceLocation getModelLocation(EntityDesertVulture e) {
            return new ResourceLocation(ExoArsenal.MODID, "geo/desert_vulture.geo.json");
        }

        public ResourceLocation getTextureLocation(EntityDesertVulture e) {
            return new ResourceLocation("textures/blocks/sandstone_normal.png");
        }

        public ResourceLocation getAnimationFileLocation(EntityDesertVulture e) {
            return new ResourceLocation(
                    ExoArsenal.MODID, "animations/desert_scourge.animation.json");
        }

        @Override
        public void setLivingAnimations(EntityDesertVulture e, Integer id, AnimationEvent event) {
            super.setLivingAnimations(e, id, event);
            float t = e.ticksExisted + event.getPartialTick(),
                    flap = (float) Math.sin(t * .25) * .5F;
            boolean dive = e.ticksExisted % 100 >= 55 && e.ticksExisted % 100 < 78;
            getBone("leftWing").setRotationZ(dive ? .22F : flap);
            getBone("rightWing").setRotationZ(dive ? -.22F : -flap);
            getBone("leftFeathers").setRotationY(dive ? -.65F : -.12F);
            getBone("rightFeathers").setRotationY(dive ? .65F : .12F);
            getBone("neck").setRotationX(dive ? -.25F : .1F);
            getBone("body").setRotationX(dive ? -.3F : 0);
        }
    }
}
