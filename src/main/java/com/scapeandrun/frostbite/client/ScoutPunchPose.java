package com.scapeandrun.frostbite.client;

import java.util.Map;

public final class ScoutPunchPose {
    private ScoutPunchPose() {}

    private static float c(float t, float... k) {
        return ScoutBrawlPose.curve(t, k);
    }

    private static void r(Map<String, float[]> m, String n, float x, float y, float z) {
        m.put(n, new float[] {x, y, z});
    }

    public static void apply(Map<String, float[]> m, float t) {
        r(
                m,
                "chassis",
                c(t, 0, 0, 12, 4, 18, -8, 24, 0, 30, -6, 36, 3, 42, -10, 48, 8, 56, -18, 62, -5),
                c(t, 0, 0, 12, -16, 18, 18, 24, 8, 30, -14, 36, -20, 42, 20, 48, 0, 56, 0, 62, 0),
                0);

        r(
                m,
                "rightSwordArm",
                c(
                        t, 0, 48, 12, 62, 18, 78, 23, 58, 44, 48, 49, 138, 53, 142, 56, 76, 60, 62,
                        62, 48),
                c(t, 0, 28, 12, 5, 18, 68, 23, 48, 44, 28, 50, 35, 56, 48, 62, 28),
                c(t, 0, 0, 12, -12, 18, 16, 25, 0, 48, 0, 53, -8, 56, 0));
        r(
                m,
                "rightSwordElbow",
                c(t, 0, 32, 12, 52, 18, 48, 25, 32, 48, 32, 53, 55, 56, 8, 62, 32),
                0,
                -8);

        r(
                m,
                "clawArm",
                c(t, 0, 32, 22, 10, 26, 4, 30, 110, 34, 90, 41, 32, 62, 32),
                c(t, 0, -25, 24, -12, 30, -62, 36, -48, 42, -25),
                c(t, 0, -12, 24, -18, 30, 8, 42, -12));
        r(m, "leftViceElbow", c(t, 0, 35, 24, 65, 30, 30, 35, 22, 42, 35), 0, 0);

        r(
                m,
                "leftSwordArm",
                c(t, 0, 48, 32, 48, 37, 72, 42, 78, 46, 64, 52, 48, 62, 48),
                c(t, 0, -28, 34, -64, 38, -70, 42, -8, 46, 8, 54, -28),
                c(t, 0, 0, 36, 16, 42, -18, 52, 0));
        r(m, "leftSwordElbow", c(t, 0, 32, 36, 56, 42, 28, 47, 20, 54, 32), 0, 8);

        r(
                m,
                "viceArm",
                c(t, 0, 32, 44, 32, 50, 120, 53, 125, 56, 65, 60, 48, 62, 32),
                c(t, 0, 25, 48, 35, 56, 55, 62, 25),
                12);
        r(m, "rightViceElbow", c(t, 0, 35, 50, 60, 56, 12, 62, 35), 0, 0);
        r(
                m,
                "leftThigh",
                c(t, 0, 0, 12, 6, 18, -4, 24, 5, 30, -8, 42, 4, 50, 12, 56, 5, 62, 0),
                0,
                -5);
        r(m, "rightThigh", c(t, 0, 0, 12, -5, 18, 6, 30, 5, 42, -4, 50, 12, 56, 5, 62, 0), 0, 5);
        r(m, "pilot", c(t, 0, 0, 18, 3, 24, 0, 30, 3, 36, 0, 42, 4, 48, 0, 56, 6, 62, 0), 0, 0);
    }
}
