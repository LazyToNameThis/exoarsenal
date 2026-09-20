package com.exoarsenal.expedition.client;

import java.util.*;

public final class WulfrumArsenalModels {
    public static final int DARK = 0x182B29,
            METAL = 0x586C59,
            IVORY = 0xC1C7AD,
            EDGE = 0x829582,
            GREEN = 0x4CE89B,
            CORE = 0xDCFFE2;
    private static final Map<String, ExpeditionMesh> MODELS = new HashMap<>();

    public static ExpeditionMesh get(String id) {
        return MODELS.computeIfAbsent(id, WulfrumArsenalModels::build);
    }

    public static boolean owns(String id) {
        return id.startsWith("pg_c_")
                || id.equals("energized_wulfrum_scrap")
                || id.equals("wulfrum_heart");
    }

    private static ExpeditionMesh build(String id) {
        ExpeditionMesh m = new ExpeditionMesh();
        switch (id) {
            case "pg_c_awl":
                m.box(-7, -3, -2.8, 11, 6, 5.6, DARK)
                        .box(-6, -2, -3, 8, 4, 6, METAL)
                        .box(-5, 2, -2.5, 8, 1.3, 5, IVORY);
                m.box(-6, -2.4, -3.2, 3, 4.8, .4, IVORY)
                        .box(-2, -1.5, -3.15, 4, 3, .4, EDGE)
                        .box(-1, -.5, -3.4, 3, 1, .3, GREEN);
                m.box(2, -2, -2, 3, 4, 4, IVORY).box(4, -1.3, -1.5, 2, 2.6, 3, DARK);
                m.plate(
                        new double[][] {
                            {5, -.4}, {9, -1.3}, {20, -.7}, {28, 1}, {20, 2.3}, {10, 2.6}, {5, 1.4}
                        },
                        .35,
                        GREEN);
                m.plate(
                        new double[][] {{6, .4}, {11, .2}, {23, 1.1}, {11, 1.6}, {6, 1.1}},
                        .38,
                        CORE);
                m.box(-5, -6, -1.5, 2, 3, 3, METAL).box(-5, -5.5, -1.7, 2, .6, 3.4, EDGE);
                for (int x : new int[] {-4, -2, 0}) m.box(x, 2.8, -2, 1, 1.2, 4, EDGE);
                break;
            case "pg_c_little_boy":
                m.box(-7, -2.5, -2.5, 11, 5, 5, DARK)
                        .box(-6, -1.8, -2.8, 8, 3.6, 5.6, METAL)
                        .box(-4, 2, -2.4, 7, 1.4, 4.8, IVORY);
                m.box(-6, -6, -1.5, 2.5, 4, 3, METAL)
                        .box(-7, -6, -1.8, 3.5, 1, 3.6, EDGE)
                        .box(0, -4, -2, 3, 2, 4, DARK);
                m.box(-9, -1.5, -2, 3, 3, 4, IVORY).box(-10, -.8, -1.5, 1, 1.6, 3, GREEN);
                m.box(3, -2.3, -2.3, 2, 4.6, 4.6, IVORY)
                        .box(4, -1.5, -1.5, 10, 3, 3, DARK)
                        .box(13, -2, -2, 2, 4, 4, METAL);
                for (int x : new int[] {5, 8, 11}) {
                    m.box(x, 1.5, -1.8, 1, 1, 3.6, EDGE).box(x, -2.5, -1.8, 1, 1, 3.6, EDGE);
                }
                m.box(5, -.5, -1.8, 9, 1, .4, GREEN)
                        .box(15, -1.2, -1.2, .5, 2.4, 2.4, GREEN)
                        .box(15.5, -.4, -.4, .1, .8, .8, CORE);
                m.box(-3, 3.4, -.6, 4, .8, 1.2, DARK).box(-1, 3.5, -.7, 1, .8, 1.4, GREEN);
                break;
            case "pg_c_talons":
                m.box(-6, -2.8, -3.2, 9, 5.6, 6.4, DARK)
                        .box(-5, -2.2, -3.5, 7, 4.4, 7, METAL)
                        .box(-4, 2, -3.3, 6, 1.2, 6.6, IVORY);
                m.box(2, -2.4, -3, 2, 4.8, 6, EDGE);
                for (int z : new int[] {-2, 0, 2}) {
                    m.box(3, -1, z - .5, 3, 2, 1, DARK);
                    m.tube(5, .4, z, 13, 1.5, z, .45, .2, GREEN)
                            .tube(13, 1.5, z, 17, -1.8, z, .2, .05, CORE);
                }
                m.box(-3, -.6, -3.8, 4, 1.2, .4, GREEN);
                m.box(-5, -5, -1.8, 2, 2.3, 3.6, IVORY);
                break;
            case "pg_c_striker":
                m.box(-5, -1, -1.2, 7, 2, 2.4, DARK)
                        .box(-4, -1.2, -1.3, 1, 2.4, 2.6, EDGE)
                        .box(-1, -1.2, -1.3, 1, 2.4, 2.6, EDGE);
                m.box(2, -3, -1, 1.4, 6, 2, IVORY).box(3, -1.3, -.8, 2, 2.6, 1.6, METAL);
                m.plate(
                                new double[][] {{4, -.8}, {8, -1.7}, {17, 0}, {8, 1.7}, {4, .8}},
                                .25,
                                GREEN)
                        .plate(new double[][] {{5, -.2}, {15, 0}, {5, .4}}, .28, CORE);
                m.box(-7, -1.3, -1.3, 2, 2.6, 2.6, METAL);
                break;
            case "energized_wulfrum_scrap":
                m.plate(
                        new double[][] {
                            {-6, -3}, {-2, -4}, {0, -2}, {5, -3}, {6, 0}, {2, 3}, {-4, 4}, {-6, 1}
                        },
                        1,
                        METAL);
                m.box(-5, -1, -1.3, 4, 2, .4, IVORY)
                        .box(1, -2, -1.3, 3, 1, .4, EDGE)
                        .box(-1, 0, -1.5, 1, 3, .4, GREEN)
                        .box(0, 2, -1.5, 3, 1, .4, GREEN);
                break;
            case "wulfrum_heart":
                m.plate(
                        new double[][] {
                            {-6, 3}, {-4, 6}, {-1, 6}, {0, 4}, {1, 6}, {4, 6}, {6, 3}, {5, -1},
                            {0, -7}, {-5, -1}
                        },
                        1.8,
                        DARK);
                m.plate(
                        new double[][] {
                            {-4, 3}, {-3, 4}, {-1, 4}, {0, 2}, {2, 4}, {4, 3}, {3, 0}, {0, -4},
                            {-3, 0}
                        },
                        1.9,
                        GREEN);
                m.box(-1, -2, -2.1, 2, 6, .4, CORE)
                        .box(-4, 1, -2.2, 8, 1, .3, IVORY)
                        .box(-2, -5, -2, 4, 1, .3, METAL);
                for (int s : new int[] {-1, 1})
                    m.tube(s * 4, 4, 0, s * 7, 1, 0, .6, .6, IVORY)
                            .tube(s * 7, 1, 0, s * 4, -3, 0, .6, .6, METAL);
                break;
            case "head":
                m.box(-3.6, -3.3, -3.2, 7.2, 6.6, 6.4, DARK)
                        .box(-3.8, -1.7, -3.5, 7.6, 3.4, 1, METAL)
                        .box(-3, -2.5, -3.7, 6, 1, 1, IVORY);
                m.box(-2.6, -.7, -3.8, 5.2, 1.5, .4, GREEN)
                        .box(-1, -.5, -4, 2, 1, .3, CORE)
                        .box(-3, 1.7, -3.5, 6, 1.3, 1, EDGE);
                m.box(-4, -1, -1.5, 1, 3, 3, IVORY)
                        .box(3, -1, -1.5, 1, 3, 3, IVORY)
                        .box(2, 3, -1, .6, 3, .6, EDGE);
                break;
            case "torso":
                m.box(-4, -6, -2.2, 8, 12, 4.4, DARK)
                        .box(-4.5, 1, -2.7, 9, 4, 5.4, METAL)
                        .box(-4, 4, -2.8, 8, 1.4, 5.6, IVORY);
                m.box(-1.5, 1.8, -3, 3, 2.2, .5, GREEN).box(-.6, 2.1, -3.3, 1.2, 1.6, .4, CORE);
                m.box(-3.5, -3.5, -2.6, 7, 3, 1, EDGE).box(-2.8, -5.5, -2.4, 5.6, 1.5, 4.8, METAL);
                m.box(-2.5, 0, 2, 5, 5, 2, DARK);
                for (int x : new int[] {-3, 2}) m.box(x, 1, 3, 1, 3, 1, GREEN);
                break;
            case "upper_arm":
                m.box(-1.5, -5, -1.5, 3, 5, 3, DARK)
                        .box(-2, -2, -2, 4, 3, 4, IVORY)
                        .box(-1.8, -4, -1.8, 3.6, 2, 3.6, METAL);
                break;
            case "forearm":
                m.box(-1.8, -5.5, -1.8, 3.6, 5.5, 3.6, METAL)
                        .box(-2.1, -2, -2.1, 4.2, 1.6, 4.2, IVORY)
                        .box(-.5, -4.5, -2.1, 1, 2, .4, GREEN);
                break;
            case "palm":
                m.box(-1.6, -2, -1, 3.2, 2, 2, DARK).box(-1.7, -1.4, -1.2, 3.4, 1.2, .5, IVORY);
                break;
            case "finger":
                m.box(-.35, -1.4, -.4, .7, 1.4, .8, METAL).box(-.4, -.9, -.5, .8, .5, .3, IVORY);
                break;
            case "leg":
                m.box(-1.7, -5, -1.8, 3.4, 5, 3.6, DARK)
                        .box(-2, -3, -2.1, 4, 3, 4.2, METAL)
                        .box(-1.6, -4.7, -2.3, 3.2, 1.3, .6, IVORY);
                break;
            case "shin":
                m.box(-1.8, -5, -1.7, 3.6, 5, 3.4, METAL)
                        .box(-2, -2, -2.2, 4, 2, 1, IVORY)
                        .box(-.5, -4, -2.1, 1, 2, .4, GREEN)
                        .box(-2, -6, -3.5, 4, 1.6, 5.5, DARK)
                        .box(-1.9, -5.8, -3.8, 3.8, 1, 1, EDGE);
                break;
            case "laser":
                m.box(-5, -.15, -.15, 10, .3, .3, CORE).box(-4, -.3, -.3, 8, .6, .6, GREEN);
                break;
        }

        if (id.equals("pg_c_awl"))
            for (int side : new int[] {-1, 1}) {
                double z = side * 3.25;
                m.box(-5.6, -1.7, z, 2.1, .45, .18, EDGE).box(-5.6, 1.3, z, 2.1, .4, .18, DARK);
                m.box(-2.5, -1.2, z, 3.6, .35, .18, DARK)
                        .box(-2.5, 1, z, 3.6, .35, .18, IVORY)
                        .box(-1.5, -.4, side * 3.5, 2.2, .65, .2, GREEN);
                m.box(-5.5, -1.2, side * 3.4, .45, .45, .2, CORE)
                        .box(.7, 1.2, side * 3.4, .4, .4, .2, IVORY);
            }
        if (id.equals("pg_c_little_boy"))
            for (int side : new int[] {-1, 1}) {
                double z = side * 2.9;
                m.box(-5.5, -1.3, z, 6, .4, .2, EDGE)
                        .box(-4.8, 1, z, 4.4, .4, .2, IVORY)
                        .box(-4, -.7, z, 3, 1.4, .25, DARK)
                        .box(-3, -.35, side * 3.15, 1.7, .7, .15, GREEN);
                for (int x : new int[] {-5, 0, 2})
                    m.box(x, -1.5, side * 3.15, .45, .45, .15, IVORY);
                m.box(-8, -.7, side * 2.3, 1.5, 1.4, .3, DARK)
                        .box(6, -.35, side * 1.9, 5, .7, .2, GREEN);
            }
        if (id.equals("pg_c_talons"))
            for (int side : new int[] {-1, 1}) {
                double z = side * 3.6;
                m.box(-4.5, 1.4, z, 5, .4, .2, IVORY)
                        .box(-4.5, -1.5, z, 5, .4, .2, DARK)
                        .box(-4.3, -.8, z, 1.2, 1.6, .2, EDGE)
                        .box(-2.8, -.45, side * 3.8, 3.3, .9, .2, GREEN);
                for (int x : new int[] {-4, 0}) m.box(x, 1, side * 3.85, .45, .45, .15, CORE);
            }
        return m;
    }
}
