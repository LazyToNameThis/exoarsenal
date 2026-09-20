package com.exoarsenal.client;

import com.exoarsenal.entity.ScoutCombatPattern;
import java.util.Map;

public final class ScoutWeaponPose {
    private ScoutWeaponPose() {}

    private static float c(float t, float... k) {
        return ScoutBrawlPose.curve(t, k);
    }

    private static void r(Map<String, float[]> out, String n, float x, float y, float z) {
        out.put(n, new float[] {x, y, z});
    }

    public static void apply(Map<String, float[]> out, int attack, float t) {
        float arm = 0, yaw = 0, roll = 0, elbow = 55, torso = 0, twist = 0, shield = 0;
        switch (attack) {
            case ScoutCombatPattern.SLASH:
                arm = c(t, 0, 0, 12, 45, 17, 48, 20, -12, 24, -24, 31, -8, 42, 0);
                yaw = c(t, 0, 0, 12, 28, 17, 26, 20, -38, 24, -52, 32, -18, 42, 0);
                roll = c(t, 0, 0, 14, -18, 20, 16, 26, 22, 42, 0);
                elbow = c(t, 0, 55, 14, 82, 20, 22, 25, 16, 34, 42, 42, 55);
                torso = c(t, 0, 0, 14, 5, 20, -9, 25, -12, 42, 0);
                twist = c(t, 0, 0, 14, 16, 20, -18, 25, -23, 42, 0);
                break;
            case ScoutCombatPattern.CROSS:
                arm = c(t, 0, 0, 14, 50, 19, 52, 22, -12, 26, -20, 30, 32, 34, -18, 39, -26, 56, 0);
                yaw = c(t, 0, 0, 14, 32, 19, 28, 22, -40, 26, -48, 30, -42, 34, 35, 39, 43, 56, 0);
                roll = c(t, 0, 0, 18, -22, 22, 25, 28, 28, 32, 24, 34, -24, 40, -30, 56, 0);
                elbow = c(t, 0, 55, 16, 85, 22, 24, 27, 55, 31, 68, 34, 20, 40, 30, 56, 55);
                torso = c(t, 0, 0, 18, 4, 22, -10, 28, -4, 34, -12, 40, -7, 56, 0);
                twist = c(t, 0, 0, 18, 18, 22, -24, 29, -18, 34, 24, 40, 18, 56, 0);
                break;
            case ScoutCombatPattern.SWEEP_CW:
            case ScoutCombatPattern.SWEEP_CCW:
                float sign = attack == ScoutCombatPattern.SWEEP_CW ? 1 : -1;
                arm = c(t, 0, 0, 14, 20, 21, -12, 31, -16, 37, -8, 48, 0);
                yaw = sign * c(t, 0, 0, 14, 50, 21, 44, 26, 0, 31, -52, 36, -58, 48, 0);
                elbow = c(t, 0, 55, 14, 68, 21, 25, 31, 18, 38, 34, 48, 55);
                roll = sign * c(t, 0, 0, 14, -14, 26, 0, 34, 18, 48, 0);
                torso = c(t, 0, 0, 15, -4, 26, -10, 34, -12, 48, 0);
                twist = sign * c(t, 0, 0, 15, 25, 26, 0, 34, -30, 48, 0);
                break;
            case ScoutCombatPattern.BASH:
                shield = c(t, 0, 0, 10, 38, 15, 42, 18, 90, 21, 96, 28, 42, 40, 0);
                torso = c(t, 0, 0, 12, 6, 18, -14, 22, -16, 40, 0);
                twist = c(t, 0, 0, 12, -12, 18, 12, 25, 15, 40, 0);
                arm = c(t, 0, 0, 12, 12, 22, 18, 40, 0);
                break;
            case ScoutCombatPattern.CHARGE:
                shield = c(t, 0, 0, 12, 55, 20, 78, 38, 78, 43, 88, 50, 35, 58, 0);
                torso = c(t, 0, 0, 12, -8, 20, -16, 38, -16, 43, -20, 58, 0);
                arm = c(t, 0, 0, 16, 20, 38, 20, 58, 0);
                yaw = c(t, 0, 0, 16, 18, 38, 18, 58, 0);
                break;
            case ScoutCombatPattern.SPIN:
                arm = c(t, 0, 0, 14, 62, 22, 80, 46, 80, 54, 36, 64, 0);
                yaw = c(t, 0, 0, 14, 50, 22, 55, 28, 0, 34, -55, 40, 0, 46, 55, 54, 20, 64, 0);
                roll = c(t, 0, 0, 22, -10, 28, 30, 34, 55, 40, 25, 46, -10, 64, 0);
                elbow = c(t, 0, 55, 16, 40, 22, 18, 46, 18, 56, 45, 64, 55);
                twist = c(t, 0, 0, 22, 18, 28, 0, 34, -18, 40, 0, 46, 18, 64, 0);
                torso = -6 * c(t, 0, 0, 16, 1, 50, 1, 64, 0);
                break;
            case ScoutCombatPattern.STUN:
                torso = c(t, 0, -12, 6, -22, 14, -18, 60, -18, 80, 0);
                arm = c(t, 0, 0, 8, -10, 60, -10, 80, 0);
                elbow = 30;
                break;
            case ScoutCombatPattern.MORPH:
                arm = c(t, 0, 0, 10, 50, 16, 55, 22, 48, 32, 0);
                elbow = c(t, 0, 55, 10, 75, 16, 78, 24, 70, 32, 55);
                yaw = c(t, 0, 0, 10, -20, 22, -20, 32, 0);
                break;
            case ScoutCombatPattern.THROW:
                arm =
                        c(
                                t, 0, 0, 14, 42, 18, 45, 22, -18, 28, -26, 42, 25, 72, 25, 82, 65,
                                100, 45, 120, 0);
                elbow =
                        c(
                                t, 0, 55, 14, 80, 22, 18, 28, 12, 42, 40, 72, 40, 82, 25, 100, 65,
                                120, 55);
                yaw = c(t, 0, 0, 14, 22, 22, -20, 28, -26, 44, -8, 100, -8, 120, 0);
                torso = c(t, 0, 0, 14, 6, 22, -12, 30, -14, 48, 0, 82, -5, 100, 4, 120, 0);
                break;
            case ScoutCombatPattern.SWORD_RELAY:
                arm =
                        c(
                                t, 0, 0, 14, 40, 22, -15, 30, -22, 38, 38, 42, 42, 46, -18, 51, -28,
                                65, 18, 104, 35, 132, 35, 150, 0);
                elbow =
                        c(
                                t, 0, 55, 14, 82, 22, 18, 30, 18, 40, 70, 46, 15, 52, 10, 68, 40,
                                132, 40, 150, 55);
                yaw =
                        c(
                                t, 0, 0, 16, 24, 22, -22, 32, -28, 40, -38, 46, 28, 54, 36, 72, -8,
                                132, -8, 150, 0);
                torso = c(t, 0, 0, 16, 5, 22, -10, 34, 0, 42, 8, 46, -16, 54, -20, 72, 0, 150, 0);
                break;
            case ScoutCombatPattern.HAMMER_RELAY:
                arm =
                        c(
                                t, 0, 0, 14, 44, 22, -18, 30, -26, 52, 30, 64, 40, 68, 46, 72, -22,
                                80, -30, 100, 20, 214, 25, 240, 42, 260, 0);
                elbow =
                        c(
                                t, 0, 55, 14, 82, 22, 18, 32, 16, 56, 48, 68, 80, 72, 15, 80, 10,
                                100, 40, 240, 40, 260, 55);
                yaw =
                        c(
                                t, 0, 0, 14, 24, 22, -22, 32, -30, 56, -12, 68, 28, 72, -32, 82,
                                -40, 108, -8, 240, -8, 260, 0);
                torso =
                        c(
                                t, 0, 0, 14, 6, 22, -12, 32, -15, 52, 0, 68, 8, 72, -18, 82, -22,
                                110, 0, 260, 0);
                break;
            case ScoutCombatPattern.SCISSOR_HUNT:
                arm = c(t, 0, 0, 10, 32, 16, -12, 24, -20, 40, 26, 144, 26, 160, 38, 180, 0);
                elbow = c(t, 0, 55, 10, 74, 16, 18, 24, 14, 42, 40, 160, 40, 180, 55);
                yaw = c(t, 0, 0, 10, 20, 16, -20, 24, -28, 44, -10, 160, -10, 180, 0);
                torso = c(t, 0, 0, 10, 5, 16, -10, 24, -12, 44, 0, 180, 0);
                break;
            case ScoutCombatPattern.SCISSOR_SWEEP:
                arm = c(t, 0, 0, 16, 24, 24, -12, 36, -18, 46, -8, 64, 0);
                elbow = c(t, 0, 55, 16, 64, 24, 22, 36, 16, 48, 35, 64, 55);
                yaw = c(t, 0, 0, 16, 48, 24, 40, 30, 0, 36, -48, 44, -56, 64, 0);
                twist = c(t, 0, 0, 18, 20, 30, 0, 38, -26, 64, 0);
                torso = c(t, 0, 0, 18, -4, 30, -12, 42, -14, 64, 0);
                break;
            case ScoutCombatPattern.SCISSOR_CHOPS:
                arm =
                        c(
                                t, 0, 0, 12, 32, 17, 36, 20, -16, 25, -24, 36, 4, 44, 32, 49, 36,
                                52, -16, 57, -24, 68, 4, 76, 38, 81, 42, 84, -20, 89, -28, 112, 0);
                elbow =
                        c(
                                t, 0, 55, 12, 72, 20, 18, 26, 12, 36, 55, 44, 72, 52, 18, 58, 12,
                                68, 55, 76, 78, 84, 16, 90, 10, 112, 55);
                torso =
                        c(
                                t, 0, 0, 16, 3, 20, -14, 26, -17, 38, 0, 48, 3, 52, -14, 58, -17,
                                70, 0, 80, 5, 84, -18, 90, -22, 112, 0);
                yaw =
                        c(
                                t, 0, 0, 12, 12, 20, -12, 36, 0, 44, -12, 52, 12, 68, 0, 76, 10, 84,
                                -10, 112, 0);
                break;
            default:
                return;
        }
        r(out, "leftSwordArm", arm, yaw, roll);
        r(out, "leftSwordElbow", elbow, 0, 8);
        r(out, "chassis", torso, twist, 0);
        r(out, "rightSwordArm", shield, -shield * .25F, 0);
        r(out, "rightSwordElbow", shield * .25F, 0, -8);

        r(out, "pilot", -torso * .18F, 0, 0);
        r(out, "pilotHead", torso * .12F, -twist * .2F, 0);
    }
}
