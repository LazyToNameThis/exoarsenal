package com.exoarsenal.entity;

import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.*;
import static com.exoarsenal.entity.WulfrumCoordinationScore.Pattern;

final class WulfrumPhaseTwoPairCombat extends WulfrumPairCombat {
    WulfrumPhaseTwoPairCombat(
            List<EntityLivingBase> actors, EntityPlayer player, boolean expert, Pattern pattern) {
        super(actors, player, expert, pattern);
    }

    @Override
    protected void executePattern() {
        phaseTwo();
    }

    private void phaseTwo() {
        if (punishTick >= 0) {
            recoverPair();
            return;
        }
        if (replyTick >= 0 && elapsedTicks - replyTick < 14) {
            move(reflected, reflected.getPositionVector().add(launchDirection.scale(3)), 3);
            if (elapsedTicks % 2 == 0) impact(reflected.getPositionVector());
            if (brawler != null) brawler.engineMask(0);
            return;
        }
        switch (pattern) {
            case attack_21:
            case attack_27:
            case attack_25:
            case attack_31:
                redirectSaw();
                break;
            case attack_22:
            case attack_26:
            case attack_40:
                accelerator();
                break;
            case attack_23:
            case attack_28:
                vectorControl();
                break;
            case attack_24:
            case attack_29:
                wake();
                break;
            case attack_30:
                slipstream();
                break;
            case attack_32:
                launcher();
                break;
            case attack_33:
                lathe();
                break;
            case attack_34:
                intersection();
                break;
            case attack_35:
                cage();
                break;
            case attack_36:
                rails();
                break;
            case attack_37:
                parity();
                break;
            case attack_38:
                overclock();
                break;
            case attack_39:
                vectors();
                break;
            case attack_41:
            case attack_42:
            case attack_46:
                booster();
                break;
            case attack_43:
                probeSlingshot();
                break;
            case attack_44:
                missileCross();
                break;
            case attack_45:
                launchpad();
                break;
            case attack_47:
                stripSaw();
                break;
            case attack_48:
                mountedTurret();
                break;
            case attack_49:
            case attack_53:
                probeFactory();
                break;
            case attack_50:
                conductedFaults();
                break;
            case attack_51:
                sawtooth();
                break;
            case attack_52:
                eclipse();
                break;
            default:
                throw new IllegalStateException("Missing phase-two choreography: " + pattern);
        }
    }

    private void recovery() {
        punishTick = elapsedTicks;
        if (brawler != null) brawler.engineMask(0);
        if (excavator != null) excavator.coordinationStall(true);
    }

    private void recoverPair() {
        int age = elapsedTicks - punishTick;
        if (brawler != null) {
            brawler.engineMask(0);
            move(brawler, center.addVector(-7, 2, 0), .5);
        }
        if (seer != null) {
            move(seer, center.addVector(7, 2, 0), .5);
            seer.coordinationPose(SeerObserverPattern.SAW_ORBIT, 0);
        }
        if (observer != null) move(observer, center.addVector(0, 5, -10), .5);
        if (excavator != null) {
            excavator.coordinationStall(true);
            move(excavator, center.addVector(0, 1, 6), .45);
        }
        if (age % 12 == 0) impact(center.addVector(0, 2, 0));
        if (age >= 55) completed = true;
    }

    private void chargeBody(Vec3d goal, double speed) {
        Vec3d delta = goal.subtract(brawler.getPositionVector());
        move(brawler, goal, speed);
        brawler.engineMask(31);
        if (elapsedTicks - lastParryTick > 14 && delta.lengthSquared() > .01) brawler.ram(12, true);
    }

    private void sawPass(Vec3d goal, double speed) {
        move(seer, goal, speed);
        seer.look(goal.addVector(0, 1.5, 0));
        seer.slash(10);
    }

    private void wakeLine(Vec3d from, Vec3d to, int life) {
        spawn(
                new EntityBrawlerEffect(
                        brawler, EntityBrawlerEffect.JETWASH, from, to, 6, life, .7F, 5));
    }

    private void suppress(Vec3d at, int interval) {
        if (elapsedTicks % interval == 0) ray(observer.pupil(), at, 8, 7);
    }

    private void aimCharge(EntityLivingBase actor, Vec3d at) {
        marks[10] = at;
        Vec3d d = at.subtract(actor.getPositionVector()).normalize();
        marks[11] = at.add(d.scale(14));
        ray(actor.getPositionVector(), marks[11], 18, 0);
    }

    private void accelerateHeldShards() {
        if (elapsedTicks % 6 != 0) return;
        for (EntityWulfrumShard shard : shards)
            if (!shard.isDead && shard.held()) {
                ray(observer.pupil(), shard.getPositionVector(), 5, 0);
                shard.launchAfter(
                        player.getPositionEyes(1)
                                .subtract(shard.getPositionVector())
                                .normalize()
                                .scale(2.5));
                shard.releaseIn(5);
                break;
            }
    }

    private void accelerator() {
        int length = pattern == Pattern.attack_26 ? 155 : pattern == Pattern.attack_40 ? 275 : 210;
        int q = pattern == Pattern.attack_26 ? (elapsedTicks - 1) % 155 + 1 : elapsedTicks;
        double direction = pattern == Pattern.attack_26 && elapsedTicks > 155 ? -1 : 1;
        move(observer, center.addVector(0, 9, 0), 1.5);
        if (q < length - 38) {
            double a = direction * (q * .045 + q * q * .00055), radius = 12;
            move(seer, orbit(a, radius, 5), 5);
            move(brawler, orbit(-a + Math.PI, radius + 1, 5), 5);
            chain(observer.pupil(), seer.pupil());
            seer.slash(9);
            suppress(center.addVector(Math.cos(a) * 3, 1, Math.sin(a) * 3), 9);
            if (q % 15 == 0) shard(seer.pupil(), 1).hold();
            if (pattern == Pattern.attack_40) {
                if (q % 32 == 0)
                    spawn(
                            new EntityBrawlerEffect(
                                    brawler,
                                    EntityBrawlerEffect.HEX,
                                    brawler.getPositionVector(),
                                    center,
                                    14,
                                    28,
                                    2,
                                    7));
                accelerateHeldShards();
                if (q % 60 == 45) {
                    impact(seer.pupil());
                    hitstopTicks = 2;
                }
            }
        } else {
            if (q == length - 38) {
                aim = player.getPositionEyes(1);
                marks[0] = aim.add(seer.getPositionVector().subtract(aim).normalize().scale(-18));
                marks[1] =
                        aim.add(brawler.getPositionVector().subtract(aim).normalize().scale(-18));
                ray(seer.pupil(), marks[0], 16, 0);
                ray(brawler.getPositionVector(), marks[1], 24, 0);
            }
            if (q < length - 22) {
                move(seer, seer.getPositionVector(), 0);
                move(brawler, brawler.getPositionVector(), 0);
            } else if (q < length - 14) sawPass(marks[0], 5.5);
            else {
                sawPass(marks[0], 5.5);
                chargeBody(marks[1], 5.8);
                suppress(aim, 5);
            }
        }
        if (parryCount > 0 && pattern == Pattern.attack_40) {
            move(brawler, seer.getPositionVector(), 4);
            if (brawler.getDistanceSq(seer) < 16) {
                impact(seer.pupil());
                burst(seer.pupil(), 18);
                hitstopTicks = 10;
                recovery();
            }
        }
    }

    private void redirectSaw() {
        int q = (elapsedTicks - 1) % 72, round = (elapsedTicks - 1) / 72;
        int rounds = pattern == Pattern.attack_27 ? 4 : 3;
        move(observer, orbit(round * 1.2 + 1, 17, 10), 2);
        Vec3d contact = orbit(round * Math.PI * .5, 8, round == 2 ? 12 : 5);
        if (round >= rounds) {
            if (parryCount > 0 && (pattern == Pattern.attack_31 || pattern == Pattern.attack_25)) {
                move(brawler, seer.getPositionVector(), 4);
                if (brawler.getDistanceSq(seer) < 20) {
                    impact(seer.pupil());
                    recovery();
                }
            } else {
                if (q == 0) aimCharge(brawler, player.getPositionEyes(1));
                if (q > 16) chargeBody(marks[11], 5);
                else move(brawler, orbit(0, 18, 3), 2);
                chain(observer.pupil(), seer.pupil());
                move(seer, observer.getPositionVector().addVector(4, 0, 0), 3);
            }
            return;
        }
        if (q < 22) {
            move(
                    seer,
                    observer.getPositionVector()
                            .addVector(Math.cos(q * .35) * 5, Math.sin(q * .35) * 3, 0),
                    3.5);
            chain(observer.pupil(), seer.pupil());
            move(brawler, contact.addVector(5, 0, 0), 3);
        } else if (q < 34) {
            move(seer, contact, 4);
            move(brawler, contact, 4);
            brawler.engineMask(1 | (2 << round % 4));
            if (!clipped.contains(round) && seer.getDistanceSq(brawler) < 16) {
                clipped.add(round);
                impact(contact);
                hitstopTicks = 3;
                aim = player.getPositionEyes(1);
                marks[0] = aim.add(aim.subtract(contact).normalize().scale(15));
                marks[1] = contact.addVector(0, 0, round % 2 == 0 ? 16 : -16);
                if (pattern == Pattern.attack_31) ray(observer.pupil(), contact, 0, 5);
            }
        } else if (q < 51) {
            sawPass(marks[0], round == rounds - 1 ? 6 : 4.5);
            move(brawler, marks[1], 4);
        } else {
            chain(observer.pupil(), seer.pupil());
            move(seer, observer.getPositionVector().addVector(4, 0, 0), 4);
            move(brawler, orbit(round + 1, 16, 5), 3);
        }
        if (round == rounds - 1 && q == 51) aimCharge(brawler, player.getPositionEyes(1));
        if (round == rounds - 1 && q > 60) chargeBody(marks[11], 5.5);
    }

    private void vectorControl() {
        move(observer, center.addVector(0, 13, -18), 2);
        if (elapsedTicks < 190) {
            int q = (elapsedTicks - 1) % 40, n = (elapsedTicks - 1) / 40;
            Vec3d target = orbit(n * Math.PI * .5, 16, n % 2 == 0 ? 4 : 15);
            if (q == 3) {
                Vec3d socket =
                        brawler.localToWorld(
                                new Vec3d(n % 2 == 0 ? -1.5 : 1.5, n % 2 == 0 ? 0 : 1, -1));
                ray(observer.pupil(), socket, 6, 0);
                brawler.engineMask(2 << n % 4);
                impact(socket);
            }
            if (q > 9) move(brawler, target, 4.5);
            else move(brawler, brawler.getPositionVector(), 0);
            if (pattern == Pattern.attack_28) {
                if (q == 12) {
                    marks[0] = player.getPositionEyes(1).addVector(n % 2 == 0 ? 15 : -15, 0, 0);
                    ray(seer.pupil(), marks[0], 10, 0);
                }
                if (q > 22) sawPass(marks[0], 4.5);
                else move(seer, orbit(n * Math.PI / 2, 19, 4), 3);
            } else move(seer, observer.getPositionVector().addVector(5, 0, 0), 2);
        } else if (elapsedTicks < 218) {
            brawler.engineMask(31);
            if (elapsedTicks % 4 == 0) ray(observer.pupil(), brawler.getPositionVector(), 1, 0);
            if (elapsedTicks == 200) aimCharge(brawler, player.getPositionEyes(1));
        } else if (parryCount == 0) chargeBody(marks[11], 6);
        else if (pattern == Pattern.attack_28 && elapsedTicks < replyTick + 38) {
            if (elapsedTicks == replyTick + 14) {
                marks[0] =
                        player.getPositionEyes(1)
                                .add(
                                        player.getPositionEyes(1)
                                                .subtract(seer.pupil())
                                                .normalize()
                                                .scale(16));
                ray(seer.pupil(), marks[0], 8, 0);
            }
            if (elapsedTicks > replyTick + 22) sawPass(marks[0], 5.5);
        } else recovery();
    }

    private void wake() {
        move(observer, center.addVector(0, 12, 0), 2);
        int round = (elapsedTicks - 1) / 55, q = (elapsedTicks - 1) % 55;
        double sign = round % 2 == 0 ? 1 : -1;
        Vec3d from = center.addVector(-24 * sign, 4, round * 4 - 6),
                to = center.addVector(24 * sign, 4, round * 4 - 6);
        if (round < 4) {
            if (q < 14) {
                move(brawler, from, 4);
                move(seer, from.addVector(-sign * 5, 0, 0), 3);
                chain(observer.pupil(), seer.pupil());
            } else {
                Vec3d before = brawler.getPositionVector();
                move(brawler, to, 5);
                if (q % 5 == 0) wakeLine(before, brawler.getPositionVector(), 35);
                sawPass(to, 3.5 + round * .65);
                if (round >= 2 && q % 8 == 0) shard(seer.pupil(), 1).hold();
            }
            if (pattern == Pattern.attack_29) accelerateHeldShards();
        } else {
            if (q == 0) {
                marks[0] = player.getPositionEyes(1);
                marks[1] = marks[0].add(marks[0].subtract(seer.pupil()).normalize().scale(20));
                ray(seer.pupil(), marks[1], 18, 0);
            }
            if (q > 18) {
                sawPass(marks[1], 6);
                move(brawler, seer.getPositionVector().addVector(0, 0, -4), 6);
                suppress(marks[0].addVector(3, 0, 0), 5);
            } else chain(observer.pupil(), seer.pupil());
        }
    }

    private void slipstream() {
        move(observer, center.addVector(0, 12, 16), 2);
        if (elapsedTicks < 35) {
            move(brawler, center.addVector(-25, 4, 0), 3);
            move(seer, brawler.getPositionVector().addVector(-5, 0, 0), 3);
            if (elapsedTicks == 25) aimCharge(brawler, player.getPositionEyes(1));
        } else if (elapsedTicks < 75) {
            Vec3d before = brawler.getPositionVector();
            if (elapsedTicks < 50) move(brawler, marks[11], 4);
            else move(brawler, center.addVector(8, 16, 14), 4);
            if (elapsedTicks % 4 == 0) wakeLine(before, brawler.getPositionVector(), 25);
            sawPass(marks[11], 4.5);
        } else if (elapsedTicks < 115) {
            move(brawler, orbit(elapsedTicks * .06, 20, 7), 3);
            suppress(player.getPositionEyes(1).addVector(4, 0, 0), 7);
            chain(observer.pupil(), seer.pupil());
            move(seer, observer.getPositionVector().addVector(4, 0, 0), 3);
            if (elapsedTicks == 108) aimCharge(brawler, player.getPositionEyes(1));
        } else if (parryCount == 0) chargeBody(marks[11], 5.5);
        else recovery();
    }

    private void launcher() {
        Vec3d hub = center.addVector(0, 9, 0);
        if (elapsedTicks < 125) {
            double a = elapsedTicks * .025 + elapsedTicks * elapsedTicks * .0005;
            move(brawler, hub.addVector(Math.cos(a) * 2, 0, Math.sin(a) * 2), 3);
            move(seer, hub.addVector(-Math.cos(a) * 3, 0, -Math.sin(a) * 3), 3);
            move(observer, hub.addVector(0, 4, 0), 2);
            chain(observer.pupil(), seer.pupil());
            brawler.engineMask((1 << Math.min(5, 1 + elapsedTicks / 24)) - 1);
            if (elapsedTicks % 24 == 0) impact(brawler.getPositionVector());
        } else if (elapsedTicks < 155) {
            if (elapsedTicks == 125) {
                marks[0] = player.getPositionEyes(1);
                marks[1] = marks[0].add(marks[0].subtract(seer.pupil()).normalize().scale(20));
                ray(seer.pupil(), marks[1], 16, 0);
            }
            if (elapsedTicks > 141) {
                sawPass(marks[1], 6.5);
                move(brawler, center.addVector(20, 7, 0), 4);
            }
        } else if (elapsedTicks < 178) {
            move(brawler, center.addVector(20, 4, 0), 3);
            if (elapsedTicks == 160) aimCharge(brawler, player.getPositionEyes(1));
        } else if (parryCount == 0) chargeBody(marks[11], 6);
        else recovery();
    }

    private void lathe() {
        move(observer, center.addVector(0, 8, 0), 2);
        move(seer, orbit(elapsedTicks * .13, 9, 6), 4);
        move(brawler, orbit(-elapsedTicks * .09, 15, 6), 4);
        chain(observer.pupil(), seer.pupil());
        seer.slash(9);
        if (elapsedTicks > 30 && elapsedTicks < 220 && elapsedTicks % 12 == 0) {
            Vec3d cut = seer.pupil(),
                    end = cut.add(cut.subtract(observer.pupil()).normalize().scale(8));
            ray(observer.pupil(), cut, 3, 5);
            ray(cut, end, 12, 8);
            marks[elapsedTicks / 12 % 8] = end;
        }
        if (elapsedTicks > 60 && elapsedTicks < 220 && elapsedTicks % 24 == 0) {
            Vec3d at = marks[elapsedTicks / 24 % 8];
            impact(at);
            ray(at, player.getPositionEyes(1), 14, 8);
        }
        if (elapsedTicks == 222) aimCharge(brawler, center.addVector(0, 6, 0));
        if (elapsedTicks > 240) {
            chargeBody(marks[11], 5);
            if (elapsedTicks == 248) {
                impact(center.addVector(0, 6, 0));
                burst(center.addVector(0, 6, 0), 12);
            }
        }
    }

    private void intersection() {
        move(observer, center.addVector(0, 17, 20), 2);
        int q = (elapsedTicks - 1) % 28, leg = (elapsedTicks - 1) / 28;
        if (leg >= 10) {
            chain(observer.pupil(), seer.pupil());
            move(seer, observer.getPositionVector(), 3);
            move(brawler, orbit(elapsedTicks * .03, 18, 4), 3);
            return;
        }
        double sign = leg % 2 == 0 ? 1 : -1;
        Vec3d a = center.addVector(sign * 17, 3 + (leg % 3) * 3, -14 + (leg % 5) * 7),
                z = center.addVector(-sign * 17, 9 - (leg % 3) * 3, -7 + (leg % 5) * 7);
        if (q == 0) {
            marks[0] = seer.getPositionVector();
            marks[1] = brawler.getPositionVector();
            ray(marks[0], a, 10, 0);
            ray(marks[1], z, 10, 0);
        }
        if (q > 10) {
            sawPass(a, 5.5);
            chargeBody(z, 5);
            if (q % 4 == 0) {
                spawn(
                        new EntityBrawlerEffect(
                                brawler,
                                EntityBrawlerEffect.HEAD_ECHO,
                                brawler.getPositionVector(),
                                z,
                                8,
                                22,
                                1.5F,
                                0));
                ray(seer.pupil(), seer.pupil().subtract(seer.getLookVec().scale(2)), 12, 5);
            }
        }
        if (q == 22) {
            Vec3d cross = a.add(z).scale(.5);
            ray(observer.pupil(), cross, 10, 8);
            spawn(
                    new EntityBrawlerEffect(
                            brawler, EntityBrawlerEffect.HEX, cross, Vec3d.ZERO, 10, 20, 3, 7));
        }
    }

    private void cage() {
        Vec3d hub = center.addVector(0, 9, 0);
        move(observer, center.addVector(0, 23, 0), 2);
        if (elapsedTicks == 12) {
            cageVelocity = player.getPositionEyes(1).subtract(seer.pupil()).normalize().scale(2.2);
            for (int n = 0; n < 48; n++) {
                EntityBrawlerEffect panel =
                        new EntityBrawlerEffect(
                                brawler, EntityBrawlerEffect.SHELL, hub, hub, 20, 230, 2.65F, 0);
                spawn(panel);
                cagePanels.add(panel);
            }
        }
        cageStrength *= .88;
        for (int n = 0; n < cagePanels.size(); n++) {
            double a = n * 2.399963, y = 1 - 2 * (n + .5) / 48, rr = Math.sqrt(1 - y * y);
            Vec3d normal = new Vec3d(Math.cos(a) * rr, y, Math.sin(a) * rr);
            cagePanels
                    .get(n)
                    .endpoints(
                            hub.add(
                                    normal.scale(
                                            WulfrumCoordinationGeometry.cageRadius(
                                                    normal, cageDent, cageStrength))),
                            hub);
        }
        if (elapsedTicks < 235) {
            int q = elapsedTicks % 35;
            if (q == 0) {
                marks[10] = hub.add(player.getPositionEyes(1).subtract(hub).normalize().scale(12));
            }
            if (q > 25) {
                move(brawler, marks[10], 4);
                if (q == 32) {
                    impact(brawler.getPositionVector());
                    cageDent = marks[10].subtract(hub).normalize();
                    cageStrength = 1;
                }
            } else move(brawler, orbit(elapsedTicks * .055, 18, 8), 3);
            Vec3d next = seer.pupil().add(cageVelocity), offset = next.subtract(hub);
            double wall =
                    WulfrumCoordinationGeometry.cageRadius(offset, cageDent, cageStrength) - 1.7;
            if (offset.lengthVector() > wall) {
                Vec3d normal = offset.normalize();
                cageVelocity = WulfrumCoordinationGeometry.reflect(cageVelocity, normal);
                next = hub.add(normal.scale(wall));
                impact(next);
            }
            sawPass(next.addVector(0, -1.5, 0), 3);
            if (q == 15) ray(observer.pupil(), player.getPositionEyes(1), 12, 8);
        } else {
            if (elapsedTicks == 235) {
                burst(center.addVector(0, 8, 0), 12);
                aimCharge(brawler, player.getPositionEyes(1));
            }
            if (elapsedTicks > 253 && parryCount == 0) chargeBody(marks[11], 6);
            if (parryCount > 0) recovery();
        }
    }

    private void rails() {
        move(observer, center.addVector(-24, 7, 0), 2);
        if (elapsedTicks < 220) {
            if (elapsedTicks % 14 == 0)
                for (int side : new int[] {-1, 1})
                    ray(
                            observer.pupil().addVector(0, 0, side * 3),
                            center.addVector(26, 4, side * 3),
                            8,
                            7);
            int q = elapsedTicks % 60, round = elapsedTicks / 60;
            double sign = round % 2 == 0 ? 1 : -1;
            move(brawler, center.addVector(sign * (-16 + q * .6), 7, -10 + q * .34), 4);
            if (q == 25) {
                impact(seer.pupil());
                marks[0] = center.addVector(sign * 20, 4, round == 2 ? 12 : 0);
            }
            sawPass(marks[0], q > 25 ? 4.5 : 1.5);
        } else {
            if (elapsedTicks == 220) {
                marks[0] = player.getPositionEyes(1);
                marks[1] = marks[0].add(marks[0].subtract(seer.pupil()).normalize().scale(18));
                move(brawler, marks[0].addVector(0, 20, 0), 4);
                ray(seer.pupil(), marks[1], 18, 0);
            }
            if (elapsedTicks > 238) {
                sawPass(marks[1], 5.5);
                chargeBody(marks[0].addVector(0, -10, 0), 5);
            }
        }
    }

    private void parity() {
        move(observer, center.addVector(0, 12, 20), 2);
        move(seer, orbit(elapsedTicks * .08, 12, 8), 3.5);
        chain(observer.pupil(), seer.pupil());
        if (parryCount >= 4) {
            if (punishTick < 0) {
                impact(brawler.getPositionVector());
                recovery();
            }
            return;
        }
        int cycle = (elapsedTicks - 1) % 78;
        if (replyTick >= 0 && elapsedTicks - replyTick == 14) {
            if (parryCount % 2 == 1) {
                for (int n = 0; n < 4; n++)
                    ray(
                            observer.pupil(),
                            brawler.getPositionVector().addVector(n % 2 == 0 ? -1 : 1, n / 2, 0),
                            2,
                            0);
            } else {
                move(seer, brawler.getPositionVector(), 5);
                impact(brawler.getPositionVector());
            }
            brawler.engineMask(31);
            aimCharge(brawler, player.getPositionEyes(1));
        }
        if (elapsedTicks < 28) {
            move(brawler, center.addVector(0, 5, -23), 3);
            if (elapsedTicks == 20) aimCharge(brawler, player.getPositionEyes(1));
        } else if (replyTick >= 0 && elapsedTicks - replyTick < 30)
            move(brawler, brawler.getPositionVector(), 0);
        else if (cycle < 65) chargeBody(marks[11], 4.5 + parryCount * .7);
        else {
            move(brawler, center.addVector(0, 5, -23), 4);
            if (cycle == 77) aimCharge(brawler, player.getPositionEyes(1));
        }
    }

    private void overclock() {
        Vec3d forge = center.addVector(0, 7, 0);
        move(observer, forge.addVector(0, 0, 12), 2);
        if (elapsedTicks < 110) {
            move(seer, forge, 2);
            move(brawler, forge.addVector(0, 0, -5), 2);
            brawler.engineMask(31);
            if (elapsedTicks % 4 == 0) ray(observer.pupil(), seer.pupil(), 0, 0);
            if (elapsedTicks % 8 == 0) wakeLine(brawler.getPositionVector(), seer.pupil(), 12);
        } else if (elapsedTicks < 170) {
            if (elapsedTicks == 110) {
                marks[0] = player.getPositionEyes(1);
                marks[1] = marks[0].add(marks[0].subtract(forge).normalize().scale(22));
                ray(seer.pupil(), marks[1], 18, 0);
            }
            if (elapsedTicks > 128) {
                Vec3d old = seer.pupil();
                sawPass(marks[1], 5.5);
                if (elapsedTicks % 6 == 0)
                    spawn(
                            new EntityBrawlerEffect(
                                    brawler,
                                    EntityBrawlerEffect.PLANE,
                                    old,
                                    seer.pupil(),
                                    5,
                                    85,
                                    2,
                                    6));
            }
        } else if (elapsedTicks < 205) {
            move(brawler, marks[0], 5);
            if (elapsedTicks == 190) {
                impact(marks[0]);
                for (int n = 0; n < 18; n++)
                    shard(marks[0].addVector(Math.cos(n) * 8, n % 4, Math.sin(n) * 8), 1).hold();
            }
        } else accelerateHeldShards();
    }

    private void vectors() {
        int round = elapsedTicks / 70, q = elapsedTicks % 70;
        double angle = round * Math.PI / 4;
        Vec3d u = new Vec3d(Math.cos(angle), Math.sin(angle), 0),
                v = new Vec3d(-u.y, u.x, 0),
                hub = center.addVector(0, 8, 0);
        move(observer, center.addVector(0, 21, 12), 2);
        if (q < 22) {
            move(seer, hub.add(u.scale(-20)), 4);
            move(brawler, hub.add(v.scale(-20)), 4);
            if (q == 12) {
                marks[0] = player.getPositionEyes(1);
                marks[1] = marks[0].add(u.scale(22));
                marks[2] = marks[0].add(v.scale(22));
                ray(seer.pupil(), marks[1], 12, 0);
                ray(brawler.getPositionVector(), marks[2], 20, 0);
            }
        } else {
            sawPass(marks[1], 5);
            if (q > 30) chargeBody(marks[2], 5.5);
            suppress(marks[0].addVector(0, 0, q % 2 == 0 ? 4 : -4), 10);
        }
        if (round >= 3 && parryCount > 0) recovery();
    }

    private void booster() {
        boolean mass = pattern == Pattern.attack_46, burrow = pattern == Pattern.attack_42;
        int q = (elapsedTicks - 1) % 100, round = (elapsedTicks - 1) / 100;
        if (mass && parryCount >= 3) {
            recovery();
            return;
        }
        if (!mass && parryCount >= 2) {
            recovery();
            return;
        }
        if (!mass && parryCount == 1) {
            if (elapsedTicks - replyTick < 28) {
                move(brawler, center.addVector(0, 13, -18), 4);
                move(excavator, excavator.getPositionVector().add(launchDirection.scale(1.5)), 1.5);
                if (elapsedTicks - replyTick == 27) aimCharge(brawler, player.getPositionEyes(1));
            } else chargeBody(marks[11], 5.5);
            return;
        }
        if (q < 30) {
            move(excavator, center.addVector(-27, burrow ? -6 : 2, 0), 3);
            Vec3d rear = excavator.segment(17, 1);
            move(brawler, mass ? rear.addVector(-14, 4, 0) : rear.addVector(-2, 0, 0), 4);
            if (mass) {
                deploy(6);
                for (int n = 0; n < probes.size(); n++) {
                    place(
                            n,
                            brawler.getPositionVector()
                                    .addVector(
                                            Math.cos(n * Math.PI / 3) * 5,
                                            Math.sin(n * Math.PI / 3) * 5,
                                            0),
                            false);
                    if (q == 12 + n && alive(n))
                        ray(probes.get(n).getPositionVector(), brawler.getPositionVector(), 3, 0);
                }
            }
            if (q == 22) {
                marks[0] = player.getPositionEyes(1);
                marks[1] =
                        marks[0].add(
                                marks[0].subtract(excavator.getPositionVector())
                                        .normalize()
                                        .scale(22));
                excavator.beam(excavator.drillTip(), marks[1], 20, 3, 0);
            }
        } else if (q < 46) {
            Vec3d rear = excavator.segment(17, 1);
            move(brawler, rear, 5);
            brawler.engineMask(31);
            if (q == 40) {
                impact(rear);
                hitstopTicks = 3;
            }
            if (burrow) move(excavator, excavator.getPositionVector().addVector(1, 1, 0), 2);
        } else if (q < 80) {
            bore(marks[1]);
            move(brawler, excavator.segment(17, 1), 5);
            if (q % 8 == 0) excavator.dust(excavator.ground(excavator.getPositionVector()), 12);
        } else {
            move(excavator, center.addVector(-25, 3, 0), 3);
            move(brawler, excavator.segment(17, 1).addVector(-8, 2, 0), 4);
        }
    }

    private void probeSlingshot() {
        deploy(6);
        move(excavator, orbit(elapsedTicks * .02, 25, -2), 1.8);
        Vec3d hub = center.addVector(0, 14, 0);
        for (int n = 0; n < 6; n++)
            place(
                    n,
                    hub.addVector(
                            Math.cos(n * Math.PI / 3) * 12, 0, Math.sin(n * Math.PI / 3) * 12),
                    parryCount > 0);
        if (elapsedTicks < 55) {
            move(brawler, hub, 3);
            if (elapsedTicks == 22 || elapsedTicks == 45)
                for (EntityExcavatorProbe p : probes)
                    if (!p.isDead) ray(p.getPositionVector(), brawler.getPositionVector(), 4, 0);
        } else if (elapsedTicks < 185) {
            int n = Math.min(5, (elapsedTicks - 55) / 21);
            if (alive(n)) {
                Vec3d at = probes.get(n).getPositionVector();
                move(brawler, at, 5);
                if ((elapsedTicks - 55) % 21 == 16) {
                    ray(at, brawler.getPositionVector(), 0, 0);
                    impact(at);
                }
            }
        } else if (parryCount == 0) {
            if (elapsedTicks == 185) aimCharge(brawler, player.getPositionEyes(1));
            if (elapsedTicks > 203) chargeBody(marks[11], 6);
        } else {
            for (int n = 0; n < probes.size(); n++) {
                Vec3d at =
                        hub.addVector(
                                Math.cos(n * Math.PI / 3) * 12, 0, Math.sin(n * Math.PI / 3) * 12);
                place(
                        n,
                        at.add(
                                brawler.getPositionVector()
                                        .subtract(at)
                                        .scale(Math.min(1, (elapsedTicks - replyTick) / 35D))),
                        true);
            }
            move(brawler, hub.addVector(0, -10, 0), 3);
            if (elapsedTicks - replyTick > 45) recovery();
        }
    }

    private void missileCross() {
        if (elapsedTicks < 45) {
            move(excavator, center.addVector(-25, -5, 0), 3);
            move(brawler, center.addVector(0, 25, 0), 3);
            if (elapsedTicks == 25) {
                marks[0] = player.getPositionEyes(1);
                marks[1] = marks[0].addVector(20, 0, 0);
                excavator.fault(center.addVector(-25, 0, 0), marks[0], 24, 0);
                ray(brawler.getPositionVector(), marks[0], 16, 0);
            }
        } else if (elapsedTicks < 59) {
            chargeBody(marks[0].addVector(0, -8, 0), 5);
        } else if (parryCount == 0) {
            bore(marks[1]);
            move(brawler, center.addVector(8, 13, 0), 3);
        } else if (parryCount == 1) {
            if (elapsedTicks - replyTick < 30) {
                move(excavator, brawler.getPositionVector(), 3.5);
                move(brawler, center.addVector(15, 20, 0), 3);
                if (elapsedTicks - replyTick == 25) {
                    impact(brawler.getPositionVector());
                    aimCharge(brawler, player.getPositionEyes(1));
                }
            } else chargeBody(marks[11], 5.5);
        } else recovery();
    }

    private void launchpad() {
        if (elapsedTicks == 1) {
            platform =
                    new EntityExcavatorPayload(
                            excavator,
                            EntityExcavatorPayload.CHUNK,
                            center.addVector(0, -1, 0),
                            center,
                            0,
                            300,
                            8,
                            0);
            spawn(platform);
        }
        if (elapsedTicks < 130) {
            double y = Math.max(0, elapsedTicks - 30) * .18;
            platform.place(center.addVector(0, y - 1, 0));
            move(brawler, center.addVector(0, y - 4, 0), 3);
            move(excavator, center.addVector(0, y - 12, 0), 3);
            brawler.engineMask(elapsedTicks < 30 ? 1 : 31);
            if (elapsedTicks == 30 || elapsedTicks == 90) {
                excavator.shock(platform.getPositionVector(), 10);
                impact(platform.getPositionVector());
            }
        } else if (elapsedTicks == 130) {
            platform.splitInHalf();
            excavator.debris(platform.getPositionVector(), 20);
            marks[0] = player.getPositionEyes(1);
        } else if (elapsedTicks < 170) {
            move(excavator, marks[0].addVector(0, 22, 0), 4);
            move(brawler, marks[0].addVector(0, -12, 0), 3);
            if (elapsedTicks == 152)
                excavator.beam(excavator.drillTip(), marks[0].addVector(0, -15, 0), 20, 5, 0);
        } else if (parryCount == 0) {
            bore(marks[0].addVector(0, -15, 0));
            move(brawler, marks[0], 2);
        } else {
            move(excavator, brawler.getPositionVector(), 4);
            if (excavator.getDistanceSq(brawler) < 25) {
                impact(brawler.getPositionVector());
                excavator.shock(brawler.getPositionVector(), 24);
                hitstopTicks = 10;
                recovery();
            }
        }
    }

    private void stripSaw() {
        move(
                excavator,
                center.addVector(
                        -24 + Math.min(elapsedTicks, 240) * .2,
                        -1,
                        Math.sin(elapsedTicks * .02) * 8),
                2);
        Vec3d rail = excavator.segment(4, 1).addVector(0, 3, 0);
        move(seer, rail, 3.5);
        move(observer, rail.addVector(0, 8, 8), 3);
        chain(observer.pupil(), seer.pupil());
        seer.slash(9);
        if (elapsedTicks > 25 && elapsedTicks < 215 && elapsedTicks % 10 == 0) {
            impact(rail);
            for (int n = 0; n < 3; n++) shard(rail.addVector(n - 1, 3 + n, 0), 1).hold();
        }
        if (elapsedTicks > 50) accelerateHeldShards();
    }

    private void mountedTurret() {
        Vec3d path =
                center.addVector(
                        Math.cos(elapsedTicks * .035) * 16, -5, Math.sin(elapsedTicks * .035) * 16);
        if (elapsedTicks < 180) {
            move(excavator, path, 2.5);
            Vec3d mount = excavator.segment(3, 1);
            double surface = excavator.ground(mount).y;
            move(observer, new Vec3d(mount.x, surface + 3, mount.z), 3);
            move(
                    seer,
                    observer.getPositionVector()
                            .addVector(
                                    Math.cos(elapsedTicks * .13) * 8,
                                    0,
                                    Math.sin(elapsedTicks * .13) * 8),
                    4);
            chain(observer.pupil(), seer.pupil());
            seer.slash(9);
            if (elapsedTicks > 30) suppress(player.getPositionEyes(1), 9);
        } else if (elapsedTicks < 210) {
            move(excavator, center.addVector(0, 22, 0), 4);
            move(observer, excavator.getPositionVector().addVector(3, 5, 0), 4);
            move(seer, observer.getPositionVector().addVector(5, 0, 0), 4);
            if (elapsedTicks == 195) {
                marks[0] = player.getPositionEyes(1);
                marks[1] = marks[0].addVector(0, -12, 0);
                ray(seer.pupil(), marks[1], 18, 0);
            }
        } else sawPass(marks[1], 5.5);
    }

    private void probeFactory() {
        boolean factory = pattern == Pattern.attack_53;
        deploy(factory ? 12 : 6);
        if (!factory || elapsedTicks < 300) move(excavator, orbit(elapsedTicks * .025, 22, -3), 2);
        move(observer, center.addVector(0, 13, 0), 2);
        boolean pending = false;
        for (int n = 0; n < probes.size(); n++)
            if (alive(n) && probeStage[n] == 0 && elapsedTicks - 30 - n * (factory ? 16 : 30) >= 18)
                pending = true;
        if (!factory || elapsedTicks < 300) {
            if (!pending) move(seer, orbit(elapsedTicks * .12, 13, 6), 4);
            chain(observer.pupil(), seer.pupil());
            seer.slash(8);
        }
        boolean serving = false;
        for (int n = 0; n < probes.size(); n++) {
            if (!alive(n)) continue;
            if (factory && elapsedTicks >= 300) {
                place(n, excavator.segment(n % 18, 1), false);
                continue;
            }
            int age = elapsedTicks - 30 - n * (factory ? 16 : 30);
            Vec3d station = orbit(n * Math.PI / 3, 13, 6);
            if (age < 0) place(n, excavator.segment(n % 18, 1), false);
            else if (age < 18) {
                place(n, station, false);
                if (age == 8) ray(observer.pupil(), station, 6, 0);
            } else if (probeStage[n] == 0) {
                place(n, station, true);
                if (!serving) {
                    serving = true;
                    Vec3d previous = seer.pupil();
                    move(seer, station.addVector(0, -1.5, 0), 4);
                    if (segmentDistance(station, previous, seer.pupil()) < 2.5) {
                        impact(station);
                        probeStage[n] = 1;
                        probeAge[n] = elapsedTicks;
                        probeTargets[n] = player.getPositionEyes(1);
                    }
                }
            } else {
                int travel = elapsedTicks - probeAge[n];
                Vec3d target = probeTargets[n],
                        end = target.add(target.subtract(station).normalize().scale(8));
                if (travel < 18)
                    place(n, station.add(end.subtract(station).scale(travel / 18D)), true);
                else if (travel < 36) {
                    if (travel == 18)
                        ray(probes.get(n).getPositionVector(), player.getPositionEyes(1), 8, 6);
                    Vec3d back = excavator.segment(n % 18, 1);
                    place(
                            n,
                            probes.get(n)
                                    .getPositionVector()
                                    .add(
                                            back.subtract(probes.get(n).getPositionVector())
                                                    .scale(.18)),
                            false);
                } else if (factory) place(n, excavator.segment(n % 18, 1), false);
                else if (elapsedTicks < 245) place(n, station, true);
            }
        }
        if (!factory && elapsedTicks >= 245) {
            for (int n = 0; n < probes.size(); n++) place(n, excavator.segment(n % 18, 1), false);
        }
        if (factory && elapsedTicks >= 300) {
            if (elapsedTicks == 300) {
                marks[0] = player.getPositionEyes(1);
                marks[1] = marks[0].add(marks[0].subtract(seer.pupil()).normalize().scale(20));
                ray(observer.pupil(), seer.pupil(), 8, 0);
                ray(seer.pupil(), marks[1], 25, 0);
            }
            if (elapsedTicks < 326)
                move(excavator, seer.getPositionVector().addVector(0, -4, 0), 4);
            else if (elapsedTicks < 350) sawPass(marks[1], 6);
            else {
                chain(observer.pupil(), seer.pupil());
                move(seer, observer.getPositionVector().addVector(4, 0, 0), 4);
            }
        }
    }

    private void conductedFaults() {
        move(excavator, orbit(elapsedTicks * .04, 19, -5), 2.5);
        move(observer, center.addVector(0, 15, 0), 2);
        if (elapsedTicks == 10)
            for (int n = 0; n < 5; n++) {
                Vec3d a = center.addVector(-22, .1, n * 7 - 14),
                        z = center.addVector(22, .1, n * 7 - 14);
                faults.add(new Vec3d[] {a, z});
                excavator.fault(a, z, 260, 0);
            }
        if (elapsedTicks > 35 && elapsedTicks < 250) {
            int n = (elapsedTicks - 35) / 40 % 5, q = (elapsedTicks - 35) % 40;
            Vec3d[] f = faults.get(n);
            Vec3d at = f[0].add(f[1].subtract(f[0]).scale(q / 40D));
            if (q % 8 == 0) {
                ray(observer.pupil(), at, 6, 0);
                excavator.fault(at, at.addVector(4, 0, 0), 8, 8);
            }
            if (q <= 24) move(seer, center.addVector(-20, 3, n * 7 - 10.5), 3);
            if (q == 12) {
                marks[0] = center.addVector(22, 3, n * 7 - 10.5);
                ray(seer.pupil(), marks[0], 12, 0);
            }
            if (q > 24) sawPass(marks[0], 5);
        }
    }

    private void sawtooth() {
        int leg = Math.min(7, (elapsedTicks - 1) / 36), q = (elapsedTicks - 1) % 36;
        Vec3d a = center.addVector(leg % 2 == 0 ? -20 : 20, -5, -21 + leg * 6),
                z = center.addVector(leg % 2 == 0 ? 20 : -20, -5, -15 + leg * 6);
        move(observer, center.addVector(0, 18, 0), 2);
        if (q < 12) {
            move(excavator, a, 4);
            move(seer, z.addVector(0, 8, 0), 4);
            if (q == 5) {
                excavator.fault(a.addVector(0, 5, 0), z.addVector(0, 5, 0), 16, 0);
                ray(seer.pupil(), a.addVector(0, 8, 0), 16, 0);
            }
        } else {
            double f = Math.min(1, (q - 12) / 18D);
            move(excavator, a.add(z.subtract(a).scale(f)), 4);
            sawPass(z.add(a.subtract(z).scale(f)).addVector(0, 8, 0), 5);
            if (q == 22) {
                Vec3d mid = a.add(z).scale(.5).addVector(0, 6, 0);
                move(excavator, mid, 5);
                excavator.shock(mid, 8);
                impact(mid);
            }
            if (q >= 26 && q % 4 == 0)
                ray(
                        observer.pupil(),
                        a.add(z.subtract(a).scale((q - 26) / 10D)).addVector(0, 6, 0),
                        6,
                        7);
        }
    }

    private void eclipse() {
        deploy(6);
        move(observer, center.addVector(0, 20, 0), 2);
        double lower = WulfrumCoordinationGeometry.alignedAngle(elapsedTicks, .045, Math.PI / 2),
                middle = WulfrumCoordinationGeometry.alignedAngle(elapsedTicks, -.065, Math.PI / 2),
                upper = WulfrumCoordinationGeometry.alignedAngle(elapsedTicks, .11, -Math.PI / 2);
        if (elapsedTicks <= 245) {
            move(
                    excavator,
                    orbit(lower + Math.PI / 4, WulfrumCoordinationGeometry.eclipseRadius(), 0),
                    3);
            move(seer, orbit(upper, 12, 13), 4);
            chain(observer.pupil(), seer.pupil());
            seer.slash(8);
        }
        for (int n = 0; n < 6; n++)
            if (elapsedTicks <= 245)
                place(n, orbit(middle + Math.PI / 4 + n * Math.PI * 1.5 / 5, 15, 7), true);
        if (elapsedTicks > 35 && elapsedTicks < 210 && elapsedTicks % 12 == 0)
            ray(
                    observer.pupil(),
                    center.addVector(
                            Math.cos(elapsedTicks * .1) * 3, 0, Math.sin(elapsedTicks * .1) * 3),
                    10,
                    7);
        if (elapsedTicks == 210) {
            safeAxis = new Vec3d(0, 0, 1);
            for (int side : new int[] {-1, 1})
                ray(center.addVector(side * 3, 1, 0), center.addVector(side * 3, 1, 25), 30, 0);
        }
        if (elapsedTicks > 245) {
            move(excavator, center.addVector(-25, 0, 0), 4);
            sawPass(center.addVector(25, 3, 0), 5);
            for (int n = 0; n < 6; n++)
                place(n, orbit(n * Math.PI / 3 + .4, 15 - (elapsedTicks - 245) * .35, 7), true);
        }
    }
}
