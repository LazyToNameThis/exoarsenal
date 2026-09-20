package com.exoarsenal.expedition.client;

import java.util.*;

public final class LegacyItemModels {
    public static final Set<String> IDS =
            Collections.unmodifiableSet(
                    new LinkedHashSet<>(
                            Arrays.asList(
                                    "drygrass",
                                    "flint_shard",
                                    "crude_cordage",
                                    "tinder",
                                    "canvas",
                                    "hewn_sticks",
                                    "fireclay_ball",
                                    "unfired_clay_cast",
                                    "clay_cast",
                                    "unfired_fireclay_cast",
                                    "fireclay_cast",
                                    "prototype_energy_core",
                                    "canvas_helmet",
                                    "canvas_chestplate",
                                    "canvas_leggings",
                                    "canvas_boots",
                                    "rmor_helmet",
                                    "rmor_chestplate",
                                    "rmor_leggings",
                                    "rmor_boots",
                                    "x10_helmet",
                                    "x10_chestplate",
                                    "x10_leggings",
                                    "x10_boots",
                                    "clay_river_pan",
                                    "kx20_helmet",
                                    "machined_core",
                                    "kx_gear",
                                    "frigid_alloy",
                                    "frigid_metal",
                                    "small_frozen_tin",
                                    "small_frozen_copper",
                                    "kx20_boots",
                                    "kx20_chestplate",
                                    "kx20_leggings")));
    private static final Map<String, ExpeditionMesh> CACHE = new HashMap<>();

    public static ExpeditionMesh item(String id) {
        if (!IDS.contains(id)) throw new IllegalArgumentException(id);
        return CACHE.computeIfAbsent(id, LegacyItemModels::build);
    }

    private static ExpeditionMesh build(String id) {
        ExpeditionMesh m = new ExpeditionMesh();
        int steel = 0x6E8790,
                edge = 0xB4C7C9,
                dark = 0x303B45,
                wood = 0x927455,
                cloth = 0xBAAA84,
                clay = 0xB8866B;
        if (id.endsWith("helmet")
                || id.endsWith("chestplate")
                || id.endsWith("leggings")
                || id.endsWith("boots")) {
            boolean canvas = id.startsWith("canvas");
            int plate = canvas ? cloth : steel,
                    accent =
                            id.startsWith("kx20")
                                    ? 0x57DBD1
                                    : id.startsWith("x10") ? 0xE1C35A : 0xC75665;
            if (id.endsWith("helmet")) {
                m.box(-4, -3, -3, 8, 6, 6, plate)
                        .box(-3, 3, -2.5, 6, 1, 5, edge)
                        .box(-3, -1, -3.3, 6, 2, .5, dark);
                if (!canvas)
                    m.box(-2.7, -.6, -3.6, 5.4, .6, .4, accent)
                            .box(-4.5, -2, -1, 1, 4, 3, plate)
                            .box(3.5, -2, -1, 1, 4, 3, plate);
            } else if (id.endsWith("chestplate")) {
                m.box(-3.5, -5, -2, 7, 10, 4, plate)
                        .box(-6, 1, -2.5, 2.5, 4, 5, plate)
                        .box(3.5, 1, -2.5, 2.5, 4, 5, plate)
                        .box(-3, -1, -2.4, 6, .8, .5, dark)
                        .box(-3, -4, -2.4, 6, .8, .5, dark);
                if (!canvas)
                    m.box(-1, 1, -2.6, 2, 2, .7, accent)
                            .box(-2.5, -3, 2, 5, 7, 2, dark)
                            .box(-1.5, -2, 4, 3, 5, .5, accent);
            } else if (id.endsWith("leggings")) {
                m.box(-4, 2, -2, 8, 3, 4, plate)
                        .box(-3.8, -6, -2, 3.3, 8, 4, plate)
                        .box(.5, -6, -2, 3.3, 8, 4, plate)
                        .box(-4, -3, -2.5, 3.5, 2, .7, dark)
                        .box(.5, -3, -2.5, 3.5, 2, .7, dark);
                if (!canvas)
                    m.box(-3.5, -2.6, -2.7, 2.5, .5, .3, accent)
                            .box(1, -2.6, -2.7, 2.5, .5, .3, accent);
            } else {
                for (int side : new int[] {-1, 1}) {
                    m.box(side * 2.5 - 1.5, -4, -2, 3, 7, 4, plate)
                            .box(side * 2.5 - 1.7, -5, -4, 3.4, 2, 6, dark)
                            .box(side * 2.5 - 1.5, -3, -3.5, 3, 1.5, 2, plate);
                    if (!canvas) m.box(side * 2.5 - 1, -1, -2.3, 2, .6, .4, accent);
                }
            }
            return m;
        }
        switch (id) {
            case "drygrass":
                m.tube(-2, -6, 0, -4, 5, 0, .3, .12, cloth)
                        .tube(-1, -6, .4, 0, 7, .5, .3, .1, 0xD0BD84)
                        .tube(0, -6, 0, 3, 6, 0, .3, .1, 0x9C985E)
                        .tube(1, -5, -.4, 5, 3, -.5, .3, .1, cloth)
                        .box(-2, -4, -1, 4, 1, 2, wood);
                break;
            case "flint_shard":
                m.plate(
                                new double[][] {
                                    {-3, -4}, {-4, 0}, {-1, 5}, {2, 3}, {4, -2}, {1, -5}
                                },
                                1.2,
                                0x6B7B85)
                        .plate(
                                new double[][] {{-2, -3}, {-2, 0}, {-1, 3}, {1, 1}, {2, -2}},
                                1.25,
                                0xA3B2B7);
                break;
            case "crude_cordage":
                m.tube(-3, -4, 0, 3, -4, 0, .6, .6, wood)
                        .tube(3, -4, 0, 4, 2, 0, .6, .6, cloth)
                        .tube(4, 2, 0, -2, 4, 0, .6, .6, wood)
                        .tube(-2, 4, 0, -4, 0, 0, .6, .6, cloth)
                        .tube(-4, 0, 0, 2, -1, 0, .6, .6, wood)
                        .tube(2, -1, 0, 1, 6, 0, .6, .4, cloth);
                break;
            case "tinder":
                m.box(-3, -2, -2, 6, 2, 4, wood)
                        .tube(-4, -1, 0, 3, 2, 0, .5, .2, cloth)
                        .tube(-3, 1, -1, 4, -1, 1, .4, .2, 0xD4B585)
                        .tube(0, -2, 1, -1, 4, -1, .4, .15, wood);
                break;
            case "canvas":
                m.box(-5, -3, -2, 10, 6, 4, cloth)
                        .box(-5, 2, -2.5, 10, 1, 5, 0xD6CAA8)
                        .box(-4, -2, -2.4, 8, .5, .4, wood)
                        .box(-4, 0, -2.4, 8, .5, .4, wood);
                break;
            case "hewn_sticks":
                m.tube(-3, -6, 0, -1, 6, 0, .7, .6, wood)
                        .tube(0, -6, .7, 1, 5, .7, .6, .6, 0xBA946A)
                        .tube(2, -5, -.6, 3, 6, -.6, .6, .5, wood)
                        .box(-3, -1, -1.5, 6, 1, 3, cloth);
                break;
            case "fireclay_ball":
                m.box(-3, -3, -3, 6, 6, 6, clay)
                        .box(-2, 3, -2, 4, 1, 4, 0xD5A384)
                        .box(-3.2, -1, -2, 1, 2, 3, 0x92664F);
                break;
            case "clay_river_pan":
                m.box(-5, -2, -4, 10, 1, 8, clay)
                        .box(-6, -1, -5, 12, 2, 1, clay)
                        .box(-6, -1, 4, 12, 2, 1, clay)
                        .box(-6, -1, -4, 1, 2, 8, clay)
                        .box(5, -1, -4, 1, 2, 8, clay);
                break;
            case "unfired_clay_cast":
            case "clay_cast":
            case "unfired_fireclay_cast":
            case "fireclay_cast":
                int fired = id.startsWith("unfired") ? 0x9D8072 : clay;
                m.box(-5, -2, -3, 10, 1, 6, fired)
                        .box(-5, -1, -3, 10, 2, 1, fired)
                        .box(-5, -1, 2, 10, 2, 1, fired)
                        .box(-5, -1, -2, 1, 2, 4, fired)
                        .box(4, -1, -2, 1, 2, 4, fired)
                        .box(-3, -.9, -1.5, 6, .4, 3, 0x5E4A44);
                break;
            case "small_frozen_tin":
            case "small_frozen_copper":
                m.box(-3, -2, -2, 6, 4, 4, 0xA1C4D1)
                        .box(-2, -1, -2.4, 4, 2, .8, id.endsWith("copper") ? 0xC58761 : edge)
                        .box(-1, 2, -1, 2, 1, 2, 0xD1E0E2);
                break;
            case "frigid_metal":
            case "frigid_alloy":
                m.box(-5, -2, -2, 10, 3, 4, id.equals("frigid_metal") ? steel : 0x849586)
                        .box(-4, 1, -1.5, 8, 1, 3, edge)
                        .tube(-3, -.5, -2.3, 3, -.5, -2.3, .2, .2, 0x78C5D6);
                break;
            case "prototype_energy_core":
            case "machined_core":
            case "kx_gear":
                m.box(-3, -3, -2, 6, 6, 4, dark)
                        .box(-2, -2, -2.5, 4, 4, 5, steel)
                        .box(
                                -1,
                                -1,
                                -3,
                                2,
                                2,
                                6,
                                id.equals("prototype_energy_core")
                                        ? 0xD76770
                                        : id.equals("kx_gear") ? 0x6ADAD3 : 0x92C7E1)
                        .box(-4, -1, -1, 1, 2, 2, edge)
                        .box(3, -1, -1, 1, 2, 2, edge)
                        .box(-1, -4, -1, 2, 1, 2, edge)
                        .box(-1, 3, -1, 2, 1, 2, edge);
                break;
            default:
                throw new IllegalArgumentException(id);
        }
        return m;
    }
}
