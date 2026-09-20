package com.scapeandrun.frostbite.entity;

public final class WulfrumCoordinationScore {
    public enum Pattern {
        attack_1(0, 380),
        attack_2(0, 380),
        attack_3(0, 210),
        attack_4(0, 240),
        attack_5(0, 230),
        attack_6(0, 380),
        attack_7(1, 400),
        attack_8(1, 400),
        attack_9(1, 240),
        attack_10(1, 250),
        attack_11(1, 210),
        attack_12(1, 400),
        attack_13(1, 230),
        attack_14(2, 240),
        attack_15(2, 240),
        attack_16(2, 250),
        attack_17(2, 230),
        attack_18(2, 220),
        attack_19(2, 300),
        attack_20(2, 250),
        attack_21(0, 300, true),
        attack_22(0, 320, true),
        attack_23(0, 300, true),
        attack_24(0, 300, true),
        attack_25(0, 320, true),
        attack_26(0, 360, true),
        attack_27(0, 360, true),
        attack_28(0, 320, true),
        attack_29(0, 320, true),
        attack_30(0, 300, true),
        attack_31(0, 360, true),
        attack_32(0, 300, true),
        attack_33(0, 320, true),
        attack_34(0, 320, true),
        attack_35(0, 340, true),
        attack_36(0, 320, true),
        attack_37(0, 400, true),
        attack_38(0, 340, true),
        attack_39(0, 320, true),
        attack_40(0, 400, true),
        attack_41(1, 320, true),
        attack_42(1, 320, true),
        attack_43(1, 340, true),
        attack_44(1, 320, true),
        attack_45(1, 360, true),
        attack_46(1, 400, true),
        attack_47(2, 300, true),
        attack_48(2, 300, true),
        attack_49(2, 340, true),
        attack_50(2, 300, true),
        attack_51(2, 320, true),
        attack_52(2, 340, true),
        attack_53(2, 400, true);
        public final int pair, duration;
        public final boolean phaseTwo;

        Pattern(int pair, int duration) {
            this(pair, duration, false);
        }

        Pattern(int pair, int duration, boolean phaseTwo) {
            this.pair = pair;
            this.duration = duration;
            this.phaseTwo = phaseTwo;
        }
    }

    public static Pattern select(int pair, int cursor) {
        return select(pair, cursor, false);
    }

    public static Pattern select(int pair, int cursor, boolean phaseTwo) {
        int index = Math.floorMod(cursor, count(pair, phaseTwo));
        for (Pattern p : Pattern.values())
            if (p.pair == pair && p.phaseTwo == phaseTwo && index-- == 0) return p;
        throw new IllegalArgumentException("Unknown pair");
    }

    public static int count(int pair, boolean phaseTwo) {
        return phaseTwo ? (pair == 0 ? 20 : pair == 1 ? 6 : 7) : count(pair);
    }

    public static int count(int pair) {
        return pair == 0 ? 6 : 7;
    }

    public static int arms(Pattern p) {
        if (p.phaseTwo) return 0;
        switch (p) {
            case attack_3:
                return 8;
            case attack_4:
            case attack_10:
            case attack_12:
                return 4;
            case attack_5:
                return 9;
            case attack_6:
            case attack_9:
                return 3;
            case attack_8:
                return 5;
            default:
                return p.pair == 2 ? 0 : 1;
        }
    }

    private WulfrumCoordinationScore() {}
}
