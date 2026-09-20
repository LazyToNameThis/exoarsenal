package com.exoarsenal.expedition.client;

import java.util.*;

public final class PrebossModels {
    private static final int WOOD = 0x856145,
            DARK = 0x3B3944,
            BONE = 0xDFD4B8,
            GOLD = 0xE2B95A,
            BLUE = 0x7CBDE4,
            ICE = 0xC8E5E9,
            PURPLE = 0xAD89C7,
            METAL = 0x687C64;
    private static final Map<String, ExpeditionMesh> CACHE = new HashMap<>();

    public static ExpeditionMesh item(String id) {
        return CACHE.computeIfAbsent(id, PrebossModels::build);
    }

    private static void heart(ExpeditionMesh m) {
        m.box(-5, 0, -1.5, 4, 4, 3, 0xD86B79)
                .box(1, 0, -1.5, 4, 4, 3, 0xD86B79)
                .box(-4, -2, -1.5, 8, 3, 3, 0xB64B69)
                .box(-3, -4, -1.5, 6, 2, 3, 0xB64B69)
                .box(-1, -6, -1, 2, 2, 2, 0x7F375C)
                .box(-4, 1, -1.8, 2, 2, .4, 0xF5C0B4);
    }

    private static void bloom(ExpeditionMesh m) {
        m.tube(0, -6, 0, 0, 4, 0, .35, .2, 0x657B6B)
                .tube(0, -1, 0, -3, 1, 0, .4, .1, BLUE)
                .tube(0, 1, 0, 3, 3, 0, .4, .1, ICE)
                .star(0, 4, 0, 2.5, ICE);
    }

    private static ExpeditionMesh build(String id) {
        ExpeditionMesh m = new ExpeditionMesh();
        switch (id) {
            case "fallen_star":
                m.star(0, 0, 0, 6, GOLD).box(-1, -1, -1.2, 2, 2, .5, BONE);
                break;
            case "cave_javelin":
                m.tube(0, -8, 0, 0, 4, 0, .45, .45, WOOD)
                        .box(-.7, 2, -.7, 1.4, 1, 1.4, DARK)
                        .tube(0, 4, 0, 0, 8, 0, 1.2, .05, BONE);
                break;
            case "glowing_mushroom":
                m.box(-.7, -6, -.7, 1.4, 8, 1.4, ICE)
                        .box(-4, 0, -3, 8, 2, 6, BLUE)
                        .box(-3, 2, -2, 6, 1.5, 4, 0x6387C4)
                        .box(-2, 3.5, -1, 4, .5, 2, ICE)
                        .box(-3.5, 1, -3.2, 1, 1, .3, ICE)
                        .box(1.5, .5, -3.2, 1, 1, .3, ICE);
                break;
            case "mana_crystal":
                m.tube(0, -6, 0, 0, 0, 0, .3, 3.5, BLUE)
                        .tube(0, 0, 0, 0, 6, 0, 3.5, .3, ICE)
                        .box(-1, -2, -3.7, 1.5, 4, .4, BLUE);
                break;
            case "life_crystal":
                heart(m);
                break;
            case "life_crystal_formation":
                heart(m);
                m.box(-4, -7.5, -3, 8, 1.5, 6, 0x635F6E)
                        .box(-2, -6, -2, 4, 1, 4, 0x92818E)
                        .tube(-2, -5, 0, -4, -2, 0, .8, .1, 0xD86B79)
                        .tube(2, -5, 0, 4, -3, 0, .6, .1, 0xF5C0B4);
                break;
            case "magic_mirror":
            case "ice_mirror":
                {
                    int rim = id.startsWith("ice") ? ICE : GOLD;
                    m.box(-4, -2, -.7, 8, 8, 1.4, DARK)
                            .box(-3, -1, -1, 6, 6, .4, BLUE)
                            .box(-2, 0, -1.2, 1, 4, .3, ICE)
                            .box(-1, 3, -1.2, 3, 1, .3, ICE);
                    m.box(-4.5, -2, -1, 1, 8, 2, rim)
                            .box(3.5, -2, -1, 1, 8, 2, rim)
                            .box(-3.5, 5.5, -1, 7, 1, 2, rim)
                            .box(-3.5, -2.5, -1, 7, 1, 2, rim);
                    m.box(-.8, -7, -.8, 1.6, 5, 1.6, rim)
                            .box(-1.3, -7.5, -1, 2.6, 1, 2, DARK)
                            .box(-1, 6, -.8, 2, 1.5, 1.6, id.startsWith("ice") ? BLUE : PURPLE);
                    if (id.startsWith("ice"))
                        m.tube(-4, 3, 0, -5, 6, 0, .6, .1, BLUE)
                                .tube(4, 3, 0, 5, 6, 0, .6, .1, BLUE);
                    break;
                }
            case "recall_potion":
                m.box(-3, -5, -2, 6, 6, 4, 0x7967B6)
                        .box(-2, 1, -1.5, 4, 2, 3, ICE)
                        .box(-1, 3, -1, 2, 2, 2, WOOD)
                        .box(-2, -3, -2.3, 1, 4, .4, ICE)
                        .box(-1, -3, -2.3, 3, 1, .4, GOLD)
                        .box(.5, -2, -2.3, 1, 2, .4, GOLD);
                break;
            case "lesser_mana_potion":
                m.box(-3, -5, -2, 6, 6, 4, BLUE)
                        .box(-2, 1, -1.5, 4, 2, 3, ICE)
                        .box(-1, 3, -1, 2, 2, 2, WOOD)
                        .box(-2, -3, -2.2, 1, 4, .3, ICE)
                        .box(-1, -3, -2.2, 3, 2, .3, PURPLE);
                break;
            case "shiverthorn":
                bloom(m);
                break;
            case "shiverthorn_plant":
                bloom(m);
                m.tube(0, -3, 0, 2, -1, 2, .4, .1, BLUE).tube(0, -2, 0, -2, 0, -2, .4, .1, ICE);
                break;
            case "shiverthorn_shoot":
                m.tube(0, -6, 0, 0, 3, 0, .35, .2, 0x657B6B)
                        .tube(0, -1, 0, -3, 1, 0, .4, .1, BLUE)
                        .tube(0, 1, 0, 2, 3, 0, .4, .1, ICE)
                        .box(-.6, 2, -.6, 1.2, 1.5, 1.2, BLUE);
                break;
            case "stormlion_mandible":
                m.tube(-4, -5, 0, -3, 1, 0, 1, 1.6, GOLD)
                        .tube(-3, 1, 0, 0, 5, 0, 1.6, 1, GOLD)
                        .tube(0, 5, 0, 4, 6, 0, 1, .2, BONE)
                        .box(-2, 0, -1, 2, 2, 2, DARK);
                break;
            case "flinx_fur":
                m.box(-4, -3, -1, 8, 6, 2, BONE)
                        .box(-5, -1, -1, 10, 3, 2, ICE)
                        .box(-3, 3, -1, 2, 2, 2, BONE)
                        .box(1, 3, -1, 2, 1, 2, ICE)
                        .box(-3, -5, -1, 2, 2, 2, BONE)
                        .box(1, -4, -1, 2, 1, 2, ICE);
                break;
            case "wooden_boomerang":
            case "enchanted_boomerang":
                {
                    int material = id.startsWith("enchanted") ? BLUE : WOOD;
                    m.tube(-6, -4, 0, -2, 2, 0, 1, 1.3, material)
                            .tube(-2, 2, 0, 4, 6, 0, 1.3, 1, material)
                            .box(-2.8, .6, -1.4, 2.5, 2.5, .5, DARK)
                            .tube(
                                    -4,
                                    -1,
                                    -1.2,
                                    -3,
                                    .5,
                                    -1.2,
                                    .18,
                                    .18,
                                    id.startsWith("enchanted") ? GOLD : BONE);
                    break;
                }
            case "wulfrum_knife":
                m.box(-.8, -7, -.8, 1.6, 5, 1.6, DARK)
                        .box(-2, -2, -1, 4, .7, 2, METAL)
                        .box(-1, -1, -.5, 2, 5, 1, METAL)
                        .tube(0, 4, 0, 0, 7, 0, 1, .1, BONE)
                        .box(-.3, 0, -.7, .6, 4, .3, 0xBAD886);
                break;
            case "crystalline":
                m.box(-.7, -6, -.7, 1.4, 4, 1.4, DARK)
                        .box(-2, -2, -1, 4, 1, 2, GOLD)
                        .tube(0, -1, 0, 0, 5, 0, 1.6, 1.1, PURPLE)
                        .tube(0, 5, 0, 0, 8, 0, 1.1, .1, ICE)
                        .box(-.4, 0, -1.7, .8, 5, .3, ICE);
                break;
            case "wand_of_sparking":
            case "wand_of_frosting":
                m.tube(0, -7, 0, 0, 2, 0, .7, .5, WOOD)
                        .tube(0, 1, 0, 2, 4, 0, .5, .3, WOOD)
                        .box(1, 3, -.5, 2, 2, 1, id.endsWith("sparking") ? GOLD : BLUE)
                        .tube(0, 0, 0, -2, 2, 0, .35, .1, BONE);
                break;
            case "frost_bolt":
                m.box(-5, -6, -1.5, 10, 12, 3, DARK)
                        .box(-4, -5, -1.8, 8, 10, .4, BLUE)
                        .box(-4, -4, 1.4, 8, 8, .4, BONE)
                        .box(-5, -6, -1.8, 1, 12, 3.6, ICE)
                        .star(.3, 0, -2.2, 3, ICE)
                        .box(3, -1, -2.3, 2, 2, .5, GOLD);
                break;
            case "storm_spear":
                m.box(-.55, -9, -.55, 1.1, 14, 1.1, WOOD)
                        .box(-1, 2, -1, 2, 2, 2, GOLD)
                        .tube(0, 4, 0, 0, 9, 0, 1.4, .1, BONE)
                        .tube(-1, 3, 0, -2.5, 6, 0, .5, .2, BLUE)
                        .tube(1, 3, 0, 2.5, 6, 0, .5, .2, BLUE);
                break;
            case "thunder_zapper":
                m.box(-.7, -7, -.7, 1.4, 9, 1.4, DARK)
                        .box(-2, 0, -1.5, 4, 4, 3, GOLD)
                        .tube(-1, 3, 0, -3, 6, 0, .6, .6, GOLD)
                        .tube(1, 3, 0, 3, 6, 0, .6, .6, GOLD)
                        .box(-1, 5, -1, 2, 2, 2, BLUE)
                        .box(-.5, 5.5, -1.2, 1, 1, .3, ICE);
                break;
            case "stormjaw_staff":
                m.box(-.6, -8, -.6, 1.2, 11, 1.2, DARK)
                        .tube(-1, 2, 0, -3, 5, 0, .8, .8, GOLD)
                        .tube(-3, 5, 0, -1, 8, 0, .8, .1, BONE)
                        .tube(1, 2, 0, 3, 5, 0, .8, .8, GOLD)
                        .tube(3, 5, 0, 1, 8, 0, .8, .1, BONE)
                        .box(-1, 3, -1, 2, 2, 2, BLUE);
                break;
            case "flinx_staff":
                m.tube(0, -8, 0, 0, 4, 0, .6, .6, WOOD)
                        .box(-3, 3, -2, 6, 4, 4, BONE)
                        .box(-2, 7, -1, 4, 1, 2, ICE)
                        .box(-2, 4, -2.2, 1, 1, .3, DARK)
                        .box(1, 4, -2.2, 1, 1, .3, DARK)
                        .box(-1, 2, -1.8, 2, 1, .5, PURPLE);
                break;
            case "diamond_staff":
            case "emerald_staff":
                m.box(-.6, -8, -.6, 1.2, 11, 1.2, id.startsWith("diamond") ? GOLD : METAL)
                        .box(-1, -4, -1, 2, 1, 2, DARK)
                        .box(-2, 2, -1.5, 4, 1, 3, BONE)
                        .tube(0, 3, 0, 0, 6, 0, 2, 1.2, id.startsWith("diamond") ? ICE : 0x79BC96)
                        .tube(
                                0,
                                6,
                                0,
                                0,
                                8,
                                0,
                                1.2,
                                .1,
                                id.startsWith("diamond") ? BLUE : 0xB2E9BC);
                break;
            case "band_of_regeneration":
            case "band_of_starpower":
                m.box(-4, -3, -1, 1, 6, 2, GOLD)
                        .box(3, -3, -1, 1, 6, 2, GOLD)
                        .box(-3, -4, -1, 6, 1, 2, GOLD)
                        .box(-3, 3, -1, 6, 1, 2, GOLD)
                        .box(-1.5, 2, -1.5, 3, 3, 3, id.endsWith("regeneration") ? 0xD47780 : BLUE)
                        .box(-.7, 3, -1.8, 1.4, 1, .3, ICE);
                break;
            case "hermes_boots":
                m.box(-3, -6, -2.5, 6, 2, 8, DARK)
                        .box(-2.5, -4, -2, 5, 3, 7, WOOD)
                        .box(-2.5, -1, 0, 5, 6, 4, WOOD)
                        .box(-3, 4, -.5, 6, 1, 5, GOLD);
                m.box(-1.5, -2, -2.3, 3, .5, .4, BONE)
                        .box(-1.5, 0, -.3, 3, .5, .4, BONE)
                        .box(-1.5, 2, -.3, 3, .5, .4, BONE);
                m.plate(new double[][] {{2, 0}, {3, 4}, {7, 7}, {6, 3}, {4, 0}}, .5, BONE)
                        .tube(3, 1, -.6, 5.8, 5.5, -.6, .18, .1, GOLD);
                break;
            case "aglet":
                m.tube(-4, 4, 0, 0, 1, 0, .3, .3, BONE)
                        .tube(0, 1, 0, 3, 5, 0, .3, .3, BONE)
                        .tube(-4, 4, 0, -4, -2, 0, .3, .3, BONE);
                m.box(-5, -5, -.7, 2, 4, 1.4, 0xB8C1BF)
                        .box(-4.5, -4.5, -.9, .5, 3, .3, ICE)
                        .box(2, 1, -.7, 2, 4, 1.4, GOLD)
                        .box(2.5, 1.5, -.9, .5, 3, .3, BONE);
                break;
            case "anklet_of_the_wind":
                m.box(-4, -3, -1, 1, 6, 2, 0xA26658)
                        .box(3, -3, -1, 1, 6, 2, 0xA26658)
                        .box(-3, -4, -1, 6, 1, 2, GOLD)
                        .box(-3, 3, -1, 6, 1, 2, GOLD);
                m.plate(
                                new double[][] {{0, -1}, {-1, 2}, {2, 5}, {5, 6}, {4, 2}, {2, 0}},
                                .6,
                                0x75B58C)
                        .tube(0, 0, -.8, 4, 5, -.8, .2, .1, BONE);
                break;
            case "shackle":
                m.box(-4, -1, -1.5, 1.5, 5, 3, 0x889A9F)
                        .box(2.5, -1, -1.5, 1.5, 5, 3, 0x889A9F)
                        .box(-3, 3, -1.5, 6, 1.5, 3, 0xB5BEC2)
                        .box(-3, -2, -1.5, 6, 1.5, 3, 0x65737C);
                m.box(-1, -3, -1, 2, 1, 2, DARK)
                        .tube(0, -3, 0, 0, -5, 0, .6, .6, 0x889A9F)
                        .box(-2, -6, -.7, 4, 1, 1.4, 0x889A9F)
                        .box(-2, -5, -.7, 1, 2, 1.4, 0x889A9F)
                        .box(1, -5, -.7, 1, 2, 1.4, 0x889A9F);
                break;
            case "obsidian_skull":
                m.box(-4, -1, -2, 8, 6, 4, 0x484051)
                        .box(-3, 5, -1.5, 6, 1, 3, 0x665C78)
                        .box(-3, -4, -1.5, 6, 3, 3, 0x3A3344);
                m.box(-3, 1, -2.2, 2.2, 2, .4, 0x181724)
                        .box(.8, 1, -2.2, 2.2, 2, .4, 0x181724)
                        .box(-.6, -.8, -2.2, 1.2, 1.8, .4, 0x181724);
                m.box(-2.5, 4, -2.2, 2, 1, .4, 0x8A739F)
                        .box(1, 4, -2.2, 1.5, 1, .4, 0x665C78)
                        .box(-2, -3.2, -1.8, .7, 1.3, .4, 0x8A739F)
                        .box(-.4, -3.2, -1.8, .8, 1.3, .4, 0x8A739F)
                        .box(1.3, -3.2, -1.8, .7, 1.3, .4, 0x8A739F);
                break;
            case "lucky_horseshoe":
                m.box(-5, -3, -1, 2, 8, 2, GOLD)
                        .box(3, -3, -1, 2, 8, 2, GOLD)
                        .box(-4, -5, -1, 8, 2, 2, GOLD)
                        .box(-3, -6, -1, 6, 1, 2, 0xA57E37);
                m.box(-4.5, 3, -1.2, 1, 1, .3, DARK)
                        .box(-4.5, 0, -1.2, 1, 1, .3, DARK)
                        .box(-3.8, -3, -1.2, 1, 1, .3, DARK)
                        .box(3.5, 3, -1.2, 1, 1, .3, DARK)
                        .box(3.5, 0, -1.2, 1, 1, .3, DARK)
                        .box(2.8, -3, -1.2, 1, 1, .3, DARK)
                        .box(-2, -4.5, -1.3, 4, .5, .4, BONE);
                break;
            case "feral_claws":
                m.box(-3, -4, -1.5, 6, 5, 3, 0x54806A)
                        .box(-3.5, -5, -1.7, 7, 2, 3.4, WOOD)
                        .box(-1, -4.5, -2, 2, 1, .5, GOLD);
                m.tube(-2, 0, 0, -3, 5, 0, .7, .25, BONE)
                        .tube(0, 1, 0, 0, 6, 0, .7, .25, BONE)
                        .tube(2, 0, 0, 3, 5, 0, .7, .25, BONE)
                        .tube(-3, -1, 0, -5, 1, 0, .7, .25, BONE);
                m.box(-2, 0, -1.7, 1, 1, .4, GOLD)
                        .box(-.5, 1, -1.7, 1, 1, .4, GOLD)
                        .box(1, 0, -1.7, 1, 1, .4, GOLD);
                break;
            case "cloud_in_a_bottle":
            case "blizzard_in_a_bottle":
            case "sandstorm_in_a_bottle":
                {
                    int tint =
                            id.startsWith("sandstorm")
                                    ? 0xD2AF70
                                    : id.startsWith("blizzard") ? 0x7FAFCB : 0xA8C8C7;
                    m.box(-3.5, -5, -1.5, 7, 1, 3, tint)
                            .box(-3.5, -4, -1.5, 1, 7, 3, tint)
                            .box(2.5, -4, -1.5, 1, 7, 3, tint)
                            .box(-2.5, 3, -1.5, 5, 1, 3, ICE)
                            .box(-1.5, 4, -1, 3, 2, 2, tint)
                            .box(-1.5, 6, -1, 3, 1, 2, WOOD);
                    m.box(-2, -3, -1, 4, 2, 2, id.startsWith("sandstorm") ? GOLD : ICE)
                            .box(-1, -1, -1, 3, 1, 2, tint)
                            .box(-2, 0, -1, 2, 1, 2, id.startsWith("sandstorm") ? BONE : BLUE)
                            .box(0, 1, -1, 2, 1, 2, ICE)
                            .box(-3, -3, -1.8, .4, 5, .4, ICE);
                    if (id.startsWith("blizzard")) m.star(0, -1, -1.8, 1.5, ICE);
                    break;
                }
            case "stormlion_body":
                m.box(-5, 3, -5, 10, 4, 10, METAL)
                        .box(-4, 6, -4, 8, 2, 8, GOLD)
                        .box(-3, 4, -7, 6, 3, 3, DARK)
                        .box(-3, 7, -2, 6, 1.5, 4, BONE);
                for (int z = -3; z <= 3; z += 3) m.box(-4, 6.5, z, 8, .5, 1, DARK);
                m.box(-2.5, 5, -7.3, 1.5, 1, .4, BLUE).box(1, 5, -7.3, 1.5, 1, .4, BLUE);
                break;
            case "stormlion_jaw":
                m.tube(0, 0, 0, -2, -1, -3, 1, 1, GOLD).tube(-2, -1, -3, 0, 0, -5, 1, .1, BONE);
                break;
            case "stormlion_leg":
                m.tube(0, 0, 0, 4, -1, 1, .6, .6, METAL).tube(4, -1, 1, 5, -4, 1, .6, .2, GOLD);
                break;
            case "flinx_body":
                m.box(-4, 2, -3, 8, 7, 6, 0xE1E7E3)
                        .box(-3, 9, -2, 6, 2, 4, 0xEFF2ED)
                        .box(-5, 4, -2, 1, 3, 4, 0xEFF2ED)
                        .box(4, 4, -2, 1, 3, 4, 0xEFF2ED)
                        .box(-3, 4, -3.2, 1.5, 1.5, .4, DARK)
                        .box(1.5, 4, -3.2, 1.5, 1.5, .4, DARK)
                        .box(-.7, 3, -3.4, 1.4, 1, .5, PURPLE)
                        .box(-2, 8, -2, 1, 4, 2, 0xE1E7E3)
                        .box(1, 8, -2, 1, 4, 2, 0xE1E7E3);
                break;
            case "flinx_foot":
                m.box(-1.5, 0, -2, 3, 2, 4, DARK).box(-1.5, 1, -1, 3, 1, 3, BONE);
                break;
            case "flinx_fur_coat":
                m.box(-4.3, -6, -2.5, 8.6, 12, 5, 0xE1E7E3)
                        .box(-.4, -6, -2.8, .8, 11, .5, WOOD)
                        .box(-4.5, 3, -2.7, 9, 2, 5.4, 0xEFF2ED)
                        .box(-4.5, -6, -2.5, 3.5, 1, 5, BONE)
                        .box(1, -6, -2.5, 3.5, 1, 5, BONE)
                        .box(-1, 0, -3, 2, 1, .5, GOLD)
                        .box(-1, -3, -3, 2, 1, .5, GOLD);
                break;
            case "flinx_sleeve":
                m.box(-2.3, -4, -2.3, 4.6, 8, 4.6, 0xE1E7E3)
                        .box(-2.5, 2, -2.5, 5, 2, 5, 0xEFF2ED)
                        .box(-2.5, -4, -2.5, 5, 1, 5, WOOD);
                break;
            case "spark":
                m.box(-1, -1, -1, 2, 2, 2, GOLD).box(-.5, 1, -.5, 1, 2, 1, BONE);
                break;
            case "frost_orb":
                m.box(-2, -3, -2, 4, 6, 4, BLUE)
                        .box(-3, -2, -2, 6, 4, 4, ICE)
                        .box(-1, -2, -2.5, 2, 4, .6, BONE);
                break;
            case "electric_bolt":
                m.box(-.5, -5, -.5, 1, 4, 1, BLUE)
                        .box(-.5, -1, -.5, 3, 1, 1, ICE)
                        .box(1.5, 0, -.5, 1, 4, 1, BLUE)
                        .box(-.5, 4, -.5, 3, 1, 1, ICE);
                break;
            default:
                throw new IllegalArgumentException("No pre-boss model: " + id);
        }
        return m;
    }
}
