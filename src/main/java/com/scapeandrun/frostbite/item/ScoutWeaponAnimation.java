package com.scapeandrun.frostbite.item;

public final class ScoutWeaponAnimation {
    private ScoutWeaponAnimation() {}

    public static boolean charging(String action) {
        return "crush_charge".equals(action) || "slam_charge".equals(action);
    }

    public static int duration(String action) {
        switch (action) {
            case "snap":
                return 7;
            case "crush":
                return 9;
            case "strike":
                return 10;
            case "slam":
                return 11;
            case "lock":
                return 10;
            case "rail_fire":
                return 13;
            case "aurora_cast":
                return 17;
            case "aurora_detonate":
                return 10;
            default:
                return 0;
        }
    }

    public static boolean playing(String action, long age, boolean heldCharge) {
        if (age < 0) return false;
        return charging(action) ? heldCharge : age < duration(action);
    }
}
