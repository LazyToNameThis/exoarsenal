package com.exoarsenal.client;

import com.exoarsenal.expedition.client.ExpeditionMesh;

public final class BrawlerSkin {
    private static final java.util.Map<ExpeditionMesh.Face, double[][]> UVS =
            new java.util.IdentityHashMap<>();
    private static final double[][] BOUNDS = {
        {-1.6, -2, -1.6, 1.6, 2.2, 1.1},
        {-1.7, -1.1, -2.2, 1.5, 1.4, 1.5},
        {-1.1, -2.1, -.9, 2.2, 1.8, .8},
        {-1.3, -1.6, -1, 2.5, 1.9, .8},
        {-1.3, -1.1, -1.3, 1.3, 1.2, 1.2},
        {-1.4, -1.7, -1.4, 1.4, 1.5, 1.4}
    };

    public static boolean emissive(int color) {
        return color == 0x70F2A0 || color == 0xDAFFD6;
    }

    public static float materialShade(int color) {
        return color == 0x20282B
                ? .78F
                : color == 0xABB3AE ? 1.10F : color == 0xA58B5D ? 1.03F : 1F;
    }

    public static double[] uv(int part, ExpeditionMesh.Face face, int vertex) {
        double[][] saved = UVS.get(face);
        if (saved == null) {
            saved = new double[4][];
            for (int n = 0; n < 4; n++) saved[n] = unwrap(part, face, n);
            UVS.put(face, saved);
        }
        return saved[vertex];
    }

    private static double[] unwrap(int part, ExpeditionMesh.Face face, int vertex) {
        int region =
                face.color == 0x20282B
                        ? 2
                        : face.color == 0x505B60
                                ? 1
                                : face.color == 0xABB3AE
                                        ? 3
                                        : face.color == 0xA58B5D
                                                ? 4
                                                : face.color == 0x288D51
                                                        ? 7
                                                        : Math.abs(face.nz) < .5 ? 5 : 0;
        if (region == 0 && face.ny > .6) region = 6;
        double u, v;
        if (face.localUv) {
            u = face.uv[vertex][0];
            v = face.uv[vertex][1];
        } else {
            int a = Math.abs(face.nx) > .7 ? 2 : 0, b = Math.abs(face.ny) > .7 ? 2 : 1;
            double loA = Double.POSITIVE_INFINITY, loB = loA, hiA = -loA, hiB = -loA;
            for (double[] p : face.p) {
                loA = Math.min(loA, p[a]);
                hiA = Math.max(hiA, p[a]);
                loB = Math.min(loB, p[b]);
                hiB = Math.max(hiB, p[b]);
            }

            if (java.util.Arrays.equals(face.p[0], face.p[3])) {
                loA = BOUNDS[part][a];
                hiA = BOUNDS[part][a + 3];
                loB = BOUNDS[part][b];
                hiB = BOUNDS[part][b + 3];
            }
            u = (face.p[vertex][a] - loA) / Math.max(.00001, hiA - loA);
            v = 1 - (face.p[vertex][b] - loB) / Math.max(.00001, hiB - loB);
        }

        double area = 0;
        for (int n = 1; n < 3; n++) {
            double[] a = face.p[0], b = face.p[n], c = face.p[n + 1];
            double x = (b[1] - a[1]) * (c[2] - a[2]) - (b[2] - a[2]) * (c[1] - a[1]);
            double y = (b[2] - a[2]) * (c[0] - a[0]) - (b[0] - a[0]) * (c[2] - a[2]);
            double z = (b[0] - a[0]) * (c[1] - a[1]) - (b[1] - a[1]) * (c[0] - a[0]);
            area += Math.sqrt(x * x + y * y + z * z) * .5;
        }
        if (area < .16 && !face.localUv && region != 7) {
            u = .29 + clamp(u) * .42;
            v = .29 + clamp(v) * .42;
        }
        return new double[] {
            (region * 8 + .5 + clamp(u) * 7) / 64, (part * 8 + .5 + clamp(v) * 7) / 48
        };
    }

    private static double clamp(double v) {
        return Math.max(0, Math.min(1, v));
    }

    private BrawlerSkin() {}
}
