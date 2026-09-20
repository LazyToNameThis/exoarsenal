package com.exoarsenal.entity;

public final class ExcavatorProbeScore {
    public static int blades(boolean expert) {
        return expert ? 88 : 34;
    }

    public static int end(boolean expert) {
        return blades(expert) + (expert ? 174 : 86);
    }

    public static boolean shot(boolean expert, int t) {
        return expert
                ? (t == 20 || t == 26 || t == 42 || t == 48 || t == 54 || t == 70 || t == 76)
                : t == 20;
    }

    public static float extension(boolean expert, float t) {
        float b = blades(expert);
        return Math.max(0, Math.min(1, Math.min((t - b) / 10, (end(expert) - t - 8) / 10)));
    }

    public static boolean thrown(boolean expert, int t) {
        int q = t - blades(expert);
        return expert && q >= 90 && q < 155;
    }

    public static int socketSegment(int index) {
        return 2 + index / 2;
    }

    public static int socketSide(int index) {
        return index % 2 == 0 ? -1 : 1;
    }

    private ExcavatorProbeScore() {}
}
