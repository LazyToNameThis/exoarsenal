package com.exoarsenal.client.render;

import com.exoarsenal.client.model.ScoutWeaponModel;
import com.exoarsenal.item.ItemScoutWeapon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public final class ScoutWeaponRenderer extends GeoItemRenderer<ItemScoutWeapon> {
    public ScoutWeaponRenderer() {
        super(new ScoutWeaponModel());
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

        boolean glow = name.contains("energy") || name.contains("railcore");
        float pulse =
                (float)
                        ((Math.sin(Minecraft.getSystemTime() * 0.0025D + name.hashCode()) + 1.0D)
                                * 0.5D);
        float r = name.contains("aurora") ? .48F : .22F;
        float g = name.contains("aurora") ? .72F : .78F + pulse * .12F;
        float b = 1F;
        if (!bone.isHidden())
            for (GeoCube cube : bone.childCubes) {
                IGeoRenderer.MATRIX_STACK.push();
                GlStateManager.pushMatrix();
                renderCube(buffer, cube, red, green, blue, alpha);
                if (glow) {
                    flush(buffer);
                    IGeoRenderer.MATRIX_STACK.pop();
                    IGeoRenderer.MATRIX_STACK.push();
                    GlStateManager.disableTexture2D();
                    GlStateManager.disableLighting();
                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(
                            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
                    GlStateManager.depthMask(false);
                    renderCube(buffer, cube, r, g, b, .16F + pulse * .12F);
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
        if (!bone.childBonesAreHiddenToo())
            for (GeoBone child : bone.childBones)
                renderRecursively(buffer, child, red, green, blue, alpha);
        IGeoRenderer.MATRIX_STACK.pop();
    }

    private static void flush(BufferBuilder buffer) {
        net.minecraft.client.renderer.Tessellator.getInstance().draw();
        buffer.begin(
                org.lwjgl.opengl.GL11.GL_QUADS,
                net.minecraft.client.renderer.vertex.DefaultVertexFormats
                        .POSITION_TEX_COLOR_NORMAL);
    }
}
