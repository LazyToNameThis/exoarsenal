package com.scapeandrun.frostbite.entity;

import net.minecraft.util.math.Vec3d;

public final class WulfrumSawMechanics {
    public static Vec3d trailingPoint(Vec3d handler, Vec3d direction, double length) {
        Vec3d horizontal = new Vec3d(direction.x, 0, direction.z).normalize();
        if (horizontal.lengthSquared() < 1e-8) horizontal = new Vec3d(0, 0, 1);
        return handler.subtract(horizontal.scale(length));
    }

    public static Vec3d tangent(Vec3d center, Vec3d point, Vec3d velocity) {
        Vec3d radial = point.subtract(center).normalize();
        Vec3d tangent = velocity.subtract(radial.scale(velocity.dotProduct(radial))).normalize();
        if (tangent.lengthSquared() < 1e-8) tangent = new Vec3d(-radial.z, 0, radial.x).normalize();
        if (tangent.lengthSquared() < 1e-8) tangent = new Vec3d(1, 0, 0);
        return tangent;
    }

    public static Vec3d normal(int plane) {
        return plane == 0
                ? new Vec3d(0, 1, 0)
                : plane == 1 ? new Vec3d(0, 0, 1) : new Vec3d(1, 0, 0);
    }

    public static Vec3d beltVelocity(
            Vec3d position,
            Vec3d velocity,
            Vec3d anchor,
            Vec3d desired,
            double length,
            double speed) {
        Vec3d cable = anchor.subtract(position);
        double stretch = Math.max(0, cable.lengthVector() - length);
        Vec3d drive = desired.subtract(position).normalize().scale(.8);
        Vec3d next =
                velocity.scale(.76)
                        .add(drive)
                        .add(cable.normalize().scale(Math.min(2.4, stretch * .45)));
        double magnitude = next.lengthVector();
        return magnitude > speed ? next.scale(speed / magnitude) : next;
    }

    public static Vec3d discHit(Vec3d from, Vec3d to, Vec3d center, int plane, double radius) {
        Vec3d n = normal(plane), travel = to.subtract(from);
        double divisor = travel.dotProduct(n);
        if (Math.abs(divisor) < 1e-8) return null;
        double fraction = center.subtract(from).dotProduct(n) / divisor;
        if (fraction < 0 || fraction > 1) return null;
        Vec3d point = from.add(travel.scale(fraction));
        return point.squareDistanceTo(center) <= radius * radius ? point : null;
    }

    private WulfrumSawMechanics() {}
}
