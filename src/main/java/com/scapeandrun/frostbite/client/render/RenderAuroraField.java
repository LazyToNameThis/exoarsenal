package com.scapeandrun.frostbite.client.render;

import com.scapeandrun.frostbite.entity.EntityAuroraField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

public final class RenderAuroraField extends Render<EntityAuroraField> {
    public RenderAuroraField(RenderManager manager) {
        super(manager);
        shadowSize = 0.0F;
    }

    @Override
    public void doRender(
            EntityAuroraField entity, double x, double y, double z, float yaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        GlStateManager.depthMask(false);
        double phase = (entity.ticksExisted + partialTicks) * 0.065D;
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();
        for (int ribbon = 0; ribbon < 4; ribbon++) {
            buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            for (int i = 0; i <= 20; i++) {
                double along = (i / 20.0D - 0.5D) * 10.0D;
                double wave =
                        Math.sin(phase + i * 0.42D + ribbon * 1.3D) * (0.55D + ribbon * 0.12D);
                double depth = (ribbon - 1.5D) * 0.85D;
                float red = 0.10F + ribbon * 0.06F;
                float green = 0.72F + ribbon * 0.055F;
                float blue = 0.92F;
                buffer.pos(along, 0.35D + wave, depth).color(red, green, blue, 0.13F).endVertex();
                buffer.pos(along, 4.4D + wave * 0.4D, depth)
                        .color(red + 0.18F, green, blue, 0.52F)
                        .endVertex();
            }
            tess.draw();
        }
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityAuroraField entity) {
        return null;
    }
}
