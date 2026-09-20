package com.scapeandrun.frostbite.entity;

public final class WulfrumBladePaths {
    public static double[] fencing(int blade, double progress) {
        double f = WulfrumSurvivorScore.ease(progress), side = blade % 2 == 0 ? -1 : 1;
        if (blade < 2) return new double[] {side * (7 - 14 * f), 4 - 5 * f, 2 - 4 * f};
        if (blade < 4) return new double[] {side * (8 - 10 * f), 1, 0};
        double angle = Math.PI * f;
        return new double[] {
            side * 7 * Math.cos(angle), -.5 + .35 * Math.sin(angle), 5 * Math.sin(angle) - 2
        };
    }

    public static double[] sever(int leg, double progress) {
        double f = Math.max(0, Math.min(1, progress)), s = 2 * f - 1;
        switch (leg) {
            case 0:
                return new double[] {16 * s, 1, 0};
            case 1:
                return new double[] {0, 15 * s, 0};
            case 2:
                return new double[] {14 * s, 11 * s, 0};
            case 3:
                return new double[] {-14 * s, 11 * s, 0};
            case 4:
                {
                    double a = f * Math.PI * 4, r = 14 - 10 * f;
                    return new double[] {Math.cos(a) * r, 1 + 6 * f, Math.sin(a) * r};
                }
            case 5:
                {
                    double a = f * Math.PI * 2;
                    return new double[] {Math.cos(a) * 12, 1, Math.sin(a) * 12};
                }
            default:
                return new double[] {0, 1, 18 - 36 * f};
        }
    }

    private WulfrumBladePaths() {}
}
