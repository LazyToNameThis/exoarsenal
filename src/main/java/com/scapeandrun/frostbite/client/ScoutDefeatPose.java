package com.scapeandrun.frostbite.client;

import software.bernie.geckolib3.core.processor.IBone;
import java.util.function.Function;

public final class ScoutDefeatPose {
    public static final int ESCAPE_START = 620, END = 700;
    private final Function<String, IBone> bones;

    private ScoutDefeatPose(Function<String, IBone> bones) {
        this.bones = bones;
    }

    private IBone getBone(String name) {
        return bones.apply(name);
    }

    private void visible(String name, boolean shown) {
        getBone(name).setHidden(!shown);
    }

    public static void apply(float tick, Function<String, IBone> bones) {
        new ScoutDefeatPose(bones).animateDefeat(tick);
    }

    private static float deathCurve(float t, float... keys) {
        return com.scapeandrun.frostbite.client.ScoutBrawlPose.curve(t, keys);
    }

    private void animateDefeat(float t) {
        float fall =
                -deathCurve(t, 0, 0, 118, 0, 134, .16F, 156, 1.5708F, 162, 1.52F, 172, 1.5708F);
        getBone("root").setRotationX(fall);
        for (String n : new String[] {"pelvis", "chassis", "sensorArray"}) {
            getBone(n).setRotationX(0);
            getBone(n).setRotationY(0);
            getBone(n).setRotationZ(0);
        }
        visible("cockpitGlass", t < 40);
        visible("cockpitShards", false);
        visible("cockpitCrackIce", t >= 12 && t < 40);
        float crack = deathCurve(t, 0, .01F, 12, .15F, 27, .65F, 38, 1);
        getBone("cockpitCrackIce").setScaleX(crack);
        getBone("cockpitCrackIce").setScaleY(crack);
        for (String n : new String[] {"leftWingIce", "rightWingIce"}) {
            float power = deathCurve(t, 0, 1, 35, 1, 62, .001F);
            getBone(n).setScaleX(power);
            getBone(n).setScaleY(power);
            visible(n, t < 62);
        }
        for (String n : new String[] {"leftSwordArm", "rightSwordArm"}) {
            float y = 50 - deathCurve(t, 0, 0, 70, 0, 98, 39, 104, 38, 110, 39);

            getBone(n).setPositionY((float) (y * Math.cos(fall) + 8 * Math.sin(fall)) - 44);
            getBone(n).setPositionZ((float) (-y * Math.sin(fall) + 8 * Math.cos(fall)) - 8);
            getBone(n).setRotationX(deathCurve(t, 0, 0, 70, 0, 98, 1.35F) - fall);
            getBone(n).setRotationY(0);
            getBone(n).setRotationZ(0);
        }
        for (String n : new String[] {"clawArm", "viceArm"}) {
            float y = 34 - deathCurve(t, 0, 0, 100, 0, 125, 27, 131, 26, 137, 27), z = -1;
            getBone(n).setPositionY((float) (y * Math.cos(fall) + (z + 8) * Math.sin(fall)) - 34);
            getBone(n).setPositionZ((float) (-y * Math.sin(fall) + (z + 8) * Math.cos(fall)) - 10);
            getBone(n).setRotationX(deathCurve(t, 0, 0, 100, 0, 125, 1.35F) - fall);
            getBone(n).setRotationY(0);
            getBone(n).setRotationZ(0);
        }
        for (String n : new String[] {"leftThigh", "rightThigh"})
            getBone(n).setRotationX(deathCurve(t, 0, 0, 116, 0, 134, -.6F, 156, 0));
        for (String n : new String[] {"leftShin", "rightShin"})
            getBone(n).setRotationX(deathCurve(t, 0, 0, 116, 0, 134, 1.1F, 156, .1F));

        if (t >= 172) {
            float y = pilotHeight(t);
            float z = pilotDepth(t);
            IBone pilot = getBone("pilot");
            pilot.setRotationX(-fall * deathCurve(t, 172, 0, 188, .5F, 210, 1));

            pilot.setRotationY(0);
            pilot.setRotationZ(
                    -deathCurve(
                            t, 172, 0, 205, .7F, 235, 0, 300, 1.57F, 370, 1.57F, 400, 0, 490, 0,
                            530, -1.57F, 600, -1.57F, 640, -2.1F));
            pilot.setPositionY((float) (y * Math.cos(fall) + (z + 8) * Math.sin(fall)) - 52);
            pilot.setPositionZ((float) (-y * Math.sin(fall) + (z + 8) * Math.cos(fall)) - 9);
            pilot.setPositionX(pilotSide(t));
            getBone("pilotHead")
                    .setRotationX(
                            deathCurve(
                                    t, 172, .1F, 190, .35F, 218, .45F, 235, .55F, 255, .1F, 300,
                                    .25F, 430, .25F, 470, 0));
            getBone("pilotHead")
                    .setRotationY(
                            deathCurve(
                                    t, 172, 0, 210, -.2F, 235, -.65F, 252, -.3F, 280, 0, 430, .2F,
                                    470, 0));

            getBone("pilotLeftArm")
                    .setRotationX(
                            deathCurve(
                                    t, 172, -1.1F, 186, -.9F, 202, -.35F, 218, .15F, 240, .2F, 260,
                                    -.45F, 280, .1F));
            getBone("pilotRightArm")
                    .setRotationX(
                            deathCurve(
                                    t, 172, -.8F, 192, -1.15F, 208, -.6F, 225, .2F, 252, -.65F, 272,
                                    .1F));
            getBone("pilotLeftForearm")
                    .setRotationX(
                            deathCurve(
                                    t, 172, 0, 185, .45F, 202, .1F, 218, 1.45F, 248, 1.5F, 260,
                                    .65F, 280, 1.4F));
            getBone("pilotRightForearm")
                    .setRotationX(
                            deathCurve(
                                    t, 172, 0, 192, .35F, 208, .1F, 228, 1.45F, 248, .5F, 272,
                                    1.4F));

            getBone("pilotLeftLeg")
                    .setRotationX(
                            deathCurve(
                                    t, 172, -.8F, 192, .65F, 207, .3F, 220, -.45F, 236, 0, 285,
                                    .2F));
            getBone("pilotRightLeg")
                    .setRotationX(
                            deathCurve(
                                    t, 172, .3F, 192, -.7F, 209, .55F, 225, .35F, 240, 0, 285,
                                    .35F));
            getBone("pilotLeftShin")
                    .setRotationX(
                            deathCurve(
                                    t, 172, -1.25F, 192, -1.1F, 208, -.3F, 222, -.65F, 240, 0, 285,
                                    -.3F));
            getBone("pilotRightShin")
                    .setRotationX(
                            deathCurve(
                                    t, 172, -1.25F, 192, -.5F, 210, -1.1F, 229, -.4F, 245, 0, 285,
                                    -.45F));
            if (t >= 260) {
                float speed =
                        (float)
                                Math.hypot(
                                        pilotSide(t + .5F) - pilotSide(t - .5F),
                                        pilotDepth(t + .5F) - pilotDepth(t - .5F));
                float walking = 1 - CombatMotion.smooth((t - (ESCAPE_START - 16)) / 16);
                float stride =
                        (float) Math.sin(walkDistance(t) * Math.PI / 3.4)
                                * Math.min(1, speed * 2)
                                * CombatMotion.smooth((t - 260) / 12);

                float dx = pilotSide(t + 2) - pilotSide(t - 2),
                        dz = pilotDepth(t + 2) - pilotDepth(t - 2);
                if (speed > .025F && t < ESCAPE_START)
                    pilot.setRotationZ(-(float) Math.atan2(dx, dz));
                float inspect =
                        deathCurve(
                                t, 260, 0, 325, 0, 340, 1, 365, 1, 385, 0, 430, 0, 450, 1, 480, 1,
                                505, 0);
                getBone("pilotLeftLeg").setRotationX(stride * .48F * walking - inspect * .18F);
                getBone("pilotRightLeg").setRotationX(-stride * .48F * walking + inspect * .3F);
                getBone("pilotLeftShin")
                        .setRotationX(-Math.max(0, -stride) * .6F * walking - inspect * .35F);
                getBone("pilotRightShin")
                        .setRotationX(-Math.max(0, stride) * .6F * walking - inspect * .65F);
                getBone("pilotLeftArm").setRotationX(-stride * .3F * walking - inspect * .85F);
                getBone("pilotRightArm").setRotationX(stride * .3F * walking - inspect * .3F);
                getBone("pilotLeftForearm")
                        .setRotationX(.18F + Math.max(0, stride) * .12F + inspect * .95F);
                getBone("pilotRightForearm")
                        .setRotationX(.18F + Math.max(0, -stride) * .12F + inspect * .55F);
                getBone("pilotLeftArm").setRotationZ(inspect * .16F);
                getBone("pilotRightArm").setRotationZ(-inspect * .12F);
                getBone("pilotHead").setRotationX(.12F + inspect * .48F);
                getBone("pilotHead")
                        .setRotationY(
                                deathCurve(
                                        t, 260, 0, 325, -.5F, 365, -.5F, 400, 0, 450, -.6F, 480,
                                        -.6F, 520, 0, 570, .6F, 620, .6F, 650, 0));

                float flight = CombatMotion.smooth((t - ESCAPE_START) / 22);
                getBone("pilotLeftLeg")
                        .setRotationX(getBone("pilotLeftLeg").getRotationX() - flight * .24F);
                getBone("pilotRightLeg")
                        .setRotationX(getBone("pilotRightLeg").getRotationX() + flight * .16F);
                getBone("pilotLeftShin")
                        .setRotationX(getBone("pilotLeftShin").getRotationX() - flight * .38F);
                getBone("pilotRightShin")
                        .setRotationX(getBone("pilotRightShin").getRotationX() - flight * .25F);
            }
        } else {
            getBone("pilotLeftLeg").setRotationX(1.25F);
            getBone("pilotRightLeg").setRotationX(1.25F);
        }
    }

    private static final float[] WALK_DISTANCE = walkingDistances();

    private static float[] walkingDistances() {
        float[] d = new float[622];
        for (int i = 261; i < d.length; i++)
            d[i] =
                    d[i - 1]
                            + (float)
                                    Math.hypot(
                                            pilotSide(i) - pilotSide(i - 1),
                                            pilotDepth(i) - pilotDepth(i - 1));
        return d;
    }

    private static float walkDistance(float t) {
        float clamped = Math.max(260, Math.min(620, t));
        int i = (int) clamped;
        return WALK_DISTANCE[i] + (clamped - i) * (WALK_DISTANCE[i + 1] - WALK_DISTANCE[i]);
    }

    public static float pilotHeight(float t) {
        return deathCurve(
                t, 172, 9, 180, 9, 192, 11.5F, 205, 14, 218, 14, 232, 12, 260, 12, 290, 8.5F, 335,
                8.5F, 345, 7.5F, 365, 7.5F, 385, 8.5F, 440, 8.5F, 455, 7, 480, 7, 505, 8.5F, 620,
                8.5F, 636, 14, 652, 36, 670, 100, 700, 300);
    }

    public static float pilotDepth(float t) {
        return deathCurve(
                t, 172, -60, 180, -60, 192, -63, 205, -68, 218, -73, 232, -78, 300, -78, 330, -48,
                370, -48, 440, 12, 490, 12, 620, 12, 640, 2, 670, -75, 700, -260);
    }

    public static float pilotSide(float t) {
        return deathCurve(
                t, 172, 0, 188, -.55F, 205, .45F, 222, -.25F, 260, 0, 300, 36, 490, 36, 550, 12,
                620, 12, 650, 28, 670, 70, 700, 170);
    }
}
