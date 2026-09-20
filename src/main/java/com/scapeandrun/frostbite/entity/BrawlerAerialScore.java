package com.scapeandrun.frostbite.entity;

public final class BrawlerAerialScore {
    public enum Attack {
        FOURFOLD(142, 15),
        BALLISTIC_CLEAVER(144, 15),
        JETSTREAM_BOXING(128, 7),
        ORBITAL_HARPOONS(156, 15),
        AIR_SUPERIORITY(136, 11),
        TESLA_CAGE(132, 0),
        SKYHOOK(116, 5),
        AERIAL_FOUNDRY(150, 15),
        MACH_CLEAVER(140, 3),
        VERTICAL_RAILGUN(138, 15),
        DOGFIGHT(142, 15),
        COUNTER_THRUST(132, 7),
        FALLING_ARSENAL(130, 15),
        ARM_SACRIFICE(96, 0),
        PRIMITIVE_AIR_SUPERIORITY(228, 15),
        SURVIVING_WEAPONS(76, 0);
        public final int duration, arms;

        Attack(int duration, int arms) {
            this.duration = duration;
            this.arms = arms;
        }
    }

    public static Attack decode(int index) {
        return Attack.values()[Math.max(0, Math.min(Attack.values().length - 1, index))];
    }

    public static Attack next(int current, int live, boolean critical) {
        for (int n = 1; n <= 15; n++) {
            Attack a = Attack.values()[Math.floorMod(current + n, 15)];
            if (a == Attack.ARM_SACRIFICE && !critical) continue;
            if ((a.arms & live) == a.arms) return a;
        }
        return Attack.SURVIVING_WEAPONS;
    }

    public static int[][] strikes(Attack a) {
        switch (a) {
            case FOURFOLD:
                return new int[][] {{48, 1}, {122, 0}};
            case BALLISTIC_CLEAVER:
                return new int[][] {{38, 0}, {122, 1}};
            case JETSTREAM_BOXING:
                return new int[][] {{24, 0}, {42, 1}, {72, 0}, {94, 1}, {112, 0}};
            case ORBITAL_HARPOONS:
                return new int[][] {{48, 1}, {66, 0}, {84, 1}, {138, 0}};
            case AIR_SUPERIORITY:
                return new int[][] {{64, 1}, {114, 0}};
            case TESLA_CAGE:
                return new int[][] {{42, 0}, {68, 0}, {94, 0}, {116, 0}};
            case SKYHOOK:
                return new int[][] {{84, 0}};
            case AERIAL_FOUNDRY:
                return new int[][] {{44, 1}, {62, 0}, {82, 2}, {102, 3}};
            case MACH_CLEAVER:
                return new int[][] {{34, 1}, {66, 1}, {98, 1}, {124, 0}};
            case VERTICAL_RAILGUN:
                return new int[][] {{44, 1}, {50, 0}, {116, 0}};
            case DOGFIGHT:
                return new int[][] {{38, 1}, {62, 0}, {88, 0}, {122, 1}};
            case COUNTER_THRUST:
                return new int[][] {{28, 0}, {62, 1}, {108, 0}};
            case FALLING_ARSENAL:
                return new int[][] {{38, 0}, {52, 0}, {66, 0}, {80, 0}};
            case PRIMITIVE_AIR_SUPERIORITY:
                return new int[][] {
                    {42, 1}, {54, 0}, {90, 1}, {112, 0}, {136, 0}, {160, 1}, {202, 0}
                };
            case SURVIVING_WEAPONS:
                return new int[][] {{24, 0}, {42, 1}};
            default:
                return new int[0][0];
        }
    }

    public static double cue(Attack a, double t, int arm) {
        double value = 0;
        for (int[] beat : strikes(a))
            if (beat[1] == arm
                    && (arm == 0 || a == Attack.COUNTER_THRUST)
                    && t >= beat[0] - 12
                    && t <= beat[0])
                value = Math.max(value, BrawlerScore.smooth((t - beat[0] + 12) / 12));
        return value;
    }

    public static double[] hand(Attack a, int arm, double t) {
        double side = arm < 2 ? -1 : 1;
        double x = side * 3.7, y = arm == 0 || arm == 2 ? 1.9 : -1.8, z = .4;
        double fold = a == Attack.JETSTREAM_BOXING ? .45 : 0;
        x *= 1 - fold;
        for (int[] beat : strikes(a))
            if (beat[1] == arm) {
                double pulse = BrawlerScore.pulse(t, beat[0]),
                        wind = BrawlerScore.pulse(t, beat[0] - 9) * (1 - pulse);
                x -= side * 2.7 * pulse;
                y -= .8 * pulse;
                z += 4 * pulse - 1.5 * wind;
            }
        if (a == Attack.FALLING_ARSENAL && arm == 1) {
            double angle = t * .18;
            x = Math.cos(angle) * 4.8;
            y = Math.sin(angle) * 4.8;
            z = 1;
        }
        return new double[] {x, y, z};
    }

    public static double roll(Attack a, double t) {
        switch (a) {
            case FOURFOLD:
                return 48
                        * Math.sin(Math.min(1, t / 35) * Math.PI)
                        * (1 - BrawlerScore.smooth((t - 35) / 15));
            case JETSTREAM_BOXING:
                return 360 * BrawlerScore.smooth((t - 76) / 18);
            case AIR_SUPERIORITY:
                return 180
                        * BrawlerScore.smooth((t - 82) / 18)
                        * (1 - BrawlerScore.smooth((t - 114) / 15));
            case MACH_CLEAVER:
                return 180
                        * BrawlerScore.smooth((t - 42) / 16)
                        * (1 - BrawlerScore.smooth((t - 100) / 20));
            case DOGFIGHT:
                return 32 * Math.sin(t * .08);
            case PRIMITIVE_AIR_SUPERIORITY:
                return 180
                                * BrawlerScore.smooth((t - 76) / 14)
                                * (1 - BrawlerScore.smooth((t - 100) / 12))
                        + 360 * BrawlerScore.smooth((t - 140) / 18);
            default:
                return 12 * Math.sin(t * .065);
        }
    }

    public static boolean cleaverAttached(Attack a, double t) {
        return !(a == Attack.BALLISTIC_CLEAVER && t >= 24 && t < 112
                || a == Attack.ARM_SACRIFICE && t >= 56);
    }

    public static double thrust(Attack a, double t) {
        if (a == Attack.SKYHOOK && t >= 44 && t < 66
                || a == Attack.FALLING_ARSENAL && t >= 28 && t < 92
                || a == Attack.PRIMITIVE_AIR_SUPERIORITY && t >= 170 && t < 190) return 0;
        return .65 + .35 * Math.sin(t * .12) * Math.sin(t * .12);
    }

    private BrawlerAerialScore() {}
}
