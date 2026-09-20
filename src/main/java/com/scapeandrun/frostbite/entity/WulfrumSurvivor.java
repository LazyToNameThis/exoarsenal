package com.scapeandrun.frostbite.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import java.util.*;

final class WulfrumSurvivor {
    private final EntityWulfrumAppendage[] sockets = new EntityWulfrumAppendage[6];
    private final Vec3d[] anchors = new Vec3d[6], predictions = new Vec3d[6];
    private final Vec3d[] poses = new Vec3d[6], aims = new Vec3d[6];
    private final boolean[] striking = new boolean[6];
    private final List<Entity> hazards = new ArrayList<>();
    private final Map<EntityWulfrumRay, int[]> links = new IdentityHashMap<>();
    private final Map<EntityWulfrumRay, Integer> outward = new IdentityHashMap<>();
    private final List<Vec3d[]> cuts = new ArrayList<>();
    private final List<List<Vec3d>> strokeHistory =
            Arrays.asList(
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>());
    private final List<EntityWulfrumRay> starLines = new ArrayList<>();
    private final Deque<Vec3d> history = new ArrayDeque<>();
    private Vec3d[] returnPath;
    private Vec3d center = Vec3d.ZERO,
            lock = Vec3d.ZERO,
            destination = Vec3d.ZERO,
            previous = Vec3d.ZERO;
    private Vec3d tetherVelocity = Vec3d.ZERO;
    private EntityWulfrumEye eye;
    private EntityLivingBase target;
    private boolean counter, punished, passHit;

    void counter() {
        counter = true;
    }

    void punish() {
        punished = true;
    }

    void beginCoordination() {
        for (Entity hazard : hazards) if (!hazard.isDead) hazard.setDead();
        hazards.clear();
        links.clear();
        outward.clear();
    }

    EntityWulfrumAppendage coordinateSocket(EntityWulfrumEye owner, int index) {
        if (sockets[index] == null || sockets[index].isDead) {
            sockets[index] = new EntityWulfrumAppendage(owner);
            owner.world.spawnEntity(sockets[index]);
        }
        sockets[index].tether(owner);
        return sockets[index];
    }

    void tick(EntityWulfrumEye e, EntityLivingBase player, int id, int t) {
        eye = e;
        target = player;
        hazards.removeIf(h -> h.isDead);
        links.keySet().removeIf(h -> h.isDead);
        if (t == 1) {
            for (Entity h : hazards) if (!(h instanceof EntityWulfrumEcho)) h.setDead();
            hazards.removeIf(h -> h.isDead);
            links.clear();
            outward.clear();
            starLines.clear();
            cuts.clear();
            history.clear();
            returnPath = null;
            counter = false;
            punished = false;
            center = player.getPositionVector().addVector(0, 1, 0);
            lock = center;
            previous = e.pupil();
            for (int i = 0; i < 6; i++) {
                anchors[i] = point(i * Math.PI / 3, 12, 1);
                if (id == 37 || id == 39) {
                    net.minecraft.util.math.RayTraceResult ground =
                            e.world.rayTraceBlocks(
                                    anchors[i].addVector(0, 12, 0),
                                    anchors[i].addVector(0, -20, 0),
                                    false,
                                    true,
                                    false);
                    if (ground != null) anchors[i] = ground.hitVec.addVector(0, .15, 0);
                }
                predictions[i] = center;
            }
        }
        for (int i = 0; i < 6; i++) {
            if (sockets[i] == null || sockets[i].isDead) {
                sockets[i] = new EntityWulfrumAppendage(e);
                e.world.spawnEntity(sockets[i]);
            }
            double a = i * Math.PI / 3 + t * .012;
            pose(i, e.pupil().addVector(Math.cos(a) * 3, Math.sin(a) * 3, 1), false);
        }
        e.look(player.getPositionVector().addVector(0, 1, 0));
        history.addLast(e.pupil());
        while (history.size() > 100) history.removeFirst();
        e.glide(e.pupil(), 0);
        if (e.seer()) seer(id - WulfrumSurvivorScore.SEER, t);
        else observer(id - WulfrumSurvivorScore.OBSERVER, t);
        for (Map.Entry<EntityWulfrumRay, int[]> entry : links.entrySet()) {
            int[] pair = entry.getValue();
            EntityWulfrumRay ray = entry.getKey();
            Vec3d end = endpoint(pair[1]);
            if (ray.winding()) {
                ray.winding((float) (pair[0] * Math.PI / 3 + (t - 280) * .14));
                ray.endpoints(e.pupil(), endpoint(-4));
            } else ray.endpoints(pair[0] >= 0 ? muzzle(pair[0], end) : endpoint(pair[0]), end);
            if (pair[0] >= 0) aims[pair[0]] = ray.winding() ? ray.path()[1] : end;
        }
        outward.keySet().removeIf(h -> h.isDead);
        for (Map.Entry<EntityWulfrumRay, Integer> entry : outward.entrySet()) {
            int i = entry.getValue();
            Vec3d origin = id == 48 ? e.pupil() : center;
            Vec3d end = at(i).add(at(i).subtract(origin).normalize().scale(32));
            entry.getKey().endpoints(muzzle(i, end), end);
            aims[i] = end;
        }
        for (int i = 0; i < 6; i++) {
            sockets[i].tether(id == 40 && i > 0 ? sockets[i - 1] : e);
            sockets[i].pose(at(i), aims[i], striking[i]);
        }
        if (!e.seer()) {
            for (EntityWulfrumRay ray : links.keySet()) e.rememberNetwork(ray);
            for (EntityWulfrumRay ray : outward.keySet()) e.rememberNetwork(ray);
        }
    }

    private void seer(int pattern, int t) {
        switch (pattern) {
            case 0:
                eye.glide(point(t * .012, 7, 2), .7);
                if (t == 25) {
                    lock = aim();
                    destination = lock.add(lock.subtract(eye.pupil()).normalize().scale(5));
                    echo(10, 42, false);
                }
                if (t >= 35 && t < 55) {
                    eye.glide(destination, 1.8);
                    eye.slash(7);
                }
                for (int i = 0; i < 6; i++) {
                    int start = 65 + (i / 2) * 36;
                    if (t >= start && t < start + 30) {
                        double f = (t - start) / 29D;
                        double[] p = WulfrumBladePaths.fencing(i, f),
                                next = WulfrumBladePaths.fencing(i, Math.min(1, f + .05));
                        pose(i, center.addVector(p[0], p[1], p[2]), f > .15 && f < .9);
                        aims[i] = center.addVector(next[0], next[1], next[2]);
                        if (t == start) {
                            Vec3d[] replay = new Vec3d[30];
                            for (int n = 0; n < 30; n++) {
                                double[] q = WulfrumBladePaths.fencing(i, n / 29D);
                                replay[n] = center.addVector(q[0], q[1], q[2]);
                            }
                            repeat(replay, 14);
                        }
                    }
                }
                if (t == 190) {
                    previous = eye.pupil();
                    lock = aim();
                    destination = lock.add(lock.subtract(previous).normalize().scale(8));
                    echo(9, 60, false);
                }
                if (t >= 200 && t < 225) {
                    eye.glide(destination, 2.1);
                    eye.contact(7);
                }
                if (t >= 225 && t < 250)
                    for (int i = 0; i < 6; i++) {
                        Vec3d end =
                                previous.addVector(
                                        Math.cos(i * Math.PI / 3) * 1.2,
                                        Math.sin(i * Math.PI / 3) * 1.2,
                                        0);
                        if (t == 225) {
                            predictions[i] = sockets[i].getPositionVector();
                            repeat(predictions[i], end, 14);
                        }
                        pose(
                                i,
                                lerp(
                                        predictions[i],
                                        end,
                                        WulfrumSurvivorScore.ease((t - 225) / 24D)),
                                true);
                        aims[i] = end;
                    }
                break;
            case 1:
                if (t < 225) eye.glide(point(t * .055, 8, 2), t < 45 ? .6 : 1.7);
                for (int i = 0; i < 6; i++) {
                    double a = i * Math.PI / 3 - t * .16;
                    pose(
                            i,
                            eye.pupil().addVector(Math.cos(a) * 5, 0, Math.sin(a) * 5),
                            t > 40 && t < 220);
                }
                if (t == 40) {
                    EntityWulfrumCut rotor =
                            new EntityWulfrumCut(eye, eye.pupil(), 2, 10, 175, 5, true).follow();
                    if (eye.world.spawnEntity(rotor)) hazards.add(rotor);
                }
                if (t >= 50 && t < 194 && t % 8 == 2) {
                    int n = (t - 50) / 8;
                    EntityWulfrumShard shard =
                            new EntityWulfrumShard(
                                            eye,
                                            at(n % 6),
                                            Vec3d.ZERO,
                                            0,
                                            200 - t + (n / 3) * 8,
                                            target,
                                            4)
                                    .gather(point(n * Math.PI / 9, 16, 1));
                    if (hazards.size() < 64 && eye.world.spawnEntity(shard)) hazards.add(shard);
                }
                if (t > 45 && t < 200) {
                    int beat = t % 42;
                    if (beat == 26) {
                        lock = aim();
                        destination = lock.add(lock.subtract(eye.pupil()).normalize().scale(6));
                    }
                    if (beat >= 26) {
                        eye.glide(destination, 2.6);
                        eye.contact(6);
                    }
                }
                if (t >= 225 && t < 245)
                    for (int i = 0; i < 6; i++)
                        pose(
                                i,
                                eye.pupil()
                                        .add(eye.getLookVec().scale(4 + (t - 225) * .35))
                                        .addVector(Math.cos(i) * 2, Math.sin(i) * 2, 0),
                                true);
                break;
            case 2:
                for (int i = 0; i < 6; i++) {
                    pose(
                            i,
                            lerp(eye.pupil(), anchors[i], WulfrumSurvivorScore.ease(t / 35D)),
                            false);
                    if (t == 15) ray(anchors[i], anchors[(i + 1) % 6], 20, 8, 0);
                }
                if (t < 45) eye.glide(center.addVector(0, 6, 0), .8);
                else if (t < 220) {
                    int q = (t - 45) % 35;
                    if (q == 0) {
                        lock = aim();
                        destination = lock.add(lock.subtract(eye.pupil()).normalize().scale(12));
                        passHit = false;
                        tetherVelocity = Vec3d.ZERO;
                    }
                    if (q == 24 && !passHit) {
                        lock = aim();
                        destination = lock.add(lock.subtract(eye.pupil()).normalize().scale(12));
                        tetherVelocity = tetherVelocity.scale(-.35);
                    }
                    if (q >= 8 && (q < 24 || !passHit)) {
                        tetherVelocity =
                                WulfrumTetherPhysics.pull(
                                        eye.pupil(),
                                        tetherVelocity,
                                        anchors,
                                        destination,
                                        q < 24 ? 4.5 : 5.2);
                        eye.motionX = tetherVelocity.x;
                        eye.motionY = tetherVelocity.y;
                        eye.motionZ = tetherVelocity.z;
                        eye.velocityChanged = true;
                        passHit |= eye.contact(7);
                    }
                } else {
                    if (t == 220) lock = aim();
                    if (t < 240) eye.glide(lock.addVector(0, 13, 0), 2);
                    else {
                        eye.glide(lock.addVector(0, -1, 0), 2.8);
                        eye.slash(9);
                    }
                    for (int i = 0; i < 6; i++)
                        pose(
                                i,
                                eye.pupil()
                                        .addVector(
                                                Math.cos(i * Math.PI / 3) * 3,
                                                2,
                                                Math.sin(i * Math.PI / 3) * 3),
                                false);
                }
                break;
            case 3:
                eye.glide(center.addVector(0, t < 185 ? 24 : 1, 0), t < 185 ? 1.3 : 2.6);
                int[] starts = {40, 72, 100, 124, 144, 160};
                for (int i = 0; i < 6; i++) {
                    Vec3d from = point(i * Math.PI / 3, 9, 2),
                            to = point(i * Math.PI / 3 + Math.PI, 9, 1);
                    int dt = t - starts[i];
                    if (dt >= 0 && dt < 22) stroke(i, from, to, dt / 22D);
                    else pose(i, from, false);
                    if (dt == 8) {
                        repeat(from, to, 12);
                        cuts.add(new Vec3d[] {from, to});
                        ray(from, to, 220 - t, 8, 5);
                    }
                }
                if (t > 195 && t < 215) eye.slash(9);
                if (t == 220) rupture();
                break;
            case 4:
                eye.glide(center.addVector(0, 5, 0), .6);
                if (t < 55) {
                    double bloomRadius = 1.2 + 5.8 * WulfrumSurvivorScore.ease((t - 45) / 10D);
                    for (int i = 0; i < 6; i++)
                        pose(
                                i,
                                eye.pupil()
                                        .addVector(
                                                Math.cos(i * Math.PI / 3) * bloomRadius,
                                                Math.sin(i * Math.PI / 3) * bloomRadius,
                                                1),
                                t >= 48);
                    if (t == 30) {
                        EntityWulfrumCut bloom =
                                new EntityWulfrumCut(eye, eye.pupil(), 7, 25, 9, 5, false)
                                        .sphere()
                                        .follow();
                        if (eye.world.spawnEntity(bloom)) hazards.add(bloom);
                    }
                } else if (t < 100) {
                    for (int i = 0; i < 6; i++)
                        pose(
                                i,
                                lerp(
                                        eye.pupil(),
                                        anchors[i],
                                        WulfrumSurvivorScore.ease((t - 55 - i * 4) / 20D)),
                                t >= 55 + i * 4);
                } else if (t < 235) {
                    int vertex = Math.min(5, (t - 100) / 22);
                    for (int i = 0; i < 6; i++) pose(i, anchors[i], false);
                    eye.glide(anchors[vertex].addVector(0, 1.5, 0), 2.8);
                    eye.contact(7);
                }
                if (t == 240)
                    for (Vec3d a : anchors)
                        eye.fragment(a, center.subtract(a).normalize().scale(1.1), 0, target, 5);
                break;
            case 5:
                if (t < 170) {
                    double a = t * .07;
                    Vec3d route =
                            t < 100
                                    ? point(a, 10 - t * .05, 2 + Math.sin(t * .04) * 3)
                                    : center.addVector(
                                            ((t - 100) / 14) % 2 == 0 ? -12 : 12,
                                            2,
                                            (t - 135) * .35);
                    eye.glide(route, 2.4);
                    eye.contact(6);
                } else {
                    if (t == 170 || returnPath == null) returnPath = history.toArray(new Vec3d[0]);
                    int n = Math.max(0, returnPath.length - 1 - Math.max(0, t - 170 - 54));
                    eye.glide(returnPath[n], 2.4);
                }
                Vec3d[] path = t >= 170 ? returnPath : history.toArray(new Vec3d[0]);
                for (int i = 0; i < 6; i++) {
                    int n =
                            t < 170
                                    ? path.length - 1 - (i + 1) * 9
                                    : path.length - 1 - Math.max(0, t - 170 - (5 - i) * 9);
                    pose(i, path[Math.max(0, n)], t > 35 && t < 265);
                }
                break;
            case 6:
                eye.glide(point(0, 8, 2), .6);
                for (int i = 0; i < 6; i++) {
                    double a = (i / 2) * Math.PI * 2 / 3;
                    pose(
                            i,
                            eye.pupil()
                                    .addVector(
                                            Math.cos(a) * 2 + (i % 2 == 0 ? -.7 : .7),
                                            Math.sin(a) * 2,
                                            i % 2 == 0 ? 2.2 : 1.2),
                            false);
                }
                if (counter && t < 100) {
                    for (int i = 2; i < 4; i++)
                        pose(i, lerp(sockets[i].getPositionVector(), aim(), .35), true);
                }
                if (t == 100) {
                    lock = aim();
                    destination = lock.add(lock.subtract(eye.pupil()).normalize().scale(7));
                }
                if (t >= 115 && t < 150) {
                    eye.glide(destination, 1.9);
                    eye.slash(8);
                    for (int i = 0; i < 6; i++)
                        pose(
                                i,
                                eye.pupil()
                                        .addVector(
                                                Math.cos(i * Math.PI / 3) * 5,
                                                Math.sin(i * Math.PI / 3) * 5,
                                                0),
                                false);
                }
                if (!punished && t == 175)
                    for (int i = 0; i < 6; i++) ray(anchors[i], anchors[(i + 1) % 6], 25, 40, 5);
                if (!punished && t >= 175 && t < 215)
                    for (int i = 0; i < 6; i++) pose(i, anchors[i], true);
                if (!punished && t >= 215 && t < 240) {
                    eye.glide(center, 2.3);
                    eye.slash(9);
                }
                break;
            case 7:
                eye.glide(point(t * .045, 9, 2), 1.4);
                eye.contact(t > 40 && t < 225 ? 5 : 0);
                if (t >= 35 && t < 205) {
                    int round = (t - 35) / 56, beat = (t - 35) % 56;
                    double radius = 9 - round * 2.2;
                    for (int i = 0; i < 6; i++) {
                        double angle = Math.PI / 2 + (i % 3 + 1) * Math.PI / 2;
                        int dt = beat - ((i % 3) * 10 + (i >= 3 ? 12 : 0));
                        Vec3d
                                from =
                                        center.addVector(
                                                Math.cos(angle) * radius,
                                                i < 3 ? 11 : -1,
                                                Math.sin(angle) * radius),
                                to =
                                        center.addVector(
                                                Math.cos(angle) * (radius - 1),
                                                i < 3 ? -1 : 11,
                                                Math.sin(angle) * (radius - 1));
                        pose(
                                i,
                                lerp(from, to, WulfrumSurvivorScore.ease(dt / 20D)),
                                dt > 3 && dt < 23);
                        if (dt == 0) ray(from, to, 8, 10, 4);
                    }
                }
                if (t == 205) {
                    lock = center.addVector(0, 1, 12);
                    ray(eye.pupil(), lock, 15, 0, 0);
                }
                if (t >= 225 && t < 250) {
                    eye.glide(lock, 2.5);
                    eye.contact(8);
                }
                break;
            case 8:
                for (int i = 0; i < 6; i++) {
                    double rotation =
                            t < 45
                                    ? WulfrumSurvivorScore.ease((t - 15) / 30D) * Math.PI * 2
                                    : Math.PI * 2;
                    pose(i, point(i * Math.PI / 3 + rotation, 12, 1), t >= 25 && t < 45);
                }
                if (t == 25)
                    for (int i = 0; i < 6; i++) {
                        EntityWulfrumRay edge =
                                new EntityWulfrumRay(
                                        eye.world,
                                        eye,
                                        anchors[i],
                                        anchors[(i + 2) % 6],
                                        200,
                                        22,
                                        5);
                        if (eye.world.spawnEntity(edge)) {
                            hazards.add(edge);
                            starLines.add(edge);
                        }
                    }
                if (t >= 225)
                    for (int i = 0; i < starLines.size(); i++) {
                        double f = WulfrumSurvivorScore.ease((t - 225) / 15D);
                        starLines
                                .get(i)
                                .endpoints(
                                        lerp(anchors[i], center, f),
                                        lerp(anchors[(i + 2) % 6], center, f));
                    }
                if (t >= 45 && t < 225) {
                    int n = (t - 45) / 30, i = WulfrumSurvivorScore.STAR[n];
                    eye.glide(anchors[i], 2.5);
                    eye.contact(6);
                    if ((t - 45) % 30 == 0) {
                        Vec3d a = eye.pupil(), b = anchors[i];
                        cuts.add(new Vec3d[] {a, b});
                        ray(a, b, 225 - t, 10, 5);
                        echo(8, 40, false);
                    }
                }
                if (t >= 225) eye.glide(center, 1.8);
                if (t == 240) {
                    rupture();
                    cut(center, 10, 8, 10, false);
                }
                break;
            case 9:
                if (t >= 20 && t < 50 && t % 4 == 0)
                    ((net.minecraft.world.WorldServer) eye.world)
                            .spawnParticle(
                                    net.minecraft.util.EnumParticleTypes.CLOUD,
                                    eye.posX,
                                    eye.posY + 1.5,
                                    eye.posZ,
                                    6,
                                    .8,
                                    .4,
                                    .8,
                                    .06);
                if (t < 40) {
                    eye.glide(center.addVector(0, 6, -12), .8);
                    for (int i = 0; i < 6; i++)
                        pose(
                                i,
                                eye.pupil()
                                        .subtract(eye.getLookVec().scale(5))
                                        .addVector(
                                                Math.cos(i * Math.PI / 3) * 2,
                                                Math.sin(i * Math.PI / 3) * 2,
                                                0),
                                false);
                    break;
                }
                if (t < 236) {
                    int leg = (t - 40) / 28, dt = (t - 40) % 28;
                    double[] p = WulfrumBladePaths.sever(leg, dt / 27D);
                    Vec3d end = center.addVector(p[0], p[1], p[2]);
                    eye.glide(end, 3.8);
                    eye.contact(7);
                    for (int i = 0; i < 6; i++) {
                        double offset = i * Math.PI / 3;
                        Vec3d tip =
                                end.addVector(
                                        Math.cos(offset) * 4,
                                        Math.sin(offset) * 4,
                                        Math.sin(dt * .16 + offset) * 3);
                        pose(i, tip, true);
                        List<Vec3d> trail = strokeHistory.get(i);
                        if (dt == 0) trail.clear();
                        trail.add(at(i));
                        if (dt == 27) {
                            Vec3d[] cutPath = trail.toArray(new Vec3d[0]);
                            cuts.add(cutPath);
                            EntityWulfrumRay cut =
                                    new EntityWulfrumRay(
                                                    eye.world,
                                                    eye,
                                                    cutPath[0],
                                                    cutPath[cutPath.length - 1],
                                                    280 - t,
                                                    8,
                                                    6)
                                            .path(cutPath);
                            if (hazards.size() < 64 && eye.world.spawnEntity(cut)) hazards.add(cut);
                        }
                    }
                    if (dt == 0) echo(6, 35, false);
                }
                if (t >= 236 && t < 280)
                    eye.glide(aim().subtract(target.getLookVec().scale(8)), 2.8);
                if (t == 280) rupture();
                if (t > 290) {
                    eye.glide(center.addVector(0, 1, 10), .2);
                    for (int i = 0; i < 6; i++)
                        pose(i, eye.pupil().addVector(Math.cos(i) * 2, -2, Math.sin(i) * 2), false);
                }
                break;
            default:
                break;
        }
    }

    private void observer(int pattern, int t) {
        eye.glide(point(t * .012, 13, 5), .65);
        switch (pattern) {
            case 0:
                for (int i = 0; i < 6; i++)
                    pose(
                            i,
                            point(
                                    i * Math.PI / 3
                                            + WulfrumSurvivorScore.ease((t - 130) / 25D)
                                                    * Math.PI
                                                    / 3,
                                    13,
                                    2),
                            false);
                if (t >= 40 && t < 130 && (t - 40) % 15 == 0)
                    fire(WulfrumSurvivorScore.STAR[(t - 40) / 15], aim(), 10, 14, 5);
                if (t >= 155 && t < 215 && (t - 155) % 10 == 0)
                    fire(WulfrumSurvivorScore.STAR[(t - 155) / 10], aim(), 8, 12, 5);
                if (t % 18 == 0 && t > 40 && t < 220) gun(aim(), 12, 4, 3);
                break;
            case 1:
                double a = t < 135 ? t * .018 : (270 - t) * .018;
                for (int i = 0; i < 6; i++)
                    pose(i, point(i % 3 * Math.PI * 2 / 3 + a, 9, i < 3 ? 8 : -.5), false);
                if (t == 30)
                    for (int i = 0; i < 6; i++) {
                        int j = i < 3 ? (i + 1) % 3 : 3 + (i - 2) % 3;
                        link(i, j, 12, 188, 4);
                        if (i < 3) link(i, i + 3, 12, 188, 4);
                    }
                if (t % 35 == 0) gun(center, 15, 6, 4);
                break;
            case 2:
                for (int i = 0; i < 6; i++) pose(i, anchors[i].addVector(0, 2, 0), false);
                if (t >= 35 && t < 185 && (t - 35) % 25 == 0) {
                    int i = (t - 35) / 25;
                    fire(i, center.add(center.subtract(at(i))), 16, 12, 5);
                    phantom(i, 40);
                }
                if (t == 215)
                    for (int i = 0; i < 6; i++) {
                        fire(i, center.add(center.subtract(at(i))), 20, 20, 6);
                        phantom(i, 50);
                    }
                break;
            case 3:
                for (int i = 0; i < 6; i++) {
                    int reversal = 75 + i * 15;
                    double spin =
                            (i % 2 == 1 && t > reversal
                                            ? reversal * .026 - (t - reversal) * .026
                                            : t * .026)
                                    + i * Math.PI / 3;
                    pose(
                            i,
                            eye.pupil()
                                    .addVector(
                                            Math.cos(spin) * (5 + i),
                                            i % 2,
                                            Math.sin(spin) * (5 + i)),
                            false);
                    if (t == 35) outward(i, 14, 186, 3, .2F);
                }
                if (t % 20 == 0) gun(aim(), 10, 5, 3);
                if (t > 170 && t < 220)
                    eye.glide(
                            center.addVector(Math.sin(t * .07) * 10, 4, Math.cos(t * .07) * 10),
                            1.6);
                break;
            case 4:
                for (int i = 0; i < 6; i++)
                    pose(
                            i,
                            point(i * Math.PI / 3 + t * .008, 10 + Math.sin(t * .035 + i) * 3, 2),
                            false);
                if (t == 35) link(-1, 0, 10, 85, 3);
                for (int i = 0; i < 6; i++)
                    if (t == 45 + i * 10) link(i, (i + 1) % 6, 10, 120 - t - 10, 4);
                if (t == 130) link(-1, 5, 8, 62, 3);
                for (int i = 0; i < 6; i++)
                    if (t == 138 + i * 8) link(5 - i, Math.floorMod(4 - i, 6), 8, 200 - t - 8, 4);
                if (t == 215) for (int i = 0; i < 6; i++) outward(i, 20, 20, 7, .8F);
                break;
            case 5:
                for (int i = 0; i < 6; i++) pose(i, anchors[i].addVector(0, 4, 0), false);
                if (t == 20 || t == 130) {
                    lock = target.getPositionVector().addVector(0, 1, 0);
                    for (int i = 0; i < 6; i++) fire(i, lock, 15, 0, 0);
                }
                if (t == 35 || t == 145) {
                    Vec3d velocity = new Vec3d(target.motionX, target.motionY, target.motionZ);
                    for (int i = 0; i < 6; i++) {
                        predictions[i] =
                                lock.add(velocity.scale(Math.min(i, 2) * 10))
                                        .addVector(i < 3 ? 0 : (i - 4) * 3, 0, i < 3 ? 0 : 3);
                        fire(i, predictions[i], 30 + i * 6, 7, 5);
                    }
                }
                if (t >= 100 && t < 130 || t >= 210 && t < 240)
                    if (t % 6 == 0) gun(center.addVector(Math.sin(t * .06) * 15, 1, 10), 10, 5, 4);
                break;
            case 6:
                for (int i = 0; i < 6; i++)
                    pose(
                            i,
                            point(
                                    i * Math.PI / 3 + t * .009,
                                    12 + Math.sin(t * .027 + i) * 3,
                                    2 + Math.sin(t * .03 + i) * 2),
                            false);
                if (t == 35) {
                    link(-1, 0, 16, 175, 6, .85F);
                    for (int n = 0; n < 5; n++)
                        link(
                                WulfrumSurvivorScore.MIRRORS[n],
                                WulfrumSurvivorScore.MIRRORS[n + 1],
                                16,
                                175,
                                6,
                                .85F);
                }
                if (t >= 65 && t < 205 && t % 28 == 9) {
                    link(-1, 0, 0, 4, 2);
                    for (int n = 0; n < 5; n++)
                        link(
                                WulfrumSurvivorScore.MIRRORS[n],
                                WulfrumSurvivorScore.MIRRORS[n + 1],
                                (n + 1) * 3,
                                4,
                                2);
                }
                break;
            case 7:
                if (t < 150) {
                    int i = Math.min(5, t / 25);
                    eye.glide(anchors[i].addVector(0, 3, 0), 2.4);
                    if (t % 25 == 20) echo((i + 1) * 14, 410 - t, true);
                    if (t % 8 == 0) gun(aim(), 8, 5, 4);
                }
                if (t >= 150 && t < 245) {
                    eye.glide(point(t * .035, 10, 5), 1.1);
                    if (t % 26 == 0) gun(aim(), 15, 8, 5);
                }
                break;
            case 8:
                Vec3d focus = endpoint(-2);
                Vec3d axis = focus.subtract(eye.pupil()).normalize(),
                        right = axis.crossProduct(new Vec3d(0, 1, 0)).normalize(),
                        up = right.crossProduct(axis).normalize();
                for (int i = 0; i < 6; i++) {
                    double angle = i * Math.PI / 3 + t * .025;
                    pose(
                            i,
                            eye.pupil()
                                    .add(right.scale(Math.cos(angle) * 4))
                                    .add(up.scale(Math.sin(angle) * 4))
                                    .subtract(axis),
                            false);
                    if (t == 30) link(i, -2, 12, 188, 3);
                }
                if (t == 30) link(-1, -2, 12, 193, 3);
                if (t == 185) link(-2, -3, 10, 40, 9, 1.2F);
                break;
            case 9:
                for (int i = 0; i < 6; i++)
                    pose(i, point(i * Math.PI / 3, t < 250 ? 22 : 8, 5), false);
                eye.glide(center.addVector(0, t > 315 ? 3 : 15, 0), t > 315 ? .4 : 1);
                if (t == 35)
                    for (int i = 0; i < 6; i++)
                        for (int lane = -1; lane <= 1; lane++)
                            fire(
                                    i,
                                    aim().addVector(lane * 4, 0, (i % 2 == 0 ? 1 : -1) * lane * 3),
                                    25 + i * 3,
                                    0,
                                    0);
                if (t == 55) echo(40, 290, true);
                if (t == 65) fire(0, aim(), 10, 10, 5);
                if (t >= 80 && t < 100 && t % 5 == 0) gun(aim(), 8, 5, 4);
                if (t == 105) fire(3, aim(), 10, 10, 5);
                if (t >= 115 && t < 135) eye.glide(point(0, 14, 5), 2.5);
                if (t == 140) {
                    fire(1, aim(), 10, 10, 5);
                    fire(4, aim(), 10, 10, 5);
                }
                if (t == 160)
                    for (int i = 0; i < 12; i++) gun(point(i * Math.PI / 6, 30, 2), 15, 8, 4);
                if (t == 180) {
                    fire(2, aim(), 10, 10, 5);
                    fire(5, aim(), 10, 10, 5);
                }
                if (t == 200) for (int i = 0; i < 6; i++) fire(i, anchors[(i + 2) % 6], 15, 20, 5);
                if (t >= 215 && t < 235) {
                    double spin = (t - 215) * Math.PI / 10;
                    eye.look(eye.pupil().addVector(Math.cos(spin) * 10, 0, Math.sin(spin) * 10));
                    if (t % 3 == 0)
                        gun(
                                eye.pupil().addVector(Math.cos(spin) * 32, 0, Math.sin(spin) * 32),
                                4,
                                5,
                                4);
                }
                if (t == 235) for (int i = 0; i < 6; i++) fire(i, aim(), 15, 12, 6);
                if (t == 260) for (int i = 0; i < 6; i++) fire(i, eye.pupil(), 10, 25, 0);
                if (t == 275) lock = aim().subtract(eye.pupil()).normalize();
                if (t >= 280 && t < 320) {
                    Vec3d side = lock.crossProduct(new Vec3d(0, 1, 0)).normalize();
                    if (side.lengthSquared() < .01) side = new Vec3d(1, 0, 0);
                    Vec3d vertical = side.crossProduct(lock).normalize();
                    for (int i = 0; i < 6; i++) {
                        double angle = i * Math.PI / 3 + (t - 280) * .14;
                        pose(
                                i,
                                eye.pupil()
                                        .add(side.scale(Math.cos(angle) * 4))
                                        .add(vertical.scale(Math.sin(angle) * 4)),
                                false);
                    }
                    if (t == 290) {
                        link(-1, -4, 8, 22, 10, 1.2F);
                        for (int i = 0; i < 6; i++) link(i, 6 + i, 8, 22, 5, .3F);
                    }
                }
                if (t >= 320)
                    for (int i = 0; i < 6; i++) {
                        pose(
                                i,
                                eye.pupil()
                                        .addVector(Math.cos(i) * 3, -2 - i * .3, Math.sin(i) * 3),
                                false);
                        if (i >= 3) {
                            aims[i] = at(i).addVector(0, -3, 0);
                            if (t % 10 == 0)
                                ((net.minecraft.world.WorldServer) eye.world)
                                        .spawnParticle(
                                                net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL,
                                                at(i).x,
                                                at(i).y,
                                                at(i).z,
                                                2,
                                                .15,
                                                .15,
                                                .15,
                                                .015);
                        }
                    }
                break;
            default:
                break;
        }
    }

    private Vec3d aim() {
        Vec3d velocity = new Vec3d(target.motionX, 0, target.motionZ);
        if (velocity.lengthVector() > .5) velocity = velocity.normalize().scale(.5);
        return target.getPositionVector().addVector(0, 1, 0).add(velocity.scale(4));
    }

    private Vec3d point(double a, double r, double y) {
        return center.addVector(Math.cos(a) * r, y, Math.sin(a) * r);
    }

    private static Vec3d lerp(Vec3d a, Vec3d b, double f) {
        return a.add(b.subtract(a).scale(Math.max(0, Math.min(1, f))));
    }

    private Vec3d at(int i) {
        Vec3d old = sockets[i].getPositionVector(), delta = poses[i].subtract(old);
        double speed = eye.attack() == 44 ? 5 : 3;
        return old.add(delta.scale(Math.min(1, speed / Math.max(.001, delta.lengthVector()))));
    }

    private void pose(int i, Vec3d p, boolean hit) {
        poses[i] = p;
        aims[i] = aim();
        striking[i] = hit;
    }

    private void stroke(int i, Vec3d a, Vec3d b, double f) {
        pose(i, lerp(a, b, WulfrumSurvivorScore.ease(f)), f > .25 && f < .8);
    }

    private void ray(Vec3d a, Vec3d b, int warning, int life, float damage) {
        if (hazards.size() >= 64) return;
        EntityWulfrumRay r = new EntityWulfrumRay(eye.world, eye, a, b, warning, life, damage);
        if (eye.world.spawnEntity(r)) hazards.add(r);
    }

    private Vec3d endpoint(int socket) {
        if (socket == -1) return eye.pupil();
        if (socket == -2) {
            double[] p = WulfrumSurvivorScore.focus(eye.attackTick());
            return eye.pupil().addVector(p[0], p[1], p[2]);
        }
        if (socket == -3) {
            Vec3d focus = endpoint(-2);
            return focus.add(focus.subtract(eye.pupil()).normalize().scale(45));
        }
        if (socket == -4) return eye.pupil().add(lock.scale(55));
        if (socket >= 6) return at(socket - 6).add(lock.scale(55));
        return at(socket);
    }

    private Vec3d muzzle(int i, Vec3d target) {
        Vec3d p = at(i);
        return p.add(target.subtract(p).normalize().scale(.93));
    }

    private void link(int from, int to, int warning, int life, float damage) {
        if (hazards.size() >= 64) return;
        Vec3d end = endpoint(to);
        EntityWulfrumRay r =
                new EntityWulfrumRay(
                        eye.world,
                        eye,
                        from >= 0 ? muzzle(from, end) : endpoint(from),
                        end,
                        warning,
                        life,
                        damage);
        if (eye.world.spawnEntity(r)) {
            hazards.add(r);
            links.put(r, new int[] {from, to});
            if (from >= 0) sockets[from].charge(warning, life);
        }
    }

    private void link(int from, int to, int warning, int life, float damage, float width) {
        if (hazards.size() >= 64) return;
        Vec3d end = endpoint(to);
        EntityWulfrumRay r =
                new EntityWulfrumRay(
                                eye.world,
                                eye,
                                from >= 0 ? muzzle(from, end) : endpoint(from),
                                end,
                                warning,
                                life,
                                damage)
                        .charged(width, false);
        if (to >= 6) {
            r.winding((float) (from * Math.PI / 3 + (eye.attackTick() - 280) * .14));
            r.endpoints(eye.pupil(), endpoint(-4));
        }
        if (eye.world.spawnEntity(r)) {
            hazards.add(r);
            links.put(r, new int[] {from, to});
            if (from >= 0) sockets[from].charge(warning, life);
        }
    }

    private void outward(int i, int warning, int life, float damage, float width) {
        Vec3d end = at(i).add(at(i).subtract(center).normalize().scale(32));
        EntityWulfrumRay r =
                new EntityWulfrumRay(eye.world, eye, muzzle(i, end), end, warning, life, damage)
                        .charged(width, false);
        if (hazards.size() < 64 && eye.world.spawnEntity(r)) {
            hazards.add(r);
            outward.put(r, i);
            sockets[i].charge(warning, life);
        }
    }

    private void fire(int i, Vec3d to, int warning, int life, float damage) {
        aims[i] = to;
        striking[i] = true;
        sockets[i].charge(warning, life);
        Vec3d from = muzzle(i, to);
        if (damage > 0) eye.rememberBeam(from, to, warning, life, damage);
        ray(from, to, warning, life, damage);
    }

    private void gun(Vec3d to, int warning, int life, float damage) {
        eye.rememberBeam(eye.pupil(), to, warning, life, damage);
        ray(eye.pupil(), to, warning, life, damage);
    }

    private void deathray(Vec3d from, Vec3d to, int warning, int life, float damage) {
        if (hazards.size() >= 64) return;
        EntityWulfrumRay r =
                new EntityWulfrumRay(eye.world, eye, from, to, warning, life, damage)
                        .charged(1.2F, true);
        if (eye.world.spawnEntity(r)) hazards.add(r);
    }

    private void repeat(Vec3d a, Vec3d b, int delay) {
        if (hazards.size() >= 64) return;
        EntityWulfrumAppendage e = EntityWulfrumAppendage.replay(eye, a, b, delay);
        if (eye.world.spawnEntity(e)) hazards.add(e);
    }

    private void repeat(Vec3d[] path, int delay) {
        if (hazards.size() >= 64) return;
        EntityWulfrumAppendage e = EntityWulfrumAppendage.replay(eye, path, delay);
        if (eye.world.spawnEntity(e)) hazards.add(e);
    }

    private void cut(Vec3d at, float r, int warning, int life, boolean saw) {
        if (hazards.size() >= 64) return;
        EntityWulfrumCut c = new EntityWulfrumCut(eye, at, r, warning, life, 5, saw);
        if (eye.world.spawnEntity(c)) hazards.add(c);
    }

    private void echo(int delay, int life, boolean laser) {
        if (hazards.size() >= 64) return;
        EntityWulfrumEcho e = new EntityWulfrumEcho(eye, delay, life, laser);
        if (eye.world.spawnEntity(e)) hazards.add(e);
    }

    private void phantom(int i, int life) {
        if (hazards.size() >= 64) return;
        Vec3d from = at(i), to = center.add(center.subtract(from));
        EntityWulfrumEcho e = EntityWulfrumEcho.phantom(eye, from, to, 20, life);
        if (eye.world.spawnEntity(e)) hazards.add(e);
    }

    private void rupture() {
        int perLine = Math.max(1, Math.min(4, 48 / Math.max(1, cuts.size())));
        for (Vec3d[] line : cuts)
            for (int i = 1; i <= perLine; i++) {
                double f = i * (line.length - 1) / (double) (perLine + 1);
                int n = Math.min(line.length - 2, (int) f);
                Vec3d from = lerp(line[n], line[n + 1], f - n);
                eye.fragment(from, aim().subtract(from).normalize().scale(1.1), 0, target, 4);
            }
    }
}
