package com.scapeandrun.frostbite.entity;

import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.WorldServer;
import static com.scapeandrun.frostbite.entity.WulfrumFinalPairScore.Pattern;
import static com.scapeandrun.frostbite.entity.BrawlerMartialScore.Attack;

final class SeerBrawlerFinalCombat extends WulfrumFinalPairCombat {
    SeerBrawlerFinalCombat(List<EntityLivingBase> actors, EntityPlayer player, Pattern pattern) {
        super(actors, player, pattern);
    }

    @Override
    protected void executePattern() {
        seerBrawler();
    }

    private void seerBrawler() {
        switch (pattern) {
            case attack_1:
            case attack_5:
                {
                    move(eye, circle(0, 8, 3), 1.5);
                    move(brawler, circle(0, 11, 0), .6);
                    face(brawler, eye.pupil());
                    int n = (elapsedTicks - 30) / 24, q = (elapsedTicks - 30) % 24;
                    if (elapsedTicks >= 30 && n < 7) {
                        Vec3d guard = brawler.localToWorld(brawler.hand(n % 2, 0));
                        combo(n % 2 == 0 ? Attack.PARRY_STRING : Attack.FOUR_LIMB, 24 + q, false);
                        if (n == 0) {
                            move(eye, guard.addVector(-2, 0, 0), 1.2);
                            if (q == 12) impact(guard);
                        } else {
                            stroke(n - 1, eye.pupil(), guard, q, 12);
                            if (q == 12) {
                                impact(guard);
                                marks[n] = player.getPositionEyes(1);
                            }
                            if (q > 12) stroke(n - 1, guard, marks[n], q - 12, 10);
                        }
                    }
                    if (elapsedTicks >= 208) {
                        face(brawler, player.getPositionEyes(1));
                        if (pattern == Pattern.attack_5) {
                            for (int i = 0; i < 6; i++)
                                stroke(i, eye.pupil(), ring(i, 17, 1), elapsedTicks - 208, 24);
                            approach(3, 0, .7);
                            combo(Attack.PARRY_STRING, elapsedTicks - 90, true);
                        } else {
                            slash(player.getPositionVector().addVector(-10, 1, 0), 2.3);
                            approach(3, 0, .9);
                            combo(Attack.ROCKET_DROPKICK, elapsedTicks - 110, true);
                        }
                    }
                    break;
                }
            case attack_2:
                {
                    if (elapsedTicks < 100) {
                        move(eye, circle(Math.PI, 12, 2), .7);
                        move(brawler, circle(0, 10 + elapsedTicks * .05, 0), .55);
                        combo(Attack.GROUNDED_MISSILE, Math.min(40, elapsedTicks), false);
                        for (int i = 0; i < 3; i++) {
                            socket(i, ring(i * 2, 16, 0), false);
                            socket(
                                    i + 3,
                                    brawler.getPositionVector().addVector(i - 1, 3, 0),
                                    false);
                        }
                        if (elapsedTicks == 85) {
                            aim = player.getPositionVector();
                            warning(brawler.getPositionVector(), aim, 20);
                        }
                    } else {
                        if (elapsedTicks == 100)
                            launch =
                                    aim.add(
                                            aim.subtract(brawler.getPositionVector())
                                                    .normalize()
                                                    .scale(10));
                        if (elapsedTicks < 145) {
                            move(brawler, launch, 2.8);
                            combo(Attack.MARTIAL_PROTOCOL, 326 + elapsedTicks - 100, true);
                            move(
                                    eye,
                                    brawler.getPositionVector()
                                            .subtract(launch.subtract(center).normalize().scale(7))
                                            .addVector(0, 2, 0),
                                    2.6);
                        } else
                            move(
                                    brawler,
                                    brawler.getPositionVector().add(velocity),
                                    velocity.lengthVector());
                        for (int i = 0; i < 6; i++) {
                            int n = Math.max(0, history.size() - 1 - i * 4);
                            socket(
                                    i,
                                    history.isEmpty() ? eye.pupil() : history.get(n),
                                    elapsedTicks < 190);
                        }
                        if (elapsedTicks == 149)
                            for (int i = 0; i < 6; i++)
                                bladeReplay(tips[i], aim.addVector((i - 2.5) * 2, 0, 5), i * 5);
                    }
                    break;
                }
            case attack_3:
                {
                    int q = elapsedTicks % 110, round = elapsedTicks / 110;
                    Vec3d catchAt = brawler.localToWorld(brawler.hand(round % 2, 0));
                    if (q < 25) {
                        approach(7, round % 2 == 0 ? -4 : 4, .5);
                        move(eye, catchAt.addVector(0, 1, 0), 1.7);
                        combo(Attack.MARTIAL_PROTOCOL, 148 + q, false);
                    } else if (q < 55) {
                        move(eye, center.addVector(0, 15 - (q - 25) * .45, 0), 2);
                        for (int i = 0; i < 6; i++)
                            socket(
                                    i,
                                    eye.pupil()
                                            .addVector(
                                                    Math.cos(i * Math.PI / 3) * 5,
                                                    0,
                                                    Math.sin(i * Math.PI / 3) * 5),
                                    q > 35);
                        move(brawler, eye.getPositionVector().addVector(0, -4, 0), 1.1);
                    } else if (q < 75) {
                        hand(0, eye.pupil(), .2F);
                        hand(1, eye.pupil(), .2F);
                        move(
                                eye,
                                brawler.getPositionVector()
                                        .addVector(Math.cos(q * .3) * 4, 4, Math.sin(q * .3) * 4),
                                2.5);
                    } else {
                        if (q == 75) {
                            aim = player.getPositionVector();
                            impact(eye.pupil());
                        }
                        slash(aim.addVector(round % 2 == 0 ? 14 : -14, 1, 0), 3);
                        move(brawler, aim.addVector(round % 2 == 0 ? 10 : -10, 0, 0), 1.5);
                        combo(Attack.FOUR_LIMB, Math.max(0, q - 75) + 6, false);
                    }
                    break;
                }
            case attack_4:
                {
                    move(eye, center.addVector(0, 9, 0), 1);
                    int step = Math.min(5, elapsedTicks / 45), q = elapsedTicks % 45;
                    for (int i = 0; i < 6; i++) socket(i, ring(i, 12, 1 + (i % 2)), false);
                    Vec3d rail = mix(eye.pupil(), tips[step], .6);
                    move(brawler, rail.addVector(0, -1.5, 0), 1);
                    combo(Attack.FOOTWORK, (q * 3) % 174, true);
                    if (q >= 27)
                        stroke(step, ring(step, 12, 1), player.getPositionEyes(1), q - 27, 16);
                    break;
                }
            case attack_6:
                {
                    move(eye, circle(elapsedTicks * .014, 14, 5), 1.2);
                    double radius = 14 - 8 * Math.min(1, elapsedTicks / 270D);
                    for (int i = 0; i < 6; i++) {
                        double rebound =
                                2
                                        * Math.sin(
                                                Math.max(
                                                        0,
                                                        Math.min(
                                                                Math.PI,
                                                                (elapsedTicks % 54 - 28) * .18)));
                        socket(
                                i,
                                ring(i, radius + (elapsedTicks / 54 % 6 == i ? rebound : 0), 1.5),
                                elapsedTicks % 54 > 34);
                    }
                    approach(3, 0, .4);
                    combo(Attack.KICKBOXING, elapsedTicks % 196, true);
                    if (elapsedTicks == 275) {
                        shock(brawler.getPositionVector(), 14);
                        for (int i = 0; i < 6; i++) bladeReplay(tips[i], ring(i + 3, 18, 1), 12);
                    }
                    break;
                }
            case attack_7:
                {
                    int round = elapsedTicks / 75, q = elapsedTicks % 75;
                    if (round < 3) {
                        Vec3d rear = brawler.footWorld(round % 2, 0);
                        if (q < 25) {
                            approach(8, round % 2 == 0 ? -5 : 5, .6);
                            move(eye, rear.addVector(0, .8, 1), 1.6);
                            combo(Attack.ROUNDHOUSE, Math.max(0, 38 - 25 + q), false);
                        } else {
                            if (q == 25) {
                                aim = player.getPositionEyes(1);
                                launch = aim.add(aim.subtract(eye.pupil()).normalize().scale(8));
                                impact(eye.pupil());
                            }
                            slash(launch, 3.3);
                        }
                    } else {
                        slash(center.addVector(-16, .3, 0), 3.3);
                        move(
                                brawler,
                                player.getPositionVector()
                                        .addVector(
                                                0, Math.max(1, 10 - (elapsedTicks - 225) * .2), 0),
                                1.8);
                        combo(Attack.ROCKET_DROPKICK, elapsedTicks - 225 + 25, true);
                    }
                    break;
                }
            case attack_8:
                {
                    double close = Math.max(2, 18 - elapsedTicks * .055);
                    Vec3d duel = center.addVector(close, 0, 0);
                    move(brawler, duel, .8);
                    move(
                            eye,
                            duel.addVector(
                                    Math.sin(elapsedTicks * .12) * 4,
                                    3,
                                    Math.cos(elapsedTicks * .12) * 4),
                            2);
                    combo(Attack.FOUR_LIMB, elapsedTicks % 196, false);
                    for (int i = 0; i < 6; i++)
                        socket(
                                i,
                                mix(
                                        eye.pupil(),
                                        brawler.localToWorld(brawler.hand(i % 2, 0)),
                                        BrawlerScore.pulse(elapsedTicks % 36, 18)),
                                false);
                    if (elapsedTicks % 36 == 18)
                        impact(brawler.localToWorld(brawler.hand(elapsedTicks / 36 % 2, 0)));
                    if (elapsedTicks >= 238) {
                        face(brawler, player.getPositionEyes(1));
                        approach(3, 0, 1);
                        combo(Attack.MARTIAL_PROTOCOL, 310 + elapsedTicks - 238, true);
                        for (int i = 0; i < 6; i++)
                            stroke(
                                    i,
                                    ring(i, 10, 2),
                                    ring(i + 3, 10, 1),
                                    elapsedTicks - 260 - i * 2,
                                    18);
                        if (elapsedTicks > 270) eye.slash(10);
                    }
                    break;
                }
            case attack_9:
                {
                    if (elapsedTicks < 55) {
                        approach(7, 0, .6);
                        move(eye, brawler.getPositionVector().addVector(0, 4, 1), 1.5);
                    } else if (elapsedTicks < 145) {
                        move(brawler, center.addVector(0, (elapsedTicks - 55) * .18, 0), 1);
                        move(
                                eye,
                                brawler.getPositionVector()
                                        .addVector(
                                                Math.sin((elapsedTicks - 55) * .055) * 4,
                                                4 * Math.cos((elapsedTicks - 55) * .055),
                                                0),
                                1.5);
                        combo(Attack.SUPLEX, elapsedTicks - 20, false);
                    } else if (elapsedTicks < 190) {
                        slash(center.addVector(0, 1, 0), 2.5);
                        move(brawler, center.addVector(0, 20, 0), 1);
                    } else {
                        move(eye, center.addVector(0, 23, 0), 3);
                        move(brawler, center, 2.4);
                        combo(Attack.PISTON_PILEDRIVER, 125 + elapsedTicks - 190, true);
                        if (elapsedTicks == 207) shock(center, 14);
                    }
                    for (int i = 0; i < 6; i++)
                        socket(
                                i,
                                i < 4
                                        ? brawler.getPositionVector()
                                                .addVector(
                                                        Math.cos(i * Math.PI / 2) * 2,
                                                        3,
                                                        Math.sin(i * Math.PI / 2) * 2)
                                        : ring(i, 10, 22),
                                false);
                    break;
                }
            case attack_10:
                {
                    storedCuts(12, 408);
                    move(eye, center.addVector(0, 18, 0), 1);
                    approach(3, Math.sin(elapsedTicks * .035), .55);
                    combo(Attack.MARTIAL_PROTOCOL, Math.max(0, elapsedTicks - 18), true);
                    for (int i = 0; i < 6; i++) socket(i, ring(i, 12, 4), false);
                    if (elapsedTicks == 408) {
                        impact(center);
                        if (parryCount >= 5) punishStartTick = elapsedTicks;
                    }
                    break;
                }
            default:
                throw new IllegalStateException("Missing Seer/Brawler pattern " + pattern);
        }
    }
}
