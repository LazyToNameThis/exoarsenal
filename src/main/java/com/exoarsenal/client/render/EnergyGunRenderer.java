package com.exoarsenal.client.render;

import com.exoarsenal.client.model.EnergyGunModel;
import com.exoarsenal.client.model.KXEnergyGunModel;
import com.exoarsenal.item.ItemEnergyGun;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public class EnergyGunRenderer extends GeoItemRenderer<ItemEnergyGun> {
    private final boolean kx;

    public EnergyGunRenderer() {
        this(false);
    }

    public EnergyGunRenderer(boolean kx) {
        super(kx ? new KXEnergyGunModel() : new EnergyGunModel());
        this.kx = kx;
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
        boolean glow = name.contains("energy");
        float r, g, b;
        if (glow) {
            float pulse = (float) ((Math.sin(Minecraft.getSystemTime() * 0.003D) + 1.0D) * 0.5D);
            if (kx) {
                r = 0.12F + pulse * 0.16F;
                g = 0.88F + pulse * 0.12F;
                b = 0.78F + pulse * 0.22F;
            } else if (name.startsWith("x10")) {
                r = 1.0F;
                g = 0.72F + pulse * 0.28F;
                b = 0.12F + pulse * 0.24F;
            } else {
                r = 1.0F;
                g = 0.16F + pulse * 0.12F;
                b = 0.08F + pulse * 0.06F;
            }
        } else {
            r = red;
            g = green;
            b = blue;
        }
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
                    renderCube(buffer, cube, r, g, b, .28F);
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
