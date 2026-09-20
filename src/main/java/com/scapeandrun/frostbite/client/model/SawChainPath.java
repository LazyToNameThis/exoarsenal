package com.scapeandrun.frostbite.client.model;

public final class SawChainPath {
    public static final int LINKS = 12;
    public static final double RADIUS = 3.2;
    public static final double STRAIGHT = 21.0;
    public static final double LENGTH = 2 * STRAIGHT + 2 * Math.PI * RADIUS;

    private SawChainPath() {}

    public static double[] sample(double distance) {
        double d = ((distance % LENGTH) + LENGTH) % LENGTH;
        if (d < STRAIGHT) return new double[] {-RADIUS, 18 + d, 0};
        d -= STRAIGHT;
        if (d < Math.PI * RADIUS) {
            double a = Math.PI - d / RADIUS;
            return new double[] {RADIUS * Math.cos(a), 39 + RADIUS * Math.sin(a), a - Math.PI};
        }
        d -= Math.PI * RADIUS;
        if (d < STRAIGHT) return new double[] {RADIUS, 39 - d, -Math.PI};
        d -= STRAIGHT;
        double a = -d / RADIUS;
        return new double[] {RADIUS * Math.cos(a), 18 + RADIUS * Math.sin(a), a - Math.PI};
    }
}
