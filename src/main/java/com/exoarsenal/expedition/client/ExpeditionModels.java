package com.exoarsenal.expedition.client;

import java.util.*;

public final class ExpeditionModels {
    public static final int DARK = 0x27312D,
            METAL = 0x65725B,
            EDGE = 0xB8C5A1,
            GLOW = 0xC5F36C,
            GOLD = 0xBE9B57,
            BONE = 0xE1D3AC,
            SEA = 0x50B7C3;
    private static final Map<String, ExpeditionMesh> CACHE = new HashMap<>();

    public static ExpeditionMesh item(String id) {
        return CACHE.computeIfAbsent(id, ExpeditionModels::build);
    }

    private static ExpeditionMesh build(String id) {
        ExpeditionMesh m = new ExpeditionMesh();
        if (WulfrumArsenalModels.owns(id)) return WulfrumArsenalModels.get(id);
        switch (id) {
            case "wulfrum_metal_scrap":
                m.box(-5, -4, -1, 7, 3, 2, METAL)
                        .box(-2, -1, -1, 6, 6, 2, EDGE)
                        .box(-4, 0, -1.4, 2, 4, .5, DARK)
                        .tube(2, 2, -1.2, 2, 2, -2, 1, 1, GOLD);
                break;
            case "wulfrum_energy_core":
            case "draedon_power_cell":
                m.tube(0, -5, 0, 0, 5, 0, 3.3, 3.3, DARK)
                        .tube(0, -3, 0, 0, 3, 0, 3.5, 3.5, GLOW)
                        .tube(0, -5.5, 0, 0, -3, 0, 4, 4, METAL)
                        .tube(0, 3, 0, 0, 5.5, 0, 4, 4, METAL);
                for (int s : new int[] {-1, 1}) m.box(s * 3.5 - .4, -3, -1, .8, 6, 2, EDGE);
                break;
            case "pearl_shard":
            case "prism_shard":
                m.tube(0, -5, 0, 0, 0, 0, 1, 3, id.equals("pearl_shard") ? BONE : SEA)
                        .tube(
                                0,
                                0,
                                0,
                                1,
                                7,
                                0,
                                3,
                                0,
                                id.equals("pearl_shard") ? 0xF2E9D5 : 0xAFF5F4);
                break;
            case "coral":
                m.tube(0, -6, 0, 0, 5, 0, 1.6, .9, 0xD07A73)
                        .tube(0, -1, 0, -4, 3, 0, 1.2, .8, 0xE79983)
                        .tube(-4, 3, 0, -4, 6, 0, .8, .5, 0xE79983)
                        .tube(0, 1, 0, 4, 4, 0, 1, .6, 0xD07A73);
                break;
            case "seashell":
                for (int i = -3; i <= 3; i++)
                    m.tube(
                            0,
                            -4,
                            0,
                            i * 1.8,
                            3 - Math.abs(i) * .4,
                            0,
                            .7,
                            1.2,
                            i % 2 == 0 ? BONE : 0xC1A68C);
                break;
            case "starfish":
                m.star(0, 0, 0, 6, 0xD5A785);
                break;
            case "sahara_slicers":
                for (int s : new int[] {-1, 1}) {
                    m.tube(s * 2, -6, 0, s * 2, -1, 0, .7, .7, DARK);
                    m.box(s * 2 - 1.3, -1, -.8, 2.6, .7, 1.6, GOLD);
                    m.plate(
                            new double[][] {
                                {s * 1.5, 0},
                                {s * 4, 1},
                                {s * 5.8, 3},
                                {s * 6, 5.5},
                                {s * 4.5, 8},
                                {s * 4.8, 5},
                                {s * 3.5, 3},
                                {s * 1.5, 2}
                            },
                            .35,
                            BONE);
                }
                break;
            case "scourge_of_the_desert":
                m.tube(0, -8, 0, 0, 4, 0, .65, .65, 0x715940)
                        .tube(0, 3, 0, 0, 8, 0, 1.8, 0, BONE)
                        .box(-2, 2, -1, 4, 1, 2, GOLD);
                for (int s : new int[] {-1, 1})
                    m.tube(s * .6, 2, 0, s * 2.5, 4, 0, .8, .5, GOLD)
                            .tube(s * 2.5, 4, 0, s * 2.2, 6, 0, .5, 0, BONE);
                break;
            case "wulfrum_screwdriver":
                m.tube(0, -7, 0, 0, -1, 0, 1.8, 1.8, METAL)
                        .tube(0, -1, 0, 0, 6, 0, .6, .6, EDGE)
                        .box(-1, 5, -.3, 2, 2, .6, EDGE);
                for (int i = -5; i < -1; i += 2) m.tube(0, i, 0, 0, i + .4, 0, 1.9, 1.9, DARK);
                break;
            case "barinade":
                m.tube(0, -6, 0, -3, -1, 0, 1.4, 1.1, SEA)
                        .tube(-3, -1, 0, -3, 2, 0, 1.1, 1.1, DARK)
                        .tube(-3, 2, 0, 0, 7, 0, 1.1, .3, SEA)
                        .tube(0, -6, 0, 0, 7, 0, .1, .1, 0xC8F9F5);
                m.tube(-3, 1, -1, -3, 1, 1, 1.4, 1.4, GOLD);
                break;
            case "sandstream_scepter":
            case "brittle_star_staff":
                m.tube(0, -8, 0, 0, 4, 0, .7, .7, 0x796047);
                if (id.startsWith("brittle")) m.star(0, 4, 0, 4, SEA);
                else {
                    m.tube(0, 3, 0, 0, 6, 0, 2.8, 1.6, GOLD).tube(0, 6, 0, 0, 8, 0, 1.6, 0, BONE);
                }
                break;
            case "wulfrum_blunderbuss":
                m.box(-2, -5, -2, 3, 5, 4, 0x5A4936)
                        .box(-4, -1, -2.5, 8, 4, 5, METAL)
                        .tube(2, 1, 0, 8, 1, 0, 1.8, 3.1, EDGE)
                        .tube(7.8, 1, 0, 8.1, 1, 0, 2.4, 2.4, DARK)
                        .box(-5, 0, -1.5, 2, 2, 3, DARK);
                break;
            case "wulfrum_prosthesis":
                m.box(-4, -3, -3, 8, 6, 6, METAL)
                        .box(-3, 2, -2.5, 6, 1.5, 5, EDGE)
                        .tube(3, 0, 0, 7, 0, 0, 2.7, 2.4, DARK)
                        .tube(6.9, 0, 0, 7.1, 0, 0, 1.5, 1.5, GLOW);
                for (int z : new int[] {-2, 2}) m.box(-3, -2, z, 5, 1, 1, DARK);
                break;
            case "wulfrum_drill":
                m.box(-4, -2, -2.5, 7, 4, 5, METAL).box(-3, -6, -1.5, 2, 5, 3, DARK);
                for (int i = 0; i < 5; i++)
                    m.tube(
                            2 + i,
                            0,
                            0,
                            3 + i,
                            0,
                            0,
                            2.7 - i * .5,
                            2.2 - i * .5,
                            i % 2 == 0 ? EDGE : DARK);
                break;
            case "wulfrum_controller":
                m.box(-5, -3, -1.5, 10, 6, 3, METAL)
                        .box(-2, -1, -1.8, 4, 3, .5, GLOW)
                        .tube(-3, 2, 0, -4, 7, 0, .3, .3, EDGE)
                        .tube(3, -1, -1.6, 3, -1, -2.2, .8, .8, GOLD);
                break;
            case "wulfrum_battery":
            case "rover_drive":
                m.box(-4, -4, -2, 8, 8, 4, DARK)
                        .box(-3, -3, -2.3, 6, 6, .6, METAL)
                        .tube(0, 0, -2.4, 0, 0, -3, 2, 2, id.startsWith("rover") ? SEA : GLOW)
                        .box(-2, 4, -1, 4, 1.5, 2, EDGE);
                break;
            case "wulfrum_acrobatics_pack":
                m.box(-4, -4, -2, 8, 8, 4, METAL);
                for (int s : new int[] {-1, 1})
                    m.tube(s * 3, -5, 0, s * 3, 4, 0, 1.8, 1.8, DARK)
                            .tube(s * 3, -6, 0, s * 3, -4, 0, 1, 1.8, GLOW);
                break;
            case "wulfrum_scaffold_kit":
                m.box(-5, -4, -2, 10, 7, 4, 0x725B3E)
                        .box(-4, 2, -2.5, 8, 1, 5, EDGE)
                        .tube(-3, -4, 0, -3, 5, 0, .6, .6, METAL)
                        .tube(3, -4, 0, 3, 5, 0, .6, .6, METAL);
                break;
            case "wulfrum_lure":
                m.tube(0, -5, 0, 0, -3, 0, 5, 4, METAL)
                        .tube(0, -3, 0, 0, 4, 0, 1, 1, EDGE)
                        .tube(0, 3, 0, 0, 5, 0, 3, 1, GLOW)
                        .tube(0, 5, 0, 0, 8, 0, .3, .1, EDGE);
                break;
            case "wulfrum_hat":
            case "desert_scourge_mask":
                m.box(-5, -3, -4, 10, 6, 8, id.startsWith("wulfrum") ? METAL : GOLD)
                        .box(-6, -3, -5, 12, 1, 10, DARK);
                for (int s : new int[] {-1, 1}) {
                    m.tube(
                            s * 2.5,
                            0,
                            -4,
                            s * 2.5,
                            0,
                            -4.6,
                            1.8,
                            1.8,
                            id.startsWith("wulfrum") ? GLOW : BONE);
                    if (id.startsWith("desert"))
                        m.tube(s * 4, 2, 0, s * 6, 5, -1, 1, .7, BONE)
                                .tube(s * 6, 5, -1, s * 5, 8, -2, .7, 0, BONE);
                }
                break;
            case "wulfrum_jacket":
                m.box(-4, -5, -2, 8, 10, 4, METAL)
                        .box(-7, -1, -2, 3, 6, 4, EDGE)
                        .box(4, -1, -2, 3, 6, 4, EDGE)
                        .box(-.5, -5, -2.1, 1, 10, .3, DARK)
                        .box(1, 1, -2.2, 2, 2, .4, GOLD);
                break;
            case "wulfrum_overalls":
                m.box(-4, 1, -2, 8, 4, 4, METAL)
                        .box(-4, -6, -2, 3.5, 7, 4, DARK)
                        .box(.5, -6, -2, 3.5, 7, 4, DARK)
                        .box(-3, 1, -2.3, 6, 1, .4, GOLD);
                break;
            case "sand_cloak":
                m.box(-5, -6, -.5, 10, 11, 1, 0xB49A6C).box(-4, 4, -1, 8, 2, 2, GOLD);
                for (int x = -4; x < 5; x += 2) m.box(x, -6, -.7, .5, 9, .4, 0x826C48);
                break;
            case "ocean_crest":
                m.tube(0, 0, -1, 0, 0, 1, 4, 4, SEA)
                        .star(0, 0, -1.2, 3, BONE)
                        .tube(0, 4, 0, 0, 6, 0, .7, .7, GOLD);
                break;
            case "lesser_healing_potion":
                m.tube(0, -5, 0, 0, 2, 0, 3.2, 3.2, 0xC9686B)
                        .tube(0, 2, 0, 0, 4, 0, 3.2, 1.2, 0xBDDADC)
                        .tube(0, 4, 0, 0, 6, 0, 1.2, 1.2, 0x947447);
                break;
            case "lab_seeking_mechanism":
                m.box(-4, -4, -2, 8, 8, 4, DARK)
                        .tube(0, 0, -2, 0, 0, -2.5, 3.3, 3.3, SEA)
                        .tube(0, 0, -2.7, 1, 2, -2.7, .25, .1, GLOW);
                break;
            case "mysterious_circuitry":
                m.box(-5, -4, -.5, 10, 8, 1, 0x3B624D).box(-2, -2, -.8, 4, 4, .8, DARK);
                for (int i = -4; i <= 4; i += 2) m.box(i, -3, -.8, .5, 6, .3, GOLD);
                break;
            case "dubious_plating":
                m.box(-5, -4, -1, 10, 8, 2, METAL).box(-4, -3, -1.2, 8, 6, .3, EDGE);
                for (int x : new int[] {-4, 4})
                    for (int y : new int[] {-3, 3}) m.tube(x, y, -1.2, x, y, -1.6, .5, .5, DARK);
                break;
            case "sunken_sea_schematic":
            case "planetoid_schematic":
                m.box(-5, -6, -.4, 10, 12, .8, 0x426C94);
                for (int y = -4; y < 5; y += 3) m.box(-3, y, -.6, 6, .25, .3, 0xB5E3E9);
                m.box(-3, -4, -.6, .25, 8, .3, 0xB5E3E9);
                break;
            case "draedon_sunken_sea_log":
            case "draedon_planetoid_log":
            case "desert_scourge_lore":
            case "thank_you":
                m.box(-5, -6, -1, 10, 12, 2, id.equals("thank_you") ? GOLD : 0x485A62)
                        .box(-4, -5, -1.2, 8, 10, .3, BONE);
                for (int y = -3; y < 4; y += 2) m.box(-3, y, -1.4, 6, .35, .2, 0x54646A);
                break;
            default:
                return SeaModels.item(id);
        }
        return m;
    }

    public static ExpeditionMesh robot(String part) {
        return CACHE.computeIfAbsent(
                "robot/" + part,
                key -> {
                    ExpeditionMesh m = new ExpeditionMesh();
                    switch (part) {
                        case "chassis":
                            m.box(-6, 3, -4, 12, 5, 8, DARK)
                                    .box(-5, 7, -4.5, 10, 2, 9, METAL)
                                    .box(-3, 4, -4.3, 6, 2, .5, GLOW);
                            for (int s : new int[] {-1, 1}) m.box(s * 5 - .5, 4, -3, 1, 3, 6, EDGE);
                            break;
                        case "wheel":
                            m.tube(0, 0, -1, 0, 0, 1, 3, 3, DARK)
                                    .tube(0, 0, -1.2, 0, 0, 1.2, 1.6, 1.6, METAL);
                            for (int i = 0; i < 6; i++) {
                                double a = i * Math.PI / 3;
                                m.tube(
                                        0,
                                        0,
                                        -1.3,
                                        Math.cos(a) * 2.5,
                                        Math.sin(a) * 2.5,
                                        -1.3,
                                        .2,
                                        .2,
                                        EDGE);
                            }
                            break;
                        case "duct":
                            m.tube(0, -1, 0, 0, 1, 0, 4, 4, METAL)
                                    .tube(0, 1, 0, 0, 1.1, 0, 3.1, 3.1, DARK)
                                    .box(-3, 1.2, -.5, 6, .3, 1, EDGE)
                                    .box(-.5, 1.2, -3, 1, .3, 6, EDGE);
                            break;
                        case "amplifier":
                            m.tube(0, 0, 0, 0, 2, 0, 6, 5, DARK)
                                    .tube(0, 2, 0, 0, 5, 0, 4, 4, METAL)
                                    .tube(0, 5, 0, 0, 13, 0, 1, 1, EDGE)
                                    .tube(0, 7, 0, 0, 9, 0, 4, 4, METAL)
                                    .tube(0, 12, 0, 0, 14, 0, 4, 1, GLOW)
                                    .tube(0, 14, 0, 0, 19, 0, .4, .1, EDGE);
                            break;
                        case "gyrator":
                            m.tube(0, 4, -3, 0, 4, 3, 5, 5, DARK)
                                    .tube(0, 4, -3.2, 0, 4, 3.2, 3.5, 3.5, METAL)
                                    .tube(0, 4, -3.4, 0, 4, 3.4, 1.5, 1.5, GLOW);
                            break;
                        case "mine":
                            m.tube(0, 4, 0, 0, 12, 0, 5, 5, METAL)
                                    .tube(0, 6, 0, 0, 10, 0, 5.2, 5.2, GLOW);
                            for (int i = 0; i < 4; i++) {
                                double a = i * Math.PI / 2;
                                m.tube(
                                                Math.cos(a) * 4,
                                                8,
                                                Math.sin(a) * 4,
                                                Math.cos(a) * 11,
                                                8,
                                                Math.sin(a) * 11,
                                                1.8,
                                                2.3,
                                                DARK)
                                        .tube(
                                                Math.cos(a) * 10.8,
                                                8,
                                                Math.sin(a) * 10.8,
                                                Math.cos(a) * 11.1,
                                                8,
                                                Math.sin(a) * 11.1,
                                                1.2,
                                                1.2,
                                                GLOW);
                            }
                            break;
                        case "slime":
                            m.tube(0, 1, 0, 0, 4, 0, 6, 7, 0x9BC980, .65F)
                                    .tube(0, 4, 0, 0, 8, 0, 7, 4, 0xACDE91, .65F)
                                    .tube(0, 8, 0, 0, 10, 0, 4, .1, 0xB7EFA0, .65F);
                            m.box(-3, 2, -2, 6, 4, 4, METAL).box(-2, 5, -2.5, 4, 1, .5, GLOW);
                            break;
                        default:
                            throw new IllegalArgumentException(part);
                    }
                    return m;
                });
    }
}
