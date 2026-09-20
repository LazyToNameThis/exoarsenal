package com.scapeandrun.frostbite.expedition.client;

import com.scapeandrun.frostbite.expedition.WildSpecies;
import java.util.*;
import static com.scapeandrun.frostbite.expedition.WildSpecies.*;

public final class WildModels {
    private static final Map<String, ExpeditionMesh> CACHE = new HashMap<>();

    public static ExpeditionMesh part(WildSpecies species, String part) {
        return CACHE.computeIfAbsent(species.id + ":" + part, key -> build(species, part));
    }

    private static ExpeditionMesh build(WildSpecies s, String part) {
        ExpeditionMesh m = new ExpeditionMesh();
        int c = s.color, dark = 0x443C4D, bone = 0xD8CDB0, eye = 0xEDD387, green = 0x648554;
        switch (part) {
            case "wing":
                if (s.shape == Shape.HORNET && s != DEMON)
                    m.plate(
                                    new double[][] {
                                        {0, 0}, {4, 3}, {10, 2}, {12, -1}, {8, -3}, {2, -2}
                                    },
                                    .18,
                                    0xC7C9B1)
                            .tube(0, 0, 0, 10, 1, 0, .25, .12, dark);
                else
                    m.plate(
                                    new double[][] {
                                        {0, 1}, {4, 4}, {8, 3}, {12, -2}, {8, -1}, {7, -4}, {4, -2},
                                        {2, -4}, {0, -2}
                                    },
                                    .22,
                                    c)
                            .tube(0, 1, 0, 4, 4, 0, .3, .3, bone)
                            .tube(4, 4, 0, 12, -2, 0, .3, .1, bone)
                            .tube(4, 4, 0, 7, -4, 0, .22, .12, dark);
                break;
            case "leg":
                if (s.shape == Shape.HUMAN || s == DEMON)
                    m.box(-1.4, -8, -1.4, 2.8, 8, 2.8, s == TIM || s == DEMON ? c : bone)
                            .box(-1.6, -9, -2.7, 3.2, 2, 4.2, dark);
                else m.tube(0, 0, 0, 4, 2, 0, .5, .35, c).tube(4, 2, 0, 7, -3, 1, .35, .16, dark);
                break;
            case "arm":
                m.tube(0, 0, 0, 0, -5, 0, 1.25, 1.1, c)
                        .tube(0, -5, 0, 0, -9, -1, 1.1, .9, s == FACE_MONSTER ? c : bone)
                        .box(-1.2, -10, -2, 2.4, 2, 2.5, bone);
                break;
            case "jaw":
                m.box(-3, -1, -3, 6, 2, 6, c)
                        .box(-2.5, 1, -3.1, 1, 1.5, 1, bone)
                        .box(1.5, 1, -3.1, 1, 1.5, 1, bone)
                        .box(-.5, 1, -3.1, 1, 1, 1, bone);
                break;
            case "segment":
                m.box(-3, -2, -3.4, 6, 4, 6.8, c)
                        .box(-3.3, 1, -2.7, 6.6, 1.5, 5.4, dark)
                        .box(-2.5, 2, -1, 5, 1, 2, bone);
                break;
            case "stem":
                m.box(-.45, 0, -.45, .9, 16, .9, green).box(-.7, 6, -.7, 1.4, 1.5, 1.4, c);
                break;
            case "tail":
                m.tube(0, 0, 0, 0, 0, 6, 2, 1, c).tube(0, 0, 6, 0, 1, 11, 1, .1, dark);
                break;
            case "body":
                if (s == CURSED_SKULL) {
                    m.box(-4, 2, -3, 8, 7, 6, bone)
                            .box(-3, 5, -3.4, 2, 2, .5, dark)
                            .box(1, 5, -3.4, 2, 2, .5, dark)
                            .box(-.5, 3, -3.5, 1, 1, .5, dark)
                            .box(-3, 1, -3, 1.5, 2, 1, bone)
                            .box(-.7, 1, -3, 1.4, 2, 1, bone)
                            .box(1.5, 1, -3, 1.5, 2, 1, bone);
                    return m;
                }
                if (s == DEMON) {
                    m.box(-3, 8, -2, 6, 8, 4, c)
                            .box(-2.5, 17, -2.5, 5, 5, 5, c)
                            .tube(-2, 21, 0, -4, 25, 1, .7, .1, bone)
                            .tube(2, 21, 0, 4, 25, 1, .7, .1, bone)
                            .box(-1.8, 19, -2.8, 1, 1, .4, eye)
                            .box(.8, 19, -2.8, 1, 1, .4, eye)
                            .tube(0, 9, 2, 0, 3, 6, .7, .2, c);
                    return m;
                }
                switch (s.shape) {
                    case BAT:
                        m.box(-2, 2, -2, 4, 5, 4, c)
                                .box(-2.5, 6, -2.5, 5, 3, 4, c)
                                .box(-2, 9, -1, 1, 2, 1.5, dark)
                                .box(1, 9, -1, 1, 2, 1.5, dark)
                                .box(-1.8, 7, -2.8, 1, 1, .5, eye)
                                .box(.8, 7, -2.8, 1, 1, .5, eye)
                                .box(-1, 5, -2.8, .5, 1, .5, bone)
                                .box(.5, 5, -2.8, .5, 1, .5, bone);
                        break;
                    case WORM:
                        m.box(-3, 1, -4, 6, 5, 7, c)
                                .box(-3.5, 4, -3, 7, 2, 5, dark)
                                .box(-2, 3, -4.3, 1.2, 1, .5, eye)
                                .box(.8, 3, -4.3, 1.2, 1, .5, eye)
                                .tube(-2, 2, -4, -3, 1, -7, .6, .1, bone)
                                .tube(2, 2, -4, 3, 1, -7, .6, .1, bone);
                        break;
                    case SLIME:
                        m.box(-5, 0, -4, 10, 4, 8, c)
                                .box(-4, 4, -3.5, 8, 3, 7, c)
                                .box(-2.8, 7, -2.5, 5.6, 1.5, 5, c)
                                .box(-2.5, 4, -3.8, 1.3, 1.5, .5, dark)
                                .box(1.2, 4, -3.8, 1.3, 1.5, .5, dark)
                                .box(-1, 2.8, -4.2, 2, .5, .4, dark);
                        if (s == SPIKED_ICE_SLIME || s == SPIKED_JUNGLE_SLIME)
                            m.tube(-3, 6, 0, -4, 10, 0, .7, .1, bone)
                                    .tube(0, 8, 0, 0, 12, 0, .8, .1, bone)
                                    .tube(3, 6, 0, 4, 10, 0, .7, .1, bone);
                        break;
                    case HUMAN:
                        m.box(-3, 10, -2, 6, 8, 4, s == SPORE_SKELETON ? bone : c)
                                .box(-2.8, 19, -2.8, 5.6, 6, 5.6, s == FACE_MONSTER ? c : bone)
                                .box(-2, 21, -3.1, 1.2, 1.4, .4, dark)
                                .box(.8, 21, -3.1, 1.2, 1.4, .4, dark);
                        if (s == TIM || s == DARK_CASTER)
                            m.box(-3.6, 4, -2.7, 7.2, 7, 5.4, c)
                                    .box(-4, 24, -4, 8, 1, 8, dark)
                                    .box(-2.6, 25, -2.6, 5.2, 3, 5.2, c)
                                    .box(-1.8, 28, -1.8, 3.6, 2.5, 3.6, c)
                                    .box(-.8, 30.5, -.8, 1.6, 2, 1.6, c);
                        else if (s == UNDEAD_MINER)
                            m.box(-3.2, 23, -3.2, 6.4, 2, 6.4, 0xB09B54)
                                    .box(-1, 23, -3.8, 2, 1.5, .8, eye)
                                    .box(-.6, 11, -2.3, 1.2, 6, .5, dark);
                        else if (s == FACE_MONSTER)
                            m.box(-3, 18, -3.8, 6, 4, 2, dark)
                                    .box(-2.5, 20.5, -4, 1, 1, .5, bone)
                                    .box(1.5, 20.5, -4, 1, 1, .5, bone)
                                    .box(-1, 18, -4, 2, 1, .5, bone);
                        else if (s == SPORE_SKELETON)
                            m.box(-4, 24, -3, 8, 2, 6, 0x7484B1)
                                    .box(-2.5, 26, -2, 5, 1.5, 4, 0xABB9D0)
                                    .box(-3.3, 12, -2.3, 6.6, .8, .5, dark)
                                    .box(-3.3, 14, -2.3, 6.6, .8, .5, dark);
                        else if (s == FIRE_IMP)
                            m.tube(-2, 24, 0, -3, 28, 1, .7, .1, bone)
                                    .tube(2, 24, 0, 3, 28, 1, .7, .1, bone)
                                    .tube(0, 11, 2, 0, 6, 7, .6, .15, c);
                        else
                            m.box(-3, 13, -2.3, 6, .7, .4, dark)
                                    .box(-3, 15, -2.3, 6, .7, .4, dark)
                                    .box(-3, 17, -2.3, 6, .7, .4, dark);
                        break;
                    case HORNET:
                        m.box(-2.5, 3, -4, 5, 5, 7, c)
                                .box(-2.8, 3, -1, 5.6, 5, 1.1, dark)
                                .box(-2.5, 3, 2, 5, 4, 1, dark)
                                .box(-2, 4, -6.5, 4, 4, 3, c)
                                .box(-2.3, 5, -7, 1.5, 2, .6, eye)
                                .box(.8, 5, -7, 1.5, 2, .6, eye)
                                .tube(0, 4, 3, 0, 2, 7, 1, .1, bone)
                                .tube(-1, 8, -5, -2, 11, -6, .2, .1, dark)
                                .tube(1, 8, -5, 2, 11, -6, .2, .1, dark);
                        break;
                    case EATER:
                        m.box(-3, 3, -3, 6, 5, 7, c)
                                .box(-3.5, 4, -6, 7, 4, 4, c)
                                .box(-3, 4, -6.5, 6, 3, .7, dark)
                                .box(-2.6, 6, -6.8, 1, 1, .6, bone)
                                .box(1.6, 6, -6.8, 1, 1, .6, bone)
                                .box(-2, 7, -4, 1, 1, .5, eye)
                                .box(1, 7, -4, 1, 1, .5, eye)
                                .tube(0, 5, 3, 0, 4, 8, 2, .1, c)
                                .tube(-3, 5, 0, -7, 2, 1, .8, .1, c)
                                .tube(3, 5, 0, 7, 2, 1, .8, .1, c);
                        break;
                    case PLANT:
                        m.box(-3.4, 4, -3.4, 6.8, 4, 6.8, c)
                                .box(-3, 4, -3.7, 6, 1, .5, dark)
                                .box(-2.5, 3, -3.4, 1, 2, 1, bone)
                                .box(1.5, 3, -3.4, 1, 2, 1, bone)
                                .box(-.5, 3, -3.4, 1, 1.5, 1, bone)
                                .tube(-3, 6, 1, -6, 8, 2, .8, .1, green)
                                .tube(3, 6, 1, 6, 8, 2, .8, .1, green);
                        break;
                    case BEETLE:
                        m.box(-3, 2, -3, 6, 4, 7, c)
                                .box(-.3, 3, -2.8, .6, 3, 6.8, dark)
                                .box(-2.5, 2, -6, 5, 3, 3, dark)
                                .box(-2.2, 3, -6.3, 1, 1, .4, eye)
                                .box(1.2, 3, -6.3, 1, 1, .4, eye)
                                .tube(-1.5, 2, -6, -3, 1, -8, .5, .1, bone)
                                .tube(1.5, 2, -6, 3, 1, -8, .5, .1, bone);
                        break;
                    case CRAB:
                        m.box(-4, 2, -3, 8, 4, 6, c)
                                .box(-3, 6, -3, 1, 1.5, 1, dark)
                                .box(2, 6, -3, 1, 1.5, 1, dark)
                                .tube(-3, 3, -2, -6, 3, -5, .8, .6, c)
                                .tube(3, 3, -2, 6, 3, -5, .8, .6, c)
                                .box(-7, 2, -7, 2, 3, 3, c)
                                .box(5, 2, -7, 2, 3, 3, c);
                        break;
                    case SHELL:
                        m.box(-4, 1, -5, 8, 3, 10, dark)
                                .box(-4, 4, -3, 8, 5, 7, c)
                                .box(-3, 9, -2, 6, 2, 5, c)
                                .box(-.7, 4, -3.3, 1.4, 5, .5, dark)
                                .box(-2.5, 6, -3.3, 5, 1, .5, dark)
                                .box(-2, 2, -6, 4, 3, 2, c)
                                .box(-1.5, 4, -6.3, .8, .8, .4, eye)
                                .box(.7, 4, -6.3, .8, .8, .4, eye);
                        break;
                    case LIZARD:
                        m.box(-2.5, 2, -3, 5, 3, 9, c)
                                .box(-3, 3, -6, 6, 3, 4, c)
                                .box(-3.3, 4, -5, 1, 1, 1, eye)
                                .box(2.3, 4, -5, 1, 1, 1, eye)
                                .box(-2, 5, -1, 1, 1, 2, dark)
                                .box(1, 5, 2, 1, 1, 2, dark);
                        break;
                    case SPIDER:
                        m.box(-3.5, 3, 0, 7, 5, 7, c)
                                .box(-2.5, 3, -5, 5, 4, 5, dark)
                                .box(-2, 5, -5.3, 1, 1, .5, eye)
                                .box(1, 5, -5.3, 1, 1, .5, eye)
                                .tube(-1, 3, -4, -2, 1, -7, .6, .2, bone)
                                .tube(1, 3, -4, 2, 1, -7, .6, .2, bone);
                        break;
                    case FISH:
                        m.box(-2.5, 2, -4, 5, 4, 8, c)
                                .box(-2, 2, -6, 4, 3, 2, dark)
                                .box(-2.8, 4, -3, .5, 1, 1, eye)
                                .box(2.3, 4, -3, .5, 1, 1, eye)
                                .plate(new double[][] {{0, 3}, {0, 8}, {3, 5}}, .4, c)
                                .box(-1.5, 2, -6.2, .6, 1, .4, bone)
                                .box(.9, 2, -6.2, .6, 1, .4, bone);
                        break;
                }
                break;
            default:
                throw new IllegalArgumentException(part);
        }
        return m;
    }

    public static ExpeditionMesh material(String id) {
        return CACHE.computeIfAbsent(
                "item:" + id,
                key -> {
                    ExpeditionMesh m = new ExpeditionMesh();
                    switch (id) {
                        case "stinger":
                            m.tube(0, -6, 0, 0, 4, 0, .1, 1.4, 0xC9BC79)
                                    .box(-1.5, 3, -1.5, 3, 2, 3, 0x726742);
                            break;
                        case "jungle_vine":
                            m.tube(-3, -5, 0, 2, -1, 0, .6, .6, 0x68895A)
                                    .tube(2, -1, 0, -2, 3, 0, .6, .6, 0x68895A)
                                    .tube(-2, 3, 0, 3, 6, 0, .6, .6, 0x68895A)
                                    .box(-4, 2, -.5, 3, 1, 1, 0x99B878);
                            break;
                        case "jungle_spores":
                            m.box(-3, -3, -2, 3, 3, 3, 0xB0C779)
                                    .box(1, -1, -1, 3, 3, 3, 0xD5D58A)
                                    .box(-1, 3, -2, 2, 2, 2, 0x90B866);
                            break;
                        case "rotten_chunk":
                            m.box(-4, -3, -2, 7, 5, 4, 0x87747D)
                                    .box(-3, 2, -1.5, 4, 2, 3, 0xA09781)
                                    .box(-4, -1, -2.4, 3, 1, .5, 0x5E535E);
                            break;
                        case "vertebra":
                            m.box(-2, -4, -2, 4, 8, 4, 0xD0B7A3)
                                    .box(-4, -2, -1, 8, 2, 2, 0xD0B7A3)
                                    .box(-3, 2, -1, 6, 2, 2, 0xAE9389);
                            break;
                        case "gel":
                            m.box(-4, -3, -3, 8, 4, 6, 0x84A7BD)
                                    .box(-3, 1, -2, 6, 3, 4, 0xA7C5D0)
                                    .box(-2, 2, -2.2, 1.5, 1, .4, 0xD4E1DD);
                            break;
                        case "cochineal_husk":
                        case "cyan_husk":
                        case "lac_husk":
                            int c =
                                    id.startsWith("cyan")
                                            ? 0x65AFC2
                                            : id.startsWith("lac") ? 0xAC78BA : 0xC57889;
                            m.box(-3, -4, -1, 6, 8, 2, c)
                                    .box(-.3, -3, -1.3, .6, 6, .4, 0x514450)
                                    .box(-2, 4, -1, 4, 1, 2, c);
                            break;
                        default:
                            throw new IllegalArgumentException(id);
                    }
                    return m;
                });
    }
}
