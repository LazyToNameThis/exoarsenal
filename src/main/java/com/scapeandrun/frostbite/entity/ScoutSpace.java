package com.scapeandrun.frostbite.entity;

public final class ScoutSpace {
    private ScoutSpace() {}

    public static double launchY(double height, double ticks) {
        return height / ticks + .035 * (ticks - 1) / 2;
    }

    public static double[] offset(double yaw, double side, double up, double forward) {
        double a = Math.toRadians(yaw), c = Math.cos(a), s = Math.sin(a);
        return new double[] {side * c - forward * s, up, side * s + forward * c};
    }

    public static double[] boneTranslation(double yaw, double dx, double dy, double dz) {
        double a = Math.toRadians(yaw), c = Math.cos(a), s = Math.sin(a), units = 16 / 2.35;

        return new double[] {
            (dx * c + dz * s) * units, (dy - .01) * units, (dx * s - dz * c) * units
        };
    }
}
