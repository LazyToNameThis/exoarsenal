package com.scapeandrun.frostbite.client.render;

import com.scapeandrun.frostbite.entity.EntityWulfrumShard;
import com.scapeandrun.frostbite.client.WholeModelTexture;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public final class WulfrumShardRenderer extends Render<EntityWulfrumShard> {
    private static final double[][] V = {
        {0, .55, 0}, {0, -.4, 0}, {-.22, 0, 0}, {0, 0, -.16}, {.22, 0, 0}, {0, 0, .16}
    };
    private static final int[][] F = {
        {0, 2, 3}, {0, 3, 4}, {0, 4, 5}, {0, 5, 2}, {1, 3, 2}, {1, 4, 3}, {1, 5, 4}, {1, 2, 5}
    };

    public WulfrumShardRenderer(RenderManager m) {
        super(m);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityWulfrumShard e) {
        return WholeModelTexture.get("wulfrum_eyes");
    }

    @Override
    public void doRender(
            EntityWulfrumShard e, double x, double y, double z, float yaw, float partial) {
        bindEntityTexture(e);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate((e.ticksExisted + partial) * 9, 0, 1, 0);
        GlStateManager.rotate(25, 1, 0, 1);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_COLOR);
        float bright = e.charging() && e.waitTicks() < 12 ? 1 : .85F;
        for (int face = 0; face < 8; face++)
            for (int i = 0; i < 3; i++) {
                double[] p = V[F[face][i]];
                double u = (face * 4 + (i == 0 ? 2 : i == 1 ? 0 : 4)) / 64.0,
                        v = (i == 0 ? 120 : 124) / 128.0;
                b.pos(p[0], p[1], p[2]).tex(u, v).color(bright, 1F, bright, 1F).endVertex();
            }
        Tessellator.getInstance().draw();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}
