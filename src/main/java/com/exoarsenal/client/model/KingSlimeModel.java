package com.exoarsenal.client.model;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public final class KingSlimeModel {
    private KingSlimeModel() {}

    public static void gel(int color, float alpha) {
        GlStateManager.depthMask(false);
        box(-.46, 0, -.39, .46, .12, .39, color, alpha);
        box(-.5, .12, -.43, .5, .34, .43, color, alpha);
        box(-.44, .34, -.38, .44, .51, .38, color, alpha);
        box(-.34, .51, -.29, .34, .63, .29, color, alpha);
        box(-.22, .63, -.19, .22, .68, .19, color, alpha);

        box(-.35, .36, -.387, -.24, .43, -.381, 0xB9F4FF, .65F);
        box(-.34, .43, -.387, -.30, .48, -.381, 0xD8FAFF, .7F);
        box(.29, .17, -.438, .38, .21, -.431, 0x81CFFF, .5F);
        box(-.17, .53, -.298, -.07, .55, -.291, 0xC0EBFF, .65F);
        box(.39, .2, -.2, .445, .24, -.13, 0x84CCFF, .55F);
        box(-.18, .15, .09, -.12, .2, .15, 0x97E6FF, .6F);
        box(.13, .38, .02, .17, .42, .06, 0xC0E9FF, .45F);
        GlStateManager.depthMask(true);
    }

    public static void crown(float y, boolean ruby) {
        for (int side = 0; side < 4; side++) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(side * 90, 0, 1, 0);
            box(-.27, y, -.25, .27, y + .065, -.19, 0xC48B20, 1);
            box(-.28, y + .06, -.26, .28, y + .085, -.18, 0xFFE379, 1);
            box(-.24, y + .085, -.245, -.17, y + .19, -.19, 0xEABD4F, 1);
            box(.17, y + .085, -.245, .24, y + .19, -.19, 0xEABD4F, 1);
            box(-.065, y + .085, -.245, .065, y + .16, -.19, 0xEABD4F, 1);
            box(-.035, y + .16, -.24, .035, y + .235, -.19, 0xFFE787, 1);
            box(
                    -.04,
                    y + .09,
                    -.263,
                    .04,
                    y + .16,
                    -.245,
                    side == 0 ? (ruby ? 0xD8315B : 0x493B33) : 0x3FADDF,
                    1);
            if (side != 0 || ruby) box(-.025, y + .13, -.267, 0, y + .153, -.263, 0xFFC4D4, 1);
            GlStateManager.popMatrix();
        }
    }

    public static void ninja(int pose, float time) {
        ninjaCloth(pose, time);
    }

    static void ninjaCloth(int pose, float time) {

        box(-.145, .68, -.095, .145, 1.10, .10, 0x303032, 1);
        box(-.175, .87, -.105, .175, 1.08, .105, 0x3C3C3F, 1);
        box(-.155, 1.08, -.09, .155, 1.15, .09, 0x353538, 1);
        box(-.10, 1.15, -.075, .10, 1.21, .075, 0x242426, 1);

        box(-.13, 1.04, -.106, -.08, 1.08, -.105, 0x858587, 1);
        box(-.105, .99, -.106, -.065, 1.04, -.105, 0x737376, 1);
        box(-.08, .94, -.106, -.04, .99, -.105, 0x67676A, 1);
        box(-.06, .90, -.106, -.02, .94, -.105, 0x555558, 1);
        box(.085, 1.04, -.106, .125, 1.08, -.105, 0x68686B, 1);
        box(.055, .98, -.106, .095, 1.04, -.105, 0x59595C, 1);
        box(.02, .93, -.106, .065, .98, -.105, 0x4B4B4E, 1);
        box(-.147, .72, -.102, .147, .785, .105, 0x202022, 1);
        box(-.047, .735, -.104, .043, .772, -.102, 0x57575A, 1);
        box(-.048, .64, -.102, -.006, .736, -.098, 0x4B4B4E, 1);
        box(.009, .668, -.103, .045, .735, -.098, 0x67676A, 1);

        box(-.125, 1.205, -.122, .125, 1.26, .112, 0x252528, 1);
        box(-.153, 1.26, -.139, .153, 1.47, .133, 0x242427, 1);
        box(-.133, 1.47, -.122, .133, 1.515, .115, 0x303033, 1);
        box(-.102, 1.515, -.091, .102, 1.535, .083, 0x38383B, 1);

        box(-.154, 1.30, -.104, -.153, 1.43, .055, 0x5C5C5F, 1);
        box(-.133, 1.445, -.140, -.082, 1.469, -.139, 0x89898B, 1);
        box(-.116, 1.469, -.123, -.035, 1.49, -.122, 0xA4A4A5, 1);
        box(-.132, 1.39, -.140, -.109, 1.446, -.139, 0x757578, 1);
        box(.11, 1.32, -.140, .132, 1.418, -.139, 0x555558, 1);

        box(-.108, 1.352, -.141, .108, 1.402, -.139, 0x101012, 1);
        box(-.09, 1.36, -.142, -.042, 1.395, -.141, 0xE5E5E2, 1);
        box(.042, 1.36, -.142, .09, 1.395, -.141, 0xE5E5E2, 1);
        box(-.058, 1.36, -.143, -.042, 1.389, -.142, 0x151517, 1);
        box(.042, 1.36, -.143, .058, 1.389, -.142, 0x151517, 1);
        box(-.025, 1.338, -.142, .025, 1.36, -.139, 0xE1C49C, 1);
        box(-.105, 1.272, -.14, .105, 1.337, -.139, 0x303033, 1);
        box(-.08, 1.284, -.141, .045, 1.299, -.14, 0x4A4A4D, 1);

        box(-.043, 1.372, .132, .043, 1.416, .156, 0x3C3C3F, 1);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 1.389, .149);
        GlStateManager.rotate(24, 1, 0, 0);
        box(-.032, -.012, 0, .012, .012, .145, 0x68686B, 1);
        GlStateManager.translate(0, 0, .135);
        GlStateManager.rotate(26, 1, 0, 0);
        box(-.029, -.012, 0, .009, .012, .105, 0x535356, 1);
        GlStateManager.popMatrix();
        for (int side : new int[] {-1, 1}) {
            float stride = (float) Math.sin(time * .5 + side) * 18;
            GlStateManager.pushMatrix();
            GlStateManager.translate(side * .10, .72, 0);
            GlStateManager.rotate(pose == 1 ? side * 44 : stride - 8, 1, 0, 0);
            box(-.084, -.24, -.085, .084, 0, .095, 0x333336, 1);
            box(-.08, -.265, -.08, .08, -.19, .089, 0x3C3C3F, 1);
            box(-.082, -.18, -.086, -.047, -.045, -.085, 0x59595C, 1);
            GlStateManager.translate(0, -.23, 0);
            GlStateManager.rotate(pose == 1 ? 45 : 18 + Math.abs(stride) * .35F, 1, 0, 0);
            box(-.073, -.235, -.07, .073, .015, .087, 0x2D2D30, 1);
            box(-.074, -.19, -.071, -.037, -.03, -.07, 0x555558, 1);
            box(-.074, -.239, -.073, .074, -.203, .089, 0x6B6B6E, 1);
            box(-.078, -.299, -.139, .078, -.24, .09, 0x202022, 1);
            box(-.069, -.26, -.140, .059, -.241, -.139, 0x818184, 1);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.translate(side * .182, 1.078, 0);
            GlStateManager.rotate(side * -7, 0, 0, 1);
            GlStateManager.rotate(pose == 3 ? -68 : pose == 2 ? -30 : -stride - 8, 1, 0, 0);
            box(-.071, -.197, -.076, .071, .02, .076, 0x3B3B3E, 1);
            box(-.059, -.035, -.077, .044, .014, -.076, 0x77777A, 1);
            box(-.066, -.105, -.077, -.038, -.035, -.076, 0x636366, 1);
            GlStateManager.translate(0, -.186, 0);
            GlStateManager.rotate(pose == 3 ? -16 : -36, 1, 0, 0);
            box(-.059, -.181, -.062, .059, .01, .065, 0x2A2A2D, 1);
            box(-.057, -.18, -.063, .057, -.146, -.062, 0x151517, 1);
            box(-.051, -.225, -.055, .051, -.18, .056, 0xB69B74, 1);
            box(-.048, -.20, -.056, .014, -.181, -.055, 0xE6CAA0, 1);

            if (side == 1 && pose == 3) {
                box(-.035, -.205, -.12, .035, -.181, .075, 0x48484B, 1);
                box(-.015, -.2, -.85, .015, -.186, -.10, 0xC5C9CA, 1);
            }
            GlStateManager.popMatrix();
        }
    }

    public static void box(
            double x0,
            double y0,
            double z0,
            double x1,
            double y1,
            double z1,
            int color,
            float alpha) {
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        face(b, color, alpha, 1, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
        face(b, color, alpha, .55F, x0, y0, z1, x0, y0, z0, x1, y0, z0, x1, y0, z1);
        face(b, color, alpha, .87F, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0);
        face(b, color, alpha, .68F, x1, y0, z1, x1, y1, z1, x0, y1, z1, x0, y0, z1);
        face(b, color, alpha, .75F, x0, y0, z1, x0, y1, z1, x0, y1, z0, x0, y0, z0);
        face(b, color, alpha, .92F, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1);
        Tessellator.getInstance().draw();
    }

    private static void face(BufferBuilder b, int c, float a, float shade, double... p) {
        for (int i = 0; i < 12; i += 3)
            b.pos(p[i], p[i + 1], p[i + 2])
                    .color(
                            ((c >> 16) & 255) / 255F * shade,
                            ((c >> 8) & 255) / 255F * shade,
                            (c & 255) / 255F * shade,
                            a)
                    .endVertex();
    }
}
