package com.scapeandrun.frostbite.entity;

public final class SeerObserverPattern {
    public static final int FENCING = 0,
            CROSSFIRE = 1,
            PASSING = 2,
            FLAIL = 3,
            RICOCHET = 4,
            SLINGSHOT = 5,
            SYNCHRONIZATION = 6,
            SEER_ALONE = 7,
            OBSERVER_ALONE = 8;
    public static final int SHARD_WHEEL = 9,
            ZIGZAG = 10,
            DOUBLE_PENDULUM = 11,
            CRASH_FLAIL = 12,
            SAW_ORBIT = 13,
            SAW_DIVE = 14,
            MACHINEGUN = 15;
    public static final int TRANSFORM_TICKS = 50, SHARD_CAP = 48;
    private static final int[] LENGTH = {
        180, 160, 140, 240, 210, 210, 340, 180, 220, 280, 340, 240, 240, 180, 160, 180
    };
    private static final int[] EXPERT = {
        SHARD_WHEEL,
        FENCING,
        ZIGZAG,
        CROSSFIRE,
        DOUBLE_PENDULUM,
        PASSING,
        CRASH_FLAIL,
        RICOCHET,
        SLINGSHOT
    };

    private SeerObserverPattern() {}

    public static int duration(int attack) {
        return attack >= 35
                ? WulfrumSurvivorScore.duration(attack)
                : attack >= 16
                        ? WulfrumDuetScore.duration(attack)
                        : LENGTH[Math.max(0, Math.min(LENGTH.length - 1, attack))];
    }

    public static int select(int cursor, boolean seer, boolean observer, float health) {
        return !observer
                ? SEER_ALONE
                : !seer ? OBSERVER_ALONE : Math.floorMod(cursor, health < .4F ? 7 : 6);
    }

    public static boolean guard(int attack, int tick) {
        return (attack == FENCING || attack == SEER_ALONE) && tick >= 112 && tick < 132;
    }

    public static double wheelAngle(int tick) {
        double t = Math.max(0, tick - 36);
        return t * .025 + t * t * .00038;
    }

    public static int select(
            int cursor,
            boolean seer,
            boolean observer,
            float health,
            boolean expert,
            boolean saw,
            boolean gun) {
        if (seer && saw && cursor % 3 == 1) return cursor % 2 == 0 ? SAW_ORBIT : SAW_DIVE;
        if (observer && gun && cursor % 3 == 2) return MACHINEGUN;
        if (!seer || !observer) return select(cursor, seer, observer, health);
        if (health < .4F && cursor % 7 == 6) return SYNCHRONIZATION;
        return expert
                ? EXPERT[Math.floorMod(cursor, EXPERT.length)]
                : select(cursor, true, true, health);
    }

    public static boolean detached(int attack, int tick) {
        return attack == ZIGZAG && tick >= 28 && tick < 240
                || WulfrumDuetScore.detached(attack, tick);
    }

    public static int shardRelease(int ordinal) {
        return 156 + Math.max(0, ordinal) / 3 * 12;
    }

    public static float bladeWheel(float tick) {
        double t = Math.max(0, Math.min(108, tick - 36)), angle = 2 * (t * .025 + t * t * .00038);
        if (tick <= 144) return (float) angle;
        double end = Math.ceil(angle / (Math.PI * 2)) * Math.PI * 2,
                u = Math.min(1, (tick - 144) / 24);
        return (float) (angle + (end - angle) * (1 - Math.pow(1 - u, 2)));
    }

    public static float deployment(float tick) {
        float t = Math.max(0, Math.min(1, (tick - 16) / 26));
        return t * t * (3 - 2 * t);
    }

    public static double[] zigzag(int point, boolean inverted) {
        int i = Math.max(0, Math.min(6, point));
        double y = i % 2 == 0 ? 1 : 5;
        return new double[] {i % 2 == 0 ? -10 : 10, inverted ? 7 - y : y, -12 + i * 4};
    }

    public static double[] pendulum(float t) {
        double a = Math.sin(t * .061) * 1.65, b = Math.sin(t * .093 + .8) * 2.1;
        return new double[] {
            Math.sin(a) * 7 + Math.sin(a + b) * 6,
            7 - Math.cos(a) * 3 - Math.cos(a + b) * 3,
            Math.cos(t * .035) * 5
        };
    }

    public static float damage(float base, boolean expert, int difficulty) {
        return base * (expert ? 1.12F : 1) * (difficulty == 1 ? .7F : difficulty == 3 ? 1.12F : 1);
    }
}
