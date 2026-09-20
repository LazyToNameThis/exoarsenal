package com.scapeandrun.frostbite.client;

import java.util.*;

public final class DesertScourgeMesh {
    public static final class Quad {
        public final float[][] p;
        public final float[] normal;

        Quad(float[][] p, float[] n) {
            this.p = p;
            normal = n;
        }
    }

    private static final Map<String, List<Quad>> PARTS = new HashMap<>();

    static {
        PARTS.put(
                "headShell",
                elliptic(
                        new double[][] {
                            {0, 0, -12, 8.5}, {0, 0, -8, 11}, {0, 0, -1, 11.5}, {0, 0, 5, 8.5}
                        },
                        .76));
        PARTS.put(
                "bodyShell",
                elliptic(
                        new double[][] {
                            {0, 0, -10, 7}, {0, 0, -6, 9}, {0, 0, 3, 9}, {0, 0, 9, 7.4}
                        },
                        .9));
        PARTS.put(
                "tailShell",
                elliptic(
                        new double[][] {
                            {0, 0, 8, 6.5},
                            {0, 0, 0, 6},
                            {0, 0, -10, 4.8},
                            {0, 0, -20, 2.8},
                            {0, 1, -31, .8},
                            {0, 2, -39, .03}
                        },
                        .9));
        PARTS.put(
                "headUpperJaw",
                elliptic(
                        new double[][] {
                            {0, 13, 0, 8}, {0, 13, 6, 8}, {0, 13, 12, 6.6}, {0, 13, 17, 4.6}
                        },
                        .4));
        PARTS.put(
                "headLowerJaw",
                elliptic(
                        new double[][] {
                            {0, -17, 0, 6.8}, {0, -17, 7, 6.8}, {0, -17, 12, 5.2}, {0, -17, 16, 3.8}
                        },
                        .4));
        List<Quad> horns = new ArrayList<>();
        loft(
                horns,
                new double[][] {
                    {-10, 4, -3, 3.1},
                    {-15, 5, 5, 2.6},
                    {-18, 6, 13, 1.7},
                    {-17, 7, 20, .8},
                    {-13, 7, 25, .04}
                });
        loft(
                horns,
                new double[][] {
                    {10, 4, -3, 3.1},
                    {15, 5, 5, 2.6},
                    {18, 6, 13, 1.7},
                    {17, 7, 20, .8},
                    {13, 7, 25, .04}
                });
        loft(
                horns,
                new double[][] {
                    {0, 9, -6, 2.8}, {0, 14, -9, 2}, {0, 18, -13, 1}, {0, 21, -18, .03}
                });
        loft(
                horns,
                new double[][] {
                    {-8, -4, 0, 1.8}, {-12, -5, 6, 1.35}, {-13, -4, 11, .65}, {-10, -3, 15, .03}
                });
        loft(
                horns,
                new double[][] {
                    {8, -4, 0, 1.8}, {12, -5, 6, 1.35}, {13, -4, 11, .65}, {10, -3, 15, .03}
                });
        PARTS.put("headHorns", Collections.unmodifiableList(horns));
        List<Quad> upper = new ArrayList<>();
        fang(upper, -6, 3.8, 7, -1, 5.3);
        fang(upper, 6, 3.8, 7, -1, 5.3);
        fang(upper, -3.8, 4.2, 12, -1, 4.5);
        fang(upper, 3.8, 4.2, 12, -1, 4.5);
        fang(upper, 0, 4.5, 14, -1, 3.4);
        PARTS.put("headUpperTeeth", Collections.unmodifiableList(upper));
        List<Quad> lower = new ArrayList<>();
        fang(lower, -4.5, -5.4, 8, 1, 4.5);
        fang(lower, 4.5, -5.4, 8, 1, 4.5);
        fang(lower, -1.8, -5.4, 12, 1, 3.5);
        fang(lower, 1.8, -5.4, 12, 1, 3.5);
        PARTS.put("headLowerTeeth", Collections.unmodifiableList(lower));
        List<Quad> body = new ArrayList<>();
        loft(
                body,
                new double[][] {
                    {0, 8, -1, 2.1}, {0, 12, -5, 1.5}, {0, 15, -10, .65}, {0, 16, -14, .03}
                });
        loft(
                body,
                new double[][] {
                    {-8, 0, 1, 1.8}, {-12, 0, -2, 1.3}, {-15, 1, -6, .55}, {-16, 1, -10, .03}
                });
        loft(
                body,
                new double[][] {
                    {8, 0, 1, 1.8}, {12, 0, -2, 1.3}, {15, 1, -6, .55}, {16, 1, -10, .03}
                });
        PARTS.put("bodySpines", Collections.unmodifiableList(body));
        List<Quad> tail = new ArrayList<>();
        loft(
                tail,
                new double[][] {
                    {0, 4, -1, 2}, {0, 8, -6, 1.3}, {0, 10, -12, .5}, {0, 10, -17, .03}
                });
        loft(
                tail,
                new double[][] {
                    {-5, 0, -4, 2}, {-10, 0, -9, 1.3}, {-11, 0, -15, .5}, {-8, 0, -19, .03}
                });
        loft(
                tail,
                new double[][] {
                    {5, 0, -4, 2}, {10, 0, -9, 1.3}, {11, 0, -15, .5}, {8, 0, -19, .03}
                });
        loft(
                tail,
                new double[][] {
                    {0, 0, -19, 2.2}, {0, 1, -27, 1.4}, {0, 2, -34, .6}, {0, 3, -40, .02}
                });
        PARTS.put("tailSpines", Collections.unmodifiableList(tail));
    }

    private DesertScourgeMesh() {}

    public static List<Quad> part(String name) {
        return PARTS.get(name);
    }

    private static List<Quad> elliptic(double[][] path, double flatten) {
        List<Quad> raw = new ArrayList<>(), result = new ArrayList<>();
        loft(raw, path);
        for (Quad q : raw) {
            float[][] p = new float[4][3];
            for (int i = 0; i < 4; i++)
                p[i] = new float[] {q.p[i][0], (float) (q.p[i][1] * flatten), q.p[i][2]};
            double[] n = unit(q.normal[0], q.normal[1] / flatten, q.normal[2]);
            result.add(new Quad(p, new float[] {(float) n[0], (float) n[1], (float) n[2]}));
        }
        return Collections.unmodifiableList(result);
    }

    private static void fang(
            List<Quad> list, double x, double y, double z, int side, double length) {
        loft(
                list,
                new double[][] {
                    {x, y, z, 1.15},
                    {x, y + side * length * .45, z + .3, .9},
                    {x * .95, y + side * length * .83, z + 1, .45},
                    {x * .9, y + side * length, z + 1.8, .02}
                });
    }

    private static void loft(List<Quad> faces, double[][] points) {

        for (int row = 0; row < points.length - 1; row++) {
            double[] a = points[row], b = points[row + 1];
            double[] tangent = unit(b[0] - a[0], b[1] - a[1], b[2] - a[2]);
            double[] u =
                    Math.abs(tangent[1]) > .98
                            ? new double[] {1, 0, 0}
                            : unit(tangent[2], 0, -tangent[0]);
            double[] v = {
                tangent[1] * u[2] - tangent[2] * u[1],
                tangent[2] * u[0] - tangent[0] * u[2],
                tangent[0] * u[1] - tangent[1] * u[0]
            };
            int steps = Math.abs(a[3] - b[3]) < .2 ? 1 : 3;
            for (int step = 0; step < steps; step++) {
                double start = (double) step / steps, end = (double) (step + 1) / steps;
                double radius = Math.max(.25, a[3] + (b[3] - a[3]) * (start + end) * .5);
                float[][] corners = new float[8][3];
                for (int endIndex = 0; endIndex < 2; endIndex++)
                    for (int corner = 0; corner < 4; corner++) {
                        double t = endIndex == 0 ? start - .018 : end + .018;
                        double du = (corner == 0 || corner == 3 ? -1 : 1) * radius,
                                dv = (corner < 2 ? -1 : 1) * radius;
                        double x = a[0] + (b[0] - a[0]) * t + u[0] * du + v[0] * dv;
                        double y = a[1] + (b[1] - a[1]) * t + u[1] * du + v[1] * dv;
                        double z = a[2] + (b[2] - a[2]) * t + u[2] * du + v[2] * dv;
                        corners[endIndex * 4 + corner] =
                                new float[] {(float) -x / 16, (float) y / 16, (float) z / 16};
                    }
                int[][] sides = {
                    {0, 1, 5, 4},
                    {1, 2, 6, 5},
                    {2, 3, 7, 6},
                    {3, 0, 4, 7},
                    {3, 2, 1, 0},
                    {4, 5, 6, 7}
                };
                for (int[] side : sides)
                    addFace(
                            faces,
                            new float[][] {
                                corners[side[0]],
                                corners[side[1]],
                                corners[side[2]],
                                corners[side[3]]
                            });
            }
        }
    }

    private static void addFace(List<Quad> faces, float[][] p) {
        double ax = p[1][0] - p[0][0],
                ay = p[1][1] - p[0][1],
                az = p[1][2] - p[0][2],
                bx = p[3][0] - p[0][0],
                by = p[3][1] - p[0][1],
                bz = p[3][2] - p[0][2];
        double[] n = unit(ay * bz - az * by, az * bx - ax * bz, ax * by - ay * bx);
        faces.add(new Quad(p, new float[] {(float) n[0], (float) n[1], (float) n[2]}));
    }

    private static double[] unit(double x, double y, double z) {
        double len = Math.sqrt(x * x + y * y + z * z);
        return len < 1e-9 ? new double[] {1, 0, 0} : new double[] {x / len, y / len, z / len};
    }
}
