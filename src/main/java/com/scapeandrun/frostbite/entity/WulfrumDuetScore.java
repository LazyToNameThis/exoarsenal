package com.scapeandrun.frostbite.entity;

public final class WulfrumDuetScore {
    public static final int FENCER = 16,
            RAILSHOT = 17,
            LOOM = 18,
            RELAY = 19,
            CROSS_EYED = 20,
            PINCER = 21,
            FEEDBACK = 22;
    public static final int EXECUTION = 23,
            SANDER = 24,
            SUPPRESS = 25,
            WOODCHIPPER = 26,
            RICOCHET_GUN = 27,
            DRIVE = 28,
            CUT_BEAM = 29,
            SAWSTORM = 30,
            OVERCLOCK = 31,
            FAILURE = 32,
            SEER_FINAL = 33,
            OBSERVER_FINAL = 34;
    private static final int[] LENGTH = {
        280, 260, 260, 320, 260, 216, 260, 260, 220, 240, 260, 240, 260, 240, 260, 260, 280, 240,
        240
    };
    private static final int[] FIRST = {
        0, FENCER, 1, RAILSHOT, LOOM, 2, RELAY, CROSS_EYED, 3, PINCER, 4, 5
    };
    private static final int[] EXPERT = {
        9, FENCER, 10, RAILSHOT, LOOM, 11, RELAY, CROSS_EYED, 12, PINCER, FEEDBACK
    };

    public static int duration(int id) {
        return LENGTH[Math.max(0, Math.min(LENGTH.length - 1, id - 16))];
    }

    public static int select(int cursor, boolean expert, boolean second) {
        return second
                ? 23 + Math.floorMod(cursor, 10)
                : (expert ? EXPERT : FIRST)
                        [Math.floorMod(cursor, expert ? EXPERT.length : FIRST.length)];
    }

    public static boolean detached(int id, int t) {
        return id == RELAY && (t >= 35 && t < 235 && (t - 35) % 40 < 28 || t >= 235 && t < 295)
                || id == DRIVE && t >= 208
                || id == FAILURE && t >= 190 && t < 235;
    }

    public static float bladeTilt(int id, float t) {
        if (id == FENCER) return .65F * (float) Math.sin(t * .12);
        if (id == CROSS_EYED) return .8F;
        if (id == FEEDBACK) return .65F;
        if (id == RICOCHET_GUN) return t < 180 ? .4F + (float) Math.sin(t * .12) * .35F : 1.5F;
        return 0;
    }

    public static float spin(int id, float t) {
        return id == RELAY || id == OVERCLOCK || id == WOODCHIPPER
                ? t * .23F
                : id == FAILURE ? t * .35F : 0;
    }

    public static double[] appendage(float age, int i, boolean seer) {
        double a = i * Math.PI / 3 + age * .014;
        return new double[] {
            Math.cos(a) * (seer ? 3.8 : 2.3),
            Math.sin(a) * (seer ? 3.2 : 2.3),
            seer ? Math.sin(age * .06 + i) * 1.3 : 1.2
        };
    }

    private WulfrumDuetScore() {}
}
