package com.exoarsenal.client;

public final class RobotColors {
    private RobotColors() {}

    public static int color(String bone, boolean warning) {
        String n = bone.toLowerCase(java.util.Locale.ROOT);
        if (n.contains("glow") || n.endsWith("ice") || n.contains("coil"))
            return warning ? 0xFFD28B : 0x9CEBFF;
        if (n.contains("jaw") || n.contains("finger") || n.contains("blade")) return 0xE1EEFF;
        if (n.contains("knee")
                || n.contains("elbow")
                || n.contains("piston")
                || n.contains("joint")) return 0xB6A7A0;
        if (n.contains("foot") || n.contains("shin") || n.contains("back")) return 0x909DB8;
        if (n.contains("shield") || n.contains("shoulder") || n.contains("carapace"))
            return 0xC7D9EF;
        return 0xB4C7E2;
    }
}
