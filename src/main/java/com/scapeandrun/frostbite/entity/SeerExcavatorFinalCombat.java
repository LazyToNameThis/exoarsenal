package com.scapeandrun.frostbite.entity;

import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.WorldServer;
import static com.scapeandrun.frostbite.entity.WulfrumFinalPairScore.Pattern;
import static com.scapeandrun.frostbite.entity.BrawlerMartialScore.Attack;

final class SeerExcavatorFinalCombat extends WulfrumFinalPairCombat {
    SeerExcavatorFinalCombat(List<EntityLivingBase> actors, EntityPlayer player, Pattern pattern) {
        super(actors, player, pattern);
    }

    @Override
    protected void executePattern() {
        seerDrill();
    }

    private void seerDrill() {
        switch (pattern) {
            case attack_21:
                {
                    int q = elapsedTicks % 100;
                    if (q == 1) {
                        aim = player.getPositionVector();
                        launch = aim.addVector(18, 0, 0);
                        warning(aim.addVector(-20, 0, 0), launch, 28);
                    }
                    if (q < 30) move(excavator, aim.addVector(-20, -5, 0), 2);
                    else
                        chargeDrill(
                                q < 75 ? launch : aim.addVector(22, 8, 0),
                                q < 75 ? 2.6 : 1.5,
                                false);
                    if (q < 75) {
                        move(eye, excavator.segment(2, 1).addVector(0, 1, 0), 3);
                        for (int i = 0; i < 6; i++) {
                            double a = i * Math.PI / 3 + elapsedTicks * .2;
                            socket(
                                    i,
                                    excavator
                                            .drillTip()
                                            .addVector(0, Math.cos(a) * 3, Math.sin(a) * 3),
                                    q > 30);
                        }
                    } else {
                        move(eye, excavator.drillTip().addVector(0, 6, 0), 2);
                        for (int i = 0; i < 6; i++) {
                            double a = i * Math.PI / 3 + elapsedTicks * .2;
                            socket(
                                    i,
                                    eye.pupil()
                                            .addVector(
                                                    Math.cos(a) * (3 + (q - 75) * .15),
                                                    Math.sin(a) * 3,
                                                    (q - 75) * .4),
                                    true);
                        }
                    }
                    break;
                }
            case attack_22:
                {
                    int n = Math.min(5, elapsedTicks / 50), q = elapsedTicks % 50;
                    for (int i = 0; i < 6; i++) socket(i, ring(i, 16, 3), false);
                    if (q == 1) {
                        marks[n] = ring(n, 14, 0);
                        excavator.fault(
                                excavator.getPositionVector().addVector(0, 4, 0), marks[n], 20, 0);
                    }
                    if (q < 24) move(excavator, marks[n].addVector(0, -5, 0), 2);
                    else if (q < 35) {
                        move(excavator, marks[n].addVector(0, 6, 0), 2);
                        socket(n, marks[n].addVector(0, 3 + (q - 24) * .3, 0), false);
                        if (q == 28) {
                            excavator.debris(marks[n], 10);
                            aim = ring(n + 1, 15, 3);
                        }
                    } else slash(aim, 3.2);
                    break;
                }
            case attack_23:
                {
                    platform(7);
                    double lift = Math.min(15, Math.max(0, elapsedTicks - 35) * .075);
                    platform.place(floor(center).addVector(0, lift - 1.2, 0));
                    move(eye, center.addVector(0, lift + 8, 0), 1.5);
                    for (int i = 0; i < 6; i++) {
                        Vec3d rim =
                                center.addVector(
                                        Math.cos(i * Math.PI / 3) * 7,
                                        lift + 1,
                                        Math.sin(i * Math.PI / 3) * 7);
                        int q = elapsedTicks - 60 - i * 24;
                        if (q < 0) socket(i, rim, false);
                        else stroke(i, rim, center.addVector(0, lift + .6, 0), q, 20);
                    }
                    if (elapsedTicks < 215) move(excavator, center.addVector(0, -7, 0), 1.5);
                    else {
                        move(eye, center.addVector(0, 30, 0), 2.6);
                        chargeDrill(center.addVector(0, 25, 0), 3, false);
                        if (elapsedTicks == 233) {
                            platform.shatter();
                            shock(center, 12);
                        }
                    }
                    break;
                }
            case attack_24:
                {
                    deploy(6);
                    move(eye, center.addVector(0, 7, 0), 1);
                    move(excavator, circle(elapsedTicks * .025, 22, -3), 1.5);
                    for (int i = 0; i < 6; i++) {
                        double a = i * Math.PI / 3 + elapsedTicks * .045;
                        Vec3d at =
                                center.addVector(
                                        Math.cos(a) * 12,
                                        2 + Math.sin(a * 2) * 3,
                                        Math.sin(a) * 12);
                        if (elapsedTicks < 230) {
                            int q = Math.floorMod(elapsedTicks - 1 - i * 6, 42);
                            if (q == 0)
                                probeRecoil[i] =
                                        at.subtract(player.getPositionEyes(1)).normalize().scale(3);
                            if (q < 10)
                                at = at.add(probeRecoil[i].scale(Math.sin(q * Math.PI / 10)));
                            socket(i, at, elapsedTicks > 30);
                            probe(i, at, true);
                            if (q == 0) probeShot(i, player.getPositionEyes(1), 10);
                        } else {
                            socket(
                                    i,
                                    eye.pupil().addVector(Math.cos(a) * 3, Math.sin(a) * 3, 0),
                                    false);
                            probe(
                                    i,
                                    player.getPositionEyes(1)
                                            .addVector(Math.cos(a) * 2, 0, Math.sin(a) * 2),
                                    true);
                        }
                    }
                    if (elapsedTicks > 230)
                        slash(player.getPositionVector().addVector(0, 1, 0), 1.6);
                    break;
                }
            case attack_25:
                {
                    move(eye, center.addVector(0, 12, 0), 1);
                    int n = Math.min(5, elapsedTicks / 42), q = elapsedTicks % 42;
                    for (int i = 0; i < 6; i++) socket(i, ring(i, 15, 1), false);
                    if (elapsedTicks < 252) {
                        Vec3d from = ring(n, 16, 0), to = ring(n + 3, 16, 0);
                        if (q == 1) excavator.fault(from, to, 30, 8);
                        stroke(n, from.addVector(0, 1, 0), to.addVector(0, 1, 0), q - 10, 18);
                        move(
                                excavator,
                                mix(from, to, q / 42D).addVector(0, q < 28 ? -5 : 4, 0),
                                2.4);
                        if (q == 31) excavator.debris(excavator.getPositionVector(), 8);
                    } else {
                        slash(center.addVector(0, 1, 0), 2.5);
                        if (elapsedTicks == 280)
                            for (int i = 0; i < 6; i++)
                                excavator.fault(ring(i, 16, 0), ring(i + 3, 16, 0), 8, 9);
                    }
                    break;
                }
            case attack_26:
                {
                    double a = elapsedTicks * .045;
                    Vec3d route = circle(a, 13, Math.sin(elapsedTicks * .04) > 0 ? 1 : -5);
                    chargeDrill(route, 2.4, false);
                    move(eye, route.addVector(0, Math.max(0, -route.y + center.y) + 1, 0), 2.3);
                    for (int i = 0; i < 6; i++) {
                        int index = Math.max(0, history.size() - 1 - (i + 1) * 8);
                        socket(
                                i,
                                history.isEmpty() ? eye.pupil() : history.get(index),
                                elapsedTicks > 45);
                    }
                    if (elapsedTicks % 45 == 1)
                        excavator.fault(floor(route), floor(circle(a + .5, 13, 0)), 18, 7);
                    break;
                }
            case attack_27:
                {
                    int[] order = {0, 3, 1, 4, 2, 5};
                    int n = Math.min(5, elapsedTicks / 45), q = elapsedTicks % 45;
                    for (int i = 0; i < 6; i++) socket(i, ring(i, 16, 1), false);
                    if (elapsedTicks < 270) {
                        Vec3d at = ring(order[n], 16, 0);
                        if (q == 1) {
                            warning(at, at.addVector(0, 8, 0), 20);
                            aim = ring(order[(n + 1) % 6], 16, 3);
                        }
                        move(excavator, at.addVector(0, q < 20 ? -5 : q < 32 ? 6 : -5, 0), 2.5);
                        if (q == 24) {
                            excavator.debris(at, 10);
                            shock(at, 6);
                        }
                        if (q > 24) slash(aim, 3.5);
                    } else {
                        move(excavator, center.addVector(-20 + (elapsedTicks - 270) * .8, 1, 0), 3);
                        slash(center.addVector(20 - (elapsedTicks - 270) * .8, 2, 0), 3);
                        if (elapsedTicks == 295) shock(center, 14);
                    }
                    break;
                }
            case attack_28:
                {
                    int[] order = {0, 2, 4, 1, 3, 0};
                    int n = Math.min(4, elapsedTicks / 58), q = elapsedTicks % 58;
                    Vec3d from = circle(order[n] * Math.PI * 2 / 5, 17, 0),
                            to = circle(order[n + 1] * Math.PI * 2 / 5, 17, 0);
                    if (q == 1) warning(from.addVector(0, 1, 0), to.addVector(0, 1, 0), 16);
                    if (q < 18) {
                        move(eye, from.addVector(0, 2, 0), 2);
                        move(excavator, from.addVector(0, -5, 0), 2);
                    } else {
                        Vec3d point = mix(from, to, (q - 18) / 35D);
                        slash(point.addVector(0, 2, 0), 3);
                        move(excavator, point.addVector(0, -4, 0), 3);
                        if (q == 48) excavator.fault(from, to, 10, 8);
                    }
                    for (int i = 0; i < 6; i++)
                        socket(
                                i,
                                eye.pupil()
                                        .addVector(
                                                Math.cos(i * Math.PI / 3) * 4,
                                                0,
                                                Math.sin(i * Math.PI / 3) * 4),
                                q > 18 && q < 50);
                    if (elapsedTicks == 302) {
                        shock(center, 17);
                        for (int i = 0; i < 6; i++)
                            bladeReplay(ring(i, 17, 1), center.addVector(0, 1, 0), 12);
                    }
                    break;
                }
            case attack_29:
                {
                    if (elapsedTicks < 70) {
                        move(excavator, center.addVector(0, -7, 0), 1.5);
                        move(eye, excavator.drillTip().addVector(0, 2, 0), 2);
                        for (int i = 0; i < 6; i++)
                            socket(
                                    i,
                                    eye.pupil()
                                            .addVector(
                                                    Math.cos(i * Math.PI / 3),
                                                    Math.sin(i * Math.PI / 3),
                                                    1),
                                    false);
                    } else if (elapsedTicks < 120) {
                        move(excavator, center.addVector(0, 8, 0), 2.5);
                        move(eye, center.addVector(0, 22, 0), 2.6);
                        for (int i = 0; i < 6; i++)
                            socket(
                                    i,
                                    eye.pupil()
                                            .addVector(
                                                    Math.cos(i * Math.PI / 3)
                                                            * (1 + (elapsedTicks - 70) * .13),
                                                    Math.sin(i * Math.PI / 3)
                                                            * (1 + (elapsedTicks - 70) * .13),
                                                    0),
                                    elapsedTicks > 105);
                        if (elapsedTicks == 110)
                            spawn(
                                    new EntityWulfrumCut(eye, eye.pupil(), 7, 8, 20, 7, false)
                                            .sphere());
                    } else {
                        for (int i = 0; i < 6; i++) socket(i, ring(i, 16, 0), false);
                        int n = (elapsedTicks - 120) / 30 % 6;
                        move(excavator, ring(n, 10, -4), 2);
                        slash(ring((n + 2) % 6, 14, 6), 2.7);
                    }
                    break;
                }
            case attack_30:
                {
                    storedCuts(12, 340);
                    move(eye, center.addVector(0, 18, 0), 1);
                    int n = Math.min(11, elapsedTicks / 27);
                    Vec3d[] line = cutLines.get(n);
                    move(
                            excavator,
                            mix(line[0], line[1], (elapsedTicks % 27) / 27D).addVector(0, -5, 0),
                            3);
                    for (int i = 0; i < 6; i++) socket(i, ring(i, 12, 3), false);
                    if (elapsedTicks >= 340) {
                        int q = elapsedTicks - 340;
                        if (q % 10 == 0) {
                            Vec3d at = ring(q / 10, 5, 0);
                            move(excavator, at.addVector(0, 6, 0), 8);
                            shock(at, 7);
                            excavator.debris(at, 12);
                        } else
                            move(excavator, excavator.getPositionVector().addVector(0, -2, 0), 2);
                    }
                    break;
                }
            default:
                throw new IllegalStateException("Missing Seer/Excavator pattern " + pattern);
        }
    }
}
