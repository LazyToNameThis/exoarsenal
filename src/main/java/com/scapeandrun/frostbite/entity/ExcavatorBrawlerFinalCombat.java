package com.scapeandrun.frostbite.entity;

import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.WorldServer;
import static com.scapeandrun.frostbite.entity.WulfrumFinalPairScore.Pattern;
import static com.scapeandrun.frostbite.entity.BrawlerMartialScore.Attack;

final class ExcavatorBrawlerFinalCombat extends WulfrumFinalPairCombat {
    ExcavatorBrawlerFinalCombat(
            List<EntityLivingBase> actors, EntityPlayer player, Pattern pattern) {
        super(actors, player, pattern);
    }

    @Override
    protected void executePattern() {
        drillBrawler();
    }

    private void drillBrawler() {
        if (stage == 8) {
            move(brawler, launch, 2.3);
            combo(Attack.ROCKET_DROPKICK, 75 + Math.min(25, stageTick), false);
            if (brawler.getPositionVector().distanceTo(launch) < 4) {
                impact(launch);
                shock(launch, 9);
                punishStartTick = elapsedTicks;
            } else if (++stageTick >= 48) complete = true;
            return;
        }
        switch (pattern) {
            case attack_41:
            case attack_42:
            case attack_50:
                rally();
                break;
            case attack_43:
                {
                    if (elapsedTicks < 65) {
                        move(brawler, circle(0, 7, 0), .8);
                        move(
                                excavator,
                                brawler.getPositionVector()
                                        .addVector(0, elapsedTicks < 35 ? -8 : 7, 4),
                                2);
                    } else if (elapsedTicks < 165) {
                        Vec3d grip = excavator.segment(3, 1);
                        hand(0, grip.addVector(-1, 0, 0), .1F);
                        hand(1, grip.addVector(1, 0, 0), .1F);
                        combo(Attack.SUPLEX, elapsedTicks - 40, false);
                        move(
                                brawler,
                                center.addVector(
                                        0, Math.sin((elapsedTicks - 65) * Math.PI / 100) * 8, 0),
                                1);
                        double a = (elapsedTicks - 65) * Math.PI / 100;
                        move(
                                excavator,
                                brawler.getPositionVector()
                                        .addVector(Math.cos(a) * 10, Math.sin(a) * 12, 0),
                                2);
                        if (elapsedTicks == 150) {
                            aim = player.getPositionVector();
                            warning(excavator.drillTip(), aim, 20);
                        }
                    } else if (stage != 2) chargeDrill(aim.addVector(12, 0, 0), 3, true);
                    else {
                        move(excavator, circle(0, 20, 1), 2);
                        if (elapsedTicks - replyStartTick > 35) punishStartTick = elapsedTicks;
                    }
                    break;
                }
            case attack_44:
                {
                    platform(8);
                    double lift = 5 + Math.sin(elapsedTicks * .06) * .65;
                    platform.place(center.addVector(0, lift, 0));
                    platform.tilt(
                            elapsedTicks % 70 > 48
                                    ? (float)
                                            (Math.sin((elapsedTicks % 70 - 48) * Math.PI / 22) * 9)
                                    : 0);
                    move(
                            brawler,
                            platform.getPositionVector()
                                    .addVector(Math.sin(elapsedTicks * .035) * 3, 1.2, 0),
                            1.2);
                    combo(Attack.KICKBOXING, elapsedTicks % 196, true);
                    int q = elapsedTicks % 70;
                    move(
                            excavator,
                            platform.getPositionVector().addVector(0, q < 45 ? -9 : -2, 0),
                            2);
                    if (q == 49) {
                        impact(platform.getPositionVector());
                        excavator.debris(platform.getPositionVector(), 8);
                    }
                    if (elapsedTicks >= 270) {
                        combo(Attack.FOUR_LIMB, 140 + elapsedTicks - 270, true);
                        if (elapsedTicks == 299) {
                            platform.shatter();
                            shock(center, 14);
                        }
                    }
                    break;
                }
            case attack_45:
                {
                    deploy(6);
                    move(excavator, circle(elapsedTicks * .02, 24, -4), 1.5);
                    int n = elapsedTicks / 48 % 6, q = elapsedTicks % 48;
                    for (int i = 0; i < 6; i++) probe(i, ring(i, 13, 2), q > 30 && i != n);
                    if (q < 18) move(brawler, ring(n, 11, 0), .9);
                    else {
                        approach(3, 0, 1);
                        combo(Attack.FOOTWORK, q + 7, true);
                    }
                    if (q == 18 && probeAlive(n)) {
                        probeShot(n, brawler.getPositionVector().addVector(0, 3, 0), 4);
                        impact(probes.get(n).getPositionVector());
                    }
                    break;
                }
            case attack_46:
                {
                    int n = Math.min(5, elapsedTicks / 45), q = elapsedTicks % 45;
                    Vec3d from = ring(n, 17, 0), to = ring(n + 3, 17, 0);
                    if (q == 1) excavator.fault(from, to, 38, 0);
                    move(excavator, mix(from, to, q / 45D).addVector(0, -4, 0), 2);
                    approach(4, 0, .6);
                    combo(Attack.STOMP, q + (n % 2 == 0 ? 15 : 53), false);
                    if (q == 25) {
                        Vec3d foot = brawler.footWorld(n % 2, 0);
                        impact(foot);
                        excavator.fault(from, to, 2, 9);
                    }
                    if (elapsedTicks > 270) {
                        combo(Attack.STOMP, 100 + elapsedTicks - 270, true);
                        if (elapsedTicks == 298) {
                            shock(center, 16);
                            for (int i = 0; i < 6; i++)
                                excavator.fault(ring(i, 17, 0), ring(i + 3, 17, 0), 6, 8);
                        }
                    }
                    break;
                }
            case attack_47:
                {
                    if (elapsedTicks < 60) {
                        move(brawler, center.addVector(0, 0, -9), .8);
                        move(
                                excavator,
                                brawler.getPositionVector()
                                        .addVector(0, elapsedTicks < 40 ? -8 : 1, -4),
                                2.5);
                    } else if (elapsedTicks < 105) {
                        move(brawler, center.addVector(0, 14, 0), 1.4);
                        combo(Attack.ROCKET_DROPKICK, 20 + (elapsedTicks - 60) / 2, false);
                        move(excavator, center.addVector(0, -2, 5), 2);
                    } else {
                        if (elapsedTicks == 105) {
                            aim = player.getPositionVector();
                            warning(brawler.getPositionVector(), aim, 18);
                        }
                        if (elapsedTicks > 123 && elapsedTicks < 168) {
                            move(brawler, aim.addVector(0, 1, 0), 2.6);
                            combo(Attack.ROCKET_DROPKICK, 35 + elapsedTicks - 123, true);
                        } else if (elapsedTicks >= 168)
                            move(brawler, floor(brawler.getPositionVector()), 1.2);
                    }
                    break;
                }
            case attack_48:
                {
                    if (elapsedTicks < 90) {
                        double a = elapsedTicks * .04;
                        move(
                                excavator,
                                center.addVector(
                                        Math.cos(a) * 5, elapsedTicks * .13, Math.sin(a) * 5),
                                1.8);
                        move(brawler, excavator.segment(3, 1).addVector(0, -3, 0), 1.5);
                        hand(0, excavator.segment(3, 1).addVector(-1, 0, 0), .1F);
                        hand(1, excavator.segment(3, 1).addVector(1, 0, 0), .1F);
                    } else if (elapsedTicks < 150) {
                        move(brawler, player.getPositionVector().addVector(0, 2, 0), 1.8);
                        combo(Attack.PISTON_PILEDRIVER, elapsedTicks - 80, false);
                        if (elapsedTicks == 130) grab();
                        move(excavator, center.addVector(7, 25, 0), 2);
                    } else if (elapsedTicks < 225) {
                        move(brawler, center.addVector(0, 25, 0), 1.2);
                        combo(Attack.PISTON_PILEDRIVER, 55 + (elapsedTicks - 150) / 2, false);
                        holdPlayer();
                    } else {
                        if (elapsedTicks == 225) {
                            aim = player.getPositionVector();
                            warning(brawler.getPositionVector(), floor(aim), 20);
                        }
                        combo(Attack.PISTON_PILEDRIVER, 100 + elapsedTicks - 225, true);
                        holdPlayer();
                        move(brawler, floor(center), 2);
                        chargeDrill(floor(center).addVector(6, 0, 0), 2.8, false);
                        if (elapsedTicks == 242) throwPlayer(new Vec3d(0, -1.5, 0));
                        if (elapsedTicks == 252) {
                            shock(center, 13);
                            shock(center.addVector(6, 0, 0), 13);
                        }
                    }
                    break;
                }
            case attack_49:
                {
                    approach(3, 0, .65);
                    int local = elapsedTicks % 196;
                    combo(Attack.FOUR_LIMB, local, true);
                    move(excavator, circle(elapsedTicks * .055, 10, -5), 2);
                    for (BrawlerMartialScore.Beat beat :
                            BrawlerMartialScore.beats(Attack.FOUR_LIMB))
                        if (local == beat.tick) {
                            Vec3d behind =
                                    player.getPositionVector()
                                            .add(
                                                    player.getPositionVector()
                                                            .subtract(brawler.getPositionVector())
                                                            .normalize()
                                                            .scale(3));
                            excavator.fault(
                                    floor(behind).addVector(-2, 0, 0),
                                    floor(behind).addVector(2, 0, 0),
                                    18,
                                    8);
                        }
                    if (elapsedTicks > 265) {
                        combo(Attack.FOUR_LIMB, 140 + elapsedTicks - 265, true);
                        if (elapsedTicks == 294) {
                            aim = player.getPositionVector();
                            warning(aim, aim.addVector(0, 12, 0), 20);
                        }
                        if (elapsedTicks > 314) {
                            chargeDrill(aim.addVector(0, 12, 0), 3, false);
                            if (elapsedTicks == 322) shock(aim, 13);
                        }
                    }
                    break;
                }
            default:
                throw new IllegalStateException("Missing Excavator/Brawler pattern " + pattern);
        }
    }

    private void rally() {
        int required = pattern == Pattern.attack_42 ? 3 : 2;
        if (stage == 0) {
            move(excavator, circle(Math.PI, 22, 0), 2);
            Vec3d rear = excavator.segment(17, 1);
            move(brawler, floor(rear).addVector(0, 0, -3), 1.5);
            face(brawler, rear);
            combo(Attack.GROUNDED_MISSILE, Math.min(45, stageTick), false);
            if (++stageTick >= 25 && brawler.getPositionVector().distanceTo(rear) < 7) {
                hand(0, rear, 1);
                impact(rear);
                aim = player.getPositionVector();
                launch = aim.add(aim.subtract(excavator.getPositionVector()).normalize().scale(20));
                warning(excavator.drillTip(), launch, 16);
                stage = 1;
                stageTick = 0;
            }
        } else if (stage == 1) {
            if (++stageTick > 16) chargeDrill(launch, 2.6 + parryCount * .55, true);
            if (stageTick > 80) {
                stage = 0;
                stageTick = 0;
            }
        } else if (stage == 2) {
            Vec3d receiver = brawler.getPositionVector().addVector(0, 3, 0);
            move(excavator, receiver, 2.8);
            face(brawler, excavator.drillTip());
            combo(
                    parryCount % 2 == 0 ? Attack.ROUNDHOUSE : Attack.FOUR_LIMB,
                    Math.min(38, ++stageTick),
                    false);
            if (excavator.drillTip().distanceTo(receiver) < 6) {
                stage = 3;
                stageTick = 0;
                impact(receiver);
            } else if (stageTick > 70) complete = true;
        } else if (stage == 3) {
            int q = ++stageTick;
            Vec3d grip = excavator.segment(2, 1);
            hand(0, grip, 1);
            hand(1, grip, 1);
            if (parryCount < required) {
                combo(
                        parryCount % 2 == 0 ? Attack.ROUNDHOUSE : Attack.FOUR_LIMB,
                        Math.max(0, 38 - 25 + q),
                        false);
                if (q == 26) {
                    aim = player.getPositionVector();
                    launch =
                            aim.add(
                                    aim.subtract(excavator.getPositionVector())
                                            .normalize()
                                            .scale(20));
                    stage = 1;
                    stageTick = 0;
                    impact(grip);
                }
            } else {
                combo(Attack.SUPLEX, 55 + q, false);
                move(
                        excavator,
                        brawler.getPositionVector()
                                .addVector(Math.cos(q * .04) * 12, 5 + Math.sin(q * .04) * 12, 0),
                        2.4);
                if (q > 65) {
                    if (pattern == Pattern.attack_50) {
                        stage = 5;
                        stageTick = 0;
                    } else {
                        shock(excavator.getPositionVector(), 12);
                        punishStartTick = elapsedTicks;
                    }
                }
            }
        } else if (stage == 5) {
            int q = ++stageTick;
            approach(3, 0, .8);
            combo(
                    q < 100 ? Attack.FOUR_LIMB : Attack.MARTIAL_PROTOCOL,
                    q < 100 ? q : 326 + q - 100,
                    true);
            Vec3d behind = player.getPositionVector().addVector(0, q < 100 ? -6 : 5, 7);
            move(excavator, behind, 2);
            if (q == 105) {
                shock(behind, 8);
                warning(brawler.getPositionVector(), player.getPositionEyes(1), 18);
            }
        }
    }
}
