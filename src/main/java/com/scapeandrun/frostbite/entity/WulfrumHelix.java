package com.scapeandrun.frostbite.entity;

import net.minecraft.util.math.Vec3d;

public final class WulfrumHelix {
    private WulfrumHelix() {}

    public static Vec3d[] path(Vec3d start, Vec3d end, double phase) {
        Vec3d axis = end.subtract(start);
        Vec3d forward = axis.normalize();
        Vec3d side = forward.crossProduct(new Vec3d(0, 1, 0));
        if (side.lengthSquared() < .0001) side = new Vec3d(1, 0, 0);
        side = side.normalize();
        Vec3d up = side.crossProduct(forward).normalize();
        Vec3d[] points = new Vec3d[49];
        for (int i = 0; i < points.length; i++) {
            double progress = i / 48.0;
            double angle = phase + progress * Math.PI * 4;
            points[i] =
                    start.add(axis.scale(progress))
                            .add(side.scale(Math.cos(angle) * 4))
                            .add(up.scale(Math.sin(angle) * 4));
        }
        return points;
    }
}
