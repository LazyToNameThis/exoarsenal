package com.exoarsenal.entity;

import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import static com.exoarsenal.entity.BrawlerMartialScore.*;
import static com.exoarsenal.entity.EntityBrawlerEffect.*;

final class BrawlerMartialCombat {
    private final EntityBrawler b;
    private final List<EntityBrawlerEffect> effects = new ArrayList<>();
    private final Set<Integer> parriedBeats = new HashSet<>();
    private final Vec3d[] steps = new Vec3d[4];
    private Vec3d locked = Vec3d.ZERO, start = Vec3d.ZERO, recoil = Vec3d.ZERO;
    private EntityPlayer held;
    private EntityArrow caught;
    private int hitstop, reaction, reactionTick, lastContact = -1, counterTick = -1;
    private boolean sweepHit;

    BrawlerMartialCombat(EntityBrawler boss) {
        b = boss;
    }

    private Attack attack() {
        return decode(b.martialAttack());
    }

    void reset() {
        release();
        if (caught != null && !caught.isDead) {
            caught.setNoGravity(false);
            caught.motionY = -.2;
        }
        caught = null;
        for (EntityBrawlerEffect e : effects) e.setDead();
        effects.clear();
        parriedBeats.clear();
        hitstop = reaction = reactionTick = 0;
        lastContact = -1;
        counterTick = -1;
        sweepHit = false;
        b.martialReaction(0, 0);
        b.martialVictim(-1);
        if (!b.world.isRemote)
            com.exoarsenal.world.ExcavatorTerrain.get(b.world).release(b.getUniqueID());
    }

    boolean paused() {
        return hitstop > 0 || reaction > 0;
    }

    private Vec3d ground(Vec3d p) {
        BlockPos at = new BlockPos(p);
        for (int i = 0; i < 100 && !b.world.getBlockState(at.down()).getMaterial().isSolid(); i++)
            at = at.down();
        for (int i = 0; i < 20 && b.world.getBlockState(at).getMaterial().isSolid(); i++)
            at = at.up();
        return new Vec3d(p.x, at.getY() + .05, p.z);
    }

    private void move(Vec3d goal, double speed) {
        Vec3d delta = goal.subtract(b.getPositionVector());
        if (delta.lengthVector() > speed) delta = delta.normalize().scale(speed);
        b.motionX = b.motionY = b.motionZ = 0;
        b.combatMove(delta);
    }

    private void approach(EntityLivingBase p, double speed, double distance) {
        Vec3d d = p.getPositionVector().subtract(b.getPositionVector());
        d = new Vec3d(d.x, 0, d.z).normalize();
        move(ground(p.getPositionVector().subtract(d.scale(distance))), speed);
    }

    private void lock(EntityLivingBase p) {
        locked = p.getPositionVector();
        start = b.getPositionVector();
    }

    private EntityBrawlerEffect effect(
            int type, Vec3d at, Vec3d end, int warning, int life, float size, float damage) {
        EntityBrawlerEffect e =
                new EntityBrawlerEffect(b, type, at, end, warning, life, size, damage);
        effects.add(e);
        b.world.spawnEntity(e);
        return e;
    }

    private void wave(Vec3d at, float radius, float damage) {
        effect(RING, ground(at), Vec3d.ZERO, 5, 28, radius, damage);
    }

    private void impact(Vec3d at) {
        ((net.minecraft.world.WorldServer) b.world)
                .spawnParticle(
                        net.minecraft.util.EnumParticleTypes.CRIT_MAGIC,
                        at.x,
                        at.y,
                        at.z,
                        18,
                        .4,
                        .5,
                        .4,
                        .2);
        b.world.playSound(
                null,
                at.x,
                at.y,
                at.z,
                net.minecraft.init.SoundEvents.BLOCK_ANVIL_LAND,
                SoundCategory.HOSTILE,
                1,
                .75F);
    }

    private void cell(Vec3d at, int delay) {
        effect(
                GROUND_CELL,
                ground(at).addVector(0, .05, 0),
                Vec3d.ZERO,
                delay,
                delay + 16,
                1.65F,
                8);
    }

    private void grab(EntityLivingBase p) {
        if (!(p instanceof EntityPlayer) || p.getDistanceSq(b) > 36) return;
        EntityPlayer player = (EntityPlayer) p;
        if (player.isCreative()
                || player.isSpectator()
                || com.exoarsenal.combat.WeaponCombat.dodgeReady(player)) return;
        held = player;
        b.martialVictim(player.getEntityId());
    }

    private void release() {
        if (held != null) {
            held.fallDistance = 0;
            held.velocityChanged = true;
        }
        held = null;
        b.martialVictim(-1);
    }

    private void hold(Vec3d at) {
        if (held == null) return;
        if (!held.isEntityAlive() || held.isSpectator() || held.getDistanceSq(b) > 1600) {
            release();
            return;
        }
        Vec3d delta = at.subtract(held.getPositionVector());
        int slices = Math.max(1, (int) Math.ceil(delta.lengthVector() / .4));
        for (int i = 1; i <= slices; i++)
            if (!b.world
                    .getCollisionBoxes(
                            held,
                            held.getEntityBoundingBox().offset(delta.scale(i / (double) slices)))
                    .isEmpty()) {
                release();
                return;
            }
        held.motionX = held.motionY = held.motionZ = 0;
        held.fallDistance = 0;
        if (held instanceof EntityPlayerMP)
            ((EntityPlayerMP) held)
                    .connection.setPlayerLocation(
                            at.x, at.y, at.z, held.rotationYaw, held.rotationPitch);
        else held.setPosition(at.x, at.y, at.z);
    }

    private void launch(double y, Vec3d horizontal) {
        if (held == null) return;
        EntityPlayer p = held;
        release();
        p.addVelocity(horizontal.x, y, horizontal.z);
        p.velocityChanged = true;
    }

    boolean counter(DamageSource source) {
        if (attack() != Attack.CATCH_RETURN || b.clock() < 20 || b.clock() > 78 || counterTick >= 0)
            return false;
        Entity object = source.getImmediateSource();
        if (source.isProjectile() && !(object instanceof EntityArrow)) return false;
        counterTick = b.clock();
        if (object instanceof EntityArrow) {
            caught = (EntityArrow) object;
            caught.motionX = caught.motionY = caught.motionZ = 0;
            caught.setNoGravity(true);
        }
        impact(b.localToWorld(b.hand(0, 0)));
        return true;
    }

    void parried(EntityPlayer player) {
        if (lastContact < 0 || parriedBeats.contains(lastContact)) return;
        parriedBeats.add(lastContact);
        release();
        recoil = b.getPositionVector().subtract(player.getPositionVector());
        recoil = new Vec3d(recoil.x, 0, recoil.z).normalize();
        hitstop = 6;
        impact(b.getPositionVector().addVector(0, 3, 0));
        if (finalPunish(attack(), lastContact, parriedBeats.size())) {
            reaction = 3;
            reactionTick = 0;
            hitstop = 14;
        } else if (attack() == Attack.ROCKET_DROPKICK
                || attack() == Attack.MARTIAL_PROTOCOL && lastContact == 316) {
            reaction = 2;
            reactionTick = 0;
        } else if (attack() == Attack.GROUNDED_MISSILE || attack() == Attack.WALL_BOUNCE) {
            reaction = 4;
            reactionTick = 0;
        }
        b.martialReaction(reaction, 0);
    }

    void recover() {
        if (hitstop > 0) {
            hitstop--;
            b.motionX = b.motionY = b.motionZ = 0;
            return;
        }
        if (reaction == 0) return;
        int t = ++reactionTick;
        b.martialReaction(reaction, t);
        if (reaction == 3) {
            if (t < 28) {
                Vec3d step = recoil.scale(1.2 * (1 - t / 28D));
                move(ground(b.getPositionVector().add(step)), step.lengthVector());
                if (t % 3 == 0) {
                    for (int side : new int[] {-1, 1}) {
                        Vec3d foot = b.localToWorld(new Vec3d(side * 1.1, -4.4, 0));
                        com.exoarsenal.world.ExcavatorTerrain.get(b.world)
                                .cut(b, new BlockPos(ground(foot)).down());
                    }
                    impact(b.getPositionVector());
                }
            }
            if (t == 12) b.martialBlownArm(1);
            if (t % 12 == 0 && t < 130) impact(b.localToWorld(b.hand(1, 0)));
            int duration = attack() == Attack.MARTIAL_PROTOCOL ? 150 : 100;
            if (t >= duration) {
                reset();
                b.advanceMartial();
            }
        } else {
            double f = Math.min(1, t / 42D);
            Vec3d at = start.add(recoil.scale(8 * f)).addVector(0, Math.sin(f * Math.PI) * 3, 0);
            move(t >= 42 ? ground(at) : at, 1.2);
            if (t >= 42) {
                reaction = 0;
                b.martialReaction(0, 0);
                if (attack() == Attack.GROUNDED_MISSILE || attack() == Attack.WALL_BOUNCE) {
                    reset();
                    b.advanceMartial();
                }
            }
        }
    }

    void transition() {
        Vec3d floor = ground(b.getPositionVector());
        move(floor, 1.8);
        if (b.transition() == 65) {
            wave(floor, 10, 0);
            impact(floor);
        }
        b.engineMask(0);
    }

    void tick(EntityLivingBase p) {
        int t = b.clock();
        Attack a = attack();
        if (t == 1) {
            reset();
            lock(p);
            Arrays.fill(steps, ground(p.getPositionVector()));
            b.martialBlownArm(-1);
        }
        effects.removeIf(e -> e.isDead);
        switch (a) {
            case KICKBOXING:
                if (t < 140) approach(p, .35, 3.2);
                else if (t < 154)
                    move(
                            ground(
                                    start.subtract(
                                            p.getPositionVector()
                                                    .subtract(start)
                                                    .normalize()
                                                    .scale(5))),
                            .8);
                else {
                    if (t == 154) lock(p);
                    move(
                            ground(locked)
                                    .addVector(
                                            0,
                                            Math.sin(Math.min(1, (t - 154) / 26D) * Math.PI) * 2,
                                            0),
                            1.1);
                }
                if (t == 110) wave(b.footWorld(1, 0), 9, 7);
                break;
            case ROCKET_DROPKICK:
                if (t < 32) approach(p, .15, 8);
                else if (t < 49) {
                    Vec3d away = b.getPositionVector().subtract(p.getPositionVector()).normalize();
                    move(ground(b.getPositionVector().add(away.scale(.7))).addVector(0, 1, 0), .9);
                } else if (t < 86) {
                    if (t == 49) lock(p);
                    move(ground(locked).addVector(0, 1.2, 0), 1.6);
                } else approach(p, .55, 3);
                break;
            case PARRY_STRING:
                approach(p, t < 140 ? .28 : .08, 3.1);
                break;
            case SUPLEX:
                grapple(p, t, false);
                break;
            case STOMP:
                if (t < 95) approach(p, .18, 3);
                else if (t < 126) {
                    if (t == 95) lock(p);
                    double f = (t - 95) / 31D;
                    move(ground(locked).addVector(0, Math.sin(f * Math.PI) * 5, 0), 1.2);
                } else move(ground(b.getPositionVector()), 1.5);
                if (t == 40 || t == 78) wave(b.footWorld(t == 40 ? 0 : 1, 0), 11, 9);
                if (t == 128) {
                    wave(b.getPositionVector(), 15, 10);
                    for (int x = -3; x <= 3; x++)
                        for (int z = -3; z <= 3; z++)
                            if (x * x + z * z <= 10)
                                cell(
                                        b.getPositionVector()
                                                .addVector(x * 3 + (z % 2) * 1.5, 0, z * 2.6),
                                        10 + (Math.abs(x) + Math.abs(z)) * 5);
                }
                break;
            case ROUNDHOUSE:
                approach(p, t < 80 ? .35 : .65, 3);
                if (t == 150) {
                    impact(b.footWorld(0, 0));
                    effect(
                            FAULT,
                            ground(b.getPositionVector()),
                            ground(b.getPositionVector().add(p.getLookVec().scale(7))),
                            8,
                            24,
                            .5F,
                            5);
                }
                break;
            case GROUNDED_MISSILE:
                for (int hit : new int[] {48, 94, 145})
                    if (t >= hit - 28 && t <= hit + 5) {
                        if (t == hit - 28) lock(p);
                        move(ground(locked), t < hit - 12 ? .4 : 1.5 + (hit / 50) * .25);
                        if (t % 5 == 0) impact(b.footWorld(t % 2, 0));
                    }
                break;
            case FOOTWORK:
                footwork(p, t, false);
                break;
            case WALL_BOUNCE:
                bounce(p, t);
                break;
            case CATCH_RETURN:
                if (counterTick < 0) approach(p, .18, 4.5);
                else {
                    int q = t - counterTick;
                    approach(p, q < 18 ? .35 : .65, 2.8);
                    if (caught != null && !caught.isDead) {
                        Vec3d hand = b.localToWorld(b.hand(0, 0));
                        caught.setPosition(hand.x, hand.y, hand.z);
                        if (q >= 24) {
                            caught.shootingEntity = b;
                            Vec3d d = p.getPositionEyes(1).subtract(hand).normalize().scale(2.2);
                            caught.motionX = d.x;
                            caught.motionY = d.y;
                            caught.motionZ = d.z;
                            caught.setNoGravity(false);
                            caught.velocityChanged = true;
                            caught = null;
                        }
                    } else if (q == 24) {
                        lastContact = t;
                        b.martialStrike(1, 11, 1.5, true);
                    }
                }
                break;
            case PISTON_PILEDRIVER:
                grapple(p, t, true);
                break;
            case FOUR_LIMB:
                approach(p, .4, 3);
                break;
            case GROUND_POUND:
                if (t < 40) approach(p, .4, 3);
                else if (sweepHit) approach(p, .7, 1.8);
                else {
                    if (t == 48) {
                        lastContact = t;
                        b.martialStrike(1, 10, 2, true);
                    }
                    approach(p, .25, 3);
                }
                break;
            case TESLA_GRID:
                footwork(p, t, true);
                break;
            case MARTIAL_PROTOCOL:
                if (t < 180) approach(p, .32, 3);
                else if (t < 207) {
                    if (t == 180) lock(p);
                    move(
                            ground(locked).addVector(0, Math.sin((t - 180) / 27D * Math.PI) * 6, 0),
                            1.2);
                    if (t == 193) grab(p);
                    if (held != null) hold(b.getPositionVector().addVector(0, 3, 2));
                    if (t == 205) launch(.7, p.getLookVec().scale(.6));
                } else if (t < 289) approach(p, .5, 3);
                else if (t < 300)
                    move(
                            ground(
                                    b.getPositionVector()
                                            .subtract(
                                                    p.getPositionVector()
                                                            .subtract(b.getPositionVector())
                                                            .normalize()
                                                            .scale(.7))),
                            .7);
                else if (t < 326) {
                    if (t == 300) lock(p);
                    move(ground(locked).addVector(0, 1, 0), 1.65);
                } else approach(p, t < 345 ? .3 : 1.35, 2.8);
                break;
        }
        for (Beat beat : beats(a))
            if (t == beat.tick) {
                if (a == Attack.GROUND_POUND && !sweepHit && t > 28) continue;
                lastContact = t;
                boolean hit;
                if (beat.limb == 5) {
                    hit = b.martialStrike(0, 14, 1.6, beat.parry);
                    if (!paused()) hit |= b.martialStrike(1, 14, 1.6, beat.parry);
                } else
                    hit =
                            b.martialStrike(
                                    beat.limb,
                                    beat.motion == Motion.JAB ? 8 : 12,
                                    beat.limb >= 2 ? 1.45 : 1.6,
                                    beat.parry);
                if (a == Attack.GROUND_POUND && t == 28) {
                    sweepHit = hit;
                    if (hit) {
                        p.addVelocity(0, .2, 0);
                        p.velocityChanged = true;
                    }
                }
                if (paused()) {
                    start = b.getPositionVector();
                    break;
                }
            }
        if (t >= a.duration && !paused()) {
            reset();
            b.advanceMartial();
        }
    }

    private void footwork(EntityLivingBase p, int t, boolean grid) {
        int length = grid ? 27 : 30,
                index = Math.min(grid ? 7 : 4, (t - 1) / length),
                q = (t - 1) % length;
        int slot = index < 4 ? index : 7 - index;
        if (q == 0) {
            Vec3d forward = p.getPositionVector().subtract(b.getPositionVector());
            forward = new Vec3d(forward.x, 0, forward.z).normalize();
            Vec3d side = new Vec3d(forward.z, 0, -forward.x);
            if (!grid || index < 4)
                steps[Math.floorMod(slot, 4)] =
                        ground(
                                p.getPositionVector()
                                        .subtract(forward.scale(index % 3 == 2 ? 5 : 3))
                                        .add(side.scale(index % 2 == 0 ? -2 : 2)));
            locked = steps[Math.floorMod(slot, 4)];
        }
        move(locked, grid ? .5 : .38);
        if (grid && q == 12) {
            cell(b.footWorld(0, 0), 34);
            cell(b.footWorld(1, 0), 34);
        }
        if (!grid && index == 4 && q < 18)
            move(locked.addVector(0, Math.sin(q / 18D * Math.PI) * 1.2, 0), .6);
    }

    private void bounce(EntityLivingBase p, int t) {
        int leg = (t - 1) / 38, q = (t - 1) % 38;
        if (leg < 3) {
            if (q == 0) {
                Vec3d direction =
                        leg == 0 ? p.getLookVec().rotateYaw(1.4F) : p.getLookVec().rotateYaw(-1.4F);
                Vec3d from = b.getPositionVector().addVector(0, 3, 0);
                RayTraceResult wall =
                        b.world.rayTraceBlocks(
                                from, from.add(direction.scale(28)), false, true, false);
                locked =
                        wall == null
                                ? ground(from.add(direction.scale(20)))
                                : wall.hitVec.subtract(direction.scale(2));
            }
            move(locked, 1.2 + leg * .35);
            if (q == 29) impact(b.getPositionVector());
        } else {
            if (t == 115) lock(p);
            move(ground(locked), 2);
        }
    }

    private void grapple(EntityLivingBase p, int t, boolean piledriver) {
        if (t < 35) {
            approach(p, .7, 2);
            if (t == 28) grab(p);
            return;
        }
        if (!piledriver && t >= 126) {
            if (t == 126) launch(1.5, Vec3d.ZERO);
            if (t < 166) {
                double f = (t - 126) / 40D;
                move(start.addVector(0, Math.sin(f * Math.PI) * 9, 0), 1.2);
            } else move(ground(b.getPositionVector()), 1.2);
            return;
        }
        if (piledriver && t >= 154) {
            move(start, 1.2);
            if (t == 163) wave(start, 15, 10);
            return;
        }
        if (held == null) {
            approach(p, .3, 3);
            return;
        }
        if (t == 35) start = ground(b.getPositionVector());
        if (piledriver) {
            if (t < 100) move(start.addVector(0, Math.min(26, (t - 35) * .5), 0), .8);
            else if (t < 155) move(start.addVector(0, Math.max(3, 26 - (t - 100) * .65), 0), 1.8);
            hold(b.localToWorld(b.hand(0, 0).add(b.hand(1, 0)).scale(.5)).addVector(0, -.6, 0));
            if (t == 153) {
                launch(-1.8, Vec3d.ZERO);
                wave(start, 15, 10);
            }
            if (t >= 155) move(start, 1.2);
        } else {
            double f = Math.min(1, (t - 35) / 85D);
            move(start.addVector(0, Math.sin(f * Math.PI) * 6, 0), 1);
            Vec3d grip =
                    b.localToWorld(b.hand(0, 0).add(b.hand(1, 0)).scale(.5)).addVector(0, -.6, 0);
            Vec3d floor = ground(grip);
            hold(new Vec3d(grip.x, Math.max(floor.y, grip.y), grip.z));
            if (t == 118) {
                wave(start, 12, 9);
                if (held != null) held.attackEntityFrom(DamageSource.causeMobDamage(b), 10);
            }
            if (t == 126) launch(1.5, Vec3d.ZERO);
        }
    }
}
