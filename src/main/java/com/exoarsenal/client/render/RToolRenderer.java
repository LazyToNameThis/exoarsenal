package com.exoarsenal.client.render;

import com.exoarsenal.client.model.RToolModel;
import com.exoarsenal.item.ItemRTool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public class RToolRenderer extends GeoItemRenderer<ItemRTool> {
    public RToolRenderer() {
        super(new RToolModel());
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
        float r = 1F;
        float g = .35F;
        float b = .12F;
        boolean glow =
                bone.getName().toLowerCase().contains("energy")
                        || bone.getName().toLowerCase().contains("laser");
        if (glow) {
            float pulse = (float) ((Math.sin(Minecraft.getSystemTime() * 0.004D) + 1.0D) * 0.5D);
            g = 0.16F + pulse * 0.16F;
            b = 0.07F + pulse * 0.06F;
        }
        if (!bone.isHidden())
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
                    renderCube(buffer, cube, r, g, b, .28F);
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
        if (!bone.childBonesAreHiddenToo())
            for (GeoBone child : bone.childBones)
                renderRecursively(buffer, child, red, green, blue, alpha);
        IGeoRenderer.MATRIX_STACK.pop();
    }
}
