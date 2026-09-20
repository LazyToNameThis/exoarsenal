package com.scapeandrun.frostbite.client;

import com.scapeandrun.frostbite.expedition.client.ExpeditionMesh;
import java.util.*;

public final class ExcavatorModel {
    public static final class Face {
        public final double[][] points;
        public final int region;
        public final float nx, ny, nz, shade;

        Face(double[][] p, int region) {
            points = p;
            this.region = region;
            double ux = p[1][0] - p[0][0],
                    uy = p[1][1] - p[0][1],
                    uz = p[1][2] - p[0][2],
                    vx = p[2][0] - p[0][0],
                    vy = p[2][1] - p[0][1],
                    vz = p[2][2] - p[0][2];
            double x = uy * vz - uz * vy,
                    y = uz * vx - ux * vz,
                    z = ux * vy - uy * vx,
                    length = Math.max(.00001, Math.sqrt(x * x + y * y + z * z));
            nx = (float) (x / length);
            ny = (float) (y / length);
            nz = (float) (z / length);
            shade = Math.max(.58F, Math.min(1F, .78F + .18F * ny - .08F * nz + .04F * nx));
        }
    }

    public static final List<Face> HEAD = new ArrayList<>(),
            DRILL = new ArrayList<>(),
            BODY = new ArrayList<>(),
            TAIL = new ArrayList<>(),
            TURRET = new ArrayList<>(),
            PROBE = new ArrayList<>(),
            STABILIZER = new ArrayList<>();
    public static final List<Face> PROBE_GUN = new ArrayList<>(), PROBE_SOCKET = new ArrayList<>();
    public static final List<Face> HEAD_JAW = new ArrayList<>(),
            PISTON = new ArrayList<>(),
            TURRET_BARREL = new ArrayList<>(),
            VENT = new ArrayList<>();
    public static final List<Face> POWER_BODY = new ArrayList<>(),
            COOLING_BODY = new ArrayList<>(),
            CARRIER_BODY = new ArrayList<>();

    public static List<Face> body(int index) {
        return index % 4 == 0
                ? POWER_BODY
                : index % 4 == 2 ? COOLING_BODY : index % 4 == 3 ? CARRIER_BODY : BODY;
    }

    static {
        sleeve(HEAD, new double[] {-11, -5, 4, 9}, new double[] {10, 14, 13, 10}, 0, 0, 4);
        ring(HEAD, 10, -10, 2, 9);
        box(HEAD, -5, 10, -7, 10, 4, 9, 3);
        box(HEAD, -3, 14, -6, 6, 2, 5, 12);

        box(HEAD, -4, 13.7, -5, 8, .8, 1, 17);
        box(HEAD, -2, 14.5, -4, 4, .45, 2, 14);
        for (int side : new int[] {-1, 1}) {
            box(HEAD, side * 8 - 1, 7, -8, 2, 2, 3, 17);
            box(HEAD, side * 8 - 1, -8, -8, 2, 2, 3, 17);
        }
        for (int slat = 0; slat < 4; slat++) box(HEAD, -4 + slat * 2, -10, -8, 1, 1.4, 7, 18);

        sleeve(
                HEAD_JAW,
                new double[] {-6, -3, 7, 11},
                new double[] {3.2, 4.3, 3.8, 2.4},
                Math.PI / 8,
                0,
                5);
        box(HEAD_JAW, -2.8, 2.4, -2, 5.6, 1.2, 8, 17);
        box(HEAD_JAW, -1.2, 3.4, 1, 2.4, .7, 4, 12);
        box(PISTON, -.65, -.65, 0, 1.3, 1.3, 10, 17);
        box(PISTON, -1.2, -1.2, -2, 2.4, 2.4, 4, 9);
        for (int rib = 0; rib < 3; rib++) box(VENT, -4, 0, rib * 2.2, 8, .7, 1.3, 18);
        sleeve(
                DRILL,
                new double[] {9, 14, 21, 28, 34, 36},
                new double[] {9.6, 8.2, 6.1, 3.8, 1.4, .08},
                0,
                .18,
                16);

        for (int flute = 0; flute < 3; flute++)
            for (int j = 0; j < 6; j++) {
                double z = 10 + j * 4, a = flute * Math.PI * 2 / 3 + j * .33, r = 9.8 - j * 1.55;
                ridge(DRILL, a, r, z, a + .33, Math.max(.15, r - 1.55), z + 4, 1.45, 21 + flute);
            }
        sleeve(BODY, new double[] {-11, -8, 6, 10}, new double[] {7.5, 10.5, 10.5, 7.5}, 0, 0, 1);
        box(BODY, -4, 9, -6, 8, 2.5, 11, 2);
        box(BODY, -3, -11, -6, 6, 2, 11, 8);

        box(BODY, -7, -5, -13, 1.2, 1.2, 6, 17);
        box(BODY, 5.8, -5, -13, 1.2, 1.2, 6, 17);
        POWER_BODY.addAll(BODY);
        COOLING_BODY.addAll(BODY);
        CARRIER_BODY.addAll(BODY);
        sleeve(
                POWER_BODY,
                new double[] {-5, -3, 3, 5},
                new double[] {10.8, 12.1, 12.1, 10.8},
                Math.PI / 8,
                0,
                9);
        box(POWER_BODY, -3, 11, -3, 6, 1.8, 6, 14);
        box(POWER_BODY, -12, -2, -3, 1.8, 4, 6, 12);
        box(POWER_BODY, 10.2, -2, -3, 1.8, 4, 6, 13);
        for (int fin = 0; fin < 5; fin++) {
            double z = -7 + fin * 3;
            box(COOLING_BODY, -8, 9, z, 16, 3.5, .8, 17);
            box(COOLING_BODY, -11, -5, z, 1, 10, .8, 18);
            box(COOLING_BODY, 10, -5, z, 1, 10, .8, 18);
        }
        box(COOLING_BODY, -5, 12, -8, 2, 1, 15, 9);
        box(COOLING_BODY, 3, 12, -8, 2, 1, 15, 9);
        box(CARRIER_BODY, -8, 8, -8, 3, 4, 15, 3);
        box(CARRIER_BODY, 5, 8, -8, 3, 4, 15, 3);
        box(CARRIER_BODY, -4, 8.5, -7, 8, 1, 13, 9);
        box(CARRIER_BODY, -3, 9.5, -5, 6, .5, 2, 15);
        ring(TAIL, 8, 0, 6, 4);
        box(TAIL, -5, -5, -9, 10, 10, 9, 1);
        box(TAIL, -3, -3, -18, 6, 6, 9, 9);
        for (int side : new int[] {-1, 1}) box(TAIL, side < 0 ? -9 : 5, -2, -14, 4, 4, 12, 10);
        box(TURRET, -4, 0, -5, 8, 3, 10, 9);
        box(TURRET, -3, 3, -4, 6, 6, 8, 10);
        List<Face> receiver = new ArrayList<>();
        sleeve(
                receiver,
                new double[] {-5, -2, 7, 12},
                new double[] {2.5, 4.5, 4, 2.2},
                Math.PI / 8,
                0,
                9);
        for (Face face : receiver) {
            double[][] p = new double[4][3];
            for (int i = 0; i < 4; i++)
                p[i] = new double[] {face.points[i][0], face.points[i][1] + 9.5, face.points[i][2]};
            TURRET.add(new Face(p, face.region));
        }
        box(TURRET_BARREL, -1.2, 8.3, 10, 2.4, 2.4, 16, 24);
        box(TURRET_BARREL, -3, 8, 12, 1, 3, 11, 17);
        box(TURRET_BARREL, 2, 8, 12, 1, 3, 11, 17);
        box(TURRET_BARREL, -2.5, 7, 23, 5, 5, 3, 25);
        box(TURRET_BARREL, -1, 8.5, 26, 2, 2, 0.5, 14);
        box(TURRET, -1, 12, -1, 2, 7, 2, 15);
        sleeve(
                PROBE,
                new double[] {-4, -2, 3, 5},
                new double[] {2.8, 4.8, 4.8, 3.3},
                Math.PI / 8,
                0,
                26);
        box(PROBE, -3, -3, 4, 6, 6, 2, 27);
        box(PROBE, -2, -2, 6, 4, 4, 1, 12);
        box(PROBE, -1, 4, -1, 2, 6, 2, 15);
        box(PROBE, -9, -1, -1, 5, 2, 3, 28);
        box(PROBE, 4, -1, -1, 5, 2, 3, 29);
        box(PROBE, -1, -9, -1, 2, 5, 3, 30);
        box(PROBE_GUN, -2, -2, 4, 4, 4, 5, 26);
        box(PROBE_GUN, -1.3, -1.3, 9, 2.6, 2.6, 6, 24);
        box(PROBE_GUN, -2, -2, 14, 4, 4, 2, 25);
        box(PROBE_GUN, -.8, -.8, 16, 1.6, 1.6, .5, 14);
        box(PROBE_SOCKET, -5, -5, -2, 10, 10, 3, 9);
        box(PROBE_SOCKET, -6, -6, 0, 2, 12, 3, 10);
        box(PROBE_SOCKET, 4, -6, 0, 2, 12, 3, 10);
        box(STABILIZER, -2, -2, -2, 4, 4, 12, 9);
        box(STABILIZER, -3, -5, 7, 6, 6, 5, 10);
        box(STABILIZER, -4, -12, 9, 8, 7, 4, 31);
    }

    private static void sleeve(
            List<Face> out,
            double[] depth,
            double[] radii,
            double angle,
            double twist,
            int region) {
        for (int band = 0; band < depth.length - 1; band++)
            for (int side = 0; side < 8; side++) {
                double a = angle + side * Math.PI / 4, b = a + Math.PI / 4;
                out.add(
                        new Face(
                                new double[][] {
                                    point(a + band * twist, radii[band], depth[band]),
                                    point(b + band * twist, radii[band], depth[band]),
                                    point(b + (band + 1) * twist, radii[band + 1], depth[band + 1]),
                                    point(a + (band + 1) * twist, radii[band + 1], depth[band + 1])
                                },
                                region + side % 2));
            }
    }

    private static double[] point(double angle, double radius, double z) {
        return new double[] {Math.cos(angle) * radius, Math.sin(angle) * radius, z};
    }

    private static void ridge(
            List<Face> out,
            double a,
            double r,
            double z,
            double b,
            double s,
            double zz,
            double height,
            int region) {
        double[] p = point(a, r, z),
                q = point(b, s, zz),
                tip = point(b + .10, s + height, zz),
                base = point(a + .10, r + height, z);
        out.add(new Face(new double[][] {p, q, tip, base}, region));
        out.add(
                new Face(
                        new double[][] {base, tip, point(b + .24, s, zz), point(a + .24, r, z)},
                        19));
    }

    private static void ring(List<Face> out, double radius, double z, double depth, int region) {
        for (int i = 0; i < 8; i++)
            rotatedBox(
                    out,
                    -radius * .42,
                    radius - 2,
                    z,
                    radius * .84,
                    4,
                    depth,
                    i * Math.PI / 4,
                    region);
    }

    private static void box(
            List<Face> out,
            double x,
            double y,
            double z,
            double w,
            double h,
            double d,
            int region) {
        rotatedBox(out, x, y, z, w, h, d, 0, region);
    }

    private static void rotatedBox(
            List<Face> out,
            double x,
            double y,
            double z,
            double w,
            double h,
            double d,
            double angle,
            int region) {
        ExpeditionMesh mesh = new ExpeditionMesh().box(x, y, z, w, h, d, 0xFFFFFF);
        for (ExpeditionMesh.Face face : mesh.faces) {
            double[][] p = new double[4][3];
            for (int i = 0; i < 4; i++) {
                double[] v = face.p[i];
                p[i] =
                        new double[] {
                            v[0] * Math.cos(angle) - v[1] * Math.sin(angle),
                            v[0] * Math.sin(angle) + v[1] * Math.cos(angle),
                            v[2]
                        };
            }
            out.add(new Face(p, region));
        }
    }

    private ExcavatorModel() {}
}
