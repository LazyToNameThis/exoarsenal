package com.scapeandrun.frostbite.entity;

public final class ScoutCombatPattern {
    public static final int SLASH = 11,
            CROSS = 12,
            CHARGE = 13,
            SPIN = 14,
            STUN = 15,
            MORPH = 16,
            LEAP = 17,
            SWEEP_CW = 18,
            SWEEP_CCW = 19,
            BASH = 20,
            THROW = 21,
            MINIGUN = 22,
            CANNON = 23,
            VICE_SNAP = 24,
            VICE_CRUSH = 25,
            SWORD_RELAY = 26,
            HAMMER_RELAY = 27,
            SCISSOR_SWEEP = 28,
            SCISSOR_CHOPS = 29,
            SCISSOR_HUNT = 30,
            BRAWL_FLURRY = 31,
            BRAWL_SIEGE = 32,
            BRAWL_CHAIN = 33,
            KATANA_CUTS = 34,
            KATANA_GLACIER = 35,
            SWORD_UPROOT = 36,
            KATANA_CHAIN = 37,
            SCISSOR_WALLS = 38,
            SCISSOR_TRENCH = 39,
            HAMMER_RICOCHET = 40,
            HAMMER_TORNADO = 41,
            EXPERT_AERIAL = 42,
            EXPERT_PENDULUM = 43,
            EXPERT_BOXING = 44,
            EXPERT_DIVE = 45,
            EXPERT_WEB = 46,
            EXPERT_FREEFALL = 47,
            RANGE_REEL = 48;
    // Expert phase 1
    private static final int[] EXPERT_FIRST = {
        SLASH,
        CROSS,
        VICE_SNAP,
        CHARGE,
        SPIN,
        STUN,
        SWORD_RELAY,
        LEAP,
        HAMMER_RELAY,
        KATANA_GLACIER,
        SCISSOR_SWEEP,
        SCISSOR_CHOPS,
        SCISSOR_HUNT
    };
    // Expert phase 2
    private static final int[] EXPERT_SECOND = {
        SWORD_UPROOT,
        KATANA_CUTS,
        KATANA_CHAIN,
        SCISSOR_WALLS,
        SCISSOR_TRENCH,
        HAMMER_RICOCHET,
        HAMMER_TORNADO
    };

    public static boolean extended(int attack) {
        return attack >= SWORD_UPROOT && attack <= EXPERT_FREEFALL;
    }

    private static final int[][] EXPERT_CONTACTS = {
        {20, 44, 112, 140, 172, 204, 228, 244}, {40, 76, 112, 166, 178},
        {40, 64, 88, 112, 136, 160, 184, 208, 238, 260, 282}, {96, 130, 154, 178, 202, 226},
        {18, 48, 70, 96, 126, 250}, {40, 72, 98, 120, 140, 218},
        {18, 30, 42, 56, 150, 158, 166, 174}, {50, 100, 196},
        {18, 28, 44, 62, 80, 100, 116, 128, 140, 150, 188}, {62, 144, 210},
        {84, 112, 154, 180}, {24, 40, 58, 202}
    };

    public static float expertContactLight(int attack, float tick) {
        if (!extended(attack)) return 0;
        float light = 0;
        for (int contact : EXPERT_CONTACTS[attack - SWORD_UPROOT])
            light = Math.max(light, Math.max(0, 1 - Math.abs(tick - contact) / 8F));
        return light;
    }

    public static int expertAt(int phase, int cursor) {
        if (phase == 4) return EXPERT_AERIAL + Math.floorMod(cursor, 6);
        int[] pattern = phase == 2 ? EXPERT_FIRST : EXPERT_SECOND;
        return pattern[Math.floorMod(cursor, pattern.length)];
    }

    public static int plannedAttack(
            boolean expert, int phase, int cursor, boolean queuedVice, boolean nextViceSnap) {
        if (expert) return expertAt(phase, cursor);
        if (phase == 4) return phaseThreeAt(cursor);
        if (phase == 3) return phaseTwoAt(cursor);
        if (queuedVice) return nextViceSnap ? VICE_CRUSH : VICE_SNAP;
        return at(cursor);
    }

    public static int sequenceLength(boolean expert, int phase) {
        if (phase == 4) return expert ? 6 : 3;
        if (expert) return phase == 2 ? EXPERT_FIRST.length : EXPERT_SECOND.length;
        return phase == 3 ? SECOND.length : SEQUENCE.length;
    }

    public static int advanceCursor(boolean expert, int phase, int cursor, boolean queuedVice) {
        if (!expert && phase == 2 && queuedVice)
            return Math.floorMod(cursor, sequenceLength(false, phase));
        return (Math.floorMod(cursor, sequenceLength(expert, phase)) + 1)
                % sequenceLength(expert, phase);
    }

    public static boolean canStart(int planned, double distanceSquared) {
        return planned != 0 && (planned != SLASH || distanceSquared < 144);
    }

    public static int openingWeaponForm(int attack) {
        switch (attack) {
            case SWORD_UPROOT:
            case SLASH:
            case CROSS:
            case CHARGE:
            case SPIN:
            case SWORD_RELAY:
                return 0;
            case HAMMER_RICOCHET:
            case HAMMER_TORNADO:
            case LEAP:
            case SWEEP_CW:
            case SWEEP_CCW:
            case BASH:
            case THROW:
            case HAMMER_RELAY:
                return 1;
            case SCISSOR_WALLS:
            case SCISSOR_TRENCH:
            case SCISSOR_SWEEP:
            case SCISSOR_CHOPS:
            case SCISSOR_HUNT:
                return 2;
            case KATANA_CHAIN:
            case KATANA_CUTS:
            case KATANA_GLACIER:
                return 3;
            default:
                return -1;
        }
    }

    public static int offhandContact(int attack) {
        return attack == SLASH ? 32 : attack == CROSS ? 46 : attack == SCISSOR_SWEEP ? 50 : -1;
    }

    public static int phaseThreeAt(int cursor) {
        return BRAWL_FLURRY + Math.floorMod(cursor, 3);
    }

    public static int phaseForHealth(float health, float maximum) {
        return health <= maximum * .333F ? 4 : health <= maximum * .666F ? 3 : 2;
    }

    private static final int[] SEQUENCE = {
        SLASH, CROSS, CHARGE, SPIN, STUN, MORPH, LEAP, LEAP, SWEEP_CW, SWEEP_CCW, BASH, THROW
    };
    private static final int[] SECOND = {
        SLASH,
        CROSS,
        VICE_SNAP,
        CHARGE,
        SPIN,
        STUN,
        SWORD_RELAY,
        LEAP,
        HAMMER_RELAY,
        SCISSOR_SWEEP,
        SCISSOR_CHOPS,
        SCISSOR_HUNT
    };
    public static final int CHUNKS = 10, FRAGMENTS_PER_CHUNK = 3;

    public static int phaseTwoAt(int cursor) {
        return SECOND[Math.floorMod(cursor, SECOND.length)];
    }

    public static int phaseTwoLength() {
        return SECOND.length;
    }

    private ScoutCombatPattern() {}

    public static int at(int cursor) {
        return SEQUENCE[Math.floorMod(cursor, SEQUENCE.length)];
    }

    public static int length() {
        return SEQUENCE.length;
    }

    public static int duration(int attack) {
        switch (attack) {
            case RANGE_REEL:
                return 120;
            case SWORD_UPROOT:
                return 300;
            case KATANA_CHAIN:
                return 240;
            case SCISSOR_WALLS:
                return 310;
            case SCISSOR_TRENCH:
                return 280;
            case HAMMER_RICOCHET:
                return 280;
            case HAMMER_TORNADO:
                return 240;
            case EXPERT_AERIAL:
                return 230;
            case EXPERT_PENDULUM:
                return 260;
            case EXPERT_BOXING:
                return 210;
            case EXPERT_DIVE:
                return 250;
            case EXPERT_WEB:
                return 260;
            case EXPERT_FREEFALL:
                return 300;
            case KATANA_CUTS:
                return 440;
            case KATANA_GLACIER:
                return 230;
            case BRAWL_FLURRY:
                return 200;
            case BRAWL_SIEGE:
                return 300;
            case BRAWL_CHAIN:
                return 160;
            case SLASH:
                return 42;
            case CROSS:
                return 56;
            case CHARGE:
                return 58;
            case SPIN:
                return 64;
            case STUN:
                return 40;
            case MORPH:
                return 32;
            case LEAP:
                return 76;
            case SWEEP_CW:
            case SWEEP_CCW:
                return 48;
            case BASH:
                return 40;
            case THROW:
                return 120;
            case MINIGUN:
                return 64;
            case CANNON:
                return 64;
            case VICE_SNAP:
                return 54;
            case VICE_CRUSH:
                return 66;
            case SWORD_RELAY:
                return 150;
            case HAMMER_RELAY:
                return 260;
            case SCISSOR_SWEEP:
                return 64;
            case SCISSOR_CHOPS:
                return 112;
            case SCISSOR_HUNT:
                return 180;
            default:
                return 1;
        }
    }

    public static String animation(int attack) {
        switch (attack) {
            case RANGE_REEL:
                return "idle";
            case KATANA_CUTS:
            case KATANA_GLACIER:
                return "idle";
            case BRAWL_FLURRY:
            case BRAWL_SIEGE:
            case BRAWL_CHAIN:
                return "idle";
            case SLASH:
                return "guard_slash";
            case CROSS:
                return "guard_cross";
            case CHARGE:
                return "shield_charge";
            case SPIN:
                return "sword_spin";
            case STUN:
                return "guard_stun";
            case MORPH:
                return "hammer_morph";
            case LEAP:
                return "hammer_leap";
            case SWEEP_CW:
                return "hammer_clockwise";
            case SWEEP_CCW:
                return "hammer_counterclockwise";
            case BASH:
                return "shield_bash";
            case THROW:
                return "hammer_throw";
            case MINIGUN:
                return "phase_minigun";
            case CANNON:
                return "phase_cannon";
            case VICE_SNAP:
                return "vice_snap";
            case VICE_CRUSH:
                return "vice_crush";
            case SWORD_RELAY:
                return "sword_relay";
            case HAMMER_RELAY:
                return "hammer_relay";
            case SCISSOR_SWEEP:
                return "scissor_sweep";
            case SCISSOR_CHOPS:
                return "scissor_chops";
            case SCISSOR_HUNT:
                return "scissor_hunt";
            default:
                return "idle";
        }
    }

    public static int stunEnd(boolean broken) {
        return broken ? 80 : 40;
    }

    public static boolean canBreakGuard(int attack, int tick, boolean broken) {
        return attack == STUN && tick >= 0 && tick < 40 && !broken;
    }

    public static double hexRadius(double x, double z) {
        double result = 0;
        for (int i = 0; i < 6; i++) {
            double a = Math.PI / 6 + i * Math.PI / 3;
            result = Math.max(result, x * Math.cos(a) + z * Math.sin(a));
        }
        return result;
    }
}
