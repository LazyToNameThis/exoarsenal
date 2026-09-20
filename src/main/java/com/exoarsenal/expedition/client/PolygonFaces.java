package com.exoarsenal.expedition.client;

import java.util.*;

public final class PolygonFaces {
    private PolygonFaces() {}

    public static List<int[]> triangles(double[][] points) {
        if (points.length < 3)
            throw new IllegalArgumentException("A silhouette needs at least three corners");
        List<Integer> ring = new ArrayList<>();
        double area = 0;
        for (int i = 0; i < points.length; i++) {
            double[] a = points[i], b = points[(i + 1) % points.length];
            if (a.length != 2 || !Double.isFinite(a[0]) || !Double.isFinite(a[1]))
                throw new IllegalArgumentException("Invalid silhouette point");
            area += a[0] * b[1] - b[0] * a[1];
            ring.add(i);
        }
        if (Math.abs(area) < 1e-9) throw new IllegalArgumentException("Zero-area silhouette");
        if (area < 0) Collections.reverse(ring);
        List<int[]> result = new ArrayList<>();
        while (ring.size() > 3) {
            boolean clipped = false;
            for (int i = 0; i < ring.size(); i++) {
                int a = ring.get((i + ring.size() - 1) % ring.size()),
                        b = ring.get(i),
                        c = ring.get((i + 1) % ring.size());
                double bend = cross(points[a], points[b], points[c]);
                if (Math.abs(bend) < 1e-10) {
                    ring.remove(i);
                    clipped = true;
                    break;
                }
                if (bend < 0) continue;
                boolean contains = false;
                for (int p : ring)
                    if (p != a
                            && p != b
                            && p != c
                            && inside(points[p], points[a], points[b], points[c])) {
                        contains = true;
                        break;
                    }
                if (contains) continue;
                result.add(new int[] {a, b, c});
                ring.remove(i);
                clipped = true;
                break;
            }
            if (!clipped)
                throw new IllegalArgumentException(
                        "Self-intersecting silhouette: " + Arrays.deepToString(points));
        }
        result.add(new int[] {ring.get(0), ring.get(1), ring.get(2)});
        return result;
    }

    private static boolean inside(double[] p, double[] a, double[] b, double[] c) {
        return cross(a, b, p) >= -1e-10 && cross(b, c, p) >= -1e-10 && cross(c, a, p) >= -1e-10;
    }

    private static double cross(double[] a, double[] b, double[] c) {
        return (b[0] - a[0]) * (c[1] - a[1]) - (b[1] - a[1]) * (c[0] - a[0]);
    }
}
