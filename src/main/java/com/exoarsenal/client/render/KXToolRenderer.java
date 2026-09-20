package com.exoarsenal.client.render;

import com.exoarsenal.client.model.KXToolModel;
import com.exoarsenal.item.ItemRTool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

import java.util.Locale;

public class KXToolRenderer extends GeoItemRenderer<ItemRTool> {
    public KXToolRenderer() {
        super(new KXToolModel());
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
        String name = bone.getName().toLowerCase(Locale.ROOT);
        boolean emitter =
                name.contains("energy")
                        || name.contains("laser")
                        || name.contains("chain")
                        || name.contains("scythe");
        float pulse =
                (float)
                        ((Math.sin(Minecraft.getSystemTime() * 0.016D + name.hashCode()) + 1D)
                                * 0.5D);
        if (!bone.isHidden())
            for (GeoCube cube : bone.childCubes) {
                IGeoRenderer.MATRIX_STACK.push();
                GlStateManager.pushMatrix();
                renderCube(buffer, cube, red * .74F, green * .9F, blue, alpha);
                if (emitter) {
                    GlStateManager.disableTexture2D();
                    GlStateManager.disableLighting();
                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(
                            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
                    GlStateManager.depthMask(false);
                    renderCube(
                            buffer,
                            cube,
                            0.07F + pulse * 0.1F,
                            0.87F + pulse * 0.13F,
                            0.82F + pulse * 0.18F,
                            .34F);
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
