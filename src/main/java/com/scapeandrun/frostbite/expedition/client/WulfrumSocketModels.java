package com.scapeandrun.frostbite.expedition.client;

public final class WulfrumSocketModels {
    public static final ExpeditionMesh BLADE = blade(), CANNON = cannon();
    private static final int DARK = 0x1B302C,
            STEEL = 0x576D5B,
            PALE = 0xB9C5AB,
            EDGE = 0x819780,
            GREEN = 0x53DDA0,
            WHITE = 0xDCFFE7;

    private static ExpeditionMesh blade() {
        ExpeditionMesh m = new ExpeditionMesh();
        m.box(-7, -2, -2, 5, 4, 4, DARK)
                .box(-6, -2.5, -2.5, 2, 5, 5, PALE)
                .box(-3, -1.5, -1.5, 5, 3, 3, STEEL);
        m.plate(new double[][] {{-2, -3}, {1, -4}, {5, -2}, {4, -1}, {1, -2}, {-1, -1}}, 1, PALE);
        m.plate(new double[][] {{-2, 3}, {1, 4}, {5, 2}, {4, 1}, {1, 2}, {-1, 1}}, 1, PALE);
        m.plate(
                new double[][] {
                    {1, -1.6}, {8, -2.3}, {19, -.8}, {27, 1.1}, {17, 2.4}, {5, 2}, {1, .8}
                },
                .38,
                GREEN);
        m.plate(new double[][] {{2, -.25}, {8, -.8}, {24, 1}, {10, .9}, {3, .6}}, .42, WHITE);
        m.plate(new double[][] {{1, 1}, {6, 1.8}, {17, 2.2}, {10, 3.1}, {4, 2.8}}, .48, STEEL);
        m.box(-4, -.6, -2.2, 5, 1.2, .6, GREEN).box(-4, -.6, 1.6, 5, 1.2, .6, GREEN);
        m.box(-5.8, -1.8, -2.65, .7, .7, .2, WHITE).box(-5.8, 1.1, -2.65, .7, .7, .2, EDGE);
        m.box(-3.2, -1.4, -1.65, .55, .5, .25, DARK).box(-1.8, -1.4, -1.65, .55, .5, .25, DARK);
        m.box(-5, -3, -.7, 1, 1.2, 1.4, EDGE).box(-5, 1.8, -.7, 1, 1.2, 1.4, EDGE);
        return m;
    }

    private static ExpeditionMesh cannon() {
        ExpeditionMesh m = new ExpeditionMesh();
        m.box(-9, -3, -3, 10, 6, 6, DARK).box(-8, -2.5, -3.3, 7, 5, 6.6, STEEL);
        m.box(-7, 2.5, -2.8, 6, 1, 5.6, PALE).box(-7, -3.5, -2.8, 6, 1, 5.6, PALE);
        m.box(-10, -1.8, -1.8, 2, 3.6, 3.6, EDGE).box(-10.2, -1, -1, 1, 2, 2, GREEN);
        m.box(-1, -2.8, -2.8, 2, 5.6, 5.6, PALE).box(1, -1.8, -1.8, 12, 3.6, 3.6, DARK);
        m.box(3, -.5, -2.05, 9, 1, .4, GREEN).box(3, -.5, 1.65, 9, 1, .4, GREEN);
        m.box(3, 1.8, -2, 1, 1, 4, EDGE)
                .box(6, 1.8, -2, 1, 1, 4, EDGE)
                .box(9, 1.8, -2, 1, 1, 4, EDGE);
        m.box(3, -2.8, -2, 1, 1, 4, STEEL)
                .box(6, -2.8, -2, 1, 1, 4, STEEL)
                .box(9, -2.8, -2, 1, 1, 4, STEEL);
        m.box(12, -2.5, -2.5, 2, 5, 5, PALE)
                .box(14, -1.8, -1.8, .5, 3.6, 3.6, DARK)
                .box(14.5, -1.2, -1.2, .15, 2.4, 2.4, GREEN)
                .box(14.7, -.45, -.45, .1, .9, .9, WHITE);
        m.box(-6, -1.5, -3.65, 3, 3, .4, PALE).box(-5, -.7, -3.9, 1.4, 1.4, .3, GREEN);
        m.box(-7.5, -2, -3.55, .65, .65, .3, PALE).box(-7.5, 1.35, -3.55, .65, .65, .3, PALE);
        m.box(-2.2, -2, -3.55, .65, .65, .3, EDGE).box(-2.2, 1.35, -3.55, .65, .65, .3, EDGE);
        m.box(4, -1.2, -1.95, .5, .4, .35, EDGE)
                .box(7, -1.2, -1.95, .5, .4, .35, EDGE)
                .box(10, -1.2, -1.95, .5, .4, .35, EDGE);
        m.plate(new double[][] {{-6, 3}, {-4, 6}, {0, 7}, {1, 5}, {-2, 3}}, .6, STEEL);
        m.plate(new double[][] {{-6, -3}, {-4, -6}, {0, -7}, {1, -5}, {-2, -3}}, .6, STEEL);
        return m;
    }

    private WulfrumSocketModels() {}
}
