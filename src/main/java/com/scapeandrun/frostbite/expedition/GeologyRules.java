package com.scapeandrun.frostbite.expedition;

public final class GeologyRules {
    private GeologyRules() {}

    public static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    public static int cave(long seed, int x, int z) {
        return (int) Math.floorMod(mix(seed ^ x * 341873128712L ^ z * 132897987541L ^ 0xCA6EL), 5);
    }

    public static boolean alternate(long seed, int tier) {
        return (mix(seed + tier * 59359L) & 1) != 0;
    }

    public static int cell(int block) {
        return Math.floorDiv(block, 128);
    }

    public static int caveX(long seed, int x, int z) {
        return x * 128 + 40 + (int) Math.floorMod(mix(seed ^ x * 31L ^ z * 971L), 48);
    }

    public static int caveZ(long seed, int x, int z) {
        return z * 128 + 40 + (int) Math.floorMod(mix(seed ^ x * 677L ^ z * 71L), 48);
    }

    public static int caveY(long seed, int x, int z) {
        return 18 + (int) Math.floorMod(mix(seed ^ x * 773L ^ z * 449L), 20);
    }
}
