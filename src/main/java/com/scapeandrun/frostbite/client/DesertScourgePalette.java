package com.scapeandrun.frostbite.client;

public final class DesertScourgePalette {
    private DesertScourgePalette() {}

    public static int color(String n) {
        return n.contains("Teeth")
                ? 0xF2E0B7
                : n.contains("Horns") || n.contains("Spines")
                        ? 0xCFB37D
                        : n.contains("Mouth")
                                ? 0x342335
                                : n.contains("Flesh")
                                        ? 0x81504C
                                        : n.contains("Eyes")
                                                ? 0xFFD16C
                                                : n.contains("Ridges")
                                                        ? 0xB28A58
                                                        : n.contains("Jaw") ? 0xAA7650 : 0x806344;
    }
}
