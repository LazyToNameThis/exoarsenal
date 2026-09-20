package com.scapeandrun.frostbite.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.List;
import static com.scapeandrun.frostbite.entity.WulfrumDuetScore.*;

final class WulfrumDuet {
    private Vec3d center = Vec3d.ZERO, lock = Vec3d.ZERO, dashEnd = Vec3d.ZERO;
    private Vec3d railDirection = new Vec3d(1, 0, 0);
    private Vec3d tetherVelocity = Vec3d.ZERO;
    private final java.util.Map<EntityWulfrumRay, EntityWulfrumCut> sawFeeds =
            new java.util.HashMap<>();
    private final List<EntityWulfrumRay> beams = new ArrayList<>();
    private final List<EntityWulfrumCut> cuts = new ArrayList<>();
    private final List<EntityWulfrumRay> loom = new ArrayList<>();
    private final List<EntityWulfrumRay> feeds = new ArrayList<>();
    private final List<EntityWulfrumShard> cloud = new ArrayList<>();
    private EntityWulfrumRay heldBeam;
    private EntityWulfrumRay splitLeft, splitRight;
    private boolean impact;
    private int clockOverride = -1, relayWait, railLeg;

    int clockOverride() {
        return clockOverride;
    }

    private final List<Vec3d> marks = new ArrayList<>();
    private EntityWulfrumEye owner;
    private EntityLivingBase target;

    void tick(
            EntityWulfrumEye conductor,
            EntityWulfrumEye s,
            EntityWulfrumEye o,
            EntityLivingBase player,
            int id,
            int t) {
        owner = conductor;
        target = player;
        clockOverride = -1;
        if (t == 1) {
            relayWait = 0;
            railLeg = 0;
            tetherVelocity = Vec3d.ZERO;
            sawFeeds.clear();
        }
        if (t == 1) {
            for (EntityWulfrumShard shard : cloud) if (shard.held()) shard.setDead();
            cloud.clear();
        }
        if (t == 1) {
            for (EntityWulfrumRay b : beams) b.setDead();
            for (EntityWulfrumCut c : cuts) c.setDead();
            cuts.clear();
            beams.clear();
            loom.clear();
            feeds.clear();
            heldBeam = null;
            splitLeft = null;
            splitRight = null;
            impact = false;
            marks.clear();
            center = player.getPositionVector().addVector(0, 1, 0);
            lock = center;
        }
        beams.removeIf(b -> b.isDead);
        cuts.removeIf(c -> c.isDead);
        if (s != null) s.look(player.getPositionVector().addVector(0, 1, 0));
        if (o != null) o.look(player.getPositionVector().addVector(0, 1, 0));
        if (id == SEER_FINAL || id == OBSERVER_FINAL) {
            survivor(conductor, t);
            return;
        }
        if (s == null || o == null) return;
        if (id != RAILSHOT || t >= 200) o.chainAnchor(null);
        double a = t * .035;
        switch (id) {
            case FENCER:
                Vec3d victim = target.getPositionVector().addVector(0, 1, 0),
                        back = target.getLookVec().scale(-12);
                o.glide(victim.add(back).addVector(0, 3, 0), 1.2);
                int fencing = t % 96;
                s.look(victim);
                if (t < 192) {
                    if (fencing < 10 || fencing >= 76 && fencing < 88) {
                        s.glide(victim, 1.9);
                        s.slash(6);
                    } else if (fencing >= 26 && fencing < 38) {
                        Vec3d side = s.pupil().subtract(victim).normalize().rotateYaw(.25F);
                        s.glide(victim.add(side.scale(4)), 1.7);
                        s.slash(6);
                    } else if (fencing >= 46 && fencing < 58)
                        s.glide(victim.add(s.pupil().subtract(victim).normalize().scale(9)), 1.6);
                    else s.glide(s.pupil(), 0);
                    if (fencing == 12 || fencing == 40 || fencing == 60 || fencing == 66) {
                        EntityWulfrumRay feed =
                                persistent(
                                        o.pupil(),
                                        s.pupil().add(s.getLookVec().scale(3)),
                                        4,
                                        8,
                                        3,
                                        .12F);
                        feeds.add(feed);
                    }
                }
                if (t == 202)
                    heldBeam =
                            persistent(
                                    o.pupil(),
                                    s.pupil().add(s.getLookVec().scale(3)),
                                    10,
                                    62,
                                    4,
                                    .65F);
                if (t >= 202 && t < 244) {
                    s.glide(victim.addVector(Math.sin(t * .15) * 5, 1, Math.cos(t * .15) * 5), 1.8);
                    s.slash(6);
                    if (heldBeam != null)
                        heldBeam.endpoints(o.pupil(), s.pupil().add(s.getLookVec().scale(3)));
                }
                if (t == 244) {
                    if (heldBeam != null) heldBeam.setDead();
                    splitLeft = persistent(s.pupil(), victim, 0, 28, 8, 1.1F);
                }
                if (t >= 244 && splitLeft != null) {
                    double sweep = (t - 244) * Math.PI / 14;
                    splitLeft.endpoints(
                            s.pupil(),
                            s.pupil().addVector(Math.cos(sweep) * 30, 0, Math.sin(sweep) * 30));
                }
                for (EntityWulfrumRay feed : feeds)
                    if (!feed.isDead) {
                        feed.endpoints(o.pupil(), s.pupil().add(s.getLookVec().scale(3)));
                        if (feed.firing() && bladeTouches(s, feed)) {
                            Vec3d point = feed.end(),
                                    direction = victim.subtract(point).normalize();
                            beam(point, point.add(direction.scale(30)), 0, 8, 5);
                            feed.setDead();
                        }
                    }
                break;
            case RAILSHOT:
                if (t < 40) {
                    o.glide(center.addVector(0, 5, -12), .8);
                    s.glide(o.getPositionVector().addVector(0, 0, 3), 1);
                    if (t == 25) {
                        lock = target.getPositionVector().addVector(0, 1, 0);
                        beam(s.pupil(), lock, 15, 0, 0);
                    }
                } else if (t < 200) {
                    if (t == 40) {
                        Vec3d toward = lock.subtract(s.pupil());
                        railDirection = new Vec3d(toward.x, 0, toward.z).normalize();
                        dashEnd = s.pupil().add(railDirection.scale(24));
                    }
                    o.chainAnchor(dashEnd);
                    s.look(s.pupil().add(railDirection));
                    s.glide(dashEnd, 3.8 + railLeg * .35);
                    s.contact(7);
                    o.glide(center.addVector(0, 6, 0), 1);
                    if (s.pupil().squareDistanceTo(dashEnd) < 4) {
                        railLeg++;
                        fan(s.pupil(), railDirection, 7, .14, 5);
                        railDirection = railDirection.rotateYaw((float) Math.PI / 2);
                        dashEnd = s.pupil().add(railDirection.scale(24));
                        if (railLeg >= 4) clockOverride = 199;
                    }
                    if (t >= 198 && railLeg < 4)
                        clockOverride = ++relayWait < 100 ? 197 : duration(RAILSHOT);
                } else {
                    double circle = (t - 200) * Math.PI / 25;
                    s.glide(
                            o.pupil().addVector(Math.cos(circle) * 4, 0, Math.sin(circle) * 4),
                            3.5);
                    s.slash(7);
                    if (t == 224) ring(o.pupil(), 5, 0, 18, 7);
                }
                break;
            case LOOM:
                o.glide(orbit(a, 11, 5), .9);
                s.glide(orbit(-a, 7, 2), 1.1);
                if (t == 20)
                    for (int i = 0; i < 6; i++) {
                        Vec3d p = orbit(i * Math.PI / 3, 17, 0);
                        marks.add(p);
                        beam(o.pupil(), p, 20, 185, 4);
                        loom.add(beams.get(beams.size() - 1));
                    }
                for (int i = 0; i < loom.size(); i++)
                    if (!loom.get(i).isDead) loom.get(i).endpoints(o.pupil(), marks.get(i));
                if (t >= 50 && t < 200) {
                    for (EntityWulfrumRay wire : loom)
                        if (!wire.isDead) {
                            Vec3d mid = wire.getPositionVector().add(wire.end()).scale(.5);
                            s.look(mid);
                            s.glide(mid, 2.8);
                            if (bladeTouches(s, wire)) {
                                Vec3d direction =
                                        wire.end().subtract(wire.getPositionVector()).normalize();
                                fan(mid, direction, 4, .08, 4);
                                wire.setDead();
                            }
                            break;
                        }
                }
                s.slash(5);
                break;
            case RELAY:
                if (t < 35) {
                    o.glide(center.addVector(0, 4, 0), 1.8);
                    double swing = t * Math.PI / 20;
                    s.glide(o.pupil().addVector(Math.cos(swing) * 9, 0, Math.sin(swing) * 9), 3.2);
                    s.contact(6);
                } else if (t < 235) {
                    int leg = (t - 35) / 40, beat = (t - 35) % 40;
                    if (beat == 0) {
                        lock = target.getPositionVector().addVector(0, 1, 0);
                        Vec3d direction = lock.subtract(s.pupil()).normalize();
                        dashEnd = lock.add(direction.scale(11));
                        railDirection = direction;
                    }

                    o.glide(dashEnd.addVector(0, 2, 0), 4.1 + leg * .2);
                    if (beat < 28) {
                        s.glide(dashEnd, 3.2 + leg * .35);
                        s.contact(6);
                    } else {
                        s.glide(o.pupil().subtract(railDirection.scale(3)), 3.8 + leg * .35);
                        if (beat == 28 && s.pupil().squareDistanceTo(o.pupil()) > 25) {

                            clockOverride = ++relayWait < 80 ? t - 1 : duration(RELAY);
                            break;
                        }
                        relayWait = 0;
                        o.chainAnchor(s.pupil());
                        s.slash(6);
                        if (beat == 29) fan(s.pupil(), railDirection, 3, .25, 4);
                    }
                } else if (t < 283) {
                    int beat = (t - 235) % 16;
                    if (beat == 0) {
                        lock = target.getPositionVector().addVector(0, 1, 0);
                        dashEnd = lock.add(lock.subtract(s.pupil()).normalize().scale(8));
                    }
                    s.look(dashEnd);
                    s.glide(dashEnd, 4.5);
                    s.slash(7);
                    o.glide(orbit(-a, 12, 4), 1.8);
                } else if (t < 295) {
                    s.glide(s.pupil(), 0);
                    o.glide(o.pupil(), 0);
                    o.chainAnchor(
                            o.pupil().add(s.pupil().subtract(o.pupil()).scale((295 - t) / 12D)));
                    if (t == 294) {
                        center = s.pupil().add(o.pupil()).scale(.5);
                        lock = o.pupil();
                        dashEnd = s.pupil();
                    }
                } else {
                    s.glide(lock.add(lock.subtract(center).normalize().scale(10)), 4.8);
                    o.glide(dashEnd.add(dashEnd.subtract(center).normalize().scale(10)), 4.8);
                    s.contact(7);
                    o.contact(7);
                    if (t == 301) {
                        beam(center.addVector(-18, 0, -18), center.addVector(18, 0, 18), 4, 10, 7);
                        beam(center.addVector(-18, 0, 18), center.addVector(18, 0, -18), 4, 10, 7);
                    }
                }
                break;
            case CROSS_EYED:
                s.glide(orbit(a, 11, 1), 1);
                o.glide(orbit(a + Math.PI, 11, 4), 1);
                if (t < 180 && t % 24 == 0) {
                    Vec3d p = s.pupil(), d = p.subtract(o.pupil()).normalize();
                    beam(o.pupil(), p, 4, 16, 4);
                    Vec3d q = p.addVector(-d.z * 18, 0, d.x * 18);
                    loom.add(persistent(p, q, 6, 210 - t, 4, .18F));
                }
                if (t < 180)
                    for (EntityWulfrumRay edge : loom)
                        if (!edge.isDead && edge.ticksExisted > 8 && bladeTouches(s, edge))
                            edge.setDead();
                if (t == 200) {
                    for (EntityWulfrumRay edge : loom)
                        if (!edge.isDead) {
                            Vec3d d = edge.end().subtract(edge.getPositionVector()),
                                    normal = new Vec3d(-d.z, 0, d.x).normalize();
                            double sign =
                                    target.getPositionVector()
                                                            .subtract(edge.getPositionVector())
                                                            .dotProduct(normal)
                                                    >= 0
                                            ? 1
                                            : -1;
                            for (int i = 1; i <= 4; i++) {
                                Vec3d p = edge.getPositionVector().add(d.scale(i / 5D));
                                EntityWulfrumShard shard =
                                        new EntityWulfrumShard(
                                                        owner, p, Vec3d.ZERO, 0, 16, target, 5)
                                                .launchAfter(normal.scale(sign * 1.7));
                                owner.world.spawnEntity(shard);
                            }
                            edge.setDead();
                        }
                    ring(s.pupil(), 14, 0, 8, 6);
                }
                if (t > 200) {
                    s.glide(center, 1.8);
                    s.slash(6);
                }
                break;
            case PINCER:
                int pass = Math.min(2, t / 72), pt = t % 72;
                Vec3d axis =
                        pass == 0
                                ? new Vec3d(1, 0, 0)
                                : pass == 1 ? new Vec3d(0, 1, 0) : new Vec3d(1, 1, 0).normalize();
                if (pt < 22) {
                    lock = target.getPositionVector().addVector(0, 1, 0);
                    s.glide(lock.add(axis.scale(15)), 2);
                    o.glide(lock.subtract(axis.scale(15)), 2);
                } else if (pt < 34) {
                    s.glide(lock.subtract(axis.scale(2)), 3.4);
                    o.glide(lock.add(axis.scale(2)), 3.4);
                    s.contact(6);
                    o.contact(6);
                } else {
                    if (pt == 34) lock = target.getPositionVector().addVector(0, 1, 0);
                    double spin = (pt - 34) * .18;
                    Vec3d radial =
                            axis.scale(Math.cos(spin) * 5).addVector(0, 0, Math.sin(spin) * 5);
                    s.glide(lock.add(radial), 3.2);
                    o.glide(lock.subtract(radial), 3.2);
                    s.look(lock);
                    s.slash(6);
                    if (pt % 8 == 0) beam(o.pupil(), lock, 3, 6, 4);
                }
                break;
            case FEEDBACK:
                s.glide(orbit(a, 9, 3), 1);
                o.glide(orbit(a + Math.PI, 9, 3), 1);
                if (t >= 30 && t < 174 && (t - 30) % 24 == 0) {
                    if (heldBeam != null) heldBeam.setDead();
                    int exchange = (t - 30) / 24;
                    heldBeam =
                            persistent(
                                    exchange % 2 == 0 ? o.pupil() : s.pupil(),
                                    exchange % 2 == 0 ? s.pupil() : o.pupil(),
                                    4,
                                    25,
                                    5 + exchange * .3F,
                                    .15F + exchange * .12F);
                }
                if (t < 190 && heldBeam != null) {
                    int exchange = Math.max(0, (t - 30) / 24);
                    heldBeam.endpoints(
                            exchange % 2 == 0 ? o.pupil() : s.pupil(),
                            exchange % 2 == 0 ? s.pupil() : o.pupil());
                }
                if (t == 190) {
                    if (heldBeam != null) heldBeam.setDead();
                    for (int i = 0; i < 24; i++) {
                        Vec3d p = o.pupil().add(s.pupil().subtract(o.pupil()).scale(i / 23D));
                        EntityWulfrumShard shard =
                                new EntityWulfrumShard(owner, p, Vec3d.ZERO, 0, 1, target, 4)
                                        .hold();
                        owner.world.spawnEntity(shard);
                        cloud.add(shard);
                    }
                    heldBeam = persistent(o.pupil(), s.pupil(), 6, 26, 3, 1.1F);
                }
                if (t > 190 && t < 218 && heldBeam != null && heldBeam.firing())
                    for (EntityWulfrumShard shard : cloud)
                        if (shard.held()
                                && segmentDistance(
                                                shard.getPositionVector(),
                                                heldBeam.getPositionVector(),
                                                heldBeam.end())
                                        < 1.3)
                            shard.launch(
                                    target.getPositionVector()
                                            .addVector(0, 1, 0)
                                            .subtract(shard.getPositionVector())
                                            .normalize()
                                            .scale(.6));
                if (t >= 218) {
                    dash(s, t - 218, 25, 3.4);
                    for (EntityWulfrumShard shard : cloud)
                        if (!shard.isDead && shard.getDistanceSq(s) < 12)
                            shard.launch(
                                    shard.getPositionVector()
                                            .subtract(s.pupil())
                                            .normalize()
                                            .scale(2));
                }
                break;
            case EXECUTION:
                dash(s, t, 42, 2);
                o.glide(orbit(-a, 13, 3), .8);
                if (t % 42 == 25) {
                    EntityWulfrumCut trail =
                            new EntityWulfrumCut(owner, s.pupil(), 3, 8, 70, 4, false)
                                    .plane((t / 42) % 3);
                    if (owner.world.spawnEntity(trail)) cuts.add(trail);
                }
                if (t % 18 == 0) {
                    Vec3d end = target.getPositionVector().addVector(Math.sin(t * .1) * 5, 1, 0);
                    EntityWulfrumCut crossing = null;
                    double nearest = Double.MAX_VALUE;
                    for (EntityWulfrumCut trail : cuts)
                        if (!trail.isDead
                                && trail.active()
                                && segmentDistance(trail.getPositionVector(), o.pupil(), end)
                                        < trail.radius()) {
                            double distance = o.pupil().squareDistanceTo(trail.getPositionVector());
                            if (distance < nearest) {
                                nearest = distance;
                                crossing = trail;
                            }
                        }
                    if (crossing == null) beam(o.pupil(), end, 9, 6, 3);
                    else {
                        Vec3d p = crossing.getPositionVector(),
                                d = end.subtract(o.pupil()).normalize();
                        beam(o.pupil(), p, 9, 6, 3);
                        for (int i = -1; i <= 1; i++)
                            beam(p, p.add(d.rotateYaw(i * .3F).scale(18)), 9, 6, 3);
                    }
                }
                break;
            case SANDER:
                if (t == 1) {
                    railDirection =
                            new Vec3d(target.getLookVec().x, 0, target.getLookVec().z).normalize();
                    if (railDirection.lengthSquared() < .01) railDirection = new Vec3d(0, 0, 1);
                }
                if (t < 28) {
                    o.glide(center.subtract(railDirection.scale(10)).addVector(0, 6, 0), 2.4);
                    Vec3d reel =
                            ground(WulfrumSawMechanics.trailingPoint(o.pupil(), railDirection, 5))
                                    .addVector(0, 1.45, 0);
                    s.look(reel.add(railDirection));
                    s.glide(reel, 3.4);
                } else if (t < 154) {

                    double advance = (t - 28) * .42 - 10;
                    Vec3d route =
                            center.add(railDirection.scale(advance))
                                    .add(
                                            railDirection
                                                    .rotateYaw((float) Math.PI / 2)
                                                    .scale(Math.sin((t - 28) * .032) * 5));
                    o.glide(ground(route).addVector(0, 7, 0), 2.8);
                    o.look(o.pupil().add(railDirection.scale(20)));
                    Vec3d trail =
                            ground(WulfrumSawMechanics.trailingPoint(o.pupil(), railDirection, 7));
                    s.glide(trail.addVector(0, 1.45, 0), 3.1);
                    s.look(s.pupil().add(railDirection));
                    s.contact(7);
                    Vec3d surface = ground(s.pupil());
                    if (t % 6 == 0 && Math.abs(s.pupil().y - surface.y - 1.45) < 1.2) {
                        Vec3d debris = railDirection.scale(-1).addVector(0, .75, 0);
                        fan(surface.addVector(0, .5, 0), debris, 5, .18, 3);
                        net.minecraft.util.math.BlockPos block =
                                new net.minecraft.util.math.BlockPos(surface.addVector(0, -.1, 0));
                        ((net.minecraft.world.WorldServer) owner.world)
                                .spawnParticle(
                                        net.minecraft.util.EnumParticleTypes.BLOCK_CRACK,
                                        surface.x,
                                        surface.y + .1,
                                        surface.z,
                                        22,
                                        .7,
                                        .25,
                                        .7,
                                        .25,
                                        net.minecraft.block.Block.getStateId(
                                                owner.world.getBlockState(block)));
                        ((net.minecraft.world.WorldServer) owner.world)
                                .spawnParticle(
                                        net.minecraft.util.EnumParticleTypes.CLOUD,
                                        surface.x - railDirection.x * 2,
                                        surface.y + .7,
                                        surface.z - railDirection.z * 2,
                                        7,
                                        .7,
                                        .4,
                                        .7,
                                        .12);
                    }
                    if (t % 5 == 0)
                        beam(
                                o.pupil(),
                                o.pupil().add(railDirection.scale(30)).addVector(0, -4, 0),
                                4,
                                4,
                                4);
                } else if (t < 184) {
                    if (t == 154) {
                        lock = target.getPositionVector().addVector(0, 1, 0);
                        dashEnd = o.pupil().addVector(0, 14, 0);
                        tetherVelocity = new Vec3d(s.motionX, s.motionY, s.motionZ);
                    }
                    o.glide(dashEnd, 3.4);
                    tetherVelocity =
                            WulfrumTetherPhysics.pull(
                                    s.pupil(),
                                    tetherVelocity,
                                    new Vec3d[] {o.pupil()},
                                    o.pupil(),
                                    4.8);
                    s.glide(s.pupil().add(tetherVelocity.scale(3)), tetherVelocity.lengthVector());
                    s.look(lock);
                    s.contact(8);
                } else {
                    if (t == 184) {
                        railDirection = lock.subtract(s.pupil()).normalize();
                        dashEnd = lock.add(railDirection.scale(16));
                    }
                    s.look(dashEnd);
                    s.glide(dashEnd, 5.2);
                    s.contact(8);
                    o.glide(o.pupil().addVector(0, 1, 0), .3);
                }
                break;
            case SUPPRESS:
                o.glide(center.addVector(0, 6, -14), .7);
                int beat = t / 40;
                double side = beat % 2 == 0 ? 1 : -1;
                if (t % 40 < 16) {
                    lock = target.getPositionVector().addVector(side * 5, 1, 0);
                    s.glide(center.addVector(side * 14, 2, 0), 1.2);
                } else {
                    s.glide(lock, 2);
                    s.contact(6);
                }
                if (t % 8 == 0) {
                    Vec3d end =
                            center.addVector(
                                    -side * (12 - (t % 40) * .6),
                                    beat >= 4 ? (t % 40) * .25 : 1,
                                    14);
                    beam(o.pupil(), end, 8, 5, 4);
                }
                break;
            case WOODCHIPPER:
                o.glide(center.addVector(0, 5, 0), .8);
                s.glide(orbit(a * 2, 10, 1), 1.8);
                s.contact(6);
                if (t >= 20 && t < 92 && (t - 20) % 3 == 0) {
                    Vec3d p = orbit((t - 20) / 3 * Math.PI / 12, 10, 1);
                    EntityWulfrumShard shard =
                            new EntityWulfrumShard(owner, s.pupil(), Vec3d.ZERO, 0, 1, target, 4)
                                    .gather(p)
                                    .hold();
                    owner.world.spawnEntity(shard);
                    cloud.add(shard);
                }
                if (t >= 95 && t < 203 && (t - 95) % 18 == 0) {
                    int first = (t - 95) / 18 * 3;
                    for (int i = first; i < first + 3 && i < cloud.size(); i++)
                        beam(o.pupil(), cloud.get(i).getPositionVector(), 4, 5, 3);
                }
                for (EntityWulfrumShard shard : cloud)
                    if (shard.held() && !shard.isDead)
                        for (EntityWulfrumRay ray : beams)
                            if (ray.firing()
                                    && segmentDistance(
                                                    shard.getPositionVector(),
                                                    ray.getPositionVector(),
                                                    ray.end())
                                            < .6) {
                                shard.launch(
                                        target.getPositionVector()
                                                .addVector(0, 1, 0)
                                                .subtract(shard.getPositionVector())
                                                .normalize()
                                                .scale(1.7));
                                break;
                            }
                if (t >= 220) {
                    s.glide(orbit((t - 220) * .3, 10, 1), 3.5);
                    for (EntityWulfrumShard shard : cloud)
                        if (shard.held() && (shard.getDistanceSq(s) < 16 || t == 244))
                            shard.launch(
                                    shard.getPositionVector()
                                            .subtract(center)
                                            .normalize()
                                            .scale(1.8));
                }
                break;
            case RICOCHET_GUN:
                s.glide(orbit(a, 8, 2), 1);
                o.glide(center.addVector(0, 6, -14), .7);
                if (t > 30 && t < 180 && t % 4 == 0)
                    feeds.add(
                            persistent(
                                    o.pupil(),
                                    s.pupil().add(s.getLookVec().scale(2)),
                                    3,
                                    5,
                                    3,
                                    .12F));
                for (EntityWulfrumRay pulse : feeds)
                    if (!pulse.isDead && pulse.firing() && bladeTouches(s, pulse)) {
                        Vec3d hit = pulse.end(),
                                incoming = hit.subtract(pulse.getPositionVector()).normalize(),
                                aim =
                                        target.getPositionVector()
                                                .addVector(0, 1, 0)
                                                .subtract(hit)
                                                .normalize();
                        Vec3d normal = incoming.subtract(aim).normalize();
                        Vec3d reflected =
                                ExcavatorGeometry.reflected(incoming, normal)
                                        .rotateYaw((float) (Math.sin(t * .31) * .2));
                        beam(hit, hit.add(reflected.scale(26)), 0, 5, 3);
                        pulse.setDead();
                    }
                if (t == 190) {
                    heldBeam = persistent(o.pupil(), s.pupil(), 8, 42, 4, .3F);
                    splitLeft = persistent(s.pupil(), center, 8, 42, 6, .75F);
                }
                if (t >= 190 && heldBeam != null) {
                    heldBeam.endpoints(o.pupil(), s.pupil());
                    splitLeft.endpoints(
                            s.pupil(), center.addVector(Math.sin((t - 190) * .035) * 23, 1, 18));
                }
                break;
            case DRIVE:
                if (t < 32) {
                    o.glide(orbit(0, 8, 5), 2);
                    s.glide(orbit(Math.PI, 11, 2), 2);
                } else if (t < 208) {
                    int beltBeat = (t - 32) % 44, leg = (t - 32) / 44;
                    if (beltBeat == 0) {
                        lock = target.getPositionVector().addVector(0, 1, 0);
                        dashEnd =
                                lock.addVector(
                                        Math.cos(leg * Math.PI * .65) * 12,
                                        5,
                                        Math.sin(leg * Math.PI * .65) * 12);
                    }
                    o.glide(dashEnd, beltBeat < 12 ? 4.8 : 1.2);
                    Vec3d desired =
                            lock.addVector(Math.cos(t * .10) * 10, 2, Math.sin(t * .10) * 10);
                    tetherVelocity =
                            WulfrumSawMechanics.beltVelocity(
                                    s.pupil(), tetherVelocity, o.pupil(), desired, 12, 4.5);
                    s.glide(s.pupil().add(tetherVelocity.scale(3)), tetherVelocity.lengthVector());
                    s.look(lock);
                    s.contact(6);
                    if (heldBeam == null)
                        heldBeam = persistent(o.pupil(), s.pupil(), 10, 190, 5, .2F);
                    heldBeam.endpoints(
                            o.pupil().subtract(o.getLookVec().scale(1.5)),
                            s.pupil().subtract(s.getLookVec().scale(1.5)));
                    if (beltBeat >= 18 && beltBeat < 36 && beltBeat % 4 == 2)
                        beam(o.pupil(), lock.addVector((beltBeat - 26) * .7, 0, 0), 5, 4, 4);
                } else {
                    if (t == 208) {
                        if (heldBeam != null) heldBeam.setDead();
                        Vec3d tangent =
                                WulfrumSawMechanics.tangent(o.pupil(), s.pupil(), tetherVelocity);
                        lock = s.pupil().add(tangent.scale(45));
                        fan(s.pupil(), tangent.scale(-1), 6, .3, 3);
                    }
                    s.look(lock);
                    s.glide(lock, 6.5);
                    s.contact(8);
                    o.glide(o.pupil(), 0);
                }
                break;
            case CUT_BEAM:
                o.glide(center.addVector(0, 4, -16), .8);
                s.glide(center.addVector(Math.sin(t * .045) * 8, 2 + Math.sin(t * .027) * 4, 0), 1);
                if (t == 35) {
                    heldBeam = persistent(o.pupil(), s.pupil(), 12, 190, 4, .7F);
                    splitLeft = persistent(s.pupil(), center, 12, 190, 5, .3F);
                    splitRight = persistent(s.pupil(), center, 12, 190, 5, .3F);
                }
                if (t >= 35 && heldBeam != null && !impact) {
                    Vec3d d = s.pupil().subtract(o.pupil()).normalize();
                    heldBeam.endpoints(o.pupil(), s.pupil());
                    splitLeft.endpoints(s.pupil(), s.pupil().add(d.rotateYaw(.3F).scale(25)));
                    splitRight.endpoints(s.pupil(), s.pupil().add(d.rotateYaw(-.3F).scale(25)));
                }
                if (t >= 200 && !impact) {
                    s.glide(o.pupil(), 2.8);
                    if (s.pupil().distanceTo(o.pupil()) < 3) {
                        impact = true;
                        splitLeft.setDead();
                        splitRight.setDead();
                        heldBeam.setDead();
                        heldBeam =
                                persistent(
                                        o.pupil(), o.pupil().addVector(0, -8, 25), 0, 32, 6, .8F);
                        lock = new Vec3d(t, 0, 0);
                    }
                }
                if (impact)
                    heldBeam.endpoints(
                            o.pupil(), o.pupil().addVector(0, (t - lock.x) * 1.4 - 8, 25));
                break;
            case SAWSTORM:
                if (t < 120) {
                    int sawBeat = t % 20;
                    if (sawBeat == 1) {
                        double angle = (t / 20) * Math.PI / 3;
                        lock = target.getPositionVector().addVector(0, 1, 0);
                        dashEnd =
                                lock.addVector(
                                        Math.cos(angle) * 6,
                                        1 + Math.sin(angle * 2),
                                        Math.sin(angle) * 6);
                    }
                    s.look(dashEnd);
                    s.glide(dashEnd, 3.8);
                    s.contact(6);
                    if (sawBeat == 17) {
                        EntityWulfrumCut echo =
                                new EntityWulfrumCut(owner, s.pupil(), 2.4F, 5, 250 - t, 3, true)
                                        .plane((t / 20) % 3);
                        if (owner.world.spawnEntity(echo)) cuts.add(echo);
                    }
                } else if (t < 220) {
                    dash(s, t - 120, 24, 3.3);
                    if (t % 8 == 0 && !cuts.isEmpty()) {
                        EntityWulfrumCut echo = cuts.get((t / 8) % cuts.size());
                        if (!echo.isDead) {
                            Vec3d p = echo.getPositionVector();
                            EntityWulfrumRay pulse =
                                    persistent(
                                            o.pupil(),
                                            p.add(p.subtract(o.pupil()).normalize().scale(.4)),
                                            4,
                                            5,
                                            3,
                                            .12F);
                            sawFeeds.put(pulse, echo);
                        }
                    }
                    java.util.Iterator<java.util.Map.Entry<EntityWulfrumRay, EntityWulfrumCut>> it =
                            sawFeeds.entrySet().iterator();
                    while (it.hasNext()) {
                        java.util.Map.Entry<EntityWulfrumRay, EntityWulfrumCut> entry = it.next();
                        EntityWulfrumRay pulse = entry.getKey();
                        EntityWulfrumCut echo = entry.getValue();
                        if (pulse.isDead || echo.isDead) {
                            it.remove();
                            continue;
                        }
                        if (!pulse.firing()) continue;
                        Vec3d hit =
                                WulfrumSawMechanics.discHit(
                                        pulse.getPositionVector(),
                                        pulse.end(),
                                        echo.getPositionVector(),
                                        echo.plane(),
                                        echo.radius());
                        if (hit != null) {
                            Vec3d reflected =
                                    ExcavatorGeometry.reflected(
                                            pulse.end()
                                                    .subtract(pulse.getPositionVector())
                                                    .normalize(),
                                            WulfrumSawMechanics.normal(echo.plane()));
                            beam(hit, hit.add(reflected.scale(26)), 0, 6, 4);
                            pulse.setDead();
                            it.remove();
                        }
                    }
                } else s.glide(center.addVector(0, 4, 0), 1.2);
                o.glide(orbit(-a, 15, 5), 1);
                if (t >= 220 && !impact) {
                    boolean arrived = true;
                    for (EntityWulfrumCut echo : cuts)
                        if (!echo.isDead) {
                            Vec3d p = echo.getPositionVector(),
                                    delta = s.pupil().subtract(p),
                                    next =
                                            p.add(
                                                    delta.scale(
                                                            Math.min(
                                                                    1,
                                                                    1.6
                                                                            / Math.max(
                                                                                    .001,
                                                                                    delta
                                                                                            .lengthVector()))));
                            echo.setPosition(next.x, next.y, next.z);
                            echo.velocityChanged = true;
                            if (delta.lengthVector() > 2) arrived = false;
                        }
                    if (arrived) {
                        for (EntityWulfrumCut echo : cuts) echo.setDead();
                        impact = true;
                        ring(s.pupil(), 7, 2, 12, 7);
                    }
                }
                break;
            case OVERCLOCK:
                int swap = Math.min(3, t / 60);
                double progress = Math.max(0, Math.min(1, (t % 60 - 40) / 20D));
                progress = progress * progress * (3 - 2 * progress);
                double radius = (swap % 2 == 0 ? 7 : 14) + (swap % 2 == 0 ? 7 : -7) * progress;
                s.glide(orbit(a * (1 + swap * .3), radius, 2), 1.5 + swap * .2);
                o.glide(orbit(-a * (1 + swap * .3), 21 - radius, 4), 1.5);
                s.slash(6);
                if (t % 10 == 0) beam(o.pupil(), center, 6, 5, 4);
                if (t >= 210 && !impact) {
                    Vec3d muzzle = o.pupil().add(o.getLookVec().scale(2));
                    s.look(muzzle);
                    s.glide(muzzle.subtract(s.getLookVec().scale(3)), 3.4);
                    if (WulfrumAttackGeometry.intersects(
                            s.pupil(),
                            s.pupil().add(s.getLookVec().scale(6)),
                            o.pupil(),
                            muzzle,
                            .65)) {
                        impact = true;
                        for (int i = 0; i < 12; i++)
                            beam(
                                    muzzle,
                                    muzzle.addVector(
                                            Math.cos(i * Math.PI / 6) * 24,
                                            0,
                                            Math.sin(i * Math.PI / 6) * 24),
                                    3,
                                    10,
                                    5);
                    }
                }
                break;
            case FAILURE:
                if (t > 40 && t < 235 && t % 22 == 0) {
                    fan(s.pupil(), s.pupil().subtract(o.pupil()), 3, .7, 3);
                    ((net.minecraft.world.WorldServer) owner.world)
                            .spawnParticle(
                                    net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL,
                                    o.posX,
                                    o.posY + 1.5,
                                    o.posZ,
                                    9,
                                    .5,
                                    .5,
                                    .5,
                                    .04);
                }
                if (t < 120) {
                    dash(s, t, 30, 2.3);
                    o.glide(orbit(-a, 12, 4), 1.2);
                    if (t % 10 == 0 && t % 50 != 0)
                        beam(o.pupil(), center.addVector(Math.sin(t * .2) * 10, 1, 0), 6, 5, 4);
                } else if (t < 190) {
                    s.glide(
                            o.getPositionVector()
                                    .addVector(Math.cos(a * 3) * 10, 0, Math.sin(a * 3) * 10),
                            2.4);
                    if (t % 8 == 0) beam(o.pupil(), orbit(t * .2, 24, 1), 5, 5, 4);
                } else if (t < 235) dash(s, t - 190, 22, 2.6);
                else {
                    s.glide(o.getPositionVector().addVector(5, 0, 0), .6);
                    o.glide(center.addVector(0, 5, 12), .45);
                }
                break;
            default:
                break;
        }
    }

    private Vec3d orbit(double a, double radius, double y) {
        return center.addVector(Math.cos(a) * radius, y, Math.sin(a) * radius);
    }

    private Vec3d ground(Vec3d p) {
        net.minecraft.util.math.RayTraceResult hit =
                owner.world.rayTraceBlocks(
                        p.addVector(0, 12, 0), p.addVector(0, -24, 0), false, true, false);
        return hit == null ? p : hit.hitVec;
    }

    private boolean bladeTouches(EntityWulfrumEye seer, EntityWulfrumRay ray) {
        Vec3d from = seer.pupil(), to = from.add(seer.getLookVec().scale(6));
        return WulfrumAttackGeometry.intersects(from, to, ray.getPositionVector(), ray.end(), .65);
    }

    private EntityWulfrumRay persistent(
            Vec3d from, Vec3d to, int warning, int life, float damage, float width) {
        EntityWulfrumRay ray =
                new EntityWulfrumRay(owner.world, owner, from, to, warning, life, damage)
                        .charged(width, false);
        owner.world.spawnEntity(ray);
        beams.add(ray);
        return ray;
    }

    private static double segmentDistance(Vec3d p, Vec3d a, Vec3d b) {
        Vec3d d = b.subtract(a);
        double f =
                d.lengthSquared() < 1e-8
                        ? 0
                        : Math.max(0, Math.min(1, p.subtract(a).dotProduct(d) / d.lengthSquared()));
        return p.distanceTo(a.add(d.scale(f)));
    }

    private void dash(EntityWulfrumEye e, int t, int period, double speed) {
        int p = Math.floorMod(t, period);
        if (p < 10) {
            lock =
                    target.getPositionVector()
                            .addVector(0, 1, 0)
                            .add(new Vec3d(target.motionX, 0, target.motionZ).scale(5));
            dashEnd = lock.add(lock.subtract(e.pupil()).normalize().scale(7));
            e.glide(orbit(t * .07, 12, 2), .8);
        } else {
            e.glide(dashEnd, speed);
            e.contact(6);
        }
    }

    private void beam(Vec3d from, Vec3d to, int warn, int life, float damage) {
        if (beams.size() >= 40) return;
        EntityWulfrumRay b = new EntityWulfrumRay(owner.world, owner, from, to, warn, life, damage);
        if (owner.world.spawnEntity(b)) beams.add(b);
    }

    private void fan(Vec3d p, Vec3d direction, int count, double spread, float damage) {
        Vec3d d = direction.normalize();
        for (int i = 0; i < count; i++)
            owner.fragment(
                    p,
                    d.rotateYaw((float) ((i - (count - 1) * .5) * spread)).scale(1.1),
                    0,
                    target,
                    damage);
    }

    private void ring(Vec3d p, double r, int warning, int life, float damage) {
        cut(p, r, warning, life, damage, false);
    }

    private void cut(Vec3d p, double r, int warning, int life, float damage, boolean saw) {
        if (cuts.size() >= 8) return;
        EntityWulfrumCut c = new EntityWulfrumCut(owner, p, (float) r, warning, life, damage, saw);
        if (owner.world.spawnEntity(c)) cuts.add(c);
    }

    private void survivor(EntityWulfrumEye e, int t) {
        e.glide(
                orbit(t * (e.seer() ? .05 : -.025), e.seer() ? 8 : 14, e.seer() ? 2 : 5),
                e.seer() ? 1.5 : .9);
        if (e.seer()) {
            dash(e, t, 40, 2.2);
            e.slash(7);
        }
        if (t % 30 == 12)
            for (int i = 0; i < 6; i++) {
                double[] p = appendage(e.ticksExisted, i, e.seer());
                Vec3d at = e.pupil().addVector(p[0], p[1], p[2]);
                beam(
                        at,
                        target.getPositionVector().addVector(0, 1, 0),
                        e.seer() ? 15 : 18,
                        e.seer() ? 7 : 12,
                        e.seer() ? 5 : 4);
            }
    }
}
