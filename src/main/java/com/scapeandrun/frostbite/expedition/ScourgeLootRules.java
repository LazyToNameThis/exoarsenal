package com.scapeandrun.frostbite.expedition;

import java.util.Random;

public final class ScourgeLootRules {
    private ScourgeLootRules() {}

    public static int materialCount(Random r, boolean expert) {
        return (expert ? 30 : 25) + r.nextInt(expert ? 11 : 6);
    }

    public static int weaponMask(Random r, boolean expert) {
        int mask = 0;
        for (int i = 0; i < 5; i++) if (r.nextInt(expert ? 3 : 4) == 0) mask |= 1 << i;
        return mask == 0 ? 1 << r.nextInt(5) : mask;
    }
}
