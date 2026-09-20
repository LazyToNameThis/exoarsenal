package com.exoarsenal.config;

import net.minecraftforge.common.config.Configuration;
import java.io.File;

public final class ModConfig {
    public static boolean replaceTerrain = true;
    public static boolean removeAllOreGeneration = true;
    public static boolean removeAllTreeGeneration = true;
    public static int stumpChancePerChunk = 3;
    public static int soilTransitionRoll = 128;
    public static boolean suppressHbmWorldGeneration = true;

    private ModConfig() {}

    public static void load(File file) {
        Configuration c = new Configuration(file);
        c.load();
        c.removeCategory(c.getCategory("oxygen"));
        replaceTerrain =
                c.getBoolean(
                        "replaceTerrain",
                        "world",
                        true,
                        "Replace vanilla surface and underground terrain in newly populated chunks.");
        removeAllOreGeneration =
                c.getBoolean(
                        "removeAllOreGeneration",
                        "world",
                        true,
                        "Remove ore-dictionary ores from newly populated Overworld chunks.");
        removeAllTreeGeneration =
                c.getBoolean(
                        "removeAllTreeGeneration",
                        "world",
                        true,
                        "Cancel trees and scrub non-frozen logs/leaves from new chunks.");
        stumpChancePerChunk =
                c.getInt(
                        "stumpAttemptsPerChunk",
                        "world",
                        3,
                        0,
                        20,
                        "Attempts to place 3-5 block frozen stumps per chunk.");
        soilTransitionRoll =
                c.getInt(
                        "soilTransitionRoll",
                        "progression",
                        128,
                        64,
                        4096,
                        "One successful qualifying soil transition per this many random ticks. 128 plus crop growth penalties targets several hours to the first real Dirt across a 16-plot recovery farm.");
        suppressHbmWorldGeneration =
                c.getBoolean(
                        "suppressHbmWorldGeneration",
                        "compatibility",
                        true,
                        "Unregister HBM Well Forged's combined ore/structure generator. Required to prevent cascading chunk generation and enforce the no-ore world rule.");
        if (c.hasChanged()) c.save();
    }
}
