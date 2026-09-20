package com.scapeandrun.frostbite.entity;

import net.minecraft.util.math.Vec3d;
import static com.scapeandrun.frostbite.entity.BrawlerMartialScore.*;

public final class BrawlerMartialPose {
    public final double[] hip = {-8, 8}, knee = {16, 16}, ankle = {-8, -24};
    public final Vec3d[] hand = {new Vec3d(-2.3, -.8, 1.6), new Vec3d(2.3, -.65, 1.5)};
    public double pitch, yaw, roll, crouch, thrust;

    public static BrawlerMartialPose sample(Attack a, double t, int reaction, double rt) {
        BrawlerMartialPose p = new BrawlerMartialPose();
        double stride = Math.sin(t * .22);
        p.hip[0] += stride * 5;
        p.hip[1] -= stride * 5;
        p.crouch = .06 * (1 - Math.cos(t * .44));
        for (Beat b : beats(a)) {
            double f = pulse(t, b.tick);
            if (f == 0) continue;
            int side = b.limb % 2;
            double sign = side == 0 ? -1 : 1;
            if (b.limb < 2 || b.limb == 5) {
                Vec3d hit = new Vec3d(sign * .6, -3.1, 3.4);
                switch (b.motion) {
                    case HOOK:
                    case BACKFIST:
                        hit =
                                new Vec3d(
                                        sign
                                                * (2.7
                                                        - 4.2
                                                                * BrawlerScore.smooth(
                                                                        (t - b.tick + 8) / 12)),
                                        -2.8,
                                        2.5);
                        p.yaw += sign * 36 * f;
                        break;
                    case UPPERCUT:
                        hit =
                                new Vec3d(
                                        sign * .5,
                                        -4 + 2 * BrawlerScore.smooth((t - b.tick + 8) / 12),
                                        2.8);
                        p.crouch += .5 * f;
                        break;
                    case SLAM:
                        hit = new Vec3d(sign * .8, -4.1, 2.8);
                        p.pitch += 18 * f;
                        p.crouch += .8 * f;
                        break;
                    case ELBOW:
                        hit = new Vec3d(sign * 1.1, -2.6, 2.5);
                        p.yaw -= sign * 18 * f;
                        break;
                    default:
                        p.yaw += sign * 12 * f;
                        break;
                }
                if (b.limb == 5)
                    for (int i = 0; i < 2; i++)
                        p.hand[i] = blend(p.hand[i], new Vec3d(i == 0 ? -.7 : .7, hit.y, hit.z), f);
                else p.hand[side] = blend(p.hand[side], hit, f);
            } else if (b.limb < 4) {
                if (b.motion == Motion.KNEE) {
                    p.hip[side] -= 100 * f;
                    p.knee[side] += 110 * f;
                    p.crouch += .2 * f;
                } else if (b.motion == Motion.STOMP) {
                    double wind =
                            BrawlerScore.smooth((t - b.tick + 22) / 10)
                                    * (1 - BrawlerScore.smooth((t - b.tick + 5) / 5));
                    p.hip[side] -= 75 * wind;
                    p.knee[side] += 80 * wind;
                    p.crouch += .4 * f;
                } else if (b.motion == Motion.DROPKICK) {
                    p.pitch -= 68 * f;
                    p.hip[0] -= 22 * f;
                    p.hip[1] -= 22 * f;
                    p.knee[0] *= 1 - f;
                    p.knee[1] *= 1 - f;
                    p.thrust = Math.max(p.thrust, f);
                } else {
                    p.hip[side] -= (b.motion == Motion.HEEL ? 98 : 48) * f;
                    p.knee[side] *= 1 - f;
                    if (b.motion == Motion.HEEL) p.yaw += sign * 150 * f;
                }
                p.ankle[side] = -p.hip[side] - p.knee[side];
            } else {
                p.pitch += 35 * f;
                p.crouch += .7 * f;
            }
        }
        if (a == Attack.GROUNDED_MISSILE || a == Attack.MARTIAL_PROTOCOL && t > 338) {
            double f =
                    a == Attack.GROUNDED_MISSILE
                            ? BrawlerScore.smooth(t / 24)
                                    * (1 - BrawlerScore.smooth((t - 166) / 14))
                            : BrawlerScore.smooth((t - 338) / 12)
                                    * (1 - BrawlerScore.smooth((t - 376) / 14));
            p.pitch += 22 * f;
            p.hip[0] += Math.sin(t * .65) * 38 * f;
            p.hip[1] -= Math.sin(t * .65) * 38 * f;
            p.knee[0] += Math.max(0, Math.sin(t * .65)) * 50 * f;
            p.knee[1] += Math.max(0, -Math.sin(t * .65)) * 50 * f;
            p.thrust = .9 * f;
        }
        if (a == Attack.ROCKET_DROPKICK && t < 49) {
            double f = BrawlerScore.smooth(t / 18) * (1 - BrawlerScore.smooth((t - 32) / 17));
            p.crouch += f * .9;
            p.knee[0] += 55 * f;
            p.knee[1] += 55 * f;
            p.hip[0] -= 25 * f;
            p.hip[1] -= 25 * f;
            p.thrust = f;
        }
        if (a == Attack.SUPLEX) {
            double hold =
                    BrawlerScore.smooth((t - 28) / 20) * (1 - BrawlerScore.smooth((t - 118) / 20));
            p.pitch =
                    -180 * BrawlerScore.smooth((t - 55) / 65)
                            - 180 * BrawlerScore.smooth((t - 120) / 28);
            for (int i = 0; i < 2; i++) {
                p.hand[i] = blend(p.hand[i], new Vec3d(i == 0 ? -.8 : .8, .3, 1.3), hold);
                p.knee[i] += (60 - p.knee[i]) * hold;
            }
        }
        if (a == Attack.PISTON_PILEDRIVER) {
            double hold =
                    BrawlerScore.smooth((t - 28) / 25) * (1 - BrawlerScore.smooth((t - 148) / 18));
            p.pitch =
                    180
                            * BrawlerScore.smooth((t - 85) / 30)
                            * (1 - BrawlerScore.smooth((t - 150) / 18));
            for (int i = 0; i < 2; i++)
                p.hand[i] = blend(p.hand[i], new Vec3d(i == 0 ? -.65 : .65, -1.2, 1.8), hold);
            p.thrust = hold;
        }
        if (a == Attack.ROUNDHOUSE) {
            p.yaw +=
                    360 * BrawlerScore.smooth((t - 40) / 30)
                            + 720 * BrawlerScore.smooth((t - 80) / 65);
            p.thrust = t > 30 && t < 146 ? 1 : 0;
        }
        if (a == Attack.FOOTWORK || a == Attack.TESLA_GRID) {
            p.hip[0] += stride * 12;
            p.hip[1] -= stride * 12;
        }
        if (reaction > 0) {
            if (reaction == 2 || reaction == 4) {
                double f = BrawlerScore.smooth(rt / 42);
                p.pitch = -360 * f;
                p.crouch = .6 * Math.sin(f * Math.PI);
                p.hand[0] = new Vec3d(-1.8, -4, 0);
                p.hand[1] = new Vec3d(1.8, -4, 0);
                p.knee[0] = p.knee[1] = 65 * Math.sin(f * Math.PI);
            } else if (reaction == 3) {
                double end = a == Attack.MARTIAL_PROTOCOL ? 150 : 100,
                        f =
                                BrawlerScore.smooth((rt - 28) / 16)
                                        * (1 - BrawlerScore.smooth((rt - end + 20) / 20));
                p.crouch = 1.3 * f;
                p.pitch = 18 * f;
                p.hip[0] = -68 * f;
                p.knee[0] = 115 * f;
                p.hip[1] = 30 * f;
                p.knee[1] = 95 * f;
                p.ankle[0] = -47 * f;
                p.ankle[1] = -65 * f;
                p.hand[0] = blend(p.hand[0], new Vec3d(-3.2, -1.8, .2), f);
                p.hand[1] = blend(p.hand[1], new Vec3d(3.2, -1.4, .4), f);
                p.thrust = 0;
            } else {
                p.pitch -= 10 * Math.sin(Math.min(1, rt / 12) * Math.PI);
            }
        }
        return p;
    }

    public Vec3d hip(int side) {
        return new Vec3d(side == 0 ? -1.1 : 1.1, -1.15, 0);
    }

    public Vec3d knee(int side) {
        return hip(side).add(rotateX(new Vec3d(0, -1.65, 0), hip[side]));
    }

    public Vec3d foot(int side) {
        return knee(side).add(rotateX(new Vec3d(0, -1.65, .25), hip[side] + knee[side]));
    }

    public Vec3d body(Vec3d v) {
        return rotateX(v.rotateYaw((float) Math.toRadians(yaw)), pitch);
    }

    public static Vec3d rotateX(Vec3d v, double degrees) {
        double a = Math.toRadians(degrees);
        return new Vec3d(
                v.x, v.y * Math.cos(a) - v.z * Math.sin(a), v.y * Math.sin(a) + v.z * Math.cos(a));
    }

    private static Vec3d blend(Vec3d a, Vec3d b, double f) {
        return a.add(b.subtract(a).scale(f));
    }

    private BrawlerMartialPose() {}
}
