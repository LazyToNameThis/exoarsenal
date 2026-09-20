package com.scapeandrun.frostbite.entity;

import net.minecraft.util.math.Vec3d;

public final class WulfrumAttackGeometry {
    public static boolean intersects(Vec3d a, Vec3d b, Vec3d c, Vec3d d, double radius) {
        Vec3d u = b.subtract(a), v = d.subtract(c), r = a.subtract(c);
        double uu = u.dotProduct(u),
                vv = v.dotProduct(v),
                uv = u.dotProduct(v),
                ur = u.dotProduct(r),
                vr = v.dotProduct(r),
                s,
                t;
        if (uu < 1e-10 && vv < 1e-10) return a.squareDistanceTo(c) <= radius * radius;
        if (uu < 1e-10) {
            s = 0;
            t = clamp(vr / vv);
        } else if (vv < 1e-10) {
            t = 0;
            s = clamp(-ur / uu);
        } else {
            double determinant = uu * vv - uv * uv;
            s = determinant > 1e-10 ? clamp((uv * vr - ur * vv) / determinant) : 0;
            t = (uv * s + vr) / vv;
            if (t < 0) {
                t = 0;
                s = clamp(-ur / uu);
            } else if (t > 1) {
                t = 1;
                s = clamp((uv - ur) / uu);
            }
        }
        return a.add(u.scale(s)).squareDistanceTo(c.add(v.scale(t))) <= radius * radius;
    }

    private static double clamp(double value) {
        return Math.max(0, Math.min(1, value));
    }

    private WulfrumAttackGeometry() {}
}
