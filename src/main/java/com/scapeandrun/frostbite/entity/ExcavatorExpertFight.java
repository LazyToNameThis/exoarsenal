package com.scapeandrun.frostbite.entity;

import com.scapeandrun.frostbite.world.ExcavatorTerrain;
import net.minecraft.entity.player.*;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.WorldServer;
import java.util.*;
import static com.scapeandrun.frostbite.entity.ExcavatorExpertScore.*;

final class ExcavatorExpertFight {
    private int cursor,
            pattern,
            stage,
            last = -1,
            parries,
            freezeTicks,
            carryTicks,
            tetherLeg,
            tensionTicks,
            collisionCooldown,
            surveyStep,
            surveyBeat;
    private double clock;
    private boolean falseUsed, flashed, overbore, parryContact, tutorialShown;
    private Vec3d center = Vec3d.ZERO,
            lock = Vec3d.ZERO,
            start = Vec3d.ZERO,
            direction = new Vec3d(1, 0, 0),
            priorVelocity = Vec3d.ZERO;
    private Vec3d recoilOrigin = Vec3d.ZERO, contactPoint = Vec3d.ZERO;
    private Vec3d braceAnchor = Vec3d.ZERO;
    private Vec3d lastPlayerPosition,
            observedVelocity = Vec3d.ZERO,
            corrections = Vec3d.ZERO,
            passVelocity = Vec3d.ZERO;
    private final Vec3d[] marks = new Vec3d[6], routes = new Vec3d[6];
    private final List<Vec3d> surveyHistory = new ArrayList<>();
    private final List<EntityExcavatorPayload> platforms = new ArrayList<>();
    private final List<EntityExcavatorPayload> tethers = new ArrayList<>();
    private UUID target;

    void begin(EntityExcavator e, EntityPlayer p, boolean second) {
        if (second != overbore) {
            cursor = 0;
            overbore = second;
        }
        pattern = select(second, cursor++);
        stage = 0;
        clock = 0;
        last = -1;
        parries = 0;
        falseUsed = false;
        flashed = false;
        target = p.getUniqueID();
        center = e.surface(p.getPositionVector());
        start = e.getPositionVector();
        lock = p.getPositionVector();
        e.clearEffects();
        platforms.clear();
        e.parryCount(0);
        tetherLeg = 0;
        tensionTicks = 0;
        collisionCooldown = 0;
        tethers.clear();
        for (int i = 0; i < 6; i++) {
            marks[i] =
                    e.surface(
                            center.addVector(
                                    Math.cos(i * Math.PI / 3) * 18,
                                    0,
                                    Math.sin(i * Math.PI / 3) * 18));
            routes[i] = marks[i];
        }
        if (pattern == IMPACT || pattern == FALSE_IMPACT || pattern == TENNIS || pattern == COUNTER)
            windup(e, p);
        e.expertState(pattern, stage, 0, 1);
    }

    boolean parrySequence() {
        return stage != 0;
    }

    void opening(EntityExcavator e, EntityPlayer p) {
        cursor = 0;
        begin(e, p, false);
        tutorialShown = false;
        charge(e, p);
        e.expertState(pattern, 2, 0, 1);
    }

    boolean tutorialParry(EntityExcavator e, EntityPlayer p) {
        if (stage != 12 || !p.getUniqueID().equals(target) || !p.isEntityAlive()) return false;
        contactPoint = e.drillTip();
        parryContact = true;
        stage = 2;
        try {
            parried(e, p);
        } finally {
            parryContact = false;
        }
        e.expertState(pattern, stage, 0, 0);
        return true;
    }

    private void state(int next) {
        stage = next;
        clock = 0;
        last = -1;
    }

    private void windup(EntityExcavator e, EntityPlayer p) {
        state(1);
        direction = p.getPositionVector().subtract(e.getPositionVector());
        direction = new Vec3d(direction.x, 0, direction.z).normalize();
        if (direction.lengthSquared() < .1) direction = new Vec3d(1, 0, 0);
        start = e.surface(p.getPositionVector().subtract(direction.scale(36))).addVector(0, 1, 0);
        lock = p.getPositionVector().addVector(0, 1, 0);
        e.dock();
        if (pattern == TENNIS) {
            e.deploy(3);
            for (int i = 0; i < 3; i++)
                if (e.probe(i) != null)
                    e.probe(i)
                            .surveySite(
                                    lock.subtract(direction.scale(25 + i * 5))
                                            .addVector(
                                                    direction.z * (i - 1) * 8,
                                                    -5,
                                                    -direction.x * (i - 1) * 8));
        }
    }

    private void charge(EntityExcavator e, EntityPlayer p) {
        state(2);
        flashed = false;
        start = e.getPositionVector();
        lock = p.getPositionVector().addVector(0, 1, 0);
        direction = lock.subtract(start).normalize();
    }

    void transition(EntityExcavator e, EntityPlayer p, int t) {
        if (t == 1) {
            e.clearEffects();
            e.dock();
        }
        e.drive(Vec3d.ZERO);
        e.expertState(pattern, 11, t, 1);
        if (t == 35 || t == 65) sound(e, SoundEvents.BLOCK_PISTON_CONTRACT, .55F);
        if (t >= 85 && t < 110 && t % 5 == 0) {
            e.beam(e.turret(t % 6), e.drillTip(), 0, 6, 0);
            sparks(e, e.drillTip(), 5);
        }
        if (t == 110) {
            e.overboreShield(true);
            sound(e, SoundEvents.ENTITY_IRONGOLEM_ATTACK, .5F);
            e.shock(e.surface(e.getPositionVector()), 8);
        }
        if (t > 120) e.guide(e.surface(p.getPositionVector()).addVector(0, -18, 0), 2);
    }

    void tick(EntityExcavator e, EntityPlayer p) {
        double distance = e.drillTip().distanceTo(p.getPositionVector().addVector(0, 1, 0));
        double rate = ExcavatorExpertScore.rate(pattern, stage == 2, distance, stage == 3);
        if (stage == 12) {
            if (!p.getUniqueID().equals(target)) {
                opening(e, p);
                return;
            }
            e.drive(Vec3d.ZERO);
            e.expertState(pattern, 12, freezeTicks++, 0);
            p.motionX = p.motionY = p.motionZ = 0;
            p.velocityChanged = true;
            p.fallDistance = 0;
            holdPlayer(p, lock, p.rotationYaw);

            return;
        }
        if (lastPlayerPosition != null) {
            Vec3d sample = p.getPositionVector().subtract(lastPlayerPosition);
            if (sample.lengthSquared() < 16)
                observedVelocity = observedVelocity.scale(.4).add(sample.scale(.6));
        }
        lastPlayerPosition = p.getPositionVector();
        if (stage == 3) {
            e.drive(Vec3d.ZERO);
            e.expertState(pattern, stage, freezeTicks, 0);
            if (p.getUniqueID().equals(target)) {
                double pressure =
                        .25 * Math.sin(Math.PI * freezeTicks / ExcavatorParryMotion.HITSTOP);
                Vec3d at = braceAnchor.add(direction.scale(pressure));
                float yaw = (float) Math.toDegrees(Math.atan2(direction.x, -direction.z));
                p.motionX = p.motionY = p.motionZ = 0;
                p.fallDistance = 0;
                p.velocityChanged = true;
                holdPlayer(p, at, yaw);
                p.renderYawOffset = yaw;
            }
            if (++freezeTicks >= ExcavatorParryMotion.HITSTOP) {
                freezeTicks = 0;
                state(4);
            }
            return;
        }
        clock += rate;
        int t = (int) clock;
        boolean beat = t != last;
        last = t;
        e.expertState(pattern, stage, t, rate);
        if (stage != 0) {
            parryTick(e, p, t, beat);
            return;
        }
        if (!beat) return;
        switch (pattern) {
            case RAILGUN:
                railgun(e, p, t);
                break;
            case SURVEY:
                survey(e, p, t);
                break;
            case CRUSTBREAKER:
                crustbreaker(e, p, t);
                break;
            case HARPOONS:
                harpoons(e, p, t);
                break;
            case EXTRACTION:
                extraction(e, p, t);
                break;
            case FEEDBACK:
                feedback(e, p, t);
                break;
            case REVOKED:
                revoked(e, p, t);
                break;
            case CROSSFIRE:
                crossfire(e, p, t);
                break;
            case ACCIDENT:
                accident(e, p, t);
                break;
            case COMPLETE:
                complete(e, p, t);
                break;
            default:
                windup(e, p);
        }
    }

    private void parryTick(EntityExcavator e, EntityPlayer p, int t, boolean beat) {
        if (stage == 1) {
            if (t < 24) e.guide(start, 2.6);
            else e.brake();
            e.face(p.getPositionVector().addVector(0, 1, 0).subtract(e.getPositionVector()));
            if (beat && t >= 24 && t < 44 && t % 5 == 0) {
                e.beam(e.drillTip(), p.getPositionVector().addVector(0, 1, 0), 0, 3, 0);
                sparks(e, e.drillTip(), 4 + (t - 24) / 3);
                sound(e, SoundEvents.BLOCK_PISTON_EXTEND, .7F + (t - 24) * .025F);
            }
            if (t >= 46) charge(e, p);
            return;
        }
        if (stage == 2) {
            if (pattern == IMPACT
                    && !tutorialShown
                    && e.drillTip().distanceTo(p.getPositionVector().addVector(0, 1, 0)) < 7) {

                direction = new Vec3d(direction.x, 0, direction.z).normalize();
                if (direction.lengthSquared() < .01) direction = new Vec3d(1, 0, 0);
                Vec3d pose =
                        p.getPositionVector().subtract(direction.scale(9)).addVector(0, -.5, 0);
                e.setPosition(pose.x, pose.y, pose.z);
                e.rotationYaw = (float) Math.toDegrees(Math.atan2(direction.x, direction.z));
                e.rotationPitch = 0;
                e.face(direction);
                tutorialShown = true;
                freezeTicks = 0;
                lock = p.getPositionVector();
                state(12);
                e.drive(Vec3d.ZERO);
                e.expertState(pattern, 12, 0, 0);
                return;
            }
            Vec3d aim = p.getPositionVector().addVector(0, 1, 0);
            direction = aim.subtract(e.drillTip()).normalize();
            double speed =
                    Math.min(7.5, 4.6 + parries * .9 + (pattern == COMPLETE ? 1 : 0) + t * .045);
            e.drive(direction.scale(speed));
            double distance = e.drillTip().distanceTo(aim);
            if (pattern == FALSE_IMPACT
                    && !falseUsed
                    && e.getPositionVector().distanceTo(start) > start.distanceTo(lock) * .45) {
                falseUsed = true;
                state(8);
                e.drive(Vec3d.ZERO);
                return;
            }
            if (!flashed && distance < 15) {
                flashed = true;
                sound(e, SoundEvents.BLOCK_NOTE_PLING, 1.8F);
                sparks(e, e.drillTip(), 28);
                e.fault(e.drillTip().addVector(-3, 0, 0), e.drillTip().addVector(3, 0, 0), 0, 0);
            }
            AxisAlignedBB body = p.getEntityBoundingBox().grow(1.5);
            Vec3d next = e.drillTip().add(direction.scale(speed * e.encounterRate()));
            RayTraceResult impact = body.calculateIntercept(e.drillTip(), next);
            if (body.contains(e.drillTip()) || impact != null) {
                contactPoint = body.contains(e.drillTip()) ? e.drillTip() : impact.hitVec;

                parryContact = true;
                try {
                    p.attackEntityFrom(DamageSource.causeMobDamage(e), 14);
                } finally {
                    parryContact = false;
                }
                if (stage == 2) {
                    state(7);
                    carryTicks = 0;
                    lock = direction;
                }
            }
            if (t > 160) {
                state(6);
                e.drive(Vec3d.ZERO);
            }
            return;
        }
        if (stage == 4) {
            Vec3d away = direction.scale(-2.2);
            e.drive(away);
            e.face(direction);
            boolean caught = false;
            boolean finalRecoil = parries >= parries(pattern);
            if (finalRecoil) {
                Vec3d horizontal = new Vec3d(direction.x, 0, direction.z).normalize();
                if (horizontal.lengthSquared() < .01) horizontal = new Vec3d(0, 0, 1);
                Vec3d at =
                        recoilOrigin.subtract(
                                horizontal.scale(
                                        ExcavatorParryMotion.distance(t, pattern == COMPLETE)));
                double ground = e.surface(at).y + .1;
                double base =
                        recoilOrigin.y
                                + (ground - recoilOrigin.y)
                                        * ExcavatorParryMotion.smooth(
                                                (t - 8)
                                                        / (double)
                                                                (ExcavatorParryMotion.LANDING - 8));
                at = new Vec3d(at.x, base + ExcavatorParryMotion.rise(t), at.z);
                e.drive(at.subtract(e.getPositionVector()));
                double rear = Math.toRadians(-ExcavatorParryMotion.rearPitch(t));
                e.face(horizontal.scale(Math.cos(rear)).addVector(0, Math.sin(rear), 0));
                if (beat && t == ExcavatorParryMotion.LANDING)
                    e.parryLanding(p, e.surface(at), direction);
            }
            if (pattern == TENNIS && parries < 3) {
                EntityExcavatorProbe catcher = e.probe(parries - 1);
                if (catcher != null && catcher.isEntityAlive()) {
                    e.guide(catcher.getPositionVector(), 2.8 + parries * .7);
                    caught =
                            e.getPositionVector().squareDistanceTo(catcher.getPositionVector()) < 9;
                } else if (t > 25) {
                    state(6);
                    return;
                }
            }
            if (beat && (!finalRecoil || t >= ExcavatorParryMotion.LANDING)) {
                cut(e, e.surface(e.getPositionVector()), 3);
                if (t % 6 == 0) sparks(e, e.surface(e.getPositionVector()), 8);
            }
            if (pattern == TENNIS && parries < 3
                    ? caught
                    : t >= (finalRecoil ? ExcavatorParryMotion.RECOIL_END : 25)) {
                if (parries < parries(pattern)) {
                    if (pattern == COUNTER) {
                        state(parries == 1 ? 9 : 10);
                        start = e.getPositionVector();
                    } else {
                        state(5);
                    }
                } else {
                    state(6);
                    e.drive(Vec3d.ZERO);
                }
            }
            return;
        }
        if (stage == 5) {
            e.drive(Vec3d.ZERO);
            if (beat && t == 8) {
                e.shock(e.getPositionVector(), 4);
                sparks(e, e.getPositionVector(), 20);
            }
            if (t >= 15) charge(e, p);
            return;
        }
        if (stage == 6) {
            e.brake();
            if (pattern == COMPLETE && t < 15) {
                e.guide(e.surface(e.getPositionVector()).addVector(0, -2, 0), .3);
                e.face(new Vec3d(direction.x, -.9, direction.z));
            }
            if (beat && t % 15 == 0) sound(e, SoundEvents.BLOCK_PISTON_CONTRACT, .5F);
            if (t >= (pattern == COMPLETE ? 40 : e.introTick() == -1 ? 40 : 18)
                    && !e.finishOpening(p)) begin(e, p, e.deep());
            return;
        }
        if (stage == 7) {
            e.drive(lock.scale(1.6));

            if (carryTicks == 0 && p.getUniqueID().equals(target) && p.isEntityAlive()) {
                Vec3d away = new Vec3d(lock.x, 0, lock.z).normalize();
                p.addVelocity(away.x * .8, .45, away.z * .8);
                p.velocityChanged = true;
            }
            if (++carryTicks >= 20) {
                state(6);
                e.drive(Vec3d.ZERO);
            }
            return;
        }
        if (stage == 8) {
            Vec3d behind = p.getPositionVector().add(direction.scale(12));
            if (t < 28) {
                e.guide(e.surface(behind).addVector(0, -8, 0), 2.7);
                if (beat && t % 5 == 0)
                    e.fault(e.surface(e.getPositionVector()), e.surface(behind), 10, 0);
            } else {
                e.guide(behind.addVector(0, 5, 0), 2.7);
                e.face(p.getPositionVector().subtract(e.getPositionVector()));
            }
            if (t >= 45) charge(e, p);
            return;
        }
        if (stage == 9) {
            if (t == 1) {
                start = e.surface(e.drillTip()).addVector(0, .2, 0);
                cut(e, start, 2);
            }
            double a = t * Math.PI / 26;
            Vec3d axis =
                    new Vec3d(
                                    direction.x * Math.cos(a) - direction.z * Math.sin(a),
                                    0,
                                    direction.z * Math.cos(a) + direction.x * Math.sin(a))
                            .normalize();
            Vec3d head = start.subtract(axis.scale(3.6)).addVector(0, -1.5, 0);
            e.drive(head.subtract(e.getPositionVector()));
            e.face(axis);
            if (t >= 26) charge(e, p);
            return;
        }
        if (stage == 10) {
            e.guide(p.getPositionVector().addVector(0, 22, 0), 3);
            if (beat && t == 1) e.deploy(3);
            if (t >= 30) charge(e, p);
        }
    }

    private static void holdPlayer(EntityPlayer p, Vec3d at, float yaw) {
        Vec3d delta = at.subtract(p.getPositionVector());
        if (delta.lengthVector() > .35) delta = delta.normalize().scale(.35);
        p.move(net.minecraft.entity.MoverType.SELF, delta.x, delta.y, delta.z);
        if (p instanceof EntityPlayerMP)
            ((EntityPlayerMP) p)
                    .connection.setPlayerLocation(p.posX, p.posY, p.posZ, yaw, p.rotationPitch);
    }

    void parried(EntityExcavator e, EntityPlayer player) {
        if (stage != 2 || !parryContact || !player.getUniqueID().equals(target)) return;
        braceAnchor = player.getPositionVector();
        contactPoint = braceAnchor.subtract(direction.scale(.85)).addVector(0, 1.25, 0);
        Vec3d contactShift = contactPoint.subtract(e.drillTip());
        e.setPosition(e.posX + contactShift.x, e.posY + contactShift.y, e.posZ + contactShift.z);
        recoilOrigin = e.getPositionVector();
        parries++;
        e.parryCount(parries);
        state(3);
        freezeTicks = 0;
        e.drive(Vec3d.ZERO);
        e.clearEffects();
        com.scapeandrun.frostbite.network.PacketExcavatorParry.send(
                e, player, contactPoint, direction, 0);
        sound(e, SoundEvents.BLOCK_ANVIL_LAND, .65F);
        sound(e, SoundEvents.ENTITY_IRONGOLEM_ATTACK, .6F);
        for (int i = -1; i <= 1; i += 2)
            e.fault(
                    e.drillTip(),
                    e.drillTip().addVector(direction.z * i * 14, 0, -direction.x * i * 14),
                    0,
                    0);
        if (pattern == HARPOONS) {
            for (int i = 0; i < 6; i++) if (e.probe(i) != null) e.probe(i).setDead();
        }
        if (pattern == COUNTER && parries == 3) e.overboreShield(false);
        if (pattern == COMPLETE)
            for (int i = 0; i < 5; i++) e.fault(marks[i], marks[i].addVector(0, 18, 0), 0, 0);
    }

    private void railgun(EntityExcavator e, EntityPlayer p, int t) {
        if (t == 1) {
            lock = e.surface(p.getPositionVector());
            start = lock.addVector(-25, -8, 0);
            e.dock();
        }
        if (t < 30) e.guide(start, 2);
        else if (t < 80) {
            double f = (t - 30) / 50D;
            Vec3d at = lock.addVector(-25 + f * 50, -7, 0);
            e.guide(at, 2.2);
            if (t % 5 == 0) {
                Vec3d ground = e.surface(at);
                e.bulge(ground, 2, 45, 2.5);
                e.fault(ground, ground.addVector(2, 0, 0), 45, 5);
            }
            if (t == 30) {
                e.laserWall(lock.addVector(-25, 0, -4), lock.addVector(25, 0, -4), 15, 60);
                e.laserWall(lock.addVector(-25, 0, 4), lock.addVector(25, 0, 4), 15, 60);
            }
        } else if (t < 110) {
            e.guide(lock.addVector(15, 24, 0), 3);
            if (t == 95) e.deploy(3);
        }
        if (t == 112)
            for (int i = 0; i < 3; i++)
                if (e.probe(i) != null) e.probe(i).surveySite(lock.addVector(-15 + i * 15, 0, 0));
        if (t >= 125 && t < 170 && t % 5 == 0) {
            int i = (t - 125) / 5;
            Vec3d at = lock.addVector(-24 + i * 6, 0, 0);
            e.beam(
                    e.probe(i % 3) == null ? e.turret(i) : e.probe(i % 3).getPositionVector(),
                    at,
                    5,
                    10,
                    6);
            e.fault(at, at.addVector(5, 0, 0), 10, 7);
        }
        if (t >= 205) begin(e, p, e.deep());
    }

    private void survey(EntityExcavator e, EntityPlayer p, int t) {
        if (t == 1) e.deploy(4);
        if (t == 10) for (int i = 0; i < 4; i++) if (e.probe(i) != null) e.probe(i).scan();
        if (t == 50) {
            Vec3d v = new Vec3d(observedVelocity.x, 0, observedVelocity.z);
            corrections = Vec3d.ZERO;
            for (int i = 0; i < 4; i++) {
                marks[i] = e.surface(p.getPositionVector().add(v.scale(12 + i * 8)));
                Vec3d axis = new Vec3d(Math.cos(i * Math.PI / 2), 0, Math.sin(i * Math.PI / 2));
                routes[i] = marks[i].subtract(axis.scale(18));
                remember(marks[i]);
            }
            priorVelocity = v;
            for (int i = 0; i < 4; i++) if (e.probe(i) != null) e.probe(i).lock();
        }
        if (t >= 60 && t < 180) {
            int pass = (t - 60) / 30, q = (t - 60) % 30;
            if (q == 0) {
                passVelocity = observedVelocity;
                if (pass == 3) {
                    marks[3] =
                            e.surface(
                                    p.getPositionVector()
                                            .add(observedVelocity.scale(12))
                                            .add(corrections.scale(8D / 3)));
                    routes[3] = marks[3].addVector(0, 0, 18);
                }
            }
            if (q == 29 && pass < 3)
                corrections = corrections.add(observedVelocity.subtract(passVelocity));
            Vec3d axis = marks[pass].subtract(routes[pass]).normalize(), at = marks[pass];
            if (q < 8) e.guide(routes[pass].addVector(0, -3, 0), 3.5);
            else {
                e.guide(at.add(axis.scale(18)).addVector(0, -.5, 0), 3.5);
                if (q % 4 == 0) e.fault(e.surface(e.getPositionVector()), at, 12, 4);
            }
        }
        if (t >= 155 && t < 185)
            for (int i = 0; i < 4; i++)
                if (e.probe(i) != null && e.probe(i).isEntityAlive())
                    e.probe(i).surveySite(routes[i].addVector(0, -4, 0));
        if (t == 185)
            for (int i = 0; i < 4; i++)
                if (e.probe(i) != null && e.probe(i).isEntityAlive()) {
                    Vec3d axis = marks[i].subtract(routes[i]).normalize();
                    e.beam(
                            e.probe(i).getPositionVector(),
                            marks[i].add(axis.scale(18)).addVector(0, 1, 0),
                            10,
                            24,
                            7);
                }
        if (t >= 220) begin(e, p, e.deep());
    }

    private void crustbreaker(EntityExcavator e, EntityPlayer p, int t) {
        if (t == 1) {
            lock = e.surface(p.getPositionVector());
            e.dock();
        }
        if (t < 80) e.guide(lock.addVector(0, -25, 0), 2);
        if (t == 80 || t == 105 || t == 130) {
            double radius = t == 80 ? 22 : t == 105 ? 14 : 7;
            for (int i = 0; i < 12; i++) {
                double a = i * Math.PI / 6, b = (i + 1) * Math.PI / 6;
                e.fault(
                        lock.addVector(Math.cos(a) * radius, 0, Math.sin(a) * radius),
                        lock.addVector(Math.cos(b) * radius, 0, Math.sin(b) * radius),
                        25,
                        8);
            }
        }
        if (t >= 150 && t < 185) e.guide(lock.addVector(0, 35, 0), 4);
        if (t == 150) e.deploy(6);
        if (t == 155) e.dock();
        if (t >= 185 && t < 195) e.brake();
        if (t >= 195 && collisionCooldown == 0) e.guide(lock.addVector(0, -3, 0), 5);
        else if (t >= 195) {
            if (t < 220) e.brake();
            else e.guide(lock.addVector(12, -5, 0), 1.5);
        }
        if (t >= 195 && collisionCooldown == 0 && e.drillTip().y <= lock.y + 1) {
            collisionCooldown = 1;
            e.drive(Vec3d.ZERO);
            e.shock(lock, 26);
            e.debris(lock, 20);
            for (int i = 0; i < 3; i++) {
                Vec3d end =
                        lock.addVector(
                                Math.cos(i * Math.PI * 2 / 3) * 32,
                                0,
                                Math.sin(i * Math.PI * 2 / 3) * 32);
                e.fault(lock, end, 8, 9);
                e.fireTurret(i, end.addVector(0, 1, 0), 8, 22, 7);
                for (int j = 1; j <= 2; j++)
                    e.fault(
                            lock.add(end.subtract(lock).scale(j / 3D)),
                            end.addVector(5 * j, 0, -4 * j),
                            12 + j * 4,
                            6);
            }
        }
        if (t >= 260) begin(e, p, e.deep());
    }

    private void harpoons(EntityExcavator e, EntityPlayer p, int t) {
        if (t == 1) {
            e.deploy(6);
            for (int i = 0; i < 6; i++) if (e.probe(i) != null) e.probe(i).surveySite(marks[i]);
        }
        if (t < 40) e.guide(marks[0].addVector(0, 1, 0), 3);
        if (t == 25)
            for (int i = 0; i < 6; i++) {
                Vec3d incoming = marks[i].subtract(marks[(i + 5) % 6]).normalize(),
                        cross = new Vec3d(-incoming.z, 0, incoming.x).scale(5),
                        at = marks[i].addVector(0, 1, 0);
                tethers.add(e.tether(at.subtract(cross), at.add(cross)));
            }
        if (t >= 40) {
            int index = (tetherLeg + 1) % 6;
            Vec3d goal = marks[index].addVector(0, 1, 0);
            EntityExcavatorPayload tether = tethers.get(index);
            double speed = 2.4 + tetherLeg * .7;
            if (tensionTicks > 0) {
                double tension = Math.sin(Math.PI * Math.min(1, tensionTicks / 8D)) * 2.6;
                tether.tension((float) -tension);
                Vec3d at = goal.add(direction.scale(tension));
                e.drive(at.subtract(e.getPositionVector()));
                e.face(direction);
                if (++tensionTicks > 8) {
                    tether.tension(0);
                    tensionTicks = 0;
                    tetherLeg++;
                    sound(e, SoundEvents.BLOCK_ANVIL_HIT, 1.3F);
                    if (tetherLeg >= 6) {
                        tether.setDead();
                        charge(e, p);
                    }
                }
                return;
            }
            direction = goal.subtract(e.getPositionVector()).normalize();
            Vec3d next = e.getPositionVector().add(direction.scale(speed));
            e.drive(direction.scale(speed));
            if (ExcavatorGeometry.crosses(
                    e.getPositionVector(), next, tether.getPositionVector(), tether.end(), 1.4)) {
                tensionTicks = 1;
                sparks(e, goal, 12);
            }
        }
    }

    private void extraction(EntityExcavator e, EntityPlayer p, int t) {
        if (t == 1) {
            lock = e.surface(p.getPositionVector());
            e.deploy(6);
            for (int i = 0; i < 6; i++)
                e.fault(
                        lock.addVector(
                                Math.cos(i * Math.PI / 3) * 10, 0, Math.sin(i * Math.PI / 3) * 10),
                        lock.addVector(
                                Math.cos((i + 1) * Math.PI / 3) * 10,
                                0,
                                Math.sin((i + 1) * Math.PI / 3) * 10),
                        55,
                        0);
        }
        if (t < 45) {
            double angle = t * Math.PI * 2 / 44;
            e.guide(lock.addVector(Math.cos(angle) * 10, -3, Math.sin(angle) * 10), 2.5);
            if (t % 6 == 0)
                e.fault(
                        e.surface(e.getPositionVector()),
                        lock.addVector(Math.cos(angle + .2) * 10, 0, Math.sin(angle + .2) * 10),
                        20,
                        0);
        } else if (t < 60) e.guide(lock.addVector(0, -7, 0), 2);
        if (t == 25)
            for (int i = 0; i < 6; i++)
                if (e.probe(i) != null)
                    e.probe(i)
                            .pull(
                                    lock.addVector(
                                            Math.cos(i * Math.PI / 3) * 9,
                                            0,
                                            Math.sin(i * Math.PI / 3) * 9));
        if (t == 60) platforms.addAll(e.liftSectionedIsland(lock));
        if (t >= 60 && t < 125) {
            for (EntityExcavatorPayload piece : platforms)
                if (!piece.isDead) piece.place(piece.getPositionVector().addVector(0, .22, 0));
            e.guide(lock.addVector(0, (t - 60) * .22 - 5, 0), 2);
            for (int i = 0; i < 6; i++)
                if (e.probe(i) != null)
                    e.probe(i)
                            .pull(
                                    lock.addVector(
                                            Math.cos(i * Math.PI / 3) * 9,
                                            (t - 59) * .22,
                                            Math.sin(i * Math.PI / 3) * 9));
        }
        if (t >= 125 && t < 195) {
            double angle = (t - 125) * .035;
            e.guide(lock.addVector(Math.sin(angle) * 5, 8, Math.cos(angle) * 5), .7);
            e.face(new Vec3d(-Math.sin(angle), .5, -Math.cos(angle)));
        }
        if (t >= 125 && t <= 195) {
            int index = 1 + (t - 125) / 20, q = (t - 125) % 20;
            if (index < platforms.size()) {
                EntityExcavatorPayload piece = platforms.get(index);
                Vec3d offset =
                        new Vec3d(
                                index == 1 ? -6 : index == 2 ? 6 : 0,
                                1,
                                index == 3 ? -6 : index == 4 ? 6 : 0);
                if (q < 10) {
                    Vec3d slice =
                            piece.getPositionVector()
                                    .add(offset)
                                    .addVector(
                                            index < 3 ? 0 : (q - 5) * 1.2,
                                            0,
                                            index < 3 ? (q - 5) * 1.2 : 0);
                    e.fireTurret(index, slice, 0, 2, 5);
                }
                if (q == 10) piece.launch(new Vec3d(offset.x * .025, -.12, offset.z * .025));
            }
        }
        if (t >= 195 && t < 220) e.guide(lock.addVector(0, 22, 0), 3.5);
        if (t >= 195
                && !platforms.isEmpty()
                && !platforms.get(0).isDead
                && e.drillTip().y >= platforms.get(0).posY) {
            platforms.get(0).shatter();
            e.debris(lock.addVector(0, 15, 0), 16);
            e.dock();
        }
        if (t > 220) e.guide(p.getPositionVector().addVector(0, -3, 0), 3);
        if (t >= 245) begin(e, p, e.deep());
    }

    private void feedback(EntityExcavator e, EntityPlayer p, int t) {
        Vec3d at = e.surface(center.addVector(Math.cos(t * .025) * 14, 0, Math.sin(t * .025) * 14));
        if (t < 160) e.guide(at.addVector(0, -.9, 0), 1.3);
        else e.guide(p.getPositionVector().addVector(0, 1, 0), 2.2);
        if (t % 2 == 0) {
            Vec3d axis =
                    e.drillTip().subtract(e.getPositionVector().addVector(0, 1.5, 0)).normalize();
            for (int i = 0; i < 2; i++) {
                double angle = Math.toRadians(e.drillRotation(0)) + i * Math.PI;
                Vec3d normal = ExcavatorGeometry.cutterNormal(axis, angle),
                        contact = e.drillTip().subtract(axis.scale(1.8)).add(normal.scale(.85)),
                        incoming = contact.subtract(e.turret(i)).normalize();
                Vec3d outgoing = ExcavatorGeometry.reflected(incoming, normal);
                e.fireTurret(i, contact, 0, 3, 0);
                e.beam(contact, contact.add(outgoing.scale(30)), 0, 3, 7);
            }
        }
        if (t >= 225) begin(e, p, e.deep());
    }

    private void revoked(EntityExcavator e, EntityPlayer p, int t) {
        if (t == 1) e.deploy(3);
        if (t < 150) {
            int pass = Math.min(2, t / 50), q = t % 50;
            Vec3d a = center.addVector(-22, 0, (pass - 1) * 10),
                    b = center.addVector(22, 0, (pass - 1) * 10);
            if (q == 1 && e.probe(pass) != null) e.probe(pass).surveySite(a);
            if (q < 15) e.guide(a.addVector(0, 1, 0), 3);
            else {
                e.guide(b.addVector(0, -.5, 0), 2.5);
                cut(e, e.surface(e.getPositionVector()), 2);
            }
        } else if (t < 220) {
            int q = (t - 150) % 30;
            if (q == 0) lock = e.surface(p.getPositionVector());
            e.guide(lock.addVector(0, q >= 10 && q < 18 ? 3 : -6, 0), 3);
            if (q == 4) {
                e.fault(lock, lock.addVector(2, 0, 2), 8, 8);
                e.fireTurret(t % 6, center.addVector(22, 1, (t % 3 - 1) * 10), 8, 12, 7);
            }
        }
        if (t == 220) {
            lock = e.surface(p.getPositionVector());
            EntityExcavatorPayload wall = e.lift(lock, 4, 70);
            platforms.add(wall);
        }
        if (t >= 220 && t < 245 && !platforms.isEmpty()) {
            EntityExcavatorPayload wall = platforms.get(0);
            wall.place(wall.getPositionVector().addVector(0, .25, 0));
            wall.tilt(Math.min(90, (t - 220) * 4));
            e.guide(lock.addVector(0, 4, -12), 2.5);
        }
        if (t >= 245 && !platforms.isEmpty()) {
            EntityExcavatorPayload wall = platforms.get(0);
            e.guide(
                    wall.isDead
                            ? p.getPositionVector().addVector(0, 1, 0)
                            : wall.getPositionVector(),
                    4);
            if (!wall.isDead && e.drillTip().distanceTo(wall.getPositionVector()) < 3) {
                wall.shatter();
                e.shock(wall.getPositionVector(), 10);
            }
        }
        if (t >= 290) begin(e, p, e.deep());
    }

    private void crossfire(EntityExcavator e, EntityPlayer p, int t) {
        if (t == 1) e.deploy(4);
        if (t < 140) {
            int pass = Math.min(3, t / 35), q = t % 35;
            double a =
                    pass == 0
                            ? Math.PI / 2
                            : pass == 1 ? 0 : pass == 2 ? Math.PI / 4 : Math.PI * 3 / 4;
            Vec3d d = new Vec3d(Math.cos(a), 0, Math.sin(a));
            marks[pass] = center.subtract(d.scale(25));
            routes[pass] = center.add(d.scale(25));
            if (q < 10) e.guide(marks[pass].addVector(0, -5, 0), 3);
            else e.guide(routes[pass].addVector(0, -5, 0), 3.2);
            if (q == 10) e.surveyLine(marks[pass], routes[pass], 270 - t);
        }
        if (t >= 140 && t < 165) e.guide(center.addVector(0, 7, 0), 2);
        if (t == 165) {
            for (int i = 0; i < 4; i++) {
                e.fireTurret(i, center.addVector(0, 1, 0), 0, 12, 0);
                for (int step = 0; step < 6; step++) {
                    double f = step / 6D, g = (step + 1) / 6D;
                    e.fault(
                            center.add(marks[i].subtract(center).scale(f)),
                            center.add(marks[i].subtract(center).scale(g)),
                            step * 5,
                            8);
                    e.fault(
                            center.add(routes[i].subtract(center).scale(f)),
                            center.add(routes[i].subtract(center).scale(g)),
                            step * 5,
                            8);
                }
            }
        }
        if (t >= 190 && t < 245) {
            e.guide(p.getPositionVector(), 1.8);
            if (t % 10 == 0) {
                int i = (t / 10) % 4;
                Vec3d moving = center.addVector(Math.sin(t * .05) * 6, 0, Math.cos(t * .05) * 6);
                if (e.probe(i) != null) e.probe(i).surveySite(moving);
                e.fault(moving, routes[i], 12, 7);
            }
        }
        if (t >= 275) begin(e, p, e.deep());
    }

    private void accident(EntityExcavator e, EntityPlayer p, int t) {
        if (t == 1) {
            e.deploy(6);
            direction = new Vec3d(1, 0, .3).normalize();
        }
        if (collisionCooldown > 0) collisionCooldown--;
        double speed = Math.min(5, 2 + t * .018);
        if (e.getPositionVector().squareDistanceTo(center) > 900 && collisionCooldown == 0) {
            Vec3d home = center.subtract(e.getPositionVector());
            home = new Vec3d(home.x, -18, home.z).normalize();
            direction = direction.scale(.75).add(home.scale(.25)).normalize();
        }
        Vec3d tip = e.drillTip(), next = tip.add(direction.scale(speed));
        RayTraceResult hit = e.world.rayTraceBlocks(tip, next, false, true, false);
        if (hit != null && hit.sideHit != null && collisionCooldown == 0) {
            direction =
                    ExcavatorGeometry.reflected(
                            direction, new Vec3d(hit.sideHit.getDirectionVec()));
            collisionCooldown = 6;
            e.fireTurret(
                    t % 6,
                    p.getPositionVector().addVector(Math.sin(t) * 12, 4, Math.cos(t) * 12),
                    3,
                    10,
                    7);
            e.debris(hit.hitVec, 5);
        }
        e.drive(direction.scale(speed));
        if (t % 4 == 0) {
            int i = t / 4 % 6;
            RayTraceResult predicted =
                    e.world.rayTraceBlocks(
                            tip, tip.add(direction.scale(speed * 5)), false, true, false);
            if (e.probe(i) != null) {
                if (predicted == null) e.probe(i).pull(e.segment(2 + i * 2, 1));
                else {
                    e.probe(i).surveySite(predicted.hitVec);
                    e.surveyLine(
                            predicted.hitVec.addVector(-2, .1, 0),
                            predicted.hitVec.addVector(2, .1, 0),
                            8);
                }
            }
        }
        if (t >= 175) charge(e, p);
    }

    private void complete(EntityExcavator e, EntityPlayer p, int t) {
        if (t == 1) {
            surveyStep = 0;
            surveyBeat = 0;
            e.deploy(6);
            for (int i = 0; i < 5; i++)
                if (surveyHistory.size() > i)
                    marks[i] = surveyHistory.get(surveyHistory.size() - 1 - i);
            marks[5] = e.surface(p.getPositionVector());
        }
        if (t == 15)
            for (int i = 0; i < 6; i++) {
                if (e.probe(i) != null) e.probe(i).surveySite(marks[i].addVector(0, 10, 0));
                e.surveyLine(i == 0 ? center : marks[i - 1], marks[i], 300);
            }
        if (t < 55) e.guide(center.addVector(0, -12, 0), 2);
        if (t < 55) return;
        if (surveyStep >= 5) {
            charge(e, p);
            return;
        }
        Vec3d at = marks[surveyStep],
                approach = at.addVector(surveyStep == 1 ? -18 : 0, surveyStep == 3 ? 8 : -6, 0);
        if (surveyBeat == 0) {
            e.guide(approach, 4.5);
            if (e.getPositionVector().squareDistanceTo(approach) < 4) {
                surveyBeat = 1;
                falseUsed = false;
                direction = new Vec3d(1, -1, 0).normalize();
            }
            return;
        }
        surveyBeat++;
        if (surveyStep == 0 || surveyStep == 2) {
            e.guide(at.addVector(0, 12, 0), 5);
            if (!falseUsed && e.drillTip().y >= at.y) {
                falseUsed = true;
                e.debris(at, 8);
                e.shock(at, 8);
                if (surveyStep == 2)
                    for (int i = 0; i < 6; i++)
                        if (e.probe(i) != null)
                            e.beam(
                                    e.probe(i).getPositionVector(),
                                    p.getPositionVector().addVector(0, 1, 0),
                                    6,
                                    10,
                                    6);
            }
            if (falseUsed && e.posY >= at.y + 8) {
                surveyStep++;
                surveyBeat = 0;
            }
        } else if (surveyStep == 1) {
            e.guide(at.addVector(18, 1, 0), 5);
            if (surveyBeat % 3 == 0) {
                double sweep = Math.max(-15, Math.min(15, e.posX - at.x));
                e.fireTurret(surveyBeat % 2, at.addVector(sweep, 1, 18), 2, 5, 7);
            }
            if (e.posX >= at.x + 16) {
                surveyStep++;
                surveyBeat = 0;
            }
        } else if (surveyStep == 3) {
            Vec3d from = e.drillTip(), to = from.add(direction.scale(4.5));
            RayTraceResult collision = e.world.rayTraceBlocks(from, to, false, true, false);
            if (!falseUsed && collision != null && collision.sideHit != null) {
                direction =
                        ExcavatorGeometry.reflected(
                                direction, new Vec3d(collision.sideHit.getDirectionVec()));
                falseUsed = true;
                surveyBeat = 1;
                e.debris(collision.hitVec, 8);
            }
            e.drive(direction.scale(4.5));
            if (falseUsed && surveyBeat >= 12 || surveyBeat > 45) {
                surveyStep++;
                surveyBeat = 0;
            }
        } else {
            e.guide(at.addVector(0, 7, 0), 5);
            if (!falseUsed && e.drillTip().y >= at.y) {
                falseUsed = true;
                EntityExcavatorPayload piece = e.lift(at, 4, 65);
                piece.launch(new Vec3d(0, .65, 0));
                e.debris(at, 12);
            }
            if (falseUsed && e.posY >= at.y + 5) {
                surveyStep++;
                surveyBeat = 0;
            }
        }
    }

    private void remember(Vec3d at) {
        if (surveyHistory.isEmpty()
                || surveyHistory.get(surveyHistory.size() - 1).squareDistanceTo(at) > 36) {
            surveyHistory.add(at);
            if (surveyHistory.size() > 32) surveyHistory.remove(0);
        }
    }

    private void cut(EntityExcavator e, Vec3d at, int radius) {
        for (int x = -radius; x <= radius; x++)
            for (int z = -radius; z <= radius; z++)
                for (int y = 1; y <= 3; y++)
                    ExcavatorTerrain.get(e.world).cut(e, new BlockPos(at).add(x, -y, z));
        remember(at);
    }

    private static void sparks(EntityExcavator e, Vec3d at, int count) {
        for (int i = 0; i < Math.min(24, count); i++) {
            double a = i * 2.399963;
            ((WorldServer) e.world)
                    .spawnParticle(
                            EnumParticleTypes.REDSTONE,
                            at.x + Math.cos(a),
                            at.y + (i % 5) * .2,
                            at.z + Math.sin(a),
                            0,
                            .25,
                            1,
                            .55,
                            1);
        }
    }

    private static void sound(EntityExcavator e, SoundEvent sound, float pitch) {
        e.world.playSound(null, e.posX, e.posY, e.posZ, sound, SoundCategory.HOSTILE, 1.5F, pitch);
    }
}
