package com.exoarsenal.expedition.client;

import java.util.HashMap;
import java.util.Map;

public final class SeaModels {
    private static final int DEEP = 0x254D5C,
            TEAL = 0x438D9C,
            EDGE = 0x9FD6D2,
            IVORY = 0xE6D5AC,
            CORAL = 0xD2847E,
            SHADOW = 0x423D54;
    private static final Map<String, ExpeditionMesh> CACHE = new HashMap<>();

    public static ExpeditionMesh item(String id) {
        return CACHE.computeIfAbsent(id, SeaModels::build);
    }

    private static ExpeditionMesh build(String id) {
        ExpeditionMesh m = new ExpeditionMesh();
        switch (id) {
            case "sea_remains":
                m.box(-5, -3, -1.5, 9, 3, 3, IVORY)
                        .box(-4, -.5, -1, 6, 2, 2, CORAL)
                        .box(1, -4, -1, 3, 7, 2, TEAL)
                        .box(-5, -2, -1.8, 2, .5, .4, SHADOW)
                        .box(-1, -2, -1.8, 2, .5, .4, SHADOW)
                        .tube(2, 2, 0, 4, 5, 0, 1.2, .3, EDGE);
                break;
            case "sturdy_fossil":
                m.box(-5, -4, -1, 10, 8, 2, 0x9B805F)
                        .box(-4, -3, -1.4, 8, 6, .5, 0x6E5946)
                        .tube(-3, -2, -1.8, 3, 2, -1.8, .4, .4, IVORY);
                for (int i = -2; i <= 2; i++)
                    m.tube(i, i * .5, -1.8, i - 1, i * .5 + 2, -1.8, .25, .2, IVORY);
                break;
            case "white_pearl":
            case "black_pearl":
            case "pink_pearl":
            case "giant_pearl":
                {
                    int c = id.startsWith("black") ? SHADOW : id.startsWith("pink") ? CORAL : IVORY;
                    m.box(-3, -4, -2, 6, 8, 4, c)
                            .box(-4, -3, -2, 8, 6, 4, c)
                            .box(-2, -3, -3, 4, 6, 6, c)
                            .box(-2, 1, -3.2, 2, 2, .3, EDGE)
                            .box(1, -2, -3.2, 1, 1, .3, c == SHADOW ? TEAL : IVORY);
                    break;
                }
            case "ilmeris_spark":
            case "amidias_pendant":
                m.tube(0, -4, 0, 0, 4, 0, 2.2, 2.2, DEEP)
                        .box(-1.2, -2, -2.4, 2.4, 4, .5, EDGE)
                        .tube(0, 4, 0, 0, 6, 0, .5, .5, IVORY)
                        .tube(-2, -3, 0, -4, -6, 0, .5, .1, TEAL)
                        .tube(2, -3, 0, 4, -6, 0, .5, .1, TEAL);
                break;
            case "urchin_mace":
                m.box(-.8, -8, -.8, 1.6, 10, 1.6, SHADOW)
                        .box(-1, -6, -1, 2, 1, 2, IVORY)
                        .box(-3, 1, -2.5, 6, 5, 5, CORAL)
                        .box(-2, 0, -2, 4, 7, 4, TEAL);
                for (int s : new int[] {-1, 1}) {
                    m.tube(s * 2, 2, 0, s * 5, 1, 0, 1, .1, IVORY)
                            .tube(s * 2, 5, 0, s * 4, 8, 0, 1, .1, IVORY)
                            .tube(0, 3, s * 2, 0, 3, s * 5, 1, .1, IVORY);
                }
                break;
            case "redtide_spear":
                m.box(-.55, -9, -.55, 1.1, 14, 1.1, DEEP)
                        .box(-.8, -7, -.8, 1.6, 3, 1.6, SHADOW)
                        .box(-2.5, 3, -1, 5, 1, 2, IVORY)
                        .tube(0, 4, 0, 0, 10, 0, 1.7, .1, CORAL)
                        .tube(-1.5, 3, 0, -3, 6, 0, .5, .1, IVORY)
                        .tube(1.5, 3, 0, 3, 6, 0, .5, .1, IVORY);
                break;
            case "reed_blowgun":
                m.box(-7, -1, -1, 14, 2, 2, TEAL)
                        .box(-6, -1.2, -1.2, 1, 2.4, 2.4, IVORY)
                        .box(4, -1.2, -1.2, 1, 2.4, 2.4, IVORY)
                        .box(7, -.6, -.6, .3, 1.2, 1.2, SHADOW)
                        .box(-1, -2, -1.2, 3, 1, 2.4, CORAL);
                break;
            case "coral_spout":
                m.box(-.7, -8, -.7, 1.4, 11, 1.4, DEEP)
                        .tube(0, 1, 0, -3, 4, 0, 1, .7, CORAL)
                        .tube(-3, 4, 0, -3, 7, 0, .7, .5, CORAL)
                        .tube(0, 2, 0, 2, 5, 0, 1.3, 1, IVORY)
                        .tube(2, 5, 0, 2, 8, 0, 1, .8, TEAL)
                        .box(1.5, 7.7, -.5, 1, .4, 1, SHADOW);
                break;
            case "cnidarian":
                m.box(-.6, -8, -.6, 1.2, 11, 1.2, DEEP)
                        .box(-3, 3, -2, 6, 2, 4, CORAL)
                        .box(-2, 5, -1.5, 4, 1.5, 3, EDGE);
                for (int x : new int[] {-2, 0, 2}) m.tube(x, 3, 0, x + .6, .5, 0, .3, .15, IVORY);
                break;
            case "fishbone_boomerang":
                m.tube(-5, -5, 0, 0, 1, 0, 1.1, 1.1, IVORY)
                        .tube(0, 1, 0, 5, 6, 0, 1.1, .7, IVORY)
                        .tube(-3, -2, 0, -6, 0, 0, .4, .1, TEAL)
                        .tube(-1, 0, 0, -3, 3, 0, .4, .1, TEAL)
                        .tube(2, 3, 0, 0, 6, 0, .4, .1, TEAL)
                        .box(3, 4, -1.2, 1, 1, .4, SHADOW);
                break;
            case "shield_of_the_ocean":
                m.box(-5, -5, -1, 10, 10, 2, DEEP)
                        .box(-4, -7, -1, 8, 14, 2, TEAL)
                        .box(-3, -6, -1.3, 6, 12, .5, IVORY)
                        .box(-2, -5, -1.6, 4, 10, .5, TEAL)
                        .star(0, 0, -2, 3, CORAL)
                        .box(-2, -2, 1, 4, 1, 2, SHADOW)
                        .box(-2, 1, 1, 4, 1, 2, SHADOW);
                break;
            case "greatbay_pickaxe":
                m.box(-.7, -8, -.7, 1.4, 13, 1.4, DEEP)
                        .tube(-5, 3, 0, 0, 5, 0, 1.2, 1.5, IVORY)
                        .tube(0, 5, 0, 6, 2, 0, 1.5, .3, TEAL)
                        .box(-1, 3, -1.5, 2, 3, 3, CORAL);
                break;
            case "reefclaw_hamaxe":
                m.box(-.7, -8, -.7, 1.4, 13, 1.4, DEEP)
                        .box(-5, 2, -2, 4, 4, 4, IVORY)
                        .box(-6, 2.5, -1.6, 1, 3, 3.2, TEAL)
                        .tube(1, 4, 0, 5, 5, 0, 2, 2.5, CORAL)
                        .tube(5, 5, 0, 5, 0, 0, 2.5, .6, IVORY);
                break;
            case "victide_shellmet":
            case "victide_coral_turban":
            case "victide_hermit_helmet":
            case "victide_mask":
            case "victide_headcrab":
                m.box(-4.5, 1, -4.5, 9, 3.5, 9, TEAL)
                        .box(-4.8, -3, -3.5, 1.2, 5, 7, IVORY)
                        .box(3.6, -3, -3.5, 1.2, 5, 7, IVORY)
                        .box(-4, -3, 3, 8, 5, 1.5, DEEP)
                        .box(-4.5, 1, -4.8, 9, 1, .5, IVORY);
                if (id.endsWith("shellmet")) {
                    m.box(-1, 2, -5, 2, 5, 9, IVORY).box(-.5, -2, -4.9, 1, 4, .6, TEAL);
                } else if (id.endsWith("turban")) {
                    m.box(-5, 2, -4.5, 10, 1, 9, CORAL)
                            .tube(-3, 3, 0, -5, 7, 0, .8, .5, CORAL)
                            .tube(-5, 6, 0, -7, 7, 0, .5, .2, IVORY);
                } else if (id.endsWith("helmet")) {
                    m.box(-3, 4, -2, 6, 2, 6, IVORY)
                            .box(-2, 6, -1, 4, 2, 4, CORAL)
                            .box(-1, 8, 0, 2, 1, 2, IVORY);
                } else if (id.endsWith("mask")) {
                    m.box(-3.8, -3, -4.7, 7.6, 5, .8, IVORY)
                            .box(-3, -.5, -5, 2, 1, .3, DEEP)
                            .box(1, -.5, -5, 2, 1, .3, DEEP)
                            .tube(0, -3, -4, 0, -5, -5, .7, .1, CORAL);
                } else {
                    m.box(-4, 4, -3, 8, 2, 6, CORAL);
                    for (int side : new int[] {-1, 1}) {
                        m.tube(side * 3, 4, 0, side * 6, 6, -1, .7, .4, IVORY)
                                .box(side * 5.5 - .5, 5, -2, 1, 2, 1, DEEP);
                    }
                }
                break;
            case "victide_breastplate":
                m.box(-4.4, -5, -2.5, 8.8, 10, 5, DEEP)
                        .box(-4, -3, -3, 8, 7, 1, TEAL)
                        .box(-3, 2, -3.3, 6, 2, .6, IVORY)
                        .box(-1, -2, -3.4, 2, 4, .6, CORAL)
                        .box(-6, 1, -2, 2, 4, 4, IVORY)
                        .box(4, 1, -2, 2, 4, 4, IVORY);
                break;
            case "victide_greaves":
                m.box(-4, 2, -2, 8, 3, 4, TEAL)
                        .box(-4, -6, -2, 3.5, 8, 4, DEEP)
                        .box(.5, -6, -2, 3.5, 8, 4, DEEP)
                        .box(-4, -2, -2.5, 3.5, 2, 1, IVORY)
                        .box(.5, -2, -2.5, 3.5, 2, 1, IVORY);
                break;
            case "armor_leg":
                m.box(-2, -5, -2, 4, 10, 4, DEEP)
                        .box(-2.2, -1, -2.7, 4.4, 3, 1, IVORY)
                        .box(-1.6, 2, -2.4, 3.2, 3, .7, TEAL)
                        .box(-2, -5, -2.4, 4, 1, .6, CORAL);
                break;
            case "armor_arm":
                m.box(-2.3, -4, -2.3, 4.6, 8, 4.6, TEAL)
                        .box(-2.6, 2, -2.6, 5.2, 2, 5.2, IVORY)
                        .box(-2.5, -3, -2.5, 5, 1, 5, CORAL);
                break;
            case "clam_lower":
                m.box(-7, 0, -5, 14, 2, 10, DEEP)
                        .box(-8, 2, -6, 16, 1.5, 12, TEAL)
                        .box(-6, 3, -4, 12, .5, 8, CORAL);
                break;
            case "clam_upper":
                m.box(-8, 0, -6, 16, 1.5, 12, IVORY)
                        .box(-7, 1.5, -5, 14, 2, 10, TEAL)
                        .box(-5, 3.5, -4, 10, 1.5, 8, TEAL)
                        .box(-3, 5, -2, 6, 1, 4, IVORY);
                for (int x = -6; x <= 6; x += 2) m.box(x - .3, 1, -5.8, .6, 2, 10.6, EDGE);
                break;
            case "ray_body":
                m.box(-3, 0, -5, 6, 2, 9, TEAL)
                        .box(-2, 1.5, -4, 4, 1, 6, EDGE)
                        .tube(0, 1, 3, 0, .5, 10, 1, .3, DEEP)
                        .tube(0, .5, 10, 1, 1, 15, .3, .1, IVORY)
                        .box(-2, 2, -4, 1, .5, 1, SHADOW)
                        .box(1, 2, -4, 1, .5, 1, SHADOW);
                break;
            case "ray_wing":
                m.box(0, 0, -4, 3, 1, 8, TEAL)
                        .box(3, 0, -3, 3, .8, 6, TEAL)
                        .box(6, 0, -1.5, 2, .5, 3, EDGE);
                break;
            case "bell":
                m.box(-4, 0, -4, 8, 2, 8, TEAL)
                        .box(-3, 2, -3, 6, 2, 6, EDGE)
                        .box(-2, 4, -2, 4, 1, 4, IVORY)
                        .box(-2, -1, -2, 4, 2, 4, CORAL);
                break;
            case "tentacle":
                m.tube(0, 0, 0, .6, -3, 0, .3, .2, EDGE).tube(.6, -3, 0, 0, -6, .5, .2, .1, TEAL);
                break;
            case "prism_back":
                m.box(-4, 0, -3, 8, 3, 6, DEEP)
                        .box(-3, 3, -2, 6, 1, 4, TEAL)
                        .tube(-2, 3, 0, -3, 8, 0, 1.2, .1, EDGE)
                        .tube(1, 3, 0, 2, 10, 0, 1.5, .1, TEAL)
                        .box(-3, 1, -3.2, 1, 1, .4, IVORY)
                        .box(2, 1, -3.2, 1, 1, .4, IVORY);
                break;
            case "minnow":
                m.box(-1, 0, -3, 2, 2, 5, TEAL)
                        .box(-.5, .5, -3.3, 1, 1, .4, SHADOW)
                        .box(-2, .4, 2, 4, 1, 1, EDGE)
                        .box(-1, 1.8, -1, 2, .5, 2, IVORY);
                break;
            case "floaty":
                m.box(-3, -1, -3, 6, 4, 6, CORAL)
                        .box(-2, 3, -2, 4, 1, 4, IVORY)
                        .tube(-3, 1, 0, -5, 2, 0, .5, .2, TEAL)
                        .tube(3, 1, 0, 5, 2, 0, .5, .2, TEAL)
                        .box(-1, -1, -3.2, 2, 2, .4, DEEP);
                break;
            case "cnidrion_body":
                m.box(-3, 9, -2, 6, 11, 5, CORAL)
                        .box(-2.5, 10, -3, 5, 9, 1.5, IVORY)
                        .box(-2, 6, 0, 4, 5, 4, TEAL)
                        .tube(0, 7, 1, 1, 3, 3, 1.8, 1.3, CORAL)
                        .tube(1, 3, 3, 0, 2, 6, 1.3, .8, CORAL)
                        .tube(0, 2, 6, -2, 4, 6, .8, .4, IVORY);
                for (int y = 10; y <= 18; y += 3) m.box(-2.5, y, -3.3, 5, .5, .5, DEEP);
                break;
            case "cnidrion_head":
                m.box(-3, -2, -3, 6, 5, 6, CORAL)
                        .box(-2.5, -1, -5, 5, 3, 3, IVORY)
                        .box(-1.2, -.3, -8, 2.4, 1.6, 4, TEAL)
                        .box(-.7, 0, -8.2, 1.4, 1, .3, SHADOW)
                        .box(-3.2, .5, -2, 1, 1, 1, DEEP)
                        .box(2.2, .5, -2, 1, 1, 1, DEEP)
                        .tube(0, 2, 1, 0, 6, 3, 1, .2, IVORY);
                break;
            case "snail":
                m.box(-4, 0, -2, 8, 1, 4, TEAL)
                        .box(-2, 1, -2.5, 5, 4, 5, IVORY)
                        .box(-1, 5, -1.5, 3, 1, 3, CORAL)
                        .box(-1, 2, -2.8, 2, 2, .4, TEAL)
                        .tube(-3, 1, -1, -4, 3, -1, .2, .1, IVORY)
                        .tube(-3, 1, 1, -4, 3, 1, .2, .1, IVORY);
                break;
            case "water_bolt":
                m.tube(0, -5, 0, 0, 2, 0, .5, 1.7, TEAL)
                        .tube(0, 2, 0, 0, 5, 0, 1.7, .3, EDGE)
                        .box(-.4, 0, -1, .8, 3, .5, IVORY);
                break;
            case "bubble":
                m.tube(0, -2, 0, 0, 2, 0, 2, 2, EDGE, .35F)
                        .box(-1, 1, -2.2, 1, 1, .3, IVORY)
                        .box(1, -1, -2.2, .6, .6, .3, EDGE);
                break;
            case "coral_dart":
                m.tube(0, -4, 0, 0, 4, 0, .8, .1, CORAL).box(-.3, -3, -.9, .6, 2, .3, IVORY);
                break;
            case "urchin":
                m.box(-2, -2, -2, 4, 4, 4, TEAL);
                for (int side : new int[] {-1, 1}) {
                    m.tube(side * 2, 0, 0, side * 5, 0, 0, .8, .1, IVORY)
                            .tube(0, side * 2, 0, 0, side * 5, 0, .8, .1, CORAL)
                            .tube(0, 0, side * 2, 0, 0, side * 5, .8, .1, IVORY);
                }
                break;
            case "king_body":
                m.box(-4, 12, -2, 8, 11, 4, TEAL)
                        .box(-4, 11, -2.5, 8, 2, 5, IVORY)
                        .box(-3, 14, -2.4, 6, 7, .6, DEEP)
                        .box(-.7, 18, -2.8, 1.4, 2, .5, CORAL);
                break;
            case "king_head":
                m.box(-3.5, -3, -3.5, 7, 7, 7, 0xA9C6B0)
                        .box(-3, 2, -3.7, 6, 2, 1, IVORY)
                        .box(-3.8, -2, -2, 1, 4, 5, IVORY)
                        .box(2.8, -2, -2, 1, 4, 5, IVORY)
                        .box(-2.5, 0, -3.7, 1.5, .6, .4, DEEP)
                        .box(1, 0, -3.7, 1.5, .6, .4, DEEP)
                        .box(-3, 4, -3, 6, 1, 6, TEAL)
                        .box(-.5, 5, -3, 1, 2, 1, IVORY);
                break;
            case "king_arm":
                m.box(-1.5, -10, -1.5, 3, 10, 3, TEAL)
                        .box(-1.5, -11, -1.5, 3, 3, 3, 0xA9C6B0)
                        .box(-1.7, -8, -1.7, 3.4, 1, 3.4, IVORY);
                break;
            case "king_leg":
                m.box(-1.7, -11, -1.7, 3.4, 11, 3.4, DEEP).box(-1.8, -12, -2.5, 3.6, 2, 4.3, TEAL);
                break;
            default:
                return PrebossModels.item(id);
        }
        return m;
    }
}
