package com.scapeandrun.frostbite.client;

import java.util.Map;
import com.scapeandrun.frostbite.entity.ScoutRigMath;

public final class ScoutEntrance {
    public static final int RELEASE = 60,
            CATCH = 100,
            TWIRL_START = 112,
            TWIRL_END = 156,
            DIALOGUE = 202,
            END = 248;

    private static float c(float t, float... keys) {
        return ScoutBrawlPose.curve(t, keys);
    }

    private static void pose(Map<String, float[]> m, String n, float x, float y, float z) {
        m.put(n, new float[] {x, y, z});
    }

    public static boolean airborne(float t) {
        return t >= RELEASE && t < CATCH;
    }

    public static float spin(float t) {
        return (float)
                (Math.PI * 6 * CombatMotion.smooth((t - TWIRL_START) / (TWIRL_END - TWIRL_START)));
    }

    public static float cape(float t) {
        return CombatMotion.smooth((t - 174) / 26);
    }

    public static double[] sword(float t) {
        double f = Math.max(0, Math.min(1, (t - RELEASE) / (double) (CATCH - RELEASE)));
        double[] a = ScoutRigMath.hand(2, 0, RELEASE), b = ScoutRigMath.hand(2, 0, CATCH);
        return new double[] {
            a[0] + (b[0] - a[0]) * f,
            a[1] + (b[1] - a[1]) * f + 6 * 4 * f * (1 - f),
            a[2] + (b[2] - a[2]) * f
        };
    }

    public static void apply(Map<String, float[]> m, float t) {
        if (t < 48) return;
        Map<String, float[]> preceding = new java.util.HashMap<>(m);
        pose(
                m,
                "chassis",
                c(
                        t, 48, 3, 56, -6, 66, 4, 96, -7, 104, 3, 120, 0, 168, 0, 194, 16, 224, 16,
                        248, 0),
                c(t, 48, 0, 90, -12, 104, 9, 118, 0),
                0);
        pose(
                m,
                "leftSwordArm",
                c(
                        t, 48, 0, 54, -25, 60, 78, 72, 90, 88, 68, 100, 52, 108, 42, 156, 42, 180,
                        20, 224, 20, 248, 0),
                c(t, 48, 0, 76, -12, 90, -38, 100, 18, 112, 0),
                -12);
        pose(
                m,
                "leftSwordElbow",
                c(
                        t, 48, 0, 56, -35, 60, -8, 80, -10, 92, -35, 100, -18, 112, -55, 156, -55,
                        182, -25, 224, -25, 248, 0),
                0,
                0);
        pose(
                m,
                "rightSwordArm",
                c(t, 48, 0, 70, 12, 100, 8, 156, 8, 184, 38, 224, 38, 248, 0),
                0,
                12);
        pose(m, "rightSwordElbow", c(t, 48, 0, 156, 0, 184, -45, 224, -45, 248, 0), 0, 0);
        pose(
                m,
                "sensorArray",
                c(t, 48, 0, 66, -22, 80, -28, 98, 0, 160, 0, 194, 10, 224, 10, 248, 0),
                c(t, 48, 0, 90, -12, 104, 0),
                0);
        pose(m, "leftThigh", c(t, 48, 8, 60, 0, 164, 0, 194, 78, 224, 78, 248, 0), 0, -5);
        pose(m, "leftShin", c(t, 48, -6, 60, 0, 164, 0, 194, -94, 224, -94, 248, 0), 0, 0);
        pose(m, "rightThigh", c(t, 48, 2, 60, 0, 164, 0, 194, 18, 224, 18, 248, 0), 0, 5);
        pose(m, "rightShin", c(t, 48, -3, 60, 0, 164, 0, 194, -88, 224, -88, 248, 0), 0, 0);
        String[] fingers = {"Index", "Middle", "Ring", "Little", "Thumb"};
        for (int i = 0; i < 5; i++) {
            float grip =
                    c(
                            t,
                            48,
                            1,
                            58,
                            0,
                            92,
                            0,
                            102 + i,
                            1,
                            110,
                            1,
                            118,
                            i == 0 ? 0 : 1,
                            150,
                            i == 0 ? 0 : 1,
                            160,
                            1);
            pose(m, "left" + fingers[i], -grip * (i == 4 ? 25 : 62 + i * 4), 0, 0);
            pose(m, "left" + fingers[i] + "Tip", -grip * (i == 4 ? 18 : 52), 0, 0);
        }
        float blend = CombatMotion.smooth((t - 48) / 8);
        if (blend < 1)
            for (Map.Entry<String, float[]> entry : m.entrySet()) {
                float[] previous = preceding.get(entry.getKey());
                if (previous != null)
                    for (int axis = 0; axis < 3; axis++)
                        entry.getValue()[axis] =
                                previous[axis] + (entry.getValue()[axis] - previous[axis]) * blend;
            }
    }

    private ScoutEntrance() {}
}
