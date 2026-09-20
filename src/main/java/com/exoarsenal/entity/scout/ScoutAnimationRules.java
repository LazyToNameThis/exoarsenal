package com.exoarsenal.entity.scout;

import com.exoarsenal.entity.ScoutCombatPattern;

public final class ScoutAnimationRules {
    private static final int NO_SCENE = 0;
    private static final int INTRO_FALL = 1;
    private static final int INTRO_RISE = 2;
    private static final int RAGE_AIR = 4;
    private static final int RAGE_LAND = 5;
    private static final int NO_ATTACK = 0;
    private static final int JUMP_MOVEMENT = 3;
    private static final int LAND_MOVEMENT = 5;
    private static final int HAMMER_TAKEOFF_TICK = 16;
    private static final int METEOR_DIVE_TICK = 38;

    private ScoutAnimationRules() {}

    public static String clip(int scene, int attack, int attackTick, int movement, boolean landed) {
        if (scene != NO_SCENE) {
            switch (scene) {
                case INTRO_FALL:
                    return "intro_fall";
                case INTRO_RISE:
                    return "intro_rise";
                case RAGE_AIR:
                    return "rage_air";
                case RAGE_LAND:
                    return "rage_land";
                default:
                    return "idle";
            }
        }
        if (attack == ScoutCombatPattern.LEAP && attackTick >= HAMMER_TAKEOFF_TICK) {
            if (landed) return "hammer_land";
            return attackTick >= METEOR_DIVE_TICK ? "meteor_dive" : "hammer_air";
        }
        return attack >= ScoutCombatPattern.SLASH
                ? ScoutCombatPattern.animation(attack)
                : movementClip(movement);
    }

    public static boolean loops(
            int scene, int attack, int attackTick, int movement, boolean landed) {
        if (scene != NO_SCENE) return scene == INTRO_FALL || scene == RAGE_AIR;
        if (attack == ScoutCombatPattern.LEAP && attackTick >= HAMMER_TAKEOFF_TICK) return !landed;
        return attack == ScoutCombatPattern.STUN
                || (attack == NO_ATTACK && movement != JUMP_MOVEMENT && movement != LAND_MOVEMENT);
    }

    private static String movementClip(int movement) {
        switch (movement) {
            case 1:
                return "run";
            case 2:
                return "walk";
            case JUMP_MOVEMENT:
                return "jump";
            case 4:
                return "fall";
            case LAND_MOVEMENT:
                return "land";
            case 6:
                return "turn_left";
            case 7:
                return "turn_right";
            default:
                return "idle";
        }
    }
}
