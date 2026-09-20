package com.exoarsenal.client;

import java.util.ArrayList;
import java.util.List;

public final class ScoutWingGeometry {
    public static final class Vertex {
        public final double x, y, z, nx, ny, nz;
        public final float light;

        Vertex(double x, double y, double z, double nx, double ny, double nz, float light) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.nx = nx;
            this.ny = ny;
            this.nz = nz;
            this.light = light;
        }
    }

    public static final List<Vertex> INNER = new ArrayList<>(), OUTER = new ArrayList<>();

    static {
        feather(INNER, 6.2, 1.0, -8, 44, 9, -12, 57, 9, -23, 60, 11, -36, 55, 13);
        feather(INNER, 6.5, 1.1, -11, 48, 9, -18, 61, 9, -27, 64, 11, -44, 56, 14);
        feather(INNER, 6.0, 1.0, -16, 54, 9, -23, 67, 10, -35, 71, 12, -52, 64, 15);
        feather(INNER, 5.2, .9, -22, 60, 10, -31, 74, 11, -44, 78, 13, -59, 74, 16);
        feather(INNER, 5.0, .8, -9, 43, 10, -16, 45, 12, -24, 29, 14, -27, 15, 15);
        feather(INNER, 5.8, .9, -13, 48, 11, -23, 48, 13, -32, 29, 15, -36, 10, 16);
        feather(INNER, 6.2, 1.0, -18, 53, 12, -30, 52, 14, -41, 29, 16, -46, 12, 17);
        feather(INNER, 6.0, .9, -24, 58, 13, -37, 55, 15, -50, 33, 17, -56, 17, 18);

        feather(OUTER, 6.1, 1.0, -27, 63, 11, -41, 65, 13, -56, 40, 16, -64, 21, 19);
        feather(OUTER, 6.0, .95, -31, 67, 11, -46, 68, 13, -64, 47, 16, -73, 29, 20);
        feather(OUTER, 5.8, .9, -35, 70, 12, -51, 73, 14, -70, 55, 17, -80, 39, 21);
        feather(OUTER, 5.5, .85, -39, 73, 12, -57, 77, 14, -75, 64, 18, -86, 50, 22);
        feather(OUTER, 5.1, .8, -43, 75, 13, -62, 82, 15, -79, 75, 18, -90, 64, 22);
        feather(OUTER, 4.6, .75, -46, 76, 13, -61, 87, 15, -78, 88, 18, -91, 80, 22);

        feather(OUTER, 3.8, .7, -33, 68, 10, -45, 83, 11, -59, 94, 14, -75, 96, 17);
        feather(OUTER, 3.2, .65, -27, 62, 10, -34, 77, 10, -45, 89, 12, -57, 94, 15);
    }

    private ScoutWingGeometry() {}

    private static double cubic(double a, double b, double c, double d, double t) {
        double u = 1 - t;
        return u * u * u * a + 3 * u * u * t * b + 3 * u * t * t * c + t * t * t * d;
    }

    private static Vertex vertex(double[] p, double width, double thickness, int row, int side) {
        double t = row / 32D, a = side * Math.PI / 6, u = 1 - t;
        double x = cubic(p[0], p[3], p[6], p[9], t),
                y = cubic(p[1], p[4], p[7], p[10], t),
                z = cubic(p[2], p[5], p[8], p[11], t);
        double dx =
                3 * u * u * (p[3] - p[0]) + 6 * u * t * (p[6] - p[3]) + 3 * t * t * (p[9] - p[6]);
        double dy =
                3 * u * u * (p[4] - p[1]) + 6 * u * t * (p[7] - p[4]) + 3 * t * t * (p[10] - p[7]);
        double length = Math.max(.001, Math.sqrt(dx * dx + dy * dy)),
                nx = -dy / length,
                ny = dx / length;
        double taper = Math.pow(Math.sin(Math.PI * (.035 + .965 * t)), .68) * (1 - .48 * t);
        double lateral = Math.cos(a) * width * taper, depth = Math.sin(a) * thickness * taper;
        float light = (float) (.08 + .7 * Math.pow(Math.abs(Math.sin(a)), 4) + .1 * t);
        return new Vertex(
                x + nx * lateral,
                y + ny * lateral,
                z + depth,
                nx * Math.cos(a),
                ny * Math.cos(a),
                Math.sin(a),
                light);
    }

    private static void feather(List<Vertex> out, double width, double thickness, double... p) {
        for (int row = 0; row < 32; row++)
            for (int side = 0; side < 12; side++) {
                out.add(vertex(p, width, thickness, row, side));
                out.add(vertex(p, width, thickness, row + 1, side));
                out.add(vertex(p, width, thickness, row + 1, (side + 1) % 12));
                out.add(vertex(p, width, thickness, row, (side + 1) % 12));
            }
    }
}
