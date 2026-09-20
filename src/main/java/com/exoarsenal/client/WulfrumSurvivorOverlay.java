package com.exoarsenal.client;

import com.exoarsenal.entity.EntityWulfrumEye;
import com.exoarsenal.client.render.WulfrumEyeRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;

public final class WulfrumSurvivorOverlay {
    public static ScoutBossBarLayout.Canvas fracture(
            final ScoutBossBarLayout.Canvas out,
            final boolean seer,
            final int tick,
            final boolean powered) {
        if (tick < 45 && !powered) return out;
        final int left = seer ? 224 : 56, right = seer ? 365 : 197;
        return new ScoutBossBarLayout.Canvas() {
            public void rect(int x, int y, int xx, int yy, int color) {
                if (xx <= left || x >= right) {
                    out.rect(x, y, xx, yy, color);
                    return;
                }
                if (x < left) out.rect(x, y, left, yy, color);
                if (xx > right) out.rect(right, y, xx, yy, color);
                if (powered || tick >= 108) return;
                for (int piece = 0; piece < 5; piece++) {
                    int a = Math.max(x, left + piece * 29),
                            b = Math.min(xx, Math.min(right, left + (piece + 1) * 29));
                    if (b <= a) continue;
                    double age = Math.max(0, tick - 45 - piece * 3),
                            turn = age * .065 * (piece % 2 == 0 ? 1 : -1),
                            scale = Math.max(.16, Math.abs(Math.cos(turn)));
                    int center = left + piece * 29 + 14,
                            shift = (int) ((piece - 2) * age * .12),
                            fall = (int) (age * age * .017);
                    int px = center + shift + (int) ((a - center) * scale),
                            qx = center + shift + (int) ((b - center) * scale);
                    int py = y + fall + (int) (Math.sin(turn) * (a - center) * .22),
                            qy = yy + fall + (int) (Math.sin(turn) * (b - center) * .22);
                    int alpha = (int) ((color >>> 24) * Math.max(0, 1 - age / 65));
                    int tint = color & 0xFFFFFF;
                    if (!seer && age < 20) tint = 0xFF9C42;
                    if (qy > 0 && py < 80)
                        out.rect(
                                Math.max(0, px),
                                Math.max(0, py),
                                Math.max(Math.max(0, px), Math.min(420, qx)),
                                Math.min(80, Math.max(py + 1, qy)),
                                alpha << 24 | tint);
                }
            }

            public void label(String text, int x, int y, int color) {
                if (x < left || x > right) out.label(text, x, y, color);
            }

            public void heading(String text, int x, int y, int color, float scale) {
                if (x < left || x > right) out.heading(text, x, y, color, scale);
            }
        };
    }

    static void draw(ScoutBossBarLayout.Canvas c, EntityWulfrumEye eye, float partial) {
        draw(
                c,
                eye.seer(),
                eye.survivorTick(),
                eye.overclocked(),
                eye.energyShield(),
                eye.ticksExisted + partial);
    }

    public static void draw(
            ScoutBossBarLayout.Canvas c,
            boolean left,
            int t,
            boolean powered,
            float shield,
            float age) {
        int dead = left ? 220 : 56, live = left ? 56 : 220;
        if (t >= 45 || powered) {

            for (int j = 0; j < 8; j++) {
                int edge = left ? 222 : 196,
                        y = 22 + j * 3,
                        w = left ? 2 + (j * 7 % 5) : 2 + (j % 3);
                int color = left ? 0xFF8DAB97 : t > 0 && t < 135 ? 0xFFFF7C29 : 0xFF683E2A;
                c.rect(edge - w, y, edge + w, y + 2, color);
                if (!left && t > 0 && t < 120) c.rect(edge - w, y, edge + w, y + 1, 0xFFFFE6A4);
            }
            if (t >= 70 && t < 135)
                for (int i = 0; i < 16; i++) {
                    float f = ((t - 70) * .025F + i / 16F) % 1;
                    int x = (int) ((dead + 72) * (1 - f) + (live + 72) * f);
                    int y = 32 - (int) (Math.sin(f * Math.PI) * 20);
                    c.rect(x, y, x + 2, y + 2, 0xFFC1FFC0);
                }
        }
        if (powered || t >= 110) {
            int color = 0xFF000000 | (java.awt.Color.HSBtoRGB(age * .003F % 1, .55F, 1) & 0xFFFFFF);
            c.rect(live, 25, live + 144, 27, color);
            c.rect(live, 41, live + (int) (144 * shield / 50), 43, 0xFFDEFFD9);
        }
        if (!left && t >= 45 && t < 85)
            for (int i = 0; i < 6; i++) {
                int x = dead + Math.floorMod(t * 17 + i * 23, 144);
                c.rect(x, 20, x + 2, 45, 0xFFADFFD0);
            }
    }

    static void actor(EntityWulfrumEye eye, float partial) {
        int t = eye.survivorTick();
        if (t < 25 || t >= 135) return;
        float x = eye.seer() ? 292 : 128, y = 55;
        if (eye.seer() && t >= 40 && t < 85) {
            float pass = (t - 40) / 45F;
            x = 208 + pass * 168;
            y = 48 - (float) Math.sin(pass * Math.PI * 5) * 16;
        }
        if (!eye.seer()) {
            x = 182 + (float) Math.sin(t * .16) * 3;
            y = 58 + (float) Math.cos(t * .11) * 4;
        }
        float scale = 9 * (t < 40 ? (t - 25) / 15F : t > 120 ? (135 - t) / 15F : 1);
        if (!eye.seer() && t >= 45 && t < 85) {
            com.exoarsenal.client.render.WulfrumRayRenderer.begin();
            net.minecraft.client.renderer.BufferBuilder mesh =
                    net.minecraft.client.renderer.Tessellator.getInstance().getBuffer();
            mesh.begin(7, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR);
            for (int i = 0; i < 3; i++) {
                double hit = 58 + Math.floorMod(t * 13 + i * 37, 138);
                net.minecraft.util.math.Vec3d
                        from = new net.minecraft.util.math.Vec3d(x - 12, y - 12, 120),
                        to = new net.minecraft.util.math.Vec3d(hit, 28 + i * 4, 0);
                com.exoarsenal.client.render.WulfrumRayRenderer.tube(
                        mesh, from, to, .8, 0x8DFFB6, .85F);
                com.exoarsenal.client.render.WulfrumRayRenderer.tube(
                        mesh, to, to.addVector(0, -6 - i * 2, 5), 1.1, 0xFFB15C, .9F);
            }
            net.minecraft.client.renderer.Tessellator.getInstance().draw();
            com.exoarsenal.client.render.WulfrumRayRenderer.finish();
        }
        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 160 + Math.sin(t * .09) * 28);
        GlStateManager.scale(scale, -scale, scale);
        GlStateManager.rotate(180 + (eye.seer() ? (float) Math.sin(t * .35) * 32 : 0), 0, 0, 1);
        GlStateManager.rotate(20 + (float) Math.sin(t * .12) * 18, 1, 0, 0);
        RenderHelper.enableStandardItemLighting();
        mc.getRenderManager().setRenderShadow(false);
        try {
            WulfrumEyeRenderer.foreground = true;
            mc.getRenderManager().renderEntity(eye, 0, 0, 0, eye.seer() ? 90 : 180, partial, false);
        } finally {
            WulfrumEyeRenderer.foreground = false;
            mc.getRenderManager().setRenderShadow(true);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
            GlStateManager.color(1, 1, 1, 1);
        }
    }

    private WulfrumSurvivorOverlay() {}
}
