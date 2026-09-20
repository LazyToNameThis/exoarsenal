package com.scapeandrun.frostbite.client.render;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.Vec3d;

public final class WulfrumFire {
    public static Vec3d sample(
            Vec3d origin,
            Vec3d direction,
            double length,
            double radius,
            double age,
            int tongue,
            double f,
            double edge) {
        Vec3d axis = direction.normalize(),
                right =
                        axis.crossProduct(
                                        Math.abs(axis.y) > .9
                                                ? new Vec3d(1, 0, 0)
                                                : new Vec3d(0, 1, 0))
                                .normalize(),
                up = axis.crossProduct(right);
        double phase = tongue * 2.399963,
                flutter = Math.sin(age * .63 - f * 11 + phase),
                span = length * (.76 + .18 * Math.sin(age * .31 + phase));
        double spread = radius * (1 - f) * (.7 + .25 * Math.sin(f * 9 - age * .45 + phase)),
                angle = phase + f * .7 + flutter * .12;
        return origin.add(axis.scale(span * f))
                .add(right.scale(Math.cos(angle) * spread + edge * radius * (1 - f) * .32))
                .add(up.scale(Math.sin(angle) * spread + flutter * radius * f * .22));
    }

    public static void emit(
            BufferBuilder b,
            Vec3d origin,
            Vec3d direction,
            double length,
            double radius,
            double age) {
        if (length <= .02 || radius <= 0) return;
        for (int layer = 0; layer < 3; layer++)
            for (int tongue = 0; tongue < 7; tongue++)
                for (int step = 0; step < 9; step++) {
                    double f = step / 9D,
                            g = (step + 1) / 9D,
                            l = length * (layer == 0 ? 1.15 : layer == 1 ? 1 : .55),
                            r = radius * (layer == 0 ? 1.3 : layer == 1 ? 1 : .45);
                    int color = layer == 0 ? 0x179A43 : layer == 1 ? 0x4DFF69 : 0xD5FFD1;
                    float alpha =
                            (layer == 0 ? .16F : layer == 1 ? .52F : .86F) * (float) (1 - f * .8);
                    vertex(b, sample(origin, direction, l, r, age, tongue, f, -1), color, alpha);
                    vertex(
                            b,
                            sample(origin, direction, l, r, age, tongue, g, -1),
                            color,
                            alpha * .8F);
                    vertex(
                            b,
                            sample(origin, direction, l, r, age, tongue, g, 1),
                            color,
                            alpha * .8F);
                    vertex(b, sample(origin, direction, l, r, age, tongue, f, 1), color, alpha);
                }
        for (int i = 0; i < 7; i++) {
            double f = (age * .055 + i * .143) % 1;
            Vec3d p = sample(origin, direction, length * 1.7, radius * 1.6, age, i, f, 0);
            WulfrumRayRenderer.tube(
                    b,
                    p,
                    p.add(direction.normalize().scale(.08 + f * .12)),
                    .025 * (1 - f) + .008,
                    0xA4FF9F,
                    (float) (1 - f) * .8F);
        }
    }

    private static void vertex(BufferBuilder b, Vec3d p, int c, float a) {
        b.pos(p.x, p.y, p.z)
                .color((c >> 16 & 255) / 255F, (c >> 8 & 255) / 255F, (c & 255) / 255F, a)
                .endVertex();
    }

    private WulfrumFire() {}
}
