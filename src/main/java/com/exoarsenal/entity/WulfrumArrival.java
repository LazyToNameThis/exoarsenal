package com.exoarsenal.entity;

public final class WulfrumArrival {
    public static final int END = 100;

    public static double height(double age) {
        double u = Math.max(0, Math.min(1, (age - 14) / 66));
        return 48 * (1 - u * u * (3 - 2 * u));
    }

    public static double beamBottom(double age) {
        return 64 * (1 - Math.max(0, Math.min(1, age / 14)));
    }

    public static float strength(double age) {
        return (float) Math.max(0, Math.min(1, age / 8) * Math.min(1, (END - age) / 20));
    }

    public static float title(double age) {
        return (float) Math.max(0, Math.min(1, (age - 72) / 16) * Math.min(1, (172 - age) / 24));
    }
}
