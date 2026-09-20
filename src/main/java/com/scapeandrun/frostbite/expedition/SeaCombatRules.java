package com.scapeandrun.frostbite.expedition;

public final class SeaCombatRules {
    private SeaCombatRules() {}

    public static int cnidrionInterval(float fraction) {
        return fraction < .1F ? 33 : fraction < .33F ? 50 : 100;
    }

    public static boolean cnidrionMovesWhileCasting(float fraction) {
        return fraction < .6F;
    }

    public static boolean clamAwakens(int directHits) {
        return directHits >= 5;
    }

    public static boolean clamSuspended(int cycle, boolean summons) {
        return !summons && cycle > 25 && cycle < 42;
    }

    public static boolean clamDrops(int cycle, boolean summons) {
        return !summons && cycle == 42;
    }

    public static boolean clamRecovered(int cycle) {
        return cycle >= 100;
    }
}
