package com.exoarsenal.expedition;

public final class DeepWorldRules {
    private DeepWorldRules() {}

    public static int center(long seed, int cellX, int cellZ, boolean x) {
        long hash = GeologyRules.mix(seed ^ cellX * 49999L ^ cellZ * 79999L);
        return (x ? cellX : cellZ) * 512 + 192 + (int) ((hash >>> (x ? 0 : 8)) & 63) - 32;
    }
}
