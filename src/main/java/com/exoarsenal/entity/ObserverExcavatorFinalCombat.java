package com.exoarsenal.entity;

import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.WorldServer;
import static com.exoarsenal.entity.WulfrumFinalPairScore.Pattern;
import static com.exoarsenal.entity.BrawlerMartialScore.Attack;

final class ObserverExcavatorFinalCombat extends WulfrumFinalPairCombat {
    ObserverExcavatorFinalCombat(
            List<EntityLivingBase> actors, EntityPlayer player, Pattern pattern) {
        super(actors, player, pattern);
    }

    @Override
    protected void executePattern() {
        observerDrill();
    }

    private void observerDrill() {
        switch (pattern) {
            case attack_31:
                {
                    move(
                            excavator,
                            circle(elapsedTicks * .025, 16, 1 + Math.sin(elapsedTicks * .035) * 2),
                            2);
                    move(eye, excavator.segment(3, 1).addVector(0, 5, 0), 2);
                    for (int i = 0; i < 6; i++) {
                        socket(
                                i,
                                excavator
                                        .segment(2 + i * 2, 1)
                                        .addVector(i % 2 == 0 ? -2 : 2, 1, 0),
                                false);
                        if (elapsedTicks % 42 == i * 6) cannon(i, player.getPositionEyes(1), 15, 7);
                    }
                    if (elapsedTicks > 30 && elapsedTicks % 12 == 0)
                        ray(
                                eye.pupil(),
                                eye.pupil()
                                        .add(
                                                excavator
                                                        .drillTip()
                                                        .subtract(excavator.segment(2, 1))
                                                        .normalize()
                                                        .scale(35)),
                                9,
                                5,
                                6);
                    break;
                }
            case attack_32:
                {
                    deploy(6);
                    move(eye, center.addVector(0, 15, 0), 1);
                    move(excavator, circle(elapsedTicks * .03, 22, -4), 2);
                    for (int i = 0; i < 6; i++) {
                        double a = i * Math.PI / 3 + elapsedTicks * .023;
                        Vec3d at =
                                center.addVector(
                                        Math.cos(a) * 15,
                                        5 + Math.sin(a * 2) * 4,
                                        Math.sin(a) * 15);
                        int q = Math.floorMod(elapsedTicks - i * 7, 48);
                        if (q < 12)
                            at =
                                    at.add(
                                            at.subtract(center)
                                                    .normalize()
                                                    .scale(Math.sin(q * Math.PI / 12) * 3));
                        probe(i, at, false);
                        socket(i, at.addVector(0, 1.5, 0), false);
                        if (q == 1) {
                            probeShot(i, player.getPositionEyes(1), 10);
                            cannon(i, player.getPositionEyes(1), 17, 7);
                        }
                    }
                    if (elapsedTicks % 70 == 40)
                        for (int i = 0; i < 6; i++)
                            if (probeAlive(i) && probeAlive((i + 1) % 6))
                                ray(tips[i], tips[(i + 1) % 6], 16, 20, 6);
                    break;
                }
            case attack_33:
                {
                    deploy(6);
                    move(eye, center.addVector(0, 16, 0), 1);
                    int n = Math.min(5, elapsedTicks / 50), q = elapsedTicks % 50;
                    for (int i = 0; i < 6; i++) {
                        probe(i, ring(i, 15, .8), false);
                        socket(i, ring(i, 14, 9), false);
                    }
                    if (q == 1 && probeAlive(n)) {
                        marks[n] =
                                player.getPositionEyes(1)
                                        .addVector(player.motionX * 16, 0, player.motionZ * 16);
                        warning(probes.get(n).getPositionVector(), marks[n], 20);
                    }
                    if (q == 18 && probeAlive(n)) cannon(n, marks[n], 12, 8);
                    if (q == 30) {
                        aim = player.getPositionVector();
                        excavator.fault(floor(marks[n]), floor(aim), 12, 0);
                    }
                    move(excavator, aim.addVector(0, q < 40 ? -6 : 6, 0), q < 40 ? 2 : 3);
                    if (q == 43) shock(aim, 6);
                    break;
                }
            case attack_34:
                {
                    Vec3d focus = center.addVector(-12, 2, 0);
                    move(eye, focus.addVector(0, 12, 0), 1);
                    for (int i = 0; i < 6; i++)
                        socket(
                                i,
                                focus.addVector(
                                        0,
                                        Math.cos(i * Math.PI / 3) * 6,
                                        Math.sin(i * Math.PI / 3) * 6),
                                false);
                    if (elapsedTicks == 15) {
                        spawn(new EntityWulfrumCut(eye, focus, 3, 60, 160, 0, false).sphere());
                        for (int i = 0; i < 6; i++) cannon(i, focus, 20, 0);
                    }
                    if (elapsedTicks < 70) move(excavator, focus.addVector(-18, -2, 0), 2);
                    else if (elapsedTicks < 210) {
                        Vec3d before = excavator.drillTip();
                        chargeDrill(center.addVector(28, -3, 0), 2.6, false);
                        if (elapsedTicks % 10 == 0) {
                            Vec3d after = excavator.drillTip();
                            ray(
                                    floor(before).addVector(0, .5, 0),
                                    floor(after).addVector(0, .5, 0),
                                    28,
                                    55,
                                    8);
                            excavator.fault(floor(before), floor(after), 28, 5);
                        }
                    }
                    break;
                }
            case attack_35:
                {
                    move(eye, center.addVector(0, 18, 0), 1);
                    int n = elapsedTicks / 48 % 6, q = elapsedTicks % 48;
                    double angle = elapsedTicks * .009 + stage * .2;
                    for (int i = 0; i < 6; i++) {
                        double a = i * Math.PI / 3 + angle;
                        socket(
                                i,
                                center.addVector(
                                        Math.cos(a) * 13, (i % 2) * 9 + 1, Math.sin(a) * 13),
                                false);
                    }
                    if (q == 1)
                        for (int i = 0; i < 6; i++) {
                            double a = i * Math.PI / 3 + angle;
                            cannon(
                                    i,
                                    center.addVector(
                                            -Math.cos(a) * 13, 10 - (i % 2) * 9, -Math.sin(a) * 13),
                                    20,
                                    7);
                            cannon(i, tips[(i + 1) % 6], 20, 7);
                        }
                    Vec3d entry = ring(n, 16, 0), exit = ring(n + 3, 16, 0);
                    if (q < 22) move(excavator, entry.addVector(0, -5, 0), 2);
                    else {
                        chargeDrill(exit.addVector(0, 3, 0), 3, false);
                        if (q == 32) {
                            stage++;
                            impact(excavator.drillTip());
                        }
                    }
                    break;
                }
            case attack_36:
                {
                    int clock = elapsedTicks % 180;
                    double angle = clock * .045;
                    excavator.coordinationPose(
                            WulfrumCoordinationScore.Pattern.attack_52.ordinal(), clock);
                    move(
                            excavator,
                            circle(
                                    angle + Math.PI / 4,
                                    WulfrumCoordinationGeometry.eclipseRadius(),
                                    0),
                            1.5);
                    move(eye, center.addVector(0, 12, 0), 1);
                    for (int i = 0; i < 6; i++) {
                        socket(i, excavator.segment(2 + i * 2, 1).addVector(0, 2, 0), false);
                        if (elapsedTicks % 54 == i * 8) cannon(i, center.addVector(0, 1, 0), 18, 8);
                    }
                    break;
                }
            case attack_37:
                {
                    move(eye, circle(elapsedTicks * .018, 18, 12), 1);
                    move(excavator, circle(-elapsedTicks * .035, 15, -3), 2);
                    int round = elapsedTicks / 70, q = elapsedTicks % 70;
                    if (q == 1) {
                        Vec3d at = ring(round % 6, 8, 0);
                        EntityExcavatorPayload rock =
                                new EntityExcavatorPayload(
                                        excavator,
                                        EntityExcavatorPayload.CHUNK,
                                        at,
                                        Vec3d.ZERO,
                                        0,
                                        75,
                                        2,
                                        0);
                        spawn(rock);
                        platforms.add(rock);
                        marks[round] = at;
                        excavator.debris(at, 8);
                    }
                    if (!platforms.isEmpty()) {
                        EntityExcavatorPayload rock = platforms.get(platforms.size() - 1);
                        Vec3d at =
                                marks[round].addVector(
                                        Math.sin(q * .045) * 2,
                                        Math.sin(Math.min(1, q / 70D) * Math.PI) * 8,
                                        0);
                        rock.place(at);
                        float tilt = 35 + q * .3F;
                        rock.tilt(tilt);
                        for (int i = 0; i < 6; i++)
                            socket(
                                    i,
                                    eye.pupil()
                                            .addVector(
                                                    Math.cos(i * Math.PI / 3) * 3,
                                                    Math.sin(i * Math.PI / 3) * 3,
                                                    0),
                                    false);
                        Vec3d from = tips[round % 6],
                                incoming = at.subtract(from).normalize(),
                                normal =
                                        new Vec3d(
                                                0,
                                                Math.cos(Math.toRadians(tilt)),
                                                Math.sin(Math.toRadians(tilt)));
                        Vec3d end =
                                at.add(
                                        WulfrumCoordinationGeometry.reflect(incoming, normal)
                                                .scale(32));
                        if (q == 20) reflectedRay = ray(from, end, 14, 30, 8);
                        if (reflectedRay != null && !reflectedRay.isDead)
                            reflectedRay.path(new Vec3d[] {from, at, end});
                        if (q == 65) {
                            rock.shatter();
                            if (reflectedRay != null) reflectedRay.setDead();
                        }
                    }
                    break;
                }
            case attack_38:
                {
                    int n = elapsedTicks / 50, q = elapsedTicks % 50;
                    if (n >= 6) {
                        complete = true;
                        break;
                    }
                    Vec3d at = ring(n, 14, 5);
                    if (q < 12) move(eye, at, 3.2);
                    if (q == 12) {
                        marks[n] = eye.pupil();
                        historicalTargets[n] = player.getPositionEyes(1);
                        echoes[n] = EntityWulfrumEcho.snapshot(eye, 38);
                        spawn(echoes[n]);
                        ray(marks[n], historicalTargets[n], 8, 6, 7);
                    }
                    if (q >= 12) move(eye, ring(n + 1, 14, 5), 1.8);
                    move(excavator, at.addVector(0, q < 39 ? -10 : 3, 0), 2.5);
                    if (q == 35) {
                        ray(marks[n], historicalTargets[n], 8, 6, 7);
                        excavator.fault(
                                floor(at).addVector(-3, 0, 0), floor(at).addVector(3, 0, 0), 8, 0);
                    }
                    if (q == 43) {
                        shock(floor(at), 8);
                        excavator.debris(floor(at), 10);
                    }
                    break;
                }
            case attack_39:
                {
                    deploy(20);
                    move(eye, center.addVector(0, 20, 0), 1);
                    move(excavator, circle(elapsedTicks * .055, 9, -5), 2);
                    double radius = 23 - 10 * Math.min(1, elapsedTicks / 300D),
                            rotation = elapsedTicks * .007;
                    for (int i = 0; i < 20; i++) {
                        double x = (i % 5 - 2) * radius * .45, z = (i / 5 - 1.5) * radius * .5;
                        Vec3d at =
                                center.addVector(
                                        x * Math.cos(rotation) - z * Math.sin(rotation),
                                        1,
                                        x * Math.sin(rotation) + z * Math.cos(rotation));
                        probe(i, at, false);
                    }
                    for (int i = 0; i < 6; i++) {
                        socket(i, ring(i, 18, 8), false);
                        if (elapsedTicks % 42 == i * 6) {
                            int from = (elapsedTicks / 42 * 3 + i) % 20, to = (from + 5) % 20;
                            if (probeAlive(from) && probeAlive(to)) {
                                Vec3d a = probes.get(from).getPositionVector(),
                                        z = probes.get(to).getPositionVector();
                                ray(a, z, 18, 16, 8);
                            }
                        }
                    }
                    if (elapsedTicks % 75 == 50) {
                        move(excavator, player.getPositionVector().addVector(0, 5, 0), 4);
                        shock(player.getPositionVector(), 6);
                    }
                    break;
                }
            case attack_40:
                {
                    deploy(20);
                    move(eye, center.addVector(0, 24, 0), 1);
                    double angle = elapsedTicks * .055;
                    move(excavator, circle(angle, 16, -3), 2.2);
                    for (int i = 0; i < 20; i++) probe(i, circle(i * Math.PI / 10, 16, .7), false);
                    for (int i = 0; i < 6; i++) socket(i, ring(i, 14, 10), false);
                    if (elapsedTicks < 250 && elapsedTicks % 8 == 0) {
                        Vec3d a = circle(angle, 16, 0), z = circle(angle + .45, 16, 0);
                        excavator.fault(a, z, 20, 7);
                        com.exoarsenal.world.ExcavatorTerrain.get(player.world)
                                .cut(excavator, new BlockPos(floor(a)).down());
                    }
                    if (elapsedTicks >= 250 && elapsedTicks < 350 && elapsedTicks % 15 == 0) {
                        int i = (elapsedTicks - 250) / 15 % 6;
                        cannon(i, ring(i, 16, 0), 14, 8);
                        for (int n = 0; n < 20; n++)
                            if (probeAlive(n) && probeAlive((n + 1) % 20))
                                ray(
                                        probes.get(n).getPositionVector(),
                                        probes.get((n + 1) % 20).getPositionVector(),
                                        18 + n,
                                        10,
                                        6);
                    }
                    if (elapsedTicks == 350) {
                        aim = player.getPositionEyes(1);
                        ray(eye.pupil(), aim, 28, 30, 14).charged(1.4F, true);
                        warning(center, center.addVector(0, 10, 0), 28);
                    }
                    if (elapsedTicks >= 378) {
                        chargeDrill(center.addVector(0, 12, 0), 3, false);
                        if (elapsedTicks == 385) shock(center, 16);
                    }
                    break;
                }
            default:
                throw new IllegalStateException("Missing Observer/Excavator pattern " + pattern);
        }
    }
}
