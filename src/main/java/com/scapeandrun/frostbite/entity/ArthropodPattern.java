package com.scapeandrun.frostbite.entity;

public final class ArthropodPattern {
    private ArthropodPattern() {}

    private static final int[] FIRST = {1, 3, 4, 2, 7}, SECOND = {1, 5, 7, 4, 8, 2, 6, 9, 3};

    public static int at(boolean phaseTwo, int cursor) {
        int[] p = phaseTwo ? SECOND : FIRST;
        return p[Math.floorMod(cursor, p.length)];
    }

    public static int duration(int attack) {
        switch (attack) {
            case 1:
                return 70;
            case 2:
                return 72;
            case 3:
                return 64;
            case 4:
                return 60;
            case 5:
                return 80;
            case 6:
                return 82;
            case 7:
                return 66;
            case 8:
                return 88;
            case 9:
                return 78;
            default:
                return 48;
        }
    }

    public static float impulse(float tick, float contact) {
        float rise = Math.max(0, Math.min(1, (tick - (contact - 12)) / 12));
        rise = rise * rise * (3 - 2 * rise);
        float decay = Math.max(0, Math.min(1, (tick - contact) / 16));
        decay = decay * decay * (3 - 2 * decay);
        return rise * (1 - decay);
    }
}
