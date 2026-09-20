package com.scapeandrun.frostbite.entity;

import net.minecraft.util.math.Vec3d;

final class WulfrumRecordedShot {
    final Vec3d from, to;
    final int warning, life;
    final float damage;
    final int network;
    final float radius, phase;
    final boolean winding;

    WulfrumRecordedShot(Vec3d from, Vec3d to, int warning, int life, float damage) {
        this(from, to, warning, life, damage, -1);
    }

    WulfrumRecordedShot(Vec3d from, Vec3d to, int warning, int life, float damage, int network) {
        this(from, to, warning, life, damage, network, .12F, false, 0);
    }

    WulfrumRecordedShot(
            Vec3d from,
            Vec3d to,
            int warning,
            int life,
            float damage,
            int network,
            float radius,
            boolean winding,
            float phase) {
        this.from = from;
        this.to = to;
        this.warning = warning;
        this.life = life;
        this.damage = damage;
        this.network = network;
        this.radius = radius;
        this.winding = winding;
        this.phase = phase;
    }
}
