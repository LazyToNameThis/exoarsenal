package com.exoarsenal.entity;

public final class WulfrumCombatClock {
    public static final int STEPS = 2;

    public static int ticks(int authored) {
        return authored <= 0 ? 0 : (authored + STEPS - 1) / STEPS;
    }

    private WulfrumCombatClock() {}
}
