package com.scapeandrun.frostbite.entity;

public final class SeerObserverIntro {
    public static final int END = 290;
    public static final String TITLE = "Primal Fabrications", NAME = "The Wulfrum Mechanical Trio";

    private SeerObserverIntro() {}

    public static double smooth(double t) {
        t = Math.max(0, Math.min(1, t));
        return t * t * (3 - 2 * t);
    }

    public static double[] offset(float tick, boolean seer) {
        if (tick >= 124) {
            double side = seer ? -1 : 1, t = tick - 124;
            if (t < 64) {
                double f = smooth(t / 64), a = f * Math.PI * 4;
                return new double[] {
                    side * (5 + 3 * Math.sin(a)), 3 + Math.sin(a) * 2, side * Math.sin(a * .5) * 5
                };
            }
            if (t < 124 && seer) {
                double f = (t - 64) / 12;
                int n = Math.min(4, (int) f);
                double u = smooth(f - n), a = n * 4 * Math.PI / 5, b = (n + 1) * 4 * Math.PI / 5;
                return new double[] {
                    -5 + 8 * ((1 - u) * Math.cos(a) + u * Math.cos(b) - 1),
                    3 + 6 * ((1 - u) * Math.sin(a) + u * Math.sin(b)),
                    5 * Math.sin(f * Math.PI * .8)
                };
            }
            if (t < 124) return new double[] {5, 3, 0};
            return new double[] {side * 5, 3, 0};
        }
        double descent = smooth((tick - 12) / 82),
                settle = smooth((tick - 94) / 30),
                side = seer ? -1 : 1;
        double height = 3 + 68 * (1 - descent) + Math.sin(Math.PI * settle) * 1.2;
        return new double[] {side * (5 + 9 * (1 - descent)), height, 4 * (1 - descent)};
    }

    public static float titleAlpha(float tick) {
        return (float)
                Math.max(0, Math.min(smooth((tick - 248) / 12), 1 - smooth((tick - 275) / 15)));
    }

    public static float weapon(float tick) {
        return (float) smooth((tick - 100) / 28);
    }
}
