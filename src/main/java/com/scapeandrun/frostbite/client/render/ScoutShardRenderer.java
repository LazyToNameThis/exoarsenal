package com.scapeandrun.frostbite.client.render;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.entity.EntityScoutShard;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.core.util.Color;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public final class ScoutShardRenderer extends GeoEntityRenderer<EntityScoutShard> {
    public ScoutShardRenderer(RenderManager manager) {
        super(manager, new ShardModel());
        shadowSize = 0;
    }

    @Override
    public boolean shouldRender(
            EntityScoutShard shard,
            net.minecraft.client.renderer.culling.ICamera camera,
            double x,
            double y,
            double z) {
        return super.shouldRender(shard, camera, x, y, z)
                || shard.kind() >= 8
                        && camera.isBoundingBoxInFrustum(
                                shard.getEntityBoundingBox().grow(shard.kind() == 10 ? 12 : 3));
    }

    @Override
    public void doRender(
            EntityScoutShard shard, double x, double y, double z, float yaw, float partial) {
        if (shard.kind() == 10) {
            renderWave(shard, x, y, z, partial);
            return;
        }
        if (shard.kind() == 8 || shard.kind() == 9) {
            net.minecraft.client.renderer.GlStateManager.pushMatrix();
            net.minecraft.client.renderer.GlStateManager.translate(x, y, z);
            net.minecraft.client.renderer.GlStateManager.scale(
                    shard.kind() == 9 ? 1.4 : 1.2, 1, shard.kind() == 9 ? 1.4 : 1.2);
            if (shard.kind() == 8)
                net.minecraft.client.renderer.GlStateManager.rotate(
                        (shard.age() + partial) * 2, 0, 1, 0);
            bindTexture(net.minecraft.client.renderer.texture.TextureMap.LOCATION_BLOCKS_TEXTURE);
            net.minecraft.client.renderer.GlStateManager.enableTexture2D();
            net.minecraft.client.renderer.GlStateManager.color(1, 1, 1, 1);
            net.minecraft.client.renderer.BlockRendererDispatcher blocks =
                    net.minecraft.client.Minecraft.getMinecraft().getBlockRendererDispatcher();
            for (int dx = -1; dx <= 1; dx++)
                for (int dz = -1; dz <= 1; dz++) {
                    net.minecraft.client.renderer.GlStateManager.pushMatrix();
                    net.minecraft.client.renderer.GlStateManager.translate(dx - .5, 0, dz - .5);
                    blocks.renderBlockBrightness(
                            net.minecraft.init.Blocks.PACKED_ICE.getDefaultState(), 1);
                    net.minecraft.client.renderer.GlStateManager.popMatrix();
                }
            net.minecraft.client.renderer.GlStateManager.popMatrix();
            return;
        }
        super.doRender(shard, x, y, z, yaw, partial);
    }

    private void renderWave(EntityScoutShard shard, double x, double y, double z, float partial) {
        float age = shard.age() + partial;
        if (age < 0 || age > 26) return;
        double outer = age * .42, inner = Math.max(0, outer - .65);
        float alpha = Math.max(0, 1 - age / 26);
        net.minecraft.client.renderer.GlStateManager.pushMatrix();
        net.minecraft.client.renderer.GlStateManager.translate(x, y + .05, z);
        net.minecraft.client.renderer.GlStateManager.disableTexture2D();
        net.minecraft.client.renderer.GlStateManager.disableLighting();
        net.minecraft.client.renderer.GlStateManager.disableCull();
        net.minecraft.client.renderer.GlStateManager.enableBlend();
        net.minecraft.client.renderer.GlStateManager.blendFunc(
                org.lwjgl.opengl.GL11.GL_SRC_ALPHA, org.lwjgl.opengl.GL11.GL_ONE);
        net.minecraft.client.renderer.GlStateManager.depthMask(false);
        net.minecraft.client.renderer.BufferBuilder b =
                net.minecraft.client.renderer.Tessellator.getInstance().getBuffer();
        b.begin(
                org.lwjgl.opengl.GL11.GL_QUADS,
                net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < 36; i++) {
            double a = i * Math.PI / 18, c = (i + 1) * Math.PI / 18;
            b.pos(Math.cos(a) * inner, 0, Math.sin(a) * inner)
                    .color(.25F, .7F, 1F, alpha * .4F)
                    .endVertex();
            b.pos(Math.cos(a) * outer, .15, Math.sin(a) * outer)
                    .color(.8F, .95F, 1F, alpha)
                    .endVertex();
            b.pos(Math.cos(c) * outer, .15, Math.sin(c) * outer)
                    .color(.8F, .95F, 1F, alpha)
                    .endVertex();
            b.pos(Math.cos(c) * inner, 0, Math.sin(c) * inner)
                    .color(.25F, .7F, 1F, alpha * .4F)
                    .endVertex();
        }
        net.minecraft.client.renderer.Tessellator.getInstance().draw();
        net.minecraft.client.renderer.GlStateManager.depthMask(true);
        net.minecraft.client.renderer.GlStateManager.disableBlend();
        net.minecraft.client.renderer.GlStateManager.blendFunc(
                org.lwjgl.opengl.GL11.GL_SRC_ALPHA, org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA);
        net.minecraft.client.renderer.GlStateManager.enableCull();
        net.minecraft.client.renderer.GlStateManager.enableTexture2D();
        net.minecraft.client.renderer.GlStateManager.enableLighting();
        net.minecraft.client.renderer.GlStateManager.popMatrix();
    }

    @Override
    public Color getRenderColor(EntityScoutShard shard, float partial) {
        if (shard.kind() == 1) return Color.ofRGB(255, 65, 85);
        float glow =
                shard.kind() == 0
                        ? Math.max(0, Math.min(1, (shard.age() + partial - 42) / 12F))
                        : 0;
        return Color.ofRGB(180 + (int) (glow * 75), 225 + (int) (glow * 30), 255);
    }

    private static final class ShardModel extends AnimatedGeoModel<EntityScoutShard> {
        @Override
        public ResourceLocation getModelLocation(EntityScoutShard e) {
            return new ResourceLocation(Frostbite.MODID, "geo/scout_shard.geo.json");
        }

        @Override
        public ResourceLocation getTextureLocation(EntityScoutShard e) {
            return com.scapeandrun.frostbite.client.FrigidTexture.location();
        }

        @Override
        public ResourceLocation getAnimationFileLocation(EntityScoutShard e) {
            return new ResourceLocation(Frostbite.MODID, "animations/scout_shard.animation.json");
        }

        @Override
        public void setLivingAnimations(EntityScoutShard e, Integer id, AnimationEvent event) {
            super.setLivingAnimations(e, id, event);
            IBone root = getBone("root");
            getBone("shard").setHidden(e.kind() == 5 || e.kind() == 6);
            getBone("scissors").setHidden(e.kind() != 5);
            getBone("ridingWedge").setHidden(e.kind() != 6);
            if (e.kind() == 5) {
                float step = (e.age() + event.getPartialTick() - 1) % 24;
                float spread =
                        step < 9
                                ? .65F
                                : step < 17 ? .65F * (1 - Math.min(1, (step - 9) / 6)) : .08F;
                getBone("scissorLeft").setRotationY(spread);
                getBone("scissorRight").setRotationY(-spread);
                root.setScaleX(1.5F);
                root.setScaleY(1.5F);
                root.setScaleZ(1.5F);
                root.setRotationY(0);
                root.setRotationZ(0);
                return;
            }
            float age = e.age() + event.getPartialTick(),
                    scale =
                            e.kind() == 7
                                    ? 2.6F
                                    : e.kind() == 6
                                            ? 3.2F
                                            : e.kind() == 0 || e.kind() == 4
                                                    ? 1.1F
                                                    : e.kind() == 2 ? 1.6F : .45F;
            if (e.kind() == 0 && age >= 42) scale *= 1 + (age - 42) * .025F;
            root.setScaleX(e.kind() == 6 ? scale * 1.5F : scale);
            root.setScaleZ(e.kind() == 6 ? scale * 1.5F : scale);
            root.setScaleY(e.kind() == 2 ? Math.max(.04F, Math.min(1, (age - 10) / 6)) * 3 : scale);
            root.setRotationY(e.kind() == 2 || e.kind() == 6 ? 0 : age * .1F);
            root.setRotationZ(e.kind() == 2 || e.kind() == 6 ? 0 : age * .06F);
        }
    }
}
