package com.scapeandrun.frostbite.expedition;

public final class PrebossRules {
    private PrebossRules() {}

    public static int manaMaximum(int crystals, boolean band) {
        return 20 + Math.max(0, Math.min(9, crystals)) * 20 + (band ? 20 : 0);
    }

    public static int lifeBonus(int crystals) {
        return Math.max(0, Math.min(15, crystals)) * 2;
    }

    public static int runningTicks(int previous, boolean running, boolean ground) {
        return !running
                ? 0
                : ground
                        ? Math.min(30, Math.max(0, previous) + 1)
                        : Math.max(0, Math.min(30, previous));
    }

    public static double runningBonus(int ticks) {
        return Math.max(0, Math.min(30, ticks)) * .02;
    }

    public static double bottleVelocity(int tier) {
        return tier == 1 ? .52 : tier == 2 ? .65 : tier == 3 ? .8 : 0;
    }

    public static int nextBottleTier(int equipped, int used) {
        int available = equipped & ~used & 7;
        return (available & 4) != 0 ? 3 : (available & 2) != 0 ? 2 : (available & 1) != 0 ? 1 : 0;
    }

    public static int splitChildren(int generation, boolean empowered) {
        return generation == 0 || empowered && generation == 1 ? 3 : 0;
    }

    public static int maximumAge(int type) {
        return type == EntityType.FROST
                ? 100
                : type == EntityType.SPLIT ? 20 : type == EntityType.BOOMERANG ? 80 : 45;
    }

    public static final class EntityType {
        public static final int FROST = 1, SPLIT = 2, BOOMERANG = 3;

        private EntityType() {}
    }
}
