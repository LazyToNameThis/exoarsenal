package com.exoarsenal.entity;

public final class BrawlerLimiterScore {
    public enum Attack {
        SUPERSONIC_INTERCEPT(152),
        HEX_AFTERBURNER(156),
        FIVE_ENGINE_VECTORING(132),
        TESLA_METEOR(166),
        SHIELD_SHRAPNEL(152),
        JETWASH(148),
        ORBITAL_DECAY(156),
        THRUSTER_FEINT(110),
        TESLA_SLINGSHOT(166),
        TERMINAL_CHASE(180),
        HEX_SINGULARITY(174),
        CONTROLLED_CRASH(144),
        PHANTOM_BRAWLER(148),
        REACTOR_RAM(180),
        FIVE_POINT_IMPACT(250);
        public final int duration;

        Attack(int ticks) {
            duration = ticks;
        }
    }

    public static Attack decode(int i) {
        return Attack.values()[Math.floorMod(i, Attack.values().length)];
    }

    public static int[] parries(Attack a) {
        switch (a) {
            case SUPERSONIC_INTERCEPT:
                return new int[] {128};
            case FIVE_ENGINE_VECTORING:
                return new int[] {112};
            case TESLA_METEOR:
                return new int[] {142};
            case THRUSTER_FEINT:
                return new int[] {88};
            case TESLA_SLINGSHOT:
                return new int[] {142};
            case TERMINAL_CHASE:
                return new int[] {154};
            case CONTROLLED_CRASH:
                return new int[] {122};
            case REACTOR_RAM:
                return new int[] {156};
            case FIVE_POINT_IMPACT:
                return new int[] {168, 186, 204, 226};
            default:
                return new int[0];
        }
    }

    public static double cue(Attack a, double t) {
        double v = 0;
        for (int beat : parries(a))
            if (t >= beat - 14 && t <= beat + 3)
                v = Math.max(v, BrawlerScore.smooth((t - beat + 14) / 14));
        return v;
    }

    public static int engines(Attack a, int t) {
        if (a == Attack.TERMINAL_CHASE) return (1 << Math.min(5, 1 + t / 28)) - 1;
        if (a == Attack.THRUSTER_FEINT)
            return t < 34 ? 1 : t < 66 ? 1 | (1 << (1 + (t / 12) % 4)) : 31;
        if (a == Attack.FIVE_POINT_IMPACT && t < 100) return (1 << Math.min(5, 1 + t / 20)) - 1;
        if (a == Attack.FIVE_ENGINE_VECTORING || a == Attack.CONTROLLED_CRASH)
            return t > 100 ? 31 : 1 | (1 << (1 + (t / 22) % 4));
        return 31;
    }

    private BrawlerLimiterScore() {}
}
