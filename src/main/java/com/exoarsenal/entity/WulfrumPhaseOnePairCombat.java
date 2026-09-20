package com.exoarsenal.entity;

import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.*;
import static com.exoarsenal.entity.WulfrumCoordinationScore.Pattern;

final class WulfrumPhaseOnePairCombat extends WulfrumPairCombat {
    WulfrumPhaseOnePairCombat(
            List<EntityLivingBase> actors, EntityPlayer player, boolean expert, Pattern pattern) {
        super(actors, player, expert, pattern);
    }

    @Override
    protected void executePattern() {
        if (pattern.pair == 0) eyesBrawler();
        else if (pattern.pair == 1) drillBrawler();
        else eyesDrill();
    }

    private int contactArm() {
        return pattern == Pattern.attack_4 || pattern == Pattern.attack_12 ? 2 : 0;
    }

    private Vec3d hand() {
        return brawler.localToWorld(brawler.hand(contactArm(), 0));
    }

    private void reach(Vec3d point, float grip) {
        if (handPosition == null) handPosition = hand();
        Vec3d d = point.subtract(handPosition);
        if (d.lengthVector() > 1.8) d = d.normalize().scale(1.8);
        handPosition = handPosition.add(d);
        brawler.coordinationHand(contactArm(), handPosition, grip);
        brawler.coordinationWrist(
                contactArm(),
                point.subtract(brawler.getPositionVector().addVector(0, 1.4, 0)),
                pattern == Pattern.attack_2 && exchangeLeg == 2 ? 90 : 0);
    }

    private void exactMove(EntityLivingBase e, Vec3d p) {
        move(e, p, 4);
    }

    private void punch(int contact) {
        brawler.collaborationPose(elapsedTicks, contact);
        if (elapsedTicks == contact) brawler.strike(0, 12, 2.5, true);
    }

    private void eyesBrawler() {
        if (seer == null || observer == null) return;
        int cycle = elapsedTicks / 64, q = elapsedTicks % 64;
        switch (pattern) {
            case attack_1:
                pinball(false);
                break;
            case attack_2:
                pinball(true);
                break;
            case attack_3:
                move(observer, orbit(elapsedTicks * .02, 17, 8), 1);
                move(brawler, orbit(-.5, 18, 5), 1);
                if (elapsedTicks >= 10 && elapsedTicks <= 50 && elapsedTicks % 10 == 0) {
                    int n = elapsedTicks / 10 - 1;
                    marks[n] =
                            player.getPositionVector()
                                    .addVector(player.motionX * 15, 1, player.motionZ * 15);
                    ray(observer.pupil(), marks[n], 30, 0);
                }
                if (elapsedTicks >= 60 && elapsedTicks <= 100 && elapsedTicks % 10 == 0) {
                    Vec3d at = marks[(elapsedTicks - 60) / 10],
                            muzzle = brawler.localToWorld(brawler.hand(3, 0));
                    brawler.coordinationWrist(3, at.subtract(muzzle), 0);
                    EntityBrawlerEffect large =
                            new EntityBrawlerEffect(
                                            brawler,
                                            EntityBrawlerEffect.SHARD,
                                            muzzle,
                                            at,
                                            8,
                                            130,
                                            1,
                                            8)
                                    .launch(at.subtract(muzzle).normalize().scale(.45), 8);
                    spawn(large);
                    artillery.add(large);
                }
                if (elapsedTicks >= 80 && elapsedTicks < 170) {
                    EntityBrawlerEffect next = null;
                    for (EntityBrawlerEffect x : artillery)
                        if (!x.isDead) {
                            next = x;
                            break;
                        }
                    if (next != null) {
                        Vec3d before = seer.pupil();
                        move(seer, next.getPositionVector().addVector(0, -1.5, 0), 3.6);
                        seer.coordinationPose(SeerObserverPattern.ZIGZAG, elapsedTicks);
                        if (segmentDistance(next.getPositionVector(), before, seer.pupil()) < 2) {
                            Vec3d cut = next.getPositionVector();
                            next.setDead();
                            for (int n = 0; n < 6; n++)
                                shard(cut.addVector(Math.cos(n), Math.sin(n), Math.sin(n * 2)), 1)
                                        .hold();
                            seer.slash(8);
                        }
                    }
                } else move(seer, orbit(2, 16, 6), 1);
                if (elapsedTicks >= 100 && elapsedTicks % 8 == 0)
                    for (int n = 0; n < shards.size(); n++) {
                        EntityWulfrumShard x = shards.get(n);
                        if (!x.isDead && x.held()) {
                            ray(observer.pupil(), x.getPositionVector(), 8, 0);
                            x.launchAfter(aim.subtract(x.getPositionVector()).normalize().scale(2));
                            x.releaseIn(8);
                            break;
                        }
                    }
                break;
            case attack_4:
                if (elapsedTicks < 170) {
                    move(
                            brawler,
                            orbit(elapsedTicks * .035, 8, 13 + Math.sin(elapsedTicks * .06) * 4),
                            2);
                    if (pendulum == null) pendulum = new WulfrumTetherPhysics(hand(), 8, 9);
                    pendulum.step(
                            hand(),
                            new Vec3d(
                                    Math.sin(elapsedTicks * .09) * .12,
                                    0,
                                    Math.cos(elapsedTicks * .09) * .12));
                    exactMove(observer, pendulum.first().addVector(0, -1.5, 0));
                    exactMove(seer, pendulum.second().addVector(0, -1.5, 0));
                    chain(hand(), observer.pupil());
                    chain(observer.pupil(), seer.pupil());
                    seer.coordinationPose(SeerObserverPattern.DOUBLE_PENDULUM, elapsedTicks);
                    seer.slash(8);
                    if (elapsedTicks % 30 == 0) ray(observer.pupil(), aim, 14, 7);
                } else if (elapsedTicks < 190) {
                    move(brawler, orbit(0, 24, 6), 3);
                    move(
                            observer,
                            observer.getPositionVector()
                                    .add(pendulum.firstVelocity().normalize().scale(3)),
                            3);
                    if (elapsedTicks < 177) {
                        move(
                                seer,
                                observer.getPositionVector()
                                        .add(pendulum.second().subtract(pendulum.first())),
                                3.5);
                        chain(observer.pupil(), seer.pupil());
                    } else
                        move(
                                seer,
                                seer.getPositionVector()
                                        .add(pendulum.secondVelocity().normalize().scale(3.8)),
                                3.8);
                } else {
                    move(brawler, aim, 4);
                    move(seer, aim, 4);
                    seer.slash(10);
                    punch(202);
                    brawler.ram(12, true);
                    if (elapsedTicks == 191) ray(observer.pupil(), aim, 8, 9);
                }
                break;
            case attack_5:
                move(brawler, orbit(elapsedTicks * .04, 22, 4), 2);
                move(observer, center.addVector(0, 12, 0), 1);
                Vec3d oldSaw = seer.pupil();
                move(seer, orbit((elapsedTicks - 30) * .11, 10, 1.5), 3);
                seer.coordinationPose(SeerObserverPattern.SAW_ORBIT, elapsedTicks);
                seer.slash(9);
                if (elapsedTicks == 16)
                    for (int n = 0; n < 18; n++) shard(orbit(n * Math.PI / 9, 10, 3), 1).hold();
                if (elapsedTicks >= 30)
                    for (int n = 0; n < shards.size(); n++) {
                        EntityWulfrumShard x = shards.get(n);
                        if (!x.isDead
                                && !clipped.contains(n)
                                && segmentDistance(x.getPositionVector(), oldSaw, seer.pupil())
                                        < 2.3) {
                            Vec3d radial = x.getPositionVector().subtract(center).normalize();
                            x.launch(new Vec3d(-radial.z, 0, radial.x).scale(1.1));
                            clipped.add(n);
                        }
                    }
                if (elapsedTicks >= 50 && elapsedTicks < 140 && elapsedTicks % 18 == 0)
                    ray(observer.pupil(), orbit(elapsedTicks * .11, 14, 1), 12, 8);
                if (elapsedTicks >= 50) {
                    EntityWulfrumShard next = null;
                    int selected = -1;
                    for (int n = 0; n < shards.size(); n++)
                        if (n % 3 == 0
                                && clipped.contains(n)
                                && !redirected.contains(n)
                                && !shards.get(n).isDead) {
                            next = shards.get(n);
                            selected = n;
                            break;
                        }
                    if (next != null) {
                        Vec3d intercept =
                                next.getPositionVector()
                                        .addVector(
                                                next.motionX * 2,
                                                next.motionY * 2,
                                                next.motionZ * 2);
                        move(brawler, intercept.addVector(-4, 0, 0), 3);
                        Vec3d previousHand = hand();
                        reach(intercept, 1);
                        if (segmentDistance(next.getPositionVector(), previousHand, hand()) < 1.5) {
                            next.launch(
                                    player.getPositionEyes(1)
                                            .subtract(next.getPositionVector())
                                            .normalize()
                                            .scale(2.4));
                            redirected.add(selected);
                            hitstopTicks = 2;
                        }
                    }
                }
                break;
            case attack_6:
                zigzagRelay();
                break;
            default:
                break;
        }
    }

    private void pinball(boolean fencing) {
        exchangeTick++;
        move(
                brawler,
                orbit(
                        fencing ? Math.PI : exchangeLeg * 2.1,
                        14,
                        exchangeLeg == 2 && !fencing ? 13 : 6),
                1.8);
        move(observer, orbit(exchangeLeg * 2.1 + 1.3, 14, 9), 1.5);
        seer.coordinationPose(
                fencing ? SeerObserverPattern.FENCING : SeerObserverPattern.SAW_ORBIT,
                elapsedTicks);
        if (exchangeLeg >= 3) {
            move(seer, observer.getPositionVector().addVector(4, 0, 0), 2);
            completed = exchangeTick > 18;
            return;
        }
        if (fencing && elapsedTicks % 36 == 0) ray(observer.pupil(), aim, 14, 7);
        if (exchangeStage == 0) {
            if (!fencing && exchangeTick < 18) {
                move(
                        seer,
                        observer.getPositionVector()
                                .addVector(
                                        Math.cos(exchangeTick * .4) * 5,
                                        Math.sin(exchangeTick * .4) * 4,
                                        0),
                        2.7);
                chain(observer.pupil(), seer.pupil());
                return;
            }
            if (fencing && exchangeTick < 23) {
                move(seer, aim.addVector(Math.sin(exchangeTick * .2) * 3, 0, 0), 2.7);
                seer.slash(8);
                return;
            }
            catchPoint =
                    brawler.getPositionVector()
                            .addVector(0, 1.4, 0)
                            .add(center.subtract(brawler.getPositionVector()).normalize().scale(4));
            launchDirection = player.getPositionEyes(1).subtract(catchPoint).normalize();
            reach(catchPoint, .1F);
            Vec3d contact = hand().add(launchDirection.scale(1.4));
            move(seer, contact.addVector(0, -1.5, 0), 3.5);
            if (seer.pupil().distanceTo(contact) < 1.1) {
                exchangeStage = 1;
                exchangeTick = 0;
                catchPoint = hand();
                aim = player.getPositionEyes(1);
                launchDirection = aim.subtract(seer.pupil()).normalize();
                hitstopTicks = 3;
            }
        } else if (exchangeStage == 1) {
            double pull = Math.sin(Math.min(1, exchangeTick / 12D) * Math.PI) * -1.1;
            reach(
                    catchPoint.add(launchDirection.scale(pull)),
                    (float) Math.min(1, exchangeTick / 6D));
            exactMove(seer, hand().add(launchDirection.scale(1.4)).addVector(0, -1.5, 0));
            seer.look(fencing && exchangeLeg == 2 ? brawler.getPositionVector() : aim);
            if (exchangeTick >= 12) {
                reach(catchPoint.add(launchDirection.scale(1.2)), 1);
                impact(hand());
                exchangeStage = 2;
                exchangeTick = 0;
                if (exchangeLeg == 2 && !fencing) ray(observer.pupil(), aim, 5, 9);
            }
        } else if (exchangeStage == 2) {
            move(
                    seer,
                    seer.getPositionVector()
                            .add(launchDirection.scale(fencing ? 4.5 + exchangeLeg * .5 : 5)),
                    5.5);
            seer.look(seer.pupil().add(launchDirection));
            seer.slash(11);
            if (!fencing && exchangeTick % 3 == 0) shard(seer.pupil(), 28);
            reach(catchPoint.add(launchDirection.scale(2)), 1);
            if (exchangeTick >= 12 || seer.pupil().subtract(aim).dotProduct(launchDirection) > 4) {
                exchangeStage = 3;
                exchangeTick = 0;
            }
        } else {
            Vec3d anchor = observer.pupil().addVector(4, 0, 0);
            chain(observer.pupil(), seer.pupil());
            move(seer, anchor.addVector(0, -1.5, 0), 3.8);
            if (seer.pupil().distanceTo(anchor) < 1.2) {
                exchangeStage = 0;
                exchangeTick = 0;
                exchangeLeg++;
                brawler.clearCoordinationHands();
                handPosition = null;
            }
        }
    }

    private void zigzagRelay() {
        move(observer, center.addVector(0, 10, 0), 1.5);
        if (exchangeLeg < 12) {
            double[] z = SeerObserverPattern.zigzag(exchangeLeg % 6, exchangeLeg >= 6);
            Vec3d end = center.addVector(z[0], z[1], z[2]);
            seer.coordinationPose(SeerObserverPattern.ZIGZAG, 40 + Math.min(190, elapsedTicks));
            move(brawler, end.addVector(exchangeLeg % 2 == 0 ? -4 : 4, 2, 0), 3.8);
            reach(
                    end.addVector(0, 1.5, 0),
                    exchangeTick > 0 ? Math.min(1, exchangeTick / 4F) : .1F);
            if (exchangeTick == 0) {
                Vec3d before = seer.pupil();
                move(seer, end, 4.8);
                seer.slash(10);
                if (segmentDistance(end.addVector(0, 1.5, 0), before, seer.pupil()) < 1.3
                        && (exchangeLeg % 2 == 1 || hand().distanceTo(seer.pupil()) < 2)) {
                    exchangeTick = 1;
                    burst(seer.pupil(), 6);
                }
            } else {
                exchangeTick++;
                if (exchangeLeg % 2 == 1) chain(observer.pupil(), seer.pupil());
                else exactMove(seer, hand().addVector(0, -1.5, 0));
                if (exchangeTick >= 6) {
                    exchangeLeg++;
                    exchangeTick = 0;
                }
            }
        } else {
            if (exchangeStage == 0) {
                move(seer, observer.getPositionVector(), 4);
                chain(observer.pupil(), seer.pupil());
                if (seer.getDistanceSq(observer) < 4) {
                    exchangeStage = 1;
                    exchangeTick = 0;
                    hitstopTicks = 4;
                }
            } else {
                exchangeTick++;
                seer.coordinationPose(SeerObserverPattern.SHARD_WHEEL, exchangeTick + 36);
                observer.coordinationPose(SeerObserverPattern.SHARD_WHEEL, exchangeTick + 36);
                move(
                        seer,
                        observer.getPositionVector()
                                .addVector(
                                        Math.cos(exchangeTick * .2) * 5,
                                        0,
                                        Math.sin(exchangeTick * .2) * 5),
                        3);
                if (exchangeTick % 10 == 0)
                    ray(observer.pupil(), orbit(exchangeTick * .2, 24, 1), 8, 8);
                move(brawler, orbit(-exchangeTick * .16, 7, 2), 3);
                brawler.strike(1, 10, 2.5, false);
            }
        }
    }

    private void drillBrawler() {
        int q = elapsedTicks % 70, round = elapsedTicks / 70;
        switch (pattern) {
            case attack_7:
            case attack_8:
            case attack_12:
                rally();
                break;
            case attack_9:
                if (elapsedTicks == 8) {
                    platform =
                            new EntityExcavatorPayload(
                                    excavator,
                                    EntityExcavatorPayload.CHUNK,
                                    center.addVector(0, -1, 0),
                                    center,
                                    0,
                                    220,
                                    7,
                                    0);
                    spawn(platform);
                }
                if (elapsedTicks < 155) {
                    double y =
                            elapsedTicks < 55
                                    ? (elapsedTicks - 8) * .22
                                    : elapsedTicks < 100
                                            ? 10 - (elapsedTicks - 55) * .17
                                            : 2 + (elapsedTicks - 100) * .25;
                    if (platform != null) platform.place(center.addVector(0, Math.max(-1, y), 0));
                    move(excavator, center.addVector(0, y - 5, 0), 2);
                    if (elapsedTicks == 55 || elapsedTicks == 101) {
                        excavator.shock(center, 8);
                        excavator.debris(center, 8);
                    }
                } else if (elapsedTicks == 156 && platform != null) platform.splitInHalf();
                else if (elapsedTicks >= 160 && elapsedTicks < 190) bore(aim.addVector(0, 20, 0));
                move(
                        brawler,
                        center.addVector(
                                0, elapsedTicks < 55 ? 19 : elapsedTicks < 100 ? 8 : 23, 0),
                        2);
                punch(elapsedTicks < 100 ? 55 : 150);
                if (elapsedTicks >= 140 && elapsedTicks <= 156 && platform != null) {
                    Vec3d cut =
                            platform.getPositionVector().addVector(0, 1, (elapsedTicks - 148) * .8);
                    brawler.coordinationHand(1, cut, 1);
                    if (elapsedTicks == 155) brawler.strike(1, 10, 7, false);
                }
                break;
            case attack_10:
                if (elapsedTicks == 2) deploy(6);
                move(excavator, orbit(elapsedTicks * .025, 22, -2), 1.3);
                move(brawler, orbit(elapsedTicks * .03, 13, 9), 1.7);
                for (int n = 0; n < 3; n++) {
                    int beat = (elapsedTicks + n * 23) % 90;
                    Vec3d winch = brawler.localToWorld(brawler.hand(2, 0));
                    place(
                            n,
                            beat < 25
                                    ? winch
                                    : beat < 60
                                            ? center.addVector((beat - 25) * 1.1 - 18, 3, n * 5 - 5)
                                            : orbit(n * 2.1, 17, 8),
                            beat >= 42);
                    if (alive(n)) {
                        chain(winch, probes.get(n).getPositionVector());
                        if (beat == 30)
                            excavator.beam(probes.get(n).getPositionVector(), aim, 10, 8, 6);
                    }
                }
                if (elapsedTicks > 200)
                    for (EntityExcavatorProbe p : probes) {
                        p.releaseCoordination();
                        p.recall();
                    }
                break;
            case attack_11:
                if (elapsedTicks < 100) {
                    move(excavator, center.addVector(-24 + elapsedTicks * .45, -3, 0), 2);
                    move(brawler, excavator.getPositionVector().addVector(0, 10, 0), 2);
                    if (elapsedTicks % 12 == 0) {
                        Vec3d from = excavator.getPositionVector().addVector(0, 3, 0),
                                to = from.addVector(5, 0, 0);
                        faults.add(new Vec3d[] {from, to});
                        EntityExcavatorPayload warning =
                                new EntityExcavatorPayload(
                                        excavator,
                                        EntityExcavatorPayload.FAULT,
                                        from,
                                        to,
                                        240,
                                        240,
                                        .3F,
                                        0);
                        spawn(warning);
                        faultWarnings.add(warning);
                    }
                } else {
                    move(excavator, center.addVector(0, elapsedTicks < 120 ? 18 : 0, 0), 3);
                    if (parryCount > 0) {
                        Vec3d before = brawler.getPositionVector();
                        move(brawler, excavator.getPositionVector(), 3.8);
                        if (!collisionResolved
                                && segmentDistance(
                                                excavator.getPositionVector(),
                                                before,
                                                brawler.getPositionVector())
                                        < 3) {
                            collisionResolved = true;
                            for (EntityExcavatorPayload warning : faultWarnings) warning.setDead();
                            for (Vec3d[] fault : faults) excavator.fault(fault[0], fault[1], 0, 9);
                            excavator.shock(center, 20);
                            excavator.debris(center, 20);
                            hitstopTicks = 7;
                        }
                    } else if (elapsedTicks < 125) move(brawler, center.addVector(0, 30, 0), 3);
                    else if (elapsedTicks < 155) {
                        move(brawler, aim, 3.8);
                        punch(145);
                        brawler.ram(14, true);
                    } else move(brawler, center.addVector(8, 8, 0), 1);
                }
                break;
            case attack_13:
                if (elapsedTicks == 2) deploy(20);
                move(excavator, orbit(elapsedTicks * .02, 22, 3), 1);
                for (int n = 0; n < probes.size(); n++) {
                    if (!formationBroken) {
                        place(
                                n,
                                center.addVector(n % 2 == 0 ? -6 : 6, 6, ((n / 2) - 5) * 4),
                                false);
                        if (elapsedTicks == 55 + n * 2 && alive(n))
                            excavator.beam(
                                    probes.get(n).getPositionVector(),
                                    probes.get(n).getPositionVector().addVector(0, 0, 24),
                                    18,
                                    14,
                                    6);
                    } else if (elapsedTicks - formationBreakTick < 14)
                        place(n, orbit(n * 2.4, 14, 4 + n % 3), true);
                    else probes.get(n).releaseCoordination();
                }
                if (parryCount > 0 && !formationBroken) {
                    EntityExcavatorProbe nearest = null;
                    for (EntityExcavatorProbe probe : probes)
                        if (!probe.isDead
                                && (nearest == null
                                        || probe.getDistanceSq(brawler)
                                                < nearest.getDistanceSq(brawler))) nearest = probe;
                    if (nearest != null) {
                        Vec3d old = brawler.getPositionVector();
                        move(brawler, nearest.getPositionVector(), 4);
                        if (segmentDistance(
                                        nearest.getPositionVector(),
                                        old,
                                        brawler.getPositionVector())
                                < 2.3) {
                            formationBroken = true;
                            formationBreakTick = elapsedTicks;
                            excavator.debris(brawler.getPositionVector(), 14);
                            hitstopTicks = 5;
                        }
                    }
                } else if (elapsedTicks < 80) move(brawler, center.addVector(0, 5, -25), 2);
                else if (elapsedTicks < 120 && !formationBroken) {
                    move(brawler, aim.addVector(0, 0, 10), 3.6);
                    punch(100);
                    brawler.ram(14, true);
                } else move(brawler, center.addVector(0, 6, -15), 1);
                break;
            default:
                break;
        }
    }

    private void rally() {
        rallyTick++;
        boolean vertical = pattern == Pattern.attack_12, tennis = pattern == Pattern.attack_7;
        double angle = parryCount * Math.PI * .65;
        Vec3d station = vertical ? center.addVector(0, 27, 0) : orbit(tennis ? 0 : angle, 24, 4);
        if (rallyStage == 3) {
            move(excavator, rallyGoal, .45);
            move(brawler, center.addVector(-9, 5, 0), .6);
            excavator.coordinationStall(true);
            double recoil = Math.exp(-rallyTick * .055) * Math.sin(rallyTick * .6);
            for (int arm : new int[] {0, 2}) {
                Vec3d socket =
                        brawler.localToWorld(new Vec3d(arm == 0 ? -4 : 4, arm == 0 ? 2 : -2, 0));
                brawler.coordinationHand(
                        arm, socket.addVector(recoil * .8, -rallyTick * .015, 0), .2F);
                brawler.coordinationWrist(arm, new Vec3d(0, -.6, 1), recoil * 25);
            }
            if (rallyTick % 8 == 0) {
                excavator.debris(excavator.drillTip(), 6);
                excavator.debris(brawler.localToWorld(brawler.hand(2, 0)), 5);
            }
            if (tennis) {
                for (int n = 0; n < probes.size(); n++) {
                    place(
                            n,
                            excavator
                                    .getPositionVector()
                                    .addVector(
                                            Math.cos(n * Math.PI * 2 / 3) * 4,
                                            2,
                                            Math.sin(n * Math.PI * 2 / 3) * 4),
                            false);
                    if (alive(n))
                        chain(probes.get(n).getPositionVector(), excavator.getPositionVector());
                }
            }
            if (rallyTick >= 48) completed = true;
            return;
        }
        boolean followsRear =
                rallyStage == 0 && !tennis && !vertical
                        || rallyStage == 1 && (vertical || !tennis && parryCount == 2);
        if (!followsRear) move(brawler, station, 2.7);
        if (rallyStage == 0) {
            Vec3d launch =
                    vertical
                            ? station.addVector(0, -7, 0)
                            : tennis && parryCount == 0
                                    ? orbit(Math.PI, 28, -3)
                                    : station.add(center.subtract(station).normalize().scale(5));
            move(excavator, launch, 2.8);
            if (vertical) {
                Vec3d winch = brawler.localToWorld(new Vec3d(4, -1, 2));
                brawler.coordinationHand(2, winch, 1);
                chain(winch, excavator.getPositionVector());
                chain(winch.addVector(-1, 0, 0), excavator.segment(16, 1));
                brawler.coordinationWrist(2, excavator.getPositionVector().subtract(winch), 0);
            } else if (!tennis) {
                Vec3d rear = excavator.segment(16, 1),
                        push = player.getPositionEyes(1).subtract(rear).normalize();
                move(brawler, rear.subtract(push.scale(5)), 2.7);
                reach(rear.subtract(push.scale(Math.max(0, (28 - rallyTick) * .08))), 1);
            }
            boolean ready = tennis || vertical || hand().distanceTo(excavator.segment(16, 1)) < 2.5;
            if (rallyTick >= 28 && ready && excavator.getPositionVector().distanceTo(launch) < 3) {
                rallyStage = 1;
                rallyTick = 0;
                rallyGoal = player.getPositionEyes(1);
                rallyDirection = rallyGoal.subtract(excavator.getPositionVector()).normalize();
                ray(excavator.drillTip(), rallyGoal, 14, 0);
                if (!tennis) {
                    Vec3d contact =
                            vertical ? excavator.getPositionVector() : excavator.segment(16, 1);
                    excavator.debris(contact, 12);
                    impact(contact);
                }
            }
        } else if (rallyStage == 1) {
            if (rallyTick < 14) {
                excavator.coordinationParry(true);
                return;
            }
            if (tennis && parryCount == 0 && rallyTick < 25) {
                move(brawler, aim.addVector(8, 2, 0), 2.4);
                punch(elapsedTicks + 4);
            }
            bore(excavator.getPositionVector().add(rallyDirection.scale(8)));
            if (vertical || !tennis && parryCount == 2) {
                move(brawler, excavator.getPositionVector().subtract(rallyDirection.scale(8)), 3.7);
                reach(excavator.segment(16, 1), 1);
            }
            if (rallyTick > 48) {
                rallyStage = 0;
                rallyTick = 0;
            }
        } else {
            if (parryCount >= 3 && tennis) {
                Vec3d high = center.addVector(0, 20, 0);
                move(excavator, high, 4.2);
                if (excavator.getPositionVector().distanceTo(high) < 3) {
                    deploy(3);
                    rallyGoal = high;
                    rallyStage = 3;
                    rallyTick = 0;
                    hitstopTicks = 8;
                }
                return;
            }
            Vec3d receive = station.add(center.subtract(station).normalize().scale(5));
            reach(receive, .2F);
            move(excavator, hand(), 4.2);
            if (!tennis) {
                Vec3d winch = brawler.localToWorld(brawler.hand(2, 0));
                chain(winch, excavator.getPositionVector());
                if (vertical) chain(winch.addVector(-1, 0, 0), excavator.segment(16, 1));
                brawler.coordinationWrist(2, excavator.getPositionVector().subtract(winch), 0);
            }
            if (excavator.getPositionVector().distanceTo(hand()) < 2.5) {
                brawler.coordinationHand(0, hand(), 1);
                excavator.debris(hand(), 16);
                hitstopTicks = 8;
                if (parryCount >= 3) {
                    rallyGoal = center.addVector(8, vertical ? 3 : 8, 0);
                    rallyStage = 3;
                    rallyTick = 0;
                } else {
                    rallyStage = 0;
                    rallyTick = 16;
                }
            }
        }
    }

    private void eyesDrill() {
        if (seer == null || observer == null) return;
        switch (pattern) {
            case attack_14:
                move(
                        excavator,
                        center.addVector(-22 + elapsedTicks * .25, elapsedTicks < 145 ? 1 : -4, 0),
                        1.5);
                move(observer, center.addVector(20 - elapsedTicks * .15, 9, 0), 1);
                chain(observer.pupil(), seer.pupil());
                seer.coordinationPose(SeerObserverPattern.SAW_ORBIT, elapsedTicks);
                if (elapsedTicks < 145) {
                    move(seer, excavator.segment(Math.min(17, elapsedTicks / 9), 1), 2.5);
                    if (elapsedTicks % 7 == 0)
                        for (int n = 0; n < 3; n++)
                            shard(
                                    seer.pupil().addVector(0, n, 0),
                                    145 - elapsedTicks + (elapsedTicks / 7) * 5);
                } else {
                    move(seer, aim, 3.8);
                    seer.slash(10);
                }
                break;
            case attack_15:
                if (elapsedTicks == 2) deploy(6);
                move(excavator, orbit(elapsedTicks * .05, 18, -3), 2);
                for (int n = 0; n < 6; n++)
                    place(n, orbit(n * Math.PI / 3, 12, 5 + n % 2 * 3), false);
                move(observer, orbit(.5, 20, 12), 1);
                move(seer, orbit(2.5, 10, 4), 1);
                seer.coordinationPose(SeerObserverPattern.FENCING, elapsedTicks % 65);
                int beat = elapsedTicks % 65;
                if (beat >= 15 && beat <= 39 && beat % 8 == 7 && probes.size() == 6) {
                    int hop = (beat - 15) / 8, offset = elapsedTicks / 65;
                    int[] path = {0, 3, 1, 4};
                    int index = (path[hop] + offset) % 6;
                    if (alive(index) && (hop == 0 || alive((path[hop - 1] + offset) % 6)))
                        ray(
                                hop == 0
                                        ? observer.pupil()
                                        : probes.get((path[hop - 1] + offset) % 6)
                                                .getPositionVector(),
                                probes.get(index).getPositionVector(),
                                8,
                                5);
                }
                boolean routeAlive =
                        alive(elapsedTicks / 65 % 6)
                                && alive((3 + elapsedTicks / 65) % 6)
                                && alive((1 + elapsedTicks / 65) % 6);
                if (beat >= 43 && beat < 55 && routeAlive)
                    seer.look(probes.get((1 + elapsedTicks / 65) % 6).getPositionVector());
                if (beat == 47 && routeAlive)
                    ray(
                            probes.get((1 + elapsedTicks / 65) % 6).getPositionVector(),
                            seer.pupil(),
                            8,
                            5);
                if (beat == 55 && routeAlive) {
                    seer.look(aim);
                    seer.coordinationPose(SeerObserverPattern.FENCING, 28);
                    ray(seer.pupil(), aim, 10, 9);
                }
                break;
            case attack_16:
                {
                    int leg = Math.min(11, Math.max(0, (elapsedTicks - 20) / 16));
                    double[] z = SeerObserverPattern.zigzag(leg % 6, leg >= 6);
                    Vec3d p = center.addVector(z[0], z[1], z[2]);
                    seer.coordinationPose(SeerObserverPattern.ZIGZAG, 40 + elapsedTicks);
                    move(seer, p, 4);
                    seer.slash(9);
                    if (elapsedTicks % 16 == 8) burst(p, 6);
                    move(excavator, p.addVector(0, elapsedTicks % 16 < 11 ? -7 : 2, 0), 4);
                    if (elapsedTicks % 16 == 12) {
                        excavator.shock(p.addVector(0, -z[1], 0), 6);
                        excavator.debris(p, 8);
                    }
                    move(observer, center.addVector(0, 15, 0), 1);
                    break;
                }
            case attack_17:
                pendulumDrill();
                break;
            case attack_18:
                if (elapsedTicks == 2) deploy(6);
                move(excavator, orbit(elapsedTicks * .02, 20, -3), 1.3);
                move(observer, center.addVector(0, 15, 0), 1);
                for (int n = 0; n < 6; n++) {
                    place(n, orbit(n * Math.PI / 3, 12, 6), false);
                    if (elapsedTicks == 50 + n * 2)
                        marks[n] =
                                player.getPositionVector()
                                        .addVector(
                                                player.motionX * (16 + n * 2),
                                                1,
                                                player.motionZ * (16 + n * 2));
                    if (elapsedTicks == 70 + n * 3 && alive(n))
                        ray(probes.get(n).getPositionVector(), observer.pupil(), 8, 0);
                }
                if (elapsedTicks == 100) {
                    safeCenter = marks[5];
                    Vec3d travel = marks[5].subtract(marks[0]);
                    safeAxis = new Vec3d(travel.x, 0, travel.z).normalize();
                    if (safeAxis.lengthSquared() < .01) safeAxis = new Vec3d(0, 0, 1);
                    Vec3d across = new Vec3d(safeAxis.z, 0, -safeAxis.x);
                    for (int side : new int[] {-1, 1})
                        for (int lane = 0; lane < 3; lane++) {
                            Vec3d mid = safeCenter.add(across.scale(side * (4 + lane * 4)));
                            ray(
                                    mid.subtract(safeAxis.scale(20)),
                                    mid.add(safeAxis.scale(20)),
                                    30,
                                    9);
                        }
                }
                seer.coordinationPose(SeerObserverPattern.FENCING, elapsedTicks);
                if (elapsedTicks >= 100 && elapsedTicks < 128)
                    move(seer, safeCenter.subtract(safeAxis.scale(20)), 3);
                else if (elapsedTicks >= 128 && elapsedTicks < 158) {
                    move(seer, safeCenter.add(safeAxis.scale(20)), 3.8);
                    seer.slash(11);
                } else move(seer, orbit(2, 18, 8), 1.5);
                break;
            case attack_19:
                if (elapsedTicks == 2) deploy(20);
                move(excavator, orbit(elapsedTicks * .035, 24, 1), 2);
                move(observer, center.addVector(0, 12, 0), 1);
                seer.coordinationPose(SeerObserverPattern.SAW_ORBIT, elapsedTicks);
                move(seer, orbit(elapsedTicks * .045, 15, 5), 2);
                chain(observer.pupil(), seer.pupil());
                boolean complete = true;
                for (int n = 0; n < probes.size(); n++) {
                    if (!alive(n)) continue;
                    EntityExcavatorProbe probe = probes.get(n);
                    int stage = probeStage[n];
                    if (stage < 4) complete = false;
                    if (elapsedTicks < 10 + n * 9) {
                        place(n, excavator.segment(n % 18, 1), false);
                        continue;
                    }
                    probeAge[n]++;
                    if (stage == 0) {
                        Vec3d catchAt =
                                observer.pupil()
                                        .add(seer.pupil().subtract(observer.pupil()).scale(.55));
                        place(n, catchAt, false);
                        if (segmentDistance(
                                        probe.getPositionVector(), observer.pupil(), seer.pupil())
                                < 1) {
                            probeStage[n] = 1;
                            probeAge[n] = 0;
                        }
                    } else if (stage == 1) {
                        place(n, center.addVector(0, 4, 0), false);
                        if (probe.getPositionVector().distanceTo(center.addVector(0, 4, 0)) < 1.3) {
                            ray(probe.getPositionVector(), player.getPositionEyes(1), 8, 5);
                            probeStage[n] = 2;
                            probeAge[n] = 0;
                        }
                    } else if (stage == 2) {
                        place(n, orbit(n, 12, 5), false);
                        if (probeAge[n] >= 10) {
                            probeStage[n] = 3;
                            probeAge[n] = 0;
                        }
                    } else if (stage == 3) {
                        place(n, orbit(n + probeAge[n] * .13, 12, 3), true);
                        if (probeAge[n] >= 22) {
                            probeStage[n] = 4;
                            probe.releaseCoordination();
                            probe.recall();
                        }
                    }
                }
                if (complete && elapsedTicks > 180) {
                    move(seer, aim, 4);
                    seer.slash(12);
                }
                break;
            case attack_20:
                double r = 24 - Math.min(elapsedTicks, 180) * .06;
                if (elapsedTicks == 2) deploy(12);
                if (elapsedTicks == 190) {
                    List<Double> angles = new ArrayList<>();
                    for (EntityExcavatorProbe probe : probes)
                        if (!probe.isDead) {
                            Vec3d d = probe.getPositionVector().subtract(center);
                            angles.add(Math.atan2(d.z, d.x));
                        }
                    Collections.sort(angles);
                    double widest = -1, heading = 0;
                    for (int i = 0; i < angles.size(); i++) {
                        double a = angles.get(i),
                                z =
                                        angles.get((i + 1) % angles.size())
                                                + (i + 1 == angles.size() ? Math.PI * 2 : 0);
                        if (z - a > widest) {
                            widest = z - a;
                            heading = (a + z) * .5;
                        }
                    }
                    gap = new Vec3d(Math.cos(heading), 0, Math.sin(heading));
                }
                move(
                        excavator,
                        elapsedTicks < 195
                                ? orbit(elapsedTicks * .06, r, -3)
                                : center.subtract(gap.scale(14)).addVector(0, 12, 0),
                        elapsedTicks < 195 ? 2.7 : 3.5);
                for (int i = 0; i < 12; i++) {
                    if (elapsedTicks < 190)
                        place(i, orbit(i * Math.PI / 6 + elapsedTicks * .03, r, 2), false);
                    if (elapsedTicks < 165 && elapsedTicks % 48 == i * 4 && alive(i))
                        excavator.beam(
                                probes.get(i).getPositionVector(),
                                center.addVector(0, 1, 0),
                                18,
                                7,
                                6);
                }
                if (elapsedTicks == 204) {
                    Vec3d eruption = center.subtract(gap.scale(14));
                    excavator.shock(eruption, 14);
                    excavator.debris(eruption, 18);
                }
                move(observer, center.addVector(0, 12, 0), 1);
                if (elapsedTicks < 165 && elapsedTicks % 20 == 0)
                    ray(observer.pupil(), orbit(elapsedTicks * .08, 26, 1), 14, 7);
                seer.coordinationPose(SeerObserverPattern.SAW_ORBIT, elapsedTicks);
                if (elapsedTicks < 195) {
                    move(seer, orbit(-elapsedTicks * .09, 7 + elapsedTicks * .04, 3), 3);
                    seer.slash(9);
                } else if (elapsedTicks < 215) {
                    move(seer, center.add(gap.scale(28)).addVector(0, 2, 0), 4.2);
                    seer.slash(12);
                }
                break;
            default:
                break;
        }
    }

    private void pendulumDrill() {
        if (elapsedTicks == 2) deploy(3);
        move(excavator, orbit(elapsedTicks * .04, 14, Math.sin(elapsedTicks * .06) * 7 - 2), 2.5);
        if (elapsedTicks % 45 == 0) {
            Vec3d surface = excavator.surface(excavator.getPositionVector());
            excavator.shock(surface, 9);
            excavator.debris(surface, 12);
        }
        List<Vec3d> anchors = new ArrayList<>();
        List<Double> lengths = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            place(i, excavator.segment(i * 4, 1).addVector(0, 2, 0), false);
            if (alive(i)) {
                Vec3d anchor = probes.get(i).getPositionVector();
                if (probeCableLengths[i] == 0)
                    probeCableLengths[i] =
                            Math.max(12, Math.min(26, anchor.distanceTo(observer.pupil())));
                anchors.add(anchor);
                lengths.add(probeCableLengths[i]);
                chain(anchor, observer.pupil());
            }
        }
        if (pendulum == null)
            pendulum = new WulfrumTetherPhysics(observer.pupil(), seer.pupil(), 9);
        double[] cable = new double[lengths.size()];
        for (int i = 0; i < cable.length; i++) cable[i] = lengths.get(i);
        pendulum.stepAnchors(
                anchors.toArray(new Vec3d[0]),
                cable,
                new Vec3d(
                        Math.sin(elapsedTicks * .08) * .12,
                        .04,
                        Math.cos(elapsedTicks * .08) * .12));
        if (!anchors.isEmpty()) {
            exactMove(observer, pendulum.first().addVector(0, -1.5, 0));
            exactMove(seer, pendulum.second().addVector(0, -1.5, 0));
        } else {
            move(observer, orbit(elapsedTicks * .03, 16, 10), 1.5);
            move(
                    seer,
                    observer.getPositionVector()
                            .addVector(
                                    Math.cos(elapsedTicks * .1) * 8,
                                    Math.sin(elapsedTicks * .1) * 5,
                                    0),
                    2.5);
        }
        chain(observer.pupil(), seer.pupil());
        if (elapsedTicks % 30 == 0) ray(observer.pupil(), aim, 12, 7);
        seer.coordinationPose(SeerObserverPattern.DOUBLE_PENDULUM, elapsedTicks);
        seer.slash(9);
    }
}
