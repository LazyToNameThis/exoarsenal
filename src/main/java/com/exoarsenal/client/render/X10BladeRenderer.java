package com.exoarsenal.client.render;

import com.exoarsenal.client.model.X10BladeModel;
import com.exoarsenal.item.ItemRBlade;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public class X10BladeRenderer extends GeoItemRenderer<ItemRBlade> {
    public X10BladeRenderer() {
        super(new X10BladeModel());
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
        String name = bone.getName().toLowerCase(java.util.Locale.ROOT);
        boolean glow = name.contains("blade") || name.contains("energy") || name.contains("disc");
        float r = 1F, g = .75F, b = .1F;
        if (glow) {
            double time = Minecraft.getSystemTime() * 0.004D;
            float pulse = (float) ((Math.sin(time) + 1.0D) * 0.5D);
            g = 0.72F + pulse * 0.12F;
            b = 0.08F + pulse * 0.08F;
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
                    renderCube(buffer, cube, r, g, b, .3F);
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
