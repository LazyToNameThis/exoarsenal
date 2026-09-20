package com.scapeandrun.frostbite.entity;

public final class ScoutArenaLayout {
    public static final double HALF = 12.5, HEIGHT = 48, LIFT = 18;
    public static final int RADIUS = 8;
    public static final double DECK_MARGIN = 10.5;

    private ScoutArenaLayout() {}

    public static double x(int island) {
        return (island % 3 - 1) * 8;
    }

    public static double z(int island) {
        return (island / 3 - 1) * 8;
    }

    public static double tier(int island) {
        return island == 4 ? 0 : (island % 2 == 0 ? 1.25 : -.75);
    }

    public static boolean tile(int x, int z) {
        return Math.abs(x) <= RADIUS
                && Math.abs(z) <= RADIUS
                && Math.abs(x) + Math.abs(z) <= RADIUS * 2 - 2;
    }

    public static double rise(double ticks) {
        double t = Math.max(0, Math.min(1, ticks / 120));
        return LIFT * t * t * (3 - 2 * t);
    }

    public static double height(int island, double ticks) {
        return rise(ticks) * (1 + tier(island) / LIFT);
    }

    public static double expansion(double ticks) {
        double t = Math.max(0, Math.min(1, (ticks - 52) / 120));
        return t * t * (3 - 2 * t);
    }

    public static double riseAgeForHeight(double height) {
        double low = 0, high = 120, target = Math.max(0, Math.min(LIFT, height));
        for (int i = 0; i < 30; i++) {
            double mid = (low + high) * .5;
            if (rise(mid) < target) low = mid;
            else high = mid;
        }
        return (low + high) * .5;
    }

    public static boolean bossLanding(
            double feet, double top, double verticalSpeed, boolean flying) {
        return !flying && verticalSpeed <= 0 && feet >= top - .1;
    }

    public static int nearest(double x, double z) {
        int col = Math.max(0, Math.min(2, (int) Math.round(x / 8) + 1)),
                row = Math.max(0, Math.min(2, (int) Math.round(z / 8) + 1));
        return row * 3 + col;
    }
}
