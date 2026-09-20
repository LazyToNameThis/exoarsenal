package com.scapeandrun.frostbite.client;

import com.scapeandrun.frostbite.entity.ScoutCombatPattern;

public final class ScoutHandPose {
    private static final float[] GRIP = {62, 70, 74, 78, 42};
    private static final float[] DELAY = {0, 1.1F, 2.2F, 3.4F, .7F};

    private ScoutHandPose() {}

    public static float wrist(int attack, float tick) {
        if (ScoutCombatPattern.extended(attack)) {
            float bend = 0;
            int[] contacts;
            switch (attack) {
                case ScoutCombatPattern.SWORD_UPROOT:
                    contacts = new int[] {20, 112, 140, 172, 204, 244};
                    break;
                case ScoutCombatPattern.KATANA_CHAIN:
                    contacts = new int[] {40, 76, 112, 142};
                    break;
                case ScoutCombatPattern.HAMMER_RICOCHET:
                    contacts = new int[] {18, 126, 250};
                    break;
                case ScoutCombatPattern.HAMMER_TORNADO:
                    contacts = new int[] {40, 72, 98, 120, 140, 218};
                    break;
                default:
                    contacts = new int[0];
            }
            for (int contact : contacts) bend += snap(tick, contact);
            return bend;
        }
        if (attack == ScoutCombatPattern.KATANA_CUTS) {
            if (tick >= 30 && tick < 110)
                return -40
                        * ScoutBrawlPose.curve(
                                (tick - 30) % 16, 0, 0, 2, .1F, 4, 1, 7, 1, 12, 0, 16, 0);
            return snap(tick, 130);
        }
        if (attack == ScoutCombatPattern.KATANA_GLACIER) {
            float bend = 0;
            for (int contact : new int[] {52, 116, 160})
                bend +=
                        -40
                                * ScoutBrawlPose.curve(
                                        tick,
                                        contact - 14,
                                        0,
                                        contact - 5,
                                        -.15F,
                                        contact,
                                        1,
                                        contact + 7,
                                        .7F,
                                        contact + 18,
                                        0);
            return bend;
        }
        if (attack == ScoutCombatPattern.CROSS) return snap(tick, 22) + snap(tick, 34) * .8F;
        float contact = 20;
        if (attack == ScoutCombatPattern.SWORD_RELAY || attack == ScoutCombatPattern.HAMMER_RELAY)
            contact = attack == ScoutCombatPattern.SWORD_RELAY ? 46 : 72;
        else if (attack == ScoutCombatPattern.SCISSOR_CHOPS)
            contact = 20 + 32 * Math.min(2, Math.max(0, (int) ((tick - 4) / 32)));
        else if (attack == ScoutCombatPattern.SWEEP_CW || attack == ScoutCombatPattern.SWEEP_CCW)
            contact = 26;
        else if (attack == ScoutCombatPattern.SPIN) return (float) Math.sin(tick * .22) * 9;
        else if (attack == 0 || attack == ScoutCombatPattern.STUN) return 0;
        return snap(tick, contact);
    }

    private static float snap(float tick, float contact) {
        float wind = smooth((tick - contact + 14) / 10);
        float strike = smooth((tick - contact + 3) / 5);
        float recover = smooth((tick - contact - 3) / 14);
        return -14 * wind + 32 * strike - 18 * recover;
    }

    public static float scissorSpread(int attack, float tick) {
        if (attack == ScoutCombatPattern.SCISSOR_TRENCH)
            return tick >= 30 && tick < 76
                    ? .62F
                    : tick >= 76 && tick < 98 ? .62F * (1 - smooth((tick - 76) / 20)) : .2F;
        if (attack == ScoutCombatPattern.SCISSOR_WALLS && tick >= 20 && tick < 212)
            return .05F + .5F * (1 - smooth(((tick - 20) % 24 - 5) / 8));
        if (attack == ScoutCombatPattern.SCISSOR_CHOPS) {
            float opening = 0;
            for (int contact : new int[] {20, 52, 84})
                opening =
                        Math.max(
                                opening,
                                smooth((tick - contact + 14) / 6)
                                        * (1 - smooth((tick - contact + 4) / 4)));
            return .04F + .46F * opening;
        }
        return .22F;
    }

    public static float scissorExtension(int attack, float tick) {
        return attack == ScoutCombatPattern.SCISSOR_SWEEP ? 1 + Math.min(1, tick / 20) * .3F : 1.3F;
    }

    private static float smooth(float x) {
        x = Math.max(0, Math.min(1, x));
        return x * x * (3 - 2 * x);
    }

    public static float opening(int attack, float tick, int digit) {
        float start = 11.5F, end = 0;
        if (attack == ScoutCombatPattern.SWORD_RELAY) end = 136;
        if (attack == ScoutCombatPattern.HAMMER_RELAY) end = 246;
        if (attack == ScoutCombatPattern.SCISSOR_HUNT) {
            start = 5.5F;
            end = 172;
        }
        if (attack == ScoutCombatPattern.THROW) end = 110;
        if (attack == ScoutCombatPattern.SWORD_UPROOT) {
            start = 14;
            end = tick < 90 ? 76 : 281;
            if (tick >= 76 && tick < 238) return 0;
        }
        if (attack == ScoutCombatPattern.HAMMER_RICOCHET) {
            start = 12;
            end = 228;
        }
        if (attack == ScoutCombatPattern.HAMMER_TORNADO) {
            start = 134;
            end = 186;
        }
        if (attack == ScoutCombatPattern.SCISSOR_TRENCH) {
            start = 104;
            end = 262;
        }
        if (attack == ScoutCombatPattern.SCISSOR_WALLS) {
            start = 210;
            end = 300;
        }
        if (end == 0) return 0;
        return smooth((tick - start - DELAY[digit]) / 7) * smooth((end - tick + DELAY[digit]) / 10);
    }

    public static float curl(int attack, float tick, int digit, boolean tip) {
        float open = opening(attack, tick, digit);
        return (tip ? GRIP[digit] * .85F : GRIP[digit]) * (1 - open);
    }

    public static float[] digit(int attack, float tick, int i, boolean tip, boolean right) {
        float open = right ? 0 : opening(attack, tick, i), grip = 1 - open;
        if (i == 4) {
            float sign = right ? -1 : 1;
            return tip
                    ? new float[] {
                        (float) Math.toDegrees(-.3F * grip),
                        0,
                        (float) Math.toDegrees(sign * .5F * grip)
                    }
                    : new float[] {
                        (float) Math.toDegrees(-.18F * grip),
                        (float) Math.toDegrees(sign * .65F * grip),
                        (float) Math.toDegrees(sign * (.75F * grip + .35F * open))
                    };
        }
        float contact = attack == ScoutCombatPattern.BASH ? 18 : 20;
        float pressure = attack != 0 ? Math.max(0, 1 - Math.abs(tick - contact - i * .6F) / 10) : 0;
        float bend =
                right
                        ? (tip ? 48 + i * 2 + pressure * 6 : 55 + i * 3 + pressure * 8)
                        : curl(attack, tick, i, tip);
        return new float[] {
            -bend, 0, tip || right ? 0 : (float) -Math.toDegrees((i - 1.5F) * .07F * open)
        };
    }
}
