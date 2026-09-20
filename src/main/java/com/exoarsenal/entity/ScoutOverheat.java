package com.exoarsenal.entity;

public final class ScoutOverheat {
    public static final int DURATION = 400;

    private ScoutOverheat() {}

    public static float remaining(int tick) {
        return Math.max(0, Math.min(1, (DURATION - tick) / (float) DURATION));
    }

    public static boolean dash(int tick) {
        int beat = tick % 80;
        return beat >= 12 && beat < 24 || beat >= 48 && beat < 60;
    }

    public static boolean glacier(int tick) {
        return tick > 0 && tick < DURATION && tick % 40 == 0;
    }

    public static boolean volley(int tick) {
        return tick > 0 && tick < DURATION && tick % 10 == 0;
    }
}
