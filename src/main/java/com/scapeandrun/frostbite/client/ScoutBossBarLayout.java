package com.scapeandrun.frostbite.client;

public final class ScoutBossBarLayout {
    public interface Canvas {
        void rect(int x1, int y1, int x2, int y2, int color);

        void label(String text, int center, int y, int color);

        default void heading(String text, int center, int y, int color, float scale) {
            label(text, center, y, color);
        }
    }

    private ScoutBossBarLayout() {}

    public static void draw(Canvas canvas, int x, int y, int width, float health) {
        int gap = 5, cell = (width - 2 * gap) / 3;
        for (int phase = 0; phase < 3; phase++) {
            int left = x + phase * (cell + gap), right = left + cell;
            float fill = Math.max(0, Math.min(1, health * 3 - (2 - phase)));
            canvas.rect(left + 2, y, right - 2, y + 18, 0xEE07101A);
            canvas.rect(left, y + 3, right, y + 15, 0xFF263D4D);
            canvas.rect(left + 2, y + 1, right - 2, y + 3, 0xFF729DAF);
            canvas.rect(left + 2, y + 15, right - 2, y + 17, 0xFF14232E);
            canvas.rect(left + 3, y + 4, right - 3, y + 14, 0xFF091925);
            int edge = left + 3 + Math.round((cell - 6) * fill);
            if (edge > left + 3) {
                canvas.rect(left + 3, y + 4, edge, y + 6, 0xFFD0F9FF);
                canvas.rect(left + 3, y + 6, edge, y + 9, phase == 2 ? 0xFF8B8FEC : 0xFF55C7E2);
                canvas.rect(left + 3, y + 9, edge, y + 13, phase == 2 ? 0xFF4F579E : 0xFF246DA6);
                canvas.rect(edge - 1, y + 4, edge, y + 13, 0xFFF0FFFF);
            }
            canvas.rect(left, y + 7, left + 2, y + 11, 0xFFB1D6E4);
            canvas.rect(right - 2, y + 7, right, y + 11, 0xFFB1D6E4);
            String numeral = new String[] {"I", "II", "III"}[phase];
            canvas.label(numeral, left + cell / 2, y + 21, fill > 0 ? 0xADDBE8 : 0x415362);
        }
    }
}
