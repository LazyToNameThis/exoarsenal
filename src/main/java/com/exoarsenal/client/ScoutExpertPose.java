package com.exoarsenal.client;

import com.exoarsenal.entity.ScoutCombatPattern;
import java.util.Map;

public final class ScoutExpertPose {
    private ScoutExpertPose() {}

    private static float c(float t, float... keys) {
        return ScoutBrawlPose.curve(t, keys);
    }

    private static void r(Map<String, float[]> m, String bone, float x, float y, float z) {
        m.put(bone, new float[] {x, y, z});
    }

    private static float beat(float t, float contact) {
        return c(
                t,
                contact - 14,
                0,
                contact - 5,
                -.28F,
                contact,
                1,
                contact + 7,
                .55F,
                contact + 18,
                0);
    }

    public static void apply(Map<String, float[]> m, int attack, float t) {
        float left = 35,
                right = 35,
                elbow = 35,
                body = 0,
                twist = 0,
                lowerLeft = 25,
                lowerRight = 25;
        float leftYaw = -20,
                rightYaw = 20,
                leftLeg = 5,
                rightLeg = 5,
                rightElbow = 35,
                lowerElbow = 28;
        switch (attack) {
            case ScoutCombatPattern.SWORD_UPROOT:
                left =
                        c(
                                t, 0, 35, 12, 130, 20, 72, 32, 35, 62, 35, 76, 155, 96, 160, 112,
                                25, 126, 25, 135, 65, 140, 32, 166, 72, 172, 32, 196, 100, 204, 150,
                                218, 40, 236, 125, 244, 65, 260, 30, 281, 35, 300, 35);
                elbow =
                        c(
                                t, 0, 35, 12, 62, 20, 8, 40, 30, 90, 15, 112, 5, 126, 5, 135, 65,
                                140, 8, 166, 65, 172, 8, 196, 85, 204, 25, 218, 35);
                body =
                        c(
                                t, 0, 0, 12, -8, 20, 12, 40, 0, 96, -18, 112, 25, 135, -8, 140, 18,
                                166, -8, 172, 18, 196, -15, 204, -22, 220, 0);
                right = 35 + 60 * beat(t, 228);
                rightYaw = 20 + 65 * beat(t, 228);
                break;
            case ScoutCombatPattern.KATANA_CHAIN:
                if (t >= 24 && t < 132) {
                    float p = (t - 24) % 36;
                    left = c(p, 0, 25, 8, 22, 12, 38, 16, 85, 21, 95, 30, 25, 36, 25);
                    leftYaw = c(p, 0, -75, 12, -75, 18, 65, 25, 40, 36, -75);
                    body = c(p, 0, 0, 10, 20, 18, 12, 28, 0);
                    right = 105;
                    elbow = 12;
                } else {
                    left = 35 + 55 * beat(t, 142);
                    leftYaw = -20 + 90 * beat(t, 142);
                }
                lowerRight = 65;
                break;
            case ScoutCombatPattern.SCISSOR_WALLS:
                if (t >= 20 && t < 212) {
                    float p = (t - 20) % 24;
                    left = 50 + 40 * beat(p, 12);
                    right = 50 + 40 * beat(p, 12);
                    leftYaw = -45 + 40 * beat(p, 12);
                    rightYaw = 45 - 40 * beat(p, 12);
                    body = 8 * beat(p, 12);
                } else {
                    left = c(t, 212, 50, 216, 120, 230, 80, 250, 60, 284, 100, 300, 35, 310, 35);
                    right = 35 + 35 * beat(t, 250);
                }
                break;
            case ScoutCombatPattern.SCISSOR_TRENCH:
                left =
                        c(
                                t, 0, 35, 20, 135, 30, 25, 76, 25, 88, 65, 96, 30, 110, 145, 120,
                                100, 240, 100, 262, 35, 280, 35);
                elbow = c(t, 0, 35, 24, 10, 76, 10, 96, 60, 110, 15, 240, 30, 262, 35);
                body = c(t, 0, 0, 30, 22, 76, 22, 100, 0);
                if (t >= 32 && t < 76) {
                    leftLeg = (float) Math.sin(t * .55) * 25;
                    rightLeg = -leftLeg;
                }
                right = 50;
                break;
            case ScoutCombatPattern.HAMMER_RICOCHET:
                left =
                        c(
                                t, 0, 35, 10, 125, 18, 165, 30, 50, 104, 50, 118, 45, 126, 90, 140,
                                35, 210, 90, 228, 110, 242, 135, 250, 55, 265, 40, 280, 35);
                right = 35 + 55 * beat(t, 48) + 65 * beat(t, 96);
                lowerLeft = 25 + 70 * beat(t, 70);
                body =
                        c(
                                t, 0, 0, 10, 15, 18, -15, 30, 0, 118, -12, 126, 18, 145, 0, 228,
                                -20, 242, -10, 250, 18, 280, 0);
                twist = 75 * beat(t, 250);
                leftYaw = -20 + 90 * beat(t, 250);
                break;
            case ScoutCombatPattern.HAMMER_TORNADO:
                left = 75;
                elbow = 12;
                leftYaw = -65;
                body =
                        c(
                                t, 0, 0, 20, 8, 120, 18, 140, 12, 162, 28, 186, -20, 204, -12, 218,
                                30, 240, 0);
                if (t >= 20 && t < 132) {
                    twist = (float) Math.sin((t - 20) * .15) * 16;
                    lowerLeft = 55;
                    lowerRight = 55;
                }
                if (t >= 162) {
                    left = c(t, 162, 90, 186, 150, 204, 165, 218, 35, 240, 35);
                    right = left;
                    lowerLeft = left * .75F;
                    lowerRight = lowerLeft;
                }
                break;
            case ScoutCombatPattern.EXPERT_AERIAL:
                if (t < 62) {
                    ScoutPunchPose.apply(m, t);
                    return;
                }
                left = c(t, 62, 65, 78, 110, 92, 140, 110, 95, 124, 135, 150, 40, 180, 35, 230, 35);
                right = c(t, 62, 40, 92, 45, 112, 130, 124, 70, 140, 150, 150, 35, 180, 35);
                body = c(t, 62, 0, 92, -15, 124, 10, 140, -15, 150, 28, 180, 0);
                lowerLeft = left * .7F;
                lowerRight = right * .7F;
                leftLeg = 30;
                rightLeg = 20;
                break;
            case ScoutCombatPattern.EXPERT_PENDULUM:
                lowerLeft = 125;
                lowerRight = 125;
                left = 100;
                right = 100;
                elbow = 8;
                body =
                        c(
                                t, 0, 0, 28, -25, 70, 15, 110, -20, 132, 0, 162, -25, 196, 28, 220,
                                0, 260, 0);
                leftLeg = 30;
                rightLeg = 20;
                leftYaw = -35;
                rightYaw = 35;
                if (t > 210) {
                    left = right = 35;
                    lowerLeft = lowerRight = 25;
                    leftLeg = rightLeg = 5;
                }
                break;
            case ScoutCombatPattern.EXPERT_BOXING:
                float jab = beat(t, 18) + beat(t, 28),
                        cross = beat(t, 44),
                        upper = beat(t, 80),
                        hook = beat(t, 100);
                left = 45 + 40 * jab + 55 * upper + 35 * beat(t, 116) + 45 * beat(t, 140);
                right = 45 + 45 * cross + 35 * hook + 45 * beat(t, 128) + 70 * beat(t, 150);
                leftYaw = -25 + 40 * hook;
                rightYaw = 25 - 50 * cross;
                elbow = 35 - 30 * Math.max(jab, beat(t, 116));
                rightElbow = 35 - 30 * Math.max(cross, beat(t, 128));
                lowerLeft = 35 + 55 * beat(t, 128) + 60 * beat(t, 150);
                lowerRight = 35 + 55 * beat(t, 140);
                twist = 18 * jab - 30 * cross + 30 * hook;
                body = -8 * jab + 12 * cross;
                rightLeg = 5 + 75 * beat(t, 62);
                if (t >= 164) {
                    float drive = c(t, 164, 0, 174, -.45F, 181, -.5F, 188, 1, 197, .8F, 210, 0);
                    left = right = 45 + 25 * drive;
                    lowerLeft = lowerRight = 35 + 35 * drive;
                    elbow = rightElbow = 35 - 35 * drive;
                    lowerElbow = 28 * (1 - drive);
                    leftYaw = -20 * (1 - drive);
                    rightYaw = 20 * (1 - drive);
                    body = 20 * drive;
                }
                break;
            case ScoutCombatPattern.EXPERT_DIVE:
                left =
                        c(
                                t, 0, 35, 44, 85, 70, 80, 84, 40, 124, 145, 144, 35, 182, 160, 194,
                                170, 210, 30, 236, 25, 250, 35);
                right =
                        c(
                                t, 0, 35, 44, 40, 70, 40, 84, 95, 124, 145, 144, 35, 182, 160, 194,
                                170, 210, 30, 236, 25, 250, 35);
                lowerLeft = left * .8F;
                lowerRight = right * .8F;
                body =
                        c(
                                t, 0, 0, 44, 45, 70, 30, 84, 45, 110, 20, 144, 25, 170, 0, 194, -30,
                                210, 32, 250, 0);
                leftLeg = rightLeg = 22;
                break;
            case ScoutCombatPattern.EXPERT_WEB:
                lowerLeft = lowerRight = 110;
                left = 75;
                right = 75;
                leftYaw = -55;
                rightYaw = 55;
                body = c(t, 0, 0, 62, -10, 116, 10, 144, 35, 160, 12, 180, -15, 210, -20, 245, 0);
                if (t >= 132 && t < 170) {
                    right = 90;
                    elbow = 5;
                }
                if (t > 230) {
                    left = right = 35;
                    lowerLeft = lowerRight = 25;
                }
                break;
            case ScoutCombatPattern.EXPERT_FREEFALL:
                left =
                        c(
                                t, 0, 35, 16, 145, 24, 30, 40, 85, 58, 40, 80, 145, 108, 160, 140,
                                50, 170, 105, 188, 130, 202, 25, 270, 25, 300, 35);
                right =
                        c(
                                t, 0, 35, 16, 145, 24, 30, 40, 45, 58, 105, 80, 145, 108, 160, 140,
                                50, 170, 50, 188, 140, 202, 25, 270, 25, 300, 35);
                lowerLeft = left * .8F;
                lowerRight = right * .8F;
                body =
                        c(
                                t, 0, 0, 16, -15, 24, 25, 40, 20, 80, -20, 108, -30, 140, 10, 170,
                                0, 188, -25, 202, 32, 270, 20, 300, 0);
                leftLeg = rightLeg = 20;
                break;
            default:
                return;
        }
        r(m, "chassis", body, twist, 0);
        r(m, "leftSwordArm", left, leftYaw, -8);
        r(m, "rightSwordArm", right, rightYaw, 8);
        r(m, "leftSwordElbow", elbow, 0, 5);
        r(m, "rightSwordElbow", rightElbow, 0, -5);
        r(m, "clawArm", lowerLeft, -28, -12);
        r(m, "viceArm", lowerRight, 28, 12);
        r(m, "leftViceElbow", lowerElbow, 0, 0);
        r(m, "rightViceElbow", lowerElbow, 0, 0);
        r(m, "leftThigh", leftLeg, 0, -5);
        r(m, "rightThigh", rightLeg, 0, 5);
        r(m, "leftShin", -Math.abs(leftLeg) * .7F, 0, 0);
        r(m, "rightShin", -Math.abs(rightLeg) * .7F, 0, 0);
        r(m, "pilot", -body * .18F, -twist * .12F, 0);
        r(m, "pilotHead", body * .1F, 0, 0);
    }
}
