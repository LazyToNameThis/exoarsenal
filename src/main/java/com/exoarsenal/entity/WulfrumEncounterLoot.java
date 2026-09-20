package com.exoarsenal.entity;

import java.util.Random;

public final class WulfrumEncounterLoot {
    public final int scrap, cores;
    public final boolean heart;

    private WulfrumEncounterLoot(int scrap, int cores, boolean heart) {
        this.scrap = scrap;
        this.cores = cores;
        this.heart = heart;
    }

    public static WulfrumEncounterLoot roll(Random random, boolean expert) {
        return new WulfrumEncounterLoot(4 + random.nextInt(4), 2 + random.nextInt(2), expert);
    }
}
