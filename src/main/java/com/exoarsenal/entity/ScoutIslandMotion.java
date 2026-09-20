package com.exoarsenal.entity;

public final class ScoutIslandMotion {
    private ScoutIslandMotion() {}

    private static double smooth(double t) {
        t = Math.max(0, Math.min(1, t));
        return t * t * (3 - 2 * t);
    }

    public static double yaw(int style, int island, int selected, double tick) {
        if (tick < 0 || tick > 200 || style == 0) return 0;
        double influence = island == selected ? 1 : .2, sign = (island % 2 == 0 ? 1 : -1);
        double amplitude = style == 2 ? .22 : style == 4 ? .16 : style == 3 ? .12 : .08;
        return sign
                * influence
                * amplitude
                * Math.sin(tick * .045)
                * smooth(tick / 14)
                * (1 - smooth((tick - 90) / 90));
    }

    public static double[] offset(int style, int island, int selected, double tick) {
        double x = 0, y = 0, z = 0;
        if (tick < 0) return new double[] {0, 0, 0};
        double nearby = island == selected ? 1 : .18;
        if (style == 1) {
            y = -4 * nearby * smooth(tick / 12) * (1 - smooth((tick - 32) / 55));
        } else if (style == 2) {
            double displacement = 3.2 * smooth(tick / 22) * (1 - smooth((tick - 42) / 65));
            double angle = selected * Math.PI * .25;
            x = Math.cos(angle) * displacement * nearby;
            z = Math.sin(angle) * displacement * nearby;
        } else if (style == 3) {

            if (island == selected) {
                if (tick < 55) y = -9 * smooth(tick / 55);
                else if (tick < 85) y = -9 + 17 * smooth((tick - 55) / 30);
                else y = 8 * (1 - smooth((tick - 100) / 65));
            } else y = -.7 * Math.sin(Math.min(Math.PI, tick * Math.PI / 100));
        } else if (style == 4) {
            double pull = 2.2 * smooth(tick / 30) * (1 - smooth((tick - 55) / 45));
            int opposite = 8 - selected;
            if (island == selected || island == opposite) {
                x = -ScoutArenaLayout.x(island) / 8 * pull;
                z = -ScoutArenaLayout.z(island) / 8 * pull;
            }
        } else if (style == 5 && island == selected) {

            y = 7 * smooth(tick / 12) * (1 - smooth((tick - 18) / 18));
        }
        return new double[] {x, y, z};
    }
}
