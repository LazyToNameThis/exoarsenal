package com.scapeandrun.frostbite.entity;

import java.util.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.*;
import static com.scapeandrun.frostbite.entity.BrawlerAerialScore.Attack;
import static com.scapeandrun.frostbite.entity.EntityBrawlerEffect.*;

final class BrawlerAerialCombat {
    private final EntityBrawler b;
    private final List<EntityBrawlerEffect> objects = new ArrayList<>(),
            blades = new ArrayList<>(),
            chains = new ArrayList<>(),
            panels = new ArrayList<>();
    private Vec3d center = Vec3d.ZERO,
            locked = Vec3d.ZERO,
            start = Vec3d.ZERO,
            axis = new Vec3d(1, 0, 0),
            recoil = Vec3d.ZERO;
    private EntityBrawlerEffect cleaver, rail, weaponChain;
    private int pause, recovery, successful, critical = -1, lastParry = -100;
    private boolean crash;

    BrawlerAerialCombat(EntityBrawler boss) {
        b = boss;
    }

    void reset() {
        for (EntityBrawlerEffect e : objects) e.setDead();
        objects.clear();
        blades.clear();
        chains.clear();
        panels.clear();
        cleaver = rail = weaponChain = null;
        pause = recovery = successful = 0;
        lastParry = -100;
        crash = false;
        b.aerialRecovery(0);
    }

    private EntityBrawlerEffect fx(
            int kind, Vec3d from, Vec3d to, int warning, int life, float radius, float damage) {
        EntityBrawlerEffect e =
                new EntityBrawlerEffect(b, kind, from, to, warning, life, radius, damage);
        b.world.spawnEntity(e);
        objects.add(e);
        return e;
    }

    private Vec3d surface(Vec3d at) {
        BlockPos p = new BlockPos(at);
        for (int n = 0; n < 64 && !b.world.getBlockState(p.down()).getMaterial().isSolid(); n++)
            p = p.down();
        for (int n = 0; n < 24 && b.world.getBlockState(p).getMaterial().isSolid(); n++) p = p.up();
        return new Vec3d(at.x, p.getY() + .1, at.z);
    }

    private Vec3d circle(double angle, double radius, double y) {
        return center.addVector(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
    }

    private Vec3d socket(int arm) {
        return b.localToWorld(b.hand(arm, 0));
    }

    private void fly(Vec3d to, double speed) {
        b.steer(to, Math.min(2.8, speed));
    }

    private void lock(EntityLivingBase target) {
        locked = target.getPositionEyes(1);
        b.aerialAim(locked);
    }

    private void warning(Vec3d to, int delay) {
        fx(CHAIN, b.getPositionVector(), to, delay, delay + 1, .025F, 0);
    }

    private void wave(Vec3d at, float radius, float damage) {
        fx(RING, surface(at), Vec3d.ZERO, 5, 24, radius, damage);
    }

    private void burst(Vec3d from, Vec3d to, int count, double speed) {
        Vec3d d = to.subtract(from).normalize();
        for (int i = 0; i < count; i++) {
            Vec3d velocity =
                    d.rotateYaw((float) ((i - (count - 1) * .5) * .13))
                            .scale(speed)
                            .addVector(0, (i % 3 - 1) * .06, 0);
            fx(SHARD, from, to, 5, 70, .38F, 7).velocity(velocity);
        }
    }

    private void hit(int arm, float damage) {
        if (b.clock() > lastParry + 6 && b.armHealth(arm) > 0)
            b.strike(arm, damage, 2.5, arm == 0 || attack() == Attack.COUNTER_THRUST);
    }

    private Attack attack() {
        return BrawlerAerialScore.decode(b.aerialAttack());
    }

    private void anchors(int count, double radius) {
        for (int i = 0; i < count; i++) {
            Vec3d at = surface(circle(i * Math.PI * 2 / count, radius, 0));
            chains.add(fx(CHAIN, socket(2), at, 10, 260, .12F, 4).tether());
        }
    }

    private void releaseAnchors(int keep) {
        while (chains.size() > keep) chains.remove(chains.size() - 1).setDead();
    }

    private void depot(int count) {
        for (int n = 0; n < count; n++) {
            Vec3d at = circle(n * Math.PI * 2 / count, 5 + n % 3 * 2, 5 + (n % 4) * 2.5);
            blades.add(fx(BLADE, at, at, 10, 260, .8F, 0));
        }
    }

    private void fireBlade(int index, Vec3d at, double speed) {
        if (index >= blades.size()) return;
        EntityBrawlerEffect e = blades.get(index);
        if (e.isDead) return;
        e.launch(at.subtract(e.getPositionVector()).normalize().scale(speed), 10);
    }

    private void dropAll() {
        for (EntityBrawlerEffect e : blades) if (!e.isDead) e.launch(new Vec3d(0, -1.15, 0), 10);
    }

    private void dive(
            int t, int begin, int contact, EntityLivingBase target, int arm, double speed) {
        if (t == begin) {
            lock(target);
            warning(locked, contact - begin);
        }
        if (t >= begin && t <= contact + 4) {
            fly(locked.addVector(0, -2.2, 0), speed);
            if (t >= contact - 2 && t <= contact + 2) hit(arm, arm == 0 ? 15 : 12);
        }
    }

    void parried(EntityPlayer player) {
        successful++;
        lastParry = b.clock();
        Attack a = attack();
        pause = a == Attack.SKYHOOK ? 12 : 6;
        recoil =
                b.getPositionVector()
                        .subtract(player.getPositionVector())
                        .normalize()
                        .scale(a == Attack.SKYHOOK ? 2.1 : .9);
        if (a == Attack.SKYHOOK) {
            Vec3d side = player.getLookVec().crossProduct(new Vec3d(0, 1, 0)).normalize();
            if (side.lengthSquared() < .1) side = new Vec3d(1, 0, 0);
            recoil = side.scale(2.1).addVector(0, .1, 0);
        }

        if (a == Attack.COUNTER_THRUST && successful < 3
                || a == Attack.PRIMITIVE_AIR_SUPERIORITY && b.clock() < 198) {
            recovery = 0;
        } else {
            recovery = a == Attack.SKYHOOK ? 30 : a == Attack.PRIMITIVE_AIR_SUPERIORITY ? 76 : 24;
            crash = a == Attack.PRIMITIVE_AIR_SUPERIORITY;
            b.aerialRecovery(recovery);
        }
        b.world.playSound(
                null,
                b.getPosition(),
                net.minecraft.init.SoundEvents.BLOCK_ANVIL_LAND,
                net.minecraft.util.SoundCategory.HOSTILE,
                1.4F,
                .7F);
    }

    boolean paused() {
        return pause > 0 || recovery > 0;
    }

    void tickRecovery() {
        if (pause > 0) {
            pause--;
            b.motionX = b.motionY = b.motionZ = 0;
            return;
        }
        if (recovery <= 0) return;
        b.aerialRecovery(recovery);
        int left = --recovery;
        if (crash) {
            Vec3d floor = surface(b.getPositionVector());
            if (b.posY > floor.y + .2)
                b.combatMove(
                        new Vec3d(
                                recoil.x * .15,
                                -Math.min(1.3, .2 + (76 - left) * .045),
                                recoil.z * .15));
            else if (left == 55 || left == 54) wave(floor, 12, 0);
        } else {
            double spin = attack() == Attack.SKYHOOK ? (30 - left) * .19 : 0;
            Vec3d v = recoil.rotateYaw((float) spin).scale(Math.min(1, left / 12D));
            b.combatMove(v.addVector(.0, .06 * Math.sin(left * .3), 0));
        }
        if (left == 0) {
            reset();
            b.advanceAerial();
        }
    }

    void tick(EntityLivingBase target) {
        int t = b.clock();
        Attack a = attack();
        objects.removeIf(e -> e.isDead);
        if (t == 1) {
            reset();
            center = target.getPositionVector();
            start = b.getPositionVector();
            axis = new Vec3d(start.x - center.x, 0, start.z - center.z).normalize();
            if (axis.lengthSquared() < .1) axis = new Vec3d(1, 0, 0);
            lock(target);
            critical = -1;
            for (int i = 0; i < 4; i++)
                if (b.armHealth(i) > 0 && b.armHealth(i) <= 20) {
                    critical = i;
                    break;
                }
        }
        b.aerialAim(locked);
        switch (a) {
            case FOURFOLD:
                if (t < 25) fly(center.addVector(8, 15, 0), 1.35);
                if (t == 22) burst(socket(3), target.getPositionEyes(1), 7, 1.1);
                dive(t, 30, 48, target, 1, 1.9);
                if (t >= 54 && t < 82) fly(center.addVector(-7, 17, 3), 1.6);
                if (t == 76) anchors(1, 13);
                if (t >= 82 && t < 108)
                    fly(circle((t - 82) * .1, 12, 8 + Math.sin((t - 82) * .12) * 5), 1.9);
                dive(t, 108, 122, target, 0, 2.3);
                if (t == 129) releaseAnchors(0);
                break;
            case BALLISTIC_CLEAVER:
                if (t < 24) fly(center.addVector(0, 17, 0), 1.7);
                if (t == 24) {
                    cleaver = fx(CUT, socket(1), locked, 0, 140, 1.6F, 10);
                    cleaver.launch(new Vec3d(0, -.4, 0), 10);
                }
                if (t == 38 && cleaver != null) {
                    hit(0, 8);
                    cleaver.launch(
                            locked.addVector(2, -1, 0)
                                    .subtract(cleaver.getPositionVector())
                                    .normalize()
                                    .scale(2.1),
                            12);
                }
                if (t == 42 && cleaver != null) {
                    cleaver.guide(center.addVector(2, 2, 0), 0);
                    weaponChain = fx(CHAIN, socket(2), cleaver.getPositionVector(), 0, 92, .1F, 4);
                }
                if (t >= 44 && t < 104 && cleaver != null) {
                    fly(circle((t - 44) * .045, 9, 13), 1.7);
                    Vec3d at =
                            b.getPositionVector()
                                    .addVector(
                                            Math.sin((t - 44) * .14) * 7,
                                            -8,
                                            Math.cos((t - 44) * .14) * 2);
                    cleaver.guide(at, 11);
                    weaponChain.endpoints(socket(2), at);
                    if (t % 12 == 8) {
                        burst(socket(3), at, 1, 1.3);
                        burst(at, target.getPositionEyes(1), 4, 1.25);
                    }
                }
                if (t == 104 && cleaver != null) locked = cleaver.getPositionVector();
                if (t >= 104 && t < 112) fly(locked, 2.1);
                if (t == 112) {
                    if (cleaver != null) cleaver.setDead();
                    if (weaponChain != null) weaponChain.setDead();
                }
                dive(t, 112, 122, target, 1, 2.1);
                break;
            case JETSTREAM_BOXING:
                if (t < 16) fly(target.getPositionVector().addVector(6, 5, 0), 1.6);
                dive(t, 16, 24, target, 0, 2);
                if (t >= 27 && t < 34) fly(center.addVector(-5, 3, 3), 1.4);
                dive(t, 34, 42, target, 1, 1.8);
                if (t >= 46 && t < 58) fly(center.addVector(2, 12, 0), 2);
                if (t == 56) fx(CHAIN, socket(2), target.getPositionEyes(1), 6, 16, .16F, 5);
                dive(t, 60, 72, target, 0, 2);
                if (t >= 76 && t < 86) fly(center.addVector(-5, 8, -3), 2);
                dive(t, 86, 94, target, 1, 2);
                dive(t, 100, 112, target, 0, 2.2);
                break;
            case ORBITAL_HARPOONS:
                if (t < 20) fly(center.addVector(0, 16, 0), 1.5);
                if (t == 20) anchors(4, 14);
                if (t >= 20 && t < 104) {
                    double angle = (t - 20) * .07;
                    fly(circle(angle, 10, 9 + Math.sin(angle * 2) * 5), 1.9);
                    if (t % 18 == 6) burst(socket(3), target.getPositionEyes(1), 4, 1.15);
                    if (t == 48 || t == 84) hit(1, 12);
                    if (t == 66) hit(0, 13);
                }
                if (t == 104) releaseAnchors(1);
                if (t >= 104 && t < 124)
                    fly(circle((t - 104) * .13, 15, 5 + Math.cos((t - 104) * .12) * 7), 2.3);
                if (t == 124) releaseAnchors(0);
                dive(t, 124, 138, target, 0, 2.6);
                break;
            case AIR_SUPERIORITY:
                if (t < 38) fly(circle(t * .055, 11, 22), 1.9);
                if (t == 20 || t == 30 || t == 40)
                    burst(socket(3), target.getPositionEyes(1), 8, .75);
                dive(t, 48, 64, target, 1, 2.3);
                if (t == 64) burst(socket(1), target.getPositionEyes(1), 9, 1.5);
                if (t >= 70 && t < 96) fly(center.addVector(-5, 16, 0), 2.2);
                dive(t, 98, 114, target, 0, 2.6);
                break;
            case TESLA_CAGE:
                if (t < 24) fly(center.addVector(0, 15, 0), 1.5);
                if (t == 20)
                    for (int n = 0; n < 6; n++) {
                        Vec3d at = circle(n * Math.PI / 3, 10, 4);
                        panels.add(fx(SHELL, at, center.addVector(0, 4, 0), 14, 120, 2.8F, 7));
                    }
                for (int n = 0; n < 4; n++) {
                    int contact = 42 + n * 26;
                    if (t == contact - 14) {
                        locked = circle(n * Math.PI / 2 + Math.PI / 6, 8, 2);
                        warning(locked, 14);
                    }
                    if (t >= contact - 14 && t < contact + 8) fly(locked, 2.2);
                    if (t == contact) {
                        hit(0, 12);
                        if (!panels.isEmpty()) {
                            Vec3d first = panels.get(n % 6).getPositionVector(),
                                    other = panels.get((n + 3) % 6).getPositionVector();
                            fx(CHAIN, first, b.getPositionVector(), 5, 14, .28F, 11);
                            fx(CHAIN, b.getPositionVector(), other, 5, 14, .28F, 11);
                        }
                    }
                }
                if (t == 122) {
                    for (EntityBrawlerEffect panel : panels) {
                        burst(panel.getPositionVector(), target.getPositionEyes(1), 3, 1);
                        panel.setDead();
                    }
                    panels.clear();
                }
                break;
            case SKYHOOK:
                if (t == 8) anchors(1, 2);
                if (t < 44) fly(center.addVector(0, 25, 0), 1.9);
                if (t == 44) {
                    start = b.getPositionVector();
                    lock(target);
                    warning(locked, 40);
                }
                if (t >= 44 && t < 66) b.combatMove(new Vec3d(0, -.03 * (t - 43), 0));
                dive(t, 66, 84, target, 0, 2.8);
                if (t == 89) {
                    releaseAnchors(0);
                    wave(b.getPositionVector(), 12, 14);
                }
                break;
            case AERIAL_FOUNDRY:
                if (t < 22) fly(center.addVector(0, 17, 0), 1.7);
                if (t == 22) depot(12);
                if (t >= 28 && t < 112 && !blades.isEmpty()) {
                    int index = Math.min(3, (t - 28) / 20);
                    fly(blades.get(index).getPositionVector().addVector(0, 1, -2), 1.8);
                }
                if (t == 44) fireBlade(0, target.getPositionEyes(1), 1.8);
                if (t == 62) fireBlade(1, target.getPositionEyes(1), 2.1);
                if (t == 72 && blades.size() > 2)
                    weaponChain =
                            fx(CHAIN, socket(2), blades.get(2).getPositionVector(), 0, 26, .12F, 4);
                if (t >= 72 && t < 88 && blades.size() > 2) {
                    Vec3d at = circle((t - 72) * .2, 8, 7);
                    blades.get(2).guide(at, 8);
                    weaponChain.endpoints(socket(2), at);
                }
                if (t == 88) {
                    fireBlade(2, target.getPositionEyes(1), 1.9);
                    weaponChain.setDead();
                }
                if (t == 102)
                    for (int n = 3; n < 6; n++) fireBlade(n, target.getPositionEyes(1), 1.5);
                if (t >= 112) fly(center.addVector(0, 22, 0), 1.8);
                if (t == 128) dropAll();
                break;
            case MACH_CLEAVER:
                for (int n = 0; n < 3; n++) {
                    int base = n * 32, contact = base + 34;
                    Vec3d side = axis.scale(n % 2 == 0 ? 32 : -32);
                    if (t >= base && t < base + 18)
                        fly(center.add(side).addVector(0, 4 + n * 3, 0), 2.5);
                    if (t == base + 18) {
                        locked = center.subtract(side).addVector(0, 5 + n * 3, 0);
                        warning(locked, 16);
                    }
                    if (t >= base + 18 && t <= contact + 1) fly(locked, 2.8);
                    if (t == contact) {
                        hit(1, 13);
                        panels.add(
                                fx(
                                        PLANE,
                                        center.addVector(0, 3 + n * 3, 0),
                                        axis,
                                        12,
                                        104 - n * 32,
                                        13,
                                        8));
                    }
                }
                dive(t, 108, 124, target, 0, 2.7);
                if (t == 126) {
                    for (EntityBrawlerEffect plane : panels) {
                        burst(plane.getPositionVector(), target.getPositionEyes(1), 6, 1.35);
                        plane.setDead();
                    }
                    panels.clear();
                }
                break;
            case VERTICAL_RAILGUN:
                if (t < 28) fly(center.addVector(0, 20, 0), 1.8);
                if (t == 20) {
                    for (int s : new int[] {-1, 1})
                        chains.add(
                                fx(
                                                CHAIN,
                                                socket(2),
                                                surface(center.addVector(s * 2, 0, 0)),
                                                8,
                                                116,
                                                .1F,
                                                4)
                                        .tether());
                }
                if (t == 28) rail = fx(BLADE, center.addVector(0, 15, 0), center, 12, 120, 2.2F, 0);
                if (t == 44 && rail != null) rail.launch(new Vec3d(0, -.35, 0), 8);
                if (t == 50 && rail != null) {
                    hit(0, 8);
                    rail.launch(new Vec3d(0, -2.7, 0), 18);
                }
                if (t == 57) {
                    wave(center, 12, 12);
                    if (rail != null) rail.setDead();
                    rail = fx(BLADE, surface(center), center.addVector(0, 20, 0), 14, 72, 2.2F, 14);
                }
                if (t >= 72 && t < 105 && rail != null) {
                    rail.guide(surface(center).addVector(0, (t - 72) * .55, 0), 14);
                    fly(center.addVector(4, 20 - (t - 72) * .4, 0), 1.8);
                }
                if (t >= 105 && t < 116 && rail != null)
                    fly(rail.getPositionVector().addVector(0, -1, 0), 1.8);
                if (t == 116 && rail != null) {
                    hit(0, 12);
                    burst(rail.getPositionVector(), target.getPositionEyes(1), 10, 1.3);
                    rail.setDead();
                    releaseAnchors(0);
                }
                break;
            case DOGFIGHT:
                if (t == 12) burst(socket(3), target.getPositionEyes(1), 6, 1.2);
                if (t < 28) fly(target.getPositionVector().addVector(6, 5, 0), 1.8);
                dive(t, 28, 38, target, 1, 2.3);
                if (t == 44) anchors(1, 12);
                dive(t, 48, 62, target, 0, 2.3);
                if (t >= 66 && t < 78) fly(target.getPositionVector().addVector(-4, 14, 0), 2.3);
                dive(t, 78, 88, target, 0, 2.5);
                if (t == 94) {
                    releaseAnchors(0);
                    center = target.getPositionVector();
                    anchors(1, 13);
                }
                if (t >= 94 && t < 110)
                    fly(
                            target.getPositionVector()
                                    .addVector(Math.cos(t * .18) * 10, 6, Math.sin(t * .18) * 10),
                            2.4);
                dive(t, 110, 122, target, 1, 2.5);
                break;
            case COUNTER_THRUST:
                dive(t, 14, 28, target, 0, 2.2);
                if (t >= 34 && t < 48) fly(target.getPositionVector().addVector(-5, 4, 0), 1.1);
                dive(t, 48, 62, target, 1, 2.1);
                if (t == 72) {
                    center = target.getPositionVector();
                    anchors(1, 10);
                    fx(
                            CHAIN,
                            socket(2),
                            target.getPositionEyes(1).addVector(0, 0, 8),
                            6,
                            14,
                            .22F,
                            10);
                }
                if (t >= 76 && t < 96)
                    fly(
                            target.getPositionVector()
                                    .subtract(target.getLookVec().scale(8))
                                    .addVector(0, 4, 0),
                            2.4);
                dive(t, 96, 108, target, 0, 2.5);
                break;
            case FALLING_ARSENAL:
                if (t < 28) fly(center.addVector(0, 24, 0), 2);
                if (t == 28) start = b.getPositionVector();
                if (t >= 28 && t < 92) {
                    b.combatMove(new Vec3d(.04 * Math.sin(t * .1), -.08 - (t - 28) * .006, 0));
                    if (t % 12 == 2) burst(socket(3), target.getPositionEyes(1), 5, .75);
                    if (t == 36 || t == 60) anchors(2, 12);
                    if (t % 14 == 10) {
                        hit(0, 11);
                        wave(b.getPositionVector(), 5, 6);
                    }
                    if (t % 8 == 0) hit(1, 10);
                }
                if (t >= 92) fly(center.addVector(0, 22, 0), 2.7);
                if (t == 94) {
                    releaseAnchors(0);
                    wave(b.getPositionVector(), 11, 10);
                }
                break;
            case ARM_SACRIFICE:
                if (critical < 0) {
                    b.advanceAerial();
                    return;
                }
                if (t < 40) fly(center.addVector(critical == 1 ? 22 : 0, 18, 0), 2);
                if (t == 40) {
                    lock(target);
                    warning(locked, 16);
                }
                if (t >= 40 && t < 56 && critical == 1) fly(center.addVector(-20, 5, 0), 2.8);
                if (t == 56) {
                    Vec3d from = socket(critical);
                    if (critical == 0)
                        fx(FIST, from, locked, 0, 70, 1.8F, 20)
                                .launch(locked.subtract(from).normalize().scale(2), 20);
                    else if (critical == 1)
                        fx(CUT, from, locked, 0, 70, 2.7F, 16)
                                .launch(locked.subtract(from).normalize().scale(1.6), 16);
                    else if (critical == 2) {
                        for (int n = 0; n < 6; n++) {
                            Vec3d end = surface(circle(n * Math.PI / 3, 12, 0));
                            fx(CHAIN, from, end, 14, 36, .22F, 12);
                        }
                    } else burst(from, locked, 18, 1.2);
                    b.sacrificeArm(critical);
                    wave(surface(from), 7, 8);
                }
                break;
            case PRIMITIVE_AIR_SUPERIORITY:
                if (t < 24) fly(center.addVector(0, 26, 0), 2);
                if (t == 20) {
                    anchors(4, 20);
                    depot(12);
                }
                dive(t, 30, 42, target, 1, 2.5);
                if (t == 54) {
                    hit(0, 12);
                    fireBlade(0, target.getPositionEyes(1), 2);
                }
                if (t >= 56 && t < 76) fly(center.addVector(6, 24, 0), 2.4);
                if (t == 70) burst(socket(3), target.getPositionEyes(1), 9, 1.1);
                dive(t, 78, 90, target, 1, 2.6);
                if (t == 90)
                    for (int n = 1; n < 4; n++) fireBlade(n, target.getPositionEyes(1), 1.7);
                if (t >= 94 && t < 102) fly(center.addVector(12, 8, 0), 2.7);
                dive(t, 102, 112, target, 0, 2.7);
                dive(t, 124, 136, target, 0, 2.7);
                if (t >= 140 && t < 160) fly(center.addVector(-8, 19, 0), 2.4);
                if (t == 160) {
                    hit(1, 14);
                    dropAll();
                    releaseAnchors(0);
                }
                if (t >= 164 && t < 174) fly(center.addVector(0, 24, 0), 2.4);
                if (t >= 174 && t < 190) b.combatMove(new Vec3d(0, -.15 - (t - 174) * .04, 0));
                dive(t, 190, 202, target, 0, 2.8);
                break;
            case SURVIVING_WEAPONS:
                fly(circle(t * .06, 8, 7 + Math.sin(t * .1) * 3), 1.7);
                if (t == 24) hit(0, 13);
                if (t == 42) hit(1, 12);
                if (t == 18 && b.armHealth(2) > 0) anchors(2, 9);
                if ((t == 28 || t == 48) && b.armHealth(3) > 0)
                    burst(socket(3), target.getPositionEyes(1), 6, 1.2);
                break;
        }
        if (!paused() && t >= a.duration) {
            reset();
            b.advanceAerial();
        }
    }
}
