package com.exoarsenal.entity;

public final class WulfrumSurvivorScore {
    public static final int SEER = 35, OBSERVER = 45;
    public static final int[] STAR = {0, 2, 4, 1, 3, 5}, MIRRORS = {0, 3, 1, 5, 2, 4};

    public static int select(boolean seer, int cursor) {
        int p = Math.floorMod(cursor, 19);
        return (seer ? SEER : OBSERVER) + (p == 18 ? 9 : p % 9);
    }

    public static int duration(int id) {
        return (id - SEER) % 10 == 9 ? 360 : 280;
    }

    public static boolean parrying(int id, int tick) {
        return id == SEER + 6 && tick >= 20 && tick < 100;
    }

    public static boolean saw(int id) {
        return id == SEER + 1 || id == SEER + 5 || id == SEER + 7;
    }

    public static double ease(double f) {
        f = Math.max(0, Math.min(1, f));
        return f * f * (3 - 2 * f);
    }

    public static double[] focus(float tick) {
        return new double[] {Math.sin((tick - 40) * .009) * 6, 0, 6};
    }

    private WulfrumSurvivorScore() {}
}
