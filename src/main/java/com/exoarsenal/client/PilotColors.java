package com.exoarsenal.client;

public final class PilotColors {
    private PilotColors() {}

    public static int color(String name) {
        String n = name.toLowerCase(java.util.Locale.ROOT);
        if (n.contains("wulfrumlight")) return 0x76FFC0;
        if (n.contains("wulfrumtrim")) return 0xA8B59A;
        if (n.contains("wulfrum")) return 0x566A52;
        if (n.equals("pilothead") || n.equals("operatornose")) return 0xB98568;
        if (n.equals("operatorhair")) return 0x29252B;
        if (n.equals("operatorbrows")) return 0x453238;
        if (n.equals("operatoreyes")) return 0xD5E2D9;
        if (n.equals("operatorpupils")) return 0x283B43;
        if (n.equals("operatorscar")) return 0xD0A090;
        if (n.equals("operatorharness")) return 0x344436;
        if (n.equals("operatorbuckles")) return 0xB5C1C5;
        if (n.equals("operatorinsignia")) return 0x7AE8A2;
        if (n.contains("forearm") || n.contains("shin") || n.equals("operatorboots"))
            return 0x222B35;
        return 0x29382F;
    }

    public static boolean applies(String name) {
        return name.startsWith("pilot")
                || name.startsWith("operator") && !name.equals("operatorheadset");
    }
}
