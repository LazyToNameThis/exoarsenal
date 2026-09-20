package com.scapeandrun.frostbite.entity;

public final class FrigidAttackTiming {
    private FrigidAttackTiming() {}

    private static final int[] NONE = {},
            RAM = {30},
            DRONE = {18},
            BURST = {18, 24, 30},
            AMP = {24},
            BASH = {20},
            CHARGE = {22},
            STOMP = {28},
            ROVER = {18, 26};
    private static final int[] PINCER = {22, 38},
            PINCER_TWO = {22, 38, 54},
            WAVES = {28, 40, 52},
            FAN = {22, 30, 38},
            TRACK = {24, 32, 40, 48, 56},
            CRUSH = {32},
            SPIN = {24, 32, 40, 48, 56},
            FEINT = {28, 48};

    public static int[] contacts(int kind, int attack, boolean phaseTwo) {
        if (attack == 0) return NONE;
        switch (kind) {
            case 0:
                return attack == 2 ? BURST : DRONE;
            case 1:
                return AMP;
            case 2:
                return attack == 1 ? BASH : attack == 2 ? CHARGE : STOMP;
            case 3:
                return attack == 1 ? ROVER : CHARGE;
            default:
                switch (attack) {
                    case 1:
                        return phaseTwo ? PINCER_TWO : PINCER;
                    case 2:
                        return phaseTwo ? WAVES : STOMP;
                    case 3:
                        return RAM;
                    case 4:
                        return FAN;
                    case 5:
                        return STOMP;
                    case 6:
                        return TRACK;
                    case 7:
                        return CRUSH;
                    case 8:
                        return SPIN;
                    case 9:
                        return FEINT;
                    default:
                        return STOMP;
                }
        }
    }

    public static float next(int kind, int attack, boolean phaseTwo, float tick) {
        for (int contact : contacts(kind, attack, phaseTwo)) if (contact >= tick) return contact;
        return -1;
    }

    public static float recoil(int kind, int attack, boolean phaseTwo, float tick) {
        float value = 0;
        for (int contact : contacts(kind, attack, phaseTwo)) {
            float age = tick - contact;
            if (age >= 0 && age < 8) value = Math.max(value, (1 - age / 8) * (1 - age / 8));
        }
        return value;
    }
}
