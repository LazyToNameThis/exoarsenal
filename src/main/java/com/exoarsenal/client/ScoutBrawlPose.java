package com.exoarsenal.client;

import com.exoarsenal.entity.ScoutCombatPattern;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ScoutBrawlPose {
    private ScoutBrawlPose() {}

    public static float curve(float tick, float... keys) {
        if (tick <= keys[0]) return keys[1];
        for (int i = 2; i < keys.length; i += 2)
            if (tick <= keys[i]) {
                float span = keys[i] - keys[i - 2], t = (tick - keys[i - 2]) / span;
                float a = keys[i - 1], b = keys[i + 1], s = (b - a) / span;
                float before = i >= 4 ? (a - keys[i - 3]) / (keys[i - 2] - keys[i - 4]) : 0;
                float after = i + 2 < keys.length ? (keys[i + 3] - b) / (keys[i + 2] - keys[i]) : 0;
                float m0 = tangent(before, s) * span,
                        m1 = tangent(s, after) * span,
                        t2 = t * t,
                        t3 = t2 * t;
                return (2 * t3 - 3 * t2 + 1) * a
                        + (t3 - 2 * t2 + t) * m0
                        + (-2 * t3 + 3 * t2) * b
                        + (t3 - t2) * m1;
            }
        return keys[keys.length - 1];
    }

    private static float tangent(float a, float b) {
        return a * b <= 0 ? 0 : 2 * a * b / (a + b);
    }

    private static void rot(Map<String, float[]> m, String name, float x, float y, float z) {
        m.put(name, new float[] {x, y, z});
    }

    public static Map<String, float[]> sample(
            int scene, int attack, float t, float limbSwing, float limbAmount, float age) {
        Map<String, float[]> m = new LinkedHashMap<>();
        float gait = (float) Math.sin(limbSwing * .7F) * Math.min(1, limbAmount * 3);
        float recoil = attack != 0 ? curve(t, 0, 0, 12, -1.5F, 18, -2, 21, 3.5F, 25, -1, 34, 0) : 0;

        rot(m, "pilot", gait * 3 + recoil, 0, -gait * 2);
        rot(m, "pilotHead", -recoil * .6F, gait * 2, 0);
        rot(m, "pilotLeftArm", gait * 5 + recoil * 2, 0, -5);
        rot(m, "pilotRightArm", -gait * 5 - recoil * 2, 0, 5);
        rot(m, "pilotLeftForearm", recoil * 3, 0, 0);
        rot(m, "pilotRightForearm", -recoil * 3, 0, 0);
        rot(m, "pilotLeftLeg", gait * 2, 0, 0);
        rot(m, "pilotRightLeg", -gait * 2, 0, 0);
        if (scene == 1 || scene == 2)
            rot(m, "pilot", scene == 1 ? -8 : curve(t, 0, 15, 10, -5, 30, 0), 0, 0);
        if (scene == 9) {
            rot(m, "chassis", curve(t, 0, -5, 12, 16, 32, 8, 44, -14, 52, 0), 0, 0);
            rot(m, "leftThigh", curve(t, 0, 35, 16, 65, 34, 20, 45, 42, 52, 12), 0, -5);
            rot(m, "rightThigh", curve(t, 0, 30, 16, 45, 34, 10, 45, 30, 52, 5), 0, 5);
            rot(m, "leftShin", curve(t, 0, -45, 20, -70, 36, -15, 45, -55, 52, -12), 0, 0);
            rot(m, "rightShin", curve(t, 0, -40, 20, -65, 36, -10, 45, -45, 52, -8), 0, 0);
            rot(m, "leftSwordArm", curve(t, 0, 0, 18, 25, 40, -12, 52, 0), 0, -12);
            rot(m, "rightSwordArm", curve(t, 0, 25, 18, 38, 40, 10, 52, 0), 0, 12);
            return m;
        }
        if (scene == 8) {
            rot(
                    m,
                    "rightSwordArm",
                    curve(t, 0, 0, 18, 120, 29, 125, 35, 15, 42, 5, 60, 40, 90, 25),
                    0,
                    curve(t, 0, 0, 18, -12, 35, 8, 60, 0));
            rot(m, "rightSwordElbow", curve(t, 0, 0, 24, 45, 35, 8, 60, 25, 90, 15), 0, 0);
            rot(m, "chassis", curve(t, 0, 0, 24, 8, 35, -18, 42, -12, 58, 0, 80, -5), 0, 0);
            rot(m, "leftThigh", curve(t, 0, 0, 54, 0, 60, 25, 80, 35), 0, -5);
            rot(m, "rightThigh", curve(t, 0, 0, 54, 0, 60, 18, 80, 30), 0, 5);
            rot(m, "leftShin", curve(t, 0, 0, 54, 0, 80, -45), 0, 0);
            rot(m, "rightShin", curve(t, 0, 0, 54, 0, 80, -40), 0, 0);
            return m;
        }
        if (scene == 2) {

            rot(
                    m,
                    "chassis",
                    curve(t, 0, -24, 3, -30, 12, -26, 24, -20, 48, 3, 62, -2, 80, 0),
                    0,
                    curve(t, 0, -3, 20, -3, 48, 0));
            rot(m, "leftThigh", curve(t, 0, 80, 16, 80, 32, 62, 48, 8, 58, 0), 0, -5);
            rot(m, "leftShin", curve(t, 0, -95, 16, -95, 32, -70, 50, -6, 60, 0), 0, 0);
            rot(m, "rightThigh", curve(t, 0, 15, 16, 15, 34, 22, 50, 2, 60, 0), 0, 5);
            rot(m, "rightShin", curve(t, 0, -90, 16, -90, 36, -45, 52, -3, 62, 0), 0, 0);
            rot(
                    m,
                    "leftSwordArm",
                    curve(t, 0, 24, 10, 30, 28, 20, 48, 0, 58, 28, 68, -18, 84, 0),
                    curve(t, 0, -12, 48, -12, 58, -25, 68, -10, 84, 0),
                    -12);
            rot(
                    m,
                    "rightSwordArm",
                    curve(t, 0, 18, 14, 25, 32, 16, 52, 0, 61, 28, 71, -18, 88, 0),
                    curve(t, 0, 12, 52, 12, 61, 25, 71, 10, 88, 0),
                    12);
            rot(
                    m,
                    "leftSwordElbow",
                    curve(t, 0, -32, 28, -32, 52, -8, 60, -45, 70, -18, 88, 0),
                    0,
                    0);
            rot(
                    m,
                    "rightSwordElbow",
                    curve(t, 0, -28, 32, -28, 55, -8, 64, -45, 74, -18, 92, 0),
                    0,
                    0);
            rot(
                    m,
                    "sensorArray",
                    curve(t, 0, 12, 16, 10, 36, -4, 58, 0),
                    curve(t, 0, -8, 30, -8, 48, 8, 72, 0),
                    0);
            ScoutEntrance.apply(m, t);
            return m;
        }
        if (scene == 3) {
            float reach =
                    curve(t, 0, 0, 18, 45, 32, 45, 46, 75, 78, 110, 224, 110, 242, 35, 260, 0);
            rot(m, "leftSwordArm", reach, -12, -8);
            rot(m, "leftSwordElbow", curve(t, 0, 0, 40, -35, 78, 15, 224, 15, 260, 0), 0, 0);
            rot(m, "chassis", curve(t, 0, 0, 45, 8, 78, -5, 224, -5, 260, 0), -8, 0);
            rot(m, "pilotHead", 0, -10, 0);
            for (String digit : new String[] {"Index", "Middle", "Ring", "Little", "Thumb"}) {
                float grip =
                        curve(t, 0, 65, 20, 65, 26, 0, 46, 0, 54, 55, 225, 55, 238, 0, 260, 65);
                rot(m, "left" + digit, -grip, 0, 0);
                rot(m, "left" + digit + "Tip", -grip * .8F, 0, 0);
            }
            return m;
        }
        if (scene == 6) {
            float toss = curve(t, 0, 0, 16, -25, 24, 65, 38, 10, 50, 0);
            rot(m, "leftSwordArm", toss, 0, -15);
            rot(m, "rightSwordArm", toss, 0, 15);
            float stomp = 0;
            if (t >= 48 && t < 128) {
                float c = (t - 48) % 24;
                stomp = curve(c, 0, 0, 10, -50, 16, 8, 24, 0);
            }
            rot(m, "rightThigh", stomp, 0, 0);
            rot(m, "rightShin", Math.max(0, -stomp) * .65F, 0, 0);
            rot(m, "chassis", -stomp * .15F, 0, 0);
            return m;
        }
        int a = attack;
        if (a == ScoutCombatPattern.RANGE_REEL) {
            float extend =
                    curve(
                            t, 0, 0, 10, -.2F, 18, 1, 50, 1, 78, .2F, 90, .7F, 102, 1, 112, .2F,
                            120, 0);
            float slam = curve(t, 0, 0, 82, 0, 90, -.4F, 102, 1, 112, .3F, 120, 0);
            rot(m, "chassis", -7 * extend + 14 * slam, 12 * extend, 0);
            rot(m, "viceArm", -75 * extend + 65 * slam, 10, -12);
            rot(m, "rightViceElbow", 30 - 26 * extend + 35 * slam, 0, 0);
            rot(m, "clawArm", -15 * extend, 0, 18);
            rot(m, "leftSwordArm", 12, -16, -12);
            rot(m, "rightSwordArm", 18, 16, 12);
            rot(m, "leftThigh", 12 * slam, 0, -5);
            rot(m, "rightThigh", 8 * slam, 0, 5);
            rot(m, "leftShin", -18 * slam, 0, 0);
            rot(m, "rightShin", -12 * slam, 0, 0);
            return m;
        }
        if (ScoutCombatPattern.extended(a)) {
            ScoutExpertPose.apply(m, a, t);
            return m;
        }
        if (a == ScoutCombatPattern.KATANA_CUTS || a == ScoutCombatPattern.KATANA_GLACIER) {
            ScoutKatanaPose.apply(m, attack, t);
            return m;
        }
        if (a < ScoutCombatPattern.BRAWL_FLURRY) {
            ScoutWeaponPose.apply(m, attack, t);
            return m;
        }
        if (a == ScoutCombatPattern.BRAWL_FLURRY && t < 62) {
            ScoutPunchPose.apply(m, t);
            if (t > 58) {
                Map<String, float[]> launch = sample(scene, attack, 62, limbSwing, limbAmount, age);
                float blend = curve(t, 58, 0, 62, 1);
                for (Map.Entry<String, float[]> entry : launch.entrySet()) {
                    float[] from = m.get(entry.getKey()), to = entry.getValue();
                    if (from != null)
                        for (int axis = 0; axis < 3; axis++)
                            from[axis] += (to[axis] - from[axis]) * blend;
                }
            }
            return m;
        }
        float left = 15, right = 15, body = 0, twist = 0, leftLeg = 0, rightLeg = 0, elbow = -30;
        if (a == ScoutCombatPattern.BRAWL_FLURRY) {
            if (t < 62) {
                float c = (t - 8) % 16;
                left = curve(c, 0, 20, 5, -15, 8, 100, 11, 78, 16, 20);
                right = curve((c + 8) % 16, 0, 20, 5, -15, 8, 100, 11, 78, 16, 20);
                twist = (left - right) * .12F;
                body = 8;
            } else {
                leftLeg =
                        rightLeg =
                                curve(
                                        t, 62, -15, 72, -50, 80, -30, 94, 0, 112, -20, 148, -30,
                                        174, 0);
                left =
                        curve(
                                t, 62, 15, 72, 165, 80, 25, 90, 20, 96, 80, 112, 125, 126, 40, 132,
                                130, 148, 35, 156, 160, 172, 65, 200, 15);
                right =
                        curve(
                                t, 62, 15, 72, 165, 80, 25, 96, 20, 132, 35, 148, 170, 156, 115,
                                172, 25, 200, 15);
                body =
                        curve(
                                t, 62, -15, 78, 45, 92, 0, 112, -15, 132, 15, 148, -20, 156, 35,
                                174, 0);
            }
        } else if (a == ScoutCombatPattern.BRAWL_SIEGE) {
            leftLeg = curve(t, 0, 0, 12, -35, 20, -95, 28, 0, 48, 0, 58, -60, 66, 20, 85, 0);
            rightLeg = curve(t, 0, 0, 28, -35, 38, -95, 46, 0, 58, -60, 66, 20, 85, 0);
            body =
                    curve(
                            t, 0, 0, 20, -15, 28, 0, 38, -15, 48, -25, 62, 75, 70, 15, 88, 0, 96,
                            40, 104, -15, 116, 0, 254, -20, 268, 40, 290, 0);
            left =
                    curve(
                            t, 0, 20, 48, 120, 64, 30, 92, 75, 104, 135, 116, 20, 136, -15, 146, 95,
                            166, 20, 174, 65, 254, 165, 270, 20, 300, 15);
            right =
                    curve(
                            t, 0, 20, 48, 120, 64, 30, 92, 75, 104, 80, 116, 20, 174, 65, 254, 165,
                            270, 20, 300, 15);
            if (t >= 184 && t < 250) {
                float c = (t - 184) % 26;
                float shot = curve(c, 0, 100, 5, 60, 12, 35, 21, -10, 26, 100);
                if (t < 210 || t >= 236) left = shot;
                else right = shot;
            }
        } else {
            float pull = curve(t, 0, 0, 18, -30, 28, 80, 45, 60, 130, 60, 150, 0);
            float swing = t >= 48 && t <= 128 ? (float) Math.cos((t - 48) / 20 * Math.PI) * 38 : 0;
            rot(m, "viceArm", pull, 0, swing);
            rot(m, "rightViceElbow", 20, 0, 0);
            twist = -swing * .35F;
            body = Math.abs(swing) * .2F;
            left = 40;
            right = 40;
        }

        rot(m, "leftSwordArm", left, -48, -8);
        rot(m, "rightSwordArm", right, 48, 8);
        rot(m, "leftSwordElbow", elbow + Math.max(0, left - 35) * .38F, 0, 0);
        rot(m, "rightSwordElbow", elbow + Math.max(0, right - 35) * .38F, 0, 0);
        rot(m, "chassis", -body, twist, 0);
        rot(m, "leftThigh", -leftLeg, 0, 0);
        rot(m, "rightThigh", -rightLeg, 0, 0);
        rot(m, "leftShin", -Math.max(0, -leftLeg) * .65F, 0, 0);
        rot(m, "rightShin", -Math.max(0, -rightLeg) * .65F, 0, 0);
        return m;
    }
}
