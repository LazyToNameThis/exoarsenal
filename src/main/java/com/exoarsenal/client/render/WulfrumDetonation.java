package com.exoarsenal.client.render;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.Vec3d;

public final class WulfrumDetonation {
    public static void cloud(BufferBuilder b, double age) {
        double rise = 1 - Math.exp(-Math.max(0, age) * .045), height = 3 + 23 * rise;
        float fade = (float) Math.max(0, Math.min(1, (170 - age) / 45));
        for (int i = 0; i < 12; i++) {
            double f = i / 11D, y = height * f, r = (2.1 + f * 2.1 + (1 - f) * (1 - f) * 2) * rise;
            puff(
                    b,
                    new Vec3d(Math.sin(f * 8 + age * .02) * .7, y, Math.cos(f * 7) * .7),
                    r,
                    2.6,
                    r,
                    0x4A5147,
                    fade * .75F,
                    i + age * .025);
        }
        double radius = 2 + 10 * rise;
        puff(
                b,
                new Vec3d(0, height + 1, 0),
                radius * .8,
                3.8 * rise + 1,
                radius * .8,
                0x717568,
                fade * .95F,
                age * .012);
        for (int i = 0; i < 18; i++) {
            double a = i * Math.PI / 9,
                    roll = age * .035 + i * .8,
                    rr = radius + Math.cos(roll) * .7;
            Vec3d c = new Vec3d(Math.cos(a) * rr, height + Math.sin(roll) * 1.3, Math.sin(a) * rr);
            puff(b, c, 3.2, 2.7, 3.2, i % 3 == 0 ? 0x8B8E79 : 0x62695D, fade * .9F, roll);
        }
        for (int i = 0; i < 24; i++) {
            double a = i * Math.PI / 12, r = 3 + age * .18;
            puff(
                    b,
                    new Vec3d(Math.cos(a) * r, .7, Math.sin(a) * r),
                    2.2 + age * .01,
                    1.1,
                    2.2 + age * .01,
                    0x827C62,
                    fade * .45F,
                    a);
        }
        if (age < 34) {
            float heat = (float) (1 - age / 34);
            puff(
                    b,
                    new Vec3d(0, 2 + age * .15, 0),
                    3 + age * .07,
                    4,
                    3 + age * .07,
                    0xC8FFAB,
                    heat * .8F,
                    age * .04);
        }
    }

    public static void puff(
            BufferBuilder b,
            Vec3d center,
            double rx,
            double ry,
            double rz,
            int color,
            float alpha,
            double phase) {
        for (int lat = 0; lat < 8; lat++)
            for (int lon = 0; lon < 12; lon++)
                for (int k = 0; k < 4; k++) {
                    double a = (lat + (k >= 2 ? 1 : 0)) * Math.PI / 8 - Math.PI / 2,
                            c = (lon + (k == 1 || k == 2 ? 1 : 0)) * Math.PI / 6;
                    double noise = 1 + .055 * Math.sin(c * 3 + a * 5 + phase),
                            shade = .68 + .24 * Math.sin(a) + .08 * Math.cos(c - phase);
                    b.pos(
                                    center.x + Math.cos(a) * Math.cos(c) * rx * noise,
                                    center.y + Math.sin(a) * ry * noise,
                                    center.z + Math.cos(a) * Math.sin(c) * rz * noise)
                            .color(
                                    (float) ((color >> 16 & 255) / 255D * shade),
                                    (float) ((color >> 8 & 255) / 255D * shade),
                                    (float) ((color & 255) / 255D * shade),
                                    alpha)
                            .endVertex();
                }
    }

    private WulfrumDetonation() {}
}
