package com.exoarsenal.expedition.client;

import java.util.*;

public final class ExpeditionMesh {
    public static final class Face {
        public final double[][] p, uv;
        public final int color;
        public final float alpha, nx, ny, nz;
        public boolean localUv;

        Face(double[][] p, int c, float a) {
            this.p = p;
            color = c;
            alpha = a;
            double ux = p[1][0] - p[0][0],
                    uy = p[1][1] - p[0][1],
                    uz = p[1][2] - p[0][2],
                    vx = p[2][0] - p[0][0],
                    vy = p[2][1] - p[0][1],
                    vz = p[2][2] - p[0][2];
            double x = uy * vz - uz * vy,
                    y = uz * vx - ux * vz,
                    z = ux * vy - uy * vx,
                    length = Math.sqrt(x * x + y * y + z * z);
            if (length < 1e-9) length = 1;
            nx = (float) (x / length);
            ny = (float) (y / length);
            nz = (float) (z / length);
            uv = new double[4][2];
            int axis =
                    Math.abs(x) >= Math.abs(y) && Math.abs(x) >= Math.abs(z)
                            ? 0
                            : Math.abs(y) >= Math.abs(z) ? 1 : 2;
            for (int i = 0; i < 4; i++) {
                uv[i][0] = p[i][axis == 0 ? 2 : 0] / 16D;
                uv[i][1] = p[i][axis == 1 ? 2 : 1] / 16D;
            }
        }
    }

    public final List<Face> faces = new ArrayList<>();

    public ExpeditionMesh hingeX(double y, double z, double angle) {
        List<Face> result = new ArrayList<>();
        double c = Math.cos(angle), s = Math.sin(angle);
        for (Face f : faces) {
            double[][] p = new double[4][3];
            for (int i = 0; i < 4; i++) {
                double[] v = f.p[i];
                p[i] =
                        new double[] {
                            v[0],
                            y + (v[1] - y) * c - (v[2] - z) * s,
                            z + (v[1] - y) * s + (v[2] - z) * c
                        };
            }
            Face copy = new Face(p, f.color, f.alpha);
            copy.localUv = f.localUv;
            for (int i = 0; i < 4; i++) {
                copy.uv[i][0] = f.uv[i][0];
                copy.uv[i][1] = f.uv[i][1];
            }
            result.add(copy);
        }
        faces.clear();
        faces.addAll(result);
        return this;
    }

    public ExpeditionMesh armor(
            double x, double y, double z, double w, double h, double d, double bevel, int color) {
        int first = faces.size();
        double b = Math.min(bevel, Math.min(w, h) * .24), lip = Math.min(b, d * .24);
        double[][] outline = {
            {b, 0}, {w - b, 0}, {w, b}, {w, h - b}, {w - b, h}, {b, h}, {0, h - b}, {0, b}
        };
        double[][][] rings = new double[4][8][3];
        for (int r = 0; r < 4; r++)
            for (int k = 0; k < 8; k++) {
                double inset = r == 0 || r == 3 ? b * .35 : 0;
                rings[r][k] =
                        new double[] {
                            x + inset + outline[k][0] * (w - 2 * inset) / w,
                            y + inset + outline[k][1] * (h - 2 * inset) / h,
                            z + (r == 0 ? 0 : r == 1 ? lip : r == 2 ? d - lip : d)
                        };
            }
        for (int r = 0; r < 3; r++)
            for (int k = 0; k < 8; k++) {
                int n = (k + 1) % 8;
                faces.add(
                        new Face(
                                new double[][] {
                                    rings[r][k], rings[r][n], rings[r + 1][n], rings[r + 1][k]
                                },
                                color,
                                1));
            }
        for (int r : new int[] {0, 3})
            for (int k = 1; k < 7; k++) {
                double[] a = rings[r][0], b1 = rings[r][k], c = rings[r][k + 1];
                faces.add(
                        new Face(
                                r == 3
                                        ? new double[][] {a, b1, c, a}
                                        : new double[][] {a, c, b1, a},
                                color,
                                1));
            }
        for (int n = first; n < faces.size(); n++) {
            Face f = faces.get(n);
            f.localUv = true;
            for (int v = 0; v < 4; v++) {
                double[] p = f.p[v];
                f.uv[v][0] = Math.abs(f.nx) > .7 ? (p[2] - z) / d : (p[0] - x) / w;
                f.uv[v][1] = Math.abs(f.ny) > .7 ? (p[2] - z) / d : 1 - (p[1] - y) / h;
            }
        }
        return this;
    }

    public ExpeditionMesh orient(double angle, double scale) {
        List<Face> posed = new ArrayList<>();
        double c = Math.cos(angle), s = Math.sin(angle);
        for (Face face : faces) {
            double[][] p = new double[4][3];
            for (int i = 0; i < 4; i++) {
                double[] v = face.p[i];
                p[i] =
                        new double[] {
                            (v[0] * c - v[1] * s) * scale,
                            (v[0] * s + v[1] * c) * scale,
                            v[2] * scale
                        };
            }
            Face copy = new Face(p, face.color, face.alpha);
            copy.localUv = face.localUv;
            for (int i = 0; i < 4; i++) {
                copy.uv[i][0] = face.uv[i][0];
                copy.uv[i][1] = face.uv[i][1];
            }
            posed.add(copy);
        }
        faces.clear();
        faces.addAll(posed);
        return this;
    }

    public ExpeditionMesh plate(double[][] outline, double thickness, int color) {
        List<int[]> triangles = PolygonFaces.triangles(outline);
        double area = 0;
        for (int i = 0; i < outline.length; i++) {
            double[] a = outline[i], b = outline[(i + 1) % outline.length];
            area += a[0] * b[1] - b[0] * a[1];
        }
        for (int i = 0; i < outline.length; i++) {
            double[] a = outline[i], b = outline[(i + 1) % outline.length];
            if (area < 0) {
                double[] swap = a;
                a = b;
                b = swap;
            }
            faces.add(
                    new Face(
                            new double[][] {
                                {a[0], a[1], -thickness},
                                {b[0], b[1], -thickness},
                                {b[0], b[1], thickness},
                                {a[0], a[1], thickness}
                            },
                            color,
                            1));
        }
        for (int[] triangle : triangles)
            for (int side : new int[] {-1, 1}) {
                double[] a = outline[triangle[0]],
                        b = outline[triangle[side == 1 ? 1 : 2]],
                        c = outline[triangle[side == 1 ? 2 : 1]];
                double[] first = {a[0], a[1], side * thickness};
                faces.add(
                        new Face(
                                new double[][] {
                                    first,
                                    {b[0], b[1], side * thickness},
                                    {c[0], c[1], side * thickness},
                                    first
                                },
                                color,
                                1));
            }
        return this;
    }

    public ExpeditionMesh box(
            double x, double y, double z, double w, double h, double d, int color) {
        double[][] p = {
            {x, y, z},
            {x + w, y, z},
            {x + w, y + h, z},
            {x, y + h, z},
            {x, y, z + d},
            {x + w, y, z + d},
            {x + w, y + h, z + d},
            {x, y + h, z + d}
        };
        for (int[] f :
                new int[][] {
                    {0, 3, 2, 1},
                    {4, 5, 6, 7},
                    {0, 4, 7, 3},
                    {1, 2, 6, 5},
                    {0, 1, 5, 4},
                    {3, 7, 6, 2}
                })
            faces.add(new Face(new double[][] {p[f[0]], p[f[1]], p[f[2]], p[f[3]]}, color, 1));
        return this;
    }

    public ExpeditionMesh tube(
            double x,
            double y,
            double z,
            double tx,
            double ty,
            double tz,
            double r,
            double end,
            int color) {
        return tube(x, y, z, tx, ty, tz, r, end, color, 1);
    }

    public ExpeditionMesh tube(
            double x,
            double y,
            double z,
            double tx,
            double ty,
            double tz,
            double r,
            double end,
            int color,
            float alpha) {

        double dx = tx - x, dy = ty - y, dz = tz - z, len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1e-8) return this;
        int steps = Math.abs(r - end) < .05 ? 1 : 4;
        double[] forward = {dx / len, dy / len, dz / len};
        double[] side =
                Math.abs(forward[1]) > .9
                        ? new double[] {1, 0, 0}
                        : new double[] {-forward[2], 0, forward[0]};
        double sl = Math.sqrt(side[0] * side[0] + side[2] * side[2]);
        for (int j = 0; j < 3; j++) side[j] /= sl;
        double[] up = {
            forward[1] * side[2], forward[2] * side[0] - forward[0] * side[2], -forward[1] * side[0]
        };
        for (int step = 0; step < steps; step++) {
            double f = step / (double) steps,
                    t = (step + 1D) / steps,
                    width = Math.max(.08, r + (end - r) * (f + t) * .5);
            double[][] p = new double[8][3];
            for (int k = 0; k < 8; k++) {
                double along = k < 4 ? f : t;
                int corner = k % 4;
                double s = corner == 0 || corner == 3 ? -1 : 1, u = corner < 2 ? -1 : 1;
                p[k] =
                        new double[] {
                            x + dx * along + width * (side[0] * s + up[0] * u),
                            y + dy * along + width * (side[1] * s + up[1] * u),
                            z + dz * along + width * (side[2] * s + up[2] * u)
                        };
            }
            for (int[] face :
                    new int[][] {
                        {0, 3, 2, 1},
                        {4, 5, 6, 7},
                        {0, 4, 7, 3},
                        {1, 2, 6, 5},
                        {0, 1, 5, 4},
                        {3, 7, 6, 2}
                    })
                faces.add(
                        new Face(
                                new double[][] {p[face[0]], p[face[1]], p[face[2]], p[face[3]]},
                                color,
                                alpha));
        }
        return this;
    }

    public ExpeditionMesh spike(
            double x,
            double y,
            double z,
            double tx,
            double ty,
            double tz,
            double radius,
            int color) {
        int first = faces.size();
        double dx = tx - x, dy = ty - y, dz = tz - z, len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1e-8) return this;
        double[] f = {dx / len, dy / len, dz / len},
                s =
                        Math.abs(dy / len) > .9
                                ? new double[] {1, 0, 0}
                                : new double[] {-dz / len, 0, dx / len};
        double sl = Math.sqrt(s[0] * s[0] + s[2] * s[2]);
        s[0] /= sl;
        s[2] /= sl;
        double[] u = {f[1] * s[2], f[2] * s[0] - f[0] * s[2], -f[1] * s[0]};
        double[][] base = new double[4][3];
        for (int i = 0; i < 4; i++) {
            double a = i < 2 ? -1 : 1, b = i == 0 || i == 3 ? -1 : 1;
            base[i] =
                    new double[] {
                        x + radius * (s[0] * a + u[0] * b),
                        y + radius * (s[1] * a + u[1] * b),
                        z + radius * (s[2] * a + u[2] * b)
                    };
        }
        faces.add(new Face(new double[][] {base[0], base[1], base[2], base[3]}, color, 1));
        for (int i = 0; i < 4; i++)
            faces.add(
                    new Face(
                            new double[][] {
                                base[(i + 1) % 4],
                                base[i],
                                new double[] {tx, ty, tz},
                                base[(i + 1) % 4]
                            },
                            color,
                            1));
        for (int i = first; i < faces.size(); i++) {
            Face face = faces.get(i);
            face.localUv = true;
            double[][] uv =
                    i == first
                            ? new double[][] {{0, 0}, {1, 0}, {1, 1}, {0, 1}}
                            : new double[][] {{0, 1}, {1, 1}, {.5, 0}, {0, 1}};
            for (int v = 0; v < 4; v++) {
                face.uv[v][0] = uv[v][0];
                face.uv[v][1] = uv[v][1];
            }
        }
        return this;
    }

    public ExpeditionMesh star(double x, double y, double z, double radius, int color) {
        tube(x, y, z - .5, x, y, z + .5, 1.7, 1.7, color);
        for (int i = 0; i < 5; i++) {
            double a = i * Math.PI * 2 / 5;
            double mx = x + Math.sin(a) * radius * .55, my = y + Math.cos(a) * radius * .55;
            double ex = x + Math.sin(a + .13) * radius, ey = y + Math.cos(a + .13) * radius;
            tube(x, y, z, mx, my, z, 1.5, 1, color);
            tube(mx, my, z, ex, ey, z, .95, .1, color);
        }
        return this;
    }
}
