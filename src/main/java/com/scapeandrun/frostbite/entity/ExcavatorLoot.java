package com.scapeandrun.frostbite.entity;

import java.util.Random;

public final class ExcavatorLoot {
    public final int scrap, cores, probes;

    private ExcavatorLoot(int scrap, int cores, int probes) {
        this.scrap = scrap;
        this.cores = cores;
        this.probes = probes;
    }

    public static ExcavatorLoot roll(Random random) {
        return new ExcavatorLoot(
                10 + random.nextInt(13), 2 + random.nextInt(4), 1 + random.nextInt(2));
    }
}
