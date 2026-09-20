package com.exoarsenal.client.render;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.entity.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib3.geo.render.built.*;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.GeoModelProvider;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public final class DesertScourgeRenderer extends Render<EntityDesertScourge>
        implements IGeoRenderer<EntityDesertScourge> {
    private final Model model = new Model();
    private String section = "head";

    public DesertScourgeRenderer(RenderManager manager) {
        super(manager);
        shadowSize = 0;
    }

    @Override
    public GeoModelProvider getGeoModelProvider() {
        return model;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityDesertScourge e) {
        return com.exoarsenal.expedition.client.PixelTextures.location(1);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityDesertScourge e) {
        return getTextureLocation(e);
    }

    @Override
    public void doRender(
            EntityDesertScourge e, double x, double y, double z, float yaw, float partial) {
        if (e.hidden()) return;
        GeoModel mesh = model.getModel(model.getModelLocation(e));
        float jaw =
                e.attack() == DesertScourgePattern.LUNGE
                                || e.attack() == DesertScourgePattern.SAND_RUSH
                        ? .48F
                        : .12F + (float) Math.sin((e.ticksExisted + partial) * .07) * .035F;
        model.getBone("headUpperJaw").setRotationX(-jaw * .55F);
        model.getBone("headLowerJaw").setRotationX(jaw);
        Vec3d head =
                new Vec3d(
                        e.lastTickPosX + (e.posX - e.lastTickPosX) * partial,
                        e.lastTickPosY + (e.posY - e.lastTickPosY) * partial,
                        e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * partial);
        bindTexture(getTextureLocation(e));
        GlStateManager.enableRescaleNormal();
        for (int i = e.count() - 1; i >= 0; i--) {
            Vec3d p = e.visibleSegment(i, partial),
                    forward =
                            i == 0
                                    ? p.subtract(e.visibleSegment(1, partial))
                                    : e.visibleSegment(i - 1, partial).subtract(p);
            int light = e.world.getCombinedLight(new net.minecraft.util.math.BlockPos(p), 0);
            OpenGlHelper.setLightmapTextureCoords(
                    OpenGlHelper.lightmapTexUnit, light & 65535, light >> 16);
            section = i == 0 ? "head" : i == e.count() - 1 ? "tail" : "body";
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + p.x - head.x, y + p.y - head.y, z + p.z - head.z);
            GlStateManager.rotate(
                    (float) Math.toDegrees(Math.atan2(forward.x, forward.z)), 0, 1, 0);
            GlStateManager.rotate(
                    (float)
                            -Math.toDegrees(
                                    Math.atan2(
                                            forward.y,
                                            Math.sqrt(
                                                    forward.x * forward.x
                                                            + forward.z * forward.z))),
                    1,
                    0,
                    0);
            double scale = e.sizeScale() * (i == 0 ? 1.3 : 1 + .08 * Math.sin(i * .7));
            GlStateManager.scale(scale, scale, scale);
            render(mesh, e, partial, 1, 1, 1, 1);
            GlStateManager.popMatrix();
        }
        GlStateManager.disableRescaleNormal();
    }

    @Override
    public void renderRecursively(
            BufferBuilder b, GeoBone bone, float r, float g, float blue, float alpha) {
        String n = bone.getName();
        if ((n.equals("head") || n.equals("body") || n.equals("tail")) && !n.equals(section))
            return;
        int c = com.exoarsenal.client.ScourgeColors.color(n);
        java.util.List<com.exoarsenal.client.DesertScourgeMesh.Quad> mesh =
                com.exoarsenal.client.DesertScourgeMesh.part(n);
        if (mesh != null) {
            IGeoRenderer.MATRIX_STACK.push();
            IGeoRenderer.MATRIX_STACK.translate(bone);
            IGeoRenderer.MATRIX_STACK.moveToPivot(bone);
            IGeoRenderer.MATRIX_STACK.rotate(bone);
            IGeoRenderer.MATRIX_STACK.scale(bone);
            IGeoRenderer.MATRIX_STACK.moveBackFromPivot(bone);
            for (com.exoarsenal.client.DesertScourgeMesh.Quad q : mesh) {
                javax.vecmath.Vector3f normal = new javax.vecmath.Vector3f(q.normal);
                IGeoRenderer.MATRIX_STACK.getNormalMatrix().transform(normal);
                for (int i = 0; i < 4; i++) {
                    float[] v = q.p[i];
                    javax.vecmath.Vector4f p = new javax.vecmath.Vector4f(v[0], v[1], v[2], 1);
                    IGeoRenderer.MATRIX_STACK.getModelMatrix().transform(p);
                    b.pos(p.x, p.y, p.z)
                            .tex(i == 0 || i == 3 ? 0 : 1, i < 2 ? 0 : 1)
                            .color(
                                    ((c >> 16) & 255) / 255F,
                                    ((c >> 8) & 255) / 255F,
                                    (c & 255) / 255F,
                                    alpha)
                            .normal(normal.x, normal.y, normal.z)
                            .endVertex();
                }
            }
            for (GeoBone child : bone.childBones) renderRecursively(b, child, r, g, blue, alpha);
            IGeoRenderer.MATRIX_STACK.pop();
            return;
        }
        IGeoRenderer.super.renderRecursively(
                b,
                bone,
                ((c >> 16) & 255) / 255F,
                ((c >> 8) & 255) / 255F,
                (c & 255) / 255F,
                alpha);
    }

    private static final class Model extends AnimatedGeoModel<EntityDesertScourge> {
        public ResourceLocation getModelLocation(EntityDesertScourge e) {
            return new ResourceLocation(ExoArsenal.MODID, "geo/desert_scourge.geo.json");
        }

        public ResourceLocation getTextureLocation(EntityDesertScourge e) {
            return com.exoarsenal.expedition.client.PixelTextures.location(1);
        }

        public ResourceLocation getAnimationFileLocation(EntityDesertScourge e) {
            return new ResourceLocation(
                    ExoArsenal.MODID, "animations/desert_scourge.animation.json");
        }
    }
}
