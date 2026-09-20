package com.exoarsenal.entity;

import net.minecraft.util.math.Vec3d;

public final class ExcavatorGeometry {
    public static Vec3d tether(Vec3d a, Vec3d b, double tension, double f) {
        Vec3d d = b.subtract(a), normal = new Vec3d(-d.z, 0, d.x).normalize();
        return a.add(d.scale(f)).add(normal.scale(Math.sin(Math.PI * f) * tension));
    }

    public static double distanceToSegment(Vec3d point, Vec3d a, Vec3d b) {
        Vec3d d = b.subtract(a);
        double f =
                Math.max(
                        0,
                        Math.min(
                                1,
                                point.subtract(a).dotProduct(d)
                                        / Math.max(.000001, d.lengthSquared())));
        return point.distanceTo(a.add(d.scale(f)));
    }

    public static Vec3d reflected(Vec3d incoming, Vec3d normal) {
        Vec3d n = normal.normalize();
        return incoming.subtract(n.scale(2 * incoming.dotProduct(n))).normalize();
    }

    public static boolean crosses(Vec3d a, Vec3d b, Vec3d c, Vec3d d, double radius) {
        Vec3d u = b.subtract(a), v = d.subtract(c), w = a.subtract(c);
        double aa = u.dotProduct(u),
                bb = u.dotProduct(v),
                cc = v.dotProduct(v),
                dd = u.dotProduct(w),
                ee = v.dotProduct(w),
                den = aa * cc - bb * bb;
        if (aa < 1e-9) return distanceToSegment(a, c, d) <= radius;
        if (cc < 1e-9) return distanceToSegment(c, a, b) <= radius;
        double s = den < 1e-9 ? 0 : Math.max(0, Math.min(1, (bb * ee - cc * dd) / den));
        double t = Math.max(0, Math.min(1, (bb * s + ee) / cc));
        s = Math.max(0, Math.min(1, (bb * t - dd) / aa));
        return a.add(u.scale(s)).distanceTo(c.add(v.scale(t))) <= radius;
    }

    public static Vec3d cutterNormal(Vec3d axis, double angle) {
        Vec3d side = axis.crossProduct(new Vec3d(0, 1, 0));
        if (side.lengthSquared() < .001) side = new Vec3d(1, 0, 0);
        side = side.normalize();
        Vec3d up = side.crossProduct(axis).normalize();
        return side.scale(Math.cos(angle))
                .add(up.scale(Math.sin(angle)))
                .add(axis.scale(.45))
                .normalize();
    }

    public static Vec3d turretSocket(Vec3d center, double yaw, double pitch) {
        return center.addVector(
                Math.sin(yaw) * Math.sin(pitch) * 1.2,
                Math.cos(pitch) * 1.2,
                Math.cos(yaw) * Math.sin(pitch) * 1.2);
    }

    public static Vec3d turretMuzzle(Vec3d base, double yaw, double pitch) {
        double z = .95 * Math.sin(pitch) + 2.7 * Math.cos(pitch),
                y = .95 * Math.cos(pitch) - 2.7 * Math.sin(pitch);
        return base.addVector(Math.sin(yaw) * z, y, Math.cos(yaw) * z);
    }

    private ExcavatorGeometry() {}
}
