package com.exoarsenal.entity;

import net.minecraft.util.math.Vec3d;
import com.exoarsenal.entity.WulfrumCoordinationScore.Pattern;

final class WulfrumCoordinationAnimator {
    private final EntityExcavator excavator;
    private final EntityBrawler brawler;
    private final EntityWulfrumEye seer;
    private final EntityWulfrumEye observer;

    WulfrumCoordinationAnimator(
            EntityExcavator excavator,
            EntityBrawler brawler,
            EntityWulfrumEye seer,
            EntityWulfrumEye observer) {
        this.excavator = excavator;
        this.brawler = brawler;
        this.seer = seer;
        this.observer = observer;
    }

    void apply(Pattern pattern, int elapsedTicks, Vec3d aim, int exchangeLeg, int exchangeTick) {
        if (excavator != null) excavator.coordinationPose(pattern.ordinal(), elapsedTicks);
        if (seer != null) seer.coordinationPose(WulfrumPairPose.seerAttack(pattern), elapsedTicks);
        if (observer != null) {
            observer.coordinationPose(SeerObserverPattern.MACHINEGUN, 36 + elapsedTicks % 120);
            observer.look(aim);
            observer.coordinationChain(!pattern.phaseTwo);
        }
        if (brawler == null) return;
        brawler.coordinationAnimation(pattern, elapsedTicks);
        if (pattern.phaseTwo) {
            brawler.engineMask(31);
            return;
        }
        for (int arm = 0; arm < 4; arm++) {
            double[] base = BrawlerScore.hand(BrawlerScore.Pattern.PISTON, arm, 0);
            double pull = 0, sweep = 0, lift = 0;
            switch (pattern) {
                case attack_3:
                    if (arm == 3) {
                        pull = -1.1 * WulfrumPairPose.hit(elapsedTicks % 10, 3);
                        lift = .7;
                    }
                    break;
                case attack_4:
                    if (arm == 2) {
                        pull = 1.8;
                        lift = 1.2;
                        sweep = Math.sin(elapsedTicks * .07) * 1.5;
                    }
                    break;
                case attack_5:
                    if (arm == 3) {
                        sweep = Math.sin(elapsedTicks * .08) * 2;
                        lift = Math.cos(elapsedTicks * .08);
                    }
                    break;
                case attack_6:
                    if (arm == 1 && exchangeLeg >= 12) {
                        sweep = Math.sin(exchangeTick * .2) * 4;
                        pull = Math.cos(exchangeTick * .2) * 4;
                        lift = .5;
                    }
                    break;
                case attack_9:
                    if (arm == 0) {
                        lift = 2 - 5 * WulfrumPairPose.hit(elapsedTicks, 55);
                        pull = 2 * WulfrumPairPose.hit(elapsedTicks, 55);
                    }
                    break;
                case attack_10:
                    if (arm == 2) {
                        sweep = Math.sin(elapsedTicks * .09) * 2.4;
                        pull = 2 * WulfrumPairPose.hit(elapsedTicks % 30, 16);
                        lift = 1;
                    }
                    break;
                case attack_11:
                    if (arm == 0) {
                        lift = 2 - 5 * WulfrumPairPose.hit(elapsedTicks, 145);
                        pull = 3 * WulfrumPairPose.hit(elapsedTicks, 145);
                    }
                    break;
                case attack_13:
                    if (arm == 0) {
                        pull = 4 * WulfrumPairPose.hit(elapsedTicks, 100);
                        lift = .6;
                    }
                    break;
                case attack_12:
                    if (arm == 2) {
                        lift = -2;
                        pull = 2;
                        sweep = Math.sin(elapsedTicks * .06);
                    }
                    break;
                default:
                    if (arm == 0) pull = -.6 * WulfrumPairPose.hit(elapsedTicks % 64, 20);
                    break;
            }
            brawler.coordinationHand(
                    arm,
                    brawler.localToWorld(
                            new Vec3d(base[0] + sweep, base[1] + lift, base[2] + pull)),
                    1);
        }
    }
}
