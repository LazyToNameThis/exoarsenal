package com.exoarsenal.client;

import com.exoarsenal.expedition.client.ExpeditionMesh;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public final class BrawlerBossHands {
    public static final ExpeditionMesh BACK = new ExpeditionMesh(), FRONT = new ExpeditionMesh();

    static {
        int dark = 0x26372D, steel = 0x71836B, edge = 0xBBC6A4, joint = 0x405344;
        BACK.armor(-13, -11, -6, 26, 25, 5, 2, steel).armor(-9, 10, -7, 18, 9, 6, 2, dark);
        BACK.armor(-10, -8, -7, 20, 18, 2, 2, dark);
        for (int i = 0; i < 4; i++) {
            double x = -12 + i * 6, drop = i == 0 ? 2 : i == 3 ? 3 : 0;
            FRONT.armor(x, -14 + drop, -4, 5, 7, 9, 1.1, steel);
            FRONT.armor(x, -9 + drop, 3, 5, 9, 5, 1, edge);
            FRONT.armor(x, 1 + drop, 5, 5, 7, 4, 1, steel);
            FRONT.armor(x, 7 + drop, 1, 5, 5, 7, .9, steel);
            FRONT.box(x + 1, -7 + drop, 8, 3, 1, 0.4, dark);
            FRONT.tube(x + 2.5, -1 + drop, 4, x + 2.5, 2 + drop, 5, 1.6, 1.6, joint);
            FRONT.box(x + 1, 9 + drop, 8, 3, 1, .3, edge);
            FRONT.box(x + 1, -12 + drop, 5, 3, 2, .4, 0x76DB9A);
        }
        FRONT.armor(11, -2, -2, 7, 10, 7, 1.5, dark);
        FRONT.armor(9, 6, 3, 8, 6, 5, 1.4, steel);
        FRONT.armor(5, 9, 5, 7, 5, 4, 1.1, edge);
        BACK.box(-5, 14, -.5, 10, 2, 1, 0x70ECA2);
    }

    public static void draw(boolean front) {
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.disableCull();
        ExpeditionMesh mesh = front ? FRONT : BACK;
        for (int side = 0; side < 2; side++) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(side == 0 ? 47 : 373, 34, front ? 60 : -20);
            GlStateManager.scale(side == 0 ? 1 : -1, 1, 1);
            GlStateManager.rotate(-12, 1, 0, 0);
            GlStateManager.rotate(-13, 0, 1, 0);
            BufferBuilder b = Tessellator.getInstance().getBuffer();
            b.begin(7, DefaultVertexFormats.POSITION_COLOR);
            for (ExpeditionMesh.Face f : mesh.faces) {
                float light =
                        (float) (.58 + .42 * Math.max(0, -f.nx * .35 - f.ny * .5 + f.nz * .78));
                for (double[] p : f.p)
                    b.pos(p[0], p[1], p[2])
                            .color(
                                    (int) ((f.color >> 16 & 255) * light),
                                    (int) ((f.color >> 8 & 255) * light),
                                    (int) ((f.color & 255) * light),
                                    255)
                            .endVertex();
            }
            Tessellator.getInstance().draw();
            GlStateManager.popMatrix();
        }
        GlStateManager.enableCull();
        GlStateManager.disableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.popMatrix();
    }
}
