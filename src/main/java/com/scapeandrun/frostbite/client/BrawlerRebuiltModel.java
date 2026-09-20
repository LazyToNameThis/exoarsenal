package com.scapeandrun.frostbite.client;

import com.scapeandrun.frostbite.expedition.client.ExpeditionMesh;

final class BrawlerRebuiltModel {
    static final int BLACK = 0x20282B,
            STEEL = 0x505B60,
            PLATE = 0x778185,
            RIM = 0xABB3AE,
            BRASS = 0xA58B5D,
            LIGHT = 0x70F2A0,
            WHITE = 0xDAFFD6;

    static void rebuild(
            ExpeditionMesh head,
            ExpeditionMesh fist,
            ExpeditionMesh cleaver,
            ExpeditionMesh harpoon,
            ExpeditionMesh projector,
            ExpeditionMesh flail) {
        head.faces.clear();
        fist.faces.clear();
        cleaver.faces.clear();
        harpoon.faces.clear();
        projector.faces.clear();
        flail.faces.clear();
        reactor(head);
        driver(fist);
        cleaver(cleaver);
        harpoons(harpoon);
        projector(projector);
        flail(flail);
        silhouette(head, fist, cleaver, harpoon, projector, flail);
        fist.orient(-.20, 1.18);
        cleaver.orient(-.16, 1);
        harpoon.orient(.10, 1);
        projector.orient(.18, 1);
    }

    private static void silhouette(
            ExpeditionMesh head,
            ExpeditionMesh fist,
            ExpeditionMesh blade,
            ExpeditionMesh rack,
            ExpeditionMesh gun,
            ExpeditionMesh weight) {

        head.plate(
                new double[][] {
                    {-1.39, .93}, {-.89, 1.47}, {-.29, 1.55}, {-.36, 1.19}, {-1.02, .83}
                },
                .48,
                PLATE);
        head.plate(
                new double[][] {{.29, 1.55}, {.89, 1.47}, {1.39, .93}, {1.02, .83}, {.36, 1.19}},
                .48,
                PLATE);
        head.tube(-1.30, .95, .57, -.87, 1.39, .57, .035, .035, RIM);
        head.tube(.87, 1.39, .57, 1.30, .95, .57, .035, .035, RIM);
        head.armor(-.24, 1.30, -.88, .48, .43, .68, .11, BLACK);
        head.box(-.14, 1.53, -.17, .28, .08, .035, BRASS);
        head.tube(.96, 1.11, -.69, .96, 2.28, -.69, .055, .055, BLACK);
        head.box(.90, 2.18, -.75, .12, .16, .12, LIGHT);
        for (int side : new int[] {-1, 1}) {
            head.armor(side < 0 ? -1.63 : 1.26, -.87, -.86, .37, 1.60, .81, .12, BLACK);
            for (int vent = 0; vent < 7; vent++)
                head.box(side < 0 ? -1.60 : 1.29, -.71 + vent * .18, -.015, .28, .065, .10, PLATE);
            head.tube(side * .82, -.83, -.90, side * .52, -1.53, -.84, .12, .12, BLACK);
            head.tube(side * .79, -.86, -.73, side * .52, -1.50, -.67, .055, .055, BRASS);

            fist.armor(side < 0 ? -1.19 : .98, -.53, -1.61, .21, 1.08, .91, .075, PLATE);
            fist.box(side < 0 ? -1.21 : 1.18, -.16, -1.40, .035, .36, .43, BLACK);
            fist.box(side < 0 ? -1.25 : 1.215, -.11, -1.35, .035, .26, .08, LIGHT);
            gun.plate(
                    new double[][] {
                        {side * .46, .73},
                        {side * .88, .83},
                        {side * 1.17, .49},
                        {side * 1.08, .25},
                        {side * .78, .54}
                    },
                    .62,
                    PLATE);
            gun.tube(side * .75, -.46, -1.06, side * .75, -.46, .39, .10, .10, BLACK);
            gun.tube(side * .75, -.46, -.58, side * .75, -.46, .44, .045, .045, BRASS);
        }

        blade.armor(-.97, -.32, -1.03, .76, .64, .24, .13, BLACK);
        blade.box(-.82, -.19, -1.08, .43, .10, .04, BRASS);
        blade.tube(-.70, .42, -.70, -.18, 1.05, -.62, .075, .075, BLACK);
        blade.tube(-.18, 1.05, -.62, .52, 1.31, -.42, .075, .075, BLACK);
        blade.tube(-.64, -.49, -.67, -.16, -1.07, -.56, .075, .075, BLACK);
        blade.tube(-.16, -1.07, -.56, .42, -1.44, -.35, .075, .075, BLACK);

        for (int rail = 0; rail < 3; rail++) {
            double y = -.89 + rail * .89;
            rack.armor(-1.05, y - .24, -.59, .30, .48, .69, .08, PLATE);
            rack.box(-1.08, y - .10, -.10, .045, .20, .12, BRASS);
            rack.armor(.48, y + .27, .10, .39, .19, .30, .05, BLACK);
            rack.box(.56, y + .31, .42, .19, .09, .04, LIGHT);
        }
        gun.armor(-.45, -.45, -1.41, .90, .90, .29, .20, BLACK);
        gun.box(-.28, -.15, -1.45, .56, .30, .04, 0x288D51);
        for (int v = 0; v < 3; v++) gun.box(-.25, -.12 + v * .09, -1.49, .50, .035, .04, LIGHT);
        weight.tube(0, -1.65, 0, 0, -1.22, 0, .18, .18, BRASS);
        weight.armor(-.28, 1.22, -.28, .56, .24, .56, .08, PLATE);
    }

    private static void panel(
            ExpeditionMesh m, double x, double y, double z, double w, double h, double d) {
        m.armor(x, y, z, w, h, d, .13, BLACK)
                .armor(x + .04, y + .04, z + d - .04, w - .08, h - .08, .15, .11, PLATE);
        m.box(x + .10, y + h - .12, z + d + .075, w - .20, .035, .025, RIM);
        for (double bx : new double[] {x + .10, x + w - .16})
            for (double by : new double[] {y + .10, y + h - .16})
                m.box(bx, by, z + d + .08, .055, .055, .035, BRASS);
    }

    private static void ring(
            ExpeditionMesh m,
            double x,
            double y,
            double z,
            double radius,
            double width,
            double depth,
            int color) {
        for (int j = 0; j < 12; j++) {
            double a = j * Math.PI / 6, b = (j + 1) * Math.PI / 6;
            m.tube(
                    x + Math.cos(a) * radius,
                    y + Math.sin(a) * radius,
                    z,
                    x + Math.cos(b) * radius,
                    y + Math.sin(b) * radius,
                    z,
                    width,
                    width,
                    color);
        }
    }

    private static void reactor(ExpeditionMesh m) {

        m.armor(-1.03, -1.12, -.95, 2.06, 2.24, 1.70, .36, STEEL);
        ring(m, 0, 0, .28, 1.23, .24, .4, STEEL);
        ring(m, 0, 0, .55, 1.03, .16, .3, PLATE);
        ring(m, 0, 0, .78, .78, .13, .2, BLACK);

        for (int s : new int[] {-1, 1}) {
            m.tube(s * .72, .93, .74, s * 1.02, .52, .78, .20, .20, PLATE);
            m.tube(s * 1.02, -.48, .78, s * .71, -.94, .74, .20, .20, PLATE);
            m.box(s * .86 - .055, .63, 1.0, .11, .10, .06, BRASS);
            m.box(s * .86 - .055, -.72, 1.0, .11, .10, .06, BRASS);
            m.tube(s * .48, 1.26, -.48, s * .99, .76, -.66, .09, .09, RIM);
        }
        m.armor(-.59, -.70, .69, 1.18, 1.40, .19, .18, BLACK);
        m.armor(-.40, -.52, .89, .80, 1.04, .10, .12, 0x288D51);
        m.armor(-.28, -.40, .99, .56, .80, .07, .09, LIGHT);
        m.box(-.11, -.29, 1.065, .22, .58, .025, WHITE);
        panel(m, -.66, .94, -.35, 1.32, .40, 1.0);
        panel(m, -.55, -1.4, -.32, 1.1, .43, .9);
        for (int s : new int[] {-1, 1}) {
            panel(m, s < 0 ? -1.54 : 1.12, -.50, -.36, .42, 1.0, .84);
            m.box(s * 1.29 - .055, -.24, .59, .11, .48, .04, LIGHT);
            m.tube(s * .72, 1.12, -.27, s * .72, 2.08, -.27, .075, .075, STEEL);
            m.box(s * .72 - .10, 1.49, -.37, .20, .08, .20, BRASS);
            m.tube(s * .44, -1.30, -.15, s * .44, -1.91, -.15, .10, .10, STEEL);
            m.box(s * .44 - .06, -1.84, -.02, .12, .23, .055, LIGHT);
            for (int j = 0; j < 3; j++)
                panel(m, s < 0 ? -1.15 : .72, -.62 + j * .43, -1.03, .43, .32, .22);
        }
        ring(m, 0, 0, -1.12, .72, .15, .2, STEEL);

        for (int s : new int[] {-1, 1}) {
            m.armor(s < 0 ? -1.02 : .36, .92, -.76, .66, .48, 1.28, .16, PLATE);
            m.armor(s < 0 ? -1.30 : .93, .19, -.65, .37, .54, 1.22, .12, PLATE);
            m.armor(s < 0 ? -1.23 : .84, -.86, -.65, .39, .55, 1.11, .12, PLATE);
            m.tube(s * .83, .88, .91, s * 1.07, .43, .91, .055, .055, RIM);
            m.tube(s * .95, -.38, .91, s * .73, -.83, .91, .055, .055, RIM);
            for (int vent = 0; vent < 4; vent++)
                m.box(s < 0 ? -.95 : .62, -.48 + vent * .15, .87, .30, .055, .045, BLACK);
            m.tube(s * .37, 1.25, -.75, s * .37, 1.76, -.75, .10, .10, BLACK);
            m.box(s * .37 - .08, 1.62, -.84, .16, .09, .18, BRASS);
        }
        m.box(-.25, -.25, -1.35, .5, .5, .2, LIGHT);
        for (int j = 0; j < 6; j++) m.box(-.51, -.68 + j * .24, -1.42, 1.02, .08, .12, BLACK);

        for (int s : new int[] {-1, 1})
            for (int v : new int[] {-1, 1}) {
                panel(m, s < 0 ? -1.55 : 1.04, v * .70 - .24, -.69, .51, .48, .38);
                m.tube(s * 1.25, v * .70, -.75, s * 1.25, v * .70, -.19, .15, .15, BRASS);
                for (int collar = 0; collar < 3; collar++)
                    m.box(s * 1.25 - .19, v * .70 - .19, -.69 + collar * .14, .38, .38, .06, BLACK);
            }
    }

    private static final ExpeditionMesh[] DRIVER_POSES = new ExpeditionMesh[33];

    static ExpeditionMesh driverPose(float grip) {
        int pose = Math.max(0, Math.min(32, Math.round(grip * 32)));
        if (DRIVER_POSES[pose] == null) {
            ExpeditionMesh m = new ExpeditionMesh();
            driver(m, 1 - pose / 32D);
            m.orient(-.20, 1.18);
            DRIVER_POSES[pose] = m;
        }
        return DRIVER_POSES[pose];
    }

    static boolean driverMesh(ExpeditionMesh m) {
        for (ExpeditionMesh pose : DRIVER_POSES) if (pose == m) return true;
        return false;
    }

    private static void driver(ExpeditionMesh m) {
        driver(m, 0);
    }

    private static void driver(ExpeditionMesh m, double open) {

        m.armor(-.91, -.62, -.74, 1.82, 1.36, 1.12, .24, STEEL);
        panel(m, -.95, .52, -.78, 1.9, .40, 1.28);
        for (int f = 0; f < 4; f++) {
            ExpeditionMesh palm = m;
            m = new ExpeditionMesh();
            double x = -.88 + f * .45;
            panel(m, x, -.22, .32, .39, .86, .47);
            panel(m, x, -.66, .59, .39, .43, .47);
            m.box(x + .07, -.61, 1.08, .25, .28, .13, STEEL);
            m.box(x + .065, .36, .88, .26, .13, .07, RIM);
            m.armor(x + .025, .53, .24, .34, .30, .72, .065, RIM);
            m.box(x + .12, .57, .99, .11, .12, .07, BRASS);
            m.tube(x + .19, -.21, .18, x + .19, -.60, .42, .08, .08, BRASS);
            m.hingeX(.55, .25, open * (1.18 + f * .045));
            palm.faces.addAll(m.faces);
            m = palm;
        }
        ExpeditionMesh thumb = new ExpeditionMesh();
        panel(thumb, -1.31, -.57, -.03, .40, .74, .75);
        thumb.tube(-1.13, -.42, .70, -.77, -.66, .88, .17, .17, STEEL);
        thumb.hingeX(-.15, -.03, -open * .85);
        m.faces.addAll(thumb.faces);
        for (int s : new int[] {-1, 1}) {
            m.tube(s * .65, 0, -1.70, s * .65, 0, -.50, .13, .13, RIM);
            m.tube(s * .65, 0, -1.71, s * .65, 0, -1.2, .23, .23, BLACK);
            panel(m, s < 0 ? -1.06 : .80, -.42, -1.02, .26, .8, .58);
            m.box(s * .91 - .06, -.15, -.34, .12, .36, .06, LIGHT);
        }
        panel(m, -.83, -.48, -1.82, 1.66, .96, .23);
        for (int s : new int[] {-1, 1})
            for (int coil = 0; coil < 5; coil++)
                m.box(s * .65 - .18, -.20, -1.56 + coil * .14, .36, .40, .045, STEEL);
        m.armor(-.62, -.39, -2.04, 1.24, .78, .22, .18, STEEL);
        m.box(-.33, -.11, -2.08, .66, .22, .045, 0x288D51);

        for (int s : new int[] {-1, 1}) {
            m.armor(s < 0 ? -.96 : .52, .63, -1.48, .44, .31, .86, .10, PLATE);
            m.armor(s < 0 ? -1.10 : .89, -.43, -.65, .21, .67, .66, .07, PLATE);
            m.tube(s * .39, .75, -1.62, s * .39, .75, -.58, .065, .065, BRASS);
            for (int n = 0; n < 3; n++)
                m.box(s < 0 ? -.87 : .58, .955, -1.34 + n * .22, .27, .03, .07, BLACK);
        }
    }

    private static void cleaver(ExpeditionMesh m) {
        m.armor(-.64, -.68, -.62, 1.28, 1.36, 1.18, .25, STEEL);
        ring(m, 0, 0, .56, .47, .13, .2, STEEL);
        m.box(-.17, -.17, .69, .34, .34, .07, LIGHT);
        m.plate(
                new double[][] {
                    {-.40, 1.22},
                    {.12, 1.65},
                    {.91, 1.46},
                    {1.57, .89},
                    {1.87, .10},
                    {1.71, -.79},
                    {1.11, -1.55},
                    {.38, -1.83},
                    {-.11, -1.40},
                    {.68, -.91},
                    {1.00, -.19},
                    {.90, .53},
                    {.35, .96}
                },
                .34,
                STEEL);
        m.plate(
                new double[][] {
                    {.91, 1.46},
                    {1.70, .90},
                    {2.06, .10},
                    {1.90, -.87},
                    {1.13, -1.75},
                    {.38, -1.83},
                    {1.11, -1.55},
                    {1.71, -.79},
                    {1.87, .10},
                    {1.57, .89}
                },
                .27,
                LIGHT);
        m.plate(
                new double[][] {
                    {.28, 1.20},
                    {.85, 1.23},
                    {1.34, .72},
                    {1.51, .13},
                    {1.34, -.57},
                    {.84, -1.20},
                    {.33, -1.44},
                    {.96, -.48},
                    {1.16, .07},
                    {.72, .82}
                },
                .39,
                PLATE);
        for (double[] p :
                new double[][] {
                    {.66, 1.12}, {1.22, .68}, {1.37, .05}, {1.18, -.60}, {.72, -1.19}
                }) {
            m.box(p[0] - .09, p[1] - .09, .40, .18, .18, .06, BLACK)
                    .box(p[0] - .03, p[1] - .03, .47, .06, .06, .03, BRASS);
        }
        m.tube(1.58, .84, -.38, 1.70, .85, .38, .065, .065, BLACK);
        m.tube(1.77, -.33, -.38, 1.93, -.36, .38, .065, .065, BLACK);
        m.tube(1.22, -1.38, -.38, 1.33, -1.49, .38, .065, .065, BLACK);
        panel(m, -.83, -.54, -.86, .41, 1.08, .65);
        m.tube(-.66, .57, -.32, -.47, 1.15, -.12, .13, .13, RIM);

        m.armor(-1.04, -.54, -.50, .46, 1.03, .92, .12, PLATE);
        m.tube(-.42, .58, -.48, .39, 1.18, -.38, .075, .075, BRASS);
        m.tube(-.45, -.53, -.48, .31, -1.25, -.38, .075, .075, BRASS);

        for (double[] p :
                new double[][] {
                    {.56, 1.08, .32, .34},
                    {1.03, .74, .36, .37},
                    {1.30, .20, .32, .42},
                    {1.21, -.40, .33, .39},
                    {.84, -.98, .34, .36}
                }) {
            m.armor(p[0] - .12, p[1] - .16, -.27, p[2], p[3], .78, .07, PLATE);
            m.box(p[0] - .06, p[1] - .10, .53, .10, .20, .035, BLACK);
            m.box(p[0] - .03, p[1] - .07, .57, .045, .13, .025, BRASS);
        }
        m.armor(-.45, -.34, .62, .70, .68, .21, .16, PLATE);
        ring(m, -.10, 0, .86, .20, .065, .10, BLACK);
        m.box(-.17, -.08, .92, .14, .16, .035, LIGHT);
    }

    private static void harpoons(ExpeditionMesh m) {

        m.armor(-.57, -1.27, -.65, .48, 2.54, .54, .12, BLACK);
        m.tube(-.46, -1.39, -.46, -.46, 1.40, -.46, .12, .12, BRASS);
        for (int j = 0; j < 3; j++) {
            double y = -.89 + j * .89;
            panel(m, -.76, y - .32, -.58, .75, .64, .70);
            m.armor(-.63, y - .22, .15, .48, .44, .18, .08, PLATE);
            m.tube(-.2, y, 0, 1.36, y + .25, .40, .19, .19, BLACK);
            m.tube(.16, y + .06, .10, 1.48, y + .28, .44, .115, .115, RIM);
            m.spike(1.22, y + .23, .4, 2.20, y + .48, .65, .28, STEEL);
            panel(m, .18, y - .20, .12, .44, .42, .47);
            m.box(.28, y - .055, .62, .22, .12, .045, LIGHT);
            ring(m, -.43, y, -.70, .28, .095, .1, STEEL);
            m.tube(-.48, y, -.8, -.95, y + .32, -.70, .07, .07, BLACK);
            m.tube(-.95, y + .32, -.7, -.62, y + .52, .02, .07, .07, BLACK);
            m.armor(-.41, y - .26, -.39, 1.04, .49, .57, .11, STEEL);
            m.armor(.68, y + .02, .02, .42, .35, .46, .09, PLATE);
            m.box(.77, y + .08, .50, .15, .16, .04, BRASS);
            m.tube(.14, y - .10, -.22, 1.40, y + .13, .12, .055, .055, RIM);
            m.tube(1.64, y + .34, .52, 1.39, y + .57, .40, .16, .015, PLATE);
            m.tube(1.64, y + .34, .52, 1.47, y + .10, .40, .16, .015, PLATE);
            m.armor(.99, y + .10, .23, .28, .31, .39, .07, BLACK);
            m.box(1.05, y + .19, .64, .13, .08, .025, BRASS);
        }
        m.tube(-.53, -1.35, -.20, -.53, 1.38, -.20, .10, .10, BRASS);
    }

    private static void projector(ExpeditionMesh m) {
        m.armor(-.77, -.68, -1.15, 1.54, 1.36, 1.97, .27, STEEL);
        for (int s : new int[] {-1, 1}) {
            panel(m, s < 0 ? -1.04 : .64, -.63, -.92, .40, 1.26, 1.50);
            m.tube(s * .78, -.38, -1.20, s * .78, -.38, .6, .085, .085, RIM);
        }
        ring(m, 0, 0, .82, .68, .16, .2, STEEL);
        ring(m, 0, 0, 1.02, .46, .105, .1, LIGHT);
        m.box(-.24, -.24, .98, .48, .48, .08, BLACK).box(-.12, -.12, 1.08, .24, .24, .05, WHITE);
        for (int j = 0; j < 4; j++) panel(m, -.56, .60, -1.05 + j * .4, 1.12, .26, .22);
        for (int s : new int[] {-1, 1}) {
            m.armor(s < 0 ? -1.10 : .76, -.29, -.26, .34, .58, .65, .09, STEEL);
            m.box(s < 0 ? -1.12 : 1.06, -.16, -.09, .06, .30, .30, 0x288D51);
        }

        m.armor(-.65, .43, .77, 1.30, .22, .48, .08, PLATE);
        m.armor(-.65, -.65, .77, 1.30, .22, .48, .08, PLATE);
        m.armor(-.69, -.35, .78, .22, .70, .46, .07, PLATE);
        m.armor(.47, -.35, .78, .22, .70, .46, .07, PLATE);
        for (int s : new int[] {-1, 1})
            m.tube(s * .45, .43, -.91, s * .45, .43, .68, .07, .07, BRASS);
    }

    private static void flail(ExpeditionMesh m) {

        m.tube(0, -1.45, 0, 0, 1.22, 0, .27, .27, BLACK);
        for (int j = 0; j < 3; j++) {
            double y = -.87 + j * .86;
            m.armor(-.56, y - .26, -.56, 1.12, .52, 1.12, .15, STEEL);
            for (int n = 0; n < 6; n++) {
                double a = n * Math.PI / 3 + (j % 2) * Math.PI / 6,
                        x = Math.cos(a),
                        z = Math.sin(a);
                m.armor(x * .66 - .23, y - .25, z * .66 - .23, .46, .50, .46, .09, PLATE);
                m.spike(x * .79, y, z * .79, x * 1.34, y + .13, z * 1.34, .29, STEEL);
                m.spike(x * .86, y + .06, z * .86, x * 1.26, y + .15, z * 1.26, .11, RIM);
                m.armor(x * .62 - .13, y - .15, z * .62 - .13, .26, .30, .26, .06, BLACK);
                m.box(x * .51 - .065, y + .27, z * .51 - .065, .13, .08, .13, LIGHT);
            }
        }
        m.box(-.18, -1.46, -.18, .36, .20, .36, LIGHT);
    }

    private BrawlerRebuiltModel() {}
}
