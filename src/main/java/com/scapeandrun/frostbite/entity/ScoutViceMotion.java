package com.scapeandrun.frostbite.entity;

public final class ScoutViceMotion {
    private ScoutViceMotion() {}

    private static double smooth(double t) {
        t = Math.max(0, Math.min(1, t));
        return t * t * (3 - 2 * t);
    }

    public static double extension(int attack, double t, boolean right) {
        if (attack == ScoutCombatPattern.EXPERT_PENDULUM) {
            double release = right ? 92 : 192;
            return smooth((t - 12) / 16) * (1 - smooth((t - release) / 18));
        }
        if (attack == ScoutCombatPattern.EXPERT_WEB)
            return smooth((t - 12) / 18) * (1 - smooth((t - 208) / 20));
        if (attack == ScoutCombatPattern.EXPERT_FREEFALL && right)
            return smooth((t - 128) / 12) * (1 - smooth((t - 154) / 12));
        return 0;
    }

    public static double closure(int attack, double t, boolean right) {
        double deployed = extension(attack, t, right);
        return smooth((deployed - .8) / .2);
    }
}
