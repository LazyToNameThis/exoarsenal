package com.scapeandrun.frostbite.client;

import com.scapeandrun.frostbite.expedition.client.ExpeditionMesh;

public final class BrawlerJetpack {
    public static final ExpeditionMesh ENGINE = new ExpeditionMesh(),
            PETAL = new ExpeditionMesh(),
            SOCKET = new ExpeditionMesh();

    static {
        int dark = 0x22342A, metal = 0x889780, edge = 0xBAC5A4, light = 0x70F2A0;

        ENGINE.armor(-.96, -2.65, -1.08, 1.92, 5.3, 2.16, .42, 0x3E5040);
        ENGINE.armor(-.68, -2.25, -1.3, 1.36, 4.5, 2.6, .3, 0x59684E);
        ENGINE.armor(-1.18, .65, -1.24, 2.36, 1.25, 2.48, .34, metal);
        ENGINE.armor(-1.24, -1.85, -1.3, 2.48, 1.15, 2.6, .34, metal);
        ENGINE.armor(-1.02, 2, -1.1, 2.04, .8, 2.2, .3, 0xA6B18C);
        ENGINE.armor(-1.3, -2.8, -1.4, 2.6, .72, 2.8, .3, 0x71816B);
        for (int s : new int[] {-1, 1}) {
            ENGINE.tube(s * 1.28, -2.45, -.8, s * 1.28, 2.1, -.8, .16, .16, dark);
            ENGINE.tube(s * 1.28, -2.15, -.8, s * 1.28, 1.8, -.8, .07, .07, edge);
            ENGINE.tube(s * .8, -1.95, -1.44, s * .8, 1.85, -1.44, .1, .1, dark);
            ENGINE.tube(s * .8, -1.7, -1.56, s * .8, 1.6, -1.56, .04, .04, light);
            ENGINE.armor(s < 0 ? -1.52 : 1.12, -.15, -.8, .4, 1.4, 1.6, .12, 0xB6BA8A);
        }
        for (int row = 0; row < 8; row++) {
            double y = -1.9 + row * .49;
            ENGINE.armor(-.76, y, 1.15, 1.52, .25, .4, .08, 0x43523F);
            ENGINE.box(-.57, y + .05, 1.55, 1.14, .08, .06, edge);
            ENGINE.box(-.38, y + .15, 1.55, .76, .045, .05, light);
        }
        ENGINE.armor(-1.1, -3.45, -1.25, 2.2, .65, 2.5, .18, edge);
        ENGINE.armor(-.85, -3.54, -.95, 1.7, .24, 1.9, .15, dark);
        ENGINE.box(-.65, -3.59, -.72, 1.3, .06, 1.44, light);
        ENGINE.armor(-.78, 2.65, -.9, 1.56, .65, 1.8, .24, dark);
        ENGINE.box(-.55, 3.25, -.65, 1.1, .08, 1.3, edge);
        ENGINE.armor(-.55, -2.25, -1.55, 1.1, 4.5, .32, .18, metal);
        ENGINE.armor(-.34, -1.9, -1.8, .68, 3.8, .28, .12, dark);
        for (int row = 0; row < 7; row++) {
            double y = -1.65 + row * .53;
            ENGINE.box(-.25, y, -1.96, .5, .13, .16, edge);
            ENGINE.box(-.18, y + .15, -1.97, .36, .06, .06, light);
            for (int side : new int[] {-1, 1}) {
                ENGINE.armor(side < 0 ? -1.12 : .8, y, -1.36, .32, .3, .25, .07, metal);
                ENGINE.box(side < 0 ? -1.02 : .9, y + .1, -1.48, .1, .1, .12, edge);
            }
        }
        ENGINE.armor(-.86, 1.95, -1.55, 1.72, .3, .33, .1, edge);
        ENGINE.armor(-.86, -2.5, -1.55, 1.72, .3, .33, .1, edge);
        for (int n = 0; n < 12; n++) {
            double a = n * Math.PI / 6, c = Math.cos(a), s = Math.sin(a);
            ENGINE.tube(
                    c * .85,
                    -2.85,
                    s * .85,
                    c * 1.05,
                    -3.5,
                    s * 1.05,
                    .12,
                    .18,
                    n % 2 == 0 ? metal : edge);
            ENGINE.box(c * .72 - .07, -3.57, s * .72 - .07, .14, .09, .14, light);
        }
        PETAL.armor(-.65, -1.1, -.14, 1.3, 2.2, .28, .2, metal)
                .box(-.52, -.9, .15, 1.04, .06, .04, edge);
        SOCKET.armor(-.48, -.48, -.65, .96, .96, 1.3, .16, dark)
                .armor(-.6, -.6, .3, 1.2, 1.2, .32, .18, metal)
                .armor(-.4, -.4, .61, .8, .8, .12, .12, light);
    }

    private BrawlerJetpack() {}

    public static double[] uv(ExpeditionMesh.Face f, int vertex) {
        double[] p = f.p[vertex];
        int region;
        double u, v;
        if (Math.abs(f.ny) > .7) {
            region = 3;
            u = (p[0] + 1.6) / 3.2;
            v = (p[2] + 2) / 4;
        } else if (Math.abs(f.nx) > .7) {
            region = 2;
            u = (p[2] + 2) / 4;
            v = (3.4 - p[1]) / 7;
        } else {
            region = f.nz > 0 ? 0 : 1;
            u = (p[0] + 1.6) / 3.2;
            v = (3.4 - p[1]) / 7;
        }
        return new double[] {
            (region * 16 + .5 + Math.max(0, Math.min(1, u)) * 15) / 64,
            (.5 + Math.max(0, Math.min(1, v)) * 31) / 32
        };
    }
}
