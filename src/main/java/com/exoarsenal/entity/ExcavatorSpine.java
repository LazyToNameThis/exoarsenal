package com.exoarsenal.entity;

import net.minecraft.util.math.Vec3d;

public final class ExcavatorSpine {
    public static final int COUNT = 18;
    public static final double SPACING = 1.65;
    private final Vec3d[] now = new Vec3d[COUNT], old = new Vec3d[COUNT];

    public void reset(Vec3d head) {
        for (int i = 0; i < COUNT; i++) old[i] = now[i] = head.addVector(0, 0, -i * SPACING);
    }

    public void update(Vec3d head) {
        if (now[0] == null) reset(head);
        System.arraycopy(now, 0, old, 0, COUNT);
        now[0] = head;
        for (int i = 1; i < COUNT; i++) {
            Vec3d d = now[i].subtract(now[i - 1]);
            if (d.lengthSquared() < .0001) d = new Vec3d(0, 0, -1);
            now[i] = now[i - 1].add(d.normalize().scale(SPACING));
        }
    }

    public void coil(Vec3d head, Vec3d forward, double weight) {
        update(head);
        Vec3d f = forward.normalize(), side = f.crossProduct(new Vec3d(0, 1, 0));
        if (side.lengthSquared() < .01) side = new Vec3d(1, 0, 0);
        side = side.normalize();
        Vec3d up = side.crossProduct(f).normalize();
        double blend = Math.max(0, Math.min(1, weight)) * .35;
        for (int i = 1; i < COUNT; i++) {
            double a = i * .44;
            Vec3d goal =
                    head.subtract(f.scale(i * .38))
                            .add(side.scale(Math.sin(a) * 3.8))
                            .add(up.scale((1 - Math.cos(a)) * 3.8));
            Vec3d wanted = now[i].add(goal.subtract(now[i]).scale(blend));
            Vec3d delta = wanted.subtract(now[i - 1]);
            if (delta.lengthSquared() < .0001) delta = f.scale(-1);
            now[i] = now[i - 1].add(delta.normalize().scale(SPACING));
        }
    }

    public void impact(Vec3d head, Vec3d forward, double age) {
        if (now[0] == null) reset(head);
        System.arraycopy(now, 0, old, 0, COUNT);

        Vec3d translation = head.subtract(now[0]);
        for (int i = 0; i < COUNT; i++) now[i] = now[i].add(translation);
        now[0] = head;
        Vec3d f = forward.normalize(), side = f.crossProduct(new Vec3d(0, 1, 0));
        if (side.lengthSquared() < .01) side = new Vec3d(1, 0, 0);
        side = side.normalize();
        Vec3d up = side.crossProduct(f).normalize();

        for (int i = 1; i < COUNT; i++) {
            double phase = age - i * 1.15,
                    envelope = phase < 0 || phase > 28 ? 0 : Math.sin(Math.PI * phase / 28);
            double bend = Math.sin(phase * Math.PI / 14) * envelope * .68 * Math.exp(-i * .025);
            Vec3d tangent =
                    f.scale(-Math.cos(bend))
                            .add(up.scale(Math.sin(bend)))
                            .add(side.scale(envelope * Math.sin(phase * .22) * .10));
            Vec3d previous = now[i].subtract(now[i - 1]);
            if (previous.lengthSquared() < .0001) previous = f.scale(-1);
            Vec3d delta = previous.normalize().scale(.62).add(tangent.normalize().scale(.38));
            now[i] = now[i - 1].add(delta.normalize().scale(SPACING));
        }
    }

    public Vec3d point(int index, float partial) {
        if (now[0] == null) return Vec3d.ZERO;
        return old[index].add(now[index].subtract(old[index]).scale(partial));
    }

    public void eclipse(Vec3d head, double gapAngle) {
        if (now[0] == null) reset(head);
        System.arraycopy(now, 0, old, 0, COUNT);
        double radius = WulfrumCoordinationGeometry.eclipseRadius(), start = gapAngle + Math.PI / 4;
        Vec3d center = head.addVector(-Math.cos(start) * radius, 0, -Math.sin(start) * radius);
        for (int i = 0; i < COUNT; i++) {
            double a = start + i * Math.PI * 1.5 / (COUNT - 1);
            now[i] = center.addVector(Math.cos(a) * radius, 0, Math.sin(a) * radius);
        }
    }

    public void rigid(Vec3d head, Vec3d forward) {
        if (now[0] == null) reset(head);
        System.arraycopy(now, 0, old, 0, COUNT);
        for (int i = 0; i < COUNT; i++) now[i] = head.subtract(forward.scale(i * SPACING));
    }
}
