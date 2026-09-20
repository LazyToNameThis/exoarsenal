package com.exoarsenal.entity;

import com.exoarsenal.client.ScoutBrawlPose;
import com.exoarsenal.client.ScoutHandPose;
import java.util.Map;

public final class ScoutRigMath {
    private ScoutRigMath() {}

    public static double[] rotate(double[] point, double[] pivot, float[] rotation) {
        double x = point[0] - pivot[0], y = point[1] - pivot[1], z = point[2] - pivot[2];
        double a = Math.toRadians(rotation[0]), c = Math.cos(a), s = Math.sin(a), n = y * c - z * s;
        z = y * s + z * c;
        y = n;
        a = Math.toRadians(rotation[1]);
        c = Math.cos(a);
        s = Math.sin(a);
        n = x * c + z * s;
        z = -x * s + z * c;
        x = n;
        a = Math.toRadians(rotation[2]);
        c = Math.cos(a);
        s = Math.sin(a);
        n = x * c - y * s;
        y = x * s + y * c;
        x = n;
        return new double[] {n + pivot[0], y + pivot[1], z + pivot[2]};
    }

    public static double[] hand(int scene, int attack, float tick) {
        Map<String, float[]> pose = ScoutBrawlPose.sample(scene, attack, tick, 0, 0, tick);
        double[] p = {-12.5, 18, -1};
        p = rotate(p, new double[] {-12.5, 21, 0}, new float[] {0, 90, 0});
        p =
                rotate(
                        p,
                        new double[] {-12.5, 23, 0},
                        new float[] {ScoutHandPose.wrist(attack, tick), 0, 0});
        p =
                rotate(
                        p,
                        new double[] {-12.5, 32, 0},
                        pose.containsKey("leftSwordElbow")
                                ? pose.get("leftSwordElbow")
                                : new float[] {55, 0, 8});
        p =
                rotate(
                        p,
                        new double[] {-12, 44, 0},
                        pose.containsKey("leftSwordArm")
                                ? pose.get("leftSwordArm")
                                : new float[] {0, 0, -8});
        p[0] -= 24;
        p[1] += 6;
        p =
                rotate(
                        p,
                        new double[] {0, 31, 0},
                        pose.containsKey("chassis") ? pose.get("chassis") : new float[] {0, 0, 0});
        return new double[] {p[0] * 2.35 / 16, p[1] * 2.35 / 16, -p[2] * 2.35 / 16};
    }

    public static double[] vice(int scene, int attack, float tick, boolean right) {
        Map<String, float[]> pose = ScoutBrawlPose.sample(scene, attack, tick, 0, 0, tick);
        double side = right ? 1 : -1;
        double[] p = {side * 9.5, 15, 2};
        String elbow = right ? "rightViceElbow" : "leftViceElbow",
                arm = right ? "viceArm" : "clawArm";
        p =
                rotate(
                        p,
                        new double[] {side * 9.5, 24, 2},
                        pose.containsKey(elbow) ? pose.get(elbow) : new float[] {-25, 0, 0});
        p =
                rotate(
                        p,
                        new double[] {side * 8, 34, 2},
                        pose.containsKey(arm)
                                ? pose.get(arm)
                                : new float[] {0, 0, (float) (-side * 28)});
        p[0] += side * 8;
        p[2] -= 3;
        p =
                rotate(
                        p,
                        new double[] {0, 31, 0},
                        pose.containsKey("chassis") ? pose.get("chassis") : new float[] {0, 0, 0});
        return new double[] {p[0] * 2.35 / 16, p[1] * 2.35 / 16, -p[2] * 2.35 / 16};
    }
}
