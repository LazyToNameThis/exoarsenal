package com.scapeandrun.frostbite.combat;

public final class CombatRules {
    public static final float MAX_STAMINA = 100;
    public static final int INPUT_BUFFER_TICKS = 5;
    public static final int REGEN_DELAY_TICKS = 24;
    public static final int PARRY_REGEN_DELAY_TICKS = 10;
    private static final float STAMINA_PER_TICK = 1.25F;
    private static final float PARRY_STAMINA_REWARD = 12;
    private static final int DODGE_COST = 24;
    private static final int PARRY_COST = 15;
    private static final int DODGE_DURATION = 16;
    private static final int PARRY_DURATION = 18;
    private static final int STAGGER_DURATION = 16;
    private static final int COMBO_RESET_TICKS = 22;
    private static final int COMBO_LENGTH = 3;

    private CombatRules() {}

    public static boolean dodgeActive(int action, int tick) {
        return action == WeaponCombat.DODGE && tick >= 2 && tick <= 7;
    }

    public static boolean parryActive(int action, int tick, boolean brawler) {
        int firstTick = brawler ? 1 : 3;
        int lastTick = brawler ? 11 : 7;
        return action == WeaponCombat.PARRY && tick >= firstTick && tick <= lastTick;
    }

    public static int staminaCost(int action, WeaponDiscipline discipline) {
        if (action == WeaponCombat.DODGE) return DODGE_COST;
        if (action == WeaponCombat.PARRY) return PARRY_COST;
        return discipline.cost(action == WeaponCombat.HEAVY);
    }

    public static int duration(int action, WeaponDiscipline discipline) {
        switch (action) {
            case WeaponCombat.DODGE:
                return DODGE_DURATION;
            case WeaponCombat.PARRY:
                return PARRY_DURATION;
            case WeaponCombat.STAGGER:
                return STAGGER_DURATION;
            default:
                return discipline.duration(action == WeaponCombat.HEAVY);
        }
    }

    public static int nextCombo(long ticksSinceLastAction, int combo) {
        return ticksSinceLastAction < COMBO_RESET_TICKS ? (combo + 1) % COMBO_LENGTH : 0;
    }

    public static float regenerate(float stamina) {
        return Math.min(MAX_STAMINA, stamina + STAMINA_PER_TICK);
    }

    public static float rewardParry(float stamina) {
        return Math.min(MAX_STAMINA, stamina + PARRY_STAMINA_REWARD);
    }
}
