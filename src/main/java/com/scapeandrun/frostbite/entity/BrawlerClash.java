package com.scapeandrun.frostbite.entity;

public final class BrawlerClash {
    public static final int STRUGGLE = 100, IMPACT = 118, END = 132, SHIELD_HITS = 7;

    public static double returned(double tick) {
        return BrawlerScore.smooth((tick - STRUGGLE) / (IMPACT - STRUGGLE));
    }

    public static double brace(double tick) {
        return BrawlerScore.smooth(tick / 8) * (1 - BrawlerScore.smooth((tick - 94) / 10));
    }

    public static int hitsAfterReturn(int previous) {
        return Math.min(SHIELD_HITS, Math.max(0, previous) + 1);
    }

    private BrawlerClash() {}
}
