package com.scapeandrun.frostbite.entity;

public final class WulfrumFinalPairScore {
    public enum Pattern {
        attack_1(0, 300),
        attack_2(0, 300),
        attack_3(0, 340),
        attack_4(0, 320),
        attack_5(0, 300),
        attack_6(0, 340),
        attack_7(0, 330),
        attack_8(0, 320),
        attack_9(0, 320),
        attack_10(0, 450),
        attack_11(1, 320),
        attack_12(1, 340),
        attack_13(1, 340),
        attack_14(1, 300),
        attack_15(1, 300),
        attack_16(1, 340),
        attack_17(1, 340),
        attack_18(1, 340),
        attack_19(1, 330),
        attack_20(1, 450),
        attack_21(2, 320),
        attack_22(2, 340),
        attack_23(2, 320),
        attack_24(2, 340),
        attack_25(2, 330),
        attack_26(2, 340),
        attack_27(2, 340),
        attack_28(2, 340),
        attack_29(2, 340),
        attack_30(2, 400),
        attack_31(3, 320),
        attack_32(3, 340),
        attack_33(3, 340),
        attack_34(3, 320),
        attack_35(3, 340),
        attack_36(3, 340),
        attack_37(3, 340),
        attack_38(3, 340),
        attack_39(3, 360),
        attack_40(3, 420),
        attack_41(4, 400),
        attack_42(4, 500),
        attack_43(4, 340),
        attack_44(4, 340),
        attack_45(4, 340),
        attack_46(4, 320),
        attack_47(4, 320),
        attack_48(4, 360),
        attack_49(4, 340),
        attack_50(4, 520);
        public final int pair, duration;

        Pattern(int pair, int duration) {
            this.pair = pair;
            this.duration = duration;
        }
    }

    public static int pairing(boolean biped, boolean drill, boolean survivor, boolean seer) {
        if (biped && drill) return 4;
        if (!survivor) return -1;
        if (biped) return seer ? 0 : 1;
        if (drill) return seer ? 2 : 3;
        return -1;
    }

    public static Pattern select(int pair, int cursor) {
        if (pair < 0 || pair > 4) throw new IllegalArgumentException("Invalid final pair");
        return Pattern.values()[pair * 10 + Math.floorMod(cursor, 10)];
    }

    private WulfrumFinalPairScore() {}
}
