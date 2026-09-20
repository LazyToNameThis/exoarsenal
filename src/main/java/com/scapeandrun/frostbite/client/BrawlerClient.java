package com.scapeandrun.frostbite.client;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.entity.EntityBrawler;
import com.scapeandrun.frostbite.entity.EntityBrawlerEffect;
import com.scapeandrun.frostbite.entity.BrawlerScore;
import com.scapeandrun.frostbite.expedition.client.ExpeditionMesh;
import com.scapeandrun.frostbite.client.render.WulfrumRayRenderer;
import com.scapeandrun.frostbite.client.render.BrawlerMeshRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

@Mod.EventBusSubscriber(modid = Frostbite.MODID, value = Side.CLIENT)
public final class BrawlerClient {
    @SubscribeEvent
    public static void models(ModelRegistryEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(EntityBrawler.class, BrawlerRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(
                EntityBrawlerEffect.class, EffectRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(
                com.scapeandrun.frostbite.entity.EntityBrawlerArm.class,
                manager ->
                        new Render<com.scapeandrun.frostbite.entity.EntityBrawlerArm>(manager) {
                            @Override
                            protected ResourceLocation getEntityTexture(
                                    com.scapeandrun.frostbite.entity.EntityBrawlerArm arm) {
                                return null;
                            }
                        });
    }

    private static final int DARK = 0x22342A, ARMOR = 0x889780, EDGE = 0xBAC5A4, GREEN = 0x70F2A0;
    public static final ExpeditionMesh HEAD = new ExpeditionMesh(),
            FIST = new ExpeditionMesh(),
            CLEAVER = new ExpeditionMesh(),
            HARPOON = new ExpeditionMesh(),
            PROJECTOR = new ExpeditionMesh(),
            FLAIL = new ExpeditionMesh();
    public static final ExpeditionMesh PROJECTILE_BLADE =
            new ExpeditionMesh()
                    .plate(
                            new double[][] {
                                {-.18, -1.5}, {.14, -1.2}, {.3, .8}, {0, 1.7}, {-.28, .75}
                            },
                            .13,
                            0x485944)
                    .plate(
                            new double[][] {{.14, -1.2}, {.3, .8}, {0, 1.7}, {.13, .64}},
                            .16,
                            0x70F2A0)
                    .armor(-.22, -.26, -.17, .44, .52, .34, .08, 0x889780)
                    .box(-.065, -.16, .18, .13, .32, .035, 0xDAFFD6);

    static {
        BrawlerRebuiltModel.rebuild(HEAD, FIST, CLEAVER, HARPOON, PROJECTOR, FLAIL);
    }

    public static final ExpeditionMesh JETPACK = BrawlerJetpack.ENGINE;

    public static ExpeditionMesh articulatedDriver(float grip) {
        return BrawlerRebuiltModel.driverPose(grip);
    }

    private static final ExpeditionMesh IRIS = new ExpeditionMesh(),
            PISTON = new ExpeditionMesh(),
            VENT = new ExpeditionMesh(),
            SIGHT = new ExpeditionMesh();

    static {
        for (int n = 0; n < 8; n++) {
            double a = n * Math.PI / 4, x = Math.cos(a), y = Math.sin(a);
            IRIS.tube(
                    x * .63,
                    y * .63,
                    1.04,
                    x * .76 - y * .10,
                    y * .76 + x * .10,
                    1.04,
                    .055,
                    .055,
                    0xBAC5A4);
            IRIS.box(x * .71 - .025, y * .71 - .025, 1.075, .05, .05, .025, 0x70F2A0);
        }
        PISTON.tube(0, 0, -1.64, 0, 0, -.47, .065, .065, 0xBAC5A4)
                .box(-.11, -.11, -.52, .22, .22, .16, 0xA99A64);
        VENT.armor(-.45, -.06, -.42, .9, .12, .84, .035, 0x485944)
                .box(-.37, .065, -.32, .74, .025, .08, 0xBAC5A4);
        SIGHT.box(-.12, -.045, 0, .24, .09, .06, 0x70F2A0)
                .box(-.035, -.045, .061, .07, .09, .02, 0xDCFFE8);
    }

    private static void hardware(EntityBrawler entity, int arm, float partial) {
        GlStateManager.pushMatrix();
        double angle = entity.phase() == 3 || arm == 0 ? -.20 : arm == 2 ? .10 : .18;
        GlStateManager.rotate((float) Math.toDegrees(angle), 0, 0, 1);
        if (entity.phase() == 3 || arm == 0) GlStateManager.scale(1.18, 1.18, 1.18);
        double time = entity.clock() + partial;
        double[] now = BrawlerScore.hand(entity.pattern(), arm, time),
                before = BrawlerScore.hand(entity.pattern(), arm, time - .5);
        double speed =
                Math.min(
                        1,
                        Math.sqrt(
                                        Math.pow(now[0] - before[0], 2)
                                                + Math.pow(now[1] - before[1], 2)
                                                + Math.pow(now[2] - before[2], 2))
                                * 2);
        if (entity.phase() == 3 || arm == 0) {
            for (int side : new int[] {-1, 1}) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(side * .39, .76, -speed * .24);
                draw(PISTON);
                GlStateManager.popMatrix();
            }
        } else if (arm == 2) {
            int active = (int) (time / 8) % 3;
            GlStateManager.pushMatrix();
            GlStateManager.translate(.62, -.55 + active * .89, .47);
            draw(SIGHT);
            GlStateManager.popMatrix();
        } else if (arm == 3) {
            for (int side : new int[] {-1, 1}) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(0, side * .79, -.35);
                GlStateManager.rotate((float) (side * (8 + speed * 32)), 1, 0, 0);
                draw(VENT);
                GlStateManager.popMatrix();
            }
        }
        GlStateManager.popMatrix();
    }

    public static ExpeditionMesh linkage(int i, double[] h) {
        return linkage(i, h, 0);
    }

    public static Vec3d chainPoint(int i, double[] h, double t, double time) {
        Vec3d socket = new Vec3d((i < 2 ? -1 : 1) * 1.15, (i == 0 || i == 2) ? .65 : -.65, -.2),
                end = new Vec3d(h[0], h[1], h[2] - (i == 1 ? .2 : .65));
        double span = socket.distanceTo(end),
                slack = Math.max(.12, .9 - Math.max(0, span - 3) * .16),
                curve = Math.sin(Math.PI * t);
        return socket.add(end.subtract(socket).scale(t))
                .addVector(
                        Math.sin(time * .075 + i * 1.7 + t * 3) * .12 * curve,
                        -slack * curve,
                        Math.sin(time * .06 + i + t * 4) * .10 * curve);
    }

    public static ExpeditionMesh linkage(int i, double[] h, double time) {
        ExpeditionMesh m = new ExpeditionMesh();
        Vec3d socket = chainPoint(i, h, 0, time), end = chainPoint(i, h, 1, time);
        m.box(socket.x - .25, socket.y - .25, socket.z - .25, .5, .5, .5, DARK)
                .box(socket.x - .17, socket.y - .17, socket.z + .25, .34, .34, .09, ARMOR);
        m.box(end.x - .22, end.y - .22, end.z - .12, .44, .44, .3, DARK);

        int count = Math.max(8, Math.min(36, (int) Math.ceil(socket.distanceTo(end) / .26)));
        for (int link = 0; link < count; link++) {
            double t = (link + .5) / count;
            Vec3d p = chainPoint(i, h, t, time),
                    a = chainPoint(i, h, Math.max(0, t - .01), time),
                    b = chainPoint(i, h, Math.min(1, t + .01), time),
                    axis = b.subtract(a).normalize();
            Vec3d cross =
                    axis.crossProduct(
                                    Math.abs(axis.y) > .9 ? new Vec3d(1, 0, 0) : new Vec3d(0, 1, 0))
                            .normalize();
            if (link % 2 != 0) cross = axis.crossProduct(cross).normalize();
            double length = a.distanceTo(b) / .02 / count * .72;
            Vec3d along = axis.scale(length), across = cross.scale(.19);
            Vec3d[] corners = {
                p.subtract(along).subtract(across),
                p.add(along).subtract(across),
                p.add(along).add(across),
                p.subtract(along).add(across)
            };
            for (int edge = 0; edge < 4; edge++) {
                Vec3d u = corners[edge], v = corners[(edge + 1) % 4];
                m.tube(
                        u.x,
                        u.y,
                        u.z,
                        v.x,
                        v.y,
                        v.z,
                        .060,
                        .060,
                        edge == 1 ? EDGE : link % 2 == 0 ? ARMOR : 0x485944);
            }
        }
        return m;
    }

    public static int skinPart(ExpeditionMesh mesh) {
        return mesh == HEAD
                ? 0
                : mesh == FIST || BrawlerRebuiltModel.driverMesh(mesh)
                        ? 1
                        : mesh == CLEAVER
                                ? 2
                                : mesh == HARPOON
                                        ? 3
                                        : mesh == PROJECTOR ? 4 : mesh == FLAIL ? 5 : -1;
    }

    private static void draw(ExpeditionMesh mesh) {
        BrawlerMeshRenderer.draw(mesh, skinPart(mesh), mesh == JETPACK);
    }

    private static void drawGhost(ExpeditionMesh mesh, float opacity) {
        BrawlerMeshRenderer.drawGhost(mesh, opacity);
    }

    private static Vec3d cleaverEdge(EntityBrawler boss, double time) {
        double[] hand = BrawlerScore.hand(boss.pattern(), 1, time),
                wrist = BrawlerScore.wrist(boss.pattern(), 1, time);
        if (boss.aerialAttack() >= 0) {
            Vec3d actual = boss.hand(1, (float) (time - boss.clock())),
                    before = boss.hand(1, (float) (time - boss.clock() - .5)),
                    after = boss.hand(1, (float) (time - boss.clock() + .5));
            hand = new double[] {actual.x, actual.y, actual.z};
            wrist =
                    new double[] {
                        Math.max(-60, Math.min(60, -(after.y - before.y) * 55)),
                        Math.max(-60, Math.min(60, (after.x - before.x) * 55)),
                        -Math.min(30, Math.abs(after.x - before.x) * 30)
                    };
        }
        double z = Math.toRadians(wrist[2]) - .16,
                x = Math.toRadians(wrist[0]),
                y = Math.toRadians(wrist[1]);
        Vec3d p =
                new Vec3d(
                        2.03 * Math.cos(z) - .10 * Math.sin(z),
                        2.03 * Math.sin(z) + .10 * Math.cos(z),
                        .1);
        p =
                new Vec3d(
                        p.x,
                        p.y * Math.cos(x) - p.z * Math.sin(x),
                        p.y * Math.sin(x) + p.z * Math.cos(x));
        p =
                new Vec3d(
                        p.x * Math.cos(y) + p.z * Math.sin(y),
                        p.y,
                        -p.x * Math.sin(y) + p.z * Math.cos(y));
        return p.addVector(hand[0], hand[1], hand[2]);
    }

    private static void cleaverTrail(EntityBrawler boss, float partial) {
        double now = boss.clock() + partial;
        if (boss.aerialAttack() >= 0
                && !com.scapeandrun.frostbite.entity.BrawlerAerialScore.cleaverAttached(
                        com.scapeandrun.frostbite.entity.BrawlerAerialScore.decode(
                                boss.aerialAttack()),
                        now)) return;
        if (boss.phase() != 1
                || boss.armHealth(1) <= 0
                || boss.transition() > 0
                || boss.clashTick() > 0
                || !BrawlerScore.weaponInHand(boss.pattern(), 1, now)) return;
        if (cleaverEdge(boss, now).distanceTo(cleaverEdge(boss, Math.max(0, now - .5))) < .08)
            return;
        WulfrumRayRenderer.begin();
        GlStateManager.depthMask(false);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        Vec3d previous = cleaverEdge(boss, Math.max(0, now - 3));
        for (int sample = 1; sample <= 12; sample++) {
            double time = Math.max(0, now - 3 + sample * .25);
            Vec3d next = cleaverEdge(boss, time);
            float fade = sample / 12F;
            WulfrumRayRenderer.tube(buffer, previous, next, .055, 0x52DD88, fade * .26F);
            WulfrumRayRenderer.tube(buffer, previous, next, .012, 0xDCFFE8, fade * .65F);
            previous = next;
        }
        Tessellator.getInstance().draw();
        GlStateManager.depthMask(true);
        WulfrumRayRenderer.finish();
    }

    private static void aerialPressure(EntityBrawler e, float partial) {
        if (e.aerialAttack()
                != com.scapeandrun.frostbite.entity.BrawlerAerialScore.Attack.MACH_CLEAVER
                        .ordinal()) return;
        double tick = e.clock() + partial, beat = tick % 32;
        if (tick > 103 || beat < 18) return;
        WulfrumRayRenderer.begin();
        GlStateManager.depthMask(false);
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (int ring = 0; ring < 3; ring++) {
            double radius = 1.7 + ring * .8, z = 3 - ring * 1.5;
            for (int n = 0; n < 32; n++) {
                double a = n * Math.PI / 16, c = (n + 1) * Math.PI / 16;
                Vec3d from = new Vec3d(Math.cos(a) * radius, Math.sin(a) * radius, z),
                        to = new Vec3d(Math.cos(c) * radius, Math.sin(c) * radius, z);
                WulfrumRayRenderer.tube(b, from, to, .025, 0xC8FFE6, .28F - ring * .055F);
                if (ring == 0 && n % 4 == 0)
                    WulfrumRayRenderer.tube(b, new Vec3d(0, 0, 4), from, .02, 0x86FFBB, .2F);
            }
        }
        Tessellator.getInstance().draw();
        GlStateManager.depthMask(true);
        WulfrumRayRenderer.finish();
        GlStateManager.disableLighting();
    }

    private static void jetpack(EntityBrawler e, float partial) {
        double open = e.phase() == 2 ? BrawlerScore.smooth((70 - e.transition()) / 45D) : 0;
        for (int side : new int[] {-1, 1}) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(side * (2.05 + open * .3), .2, -3.0);
            draw(BrawlerJetpack.ENGINE);
            for (int petal = 0; petal < 4; petal++) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(0, -2.4, 0);
                GlStateManager.rotate(petal * 90, 0, 1, 0);
                GlStateManager.translate(0, 0, 1.35);
                GlStateManager.rotate((float) (open * 32), 1, 0, 0);
                draw(BrawlerJetpack.PETAL);
                GlStateManager.popMatrix();
            }
            GlStateManager.popMatrix();
        }
        if (e.phase() == 2)
            for (int socket = 0; socket < 4; socket++) {
                double side = socket < 2 ? -1 : 1, up = socket % 2 == 0 ? 1 : -1;
                GlStateManager.pushMatrix();
                GlStateManager.translate(side * 1.65, up * 1.2, -.3);
                GlStateManager.rotate((float) (side * open * 110), 0, 1, 0);
                draw(BrawlerJetpack.SOCKET);
                GlStateManager.popMatrix();
                if ((e.engineMask() & (1 << (socket + 1))) != 0) {
                    WulfrumRayRenderer.begin();
                    GlStateManager.depthMask(false);
                    BufferBuilder b = Tessellator.getInstance().getBuffer();
                    b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                    Vec3d from = new Vec3d(side * 1.65, up * 1.2, -.3);
                    com.scapeandrun.frostbite.client.render.WulfrumFire.emit(
                            b,
                            from,
                            new Vec3d(side * 2.3, up * .7, -1),
                            2.5,
                            .4,
                            e.ticksExisted + partial + socket * 7);
                    Tessellator.getInstance().draw();
                    GlStateManager.depthMask(true);
                    WulfrumRayRenderer.finish();
                    GlStateManager.disableLighting();
                }
            }
    }

    private static final class BrawlerRenderer extends Render<EntityBrawler> {
        BrawlerRenderer(RenderManager m) {
            super(m);
            shadowSize = 2;
        }

        @Override
        protected ResourceLocation getEntityTexture(EntityBrawler e) {
            return null;
        }

        @Override
        public void doRender(
                EntityBrawler e, double x, double y, double z, float yaw, float partial) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(
                    x, y + e.bodyHeight(partial) - Math.min(.8, e.deathTime * .012), z);
            GlStateManager.rotate(-yaw, 0, 1, 0);
            GlStateManager.rotate((float) e.flightRoll(partial), 0, 0, 1);
            if (e.bipedal()) {
                com.scapeandrun.frostbite.entity.BrawlerMartialPose pose = e.martialPose(partial);
                GlStateManager.rotate((float) pose.pitch, 1, 0, 0);
                GlStateManager.rotate((float) -pose.yaw, 0, 1, 0);
            }
            if (e.deathTime > 0) GlStateManager.rotate(Math.min(75, e.deathTime) * .7F, 0, 0, 1);
            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            draw(HEAD);
            if (e.bipedal()) biped(e, partial);
            else jetpack(e, partial);
            if (e.deathTime == 0 && !e.bipedal()) {
                double thrust = e.jetThrust(partial),
                        open =
                                e.phase() == 2
                                        ? BrawlerScore.smooth((70 - e.transition()) / 45D)
                                        : 0;
                WulfrumRayRenderer.begin();
                GlStateManager.depthMask(false);
                BufferBuilder jet = Tessellator.getInstance().getBuffer();
                jet.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                for (int side : new int[] {-1, 1}) {
                    Vec3d nozzle = new Vec3d(side * (2.05 + open * .3), -3.39, -3);
                    com.scapeandrun.frostbite.client.render.WulfrumFire.emit(
                            jet,
                            nozzle,
                            new Vec3d(side * .1, -1, -.26),
                            4.8 * thrust,
                            .72,
                            e.ticksExisted + partial + side * 4);
                }
                Tessellator.getInstance().draw();
                GlStateManager.depthMask(true);
                WulfrumRayRenderer.finish();
            }
            GlStateManager.disableLighting();
            aerialPressure(e, partial);
            GlStateManager.pushMatrix();
            GlStateManager.rotate(
                    (e.ticksExisted + partial) * (e.phase() == 3 ? 2.8F : .55F), 0, 0, 1);
            draw(IRIS);
            GlStateManager.popMatrix();
            for (int i = 0; i < 4; i++) {
                boolean phantom =
                        e.phase() == 2
                                && (e.pattern() == BrawlerScore.Pattern.PHANTOMS
                                                && Math.abs(e.clock() - (30 + i * 34)) < 15
                                        || e.pattern() == BrawlerScore.Pattern.MISSING_ARMS
                                                && (Math.abs(e.clock() - (30 + i * 36)) < 15
                                                        || e.clock() >= 156 && e.clock() < 198));
                if (e.phase() == 2 && !phantom
                        || e.phase() == 1 && e.armHealth(i) <= 0
                        || e.phase() == 3 && i > 1) continue;
                Vec3d h = e.hand(i, partial);
                ExpeditionMesh links =
                        linkage(
                                e.phase() == 3 && i == 1 ? 2 : i,
                                new double[] {h.x, h.y, h.z},
                                e.ticksExisted + partial);
                if (phantom) drawGhost(links, .22F);
                else draw(links);
                GlStateManager.pushMatrix();
                GlStateManager.translate(h.x, h.y, h.z);
                if (e.phase() == 3) GlStateManager.scale(1.35, 1.35, 1.35);
                double[] wrist = BrawlerScore.wrist(e.pattern(), i, e.clock() + partial);
                if (e.bipedal()) {
                    Vec3d delta = h.subtract(new Vec3d(i == 0 ? -2.1 : 2.1, -.3, 0));
                    wrist =
                            new double[] {
                                -Math.toDegrees(Math.atan2(delta.y, Math.hypot(delta.x, delta.z))),
                                Math.toDegrees(Math.atan2(delta.x, delta.z)),
                                i == 0 ? -8 : 8
                            };
                }
                if (e.aerialAttack() >= 0) {
                    Vec3d before = e.hand(i, partial - .5F), after = e.hand(i, partial + .5F);
                    wrist =
                            new double[] {
                                Math.max(-60, Math.min(60, -(after.y - before.y) * 55)),
                                Math.max(-60, Math.min(60, (after.x - before.x) * 55)),
                                (i < 2 ? -1 : 1) * Math.min(30, Math.abs(after.x - before.x) * 30)
                            };
                }
                if (e.coordinating()) wrist = e.coordinationWrist(i, partial);
                if (e.clashTick() > 0 && i == e.clashArm() || e.introTick() >= 140 && i == 0) {
                    double recoil =
                            e.clashTick() > 0
                                    ? com.scapeandrun.frostbite.entity.BrawlerClash.returned(
                                            e.clashTick() + partial)
                                    : 0;
                    wrist =
                            new double[] {
                                -Math.toDegrees(Math.atan2(h.y, Math.hypot(h.x, h.z))),
                                Math.toDegrees(Math.atan2(h.x, h.z)),
                                recoil * 65
                            };
                }
                GlStateManager.rotate((float) wrist[1], 0, 1, 0);
                GlStateManager.rotate((float) wrist[0], 1, 0, 0);
                GlStateManager.rotate((float) wrist[2], 0, 0, 1);
                double flail =
                        i == 3 && e.phase() == 1
                                ? BrawlerScore.flail(e.pattern(), e.clock() + partial)
                                : 0;
                if (flail > 0) {
                    GlStateManager.pushMatrix();
                    GlStateManager.rotate((float) ((e.clock() + partial) * 9 * flail), 0, 0, 1);
                    GlStateManager.scale(flail, flail, flail);
                    draw(FLAIL);
                    GlStateManager.popMatrix();
                }
                boolean held =
                        e.bipedal()
                                || (e.aerialAttack() >= 0
                                        ? (i != 1
                                                || com.scapeandrun.frostbite.entity
                                                        .BrawlerAerialScore.cleaverAttached(
                                                        com.scapeandrun.frostbite.entity
                                                                .BrawlerAerialScore.decode(
                                                                e.aerialAttack()),
                                                        e.clock() + partial))
                                        : BrawlerScore.weaponInHand(
                                                e.pattern(), i, e.clock() + partial));
                if (flail < 1 && held) {
                    GlStateManager.scale(1 - flail, 1 - flail, 1 - flail);
                    ExpeditionMesh weapon =
                            e.phase() == 3
                                            || i == 0
                                            || (e.introTick() > 0 && e.introTick() < 26 && i == 1)
                                    ? FIST
                                    : i == 1 ? CLEAVER : i == 2 ? HARPOON : PROJECTOR;
                    if (weapon == FIST) weapon = articulatedDriver(e.weaponGrip(i, partial));
                    if (phantom)
                        drawGhost(
                                weapon,
                                .30F + .1F * (float) Math.sin((e.clock() + partial) * 1.8 + i));
                    else {
                        draw(weapon);
                        hardware(e, i, partial);
                    }
                }
                GlStateManager.popMatrix();
            }
            cleaverTrail(e, partial);
            for (int indicator = 0;
                    indicator < (e.bipedal() ? 5 : e.phase() == 3 || e.aerialAttack() >= 0 ? 2 : 1);
                    indicator++) {
                int hand =
                        e.phase() == 3 || e.aerialAttack() >= 0
                                ? indicator
                                : BrawlerScore.parryArm(e.pattern(), e.clock() + partial);
                double cue =
                        e.transition() == 0 && e.clashTick() == 0
                                ? (e.phase() == 3
                                        ? BrawlerScore.parryCue(
                                                e.pattern(), e.clock() + partial, hand)
                                        : BrawlerScore.parryCue(e.pattern(), e.clock() + partial))
                                : 0;
                if (e.limiterAttack() >= 0)
                    cue =
                            e.aerialRecovery() > 0
                                    ? 0
                                    : com.scapeandrun.frostbite.entity.BrawlerLimiterScore.cue(
                                            com.scapeandrun.frostbite.entity.BrawlerLimiterScore
                                                    .decode(e.limiterAttack()),
                                            e.clock() + partial);
                if (e.aerialAttack() >= 0)
                    cue =
                            e.aerialRecovery() > 0
                                    ? 0
                                    : com.scapeandrun.frostbite.entity.BrawlerAerialScore.cue(
                                            com.scapeandrun.frostbite.entity.BrawlerAerialScore
                                                    .decode(e.aerialAttack()),
                                            e.clock() + partial,
                                            hand);
                if (e.bipedal())
                    cue =
                            e.transition() > 0
                                    ? 0
                                    : com.scapeandrun.frostbite.entity.BrawlerMartialScore.cue(
                                            com.scapeandrun.frostbite.entity.BrawlerMartialScore
                                                    .decode(Math.max(0, e.martialAttack())),
                                            hand,
                                            e.clock() + partial);
                if (hand == 1
                        && e.clashTick() == 0
                        && e.counterContact() >= 0
                        && e.clock() <= e.counterContact())
                    cue =
                            Math.max(
                                    cue,
                                    BrawlerScore.smooth(
                                            (e.clock() + partial - e.counterContact() + 14) / 14D));
                if (cue > 0) {
                    WulfrumRayRenderer.begin();
                    BufferBuilder q = Tessellator.getInstance().getBuffer();
                    q.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                    Vec3d center =
                            e.bipedal() && hand >= 2
                                    ? (hand < 4
                                            ? e.martialPose(partial).foot(hand - 2)
                                            : new Vec3d(0, -2.5, 2.5))
                                    : e.phase() == 2 ? Vec3d.ZERO : e.hand(hand, partial);
                    for (int j = 0; j < 8; j++) {
                        double a = j * Math.PI / 4;
                        Vec3d
                                from =
                                        center.addVector(
                                                Math.cos(a) * (1.4 - cue * .3),
                                                Math.sin(a) * (1.4 - cue * .3),
                                                1.05),
                                to =
                                        center.addVector(
                                                Math.cos(a) * (1.65 - cue * .3),
                                                Math.sin(a) * (1.65 - cue * .3),
                                                1.05);
                        WulfrumRayRenderer.tube(q, from, to, .035, 0xFFE6A0, (float) cue);
                    }
                    Tessellator.getInstance().draw();
                    WulfrumRayRenderer.finish();
                }
            }
            if (e.shielded() || e.temporaryShield() > 0 || e.phase() == 2 && e.transition() > 24) {
                WulfrumRayRenderer.begin();
                BufferBuilder b = Tessellator.getInstance().getBuffer();
                b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

                for (int row = -3; row <= 3; row++) {
                    double latitude = row * .37;
                    int cells = (int) (16 * Math.cos(latitude));
                    for (int c = 0; c < cells; c++) {
                        double longitude = (c + (row % 2 == 0 ? 0 : .5)) * Math.PI * 2 / cells;
                        Vec3d center =
                                new Vec3d(
                                        Math.cos(latitude) * Math.sin(longitude),
                                        Math.sin(latitude),
                                        Math.cos(latitude) * Math.cos(longitude));
                        Vec3d east = new Vec3d(Math.cos(longitude), 0, -Math.sin(longitude)),
                                north = center.crossProduct(east);
                        Vec3d old = null, first = null;
                        if (e.phase() == 2
                                && e.transition() > 0
                                && (c + row * 3 + 40) % 16 > (e.transition() - 24) / 3) continue;
                        if (e.phase() == 3 && (c + row * 3 + 42) % 7 < e.shieldHits()) continue;
                        double shieldRadius =
                                e.phase() == 2 && e.transition() > 0
                                        ? 1.05 + Math.min(1, (e.transition() - 24) / 30D)
                                        : 2.05;
                        if (e.aerialAttack()
                                == com.scapeandrun.frostbite.entity.BrawlerAerialScore.Attack
                                        .TESLA_CAGE
                                        .ordinal())
                            shieldRadius +=
                                    .85
                                            * BrawlerScore.smooth(e.clock() / 20D)
                                            * (1 - BrawlerScore.smooth((e.clock() - 20) / 16D));
                        for (int v = 0; v <= 6; v++) {
                            double a = v * Math.PI / 3;
                            Vec3d p =
                                    center.add(east.scale(Math.cos(a) * .205))
                                            .add(north.scale(Math.sin(a) * .205))
                                            .normalize()
                                            .scale(shieldRadius);
                            if (first == null) first = p;
                            if (old != null)
                                WulfrumRayRenderer.tube(
                                        b,
                                        old,
                                        p,
                                        .014,
                                        GREEN,
                                        .35F + .15F * (float) Math.sin(e.ticksExisted * .08 + c));
                            old = p;
                        }
                    }
                }
                Tessellator.getInstance().draw();
                WulfrumRayRenderer.finish();
            }
            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.popMatrix();
        }
    }

    private static void biped(EntityBrawler e, float partial) {
        com.scapeandrun.frostbite.entity.BrawlerMartialPose p = e.martialPose(partial);
        double assembly = BrawlerScore.smooth((100 - e.transition() + partial) / 70D);
        for (int side = 0; side < 2; side++) {
            Vec3d hip = p.hip(side);
            GlStateManager.pushMatrix();
            GlStateManager.translate(
                    hip.x + (side == 0 ? -2 : 2) * (1 - assembly),
                    hip.y + 2 * (1 - assembly),
                    hip.z - 2 * (1 - assembly));
            GlStateManager.rotate((float) (p.hip[side] + 90 * (1 - assembly)), 1, 0, 0);
            draw(BrawlerBipedModel.THIGH);
            GlStateManager.translate(0, -1.65, 0);
            GlStateManager.rotate((float) (p.knee[side] + 90 * (1 - assembly)), 1, 0, 0);
            draw(BrawlerBipedModel.SHIN);
            GlStateManager.translate(0, -1.65, .25);
            GlStateManager.rotate((float) p.ankle[side], 1, 0, 0);
            draw(BrawlerBipedModel.FOOT);
            if (p.thrust > .05 && e.deathTime == 0) {
                WulfrumRayRenderer.begin();
                BufferBuilder q = Tessellator.getInstance().getBuffer();
                q.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                com.scapeandrun.frostbite.client.render.WulfrumFire.emit(
                        q,
                        new Vec3d(0, .05, -.98),
                        new Vec3d(0, -.35, -1),
                        p.thrust * 3,
                        .35,
                        e.ticksExisted + partial + side * 7);
                Tessellator.getInstance().draw();
                WulfrumRayRenderer.finish();
            }
            GlStateManager.popMatrix();
        }
    }

    private static final class EffectRenderer extends Render<EntityBrawlerEffect> {
        EffectRenderer(RenderManager manager) {
            super(manager);
        }

        @Override
        protected ResourceLocation getEntityTexture(EntityBrawlerEffect e) {
            return null;
        }

        @Override
        public void doRender(
                EntityBrawlerEffect e, double x, double y, double z, float yaw, float partial) {
            if (e.kind() == EntityBrawlerEffect.HEAD_ECHO) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y + 1.4, z);
                GlStateManager.disableTexture2D();
                GlStateManager.disableLighting();
                drawGhost(HEAD, .28F);
                GlStateManager.enableTexture2D();
                GlStateManager.enableLighting();
                GlStateManager.popMatrix();
                return;
            }
            if (e.kind() == EntityBrawlerEffect.CUT
                    || e.kind() == EntityBrawlerEffect.BLADE
                    || e.kind() == EntityBrawlerEffect.SHARD
                    || e.kind() == EntityBrawlerEffect.TERRAIN
                    || e.kind() == EntityBrawlerEffect.FIST) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y, z);
                GlStateManager.disableLighting();
                GlStateManager.rotate(
                        (e.age() + partial) * (e.kind() == EntityBrawlerEffect.TERRAIN ? 5 : 18),
                        0,
                        0,
                        1);
                if (e.kind() == EntityBrawlerEffect.TERRAIN) {
                    GlStateManager.enableTexture2D();
                    Minecraft.getMinecraft()
                            .getTextureManager()
                            .bindTexture(
                                    net.minecraft.client.renderer.texture.TextureMap
                                            .LOCATION_BLOCKS_TEXTURE);
                    double r = Math.max(.3, e.radius());
                    GlStateManager.scale(r, r, r);
                    for (double[] block :
                            new double[][] {
                                {-.5, -.5, -.5},
                                {.35, -.35, -.2},
                                {-.9, -.2, -.15},
                                {-.25, .3, -.25},
                                {-.35, -.7, .3},
                                {.2, -.4, -.85}
                            }) {
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(block[0], block[1], block[2]);
                        GlStateManager.scale(.85, .85, .85);
                        Minecraft.getMinecraft()
                                .getBlockRendererDispatcher()
                                .renderBlockBrightness(e.terrainState(), 1);
                        GlStateManager.popMatrix();
                    }
                } else if (e.kind() == EntityBrawlerEffect.CUT
                        || e.kind() == EntityBrawlerEffect.FIST) {
                    GlStateManager.scale(e.radius(), e.radius(), e.radius());
                    draw(e.kind() == EntityBrawlerEffect.FIST ? FIST : CLEAVER);
                } else {
                    double growth =
                            e.kind() == EntityBrawlerEffect.BLADE && !e.active()
                                    ? .2 + .8 * BrawlerScore.smooth((e.age() + partial) / 12D)
                                    : 1;
                    GlStateManager.scale(e.radius(), e.radius() * growth, e.radius());
                    draw(PROJECTILE_BLADE);
                }
                GlStateManager.enableTexture2D();
                GlStateManager.enableLighting();
                GlStateManager.color(1, 1, 1, 1);
                GlStateManager.popMatrix();
                return;
            }
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            WulfrumRayRenderer.begin();
            BufferBuilder b = Tessellator.getInstance().getBuffer();
            b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            float alpha = e.active() ? .85F : .45F;
            int color = e.active() ? 0xA7FFD0 : 0xE4C46C;
            Vec3d end = e.end().subtract(e.getPositionVector());
            if (e.kind() == EntityBrawlerEffect.GROUND_CELL) {
                double growth =
                        e.active()
                                ? Math.sin(
                                        Math.min(1, (e.age() + partial - e.warning()) / 16D)
                                                * Math.PI)
                                : 0;
                for (int edge = 0; edge < 6; edge++) {
                    double a = edge * Math.PI / 3, c = (edge + 1) * Math.PI / 3;
                    Vec3d
                            first =
                                    new Vec3d(
                                            Math.cos(a) * e.radius(),
                                            .04,
                                            Math.sin(a) * e.radius()),
                            last =
                                    new Vec3d(
                                            Math.cos(c) * e.radius(),
                                            .04,
                                            Math.sin(c) * e.radius());
                    WulfrumRayRenderer.tube(b, first, last, e.active() ? .055 : .022, color, alpha);
                    if (e.active()) {
                        Vec3d tip = first.scale(.7).addVector(0, 2.4 * growth, 0);
                        WulfrumRayRenderer.tube(b, first, tip, .075 * growth, 0x70F2A0, alpha);
                        WulfrumRayRenderer.tube(b, tip, last, .03 * growth, 0xDAFFD6, alpha * .7F);
                    }
                }
            } else if (e.kind() == EntityBrawlerEffect.PLANE) {
                Vec3d normal = e.end().normalize(),
                        across = normal.crossProduct(new Vec3d(0, 1, 0)).normalize(),
                        up = normal.crossProduct(across).normalize();
                double radius = e.radius();
                for (int band = -6; band <= 6; band++) {
                    double off = band * radius / 7, length = Math.sqrt(radius * radius - off * off);
                    Vec3d left = up.scale(off).subtract(across.scale(length)),
                            right = up.scale(off).add(across.scale(length));
                    WulfrumRayRenderer.tube(
                            b,
                            left,
                            right,
                            e.active() ? .035 : .014,
                            color,
                            alpha * (band == 0 ? .8F : .25F));
                }
                for (int n = 0; n < 48; n++) {
                    double a = n * Math.PI / 24, c = (n + 1) * Math.PI / 24;
                    WulfrumRayRenderer.tube(
                            b,
                            across.scale(Math.cos(a) * radius).add(up.scale(Math.sin(a) * radius)),
                            across.scale(Math.cos(c) * radius).add(up.scale(Math.sin(c) * radius)),
                            .045,
                            color,
                            alpha);
                }
            } else if (e.kind() == EntityBrawlerEffect.JETWASH) {
                Vec3d axis = end.normalize(),
                        side =
                                axis.crossProduct(
                                                Math.abs(axis.y) > .9
                                                        ? new Vec3d(1, 0, 0)
                                                        : new Vec3d(0, 1, 0))
                                        .normalize(),
                        up = axis.crossProduct(side);
                for (int strand = 0; strand < 9; strand++) {
                    Vec3d old = Vec3d.ZERO;
                    for (int j = 1; j <= 16; j++) {
                        double f = j / 16D,
                                a = strand * Math.PI * 2 / 9 + f * 8 - (e.age() + partial) * .3,
                                r = (.3 + e.radius() * f) * (.75 + .25 * Math.sin(j * 1.7));
                        Vec3d p =
                                end.scale(f)
                                        .add(side.scale(Math.cos(a) * r))
                                        .add(up.scale(Math.sin(a) * r));
                        WulfrumRayRenderer.tube(
                                b,
                                old,
                                p,
                                .025 + f * .035,
                                strand % 3 == 0 ? 0xDAFFD6 : 0x70F2A0,
                                alpha * (float) (1 - f * .65));
                        old = p;
                    }
                }
            } else if (e.kind() == EntityBrawlerEffect.CHAIN) {
                int count = Math.max(2, Math.min(96, (int) Math.ceil(end.lengthVector() / .30)));
                Vec3d axis = end.normalize(),
                        across =
                                axis.crossProduct(
                                                Math.abs(axis.y) > .9
                                                        ? new Vec3d(1, 0, 0)
                                                        : new Vec3d(0, 1, 0))
                                        .normalize();
                for (int link = 0; link < count; link++) {
                    double f = (link + .5) / count;
                    Vec3d center = end.scale(f).addVector(0, -.15 * Math.sin(f * Math.PI), 0);
                    Vec3d along = axis.scale(end.lengthVector() / count * .62),
                            side = (link % 2 == 0 ? across : axis.crossProduct(across)).scale(.105);
                    Vec3d[] corners = {
                        center.subtract(along).subtract(side),
                        center.add(along).subtract(side),
                        center.add(along).add(side),
                        center.subtract(along).add(side)
                    };
                    for (int edge = 0; edge < 4; edge++)
                        WulfrumRayRenderer.tube(
                                b,
                                corners[edge],
                                corners[(edge + 1) % 4],
                                .028,
                                edge == 1 ? 0xC3D0AE : 0x657B64,
                                .95F);
                    if (link % 4 == Math.floorMod(e.age() / 2, 4))
                        WulfrumRayRenderer.tube(
                                b, center.subtract(side), center.add(side), .018, 0x9CFFCE, .85F);
                }
            } else if (e.kind() == EntityBrawlerEffect.FAULT) {
                int steps = Math.max(2, Math.min(96, (int) (end.lengthVector() * 3)));
                Vec3d old = Vec3d.ZERO;
                Vec3d side = end.crossProduct(new Vec3d(0, 1, 0)).normalize();
                for (int i = 1; i <= steps; i++) {
                    double f = i / (double) steps;
                    Vec3d p = end.scale(f).add(side.scale(Math.sin(i * 2.1) * .16));
                    WulfrumRayRenderer.tube(b, old, p, e.active() ? .055 : .022, color, alpha);
                    if (i % 3 == 0) {
                        Vec3d branch =
                                p.add(side.scale((i % 2 == 0 ? 1 : -1) * (e.active() ? .65 : .28)))
                                        .addVector(0, e.active() ? .12 : 0, 0);
                        WulfrumRayRenderer.tube(b, p, branch, .018, color, alpha * .7F);
                    }
                    old = p;
                }
            } else if (e.kind() == EntityBrawlerEffect.HEX
                    || e.kind() == EntityBrawlerEffect.SHELL
                    || e.kind() == EntityBrawlerEffect.RECALL
                    || e.kind() == EntityBrawlerEffect.RING) {
                double r = e.radius();
                boolean ring = e.kind() == EntityBrawlerEffect.RING;
                if (ring)
                    r *=
                            Math.max(
                                    .05,
                                    Math.min(
                                            1,
                                            (e.age() + partial - e.warning() + 1)
                                                    / Math.max(1, e.life() - e.warning())));
                Vec3d normal = end.normalize(),
                        east =
                                normal.crossProduct(
                                                Math.abs(normal.y) > .9
                                                        ? new Vec3d(1, 0, 0)
                                                        : new Vec3d(0, 1, 0))
                                        .normalize(),
                        north = normal.crossProduct(east).normalize();
                Vec3d old = null;
                int sides = ring ? 36 : 6;
                for (int i = 0; i <= sides; i++) {
                    double a = i * Math.PI * 2 / sides;
                    Vec3d p =
                            ring
                                    ? new Vec3d(Math.cos(a) * r, .1, Math.sin(a) * r)
                                    : e.kind() == EntityBrawlerEffect.SHELL
                                            ? east.scale(Math.cos(a) * r)
                                                    .add(north.scale(Math.sin(a) * r))
                                            : new Vec3d(Math.cos(a) * r, Math.sin(a) * r, 0);
                    if (old != null)
                        WulfrumRayRenderer.tube(b, old, p, e.active() ? .045 : .018, color, alpha);
                    old = p;
                }
                if (ring && e.active())
                    for (int j = 0; j < 24; j++) {
                        double a = j * Math.PI / 12;
                        Vec3d foot = new Vec3d(Math.cos(a) * r, .10, Math.sin(a) * r);
                        Vec3d crest =
                                foot.scale(.94).addVector(0, .18 + .12 * Math.sin(j * 2.3), 0);
                        WulfrumRayRenderer.tube(b, foot, crest, .035, 0xDDFFE5, alpha * .7F);
                    }
            } else {
                double r = e.radius(), spin = (e.age() + partial) * .3;
                Vec3d a = new Vec3d(Math.cos(spin) * r, Math.sin(spin) * r, 0), c = a.scale(-1);
                WulfrumRayRenderer.tube(
                        b,
                        a,
                        c,
                        e.kind() == EntityBrawlerEffect.TERRAIN ? r * .5 : .12,
                        color,
                        alpha);
                WulfrumRayRenderer.tube(
                        b, new Vec3d(-a.y, a.x, 0), new Vec3d(a.y, -a.x, 0), .06, color, alpha);
            }
            Tessellator.getInstance().draw();
            WulfrumRayRenderer.finish();
            GlStateManager.popMatrix();
        }
    }
}
