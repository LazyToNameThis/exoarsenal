package com.scapeandrun.frostbite.client;

public final class OrnateBossBarLayout {
    public static final int WIDTH = 420, HEIGHT = 68;
    private static final int INK = 0xFF10151F,
            SHADOW = 0xFF242B3A,
            STEEL = 0xFF465264,
            LIGHT = 0xFF8495A5,
            EDGE = 0xFFB7C6D0;

    private OrnateBossBarLayout() {}

    private static void r(ScoutBossBarLayout.Canvas c, int x, int y, int w, int h, int color) {
        if (w > 0 && h > 0) c.rect(x, y, x + w, y + h, color);
    }

    private static void p(ScoutBossBarLayout.Canvas c, int color, int... xy) {
        int top = 1000, bottom = -1000;
        for (int i = 1; i < xy.length; i += 2) {
            top = Math.min(top, xy[i]);
            bottom = Math.max(bottom, xy[i]);
        }
        for (int y = top; y < bottom; y++) {
            double left = 10000, right = -10000;
            for (int i = 0, j = xy.length - 2; i < xy.length; j = i, i += 2) {
                double ay = xy[i + 1], by = xy[j + 1];
                if ((ay <= y + .5 && by > y + .5) || (by <= y + .5 && ay > y + .5)) {
                    double x = xy[i] + (y + .5 - ay) * (xy[j] - xy[i]) / (by - ay);
                    left = Math.min(left, x);
                    right = Math.max(right, x);
                }
            }
            if (right > left)
                c.rect((int) Math.ceil(left), y, (int) Math.ceil(right), y + 1, color);
        }
    }

    private static void plate(ScoutBossBarLayout.Canvas c, int x, int y, int w, int h) {
        p(
                c, INK, x + 3, y, x + w - 3, y, x + w, y + 3, x + w, y + h - 3, x + w - 3, y + h,
                x + 3, y + h, x, y + h - 3, x, y + 3);
        r(c, x + 3, y + 1, w - 6, 1, LIGHT);
        r(c, x + 2, y + 2, w - 4, 2, STEEL);
        r(c, x + 2, y + 4, w - 4, h - 7, SHADOW);
        r(c, x + 3, y + h - 3, w - 6, 1, STEEL);
        r(c, x + 2, y + 4, 1, h - 8, LIGHT);
        r(c, x + w - 3, y + 4, 1, h - 8, 0xFF31394A);
    }

    private static void rivet(ScoutBossBarLayout.Canvas c, int x, int y) {
        r(c, x, y, 3, 3, INK);
        r(c, x, y, 2, 1, EDGE);
        r(c, x + 1, y + 1, 1, 1, STEEL);
    }

    private static void crystal(ScoutBossBarLayout.Canvas c, int x, int y, int length, boolean up) {
        int s = up ? -1 : 1;
        p(c, 0xFF26394F, x - 5, y, x + 4, y, x + 2, y + s * (length - 4), x - 1, y + s * length);
        p(c, 0xFF397CA7, x - 3, y, x + 2, y, x + 1, y + s * (length - 3), x - 1, y + s * length);
        p(c, 0xFF9CE9F3, x - 2, y, x, y, x - 1, y + s * (length - 2));
        r(c, x - 5, y - 1, 10, 3, SHADOW);
        r(c, x - 4, y, 8, 1, LIGHT);
    }

    private static void chain(ScoutBossBarLayout.Canvas c, int x, int y, boolean green) {
        plate(c, x, y, 9, 6);
        r(c, x + 3, y + 2, 4, 2, INK);
        r(c, x + 6, y + 2, 4, 2, STEEL);
        if (green) r(c, x + 1, y + 2, 1, 2, 0xFF71D995);
    }

    private static void trough(ScoutBossBarLayout.Canvas c, int x, int y, int w) {
        plate(c, x - 5, y - 6, w + 10, 24);
        r(c, x - 1, y - 1, w + 2, 13, 0xFF050B13);
        r(c, x, y, w, 11, 0xFF172535);
        r(c, x, y + 10, w, 1, 0xFF314658);
    }

    private static void fill(
            ScoutBossBarLayout.Canvas c,
            int x,
            int y,
            int w,
            float hp,
            float trail,
            boolean green,
            float tick) {
        int n = Math.round(w * clamp(hp)), lag = Math.round(w * clamp(trail));
        r(c, x, y, lag, 10, green ? 0xFF627858 : 0xFF727795);
        r(c, x, y, n, 1, green ? 0xFFD6FFD1 : 0xFFD7FAFF);
        r(c, x, y + 1, n, 2, green ? 0xFF8BE3A0 : 0xFF8DDBF3);
        r(c, x, y + 3, n, 4, green ? 0xFF42AA75 : 0xFF459DD4);
        r(c, x, y + 7, n, 2, green ? 0xFF28734E : 0xFF2A609A);
        r(c, x, y + 9, n, 1, green ? 0xFF174D38 : 0xFF243F71);
        for (int i = 0; i < 7; i++) {
            int q = Math.floorMod((int) (tick * .35) + i * 37, w + 18) - 9;
            for (int row = 1; row < 9; row++) {
                int xx = q + row / 2;
                if (xx >= 0 && xx < n) r(c, x + xx, y + row, 1, 1, green ? 0x667CFFC0 : 0x668BE8FF);
            }
        }
        if (n > 0) {
            r(c, x + n - 1, y, 1, 10, green ? 0xFFD7FFE1 : 0xFFE9FFFF);
            if (n > 2) r(c, x + n - 3, y + 2, 2, 5, green ? 0xFF84E9AE : 0xFF9BEDFF);
        }
    }

    public static float clamp(float f) {
        return Math.max(0, Math.min(1, Float.isFinite(f) ? f : 0));
    }

    public static void brawler(
            ScoutBossBarLayout.Canvas c,
            float health,
            float trail,
            float tick,
            float[] arms,
            int phase,
            int returned) {
        final int olive = 0xFF667460, ivory = 0xFFBFCBA9, brass = 0xFFB5A16B, glow = 0xFF78F0AC;
        plate(c, 113, 1, 194, 23);
        r(c, 120, 5, 180, 15, 0xFF142019);
        c.heading("X-05 \"BRAWLER\"", 210, 7, 0xDCE9C8, 1.2F);
        trough(c, 54, 29, 312);
        fill(c, 55, 29, 310, health, trail, true, tick);

        for (int side = 0; side < 2; side++)
            for (int j = 0; j < 8; j++)
                chain(c, side == 0 ? 42 + j * 9 : 298 + j * 9, 45 + (j % 3), true);
        for (int i = 0; i < 4; i++) {
            int x = 57 + i * 80;
            plate(c, x, 47, 66, 14);
            r(c, x + 4, 51, 58, 5, 0xFF0A1510);
            int amount = Math.round(56 * clamp(arms[i]));
            r(c, x + 5, 51, amount, 1, ivory);
            r(c, x + 5, 52, amount, 3, 0xFF49956A);
            r(c, x + 5, 55, amount, 1, 0xFF28533C);
            rivet(c, x + 1, 48);
            rivet(c, x + 61, 48);

            int mark = x + 29;
            if (i == 0) {
                r(c, mark, 58, 9, 5, olive);
                for (int k = 0; k < 4; k++) r(c, mark + k * 2, 58, 1, 3, ivory);
            } else if (i == 1) {
                p(
                        c, ivory, mark, 57, mark + 9, 58, mark + 12, 62, mark + 8, 66, mark + 6, 63,
                        mark + 9, 61);
            } else if (i == 2) {
                for (int k = 0; k < 3; k++)
                    p(
                            c,
                            ivory,
                            mark + k * 4,
                            65,
                            mark + k * 4,
                            59,
                            mark + k * 4 + 2,
                            56,
                            mark + k * 4 + 3,
                            60,
                            mark + k * 4 + 2,
                            65);
            } else {
                plate(c, mark, 57, 11, 9);
                r(c, mark + 3, 59, 5, 4, glow);
                r(c, mark + 4, 60, 3, 2, 0xFF162A20);
            }
            if (arms[i] <= 0) {
                r(c, x + 7, 53, 52, 1, 0xFF9B6C4B);
                for (int n = 0; n < 4; n++) r(c, x + 12 + n * 13, 51 + n % 2, 3, 4, 0xFF25372C);
            }
        }

        for (int side = 0; side < 2; side++) {
            int x = side == 0 ? 28 : 392;
            p(
                    c,
                    0xFF111C18,
                    x - 17,
                    22,
                    x - 9,
                    15,
                    x + 9,
                    15,
                    x + 17,
                    23,
                    x + 17,
                    42,
                    x + 8,
                    49,
                    x - 8,
                    49,
                    x - 17,
                    41);
            p(
                    c, olive, x - 14, 23, x - 7, 18, x + 7, 18, x + 14, 24, x + 14, 39, x + 6, 46,
                    x - 6, 46, x - 14, 39);
            plate(c, x - 10, 22, 20, 22);
            r(c, x - 6, 26, 12, 14, 0xFF18392A);
            r(c, x - 3, 27, 6, 12, glow);
            r(c, x - 1, 28, 2, 9, 0xFFDFFFE8);
            r(c, x - 12, 22, 2, 18, ivory);
            r(c, x + 10, 26, 2, 15, 0xFF354535);
            for (int y : new int[] {20, 42}) {
                r(c, x - 6, y, 12, 2, brass);
                rivet(c, x - 13, y);
                rivet(c, x + 10, y);
            }
            p(c, olive, x - 5, 15, x - 3, 6, x + 3, 6, x + 5, 15);
            r(c, x - 2, 7, 2, 6, ivory);
        }
        for (int x : new int[] {65, 98, 315, 348}) {
            r(c, x, 23, 11, 2, olive);
            r(c, x + 2, 22, 7, 1, ivory);
        }
        if (phase == 3 && returned < 7) {
            for (int n = 0; n < 7; n++) {
                int x = 174 + n * 11;
                plate(c, x, 18, 9, 7);
                r(c, x + 2, 20, 5, 3, n < returned ? 0xFF253C30 : glow);
            }
        }
    }

    public static void excavator(
            ScoutBossBarLayout.Canvas c, float health, float trail, float tick) {
        plate(c, 126, 1, 168, 22);
        c.heading("X-04 \"EXCAVATOR\"", 210, 6, 0xD2E6B9, 1.15F);
        trough(c, 57, 29, 306);
        fill(c, 58, 29, 304, health, trail, true, tick);
        for (int i = 0; i < 12; i++) {
            int x = 62 + i * 25;
            plate(c, x, 44, 22, 9);
            r(c, x + 4, 46, 14, 2, 0xFF61715A);
            r(c, x + 8, 49, 6, 2, 0xFF52DD88);
            rivet(c, x + 1, 44);
        }
        for (int side = 0; side < 2; side++) {
            int x = side == 0 ? 32 : 388;
            plate(c, x - 12, 22, 24, 27);
            plate(c, x - 7, 17, 14, 37);
            r(c, x - 3, 23, 6, 26, 0xFF2BAA60);
            r(c, x - 2, 24, 2, 24, 0xFFA0FFC4);
            for (int i = 0; i < 4; i++) {
                int dx = side == 0 ? -1 : 1, w = 10 - i * 2, px = x + dx * (14 + i * 5);
                plate(c, px - w / 2, 26 + i * 2, w, 20 - i * 4);
                r(c, px - w / 2 + 1, 27 + i * 2, Math.max(1, w - 2), 2, 0xFFBCC5A4);
            }
            r(c, x - 8, 20, 3, 29, 0xFF8D9A7A);
            rivet(c, x + 6, 23);
            rivet(c, x + 6, 45);
        }
        for (int x : new int[] {72, 112, 306, 346}) {
            r(c, x, 21, 18, 3, 0xFF22342A);
            r(c, x + 3, 20, 12, 1, 0xFFBCC5A4);
        }
    }

    private static void title(
            ScoutBossBarLayout.Canvas c, String name, int left, int width, int color) {
        plate(c, left, 2, width, 16);
        r(c, left + 6, 6, width - 12, 8, 0xFF101A25);
        c.label(name, left + width / 2, 6, color);
    }

    public static void scout(
            ScoutBossBarLayout.Canvas c,
            float health,
            float trail,
            float tick,
            boolean shield,
            float overheat) {
        title(c, "TUNDRA TREKKER", 143, 134, 0xC8E8FF);
        p(c, STEEL, 135, 16, 123, 6, 132, 7, 143, 16);
        p(c, LIGHT, 136, 15, 128, 9, 134, 11, 141, 16);
        p(c, STEEL, 285, 16, 297, 6, 288, 7, 277, 16);
        p(c, LIGHT, 284, 15, 292, 9, 286, 11, 279, 16);
        for (int side = 0; side < 2; side++) {
            int cx = side == 0 ? 34 : 386;
            for (int k = 0; k < 3; k++) chain(c, side == 0 ? 3 + k * 9 : 390 + k * 9, 29, false);
            crystal(c, cx, 23, 19, true);
            crystal(c, cx - 5, 39, 22, false);
            plate(c, cx - 13, 23, 26, 18);
            plate(c, cx - 7, 17, 14, 30);
            r(c, cx - 4, 21, 3, 20, LIGHT);
            r(c, cx + 1, 21, 3, 20, STEEL);
            rivet(c, cx - 11, 29);
            rivet(c, cx + 8, 29);
            p(c, STEEL, cx - 10, 20, cx - 17, 24, cx - 18, 29, cx - 10, 27);
            p(c, LIGHT, cx - 11, 21, cx - 16, 24, cx - 11, 24);
            p(c, STEEL, cx + 10, 37, cx + 17, 35, cx + 14, 42, cx + 8, 43);
            r(c, cx - 4, 24, 2, 5, EDGE);
            r(c, cx + 1, 33, 2, 6, 0xFF6D7A90);
            r(c, cx - 12, 32, 4, 2, 0xFF68C9EB);
            r(c, cx + 8, 25, 4, 2, 0xFF68C9EB);
            p(c, INK, cx - 13, 20, cx - 9, 11, cx - 5, 15, cx - 4, 22);
            p(c, STEEL, cx - 12, 20, cx - 9, 13, cx - 7, 17, cx - 6, 22);
            p(c, 0xFFB8E9EE, cx + 2, 19, cx + 7, 6, cx + 10, 14, cx + 9, 21);
            p(c, 0xFF4188AF, cx + 4, 19, cx + 7, 10, cx + 8, 18);
            p(c, INK, cx + 7, 43, cx + 15, 40, cx + 12, 48, cx + 6, 51);
            p(c, STEEL, cx + 8, 43, cx + 13, 42, cx + 11, 46, cx + 8, 48);
            r(c, cx - 7, 19, 1, 26, 0xFFD3DBDC);
            r(c, cx + 6, 19, 1, 26, 0xFF26313F);
        }
        trough(c, 53, 27, 314);
        fill(c, 54, 27, 312, health, trail, false, tick);
        for (int x : new int[] {76, 111, 153, 265, 305, 344})
            crystal(c, x, 43, x == 111 || x == 305 ? 13 : 8, false);
        for (int x : new int[] {59, 132, 173, 233, 280, 321}) {
            r(c, x, 21, 18, 1, 0xFF788597);
            r(c, x + 4, 22, 11, 1, 0xFF566377);
            r(c, x + 2, 42, 20, 1, 0xFF394358);
            rivet(c, x + 19, 21);
        }

        p(c, 0xFF637083, 83, 20, 105, 18, 117, 20, 114, 22, 89, 22);
        r(c, 88, 19, 15, 1, 0xFFAAB7C6);
        p(c, 0xFF44536B, 306, 20, 318, 17, 339, 20, 335, 22, 310, 22);
        r(c, 316, 19, 14, 1, 0xFF92A8BD);
        p(c, 0xFF354256, 157, 41, 180, 41, 187, 44, 165, 46, 155, 44);
        r(c, 165, 42, 14, 1, 0xFF768496);
        p(c, 0xFF354256, 232, 41, 259, 41, 264, 44, 240, 46, 233, 44);
        r(c, 239, 42, 17, 1, 0xFF768496);
        for (int x : new int[] {49, 365}) {
            plate(c, x, 21, 6, 22);
            rivet(c, x + 1, 24);
            rivet(c, x + 1, 36);
        }
        p(c, INK, 210, 38, 221, 49, 210, 65, 199, 49);
        p(c, LIGHT, 210, 40, 218, 49, 210, 61, 202, 49);
        p(c, 0xFF344263, 210, 43, 215, 49, 210, 58, 205, 49);
        r(c, 209, 45, 2, 10, 0xFFB6E4F5);
        r(c, 206, 49, 8, 2, 0xFFB6E4F5);
        r(c, 208, 55, 4, 1, 0xFF71B7DA);
        if (overheat >= 0) {
            fill(c, 54, 27, 312, overheat, overheat, false, tick * 3);
            r(c, 54, 27, Math.round(312 * clamp(overheat)), 10, 0xBCEF572B);
            c.label("OVERHEAT  " + (int) Math.ceil(overheat * 20) + "s", 210, 28, 0xFFF4D3);
        }
    }

    public static void selectionEye(ScoutBossBarLayout.Canvas c, int cx, boolean observer) {
        eye(c, cx, observer, 0, false);
    }

    private static void eye(
            ScoutBossBarLayout.Canvas c, int cx, boolean observer, float tick, boolean dead) {
        int cy = 32, green = dead ? 0xFF4A5854 : 0xFF73F7A4;

        if (observer) {
            plate(c, cx - 6, 4, 12, 13);
            plate(c, cx - 6, 48, 12, 13);
            plate(c, cx + 15, 26, 15, 12);
            p(c, STEEL, cx + 9, 17, cx + 18, 9, cx + 23, 13, cx + 16, 23);
            p(c, LIGHT, cx + 10, 17, cx + 18, 11, cx + 20, 13, cx + 14, 20);
            p(c, STEEL, cx + 10, 46, cx + 18, 54, cx + 23, 50, cx + 16, 40);
            r(c, cx - 2, 6, 4, 5, green);
            r(c, cx - 2, 54, 4, 5, green);
            r(c, cx + 24, 30, 7, 3, green);
        } else {
            p(c, STEEL, cx - 10, 17, cx - 18, 7, cx - 17, 20, cx - 13, 24);
            p(c, LIGHT, cx - 15, 15, cx - 15, 9, cx - 12, 17);
            p(c, STEEL, cx - 10, 45, cx - 18, 57, cx - 17, 43, cx - 13, 40);
            p(c, 0xFF255848, cx - 12, 34, cx - 30, 50, cx - 27, 41, cx - 15, 28);
            p(c, green, cx - 13, 33, cx - 29, 48, cx - 24, 40, cx - 14, 29);
            p(c, 0xFFD7FFE8, cx - 14, 32, cx - 27, 45, cx - 22, 38);
        }
        p(
                c, INK, cx - 9, 13, cx + 9, 13, cx + 18, 23, cx + 18, 41, cx + 9, 51, cx - 9, 51,
                cx - 18, 41, cx - 18, 23);
        p(
                c, STEEL, cx - 8, 15, cx + 8, 15, cx + 16, 24, cx + 16, 40, cx + 8, 49, cx - 8, 49,
                cx - 16, 40, cx - 16, 24);
        p(
                c, LIGHT, cx - 8, 16, cx + 7, 16, cx + 12, 22, cx + 8, 24, cx - 8, 22, cx - 13, 28,
                cx - 15, 25);
        p(c, SHADOW, cx - 8, 47, cx + 8, 47, cx + 14, 40, cx + 11, 38, cx + 5, 43, cx - 8, 43);

        p(c, 0xFFB0B7AC, cx - 9, 16, cx - 2, 16, cx - 3, 20, cx - 10, 24, cx - 14, 24);
        p(c, 0xFFDBDCD0, cx - 8, 17, cx - 3, 17, cx - 4, 19, cx - 10, 22);
        p(c, 0xFF879488, cx + 3, 16, cx + 8, 16, cx + 14, 23, cx + 10, 25, cx + 6, 20);
        r(c, cx + 5, 17, 3, 2, 0xFFC6CBBE);
        p(c, 0xFF89978E, cx - 16, 28, cx - 12, 26, cx - 12, 38, cx - 16, 37);
        r(c, cx - 15, 28, 2, 7, 0xFFCBD1C5);
        p(c, 0xFF697C73, cx + 12, 27, cx + 16, 27, cx + 16, 37, cx + 12, 40);
        r(c, cx + 13, 28, 2, 5, 0xFF9FAE9F);
        p(c, 0xFF778679, cx - 10, 42, cx - 3, 45, cx - 3, 48, cx - 8, 48, cx - 13, 42);
        r(c, cx - 9, 43, 4, 1, 0xFFB5C3AE);
        plate(c, cx - 12, 22, 24, 21);
        r(c, cx - 8, 25, 16, 14, 0xFF0B211D);
        r(c, cx - 6, 26, 12, 12, dead ? 0xFF293832 : 0xFF1E6041);
        r(c, cx - 4, 28, 8, 8, dead ? 0xFF3E4D46 : 0xFF39B16A);
        r(c, cx - 2, 27, 4, 10, green);
        r(c, cx - 1, 28, 2, 8, dead ? 0xFF77807A : 0xFFE0FFCD);
        r(c, cx - 5, 28, 2, 2, 0xFFADC7BA);
        plate(c, cx - 5, 12, 10, 7);
        r(c, cx - 2, 14, 4, 2, green);
        plate(c, cx - 5, 45, 10, 6);
        rivet(c, cx - 13, 22);
        rivet(c, cx + 10, 22);
        rivet(c, cx - 13, 40);
        rivet(c, cx + 10, 40);
        r(c, cx - 9, 19, 3, 1, SHADOW);
        r(c, cx + 7, 44, 3, 1, LIGHT);
        r(c, cx - 17, 30, 2, 4, 0xFF42534C);
        r(c, cx + 16, 32, 2, 5, 0xFF394E47);
        plate(c, cx - 21, 26, 6, 12);
        r(c, cx - 20, 29, 2, 5, 0xFFACB8A7);
        r(c, cx - 18, 31, 2, 2, green);
        plate(c, cx + 15, 26, 6, 12);
        r(c, cx + 17, 29, 2, 5, 0xFFACB8A7);
        r(c, cx + 15, 31, 2, 2, green);
        r(c, cx - 6, 21, 2, 2, 0xFFCED4BB);
        r(c, cx + 7, 24, 1, 3, 0xFF333D35);
        r(c, cx - 9, 35, 1, 3, 0xFF809B86);
        r(c, cx + 4, 41, 3, 2, 0xFFA1AE92);
        r(c, cx + 3, 7, 2, 6, STEEL);
        r(c, cx + 3, 5, 2, 2, green);
    }

    public static void eyes(
            ScoutBossBarLayout.Canvas c,
            float seer,
            float observer,
            float seerTrail,
            float observerTrail,
            float tick) {
        for (int side = 0; side < 2; side++)
            for (int k = 0; k < 8; k++) {
                int x = side == 0 ? 42 + k * 8 : 314 + k * 8,
                        y = 19 - (int) (Math.sin(k * .45) * 9);
                chain(c, x, y, true);
                chain(c, x, 43 + (int) (Math.sin(k * .45) * 9), true);
            }
        plate(c, 62, 1, 132, 22);
        plate(c, 226, 1, 132, 22);
        r(c, 68, 5, 120, 13, 0xFF101A25);
        r(c, 232, 5, 120, 13, 0xFF101A25);
        c.heading("THE SEER", 128, 6, 0xBCF3CD, 1.25F);
        c.heading("THE OBSERVER", 292, 6, 0xBCF3CD, 1.25F);
        trough(c, 56, 28, 144);
        trough(c, 220, 28, 144);
        fill(c, 56, 28, 144, seer, seerTrail, true, tick);
        fill(c, 220, 28, 144, observer, observerTrail, true, -tick);
        for (int x : new int[] {58, 191, 222, 355}) {
            plate(c, x, 22, 7, 23);
            r(c, x + 2, 27, 2, 12, 0xFF72877C);
            rivet(c, x + 2, 23);
            rivet(c, x + 2, 40);
        }
        for (int x : new int[] {77, 117, 157, 241, 281, 321}) {
            r(c, x, 22, 16, 1, 0xFF8D9B8C);
            r(c, x + 3, 23, 10, 1, 0xFF4D6055);
            r(c, x, 43, 13, 1, 0xFF3F5149);
            r(c, x + 4, 44, 4, 2, 0xFF162B23);
            r(c, x + 5, 44, 2, 1, 0xFF71D9A0);
        }
        plate(c, 198, 19, 24, 28);
        p(c, 0xFF557267, 210, 20, 219, 32, 210, 46, 201, 32);
        p(c, 0xFF182E28, 210, 24, 216, 32, 210, 42, 204, 32);
        r(c, 208, 30, 4, 7, 0xFF71F5A3);
        r(c, 205, 32, 10, 2, 0xFFD0FFE1);
        r(c, 209, 28, 2, 11, 0xFFD0FFE1);
        r(c, 207, 50, 6, 2, 0xFF537968);
        r(c, 209, 48, 2, 6, 0xFF9BCFB6);
        eye(c, 32, false, tick, seer <= 0);
        eye(c, 388, true, tick, observer <= 0);
        if (seer > 0) c.label(Math.round(clamp(seer) * 100) + "%", 128, 47, 0x9AD8B4);
        if (observer > 0) c.label(Math.round(clamp(observer) * 100) + "%", 292, 47, 0x9AD8B4);
    }
}
