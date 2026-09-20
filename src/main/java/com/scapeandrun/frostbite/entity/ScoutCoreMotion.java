package com.scapeandrun.frostbite.entity;

public final class ScoutCoreMotion {
    public static final int CHARGE = 60, END = 180;

    private ScoutCoreMotion() {}

    public static float charge(float age) {
        float t = Math.max(0, Math.min(1, age / CHARGE));
        return t * t * (3 - 2 * t);
    }

    public static float power(float age) {
        return Math.max(0, Math.min(1, (age - CHARGE) / 50));
    }

    public static double orbit(float age) {
        return 1.65 - 1.12 * charge(age);
    }

    public static double angle(int orb, float age) {
        return orb * Math.PI / 3 + age * (.035 + .035 * charge(age));
    }

    public static double beamRadius(float age) {
        return age < CHARGE ? .025 + .13 * charge(age) : .2 + .85 * power(age);
    }
}
