package com.exoarsenal.expedition.client;

import java.util.*;

public final class CaveModels {
    private static final Map<String, ExpeditionMesh> CACHE = new HashMap<>();

    public static ExpeditionMesh part(String id) {
        return CACHE.computeIfAbsent(id, CaveModels::build);
    }

    private static ExpeditionMesh build(String id) {
        ExpeditionMesh m = new ExpeditionMesh();
        int stone = 0x8C7B84,
                edge = 0xBBA5A8,
                bone = 0xD8CBB0,
                bronze = 0xB79558,
                dark = 0x48434D,
                blue = 0x87B5D5;
        switch (id) {
            case "golem":
                m.box(-5, 8, -3, 10, 10, 6, stone)
                        .box(-6, 12, -2, 12, 4, 5, edge)
                        .box(-3, 18, -2.5, 6, 5, 5, stone)
                        .box(-2, 20, -2.8, 1.3, 1, .4, blue)
                        .box(.7, 20, -2.8, 1.3, 1, .4, blue)
                        .box(-2, 9, -3.4, 4, 5, .6, dark);
                break;
            case "stone_arm":
                m.box(-2, -5, -2, 4, 6, 4, stone)
                        .box(-2.5, -9, -2.5, 5, 4.5, 5, edge)
                        .box(-1, -3, -2.3, 2, 2, .5, dark);
                break;
            case "stone_leg":
                m.box(-2, 0, -3, 4, 3, 6, edge).box(-1.8, 2, -1.8, 3.6, 7, 3.6, stone);
                break;
            case "elemental":
                m.box(-4, -4, -3, 8, 8, 6, stone)
                        .box(-2, -2, -3.4, 4, 4, .6, blue)
                        .box(-1, -1, -3.8, 2, 2, .5, bone);
                break;
            case "fragment":
                m.box(-2, -1.5, -1.5, 4, 3, 3, stone).box(-1, 1.5, -1, 2, 1, 2, edge);
                break;
            case "hoplite":
                m.box(-3, 12, -1.5, 6, 7, 3, bone)
                        .box(-3.5, 16, -2, 7, 2, 4, bronze)
                        .box(-.5, 12, -1.9, 1, 7, .5, dark)
                        .box(-2.5, 20, -2.5, 5, 5, 5, bone)
                        .box(-3, 23, -3, 6, 3, 6, bronze)
                        .box(-.5, 26, -2, 1, 2, 4, 0x92535C)
                        .box(-2, 21, -2.8, 1, 1, .4, dark)
                        .box(1, 21, -2.8, 1, 1, .4, dark)
                        .box(-3.5, 9, -2, 7, 3, 4, bronze);
                break;
            case "bone_arm":
                m.tube(0, 0, 0, 0, -5, 0, .8, .7, bone)
                        .box(-1, -6, -1, 2, 2, 2, bronze)
                        .tube(0, -6, 0, 0, -10, 0, .7, .6, bone)
                        .box(-1, -11, -1, 2, 2, 2, bone);
                break;
            case "bone_leg":
                m.tube(0, 2, 0, 0, 11, 0, .8, .9, bone)
                        .box(-1.5, 1, -2, 3, 2, 4, bronze)
                        .box(-1, 7, -1, 2, 1, 2, dark);
                break;
            case "shield":
                m.box(-3, -4, -.6, 6, 8, 1.2, bronze)
                        .box(-2, -3, -1, 4, 6, .5, dark)
                        .box(-1, -1.5, -1.5, 2, 3, .8, bronze);
                break;
            case "bat":
                m.box(-2, -2, -2, 4, 5, 4, 0x6C78A2)
                        .box(-3, 2, -2, 6, 2, 4, blue)
                        .box(-2, 4, -1, 4, 1, 2, bone)
                        .box(-1.5, 0, -2.3, 1, 1, .4, bone)
                        .box(.5, 0, -2.3, 1, 1, .4, bone);
                break;
            case "wing":
                m.plate(
                                new double[][] {
                                    {0, 0}, {3, 3}, {8, 2}, {11, -2}, {7, -1}, {5, -4}, {3, -2},
                                    {0, -3}
                                },
                                .3,
                                0x7A83B4)
                        .tube(0, 0, 0, 8, 2, 0, .3, .15, bone)
                        .tube(3, 1, 0, 5, -4, 0, .2, .1, blue);
                break;
            case "spider":
                m.box(-4, 3, -1, 8, 5, 8, 0x52475E)
                        .box(-3, 4, -5, 6, 4, 5, 0x87788C)
                        .box(-2, 5, -5.3, 1, 1, .4, 0xD5A7A2)
                        .box(1, 5, -5.3, 1, 1, .4, 0xD5A7A2)
                        .tube(-1, 4, -4, -2, 2, -7, .4, .1, bone)
                        .tube(1, 4, -4, 2, 2, -7, .4, .1, bone);
                break;
            case "spider_leg":
                m.tube(0, 0, 0, 5, 2, 1, .45, .35, 0x87788C)
                        .tube(5, 2, 1, 8, -4, 2, .35, .15, 0x52475E);
                break;
            default:
                throw new IllegalArgumentException(id);
        }
        return m;
    }
}
