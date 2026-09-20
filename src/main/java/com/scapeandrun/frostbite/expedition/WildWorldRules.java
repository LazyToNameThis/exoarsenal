package com.scapeandrun.frostbite.expedition;

public final class WildWorldRules {
    private WildWorldRules() {}

    public static double jungleField(int x, int y, int z, long seed) {
        double phase = (GeologyRules.mix(seed) & 65535) * .0001;
        return Math.abs(
                Math.sin(x * .071 + phase)
                        + Math.cos(z * .083 - phase)
                        + Math.sin(y * .17 + x * .023 - z * .017));
    }

    public static boolean evilCavity(int x, int y, int z, int surface, long seed, boolean crimson) {
        int cellX = Math.floorDiv(x, 96), cellZ = Math.floorDiv(z, 96);
        long hash = GeologyRules.mix(seed ^ cellX * 71237L ^ cellZ * 19139L);
        double dx = x - (cellX * 96 + 48 + (hash & 15) - 8),
                dz = z - (cellZ * 96 + 48 + ((hash >>> 4) & 15) - 8);
        if (crimson) {
            double center = 27 + (hash >>> 8 & 7);
            double chamber =
                    dx * dx / (21 * 21)
                            + dz * dz / (18 * 18)
                            + (y - center) * (y - center) / (11 * 11);
            double shaft = Math.abs(dx - Math.sin(y * .11) * 8);
            return chamber < 1 || shaft < 2.4 && Math.abs(dz) < 3.2 && y > center && y <= surface;
        }
        return Math.abs(dx - Math.sin(y * .07) * 3) < 2.2
                && Math.abs(dz) < 22
                && y > 14
                && y <= surface;
    }
}
