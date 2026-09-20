package com.scapeandrun.frostbite.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ExcavatorClassicScore {
    public enum Attack {
        SEISMIC_BORE(204),
        TRENCH_CUTTER(240),
        SURVEYING_PROBES(172),
        STRIP_MINE(182),
        TURRET_SWEEP(216),
        CORE_SAMPLE(208),
        FAULT_LINE(170),
        PROBE_MINEFIELD(234),
        BORE_RICOCHET(164),
        INDUSTRIAL_PROCESSING(390),
        FINAL_CHARGE(172);
        public final int duration;

        Attack(int duration) {
            this.duration = duration;
        }
    }

    public enum Action {
        BURROW,
        SURFACE_CRACK,
        CIRCLE,
        LOCK_TARGET,
        ERUPT,
        DIVE,
        SHOCKWAVE,
        TRENCH_STRAIGHT,
        TRENCH_DIAGONAL,
        TRENCH_CURVED,
        TRENCH_BURST,
        LAUNCH_PROBES,
        SCAN,
        LOCK_PROBES,
        FIRE_RECORDED_SHOTS,
        LIFT_CHUNK,
        AIM_CHUNK,
        SHOOT_CHUNK,
        DRILL_LAUNCH_CHUNK,
        ANCHOR,
        HORIZONTAL_SWEEP,
        CROSSING_SWEEPS,
        CLOSE_CORRIDOR,
        RETRACT,
        CHARGE,
        CUT_PLATFORM,
        LIFT_PLATFORM,
        SHATTER_PLATFORM,
        UPWARD_FIRE,
        FAULT_WARNING,
        FAULT_ERUPTION,
        PLANT_PROBES,
        ARM_PROBE,
        RECALL_PROBES,
        REDIRECT_RIGHT,
        REDIRECT_DIAGONAL,
        TUNNEL_RELOCATE,
        MARK_SITES,
        PROCESS_SITE,
        FAKE_ERUPTION,
        RECONNECT,
        METEOR_BORE,
        DRILL_CLICK,
        DRILL_SPUTTER,
        DRILL_RESTART,
        DRILL_STALL,
        RECOVER,
        RETREAT,
        FEED_DRILL,
        EMBED_ON_MISS
    }

    public static final class Cue {
        public final int tick, index;
        public final Action action;

        private Cue(int tick, Action action, int index) {
            this.tick = tick;
            this.action = action;
            this.index = index;
        }
    }

    private static void cue(List<Cue> list, int tick, Action action) {
        cue(list, tick, action, 0);
    }

    private static void cue(List<Cue> list, int tick, Action action, int index) {
        list.add(new Cue(tick, action, index));
    }

    public static List<Cue> timeline(Attack attack) {
        List<Cue> c = new ArrayList<>();
        switch (attack) {
            case SEISMIC_BORE:
                cue(c, 1, Action.BURROW);
                cue(c, 12, Action.SURFACE_CRACK);
                cue(c, 20, Action.CIRCLE);
                cue(c, 85, Action.LOCK_TARGET);
                cue(c, 100, Action.SURFACE_CRACK);
                cue(c, 120, Action.ERUPT);
                cue(c, 160, Action.DIVE);
                cue(c, 185, Action.SHOCKWAVE);
                break;
            case TRENCH_CUTTER:
                cue(c, 20, Action.TRENCH_STRAIGHT);
                cue(c, 55, Action.TRENCH_BURST, 0);
                cue(c, 100, Action.TRENCH_DIAGONAL);
                cue(c, 135, Action.TRENCH_BURST, 1);
                cue(c, 180, Action.TRENCH_CURVED);
                cue(c, 215, Action.TRENCH_BURST, 2);
                break;
            case SURVEYING_PROBES:
                cue(c, 12, Action.LAUNCH_PROBES, 3);
                cue(c, 30, Action.SCAN);
                cue(c, 75, Action.LOCK_PROBES);
                for (int i = 0; i < 3; i++) cue(c, 100 + i * 18, Action.FIRE_RECORDED_SHOTS, i);
                cue(c, 160, Action.RECALL_PROBES);
                break;
            case STRIP_MINE:
                cue(c, 1, Action.ANCHOR);
                for (int i = 0; i < 3; i++) cue(c, 25 + i * 15, Action.LIFT_CHUNK, i);
                cue(c, 75, Action.AIM_CHUNK, 0);
                cue(c, 85, Action.SHOOT_CHUNK, 0);
                cue(c, 105, Action.AIM_CHUNK, 1);
                cue(c, 115, Action.SHOOT_CHUNK, 1);
                cue(c, 155, Action.DRILL_LAUNCH_CHUNK, 2);
                break;
            case TURRET_SWEEP:
                cue(c, 1, Action.ANCHOR);
                cue(c, 30, Action.HORIZONTAL_SWEEP);
                cue(c, 75, Action.CROSSING_SWEEPS);
                cue(c, 120, Action.CLOSE_CORRIDOR);
                cue(c, 175, Action.RETRACT);
                cue(c, 185, Action.CHARGE);
                break;
            case CORE_SAMPLE:
                cue(c, 1, Action.BURROW);
                cue(c, 25, Action.CUT_PLATFORM);
                cue(c, 80, Action.LIFT_PLATFORM);
                cue(c, 130, Action.SHATTER_PLATFORM);
                cue(c, 185, Action.UPWARD_FIRE);
                break;
            case FAULT_LINE:
                cue(c, 1, Action.BURROW);
                for (int i = 0; i < 3; i++) cue(c, 20 + i * 20, Action.FAULT_WARNING, i);
                cue(c, 105, Action.FAULT_ERUPTION);
                cue(c, 130, Action.CHARGE);
                break;
            case PROBE_MINEFIELD:
                cue(c, 1, Action.LAUNCH_PROBES, 6);
                cue(c, 25, Action.PLANT_PROBES);
                cue(c, 45, Action.BURROW);

                for (int i = 0; i < 6; i++) cue(c, 50, Action.ARM_PROBE, i);
                cue(c, 220, Action.RECALL_PROBES);
                break;
            case BORE_RICOCHET:
                cue(c, 20, Action.LOCK_TARGET);
                cue(c, 40, Action.CHARGE);

                cue(c, 41, Action.REDIRECT_RIGHT);
                cue(c, 85, Action.REDIRECT_DIAGONAL);
                cue(c, 135, Action.TUNNEL_RELOCATE);
                break;
            case INDUSTRIAL_PROCESSING:
                cue(c, 1, Action.LAUNCH_PROBES, 6);
                cue(c, 20, Action.MARK_SITES);
                cue(c, 45, Action.BURROW);
                for (int i = 0; i < 4; i++) cue(c, 65 + i * 30, Action.PROCESS_SITE, i);
                cue(c, 185, Action.FAKE_ERUPTION, 4);
                cue(c, 220, Action.PROCESS_SITE, 5);
                cue(c, 240, Action.RECONNECT);
                cue(c, 275, Action.METEOR_BORE);
                cue(c, 300, Action.DRILL_STALL);
                cue(c, 320, Action.DRILL_CLICK);
                cue(c, 340, Action.DRILL_SPUTTER);
                cue(c, 355, Action.DRILL_RESTART);
                cue(c, 365, Action.DRILL_STALL);
                break;
            case FINAL_CHARGE:
                cue(c, 1, Action.RETREAT);
                cue(c, 25, Action.RECONNECT);
                cue(c, 40, Action.FEED_DRILL);
                cue(c, 90, Action.LOCK_TARGET);
                cue(c, 110, Action.CHARGE);
                cue(c, 111, Action.EMBED_ON_MISS);
                break;
        }
        cue(c, attack.duration - 1, Action.RECOVER);
        return Collections.unmodifiableList(c);
    }

    public static boolean deep(float healthFraction) {
        return healthFraction <= .5F;
    }

    public static boolean failing(float healthFraction) {
        return healthFraction <= .18F;
    }

    public static Attack select(int cursor, float healthFraction) {
        Attack[] pool =
                deep(healthFraction)
                        ? new Attack[] {
                            Attack.FAULT_LINE,
                            Attack.PROBE_MINEFIELD,
                            Attack.BORE_RICOCHET,
                            Attack.INDUSTRIAL_PROCESSING
                        }
                        : new Attack[] {
                            Attack.SEISMIC_BORE,
                            Attack.TRENCH_CUTTER,
                            Attack.SURVEYING_PROBES,
                            Attack.STRIP_MINE,
                            Attack.TURRET_SWEEP,
                            Attack.CORE_SAMPLE
                        };
        int slot = Math.floorMod(cursor, pool.length + (failing(healthFraction) ? 1 : 0));
        return slot == pool.length ? Attack.FINAL_CHARGE : pool[slot];
    }

    private ExcavatorClassicScore() {}
}
