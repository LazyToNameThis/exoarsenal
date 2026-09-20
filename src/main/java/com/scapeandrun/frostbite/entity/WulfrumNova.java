package com.scapeandrun.frostbite.entity;

public final class WulfrumNova {
    public static final int BURST = 20, END = 76;

    public static double radius(double age) {
        return age < BURST
                ? 1.7 * (1 - Math.min(1, age / BURST)) + .15
                : Math.min(19, (age - BURST) * .46);
    }

    public static float alpha(double age) {
        return age < BURST
                ? (float) (.25 + age / BURST * .65)
                : (float) Math.max(0, 1 - (age - BURST) / (END - BURST));
    }

    private WulfrumNova() {}
}
