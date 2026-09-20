package com.exoarsenal.entity;

public final class ScoutRangeReel {
    public static final int WINDUP = 18,
            OUTBOUND_END = 50,
            SLAM_START = 90,
            SLAM = 102,
            END = 120,
            COOLDOWN = 200;

    private ScoutRangeReel() {}

    public static boolean outOfReach(double distanceSquared) {
        return distanceSquared > 14 * 14 && distanceSquared <= 56 * 56;
    }

    public static boolean ready(double distanceSquared, int idleTicks, int cooldown) {
        return outOfReach(distanceSquared) && idleTicks >= 20 && cooldown <= 0;
    }
}
