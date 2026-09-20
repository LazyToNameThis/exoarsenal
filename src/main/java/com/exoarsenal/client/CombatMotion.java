package com.exoarsenal.client;

public final class CombatMotion {
    private CombatMotion() {}

    public static float smooth(float t) {
        t = Math.max(0.0F, Math.min(1.0F, t));
        return t * t * (3.0F - 2.0F * t);
    }

    public static float recoil(float t) {
        if (t <= 0.0F || t >= 1.0F) return 0.0F;
        if (t < 0.12F) return smooth(t / 0.12F);
        return 1.0F - smooth((t - 0.12F) / 0.88F);
    }

    public static float strike(float t) {
        if (t < 0.28F) return 0.16F * smooth(t / 0.28F);
        if (t < 0.46F) return 0.16F + 0.60F * smooth((t - 0.28F) / 0.18F);
        return 0.76F + 0.24F * smooth((t - 0.46F) / 0.54F);
    }
}
