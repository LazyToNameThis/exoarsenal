package com.exoarsenal.client.render;

import com.exoarsenal.entity.EntityScoutHardpoint;
import com.exoarsenal.entity.EntityX20Pilot;
import com.exoarsenal.entity.EntityX20Scout;
import com.exoarsenal.entity.ScoutCombatPattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public final class ScoutVfxRenderer {
    private ScoutVfxRenderer() {}

    public static void render(float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null) return;
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        GlStateManager.depthMask(false);
        for (Entity entity : mc.world.loadedEntityList) {
            if (mc.getRenderViewEntity() == null
                    || entity.getDistanceSq(mc.getRenderViewEntity()) > 96 * 96) continue;
            if (entity instanceof EntityX20Scout)
                renderScout((EntityX20Scout) entity, partialTicks);
            else if (entity instanceof EntityX20Pilot)
                renderPilot((EntityX20Pilot) entity, partialTicks);
            else if (entity instanceof com.exoarsenal.entity.EntityFrigidRobot)
                renderRobot((com.exoarsenal.entity.EntityFrigidRobot) entity, partialTicks);
        }
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GL11.glLineWidth(1F);
        GlStateManager.popMatrix();
    }

    private static void renderRobot(com.exoarsenal.entity.EntityFrigidRobot robot, float partial) {
        if (robot.attack() == 0) return;
        float t = robot.attackTick() + partial;
        Vec3d center = interpolate(robot, partial),
                muzzle = center.addVector(0, robot.height * .7, 0);
        boolean ranged =
                robot.kind() == com.exoarsenal.entity.EntityFrigidRobot.Kind.DRONE
                        || robot.kind() == com.exoarsenal.entity.EntityFrigidRobot.Kind.ROVER
                                && robot.attack() == 1
                        || robot.kind() == com.exoarsenal.entity.EntityFrigidRobot.Kind.ARTHROPOD
                                && (robot.attack() == 4 || robot.attack() == 6);
        float next =
                com.exoarsenal.entity.FrigidAttackTiming.next(
                        robot.kind().ordinal(), robot.attack(), robot.phaseTwo(), t);
        float recoil =
                com.exoarsenal.entity.FrigidAttackTiming.recoil(
                        robot.kind().ordinal(), robot.attack(), robot.phaseTwo(), t);
        if (ranged) {
            if (next >= 0 && next - t <= 12) {
                float charge = 1 - (next - t) / 12;
                dashed(muzzle, robot.visualAim(), 18, 1, .64F, .26F, .2F + charge * .35F);
                ring(muzzle, .45 - charge * .25, 1, 0, 1, .7F, .35F, .65F, 1.5F);
            }
            if (recoil > .5F) {
                Vec3d direction = robot.visualAim().subtract(muzzle).normalize();
                glowLine(muzzle, muzzle.add(direction.scale(.5 + recoil)), .65F, .9F, 1, recoil);
            }
        } else if (next >= 0 && next - t <= 14) {
            double radius =
                    robot.kind() == com.exoarsenal.entity.EntityFrigidRobot.Kind.ARTHROPOD
                            ? robot.attack() == 2 ? next == 28 ? 5 : next == 40 ? 7 : 9 : 4
                            : robot.kind() == com.exoarsenal.entity.EntityFrigidRobot.Kind.AMPLIFIER
                                            || robot.attack() == 3
                                    ? 5
                                    : 3;
            groundReticle(center, radius, 0, .38F, .78F, 1, .15F + (14 - next + t) * .025F);
        }
    }

    private static void renderScout(EntityX20Scout scout, float partial) {
        Vec3d center = interpolate(scout, partial).addVector(0, 2.0, 0);
        double phase = Minecraft.getSystemTime() * .0035D;
        if (scout.getPhase() == EntityX20Scout.PHASE_HARDPOINTS) {
            ring(center, 3.35, 0, phase, .12F, .7F, 1F, .28F, 2.2F);
            ring(center, 3.35, 1, -phase * .77, .12F, .82F, 1F, .2F, 1.6F);
            ring(center, 3.35, 2, phase * .61, .36F, .9F, 1F, .16F, 1.3F);
        }
        int attack = scout.getAttack(), tick = scout.getAttackTick();
        Vec3d aim = scout.getAim();
        if (ScoutCombatPattern.extended(attack)) renderExpert(scout, partial);
        if (scout.getProjectileKind() == 3 || scout.getGrabMode() == 4) {
            Vec3d end =
                    scout.getCaptured() != null
                            ? scout.getCaptured().getPositionVector().addVector(0, 1, 0)
                            : scout.getHammerPosition(partial);
            iceChain(scout.getViceSocket(true, partial), end, 1F);
        }
        if (attack == ScoutCombatPattern.BRAWL_CHAIN && tick < 28)
            dashed(
                    scout.getHardpointPosition(EntityScoutHardpoint.VICE),
                    aim,
                    12,
                    .35F,
                    .8F,
                    1F,
                    .5F);
        if (attack == ScoutCombatPattern.RANGE_REEL
                && tick < com.exoarsenal.entity.ScoutRangeReel.WINDUP)
            dashed(scout.getViceSocket(true, partial), aim, 16, .35F, .8F, 1F, .6F);
        if (scout.getProjectileKind() != 0 && scout.getProjectileKind() != 3)
            ring(scout.getHammerPosition(partial), .5, 1, tick * .2, .3F, .8F, 1F, .4F, 2);
        if (scout.getWaveAge() >= 0) {
            double age = scout.getWaveAge() + partial;
            hexWave(
                    scout.getWaveCenter().addVector(0, .12, 0),
                    age * .55,
                    .45F,
                    (float) Math.max(0, 1 - age / 24));
            hexWave(
                    scout.getWaveCenter().addVector(0, .16, 0),
                    Math.max(0, age * .55 - .8),
                    .08F,
                    (float) Math.max(0, 1 - age / 22) * .65F);
        }
        if (attack >= ScoutCombatPattern.SLASH) {
            if (attack == ScoutCombatPattern.MINIGUN || attack == ScoutCombatPattern.CANNON) {
                boolean cannon = attack == ScoutCombatPattern.CANNON;
                int contact = cannon ? 36 : 24;
                Vec3d muzzle =
                        ScoutRenderSockets.get(
                                scout, cannon ? "cannon" : "minigun", scout.getPhaseMuzzle(cannon));
                if (tick < contact) {
                    ring(
                            muzzle,
                            .25 + tick / (double) contact * .3,
                            2,
                            tick * .12,
                            .2F,
                            .65F,
                            1F,
                            .55F,
                            1.5F);
                    dashed(muzzle, aim, 12, .2F, .7F, 1F, .22F);
                }
                if (cannon ? tick >= 36 && tick < 39 : tick >= 24 && tick <= 49 && tick % 4 < 2) {
                    Vec3d end = muzzle.add(aim.subtract(muzzle).normalize().scale(40));
                    net.minecraft.util.math.RayTraceResult wall =
                            scout.world.rayTraceBlocks(muzzle, end, false, true, false);
                    glowLine(
                            muzzle,
                            wall == null ? end : wall.hitVec,
                            .45F,
                            .85F,
                            1F,
                            cannon ? .85F : .5F);
                }
            }
            if (attack == ScoutCombatPattern.LEAP && tick >= 16 && scout.getWaveAge() < 0)
                hexWave(
                        aim.addVector(0, .08, 0),
                        tick < 38 ? 3 : 2.7,
                        .12F,
                        tick < 38 ? .35F : .7F);
            if (attack == ScoutCombatPattern.SCISSOR_HUNT
                    && tick >= 20
                    && tick < 140
                    && scout.getHammerFlight() != 6) {
                int dash = (tick - 32) % 24;
                if (tick < 32 || dash >= 16)
                    dashed(scout.getHammerPosition(partial), aim, 12, .3F, .8F, 1F, .38F);
            }
            if (attack == ScoutCombatPattern.CHARGE && tick < 20)
                dashed(
                        center.addVector(0, -1.8, 0),
                        new Vec3d(aim.x, scout.posY + .2, aim.z),
                        12,
                        .35F,
                        .7F,
                        1F,
                        .5F);
            if (attack == ScoutCombatPattern.STUN) {
                Vec3d weak = scout.getHardpointPosition(EntityScoutHardpoint.MINIGUN_RIGHT);
                ring(
                        weak,
                        .8,
                        1,
                        0,
                        scout.isGuardBroken() ? .25F : 1F,
                        scout.isGuardBroken() ? .75F : .55F,
                        .25F,
                        .65F,
                        2);
            }
            if ((attack == ScoutCombatPattern.THROW && scout.getHammerFlight() != 0 && tick >= 68)
                    || (attack == ScoutCombatPattern.HAMMER_RELAY && scout.getHammerFlight() == 3))
                iceChain(
                        ScoutRenderSockets.get(scout, "hand", scout.getHammerHand()),
                        ScoutRenderSockets.get(scout, "hammer", scout.getHammerPosition(partial)),
                        1F);
            return;
        }
        if (attack == EntityX20Scout.ATTACK_MINIGUN_LEFT
                || attack == EntityX20Scout.ATTACK_MINIGUN_RIGHT) {
            int kind =
                    attack == EntityX20Scout.ATTACK_MINIGUN_RIGHT
                            ? EntityScoutHardpoint.MINIGUN_RIGHT
                            : EntityScoutHardpoint.MINIGUN_LEFT;
            Vec3d muzzle = scout.getMuzzlePosition(kind);
            double spin = tick * .28;
            ring(muzzle, .22 + Math.min(1, tick / 22D) * .22, 2, spin, 1F, .38F, .08F, .75F, 2.4F);
            if (tick < 22) dashed(muzzle, aim, 12, 1F, .25F, .05F, .34F);
            else glowLine(muzzle, aim, 1F, .34F, .06F, .9F);
        } else if (attack == EntityX20Scout.ATTACK_SYNC_BEAM) {
            EntityX20Pilot pilot = scout.getPilot();
            Vec3d source = scout.getReactorEmitterPosition();
            double charge = Math.min(1, tick / 46D);
            ring(
                    source,
                    .35 + charge * .34,
                    2,
                    phase * 5,
                    .16F,
                    .72F,
                    1F,
                    .32F + (float) charge * .34F,
                    2.4F);
            if (pilot != null) {
                Vec3d relay = pilot.getBladePosition();
                ring(
                        relay,
                        1.05 - charge * .72,
                        2,
                        -phase * 7,
                        .42F,
                        .92F,
                        1F,
                        .42F + (float) charge * .28F,
                        2.8F);
                if (tick < 46) {
                    dashed(source, relay, 14, .18F, .74F, 1F, .36F);
                    dashed(relay, aim, 22, .44F, .92F, 1F, .42F);
                    groundReticle(aim, 2.3 - charge * 1.35, phase, .32F, .86F, 1F, .52F);
                } else {
                    glowLine(source, relay, .18F, .76F, 1F, 1F);
                    glowLine(relay, aim, .5F, .96F, 1F, 1F);
                }
            }
        } else if (attack == EntityX20Scout.ATTACK_LASER_LEFT
                || attack == EntityX20Scout.ATTACK_LASER_RIGHT) {
            boolean right = attack == EntityX20Scout.ATTACK_LASER_RIGHT;
            int kind = right ? EntityScoutHardpoint.LASER_RIGHT : EntityScoutHardpoint.LASER_LEFT;
            Vec3d muzzle = scout.getMuzzlePosition(kind);
            double charge = Math.min(1, tick / 38D);
            for (int i = 0; i < 3; i++)
                ring(
                        muzzle,
                        (1.15 - charge * .88) * (1 + i * .22),
                        2,
                        phase * 6 + i * 2.1,
                        .18F,
                        .82F,
                        1F,
                        .25F + (float) charge * .28F,
                        1.5F + i);
            if (tick < 38) {
                dashed(muzzle, aim, 20, .2F, .8F, 1F, .28F + (.3F * (float) charge));
                groundReticle(aim, 1.8 - charge * 1.1, phase, .15F, .75F, 1F, .56F);
            } else glowLine(muzzle, aim, .25F, .9F, 1F, 1F);
        } else if (attack == EntityX20Scout.ATTACK_CLAW || attack == EntityX20Scout.ATTACK_VICE) {
            double progress =
                    Math.min(1, tick / (attack == EntityX20Scout.ATTACK_CLAW ? 25D : 31D));
            groundReticle(
                    aim,
                    2.4 - progress * 1.45,
                    phase * 3,
                    1F,
                    .22F,
                    .08F,
                    .38F + .35F * (float) progress);
            arc(
                    center.addVector(0, -1.6, 0),
                    3.7,
                    attack == EntityX20Scout.ATTACK_CLAW ? -1 : 1,
                    progress,
                    1F,
                    .3F,
                    .08F,
                    .62F);
        } else if (attack == EntityX20Scout.ATTACK_STOMP) {
            double p = Math.min(1, tick / 34D);
            groundReticle(
                    new Vec3d(scout.posX, scout.posY + .08, scout.posZ),
                    10 - p * 8.9,
                    phase,
                    1F,
                    .22F,
                    .08F,
                    .62F);
        } else if (attack == EntityX20Scout.ATTACK_OVERLOAD) {
            double p = Math.min(1, tick / 51D);
            for (int i = 0; i < 4; i++)
                ring(
                        center,
                        .7 + p * (1.2 + i * .65),
                        i % 3,
                        phase * (i + 1),
                        .2F,
                        .86F,
                        1F,
                        .18F + (float) p * .26F,
                        1.5F + i * .45F);
        } else if (attack == EntityX20Scout.ATTACK_MORTAR) {
            double p = (tick % 14) / 14D;
            groundReticle(aim, 2.8 - p * 1.8, phase, 1F, .12F, .06F, .55F);
        }
    }

    private static void renderExpert(EntityX20Scout scout, float partial) {
        int a = scout.getAttack();
        float t = scout.getAttackTick() + partial;
        Vec3d body = interpolate(scout, partial),
                hand = ScoutRenderSockets.get(scout, "hand", scout.getHammerHand()),
                aim = scout.getAim();
        if (a == ScoutCombatPattern.KATANA_CHAIN && t >= 24 && t < 142) iceChain(hand, aim, .8F);
        if ((a == ScoutCombatPattern.HAMMER_TORNADO && t >= 162 && t < 186)
                || (a == ScoutCombatPattern.HAMMER_RICOCHET && t >= 210 && t < 228)
                || (a == ScoutCombatPattern.SWORD_UPROOT && t >= 262 && t < 281)
                || (a == ScoutCombatPattern.SCISSOR_TRENCH && t >= 240 && t < 262)
                || (a == ScoutCombatPattern.SCISSOR_WALLS && t >= 284 && t < 300))
            iceChain(hand, scout.getHammerPosition(partial), 1);
        if (a == ScoutCombatPattern.HAMMER_RICOCHET && t >= 80 && t < 126) {
            Vec3d p =
                    new Vec3d(
                            scout.getHammerPosition(partial).x,
                            scout.posY + .1,
                            scout.getHammerPosition(partial).z);
            groundReticle(p, 1 + (t - 80) * .055, 0, .9F, .65F, .2F, .6F);
        }
        if (a == ScoutCombatPattern.SCISSOR_TRENCH && t >= 110 && t < 240
                || a == ScoutCombatPattern.SCISSOR_WALLS && t >= 216 && t < 284)
            dashed(scout.getHammerPosition(partial), aim, 16, .9F, .7F, .25F, .6F);
        com.exoarsenal.entity.EntityScoutArena arena = scout.arena();
        if (arena != null
                && (a == ScoutCombatPattern.EXPERT_WEB
                        || a == ScoutCombatPattern.EXPERT_PENDULUM)) {
            for (boolean right : new boolean[] {false, true})
                if (com.exoarsenal.entity.ScoutViceMotion.extension(a, t, right) > .001)
                    iceChain(
                            scout.getViceSocket(right, partial),
                            scout.getDetachedVice(right, partial),
                            .85F);
        }
        if (a == ScoutCombatPattern.EXPERT_FREEFALL
                && com.exoarsenal.entity.ScoutViceMotion.extension(a, t, true) > .001)
            iceChain(
                    scout.getViceSocket(true, partial), scout.getDetachedVice(true, partial), .85F);
        boolean dive =
                a == ScoutCombatPattern.EXPERT_DIVE && t >= 182 && t < 210
                        || a == ScoutCombatPattern.EXPERT_FREEFALL && t >= 170 && t < 202;
        if (dive) {
            hexWave(aim.addVector(0, .1, 0), 4, .14F, .65F);
            dashed(body.addVector(0, 2, 0), aim, 18, .8F, .75F, .35F, .4F);
        }
        if (a == ScoutCombatPattern.EXPERT_BOXING && t >= 164 && t < 188) {
            float p = (t - 164) / 24;
            ring(hand, .3 + p * .7, 1, 0, 1, .7F, .2F, .7F, 2);
        }
    }

    private static void renderPilot(EntityX20Pilot pilot, float partial) {
        int attack = pilot.getAttack(), tick = pilot.getAttackTick();
        if (attack == 0) return;
        Vec3d center = interpolate(pilot, partial).addVector(0, 1.05, 0);
        double yaw = Math.toRadians(-pilot.rotationYaw);
        Vec3d tip = center.addVector(-Math.sin(yaw) * 1.8, 0, Math.cos(yaw) * 1.8);
        if (attack == 3) {
            double p = Math.min(1, tick / 28D);
            ring(tip, .25 + p * .8, 2, tick * .3, .15F, .92F, 1F, .35F + (float) p * .35F, 2.2F);
            dashed(tip, pilot.getLook(1).scale(18).add(tip), 18, .15F, .88F, 1F, .42F);
        } else {
            arc(
                    center,
                    2.0,
                    attack == 1 ? -1 : 1,
                    Math.min(1, tick / (attack == 1 ? 13D : 19D)),
                    .15F,
                    .9F,
                    1F,
                    .78F);
        }
    }

    private static void hexWave(Vec3d center, double radius, float thickness, float alpha) {
        double outer = (radius + thickness) / Math.cos(Math.PI / 6),
                inner = Math.max(0, radius - thickness) / Math.cos(Math.PI / 6);
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder q = tess.getBuffer();
        q.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 6; i++) {
            double a = i * Math.PI / 3, c = Math.cos(a), s = Math.sin(a);
            v(q, center.addVector(c * outer, 0, s * outer), .18F, .55F, 1F, alpha * .35F);
            v(q, center.addVector(c * inner, .12, s * inner), .7F, .94F, 1F, alpha);
        }
        tess.draw();
    }

    private static void iceChain(Vec3d hand, Vec3d hammer, float alpha) {
        Vec3d delta = hammer.subtract(hand), axis = delta.normalize();
        Vec3d side = axis.crossProduct(new Vec3d(0, 1, 0));
        if (side.lengthSquared() < .001) side = new Vec3d(1, 0, 0);
        else side = side.normalize();
        Vec3d up = axis.crossProduct(side).normalize();
        int links = Math.min(48, Math.max(2, (int) (delta.lengthVector() * 2)));
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder q = tess.getBuffer();
        GL11.glLineWidth(2.4F);
        q.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < links; i++) {
            Vec3d c = hand.add(delta.scale((i + .5) / links));
            Vec3d across = (i % 2 == 0 ? side : up).scale(.13);
            Vec3d along = axis.scale(delta.lengthVector() / links * .6);
            Vec3d[] points = {c.add(along), c.add(across), c.subtract(along), c.subtract(across)};
            for (int j = 0; j < 4; j++) {
                v(q, points[j], .45F, .8F, 1F, alpha);
                v(q, points[(j + 1) % 4], .8F, .95F, 1F, alpha);
            }
        }
        tess.draw();
    }

    private static Vec3d interpolate(Entity e, float p) {
        return new Vec3d(
                e.prevPosX + (e.posX - e.prevPosX) * p,
                e.prevPosY + (e.posY - e.prevPosY) * p,
                e.prevPosZ + (e.posZ - e.prevPosZ) * p);
    }

    private static void glowLine(Vec3d a, Vec3d b, float r, float g, float bl, float alpha) {
        line(a, b, 12, r, g, bl, alpha * .16F);
        line(a, b, 5, r, g, bl, alpha * .46F);
        line(a, b, 1.8F, .85F, 1F, 1F, alpha);
    }

    private static void dashed(
            Vec3d a, Vec3d b, int segments, float r, float g, float bl, float alpha) {
        Vec3d d = b.subtract(a);
        for (int i = 0; i < segments; i += 2)
            line(
                    a.add(d.scale(i / (double) segments)),
                    a.add(d.scale((i + 1) / (double) segments)),
                    1.35F,
                    r,
                    g,
                    bl,
                    alpha);
    }

    private static void line(
            Vec3d a, Vec3d b, float width, float r, float g, float bl, float alpha) {
        GL11.glLineWidth(width);
        Tessellator t = Tessellator.getInstance();
        BufferBuilder q = t.getBuffer();
        q.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        v(q, a, r, g, bl, alpha);
        v(q, b, r, g, bl, alpha);
        t.draw();
    }

    private static void ring(
            Vec3d c,
            double radius,
            int plane,
            double offset,
            float r,
            float g,
            float bl,
            float alpha,
            float width) {
        GL11.glLineWidth(width);
        Tessellator t = Tessellator.getInstance();
        BufferBuilder q = t.getBuffer();
        q.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < 48; i++) {
            double a = offset + i * Math.PI * 2 / 48,
                    x = Math.cos(a) * radius,
                    y = Math.sin(a) * radius;
            Vec3d p =
                    plane == 0
                            ? c.addVector(x, 0, y)
                            : plane == 1 ? c.addVector(x, y, 0) : c.addVector(0, x, y);
            v(q, p, r, g, bl, alpha);
        }
        t.draw();
    }

    private static void groundReticle(
            Vec3d c, double radius, double phase, float r, float g, float bl, float alpha) {
        ring(new Vec3d(c.x, c.y + .04, c.z), radius, 0, phase, r, g, bl, alpha, 2.3F);
        for (int i = 0; i < 4; i++) {
            double a = phase + i * Math.PI / 2;
            Vec3d out = c.addVector(Math.cos(a) * radius, .06, Math.sin(a) * radius),
                    in = c.addVector(Math.cos(a) * radius * .62, .06, Math.sin(a) * radius * .62);
            line(in, out, 2.3F, r, g, bl, alpha);
        }
    }

    private static void arc(
            Vec3d c,
            double radius,
            int side,
            double progress,
            float r,
            float g,
            float bl,
            float alpha) {
        GL11.glLineWidth(5F);
        Tessellator t = Tessellator.getInstance();
        BufferBuilder q = t.getBuffer();
        q.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 28; i++) {
            double a = (-1.25 + i * 2.5 / 28) * side * progress;
            v(
                    q,
                    c.addVector(
                            Math.sin(a) * radius,
                            .25 + Math.sin(i * Math.PI / 28) * .6,
                            Math.cos(a) * radius),
                    r,
                    g,
                    bl,
                    alpha);
        }
        t.draw();
    }

    private static void v(BufferBuilder q, Vec3d p, float r, float g, float b, float a) {
        Minecraft mc = Minecraft.getMinecraft();
        q.pos(
                        p.x - mc.getRenderManager().viewerPosX,
                        p.y - mc.getRenderManager().viewerPosY,
                        p.z - mc.getRenderManager().viewerPosZ)
                .color(r, g, b, a)
                .endVertex();
    }
}
