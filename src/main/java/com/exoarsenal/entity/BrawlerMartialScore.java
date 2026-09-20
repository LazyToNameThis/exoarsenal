package com.exoarsenal.entity;

public final class BrawlerMartialScore {
    public enum Attack {
        KICKBOXING(196),
        ROCKET_DROPKICK(166),
        PARRY_STRING(196),
        SUPLEX(204),
        STOMP(178),
        ROUNDHOUSE(170),
        GROUNDED_MISSILE(180),
        FOOTWORK(174),
        WALL_BOUNCE(184),
        CATCH_RETURN(150),
        PISTON_PILEDRIVER(216),
        FOUR_LIMB(196),
        GROUND_POUND(170),
        TESLA_GRID(232),
        MARTIAL_PROTOCOL(390);
        public final int duration;

        Attack(int duration) {
            this.duration = duration;
        }
    }

    public enum Motion {
        JAB,
        CROSS,
        LOW_KICK,
        HOOK,
        HEEL,
        KNEE,
        ELBOW,
        BACKFIST,
        PISTON,
        STOMP,
        DROPKICK,
        HEADBUTT,
        SLAM,
        TACKLE,
        UPPERCUT
    }

    public static final class Beat {
        public final int tick, limb;
        public final Motion motion;
        public final boolean parry;

        Beat(int tick, int limb, Motion motion, boolean parry) {
            this.tick = tick;
            this.limb = limb;
            this.motion = motion;
            this.parry = parry;
        }
    }

    private static Beat b(int t, int limb, Motion m, boolean p) {
        return new Beat(t, limb, m, p);
    }

    private static final Beat[][] BEATS = {
        {
            b(28, 0, Motion.JAB, true),
            b(46, 1, Motion.CROSS, true),
            b(65, 2, Motion.LOW_KICK, false),
            b(85, 0, Motion.HOOK, true),
            b(110, 3, Motion.HEEL, false),
            b(132, 1, Motion.JAB, true),
            b(174, 2, Motion.KNEE, false)
        },
        {b(72, 2, Motion.DROPKICK, true), b(140, 1, Motion.HOOK, true)},
        {
            b(24, 0, Motion.JAB, true),
            b(43, 1, Motion.CROSS, true),
            b(63, 2, Motion.LOW_KICK, false),
            b(83, 0, Motion.ELBOW, true),
            b(102, 3, Motion.KNEE, false),
            b(125, 1, Motion.BACKFIST, true),
            b(164, 5, Motion.PISTON, true)
        },
        {b(182, 0, Motion.SLAM, true)},
        {
            b(40, 2, Motion.STOMP, false),
            b(78, 3, Motion.STOMP, false),
            b(128, 2, Motion.STOMP, false)
        },
        {
            b(38, 2, Motion.HEEL, false),
            b(72, 3, Motion.HEEL, false),
            b(94, 0, Motion.BACKFIST, true),
            b(110, 2, Motion.HEEL, false),
            b(126, 1, Motion.BACKFIST, true),
            b(142, 3, Motion.HEEL, false)
        },
        {
            b(48, 4, Motion.TACKLE, false),
            b(94, 4, Motion.TACKLE, false),
            b(145, 4, Motion.TACKLE, true)
        },
        {
            b(30, 0, Motion.JAB, true),
            b(61, 1, Motion.CROSS, true),
            b(93, 0, Motion.JAB, true),
            b(122, 1, Motion.HOOK, true),
            b(152, 0, Motion.ELBOW, true)
        },
        {b(150, 4, Motion.TACKLE, true)},
        {b(116, 1, Motion.CROSS, true)},
        {b(171, 5, Motion.SLAM, true)},
        {
            b(24, 0, Motion.JAB, true),
            b(43, 3, Motion.LOW_KICK, false),
            b(63, 1, Motion.CROSS, true),
            b(82, 2, Motion.KNEE, false),
            b(102, 0, Motion.ELBOW, true),
            b(123, 3, Motion.HEEL, false),
            b(143, 4, Motion.HEADBUTT, false),
            b(169, 5, Motion.SLAM, true)
        },
        {
            b(28, 2, Motion.LOW_KICK, false),
            b(67, 0, Motion.JAB, true),
            b(83, 1, Motion.CROSS, true),
            b(99, 0, Motion.JAB, true),
            b(115, 1, Motion.CROSS, true),
            b(145, 5, Motion.SLAM, true)
        },
        {
            b(24, 0, Motion.JAB, true),
            b(50, 2, Motion.LOW_KICK, false),
            b(77, 1, Motion.CROSS, true),
            b(104, 3, Motion.KNEE, false),
            b(136, 3, Motion.KNEE, false),
            b(161, 1, Motion.CROSS, true),
            b(187, 2, Motion.LOW_KICK, false),
            b(214, 0, Motion.JAB, true)
        },
        {
            b(28, 0, Motion.JAB, true),
            b(47, 1, Motion.CROSS, true),
            b(65, 2, Motion.LOW_KICK, false),
            b(84, 0, Motion.HOOK, true),
            b(106, 3, Motion.KNEE, false),
            b(128, 1, Motion.ELBOW, true),
            b(149, 2, Motion.LOW_KICK, false),
            b(172, 0, Motion.UPPERCUT, true),
            b(222, 3, Motion.HEEL, false),
            b(239, 1, Motion.BACKFIST, true),
            b(256, 0, Motion.CROSS, true),
            b(278, 4, Motion.TACKLE, true),
            b(316, 2, Motion.DROPKICK, true),
            b(366, 1, Motion.HOOK, true)
        }
    };

    public static Attack decode(int id) {
        return Attack.values()[Math.floorMod(id, Attack.values().length)];
    }

    public static Beat[] beats(Attack a) {
        return BEATS[a.ordinal()];
    }

    public static double pulse(double t, int at) {
        return BrawlerScore.smooth((t - at + 12) / 12) * (1 - BrawlerScore.smooth((t - at) / 13));
    }

    public static double cue(Attack a, int limb, double t) {
        double q = 0;
        for (Beat b : beats(a))
            if (b.parry && (b.limb == limb || b.limb == 5 && limb < 2) && t <= b.tick)
                q = Math.max(q, BrawlerScore.smooth((t - b.tick + 16) / 16));
        return q;
    }

    public static boolean finalPunish(Attack a, int contact, int parries) {
        return a == Attack.MARTIAL_PROTOCOL && contact == 366
                || a == Attack.PARRY_STRING && contact == 164 && parries >= 5;
    }

    private BrawlerMartialScore() {}
}
