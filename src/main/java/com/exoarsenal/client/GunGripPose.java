package com.exoarsenal.client;

public final class GunGripPose {
    private GunGripPose() {}

    private static final float[][] GRIPS = {
        {5, -1.4F},
        {6.25F, -1.25F},
        {4.25F, -1.5F},
        {5.5F, -1.25F},
        {4.75F, -1.5F},
        {5.5F, -1.4F},
        {5.5F, -1.4F},
        {5, -1.45F}
    };

    public static float[] socket(int type, boolean right, boolean support) {
        if (type < 0)
            return new float[] {
                (right ? 1 : -1) / 16F, support ? 0.12F : 0.01F, support ? -0.50F : -0.08F
            };
        int frame = type == 8 ? 4 : type == 9 ? 7 : type == 10 ? 6 : type == 11 ? 5 : type;
        float scale = frame == 2 ? 0.78F : 0.68F;
        float x = support ? (frame == 1 || frame == 5 ? 3 : 2) : GRIPS[frame][0];
        float y = support ? (frame == 1 || frame == 5 ? 10 : 8) : GRIPS[frame][1];

        return new float[] {
            (right ? 1 : -1) / 16F, 2 / 16F - x * scale / 16F, 1 / 16F - y * scale / 16F
        };
    }
}
