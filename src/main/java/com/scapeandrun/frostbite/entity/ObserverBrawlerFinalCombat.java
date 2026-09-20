package com.scapeandrun.frostbite.entity;

import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.WorldServer;
import static com.scapeandrun.frostbite.entity.WulfrumFinalPairScore.Pattern;
import static com.scapeandrun.frostbite.entity.BrawlerMartialScore.Attack;

final class ObserverBrawlerFinalCombat extends WulfrumFinalPairCombat {
    ObserverBrawlerFinalCombat(
            List<EntityLivingBase> actors, EntityPlayer player, Pattern pattern) {
        super(actors, player, pattern);
    }

    @Override
    protected void executePattern() {
        observerBrawler();
    }

    private void observerBrawler() {
        move(eye, center.addVector(0, 16, 0), 1.2);
        switch (pattern) {
            case attack_11:
                {
                    double rotation = stage * .28;
                    for (int i = 0; i < 6; i++)
                        socket(i, circle(i * Math.PI / 3 + rotation, 11, 1.3), false);
                    approach(3, Math.sin(elapsedTicks * .045) * 3, .65);
                    combo(Attack.KICKBOXING, elapsedTicks % 196, true);
                    if (elapsedTicks % 30 == 1)
                        for (int i = 0; i < 6; i++) cannon(i, tips[(i + 1) % 6], 14, 7);
                    if (elapsedTicks % 30 == 22
                            && brawler.getPositionVector().distanceTo(center) > 8) {
                        stage++;
                        impact(brawler.getPositionVector());
                    }
                    break;
                }
            case attack_12:
            case attack_17:
                {
                    int index = Math.min(5, elapsedTicks / 48), q = elapsedTicks % 48;
                    boolean cross = pattern == Pattern.attack_17;
                    for (int i = 0; i < 6; i++) socket(i, ring(i, 16, 6), false);
                    if (q == 1) {
                        marks[index] =
                                player.getPositionEyes(1)
                                        .addVector(player.motionX * 14, 0, player.motionZ * 14);
                        warning(tips[index], marks[index], 18);
                    }
                    if (cross && q == 12) cannon(index, marks[index], 9, 8);
                    if (q == 22) aim = player.getPositionVector();
                    move(
                            brawler,
                            floor(
                                    (cross ? aim : marks[index])
                                            .addVector(index % 2 == 0 ? -2 : 2, 0, 0)),
                            .9);
                    combo(
                            index % 3 == 0
                                    ? Attack.FOOTWORK
                                    : index % 3 == 1 ? Attack.FOUR_LIMB : Attack.KICKBOXING,
                            Math.max(0, q - 10) + 6,
                            true);
                    if (!cross && q == 38) cannon(index, marks[index], 12, 8);
                    if (elapsedTicks > 288) {
                        approach(3, 0, 1);
                        combo(Attack.MARTIAL_PROTOCOL, 326 + elapsedTicks - 288, true);
                    }
                    break;
                }
            case attack_13:
                {
                    int round = Math.min(3, elapsedTicks / 75), q = elapsedTicks % 75;
                    for (int i = 0; i < 6; i++) socket(i, ring(i, 17, 7), false);
                    if (q == 1) {
                        marks[0] = round == 3 ? player.getPositionEyes(1) : ring(round * 2, 7, 3);
                        spawn(new EntityWulfrumCut(eye, marks[0], 1.8F, 50, 65, 0, false).sphere());
                        for (int i = 0; i < 6; i++) cannon(i, marks[0], 12, 0);
                    }
                    move(brawler, marks[0].addVector(0, -2.5, -2.8), q < 30 ? .8 : 1.2);
                    combo(Attack.KICKBOXING, q + 4, round == 3);
                    if (q == 42) {
                        impact(marks[0]);
                        for (int i = 0; i < 6; i++)
                            ray(
                                    marks[0],
                                    marks[0].addVector(
                                            Math.cos(i * Math.PI / 3) * 25,
                                            0,
                                            Math.sin(i * Math.PI / 3) * 25),
                                    8,
                                    14,
                                    8);
                        velocity =
                                brawler.getPositionVector()
                                        .subtract(marks[0])
                                        .normalize()
                                        .scale(1.2);
                    }
                    if (q > 42) move(brawler, brawler.getPositionVector().add(velocity), 1.2);
                    break;
                }
            case attack_14:
                {
                    for (int i = 0; i < 6; i++)
                        socket(
                                i,
                                center.addVector((i % 2 == 0 ? -1 : 1) * 10, 2, -15 + i * 6),
                                false);
                    if (elapsedTicks < 170) {
                        move(brawler, center.addVector(0, 0, 15 - elapsedTicks * .1), .45);
                        combo(Attack.CATCH_RETURN, elapsedTicks % 70, false);
                        if (elapsedTicks % 25 == 1) {
                            int i = elapsedTicks / 25 % 6;
                            cannon(
                                    i,
                                    center.addVector(
                                            -tips[i].x + 2 * center.x, 2, tips[i].z - center.z),
                                    16,
                                    7);
                        }
                    } else if (stage == 8) {
                        move(brawler, launch, 2);
                        if (++stageTick == 16) {
                            for (Entity hazard : hazards)
                                if (hazard instanceof EntityWulfrumRay) hazard.setDead();
                            impact(eye.pupil());
                            punishStartTick = elapsedTicks;
                        }
                    } else {
                        approach(3, 0, 1.4);
                        combo(Attack.MARTIAL_PROTOCOL, 326 + elapsedTicks - 170, true);
                    }
                    break;
                }
            case attack_15:
                {
                    Vec3d axis = new Vec3d(0, 0, 1);
                    for (int i = 0; i < 6; i++)
                        socket(
                                i,
                                center.addVector((i % 2 == 0 ? -1 : 1) * 4, 4, -22 + i * 3),
                                false);
                    if (elapsedTicks < 50) move(brawler, center.addVector(0, 0, -18), 1);
                    if (elapsedTicks >= 50 && elapsedTicks < 140) {
                        int q = elapsedTicks - 50;
                        if (q % 12 == 0 && q / 12 < 6) {
                            int i = q / 12;
                            cannon(i, brawler.getPositionVector().addVector(0, 3, -1), 3, 0);
                            impact(brawler.getPositionVector());
                        }
                        move(brawler, center.addVector(0, 1, 14), .7 + q * .025);
                        combo(Attack.ROCKET_DROPKICK, Math.max(0, q - 8), true);
                    }
                    if (stage == 8) {
                        move(brawler, center.addVector(0, 5, -24), 2.4);
                        for (int i = 0; i < 6; i++)
                            socket(
                                    i,
                                    tips[i].addVector(
                                            (i % 2 == 0 ? -1 : 1) * stageTick * .2,
                                            stageTick * .1,
                                            0),
                                    false);
                        if (++stageTick >= 25) punishStartTick = elapsedTicks;
                    }
                    break;
                }
            case attack_16:
                {
                    int index = elapsedTicks / 48, q = elapsedTicks % 48;
                    if (index >= 6) {
                        complete = true;
                        break;
                    }
                    Vec3d at = ring(index, 10, 2);
                    if (q < 12) move(eye, at, 3);
                    if (q == 12) {
                        marks[index] = eye.pupil();
                        historicalTargets[index] = player.getPositionEyes(1);
                        echoes[index] = EntityWulfrumEcho.snapshot(eye, 40);
                        spawn(echoes[index]);
                        warning(marks[index], historicalTargets[index], 20);
                    }
                    if (q >= 12) move(eye, ring(index + 1, 14, 8), 1.4);
                    move(brawler, floor(at).addVector(0, 0, -2.7), 1);
                    face(brawler, at);
                    combo(
                            index % 2 == 0 ? Attack.FOUR_LIMB : Attack.ROUNDHOUSE,
                            index % 2 == 0 ? Math.max(0, q - 8) : q + 6,
                            false);
                    if (q >= 22 && q <= 36)
                        hand(
                                index % 2,
                                mix(
                                        brawler.localToWorld(brawler.hand(index % 2, 0)),
                                        marks[index],
                                        BrawlerScore.pulse(q, 32)),
                                1);
                    if (q == 32) {
                        impact(marks[index]);
                        if (echoes[index] != null) echoes[index].setDead();
                        Vec3d forward = historicalTargets[index].subtract(marks[index]).normalize();
                        for (int i = -1; i <= 1; i++)
                            ray(
                                    marks[index],
                                    marks[index].add(forward.rotateYaw(i * .22F).scale(28)),
                                    10,
                                    12,
                                    7);
                    }
                    break;
                }
            case attack_18:
                {
                    Vec3d focus = center.addVector(0, 25, 0);
                    for (int i = 0; i < 6; i++)
                        socket(
                                i,
                                focus.addVector(
                                        Math.cos(i * Math.PI / 3) * 10,
                                        1,
                                        Math.sin(i * Math.PI / 3) * 10),
                                false);
                    if (elapsedTicks == 20) {
                        spawn(new EntityWulfrumCut(eye, focus, 3, 30, 350, 0, false).sphere());
                        for (int i = 0; i < 6; i++) cannon(i, focus, 20, 0);
                    }
                    if (elapsedTicks < 50) {
                        approach(2, 0, .8);
                        if (elapsedTicks == 38) grab();
                        combo(Attack.PISTON_PILEDRIVER, elapsedTicks, false);
                    } else if (elapsedTicks < 120) {
                        move(brawler, focus.addVector(0, -4, 0), 1);
                        combo(Attack.PISTON_PILEDRIVER, 55 + (elapsedTicks - 50) / 2, false);
                        holdPlayer();
                        if (elapsedTicks == 112) throwPlayer(new Vec3d(1.1, .5, 0));
                    } else {
                        if (elapsedTicks == 120) {
                            aim = player.getPositionVector();
                            warning(focus, aim, 24);
                        }
                        if (elapsedTicks < 144) move(brawler, focus, 1);
                        else if (elapsedTicks < 190) {
                            move(brawler, aim, 2.4);
                            combo(Attack.PISTON_PILEDRIVER, 125 + elapsedTicks - 144, true);
                            if (brawler.getPositionVector().distanceTo(aim) < 2 && stage == 0) {
                                stage = 1;
                                shock(aim, 16);
                            }
                        }
                        if (stage == 1 && elapsedTicks % 16 == 0)
                            for (int i = 0; i < 6; i++) {
                                double a = i * Math.PI / 3 + elapsedTicks * .025;
                                ray(
                                        aim.addVector(0, 1, 0),
                                        aim.addVector(Math.cos(a) * 25, 1, Math.sin(a) * 25),
                                        12,
                                        8,
                                        7);
                            }
                    }
                    break;
                }
            case attack_19:
                {
                    approach(3, Math.sin(elapsedTicks * .085) * 5, .9);
                    combo(Attack.FOOTWORK, elapsedTicks % 174, true);
                    double yaw =
                            Math.toRadians(-brawler.rotationYaw)
                                    + brawler.martialPose(0).yaw * Math.PI / 180;
                    for (int i = 0; i < 6; i++) {
                        double a = i * Math.PI / 3 + yaw;
                        socket(
                                i,
                                brawler.getPositionVector()
                                        .addVector(Math.cos(a) * 5, 3, Math.sin(a) * 5),
                                false);
                        if (elapsedTicks % 24 == i * 3)
                            cannon(
                                    i,
                                    tips[i].addVector(Math.cos(a) * 25, 0, Math.sin(a) * 25),
                                    12,
                                    7);
                    }
                    break;
                }
            case attack_20:
                {
                    approach(3, Math.sin(elapsedTicks * .03) * 2, .6);
                    combo(Attack.MARTIAL_PROTOCOL, Math.max(0, elapsedTicks - 20), true);
                    for (int i = 0; i < 6; i++) {
                        socket(i, ring(i, 16, 5 + (i % 2) * 3), false);
                        if (elapsedTicks < 380 && elapsedTicks % 36 == i * 5)
                            cannon(
                                    i,
                                    player.getPositionEyes(1)
                                            .addVector(player.motionX * 12, 0, player.motionZ * 12),
                                    16,
                                    7);
                    }
                    if (elapsedTicks == 395) {
                        aim = player.getPositionEyes(1);
                        for (int i = disabledCannonCount; i < 6; i++) cannon(i, aim, 24, 6);
                        EntityWulfrumRay beam =
                                ray(
                                        eye.pupil(),
                                        aim,
                                        28,
                                        24,
                                        Math.max(4, 14 - disabledCannonCount * 2));
                        beam.charged(Math.max(.25F, 1.6F - disabledCannonCount * .22F), true);
                    }
                    if (elapsedTicks == 435 && disabledCannonCount >= 4)
                        punishStartTick = elapsedTicks;
                    break;
                }
            default:
                throw new IllegalStateException("Missing Observer/Brawler pattern " + pattern);
        }
    }
}
