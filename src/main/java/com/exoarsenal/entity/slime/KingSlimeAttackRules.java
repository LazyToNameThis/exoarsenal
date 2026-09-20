package com.exoarsenal.entity.slime;

public final class KingSlimeAttackRules {
    private KingSlimeAttackRules() {}

    public static final int SMALL = 0, LARGE = 1, TELEPORT = 2, DEATH = 3;
    private static final int[] INFERNUM = {SMALL, SMALL, LARGE, TELEPORT, LARGE};

    public static int attack(int index) {
        return INFERNUM[Math.floorMod(index, INFERNUM.length)];
    }

    public static int ticks(int sourceFrames) {
        return (sourceFrames + 2) / 3;
    }

    public static int jewelMode(float health, int age) {
        if (health > .55F) return 0;
        if (health > .35F) return 1;
        return (age / 80) % 2;
    }

    public static float size(boolean expert, float health) {
        return expert ? 2.2F + 2.4F * health : 2F + 4F * health;
    }

    public static float teleportScale(int tick) {
        if (tick <= 20) {
            float t = tick / 20F;
            return 1 - .9F * t * t * t;
        }
        return Math.min(1, .1F + .9F * (tick - 20) / 10F);
    }
}
