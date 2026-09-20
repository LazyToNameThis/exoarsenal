package com.exoarsenal.entity;

public final class DesertScourgePattern {
    public static final int INTRO = 0,
            HUNT = 1,
            BURROW = 2,
            LUNGE = 3,
            NUISANCES = 4,
            SAND_SPIT = 5,
            SAND_RUSH = 6,
            SANDSTORM = 7,
            GROUND_SLAM = 8,
            VULTURES = 9,
            RECOVER = 10;
    public static final int SEGMENTS = 25;

    private DesertScourgePattern() {}

    public static int phase(boolean expert, float fraction) {
        return expert
                ? (fraction <= .25F ? 3 : fraction <= .55F ? 2 : 1)
                : (fraction <= .5F ? 2 : 1);
    }

    public static int[] pool(int phase) {
        return phase >= 3
                ? new int[] {SAND_SPIT, SAND_RUSH, SANDSTORM, GROUND_SLAM, VULTURES}
                : phase == 2
                        ? new int[] {SAND_SPIT, SAND_RUSH, SANDSTORM, GROUND_SLAM}
                        : new int[] {SAND_SPIT, SAND_RUSH, SANDSTORM};
    }

    public static int choose(int phase, int previous, int choice) {
        int[] pool = pool(phase);
        int index = Math.floorMod(choice, pool.length);
        if (pool[index] == previous) index = (index + 1) % pool.length;
        return pool[index];
    }

    public static int duration(int attack) {
        switch (attack) {
            case INTRO:
                return 100;
            case SAND_SPIT:
                return 120;
            case SAND_RUSH:
                return 160;
            case SANDSTORM:
            case GROUND_SLAM:
                return 160;
            case VULTURES:
                return 50;
            case RECOVER:
                return 28;
            default:
                return 200;
        }
    }

    public static double deathSpeed(float fraction) {
        return .32 * (1 + (1 - Math.max(0, Math.min(1, fraction))) * 1.3333);
    }

    public static double deathTurn(float fraction) {
        return .024 * (1 + (1 - Math.max(0, Math.min(1, fraction))) * 1.25);
    }

    public static double[] follow(
            double px, double py, double pz, double x, double y, double z, double spacing) {
        double dx = x - px,
                dy = y - py,
                dz = z - pz,
                length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 1e-8) return new double[] {px, py, pz - spacing};
        return new double[] {
            px + dx / length * spacing, py + dy / length * spacing, pz + dz / length * spacing
        };
    }
}
