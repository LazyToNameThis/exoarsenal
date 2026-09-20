package com.scapeandrun.frostbite.client.render;

import com.scapeandrun.frostbite.client.model.X20ScoutModel;
import com.scapeandrun.frostbite.entity.EntityX20Scout;
import com.scapeandrun.frostbite.entity.ScoutCombatPattern;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

import java.util.Locale;

public final class X20ScoutRenderer extends GeoEntityRenderer<EntityX20Scout> {
    private float attackLight;
    private EntityX20Scout rendering;
    private float renderingPartial;

    public X20ScoutRenderer(RenderManager manager) {
        super(manager, new X20ScoutModel());
        shadowSize = 2.4F;
    }

    @Override
    public boolean shouldRender(
            EntityX20Scout entity,
            net.minecraft.client.renderer.culling.ICamera camera,
            double x,
            double y,
            double z) {
        if (super.shouldRender(entity, camera, x, y, z)) return true;
        if (entity.getProjectileKind() != 0
                && camera.isBoundingBoxInFrustum(
                        new net.minecraft.util.math.AxisAlignedBB(
                                        entity.getHammerPosition(), entity.getHammerPosition())
                                .grow(3))) return true;
        if (entity.getScene() == 7) return true;
        if (camera.isBoundingBoxInFrustum(entity.getEntityBoundingBox().grow(14, 6, 14)))
            return true;
        return entity.getHammerFlight() != 0
                && camera.isBoundingBoxInFrustum(
                        new net.minecraft.util.math.AxisAlignedBB(
                                        entity.getHammerPosition(), entity.getHammerPosition())
                                .grow(3));
    }

    @Override
    public void doRender(
            EntityX20Scout entity, double x, double y, double z, float yaw, float partial) {
        if (entity.ticksExisted < 2) return;
        float tick = entity.getAttackTick() + partial;
        int attack = entity.getAttack();
        float contact =
                attack == ScoutCombatPattern.BASH
                        ? 18
                        : attack == ScoutCombatPattern.CROSS
                                ? 22
                                : attack == ScoutCombatPattern.CANNON
                                        ? 36
                                        : attack == ScoutCombatPattern.MINIGUN ? 24 : 20;
        attackLight = 0;
        if (attack >= ScoutCombatPattern.SLASH && attack != ScoutCombatPattern.STUN) {
            attackLight = Math.max(0, 1 - Math.abs(tick - contact) / 9F);
            if (attack == ScoutCombatPattern.CROSS)
                attackLight = Math.max(attackLight, Math.max(0, 1 - Math.abs(tick - 34) / 7F));
            if (attack == ScoutCombatPattern.LEAP)
                attackLight =
                        entity.getWaveAge() >= 0
                                ? Math.max(0, 1 - entity.getWaveAge() / 12F)
                                : .25F;
        }
        if (entity.getScene() == 2 || entity.getScene() == 5)
            attackLight = Math.max(0, 1 - (entity.getSceneTick() + partial) / 10F);
        if (entity.getScene() == 3)
            attackLight = Math.min(1, (entity.getSceneTick() + partial) / 90F) * .65F;
        if (entity.getScene() == 0 && ScoutCombatPattern.extended(attack))
            attackLight = ScoutCombatPattern.expertContactLight(attack, tick);
        if (entity.isOverheating())
            attackLight =
                    .55F
                            + .35F
                                    * (float)
                                            Math.pow(
                                                    Math.sin(
                                                            (entity.getOverheatTick() + partial)
                                                                    * .18),
                                                    2);
        rendering = entity;
        renderingPartial = partial;
        try {
            super.doRender(entity, x, y, z, yaw, partial);
        } finally {
            attackLight = 0;
            rendering = null;
        }
    }

    @Override
    public void renderCube(
            BufferBuilder buffer, GeoCube cube, float red, float green, float blue, float alpha) {
        GeoCubeFaces.prepare(cube);
        super.renderCube(buffer, cube, red, green, blue, alpha);
    }

    @Override
    public void renderRecursively(
            BufferBuilder buffer, GeoBone bone, float red, float green, float blue, float alpha) {
        IGeoRenderer.MATRIX_STACK.push();
        IGeoRenderer.MATRIX_STACK.translate(bone);
        IGeoRenderer.MATRIX_STACK.moveToPivot(bone);
        IGeoRenderer.MATRIX_STACK.rotate(bone);
        IGeoRenderer.MATRIX_STACK.scale(bone);
        IGeoRenderer.MATRIX_STACK.moveBackFromPivot(bone);
        if (rendering != null) {
            String socket = bone.getName();
            if (socket.equals("leftSword"))
                capture(
                        "hand",
                        bone.rotationPointX / 16F,
                        bone.rotationPointY / 16F,
                        bone.rotationPointZ / 16F);
            if (socket.equals("thrownHammer")) capture("hammer", 0, 0, 0);
            if (socket.equals("phaseMinigun")) capture("minigun", 9.5F / 16, 21F / 16, -13.2F / 16);
            if (socket.equals("phaseCannon"))
                capture("cannon", -9.5F / 16, 21.5F / 16, -16.7F / 16);
        }
        String name = bone.getName().toLowerCase(Locale.ROOT);
        boolean wing = name.equals("leftwingice") || name.equals("rightwingice");
        if (wing && !bone.isHidden()) {
            flush(buffer);
            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            ScoutWingMesh.emit(buffer, name.startsWith("right"), false);
            ScoutWingMesh.emit(buffer, name.startsWith("right"), true);
            flush(buffer);
            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();
        }
        boolean energy =
                name.contains("reactorglow")
                        || bone.getName().endsWith("Ice")
                        || name.contains("visorglow");
        float pulse = (float) ((Math.sin(Minecraft.getSystemTime() * 0.0025D) + 1D) * .5D);
        if (!bone.isHidden() && !wing)
            for (GeoCube cube : bone.childCubes) {
                IGeoRenderer.MATRIX_STACK.push();
                GlStateManager.pushMatrix();
                boolean skin = com.scapeandrun.frostbite.client.ScoutOperatorPalette.applies(name);
                boolean glass = name.equals("cockpitglass");
                if (skin || glass) flush(buffer);
                if (skin) GlStateManager.disableTexture2D();
                if (glass) {
                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(
                            GlStateManager.SourceFactor.SRC_ALPHA,
                            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                    GlStateManager.depthMask(false);
                }
                int material = com.scapeandrun.frostbite.client.ScoutOperatorPalette.color(name);
                int tint =
                        com.scapeandrun.frostbite.client.FrigidPalette.color(
                                name, energy && attackLight > .35F);
                renderCube(
                        buffer,
                        cube,
                        skin ? ((material >> 16) & 255) / 255F : red * (tint >> 16 & 255) / 255F,
                        skin ? ((material >> 8) & 255) / 255F : green * (tint >> 8 & 255) / 255F,
                        skin ? (material & 255) / 255F : blue * (tint & 255) / 255F,
                        glass ? .22F : alpha);
                if (skin || glass) flush(buffer);
                if (glass) {
                    GlStateManager.depthMask(true);
                    GlStateManager.disableBlend();
                }
                if (skin) GlStateManager.enableTexture2D();
                if (energy) {
                    flush(buffer);

                    IGeoRenderer.MATRIX_STACK.pop();
                    IGeoRenderer.MATRIX_STACK.push();
                    GlStateManager.disableTexture2D();
                    GlStateManager.disableLighting();
                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(
                            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
                    GlStateManager.depthMask(false);
                    boolean hot = rendering != null && rendering.isOverheating();
                    renderCube(
                            buffer,
                            cube,
                            hot ? 1F : .24F + attackLight * .4F,
                            hot ? .25F + attackLight * .25F : .56F + attackLight * .3F,
                            hot ? .06F : 1F,
                            .10F + pulse * .06F + attackLight * .24F);
                    flush(buffer);
                    GlStateManager.depthMask(true);
                    GlStateManager.disableBlend();
                    GlStateManager.blendFunc(
                            GlStateManager.SourceFactor.SRC_ALPHA,
                            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                    GlStateManager.enableLighting();
                    GlStateManager.enableTexture2D();
                }
                GlStateManager.popMatrix();
                IGeoRenderer.MATRIX_STACK.pop();
            }
        if (!bone.childBonesAreHiddenToo()) {

            for (GeoBone child : bone.childBones)
                if (!child.getName().equals("cockpitGlass"))
                    renderRecursively(buffer, child, red, green, blue, alpha);
            for (GeoBone child : bone.childBones)
                if (child.getName().equals("cockpitGlass"))
                    renderRecursively(buffer, child, red, green, blue, alpha);
        }
        IGeoRenderer.MATRIX_STACK.pop();
    }

    private static void flush(BufferBuilder buffer) {
        net.minecraft.client.renderer.Tessellator.getInstance().draw();
        buffer.begin(
                org.lwjgl.opengl.GL11.GL_QUADS,
                net.minecraft.client.renderer.vertex.DefaultVertexFormats
                        .POSITION_TEX_COLOR_NORMAL);
    }

    private void capture(String name, float x, float y, float z) {
        javax.vecmath.Vector4f point = new javax.vecmath.Vector4f(x, y, z, 1);
        IGeoRenderer.MATRIX_STACK.getModelMatrix().transform(point);
        float yaw =
                rendering.prevRenderYawOffset
                        + net.minecraft.util.math.MathHelper.wrapDegrees(
                                        rendering.renderYawOffset - rendering.prevRenderYawOffset)
                                * renderingPartial;
        double angle = Math.toRadians(180 - yaw), c = Math.cos(angle), s = Math.sin(angle);
        double bx =
                rendering.lastTickPosX
                        + (rendering.posX - rendering.lastTickPosX) * renderingPartial;
        double by =
                rendering.lastTickPosY
                        + (rendering.posY - rendering.lastTickPosY) * renderingPartial;
        double bz =
                rendering.lastTickPosZ
                        + (rendering.posZ - rendering.lastTickPosZ) * renderingPartial;
        ScoutRenderSockets.put(
                rendering,
                name,
                new net.minecraft.util.math.Vec3d(
                        bx + point.x * c + point.z * s,
                        by + point.y + .01,
                        bz - point.x * s + point.z * c));
    }
}
