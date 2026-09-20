package com.scapeandrun.frostbite.client.render;

import com.scapeandrun.frostbite.client.model.RBladeModel;
import com.scapeandrun.frostbite.item.ItemRBlade;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class RBladeRenderer extends GeoItemRenderer<ItemRBlade> {
    public RBladeRenderer() {
        super(new RBladeModel());
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

        boolean glow = bone.getName().startsWith("blade") || "emitter".equals(bone.getName());
        float tintRed = 1F, tintGreen = .4F, tintBlue = .16F;
        if (bone.getName().startsWith("blade")) {
            int section =
                    "bladeLower".equals(bone.getName())
                            ? 0
                            : "bladeMid".equals(bone.getName())
                                    ? 1
                                    : "bladeHook".equals(bone.getName()) ? 2 : 3;
            double time = Minecraft.getSystemTime() * 0.004D - section * 0.65D;
            float pulse = (float) ((Math.sin(time) + 1.0D) * 0.5D);
            float chase = (float) ((Math.sin(time * 0.47D + Math.cos(time * 0.21D)) + 1.0D) * 0.5D);
            tintGreen = 0.16F + pulse * 0.16F;
            tintBlue = 0.07F + chase * 0.06F;
        } else if ("emitter".equals(bone.getName())) {
            float pulse = (float) ((Math.sin(Minecraft.getSystemTime() * 0.012D) + 1.0D) * 0.5D);
            tintGreen = 0.72F + pulse * 0.28F;
            tintBlue = 0.68F + pulse * 0.32F;
        }

        if (!bone.isHidden()) {
            for (GeoCube cube : bone.childCubes) {
                IGeoRenderer.MATRIX_STACK.push();
                GlStateManager.pushMatrix();
                renderCube(buffer, cube, red, green, blue, alpha);
                if (glow) {
                    GlStateManager.disableTexture2D();
                    GlStateManager.disableLighting();
                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(
                            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
                    GlStateManager.depthMask(false);
                    renderCube(buffer, cube, tintRed, tintGreen, tintBlue, .3F);
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
        }
        if (!bone.childBonesAreHiddenToo()) {
            for (GeoBone child : bone.childBones)
                renderRecursively(buffer, child, red, green, blue, alpha);
        }
        IGeoRenderer.MATRIX_STACK.pop();
    }
}
