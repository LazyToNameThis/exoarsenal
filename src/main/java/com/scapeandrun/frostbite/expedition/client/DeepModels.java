package com.scapeandrun.frostbite.expedition.client;

import java.util.*;

public final class DeepModels {
    private static final Map<String, ExpeditionMesh> CACHE = new HashMap<>();

    public static ExpeditionMesh item(String id) {
        return CACHE.computeIfAbsent(id, DeepModels::build);
    }

    private static ExpeditionMesh build(String id) {
        ExpeditionMesh m = new ExpeditionMesh();
        int gold = 0xC7AE68, iron = 0xA9BBC3, dark = 0x494655, blue = 0x719FC6, fire = 0xD59157;
        switch (id) {
            case "molten_pickaxe":
                m.tube(0, -8, 0, 0, 6, 0, .7, .7, dark)
                        .plate(
                                new double[][] {
                                    {-7, 2}, {-5, 7}, {0, 9}, {5, 7}, {7, 2}, {3, 5}, {0, 6},
                                    {-3, 5}
                                },
                                .8,
                                fire)
                        .box(-1, 5, -1, 2, 3, 2, gold);
                break;
            case "volcano":
                m.box(-1, -8, -1, 2, 6, 2, dark)
                        .box(-4, -2, -1, 8, 1.5, 2, gold)
                        .plate(
                                new double[][] {{-2, 0}, {-2, 12}, {0, 17}, {2, 12}, {2, 0}},
                                .8,
                                fire)
                        .tube(0, 0, -.9, 0, 12, -.9, .3, .2, gold);
                break;
            case "blade_of_grass":
                m.tube(0, -8, 0, 0, 0, 0, .8, .8, 0x69825B)
                        .plate(
                                new double[][] {
                                    {-1, 0}, {-3, 4}, {-2, 8}, {-3, 12}, {0, 17}, {2, 12}, {3, 8},
                                    {2, 4}, {1, 0}
                                },
                                .5,
                                0x91AD67)
                        .tube(0, 1, -.6, 0, 14, -.6, .25, .12, 0xD0CB82);
                break;
            case "golden_key":
            case "shadow_key":
                int key = id.equals("golden_key") ? gold : 0x9B7CB2;
                m.box(-2, 2, -.5, 4, 1, 1, key)
                        .box(-2, 5, -.5, 4, 1, 1, key)
                        .box(-2, 3, -.5, 1, 2, 1, key)
                        .box(1, 3, -.5, 1, 2, 1, key)
                        .box(-.5, -5, -.5, 1, 7, 1, key)
                        .box(.5, -5, -.5, 2, 1, 1, key)
                        .box(.5, -3, -.5, 1.5, 1, 1, key);
                break;
            case "hellstone_shard":
                m.box(-3, -3, -2, 6, 5, 4, dark)
                        .box(-2, 2, -1, 4, 2, 2, fire)
                        .tube(-2, -2, -2.3, 1, 2, -2.3, .35, .2, fire);
                break;
            case "hellstone_bar":
                m.box(-5, -2, -2.5, 10, 3, 5, 0x966A58)
                        .box(-4, 1, -2, 8, 1, 4, fire)
                        .box(-3, 2, -1.5, 6, .5, 3, gold);
                break;
            case "jungle_encrypted_schematic":
            case "underworld_encrypted_schematic":
                m.box(-4, -5, -.6, 8, 10, 1.2, id.startsWith("jungle") ? 0x728C73 : 0x996E64)
                        .box(-3, -4, -.8, 6, 8, .3, 0xCED1B5)
                        .box(-2, -3, -1, 4, .5, .3, dark)
                        .box(-2, -1, -1, .5, 4, .3, dark)
                        .box(-2, 2, -1, 4, .5, .3, dark)
                        .box(1.5, -1, -1, .5, 3, .3, dark)
                        .box(-.5, -.5, -1.1, 1, 1, .4, gold);
                break;
            case "jungle_research_log":
            case "underworld_research_log":
            case "water_bolt_tome":
            case "demon_scythe":
                m.box(
                                -4,
                                -5,
                                -1,
                                8,
                                10,
                                2,
                                id.equals("water_bolt_tome")
                                        ? blue
                                        : id.equals("demon_scythe") ? 0x8C689F : dark)
                        .box(-3, -4, 1, 6, 8, .6, 0xD1C9AD)
                        .box(-4, -5, -1.3, 1, 10, .4, gold)
                        .box(-2, -2, -1.4, 4, 4, .3, iron)
                        .box(-1, -1, -1.8, 2, 2, .5, id.equals("demon_scythe") ? 0xAA81C5 : blue);
                break;
            case "muramasa":
                m.box(-.8, -8, -.8, 1.6, 6, 1.6, dark)
                        .box(-3.5, -2, -1, 7, 1.5, 2, gold)
                        .plate(
                                new double[][] {
                                    {-1.4, -.5}, {-1.4, 13}, {0, 17}, {1.4, 13}, {1.4, -.5}
                                },
                                .5,
                                blue)
                        .tube(0, 0, -.6, 0, 13, -.6, .2, .15, iron);
                break;
            case "flamelash":
                m.tube(0, -8, 0, 0, 5, 0, .65, .65, dark)
                        .box(-1.3, 3, -1.3, 2.6, 2, 2.6, gold)
                        .plate(
                                new double[][] {
                                    {0, 4}, {-3, 7}, {-1, 11}, {0, 8}, {2, 12}, {3, 8}, {2, 5}
                                },
                                .5,
                                fire)
                        .box(-.5, 6, -.6, 1, 3, .3, gold);
                break;
            case "water_projectile":
                m.box(-2, -2, -2, 4, 4, 4, blue).box(-1, -1, -3, 2, 2, 1, 0xBDD9DD);
                break;
            case "fire_projectile":
                m.plate(
                                new double[][] {
                                    {0, -4}, {-2, -1}, {-3, 2}, {-1, 1}, {0, 5}, {2, 2}, {3, -1}
                                },
                                .8,
                                fire)
                        .box(-.7, -1, -1, 1.4, 3, .4, gold);
                break;
            case "scythe_projectile":
                m.plate(
                                new double[][] {
                                    {-6, -5}, {-5, 1}, {-2, 5}, {3, 6}, {7, 3}, {3, 3}, {0, 1},
                                    {-1, -2}
                                },
                                .5,
                                0x9A79B5)
                        .tube(-1, -4, 0, 2, 4, 0, .5, .5, dark);
                break;
            default:
                throw new IllegalArgumentException(id);
        }
        return m;
    }
}
