package com.exoarsenal.entity;

public final class WulfrumPairPose {
    private static double ease(double x) {
        x = Math.max(0, Math.min(1, x));
        return x * x * (3 - 2 * x);
    }

    public static double hit(double t, double contact) {
        return ease((t - contact + 12) / 12) * (1 - ease((t - contact) / 18));
    }

    public static double[] wrist(WulfrumCoordinationScore.Pattern p, int arm, double t) {
        double x = 0, y = 0, z = 0;
        switch (p) {
            case attack_1:
                x = -28 * hit(t % 66, 28);
                z = arm == 0 ? 12 * hit(t % 66, 28) : 0;
                break;
            case attack_2:
                y = arm == 0 ? 30 * hit(t % 70, 35) : 0;
                x = -18 * hit(t % 70, 35);
                break;
            case attack_3:
                x = arm == 3 ? -18 * hit(t % 10, 2) : 0;
                y = arm == 3 ? Math.sin(t * .035) * 24 : 0;
                break;
            case attack_4:
                x = arm == 2 ? -35 : 15;
                z = Math.sin(t * .07) * 22;
                break;
            case attack_5:
                y = arm == 3 ? t * 3 : 0;
                x = arm == 0 ? -30 * hit(t % 26, 15) : 0;
                break;
            case attack_6:
                z = arm == 1 && t > 170 ? t * 9 : 0;
                y = arm == 0 ? 22 * hit(t % 20, 12) : 0;
                break;
            case attack_7:
                x = arm == 0 ? -32 * hit(t % 64, 30) : 0;
                z = arm == 0 ? -10 : 0;
                break;
            case attack_8:
                x = arm == 0 ? -38 * hit(t % 65, 28) : 0;
                y = arm == 2 ? Math.sin(t * .07) * 40 : 0;
                break;
            case attack_9:
                x = arm == 0 ? 55 * hit(t, 55) : arm == 1 ? 75 * hit(t, 150) : 0;
                z = arm == 1 ? -75 * hit(t, 150) : 0;
                break;
            case attack_10:
                y = arm == 2 ? Math.sin(t * .09) * 55 : 0;
                x = arm == 2 ? -25 * hit(t % 30, 16) : 0;
                break;
            case attack_11:
                x = arm == 0 ? 65 * hit(t, 145) : 0;
                z = arm == 0 ? 20 * hit(t, 130) : 0;
                break;
            case attack_12:
                x = arm == 2 ? -55 : arm == 0 ? -20 : 0;
                y = arm == 2 ? Math.sin(t * .07) * 20 : 0;
                break;
            case attack_13:
                x = arm == 0 ? -30 * hit(t, 100) : 0;
                y = arm == 0 ? -12 : 0;
                break;
            default:
                break;
        }
        return new double[] {x, y, z};
    }

    public static double bank(WulfrumCoordinationScore.Pattern p, double t) {
        if (p.phaseTwo) return Math.sin(t * .11) * 28;
        switch (p) {
            case attack_4:
                return Math.sin(t * .07) * 24;
            case attack_6:
                return t > 170 ? -Math.sin(t * .16) * 28 : Math.sin(t * .08) * 12;
            case attack_5:
                return -18 * Math.sin(t * .04);
            case attack_12:
                return Math.sin(t * .05) * 8;
            case attack_11:
                return -25 * hit(t, 140);
            case attack_13:
                return 12 * hit(t, 100);
            default:
                return Math.sin(t * .035) * 5;
        }
    }

    public static double thrust(WulfrumCoordinationScore.Pattern p, double t) {
        if (p.phaseTwo) return .85 + .5 * hit(t % 48, 26);
        switch (p) {
            case attack_4:
                return .6 + .8 * hit(t % 36, 18);
            case attack_12:
                return .7 + .7 * hit(t % 70, 24);
            case attack_8:
                return .6 + .8 * hit(t % 65, 28);
            case attack_11:
                return .6 + .8 * hit(t, 135);
            case attack_13:
                return .5 + .9 * hit(t, 95);
            default:
                return .65 + .2 * Math.sin(t * .09);
        }
    }

    public static int seerAttack(WulfrumCoordinationScore.Pattern p) {
        if (p.phaseTwo) return SeerObserverPattern.SAW_ORBIT;
        switch (p) {
            case attack_1:
            case attack_5:
            case attack_14:
            case attack_19:
            case attack_20:
                return SeerObserverPattern.SAW_ORBIT;
            case attack_4:
            case attack_17:
                return SeerObserverPattern.DOUBLE_PENDULUM;
            case attack_6:
            case attack_16:
            case attack_3:
                return SeerObserverPattern.ZIGZAG;
            default:
                return SeerObserverPattern.FENCING;
        }
    }

    private WulfrumPairPose() {}
}
