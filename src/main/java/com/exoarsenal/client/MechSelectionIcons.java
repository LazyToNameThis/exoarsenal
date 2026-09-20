package com.exoarsenal.client;

public final class MechSelectionIcons {
    public interface Canvas {
        void rect(int x1, int y1, int x2, int y2, int color);
    }

    private static void r(Canvas c, int x, int y, int w, int h, int color) {
        c.rect(x, y, x + w, y + h, color);
    }

    private static void ring(Canvas c, int x, int y, int radius, int dark, int light) {
        int bevel = Math.max(2, radius / 2), span = radius * 2 - bevel * 2;
        r(c, x - radius + bevel, y - radius, span, 2, light);
        r(c, x - radius + bevel, y + radius - 2, span, 2, dark);
        r(c, x - radius, y - radius + bevel, 2, span, light);
        r(c, x + radius - 2, y - radius + bevel, 2, span, dark);
        for (int step = 0; step < bevel; step++) {
            r(c, x - radius + step, y - radius + bevel - step - 1, 2, 2, light);
            r(c, x + radius - step - 2, y - radius + bevel - step - 1, 2, 2, light);
            r(c, x - radius + step, y + radius - bevel + step - 1, 2, 2, dark);
            r(c, x + radius - step - 2, y + radius - bevel + step - 1, 2, 2, dark);
        }
    }

    private static void eye(Canvas c, int x, int y, boolean seer, int dark, int metal, int light) {
        r(c, x - 8, y - 6, 16, 12, dark);
        r(c, x - 6, y - 8, 12, 16, dark);
        ring(c, x, y, 8, metal, light);
        r(c, x - 4, y - 4, 8, 8, metal);
        r(c, x - 1, y - 3, 2, 6, light);
        if (seer) {
            r(c, x - 18, y - 1, 10, 2, light);
            r(c, x - 20, y, 2, 1, metal);
            r(c, x - 3, y - 12, 2, 4, light);
            r(c, x + 2, y + 8, 2, 4, metal);
        } else {
            for (int dy : new int[] {-7, 0, 7}) {
                r(c, x + 8, y + dy - 1, 5, 2, light);
                r(c, x - 12, y + dy - 1, 4, 2, metal);
            }
            r(c, x - 1, y - 13, 2, 5, light);
        }
    }

    private static void fist(Canvas c, int x, int y, int dark, int metal, int light) {
        r(c, x - 6, y - 5, 12, 10, dark);
        r(c, x - 5, y - 4, 10, 7, metal);
        r(c, x - 4, y + 4, 8, 3, dark);
        for (int finger = 0; finger < 4; finger++) {
            r(c, x - 5 + finger * 3, y - 6, 2, 6, light);
            r(c, x - 5 + finger * 3, y, 2, 2, dark);
        }
        r(c, x - 7, y - 1, 3, 5, metal);
        r(c, x - 7, y - 1, 1, 3, light);
    }

    public static void draw(int kind, Canvas c) {
        final int shadow = kind == 0 ? 0xFF18260F : kind == 1 ? 0xFF301A12 : 0xFF0C2730;
        final int side = kind == 0 ? 0xFF426B25 : kind == 1 ? 0xFF8D4822 : 0xFF227481;

        for (int depth = 4; depth >= 1; depth--) {
            final int d = depth;
            portrait(
                    kind,
                    (x, y, right, bottom, color) ->
                            c.rect(x + d, y - d, right + d, bottom - d, d == 4 ? shadow : side));
        }
        portrait(kind, c);
        int highlight = kind == 0 ? 0xFFD8FA91 : kind == 1 ? 0xFFFFCC83 : 0xFFA1F7FF;
        if (kind == 0) {
            r(c, -9, -17, 17, 1, highlight);
            r(c, 12, -12, 1, 7, side);
            r(c, 15, -9, 1, 17, side);
            r(c, -7, -9, 8, 1, highlight);
            r(c, -8, -8, 1, 5, highlight);
        } else if (kind == 1) {
            r(c, -5, -11, 11, 1, highlight);
            r(c, 9, -6, 2, 13, side);
            for (int s : new int[] {-1, 1})
                for (int v : new int[] {-1, 1}) {
                    int x = s * 20, y = v * 13;
                    r(c, x - 4, y - 8, 10, 1, highlight);
                    r(c, x + 6, y - 4, 2, 9, side);
                    r(c, x - 3, y + 5, 7, 1, shadow);
                }
        } else {
            r(c, -17, -7, 9, 1, highlight);
            r(c, -3, -1, 2, 8, side);
            r(c, 9, -11, 10, 1, highlight);
            r(c, 22, -5, 2, 9, side);
        }
    }

    private static void portrait(int kind, Canvas c) {
        int dark = kind == 0 ? 0xFF243618 : kind == 1 ? 0xFF462919 : 0xFF123943;
        int metal = kind == 0 ? 0xFF70AA35 : kind == 1 ? 0xFFCB722C : 0xFF36A7B7;
        int light = kind == 0 ? 0xFFD8FA91 : kind == 1 ? 0xFFFFCC83 : 0xFFA1F7FF;
        if (kind == 0) {
            r(c, -15, -11, 30, 22, dark);
            r(c, -11, -15, 22, 30, dark);
            ring(c, 0, 0, 16, metal, light);
            ring(c, -1, -1, 12, dark, metal);
            ring(c, -2, -2, 8, metal, light);
            ring(c, -3, -3, 5, dark, light);
            r(c, -5, -5, 4, 4, light);
            r(c, -4, -4, 2, 2, metal);
            r(c, -11, -11, 4, 2, light);
            r(c, 9, -11, 2, 4, light);
            r(c, 7, 9, 4, 2, metal);
            r(c, -11, 7, 2, 4, metal);
            r(c, -3, -20, 6, 4, metal);
            r(c, -20, -3, 4, 6, metal);
            r(c, 16, -3, 4, 6, metal);
            r(c, -3, 16, 6, 4, metal);
        } else if (kind == 2) {
            for (int n = 0; n < 6; n++) {
                int x = -6 + n * 3, y = -6 + (int) Math.round(Math.sin(n * .65) * -3);
                r(c, x, y, 5, 3, metal);
                r(c, x + 1, y + 1, 3, 1, dark);
            }
            eye(c, -12, 3, true, dark, metal, light);
            eye(c, 14, -1, false, dark, metal, light);
        } else {
            for (int s : new int[] {-1, 1})
                for (int v : new int[] {-1, 1}) {
                    for (int n = 0; n < 4; n++)
                        r(
                                c,
                                s * (7 + n * 2) - 1,
                                v * (4 + n * 2),
                                3,
                                2,
                                n % 2 == 0 ? light : metal);
                    fist(c, s * 20, v * 13, dark, metal, light);
                }
            r(c, -8, -8, 16, 16, dark);
            ring(c, 0, 0, 9, metal, light);
            r(c, -4, -5, 8, 10, metal);
            r(c, -1, -3, 2, 6, light);
            r(c, -4, -14, 2, 5, metal);
            r(c, 3, -16, 2, 7, light);
            r(c, -2, 9, 4, 5, metal);
        }
    }

    private MechSelectionIcons() {}
}
