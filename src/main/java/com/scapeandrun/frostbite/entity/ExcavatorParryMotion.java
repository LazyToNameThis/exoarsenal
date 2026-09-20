package com.scapeandrun.frostbite.entity;

public final class ExcavatorParryMotion {
    public static final int HITSTOP = 30, LANDING = 16, RECOIL_END = 36;

    public static double smooth(double t) {
        t = Math.max(0, Math.min(1, t));
        return t * t * (3 - 2 * t);
    }

    public static double distance(double tick, boolean finale) {
        double t = Math.max(0, Math.min(1, tick / RECOIL_END));
        return (finale ? 64 : 32) * (2 * t - t * t);
    }

    public static double rise(double tick) {
        return tick <= 0 || tick >= LANDING ? 0 : 5.5 * Math.sin(Math.PI * tick / LANDING);
    }

    public static float rearPitch(double tick) {
        return (float) (-65 * Math.sin(Math.PI * Math.max(0, Math.min(1, tick / LANDING))));
    }

    public static float brace(double age) {
        return age < 0 || age >= HITSTOP + 18
                ? 0
                : (float)
                        (age < 3
                                ? smooth(age / 3)
                                : age < HITSTOP ? 1 : 1 - smooth((age - HITSTOP) / 18));
    }

    public static float compression(double age) {
        return (float) (Math.sin(Math.PI * Math.max(0, Math.min(1, age / HITSTOP))) * .5);
    }

    public static double drillExtension(boolean deep, int stage, double tick) {
        double extension = deep ? 5 : 0;
        if (stage == 1) extension = -3 * Math.min(1, tick / 35);
        if (stage == 11)
            extension =
                    tick < 110
                            ? -2 + Math.sin(tick * .6) * (tick % 30 < 5 ? 1 : 0)
                            : 5 * Math.min(1, (tick - 110) / 8);
        if (stage == 3) extension -= 4 * Math.sin(Math.min(1, tick / 12) * Math.PI);
        return extension;
    }

    private ExcavatorParryMotion() {}
}
