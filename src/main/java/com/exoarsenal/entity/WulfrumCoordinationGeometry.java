package com.exoarsenal.entity;

import net.minecraft.util.math.*;

public final class WulfrumCoordinationGeometry {
    public static double cageRadius(Vec3d direction, Vec3d dent, double strength) {
        double alignment = Math.max(0, direction.normalize().dotProduct(dent.normalize()));
        return 12 - 3 * Math.pow(alignment, 12) * MathHelper.clamp(strength, 0, 1);
    }

    public static Vec3d reflect(Vec3d velocity, Vec3d normal) {
        Vec3d n = normal.normalize();
        return velocity.subtract(n.scale(2 * velocity.dotProduct(n)));
    }

    public static double alignedAngle(double t, double speed, double target) {
        if (t < 190) return t * speed;
        double dt = Math.min(50, t - 190),
                f = BrawlerScore.smooth(dt / 50),
                start = 190 * speed,
                delta = Math.atan2(Math.sin(target - start), Math.cos(target - start));
        return start + speed * dt * (1 - f) * (1 - f) + delta * f;
    }

    public static double eclipseRadius() {
        return ExcavatorSpine.SPACING
                / (2 * Math.sin((Math.PI * 1.5) / (2 * (ExcavatorSpine.COUNT - 1))));
    }

    private WulfrumCoordinationGeometry() {}
}
