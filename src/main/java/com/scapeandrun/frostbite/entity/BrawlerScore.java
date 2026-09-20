package com.scapeandrun.frostbite.entity;

public final class BrawlerScore {
    public enum Pattern {
        PISTON(1, 166, 3),
        WHEEL(1, 188, 7),
        CRUCIFIX(1, 176, 12),
        FACTORY(1, 190, 11),
        PARRY_CHECK(1, 104, 7),
        RECOIL(1, 154, 15),
        CHAIN_BOXING(1, 176, 7),
        GUILLOTINE(1, 190, 6),
        OVERDRIVE(1, 202, 15),
        BALLISTA(1, 182, 15),
        WAR_MACHINE(1, 246, 15),
        RELAY(1, 180, 15),
        NO_NEUTRAL(1, 166, 15),
        RAM(2, 180, 0),
        HEXFIELD(2, 220, 0),
        PHANTOMS(2, 164, 0),
        RAIL(2, 148, 0),
        MINES(2, 220, 0),
        HEADBUTT(2, 164, 0),
        TESLA_GRAPPLE(2, 194, 0),
        RECALL(2, 160, 0),
        COMPRESSION(2, 206, 0),
        MISSING_ARMS(2, 224, 0),
        BOXING(3, 154, 0),
        CATCH_HANDS(3, 174, 0),
        HAYMAKER(3, 142, 0),
        GRAPPLER(3, 184, 0),
        GROUND_POUND(3, 174, 0),
        KNUCKLEQUAKE(3, 166, 0),
        CLAP(3, 142, 0),
        LARIAT(3, 202, 0),
        TWO_MAN(3, 176, 0),
        THROW_WORLD(3, 160, 0),
        COUNTER(3, 150, 0),
        ONE_TWO(3, 150, 0),
        PRIMITIVE(3, 380, 0),
        FLAIL_SIEGE(1, 182, 8),
        REMAINING_WEAPON(1, 100, 0);
        public final int phase, duration, arms;

        Pattern(int p, int d, int a) {
            phase = p;
            duration = d;
            arms = a;
        }
    }

    public static Pattern next(Pattern current, int phase, int liveArms) {
        Pattern[] values = Pattern.values();
        for (int n = 1; n <= values.length; n++) {
            Pattern p = values[(current.ordinal() + n) % values.length];
            if (p.phase == phase && p != Pattern.REMAINING_WEAPON && (p.arms & liveArms) == p.arms)
                return p;
        }
        return Pattern.REMAINING_WEAPON;
    }

    public static Pattern decode(int id) {
        return Pattern.values()[Math.max(0, Math.min(Pattern.values().length - 1, id))];
    }

    public static double smooth(double v) {
        v = Math.max(0, Math.min(1, v));
        return v * v * (3 - 2 * v);
    }

    public static double pulse(double t, double contact) {
        return t < contact ? smooth((t - contact + 10) / 10) : 1 - smooth((t - contact) / 12);
    }

    public static double[] hand(Pattern p, int i, double t) {
        double side = p.phase == 3 ? (i % 2 == 0 ? -1 : 1) : (i < 2 ? -1 : 1),
                x = side * 4.1,
                y = p.phase == 3 ? 1.2 : (i == 0 || i == 2) ? 2.05 : -2.05,
                z = .5;
        int[] times;
        int[] arms;
        switch (p) {
            case PISTON:
                times = new int[] {28, 46, 66, 90, 138};
                arms = new int[] {0, 0, 1, 0, 0};
                break;
            case WHEEL:
                times = new int[] {28, 66, 94, 132};
                arms = new int[] {1, 1, 0, 2};
                break;
            case CRUCIFIX:
                times = new int[] {12, 30, 46, 62, 78, 94, 126};
                arms = new int[] {2, 3, 3, 3, 3, 3, 0};
                break;
            case FACTORY:
                times = new int[] {20, 30, 40, 50, 60, 70, 90, 140};
                arms = new int[] {3, 3, 3, 3, 3, 3, 1, 0};
                break;
            case RECOIL:
                times = new int[] {20, 34, 55, 69, 98, 121};
                arms = new int[] {3, 0, 3, 1, 2, -1};
                break;
            case CHAIN_BOXING:
                times = new int[] {10, 49, 85, 113, 142};
                arms = new int[] {2, 0, 1, 0, 0};
                break;
            case GUILLOTINE:
                times = new int[] {10, 40, 85, 130, 158};
                arms = new int[] {2, 1, 1, 1, 3};
                break;
            case OVERDRIVE:
                times = new int[] {24, 36, 48, 66, 78, 90, 108, 120, 132, 182};
                arms = new int[] {3, 1, 0, 3, 1, 0, 3, 1, 0, -1};
                break;
            case BALLISTA:
                times = new int[] {12, 50, 70, 96, 138};
                arms = new int[] {2, 3, 1, 0, 2};
                break;
            case WAR_MACHINE:
                times = new int[] {14, 55, 83, 111, 139, 167, 204};
                arms = new int[] {2, -1, -1, -1, -1, -1, 0};
                break;
            case RELAY:
                times = new int[] {20, 40, 54, 70, 90, 110, 142};
                arms = new int[] {1, 0, 2, 3, 3, 1, 0};
                break;
            case REMAINING_WEAPON:
                times = new int[] {30, 62};
                arms = new int[] {-1, -1};
                break;
            case PARRY_CHECK:
                times = new int[] {24, 42, 61, 80};
                arms = new int[] {0, 1, 2, 0};
                break;
            case NO_NEUTRAL:
                times = new int[] {24, 40, 56, 72, 88, 104, 120, 136};
                arms = new int[] {0, 1, 2, 3, 0, 2, 1, 3};
                break;
            case BOXING:
                times = new int[] {24, 40, 56, 72, 90, 124};
                arms = new int[] {0, 1, 0, 1, 1, -1};
                break;
            case CATCH_HANDS:
                times = new int[] {26, 44, 60, 74, 86, 96, 105, 114};
                arms = new int[] {0, 1, 0, 1, 0, 1, 0, 1};
                break;
            case HAYMAKER:
                times = new int[] {58, 82};
                arms = new int[] {0, 1};
                break;
            case ONE_TWO:
                times = new int[] {28, 48, 76, 92, 106, 118, 132};
                arms = new int[] {0, 1, 0, 1, 0, 1, -1};
                break;
            case TWO_MAN:
                times = new int[] {26, 46, 66, 86, 106, 126, 146};
                arms = new int[] {0, 1, 0, 1, 0, 1, -1};
                break;
            case THROW_WORLD:
                times = new int[] {94};
                arms = new int[] {0};
                break;
            case KNUCKLEQUAKE:
                times = new int[] {28, 50, 70, 86, 100, 112, 136};
                arms = new int[] {0, 1, 0, 1, 0, 1, -1};
                break;
            case PHANTOMS:
                times = new int[] {30, 64, 98, 132};
                arms = new int[] {0, 1, 2, 3};
                break;
            case MISSING_ARMS:
                times = new int[] {30, 66, 102, 138, 166, 180, 194};
                arms = new int[] {0, 1, 2, 3, 0, 1, -1};
                break;
            case PRIMITIVE:
                times = new int[] {24, 42, 60, 84, 104, 122, 146, 188, 208, 276, 340};
                arms = new int[] {0, 1, 0, -1, 0, 1, -1, 0, 1, 0, -1};
                break;
            default:
                times = new int[0];
                arms = new int[0];
        }
        for (int n = 0; n < times.length; n++)
            if (arms[n] == i || arms[n] < 0) {
                double w = pulse(t, times[n]);
                boolean hook = n % 3 == 2;
                x += (-side * 2.7 + (hook ? side * Math.sin(w * Math.PI) * 2 : 0)) * w;
                y += ((p == Pattern.KNUCKLEQUAKE ? -2.5 : n % 5 == 4 ? 2.1 : -.5)) * w;
                z += 4 * w;
                double wind = pulse(t, times[n] - 12) * (1 - w);
                z -= .8 * wind;
            }
        if (p == Pattern.CLAP) {
            double w = pulse(t, 46);
            x = side * (5 - 4.8 * w);
            z = 3 * w;
            y = 0;
        }
        if (p == Pattern.LARIAT) {
            double drive = smooth((t - 150) / 14);
            x = side * (5 - 4.3 * drive);
            y = .4;
            z = 3.8 * drive;
        }
        if (p == Pattern.COUNTER) {
            double guard = smooth(t / 14) * (1 - smooth((t - 80) / 20));
            x += (side * .75 - x) * guard;
            y += (.4 - y) * guard;
            z += (2 - z) * guard;
        }
        if (p == Pattern.GRAPPLER) {
            double close = smooth((t - 20) / 14) * (1 - smooth((t - 148) / 18));
            x += (side * .65 - x) * close;
            z += (2.8 - z) * close;
            y += (-.7 + 3.2 * smooth((t - 34) / 28) - 3.8 * smooth((t - 124) / 24) - y) * close;
            double swing = Math.sin(Math.max(0, Math.min(1, (t - 62) / 32)) * Math.PI * 2);
            x += swing * 2.3 * close;
            y -= 4 * (pulse(t, 64) + pulse(t, 90));
        }
        if (p == Pattern.GROUND_POUND) {
            double hold = smooth((t - 22) / 12) * (1 - smooth((t - 140) / 20));
            x += (side * 1.1 - x) * hold;
            z += (3 - z) * hold;
            y += (-1.8 - y) * hold;
            for (int beat : new int[] {52, 72, 92, 132})
                if (i == 0 || beat == 132) {
                    double wind = pulse(t, beat - 9) * (1 - pulse(t, beat)),
                            impact = pulse(t, beat);
                    y += 3.8 * wind - 1.0 * impact;
                    z -= .8 * wind;
                }
        }
        if (p == Pattern.BOXING && t > 98) {
            double wind = pulse(t, 112) * (1 - pulse(t, 124));
            y += 3.8 * wind - 1.6 * pulse(t, 124);
            x -= side * 1.5 * wind;
        }
        if (p == Pattern.HAYMAKER) {
            double beat = i == 0 ? 58 : 82, wind = pulse(t, beat - 12) * (1 - pulse(t, beat));
            x += side * 1.3 * wind;
            z -= 2.1 * wind;
            y += .7 * wind;
        }
        if (p == Pattern.CLAP && t > 78) {
            double upper = pulse(t, 102);
            if (i == 1) {
                x -= side * 3 * upper;
                y += 2.5 * upper;
                z += 4 * upper;
            }
        }
        if (p == Pattern.THROW_WORLD) {
            double w = smooth((t - 20) / 40), hold = smooth(t / 16) * (1 - smooth((t - 80) / 20));
            x += (side * 1.5 - x) * hold;
            y += (-2 + 5 * w - y) * hold;
            z += (2.5 - z) * hold;
        }
        if (p == Pattern.PRIMITIVE && t >= 216) {
            double lift = smooth((t - 216) / 12) * (1 - smooth((t - 252) / 12));
            x += (side * 1.5 - x) * lift;
            y += (-1 + 4 * smooth((t - 220) / 16) - y) * lift;
            z += (3 - z) * lift;
            double grip = smooth((t - 266) / 10) * (1 - smooth((t - 290) / 10));
            x += (side * .65 - x) * grip;
            z += (2.8 - z) * grip;
            y += (1.5 + 2 * smooth((t - 276) / 14) - y) * grip;
            double catchGrip = smooth((t - 302) / 8) * (1 - smooth((t - 350) / 14));
            x += (side * .75 - x) * catchGrip;
            z += (2.5 - z) * catchGrip;
            y += (2 - 4 * smooth((t - 316) / 20) - y) * catchGrip;
        }
        if (p == Pattern.FLAIL_SIEGE && i == 3) {
            double swing = smooth((t - 14) / 14) * (1 - smooth((t - 84) / 18));
            double angle = (t - 28) * .095;
            x += (Math.cos(angle) * 5.4 - x) * swing;
            y += (-.6 - y) * swing;
            z += (2 + Math.sin(angle) * 5.4 - z) * swing;
        }
        return new double[] {x, y, z};
    }

    public static double flail(Pattern p, double t) {
        return p == Pattern.FLAIL_SIEGE ? smooth(t / 18) * (1 - smooth((t - 96) / 22)) : 0;
    }

    public static float grip(Pattern p, int arm, double t) {
        if (p == Pattern.GRAPPLER) {
            double catchHand = smooth((t - 20) / 14) * (1 - smooth((t - 148) / 18));
            return (float) (.25 + .75 * catchHand);
        }
        if (p == Pattern.THROW_WORLD)
            return (float)
                    (.25
                            + .75 * smooth((t - 24) / 14) * (1 - smooth((t - 72) / 12))
                            + .75 * pulse(t, 94));
        if (p == Pattern.COUNTER) return .55F;
        double closed = .72;
        for (int[] beat : punchBeats(p))
            if ((beat[1] & (1 << arm)) != 0)
                closed = Math.max(closed, .72 + .28 * pulse(t, beat[0] - 3));
        double[] h = hand(p, arm, t);
        return (float) Math.min(1, Math.max(closed, .72 + Math.max(0, h[2] - .5) * .09));
    }

    public static double[] wrist(Pattern p, int arm, double time) {
        double[] before = hand(p, arm, Math.max(0, time - .5)), after = hand(p, arm, time + .5);
        double recovery = .3 + .7 * smooth((after[2] - before[2] + .04) / .08);
        double pitch = Math.max(-42, Math.min(42, -(after[1] - before[1]) * 55)) * recovery;
        double yaw = Math.max(-48, Math.min(48, (after[0] - before[0]) * 55)) * recovery;
        double roll = (arm % 2 == 0 ? -1 : 1) * Math.min(28, Math.abs(yaw) * .6);
        return new double[] {pitch, yaw, roll};
    }

    public static boolean weaponInHand(Pattern p, int arm, double t) {
        if (arm != 1) return true;
        switch (p) {
            case WHEEL:
                return t < 66 || t >= 160;
            case BALLISTA:
                return t < 50 || t >= 162;
            case RELAY:
                return t < 20 || t >= 138;
            case GUILLOTINE:
                return !(t >= 40 && t < 75 || t >= 85 && t < 120 || t >= 130 && t < 165);
            default:
                return true;
        }
    }

    public static int parryArm(Pattern p, double t) {
        switch (p) {
            case HAYMAKER:
                return t > 58 ? 1 : 0;
            case PARRY_CHECK:
                return t > 24 && t <= 42 ? 1 : 0;
            case ONE_TWO:
                int[] hits = {28, 48, 76, 92, 106, 118};
                for (int n = 0; n < hits.length; n++) if (t <= hits[n]) return n % 2;
                return 0;
            case CLAP:
            case COUNTER:
                return 1;
            case PRIMITIVE:
                return t > 188 ? 1 : 0;
            default:
                return 0;
        }
    }

    public static double parryCue(Pattern p, double t) {
        if (p.phase == 3) return Math.max(parryCue(p, t, 0), parryCue(p, t, 1));
        int[] contacts;
        switch (p) {
            case PISTON:
                contacts = new int[] {138};
                break;
            case PARRY_CHECK:
                contacts = new int[] {24, 42, 80};
                break;
            case HAYMAKER:
                contacts = new int[] {58, 82};
                break;
            case ONE_TWO:
                contacts = new int[] {28, 48, 76, 92, 106, 118};
                break;
            case BOXING:
                contacts = new int[] {124};
                break;
            case GROUND_POUND:
                contacts = new int[] {132};
                break;
            case CLAP:
                contacts = new int[] {102};
                break;
            case THROW_WORLD:
                contacts = new int[] {94};
                break;
            case RELAY:
                contacts = new int[] {142};
                break;
            case RAM:
                contacts = new int[] {140};
                break;
            case RAIL:
                contacts = new int[] {86};
                break;
            case CHAIN_BOXING:
                contacts = new int[] {136};
                break;
            case LARIAT:
                contacts = new int[] {164};
                break;
            case WAR_MACHINE:
                contacts = new int[] {196};
                break;
            case COMPRESSION:
                contacts = new int[] {176};
                break;
            case TESLA_GRAPPLE:
                contacts = new int[] {154};
                break;
            case PRIMITIVE:
                contacts = new int[] {188, 208};
                break;
            default:
                return 0;
        }
        double v = 0;
        for (int c : contacts)
            if (t >= c - 14 && t <= c) v = Math.max(v, smooth((t - c + 14) / 14));
        return v;
    }

    public static int[][] punchBeats(Pattern p) {
        switch (p) {
            case BOXING:
                return new int[][] {{24, 1}, {40, 2}, {56, 1}, {72, 2}, {90, 2}, {124, 3}};
            case CATCH_HANDS:
                return new int[][] {
                    {26, 1}, {44, 2}, {60, 1}, {74, 2}, {86, 1}, {96, 2}, {105, 1}, {114, 2}
                };
            case HAYMAKER:
                return new int[][] {{58, 1}, {82, 2}};
            case GROUND_POUND:
                return new int[][] {{52, 1}, {72, 1}, {92, 1}, {132, 1}};
            case KNUCKLEQUAKE:
                return new int[][] {{28, 1}, {50, 2}, {70, 1}, {86, 2}, {100, 1}, {112, 2}};
            case CLAP:
                return new int[][] {{46, 3}, {102, 2}};
            case TWO_MAN:
                return new int[][] {
                    {26, 1}, {46, 2}, {66, 1}, {86, 2}, {106, 1}, {126, 2}, {146, 3}
                };
            case ONE_TWO:
                return new int[][] {
                    {28, 1}, {48, 2}, {76, 1}, {92, 2}, {106, 1}, {118, 2}, {132, 3}
                };
            case THROW_WORLD:
                return new int[][] {{94, 1}};
            case PRIMITIVE:
                return new int[][] {
                    {24, 1}, {42, 2}, {60, 1}, {80, 1}, {122, 2}, {188, 1}, {208, 2}
                };
            default:
                return new int[0][0];
        }
    }

    public static double parryCue(Pattern p, double t, int arm) {
        if (p.phase != 3) return arm == parryArm(p, t) ? parryCue(p, t) : 0;
        double cue = 0;
        if (p == Pattern.LARIAT) {
            if (arm != 0) return 0;
            for (int beat = 8; beat <= 144; beat += 8)
                if (t >= beat - 7 && t <= beat) cue = Math.max(cue, smooth((t - beat + 7) / 7));
            if (t >= 150 && t <= 164) cue = Math.max(cue, smooth((t - 150) / 14));
        }
        for (int[] beat : punchBeats(p))
            if ((beat[1] & (1 << arm)) != 0 && t >= beat[0] - 14 && t <= beat[0])
                cue = Math.max(cue, smooth((t - beat[0] + 14) / 14));
        return cue;
    }

    private BrawlerScore() {}
}
