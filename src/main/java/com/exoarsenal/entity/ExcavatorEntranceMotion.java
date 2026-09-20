package com.exoarsenal.entity;

public final class ExcavatorEntranceMotion {
    public static double x(double tick) {
        return tick <= 120
                ? -60 + 42 * ExcavatorActuation.ease(tick / 120)
                : -18 + 6 * ExcavatorActuation.ease((tick - 120) / 30);
    }

    public static double y(double tick) {
        if (tick <= 120) {
            double arc = Math.sin(Math.max(0, tick) / 120 * Math.PI * 2);
            return -7 + 16 * arc * arc;
        }
        return -7 + 8 * ExcavatorActuation.ease((tick - 120) / 30);
    }

    public static double z(double tick) {
        return tick <= 120 ? 6 * Math.pow(Math.sin(Math.PI * Math.max(0, tick) / 120), 2) : 0;
    }

    private ExcavatorEntranceMotion() {}
}
