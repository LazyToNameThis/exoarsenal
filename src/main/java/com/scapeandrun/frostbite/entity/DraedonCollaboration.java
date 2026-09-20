package com.scapeandrun.frostbite.entity;

import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.*;
import net.minecraft.world.WorldServer;
import java.util.*;

final class DraedonCollaboration {
    private int time, next = 400;
    private Vec3d center = Vec3d.ZERO;
    private boolean expert;
    private int sequence, variant;
    private Vec3d aim = Vec3d.ZERO;
    private WulfrumCoordinationCombat choreography;
    private final int[] finalCursor = new int[5];
    private final int[] pairCursor = new int[3];
    private final List<EntityLivingBase> participants = new ArrayList<>();

    void tick(EntityDraedon director, List<EntityLivingBase> live, EntityPlayer target, int clock) {
        if (clock == 1) {
            finish();
            next = 400;
        }
        if (time > 0) {
            for (EntityLivingBase e : participants)
                if (!ready(e)) {
                    finish();
                    next = clock + 80;
                    return;
                }
            if (choreography.tick(target)) {
                finish();
                next = clock + (expert ? 80 : 130);
            }
            return;
        }
        if (clock < next || live.size() < 2) return;
        for (EntityLivingBase e : live) if (!ready(e)) return;
        participants.clear();
        participants.addAll(live);
        center = target.getPositionVector();
        aim = center.addVector(0, 1, 0);
        expert =
                com.scapeandrun.frostbite.world.FrostbiteWorldSettings.get(director.world)
                        .isExpert();
        variant = sequence++ % 2;
        time = 1;
        next = clock + (expert ? 300 : 360);
        int pair =
                find(EntityBrawler.class) != null
                        ? (find(EntityExcavator.class) != null ? 1 : 0)
                        : 2;
        EntityBrawler finalBrawler = find(EntityBrawler.class);
        EntityWulfrumEye survivor = find(EntityWulfrumEye.class);
        int finalPair =
                expert
                        ? WulfrumFinalPairScore.pairing(
                                finalBrawler != null && finalBrawler.bipedal(),
                                find(EntityExcavator.class) != null,
                                survivor != null && survivor.overclocked(),
                                survivor != null && survivor.seer())
                        : -1;
        if (finalPair >= 0) {
            final int selected = finalPair;
            participants.removeIf(
                    actor ->
                            selected == 4
                                    ? !(actor instanceof EntityBrawler)
                                            && !(actor instanceof EntityExcavator)
                                    : selected < 2
                                            ? !(actor instanceof EntityBrawler) && actor != survivor
                                            : !(actor instanceof EntityExcavator)
                                                    && actor != survivor);
            choreography =
                    WulfrumFinalPairCombat.create(
                            participants,
                            target,
                            WulfrumFinalPairScore.select(finalPair, finalCursor[finalPair]++));
            return;
        }
        if (finalBrawler != null && finalBrawler.phase() == 3) {
            finish();
            next = clock + 60;
            return;
        }
        if (pair != 1) {
            EntityWulfrumEye eye = find(EntityWulfrumEye.class);
            if (eye == null || eye.partner() == null) {
                finish();
                next = clock + 80;
                return;
            }
        }
        EntityBrawler brawler = find(EntityBrawler.class);
        int arms = 0;
        if (brawler != null)
            for (int i = 0; i < 4; i++) if (brawler.armHealth(i) > 0) arms |= 1 << i;
        EntityWulfrumEye eye = find(EntityWulfrumEye.class);
        boolean phaseTwo =
                brawler != null
                        ? brawler.phase() == 2
                        : eye != null
                                && eye.upgraded()
                                && eye.partner() != null
                                && eye.partner().upgraded();

        if (phaseTwo
                && pair == 0
                && (eye == null
                        || !eye.upgraded()
                        || eye.partner() == null
                        || !eye.partner().upgraded())) {
            finish();
            next = clock + 60;
            return;
        }
        WulfrumCoordinationScore.Pattern chosen = null;
        for (int n = 0; n < WulfrumCoordinationScore.count(pair, phaseTwo); n++) {
            WulfrumCoordinationScore.Pattern candidate =
                    WulfrumCoordinationScore.select(pair, pairCursor[pair]++, phaseTwo);
            int required = WulfrumCoordinationScore.arms(candidate);
            if ((arms & required) == required) {
                chosen = candidate;
                break;
            }
        }
        if (chosen == null) {
            finish();
            next = clock + 80;
            return;
        }
        choreography = WulfrumPairCombat.create(participants, target, expert, chosen);
    }

    private boolean ready(EntityLivingBase e) {
        if (!e.isEntityAlive()) return false;
        if (e instanceof EntityWulfrumEye) {
            EntityWulfrumEye eye = (EntityWulfrumEye) e;
            return eye.introTick() == 0 && !eye.changing();
        }
        if (e instanceof EntityExcavator) {
            EntityExcavator x = (EntityExcavator) e;
            return x.introTick() == 0
                    && x.transition() == 0
                    && x.finale() == 0
                    && x.optionalRecoil() == 0
                    && (!x.expert() || x.expertStage() == 0);
        }
        return e instanceof EntityBrawler
                && (((EntityBrawler) e).phase() <= 2 || ((EntityBrawler) e).bipedal())
                && ((EntityBrawler) e).introTick() == 0
                && ((EntityBrawler) e).transition() == 0
                && ((EntityBrawler) e).clashTick() == 0;
    }

    private void finish() {
        if (choreography != null) choreography.finish();
        choreography = null;
        time = 0;
        participants.clear();
    }

    static boolean control(EntityLivingBase e) {
        if (!e.getEntityData().hasUniqueId("DraedonTrial") || e.world.isRemote) return false;
        Entity owner =
                ((WorldServer) e.world)
                        .getEntityFromUuid(e.getEntityData().getUniqueId("DraedonTrial"));
        if (!(owner instanceof EntityDraedon)) return false;
        DraedonCollaboration score = ((EntityDraedon) owner).collaboration;
        if (score.time == 0 || !score.participants.contains(e)) return false;
        return true;
    }

    static boolean parried(EntityLivingBase e, EntityPlayer player) {
        if (e.world.isRemote || !e.getEntityData().hasUniqueId("DraedonTrial")) return false;
        Entity owner =
                ((WorldServer) e.world)
                        .getEntityFromUuid(e.getEntityData().getUniqueId("DraedonTrial"));
        if (!(owner instanceof EntityDraedon)) return false;
        DraedonCollaboration score = ((EntityDraedon) owner).collaboration;
        if (score.time == 0 || !score.participants.contains(e) || score.choreography == null)
            return false;
        score.choreography.parried(e, player);
        return true;
    }

    private <T> T find(Class<T> type) {
        for (EntityLivingBase e : participants) if (type.isInstance(e)) return type.cast(e);
        return null;
    }

    private void move(EntityLivingBase e, Vec3d goal, double speed) {
        Vec3d delta = goal.subtract(e.getPositionVector());
        if (delta.lengthVector() > speed) delta = delta.normalize().scale(speed);
        e.motionX = e.motionY = e.motionZ = 0;
        e.setPosition(e.posX + delta.x, e.posY + delta.y, e.posZ + delta.z);
        e.velocityChanged = true;
        if (delta.lengthSquared() > .01) {
            e.rotationYaw =
                    (float)
                            Math.toDegrees(
                                    Math.atan2(
                                            e instanceof EntityExcavator ? delta.x : -delta.x,
                                            delta.z));
            e.renderYawOffset = e.rotationYaw;
        }
    }

    private void ray(EntityWulfrumEye eye, Vec3d from, Vec3d to, int warning, float damage) {
        eye.world.spawnEntity(new EntityWulfrumRay(eye.world, eye, from, to, warning, 12, damage));
    }

    private void perform(EntityLivingBase e) {
        EntityExcavator drill = find(EntityExcavator.class);
        EntityBrawler fist = find(EntityBrawler.class);
        EntityWulfrumEye eye = find(EntityWulfrumEye.class);
        if (variant == 1) {
            relay(e, drill, fist, eye);
            return;
        }
        int t = time;
        double direction = expert && t >= 90 ? -1 : 1;
        if (drill != null && eye != null) {

            if (e == drill) {
                double x =
                        t < 45
                                ? -24
                                : t < 90
                                        ? -24 + (t - 45) * 1.1
                                        : 25 - (t - 90) * (expert ? 1.1 : .35);
                move(e, center.addVector(x, -.5, 0), t < 45 ? .8 : 2.1);
                if (t == 40 || expert && t == 85)
                    drill.beam(center.addVector(-26, .2, 0), center.addVector(26, .2, 0), 12, 4, 0);
                if (t == 70 || expert && t == 110) {
                    drill.shock(center, 8);
                    drill.debris(center, 8);
                }
            } else if (e instanceof EntityWulfrumEye) {
                EntityWulfrumEye w = (EntityWulfrumEye) e;
                double side = w.seer() ? -1 : 1;
                move(e, center.addVector(Math.cos(t * .035) * 14, 5, side * 10), .85);
                w.look(center.addVector(0, 1, 0));
                if (t == 25 || expert && t == 85) {
                    for (int s : new int[] {-1, 1})
                        ray(
                                w,
                                center.addVector(-24, 1, s * 5),
                                center.addVector(24, 1, s * 5),
                                45,
                                7);
                }
                if (w.seer() && t > 55 && t < 75) {
                    move(e, center.addVector(0, 1, -18), 1.7);
                    w.slash(8);
                }
            }
        } else if (fist != null && eye != null) {

            if (e == fist) {
                fist.collaborationPose(t, expert && t > 70 ? 98 : 48);
                move(e, center.addVector(t < 65 ? -6 : 6, 0, 0), t < 35 ? .6 : 1.15);
                if (t == 48 || expert && t == 98) fist.strike(0, expert ? 11 : 9, 2, true);
            } else if (e instanceof EntityWulfrumEye) {
                EntityWulfrumEye w = (EntityWulfrumEye) e;
                move(e, center.addVector(w.seer() ? 16 : -4, 4, w.seer() ? 0 : 14), .75);
                w.look(center.addVector(0, 1, 0));
                if (!w.seer() && (t == 15 || expert && t == 80))
                    for (int i = -1; i <= 1; i++)
                        ray(w, w.pupil(), center.addVector(i * 5, 1, -16), 45, 7);
                if (w.seer() && t >= 65 && t < 85) {
                    move(e, center.addVector(-18, 1, 0), 1.6);
                    w.slash(8);
                }
            }
        } else if (fist != null && drill != null) {

            if (e == drill) {
                double a = t * .065;
                Vec3d goal =
                        t < 60
                                ? center.addVector(Math.cos(a) * 17, -2, Math.sin(a) * 17)
                                : center.addVector(
                                        (t - 60) * .65 - 5,
                                        t < 80 ? (t - 60) * .5 - 2 : 8 - (t - 80) * .2,
                                        0);
                move(e, goal, 1.35);
                if (t == 35)
                    drill.beam(center.addVector(-6, .1, 0), center.addVector(6, .1, 0), 25, 3, 0);
                if (t == 65 || expert && t == 120) {
                    drill.shock(center, expert ? 12 : 8);
                    drill.debris(center, 12);
                }
            } else {
                fist.collaborationPose(t, expert && t > 105 ? 135 : 85);
                move(e, center.addVector(direction * 5, t < 65 ? 7 : 0, 2), .65);
                if (t == 85 || expert && t == 135) fist.strike(0, expert ? 12 : 10, 3, true);
                if (t == 40)
                    drill.beam(
                            fist.localToWorld(fist.hand(0, 0)),
                            center.addVector(0, .4, 0),
                            40,
                            5,
                            0);
            }
        }
    }

    private void relay(
            EntityLivingBase actor,
            EntityExcavator drill,
            EntityBrawler brawler,
            EntityWulfrumEye eyes) {
        int t = time;
        if (drill != null && eyes != null) {

            if (actor == drill) {
                if (t < 55) {
                    double a = t * .10;
                    move(actor, center.addVector(Math.cos(a) * 12, -2.5, Math.sin(a) * 12), 1.5);
                } else if (t < 80) move(actor, aim.addVector(0, (t - 55) * .55 - 3, 0), 1.9);
                else move(actor, center.addVector((t - 80) * .5, 8 - (t - 80) * .16, 0), 1.4);
                if (t == 36) {
                    drill.beam(aim.addVector(-7, -.9, 0), aim.addVector(7, -.9, 0), 24, 5, 0);
                    drill.beam(aim.addVector(0, -.9, -7), aim.addVector(0, -.9, 7), 24, 5, 0);
                }
                if (t == 62) {
                    drill.shock(aim.addVector(0, -1, 0), 10);
                    drill.debris(aim, 12);
                }
                if (expert && t == 115) {
                    drill.shock(aim.addVector(0, -1, 0), 13);
                    drill.debris(aim, 16);
                }
            } else if (actor instanceof EntityWulfrumEye) {
                EntityWulfrumEye eye = (EntityWulfrumEye) actor;
                double side = eye.seer() ? -1 : 1;
                if (eye.seer() && t >= 82 && t < 106) {
                    move(actor, aim.addVector((106 - t) * 1.4 - 16, 0, 0), expert ? 2.6 : 2);
                    eye.look(aim);
                    eye.slash(expert ? 10 : 8);
                } else {
                    double angle = t * .035;
                    move(
                            actor,
                            center.addVector(
                                    Math.cos(angle) * side * 14, 6, Math.sin(angle) * side * 14),
                            1);
                    eye.look(aim);
                }
                if (!eye.seer() && (t == 38 || t == 98)) {
                    for (int sideRay : new int[] {-1, 1})
                        ray(
                                eye,
                                eye.pupil(),
                                aim.addVector(sideRay * 5, 0, sideRay * -10),
                                expert ? 18 : 28,
                                8);
                }
                if (expert && !eye.seer() && t == 130)
                    for (int n = -1; n <= 1; n++)
                        ray(eye, eye.pupil(), aim.addVector(n * 4, 0, 0), 18, 8);
            }
        } else if (brawler != null && eyes != null) {

            if (actor == brawler) {
                brawler.collaborationPose(t, t < 80 ? 48 : 118);
                move(actor, center.addVector(t < 80 ? -8 : 8, 1, 0), .75);
                if (t == 48 || t == 118) brawler.strike(0, expert ? 13 : 10, 2.4, true);
            } else if (actor instanceof EntityWulfrumEye) {
                EntityWulfrumEye eye = (EntityWulfrumEye) actor;
                if (eye.seer()) {
                    Vec3d socket = brawler.localToWorld(brawler.hand(0, 0)).addVector(0, 1, 1);
                    if (t < 48) move(actor, socket, 1.25);
                    else if (t < 74) {
                        move(actor, aim.addVector(18, 0, 0), expert ? 2.8 : 2.2);
                        eye.look(aim);
                        eye.slash(expert ? 11 : 9);
                    } else if (t < 118) move(actor, socket, 1.2);
                    else if (t < 146) {
                        move(actor, aim.addVector(-18, 0, 0), expert ? 2.8 : 2.2);
                        eye.look(aim);
                        eye.slash(expert ? 11 : 9);
                    }
                } else {
                    move(actor, center.addVector(0, 7, t < 90 ? 14 : -14), .9);
                    eye.look(aim);
                    if (t == 24 || t == 94)
                        for (int n = -1; n <= 1; n++)
                            ray(eye, eye.pupil(), aim.addVector(n * 5, 0, 0), expert ? 20 : 30, 8);
                    if (expert && t == 150)
                        for (int n : new int[] {-1, 1})
                            ray(eye, eye.pupil(), aim.addVector(n * 3, 0, 0), 16, 9);
                }
            }
        } else if (brawler != null && drill != null) {

            if (actor == drill) {
                Vec3d launch = aim.addVector(-21, -.5, 0), exit = aim.addVector(23, -.5, 0);
                move(
                        actor,
                        t < 52 ? launch : t < 96 ? exit : aim.addVector(-23, -.5, 0),
                        t < 52 ? .8 : expert ? 2.6 : 2);
                if (t == 28) drill.beam(launch, exit, 24, 8, 0);
                if (t == 78 || expert && t == 122) {
                    drill.shock(aim.addVector(0, -1, 0), expert ? 12 : 9);
                    drill.debris(aim, 14);
                }
            } else {
                brawler.collaborationPose(t, t < 90 ? 50 : 130);
                Vec3d goal =
                        t < 52
                                ? drill.getPositionVector().addVector(-5, 1, 0)
                                : aim.addVector(t < 100 ? -5 : 5, 2, 4);
                move(actor, goal, t < 52 ? .9 : 1.2);
                if (t == 50 || t == 130) brawler.strike(0, expert ? 14 : 11, 2.8, true);
                if (expert && t == 150) {
                    drill.shock(brawler.localToWorld(brawler.hand(0, 0)), 10);
                    drill.debris(aim, 10);
                }
            }
        }
    }
}
