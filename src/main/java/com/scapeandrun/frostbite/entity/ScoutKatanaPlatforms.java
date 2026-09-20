package com.scapeandrun.frostbite.entity;

public final class ScoutKatanaPlatforms {
    public static final int COUNT = 9, BOARD = 216, DISMOUNT = 372;

    private ScoutKatanaPlatforms() {}

    public static int toss(int slot) {
        return 140 + slot * 8;
    }

    public static int strike(int slot) {
        return 248 + slot * 12;
    }

    public static double rise(double age) {
        double u = Math.max(0, Math.min(1, age / 24));
        return u * u * (3 - 2 * u);
    }
}
