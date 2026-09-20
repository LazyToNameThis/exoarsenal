package com.exoarsenal.client.render;

import com.exoarsenal.entity.EntityScourgeSand;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public final class ScourgeSandRenderer extends Render<EntityScourgeSand> {
    public ScourgeSandRenderer(RenderManager m) {
        super(m);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityScourgeSand e) {
        return null;
    }

    @Override
    public void doRender(
            EntityScourgeSand e, double x, double y, double z, float yaw, float partial) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        double age = e.ticksExisted + partial;
        if (e.kind() == 2) {
            GlStateManager.depthMask(false);
            for (int band = 0; band < 32; band++) {
                double low = band * .375,
                        high = low + .5,
                        r = .35 + band * .045,
                        spin = age * .18 + band * .34;
                for (int i = 0; i < 16; i++) {
                    double a = spin + i * Math.PI / 8, c = a + Math.PI / 8;
                    float alpha = .28F + (band % 3) * .08F;
                    v(b, Math.cos(a) * r, low, Math.sin(a) * r, alpha);
                    v(b, Math.cos(c) * r, low, Math.sin(c) * r, alpha);
                    v(b, Math.cos(c + .18) * (r + .08), high, Math.sin(c + .18) * (r + .08), alpha);
                    v(b, Math.cos(a + .18) * (r + .08), high, Math.sin(a + .18) * (r + .08), alpha);
                }
            }
        } else {
            GlStateManager.rotate((float) (age * 17), .4F, 1, .2F);
            double r = e.kind() == 1 ? .32 : .24;
            for (int i = 0; i < 6; i++) {
                double a = i * Math.PI / 3, c = a + Math.PI / 3;
                v(b, Math.cos(a) * r, -r * .5, Math.sin(a) * r, 1);
                v(b, Math.cos(c) * r, -r * .5, Math.sin(c) * r, 1);
                v(b, Math.cos(c) * r * .6, r * .7, Math.sin(c) * r * .6, 1);
                v(b, Math.cos(a) * r * .6, r * .7, Math.sin(a) * r * .6, 1);
            }
        }
        Tessellator.getInstance().draw();
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    private static void v(BufferBuilder b, double x, double y, double z, float a) {
        b.pos(x, y, z).color(.78F, .59F, .32F, a).endVertex();
    }
}
