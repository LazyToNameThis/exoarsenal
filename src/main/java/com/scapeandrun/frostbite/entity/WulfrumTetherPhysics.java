package com.scapeandrun.frostbite.entity;

import net.minecraft.util.math.Vec3d;

public final class WulfrumTetherPhysics {

    public static Vec3d pull(
            Vec3d position, Vec3d velocity, Vec3d[] anchors, Vec3d destination, double speed) {
        Vec3d wanted = destination.subtract(position).normalize(), force = Vec3d.ZERO;
        for (Vec3d anchor : anchors) {
            Vec3d direction = anchor.subtract(position).normalize();
            double alignment = direction.dotProduct(wanted);
            if (alignment > 0) force = force.add(direction.scale(alignment * .65));
        }
        return limit(velocity.scale(.88).add(force), speed);
    }

    private Vec3d first, second, oldFirst, oldSecond;
    private final double a, b;

    public WulfrumTetherPhysics(Vec3d anchor, double a, double b) {
        this.a = a;
        this.b = b;
        first = anchor.addVector(a, 0, 0);
        second = first.addVector(0, -b, 0);
        oldFirst = first;
        oldSecond = second;
    }

    public WulfrumTetherPhysics(Vec3d first, Vec3d second, double cable) {
        this.a = 0;
        this.b = cable;
        this.first = first;
        this.second = second;
        oldFirst = first;
        oldSecond = second;
    }

    public void stepAnchors(Vec3d[] anchors, double[] lengths, Vec3d drive) {
        Vec3d f = first, s = second;
        first =
                first.add(limit(first.subtract(oldFirst).scale(.98), 4))
                        .add(drive)
                        .addVector(0, -.03, 0);
        second =
                second.add(limit(second.subtract(oldSecond).scale(.985), 5)).addVector(0, -.055, 0);
        oldFirst = f;
        oldSecond = s;
        for (int pass = 0; pass < 18; pass++) {
            for (int i = 0; i < anchors.length; i++) {
                Vec3d d = first.subtract(anchors[i]);
                if (d.lengthVector() > lengths[i])
                    first = anchors[i].add(d.normalize().scale(lengths[i]));
            }
            Vec3d d = second.subtract(first);
            if (d.lengthSquared() > .00001) second = first.add(d.normalize().scale(b));
        }
    }

    public void step(Vec3d anchor, Vec3d drive) {
        Vec3d f = first, s = second;
        first =
                first.add(limit(first.subtract(oldFirst).scale(.985), 4))
                        .add(drive)
                        .addVector(0, -.045, 0);
        second =
                second.add(limit(second.subtract(oldSecond).scale(.987), 5))
                        .add(drive.scale(.6))
                        .addVector(0, -.055, 0);
        oldFirst = f;
        oldSecond = s;
        for (int i = 0; i < 12; i++) {
            first = anchor.add(first.subtract(anchor).normalize().scale(a));
            Vec3d d = second.subtract(first);
            double length = d.lengthVector();
            if (length > .0001) {
                Vec3d error = d.scale((length - b) / length * .5);
                first = first.add(error);
                second = second.subtract(error);
            }
        }
        first = anchor.add(first.subtract(anchor).normalize().scale(a));
        second = first.add(second.subtract(first).normalize().scale(b));
    }

    private static Vec3d limit(Vec3d v, double max) {
        return v.lengthVector() > max ? v.normalize().scale(max) : v;
    }

    public Vec3d first() {
        return first;
    }

    public Vec3d second() {
        return second;
    }

    public Vec3d firstVelocity() {
        return first.subtract(oldFirst);
    }

    public Vec3d secondVelocity() {
        return second.subtract(oldSecond);
    }
}
