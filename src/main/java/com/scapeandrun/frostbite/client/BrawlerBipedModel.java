package com.scapeandrun.frostbite.client;

import com.scapeandrun.frostbite.expedition.client.ExpeditionMesh;

public final class BrawlerBipedModel {
    public static final ExpeditionMesh THIGH = new ExpeditionMesh(),
            SHIN = new ExpeditionMesh(),
            FOOT = new ExpeditionMesh();

    static {
        int dark = 0x22342A, metal = 0x889780, edge = 0xBAC5A4, green = 0x70F2A0;
        THIGH.armor(-.58, -1.45, -.54, 1.16, 1.25, 1.08, .18, metal);
        THIGH.armor(-.39, -1.23, .48, .78, .92, .22, .12, 0x59684E);
        THIGH.armor(-.3, -1.15, .68, .6, .71, .12, .07, dark);
        THIGH.box(-.19, -1.02, .805, .38, .07, .025, edge);
        THIGH.box(-.19, -.84, .805, .38, .07, .025, 0xB1AB78);
        THIGH.box(-.19, -.66, .805, .38, .07, .025, green);
        THIGH.armor(-.7, -.3, -.64, 1.4, .5, 1.28, .15, dark);
        THIGH.tube(-.68, -.05, 0, .68, -.05, 0, .25, .25, edge);
        THIGH.armor(-.58, -1.82, -.52, 1.16, .45, 1.04, .15, dark);
        THIGH.tube(-.65, -1.65, 0, .65, -1.65, 0, .24, .24, edge);
        for (int s : new int[] {-1, 1}) {
            THIGH.tube(s * .43, -.3, -.62, s * .43, -1.4, -.62, .13, .13, dark);
            THIGH.tube(s * .43, -.7, -.63, s * .43, -1.58, -.63, .065, .065, edge);
            SHIN.tube(s * .46, -.14, -.46, s * .46, -1.48, -.46, .13, .09, dark);
            SHIN.tube(s * .46, -.55, -.5, s * .46, -1.6, -.5, .055, .055, edge);
            SHIN.armor(s < 0 ? -.91 : .51, -1.3, -.68, .4, 1.12, .74, .12, metal);
            SHIN.armor(s < 0 ? -.84 : .58, -1.46, -.61, .26, .2, .58, .06, dark);
            SHIN.box(s < 0 ? -.8 : .62, -1.48, -.54, .18, .05, .43, green);
        }
        SHIN.armor(-.48, -1.52, -.38, .96, 1.28, .86, .16, metal);
        SHIN.armor(-.59, -.43, .39, 1.18, .66, .35, .12, edge);
        SHIN.armor(-.35, -1.34, .44, .7, .84, .24, .08, 0x43523F);
        for (int i = 0; i < 4; i++)
            SHIN.box(-.24, -1.2 + i * .18, .68, .48, .065, .04, i == 2 ? green : edge);
        for (int s : new int[] {-1, 1})
            for (int bolt = 0; bolt < 3; bolt++) {
                THIGH.box(s * .45 - .055, -1.18 + bolt * .32, .57, .11, .11, .08, 0xB1AB78);
                SHIN.box(s * .45 - .055, -1.26 + bolt * .38, .5, .11, .11, .08, 0xB1AB78);
                FOOT.box(s * .46 - .045, .48, -.32 + bolt * .28, .09, .055, .09, 0xB1AB78);
            }
        FOOT.armor(-.64, -.18, -.65, 1.28, .43, 1.95, .16, dark);
        FOOT.armor(-.55, .07, -.49, 1.1, .4, 1.1, .14, metal);
        FOOT.armor(-.52, -.12, -.95, 1.04, .5, .46, .12, edge);
        FOOT.box(-.36, -.16, -.99, .72, .12, .06, green);
        for (int toe = 0; toe < 3; toe++) {
            double x = -.58 + toe * .4;
            FOOT.armor(x, -.15, .48, .36, .36, .97, .075, metal);
            FOOT.box(x + .045, .215, .88, .27, .04, .12, edge);
            FOOT.armor(x + .015, -.18, 1.3, .33, .28, .27, .045, edge);
        }
    }

    private BrawlerBipedModel() {}
}
