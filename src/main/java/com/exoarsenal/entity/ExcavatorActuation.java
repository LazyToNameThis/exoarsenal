package com.exoarsenal.entity;

public final class ExcavatorActuation {
    public final double jaws, braces, drill, roll, vents;

    private ExcavatorActuation(
            double jaws, double braces, double drill, double roll, double vents) {
        this.jaws = jaws;
        this.braces = braces;
        this.drill = drill;
        this.roll = roll;
        this.vents = vents;
    }

    public static double ease(double t) {
        t = Math.max(0, Math.min(1, t));
        return Math.max(0, Math.min(1, t * t * t * (t * (t * 6 - 15) + 10)));
    }

    private static double ramp(double t, double a, double b) {
        return ease((t - a) / (b - a));
    }

    private static double hold(double t, double a, double b, double c, double d) {
        return ramp(t, a, b) * (1 - ramp(t, c, d));
    }

    public static double recoil(double age) {
        return age < 0 || age > 14 ? 0 : age < 2 ? ease(age / 2) : 1 - ease((age - 2) / 12);
    }

    public static ExcavatorActuation coordinated(
            WulfrumCoordinationScore.Pattern pattern, double t, boolean stalled) {
        if (stalled) return new ExcavatorActuation(.85, 0, -2, Math.sin(t * .5) * 2, 1);
        switch (pattern) {
            case attack_7:
            case attack_8:
            case attack_12:
                return new ExcavatorActuation(
                        .35 + .5 * hold(t % 65, 0, 20, 34, 48),
                        0,
                        -2 * hold(t % 65, 0, 20, 28, 34) + 2 * hold(t % 65, 28, 34, 48, 60),
                        pattern == WulfrumCoordinationScore.Pattern.attack_8
                                ? Math.sin(t * .08) * 15
                                : 0,
                        .65);
            case attack_9:
                return classic(ExcavatorClassicScore.Attack.CORE_SAMPLE, t);
            case attack_11:
                return classic(ExcavatorClassicScore.Attack.FAULT_LINE, t);
            case attack_10:
            case attack_13:
            case attack_15:
            case attack_18:
            case attack_19:
                return new ExcavatorActuation(
                        .35,
                        0,
                        .4,
                        Math.sin(t * .025) * 7,
                        hold(t, 0, 18, pattern.duration - 25, pattern.duration));
            case attack_14:
                return new ExcavatorActuation(.4, 0, 1.2, Math.sin(t * .04) * 10, .8);
            case attack_16:
                return new ExcavatorActuation(
                        .6 * hit(t % 16, 12), 0, 2 * hit(t % 16, 12), Math.sin(t * .07) * 12, .4);
            case attack_17:
                return new ExcavatorActuation(
                        .7 * hit(t % 45, 0), 0, 1.6, Math.sin(t * .06) * 18, .6);
            case attack_20:
                return new ExcavatorActuation(
                        .5, 0, 2 * hit(t, 204), -18 * hold(t, 0, 20, 185, 205), .7);
            default:
                return new ExcavatorActuation(0, 0, 0, 0, 0);
        }
    }

    private static double hit(double t, double at) {
        return WulfrumPairPose.hit(t, at);
    }

    public static ExcavatorActuation classic(ExcavatorClassicScore.Attack attack, double t) {
        double jaws = 0, braces = 0, drill = 0, roll = 0, vents = 0;
        switch (attack) {
            case SEISMIC_BORE:
                jaws = hold(t, 88, 112, 126, 145);
                drill = -2 * hold(t, 90, 112, 118, 126) + 1.8 * hold(t, 118, 126, 145, 160);
                roll = 12 * hold(t, 20, 42, 70, 85) * Math.sin((t - 20) * .055);
                break;
            case TRENCH_CUTTER:
                double pass = t < 90 ? t : t < 170 ? t - 80 : t - 160;
                jaws = .6 * hold(pass, 8, 20, 52, 70);
                drill = 1.6 * hold(pass, 18, 28, 52, 65);
                roll = -13 * hold(pass, 15, 27, 48, 68);
                vents = .4 * hold(pass, 45, 55, 70, 85);
                break;
            case SURVEYING_PROBES:
                jaws = .65 * hold(t, 1, 18, 152, 175);
                vents = hold(t, 1, 15, 154, 179);
                break;
            case STRIP_MINE:
                braces = hold(t, 1, 20, 140, 156);
                jaws = hold(t, 12, 27, 155, 182);
                roll = 18 * hold(t, 20, 32, 62, 76) * Math.sin((t - 20) * .12);
                drill = -2.5 * hold(t, 128, 151, 155, 164) + 2 * hold(t, 155, 160, 169, 185);
                vents = hold(t, 64, 80, 170, 202);
                break;
            case TURRET_SWEEP:
                braces = hold(t, 1, 25, 165, 183);
                jaws = hold(t, 12, 30, 175, 189);
                drill = -2 * hold(t, 120, 168, 175, 185) + 2 * hold(t, 181, 188, 204, 224);
                vents = hold(t, 30, 65, 175, 205);
                break;
            case CORE_SAMPLE:
                jaws = hold(t, 25, 65, 125, 144);
                drill = 2 * hold(t, 72, 84, 126, 142);
                roll = 16 * hold(t, 25, 40, 64, 79) * Math.sin(t * .09);
                vents = hold(t, 130, 145, 198, 224);
                break;
            case FAULT_LINE:
                jaws = hold(t, 65, 94, 106, 124);
                drill = -2 * hold(t, 65, 95, 103, 111) + 2 * hold(t, 105, 114, 143, 167);
                break;
            case PROBE_MINEFIELD:
                vents = hold(t, 1, 20, 220, 247);
                jaws = .45 * hold(t, 1, 22, 34, 48);
                break;
            case BORE_RICOCHET:
                jaws = .8 * hold(t, 15, 32, 135, 162);
                drill = -2 * hold(t, 20, 34, 39, 45) + 1.5 * hold(t, 40, 47, 134, 152);
                break;
            case INDUSTRIAL_PROCESSING:
                vents = hold(t, 1, 18, 260, 288) + .8 * hold(t, 303, 320, 390, 419);
                jaws = hold(t, 235, 263, 276, 290);
                drill = 2 * hold(t, 260, 275, 298, 306);
                roll = 3 * hold(t, 320, 323, 327, 331) - 4 * hold(t, 340, 344, 352, 357);
                break;
            case FINAL_CHARGE:
                jaws = hold(t, 25, 65, 106, 122);
                drill = -3 * hold(t, 40, 87, 108, 115) + 2 * hold(t, 110, 116, 138, 147);
                vents = hold(t, 35, 65, 110, 130) + hold(t, 144, 158, 181, 199);
                break;
        }
        return new ExcavatorActuation(jaws, braces, drill, roll, Math.min(1, vents));
    }

    public static ExcavatorActuation expert(int attack, int stage, double t) {
        if (stage == 1)
            return new ExcavatorActuation(
                    ramp(t, 10, 30),
                    hold(t, 3, 18, 38, 46),
                    -3 * ramp(t, 20, 43),
                    0,
                    ramp(t, 24, 42));
        if (stage == 2)
            return new ExcavatorActuation(1 - ramp(t, 0, 12), 0, 2 * ramp(t, 0, 5), 0, 0);
        if (stage == 3 || stage == 12)
            return new ExcavatorActuation(.8, 0, -2, Math.sin(t * .28) * 1.5, .5);
        if (stage == 4 || stage == 6) return new ExcavatorActuation(.65, 0, -1, 0, 1);
        if (stage == 5)
            return new ExcavatorActuation(
                    .8 - ramp(t, 5, 15) * .5,
                    0,
                    -2 + 3 * ramp(t, 8, 15),
                    -6 * hold(t, 0, 4, 8, 15),
                    .8);
        if (stage == 7) return new ExcavatorActuation(.2, 0, 1 - ramp(t, 0, 20), 0, ramp(t, 0, 14));
        if (stage == 8)
            return new ExcavatorActuation(
                    .5 * hold(t, 20, 30, 38, 45),
                    0,
                    -1 + 2 * ramp(t, 28, 42),
                    14 * hold(t, 2, 12, 25, 40),
                    .5);
        if (stage == 9)
            return new ExcavatorActuation(
                    .85, 0, -1.5, 18 * Math.sin(Math.PI * Math.min(1, t / 26)), .7);
        if (stage == 10)
            return new ExcavatorActuation(
                    .7, 0, -2 + 3 * ramp(t, 18, 30), -10 * hold(t, 0, 10, 20, 30), 1);
        switch (attack) {
            case ExcavatorExpertScore.RAILGUN:
                return new ExcavatorActuation(
                        .7 * hold(t, 22, 35, 80, 105),
                        0,
                        1.5 * hold(t, 30, 45, 75, 95),
                        -12 * hold(t, 30, 45, 70, 100),
                        hold(t, 80, 100, 175, 205));
            case ExcavatorExpertScore.CRUSTBREAKER:
                return new ExcavatorActuation(
                        hold(t, 145, 170, 200, 220),
                        0,
                        -2 * hold(t, 175, 185, 192, 200) + 2 * hold(t, 195, 203, 218, 235),
                        16 * hold(t, 150, 165, 185, 200),
                        hold(t, 195, 210, 245, 260));
            case ExcavatorExpertScore.REVOKED:
                double pass = t % 50;
                return new ExcavatorActuation(
                        .5 * hold(pass, 0, 12, 35, 49) + .4 * hold(t, 220, 235, 260, 280),
                        0,
                        1.5 * hold(pass, 12, 18, 35, 49),
                        Math.sin(t * .045) * 10 * hold(t, 0, 15, 270, 290),
                        hold(t, 215, 230, 275, 290));
            case ExcavatorExpertScore.ACCIDENT:
                return new ExcavatorActuation(
                        .5 + .25 * Math.sin(t * .12),
                        0,
                        .6 + Math.sin(t * .19) * .5,
                        Math.sin(t * .075) * 20,
                        .7 + .25 * Math.sin(t * .085));
            case ExcavatorExpertScore.IMPACT:
            case ExcavatorExpertScore.FALSE_IMPACT:
            case ExcavatorExpertScore.TENNIS:
            case ExcavatorExpertScore.COUNTER:
                return new ExcavatorActuation(ramp(t, 0, 12) * .5, 0, -ramp(t, 0, 12), 0, .4);
            case ExcavatorExpertScore.SURVEY:
                return classic(ExcavatorClassicScore.Attack.SURVEYING_PROBES, t);
            case ExcavatorExpertScore.HARPOONS:
                return classic(ExcavatorClassicScore.Attack.PROBE_MINEFIELD, t);
            case ExcavatorExpertScore.EXTRACTION:
                return classic(ExcavatorClassicScore.Attack.CORE_SAMPLE, t);
            case ExcavatorExpertScore.FEEDBACK:
                return new ExcavatorActuation(
                        hold(t, 0, 20, 140, 165), hold(t, 0, 20, 140, 165), -1, 0, .7);
            case ExcavatorExpertScore.CROSSFIRE:
                return classic(ExcavatorClassicScore.Attack.TURRET_SWEEP, t);
            case ExcavatorExpertScore.COMPLETE:
                return classic(ExcavatorClassicScore.Attack.INDUSTRIAL_PROCESSING, t);
            default:
                return new ExcavatorActuation(.4, 0, .8, 0, .4);
        }
    }

    private ExcavatorActuation() {
        this(0, 0, 0, 0, 0);
    }
}
