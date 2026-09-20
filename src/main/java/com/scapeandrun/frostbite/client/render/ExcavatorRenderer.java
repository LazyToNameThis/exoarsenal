package com.scapeandrun.frostbite.client.render;

import com.scapeandrun.frostbite.client.ExcavatorModel;
import com.scapeandrun.frostbite.client.WholeModelTexture;
import com.scapeandrun.frostbite.entity.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import java.util.List;
import org.lwjgl.opengl.GL11;

public final class ExcavatorRenderer extends Render<EntityExcavator> {
    public ExcavatorRenderer(RenderManager m) {
        super(m);
        shadowSize = 1.6F;
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityExcavator e) {
        return WholeModelTexture.get("excavator");
    }

    @Override
    public void doRender(
            EntityExcavator e, double x, double y, double z, float yaw, float partial) {
        if (e.finale() == 5 && e.finaleTick() >= 80) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            WulfrumRayRenderer.begin();
            GlStateManager.depthMask(false);
            BufferBuilder cloud = Tessellator.getInstance().getBuffer();
            cloud.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            WulfrumDetonation.cloud(cloud, e.finaleTick() + partial - 80);
            Tessellator.getInstance().draw();
            GlStateManager.depthMask(true);
            WulfrumRayRenderer.finish();
            GlStateManager.popMatrix();
        }
        if (e.ticksExisted < 2 || e.brokenSegments() > 0) return;
        bindTexture(getEntityTexture(e));
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + 1.5, z);
        orient(
                e.prevRotationYaw
                        + net.minecraft.util.math.MathHelper.wrapDegrees(
                                        e.rotationYaw - e.prevRotationYaw)
                                * partial,
                e.prevRotationPitch
                        + net.minecraft.util.math.MathHelper.wrapDegrees(
                                        e.rotationPitch - e.prevRotationPitch)
                                * partial);
        GlStateManager.rotate(crashRoll(e, partial), 0, 0, 1);
        GlStateManager.scale(.1, .1, .1);
        draw(ExcavatorModel.HEAD);
        float tick = e.attackTick() + partial;
        ExcavatorActuation pose = actuation(e, partial);
        GlStateManager.rotate((float) pose.roll, 0, 0, 1);
        for (int i = 0; i < 4; i++) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(45 + i * 90, 0, 0, 1);
            GlStateManager.translate(11, 0, -2);
            GlStateManager.rotate((float) (pose.jaws * 24), 0, 1, 0);
            draw(ExcavatorModel.HEAD_JAW);
            GlStateManager.translate(-2, 0, -8);
            GlStateManager.scale(1, 1, 1 + pose.jaws * .35);
            draw(ExcavatorModel.PISTON);
            GlStateManager.popMatrix();
        }
        for (int side : new int[] {-1, 1}) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(side * 7, 9, -9);
            GlStateManager.rotate((float) (side * pose.vents * 32), 0, 0, 1);
            draw(ExcavatorModel.VENT);
            GlStateManager.popMatrix();
        }
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, pose.drill);
        if (e.expert()) {
            double extension = ExcavatorParryMotion.drillExtension(e.deep(), e.expertStage(), tick);
            if (e.expertStage() == 3)
                GlStateManager.rotate((float) (Math.sin(tick * .45) * 7), 0, 1, 0);
            GlStateManager.translate(0, 0, extension);
        }
        GlStateManager.rotate(e.drillRotation(partial), 0, 0, 1);
        double expansion = 1 + (e.deep() ? .15 : 0);
        GlStateManager.scale(expansion, expansion, 1);
        draw(ExcavatorModel.DRILL);
        if (e.expert() && e.deep() && (e.expertStage() != 11 || tick >= 110))
            for (int i = 0; i < 6; i++) {
                GlStateManager.pushMatrix();
                GlStateManager.rotate(i * 60, 0, 0, 1);
                GlStateManager.translate(9, 0, 4);
                GlStateManager.rotate(-18, 0, 1, 0);
                GlStateManager.scale(.24, .32, .65);
                draw(ExcavatorModel.DRILL);
                GlStateManager.popMatrix();
            }
        GlStateManager.popMatrix();
        if (e.expert() && e.expertStage() == 1 && tick >= 35 && tick < 80) {
            WulfrumRayRenderer.begin();
            BufferBuilder b = Tessellator.getInstance().getBuffer();
            b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            for (int strand = 0; strand < 2; strand++) {
                net.minecraft.util.math.Vec3d old = null;
                for (int i = 0; i <= 24; i++) {
                    double f = i / 24D,
                            a =
                                    f * Math.PI * 4
                                            + Math.toRadians(e.drillRotation(partial))
                                            + strand * Math.PI,
                            r = 11 - f * 8;
                    net.minecraft.util.math.Vec3d p =
                            new net.minecraft.util.math.Vec3d(
                                    Math.cos(a) * r, Math.sin(a) * r, 5 + f * 29);
                    if (old != null) WulfrumRayRenderer.tube(b, old, p, .18, 0xAEFFD2, .7F);
                    old = p;
                }
            }
            Tessellator.getInstance().draw();
            WulfrumRayRenderer.finish();
        }
        if (e.expert()
                && e.expertAttack() == ExcavatorExpertScore.COMPLETE
                && e.parryCount() > 0
                && (e.expertStage() == 3 || e.expertStage() == 4 || e.expertStage() == 6)) {
            WulfrumRayRenderer.begin();
            BufferBuilder b = Tessellator.getInstance().getBuffer();
            b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            for (int i = 0; i < 3; i++) {
                double a = i * Math.PI * 2 / 3;
                net.minecraft.util.math.Vec3d
                        p = new net.minecraft.util.math.Vec3d(Math.cos(a) * 7, Math.sin(a) * 7, 12),
                        q =
                                new net.minecraft.util.math.Vec3d(
                                        Math.cos(a + .2) * 12, Math.sin(a + .2) * 12, 12);
                WulfrumRayRenderer.tube(b, p, q, .35, 0x06190F, 1);
                WulfrumRayRenderer.tube(
                        b,
                        q,
                        new net.minecraft.util.math.Vec3d(Math.cos(a) * 16, Math.sin(a) * 16, 7),
                        .25,
                        0xAEFFD2,
                        .8F);
            }
            Tessellator.getInstance().draw();
            WulfrumRayRenderer.finish();
        }
        if (e.overboreShield()) {
            WulfrumRayRenderer.begin();
            BufferBuilder b = Tessellator.getInstance().getBuffer();
            b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            for (int i = 0; i < 6; i++) {
                double a = i * Math.PI / 3, c = (i + 1) * Math.PI / 3;
                WulfrumRayRenderer.tube(
                        b,
                        new net.minecraft.util.math.Vec3d(Math.cos(a) * 19, Math.sin(a) * 19, 8),
                        new net.minecraft.util.math.Vec3d(Math.cos(c) * 19, Math.sin(c) * 19, 8),
                        .3,
                        0x8DFFC0,
                        .55F);
            }
            Tessellator.getInstance().draw();
            WulfrumRayRenderer.finish();
        }
        if (e.attack() == ExcavatorClassicScore.Attack.FINAL_CHARGE && tick >= 45 && tick < 141) {
            for (int i = 0; i < 6; i++) {
                GlStateManager.pushMatrix();
                GlStateManager.rotate(i * 60, 0, 0, 1);
                GlStateManager.translate(0, 11, 12);
                GlStateManager.scale(.5, .5, .5);
                draw(ExcavatorModel.PROBE);
                GlStateManager.popMatrix();
            }
            WulfrumRayRenderer.begin();
            BufferBuilder b = Tessellator.getInstance().getBuffer();
            b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            for (int i = 0; i < 6; i++) {
                double a = i * Math.PI / 3;
                WulfrumRayRenderer.tube(
                        b,
                        new net.minecraft.util.math.Vec3d(Math.sin(a) * 11, Math.cos(a) * 11, 14),
                        new net.minecraft.util.math.Vec3d(0, 0, 34),
                        .25,
                        0xA0FFC4,
                        .4F + .3F * (float) Math.sin(tick * .2));
            }
            Tessellator.getInstance().draw();
            WulfrumRayRenderer.finish();
        }
        for (int i = 0; i < 4; i++) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(45 + i * 90, 0, 0, 1);
            GlStateManager.translate(9 + pose.braces * 2, -3, -6);
            GlStateManager.rotate((float) (8 + pose.braces * 62), 0, 1, 0);
            GlStateManager.scale(1, 1, .35 + pose.braces * .65);
            draw(ExcavatorModel.STABILIZER);
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
        if (e.finale() == 1 && e.finaleTick() >= 70 && e.finaleTick() < 135) {
            double scan = (e.finaleTick() + partial) * .07;
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + Math.cos(scan) * 4, y + 5, z + Math.sin(scan) * 4);
            GlStateManager.scale(.08, .08, .08);
            draw(ExcavatorModel.PROBE);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            WulfrumRayRenderer.begin();
            BufferBuilder b = Tessellator.getInstance().getBuffer();
            b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            net.minecraft.util.math.Vec3d from =
                    new net.minecraft.util.math.Vec3d(Math.cos(scan) * 4, 5, Math.sin(scan) * 4);
            for (int i = -1; i <= 1; i++)
                WulfrumRayRenderer.tube(
                        b,
                        from,
                        new net.minecraft.util.math.Vec3d(i * .7, 1.5, 0),
                        .025,
                        0xF7D477,
                        .8F);
            Tessellator.getInstance().draw();
            WulfrumRayRenderer.finish();
            GlStateManager.popMatrix();
        }
    }

    static void orient(float yaw, float pitch) {
        GlStateManager.rotate(yaw, 0, 1, 0);
        GlStateManager.rotate(pitch, 1, 0, 0);
    }

    private static ExcavatorActuation actuation(EntityExcavator e, float partial) {
        return e.actuation(partial);
    }

    private static float crashRoll(EntityExcavator boss, float partial) {
        if (boss == null) return 0;
        if (boss.finale() == 5)
            return (float) (540 * ExcavatorParryMotion.smooth((boss.finaleTick() + partial) / 80));
        if (boss.finale() == 1) {
            double t = boss.finaleTick() + partial;
            return (float) (18 * Math.sin(t * .16) * ExcavatorParryMotion.smooth((t - 35) / 30));
        }
        if (!boss.expert() || boss.finale() > 0) return 0;
        float t = (boss.attackTick() + partial);
        if (boss.expertAttack() == ExcavatorExpertScore.COMPLETE && boss.expertStage() == 4)
            return (float) (720 * ExcavatorParryMotion.smooth(t / ExcavatorParryMotion.RECOIL_END));
        if (boss.expertAttack() == ExcavatorExpertScore.TENNIS && boss.parryCount() == 3) {
            if (boss.expertStage() == 4) return (float) (85 * ExcavatorParryMotion.smooth(t / 24));
            if (boss.expertStage() == 6)
                return (float) (85 * (1 - ExcavatorParryMotion.smooth((t - 15) / 25)));
        }
        return 0;
    }

    public static void draw(List<ExcavatorModel.Face> faces) {
        GlStateManager.disableCull();
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        for (ExcavatorModel.Face f : faces) {
            float shade = f.shade;

            float red = f.region == 9 || f.region == 18 ? .91F : 1F,
                    green = 1F,
                    blue = f.region < 8 ? .93F : 1F;
            for (int i = 0; i < 4; i++) {
                double[] p = f.points[i];
                double u = ((f.region % 8) * 8 + (i == 0 || i == 3 ? .1 : 7.9)) / 64,
                        v = ((f.region / 8) * 8 + (i < 2 ? .1 : 7.9)) / 32;
                b.pos(p[0], p[1], p[2])
                        .tex(u, v)
                        .color(shade * red, shade * green, shade * blue, 1)
                        .normal(f.nx, f.ny, f.nz)
                        .endVertex();
            }
        }
        Tessellator.getInstance().draw();
        GlStateManager.enableCull();
        GlStateManager.color(1, 1, 1, 1);
    }

    public static final class Segment extends Render<EntityExcavatorSegment> {
        public Segment(RenderManager m) {
            super(m);
        }

        @Override
        protected ResourceLocation getEntityTexture(EntityExcavatorSegment e) {
            return WholeModelTexture.get("excavator");
        }

        @Override
        public void doRender(
                EntityExcavatorSegment e, double x, double y, double z, float yaw, float partial) {
            if (e.boss() != null && e.index() < e.boss().brokenSegments()) return;
            bindTexture(getEntityTexture(e));
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y + 1, z);
            orient(
                    e.prevRotationYaw
                            + net.minecraft.util.math.MathHelper.wrapDegrees(
                                            e.rotationYaw - e.prevRotationYaw)
                                    * partial,
                    e.prevRotationPitch
                            + net.minecraft.util.math.MathHelper.wrapDegrees(
                                            e.rotationPitch - e.prevRotationPitch)
                                    * partial);
            GlStateManager.rotate(crashRoll(e.boss(), partial), 0, 0, 1);
            GlStateManager.scale(.1, .1, .1);
            draw(e.index() == 17 ? ExcavatorModel.TAIL : ExcavatorModel.body(e.index()));
            EntityExcavator boss = e.boss();
            if (e.index() >= 2 && e.index() <= 11)
                for (int side = 0; side < 2; side++) {
                    int socket = (e.index() - 2) * 2 + side;
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(side == 0 ? -13 : 13, 5, 0);
                    GlStateManager.rotate(side == 0 ? -90 : 90, 0, 1, 0);
                    GlStateManager.scale(.65, .65, .65);
                    draw(ExcavatorModel.PROBE_SOCKET);
                    if (boss == null || !boss.probeOut(socket)) draw(ExcavatorModel.PROBE);
                    GlStateManager.popMatrix();
                }
            GlStateManager.popMatrix();
            if (e.index() % 2 == 0 && e.index() < 14) {
                GlStateManager.pushMatrix();
                net.minecraft.util.math.Vec3d socket =
                        e.cannonBase().subtract(e.getPositionVector());
                GlStateManager.translate(x + socket.x, y + socket.y, z + socket.z);
                orient(e.aimYaw(), e.aimPitch());
                GlStateManager.scale(.1, .1, .1);
                float deploy =
                        boss != null
                                        && (boss.expert()
                                                || boss.deep()
                                                || boss.attack()
                                                                != ExcavatorClassicScore.Attack
                                                                        .SEISMIC_BORE
                                                        && boss.attack()
                                                                != ExcavatorClassicScore.Attack
                                                                        .TRENCH_CUTTER)
                                ? 1
                                : .5F;
                if (boss != null && boss.transition() > 0)
                    deploy = (float) ExcavatorActuation.ease(boss.transition() / 65F);
                GlStateManager.rotate((1 - deploy) * 65, 1, 0, 0);
                if (boss != null && !boss.expert() && boss.failing() && e.index() == 4)
                    GlStateManager.rotate(28, 0, 0, 1);
                draw(ExcavatorModel.TURRET);
                GlStateManager.pushMatrix();
                double recoil = ExcavatorActuation.recoil(e.shotAge(partial));
                GlStateManager.translate(0, 0, -2.8 * recoil);
                draw(ExcavatorModel.TURRET_BARREL);
                GlStateManager.popMatrix();
                if (e.shotAge(partial) >= 0 && e.shotAge(partial) < 5) {
                    WulfrumRayRenderer.begin();
                    BufferBuilder flash = Tessellator.getInstance().getBuffer();
                    flash.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                    double spread = (1 - e.shotAge(partial) / 5) * 3;
                    for (int i = 0; i < 4; i++) {
                        double a = i * Math.PI / 2;
                        WulfrumRayRenderer.tube(
                                flash,
                                new net.minecraft.util.math.Vec3d(0, 9.5, 27),
                                new net.minecraft.util.math.Vec3d(
                                        Math.cos(a) * spread, 9.5 + Math.sin(a) * spread, 31),
                                .2,
                                0xC9FFDE,
                                .85F);
                    }
                    Tessellator.getInstance().draw();
                    WulfrumRayRenderer.finish();
                }
                GlStateManager.popMatrix();
            }
        }
    }

    public static final class Probe extends Render<EntityExcavatorProbe> {
        public Probe(RenderManager m) {
            super(m);
        }

        @Override
        protected ResourceLocation getEntityTexture(EntityExcavatorProbe e) {
            return WholeModelTexture.get("excavator");
        }

        @Override
        public void doRender(
                EntityExcavatorProbe e, double x, double y, double z, float yaw, float partial) {
            float windup = e.combatTick() - ExcavatorProbeScore.blades(e.expertProbe());
            if (e.mode() == 0 && windup >= 12 && windup < 26) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y + .4, z);
                WulfrumRayRenderer.begin();
                BufferBuilder line = Tessellator.getInstance().getBuffer();
                line.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                net.minecraft.util.math.Vec3d end = e.mark().subtract(e.getPositionVector()),
                        side =
                                new net.minecraft.util.math.Vec3d(end.z, 0, -end.x)
                                        .normalize()
                                        .scale(.65);
                for (int s : new int[] {-1, 1})
                    WulfrumRayRenderer.tube(
                            line, side.scale(s), end.add(side.scale(s)), .025, 0xFFE69A, .8F);
                Tessellator.getInstance().draw();
                WulfrumRayRenderer.finish();
                GlStateManager.popMatrix();
            }
            float t = e.combatTick() + partial,
                    extension =
                            e.mode() == 0 ? ExcavatorProbeScore.extension(e.expertProbe(), t) : 0;
            boolean thrown =
                    e.mode() == 0 && ExcavatorProbeScore.thrown(e.expertProbe(), e.combatTick());
            bindTexture(getEntityTexture(e));
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y + .4, z);
            orient(-e.rotationYaw, e.rotationPitch);
            GlStateManager.scale(.075, .075, .075);
            draw(ExcavatorModel.PROBE);
            GlStateManager.pushMatrix();
            double gunRecoil = 0;
            for (int age = 0; age <= 14; age++)
                if (ExcavatorProbeScore.shot(e.expertProbe(), e.combatTick() - 10 - age))
                    gunRecoil = Math.max(gunRecoil, ExcavatorActuation.recoil(age + partial));
            GlStateManager.translate(0, 0, -2 * gunRecoil);
            GlStateManager.scale(1, 1, Math.max(.01, 1 - extension));
            draw(ExcavatorModel.PROBE_GUN);
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
            if (extension > 0) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y, z);
                WulfrumRayRenderer.begin();
                BufferBuilder blade = Tessellator.getInstance().getBuffer();
                blade.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                net.minecraft.util.math.Vec3d forward = e.getLookVec(),
                        side =
                                new net.minecraft.util.math.Vec3d(forward.z, 0, -forward.x)
                                        .normalize(),
                        origin =
                                thrown
                                        ? e.mark().subtract(e.getPositionVector())
                                        : net.minecraft.util.math.Vec3d.ZERO;
                float q = t - ExcavatorProbeScore.blades(e.expertProbe());
                double sweep =
                        !thrown && q >= 26 && q < 60 ? Math.sin((q - 26) / 34 * Math.PI) * .9 : 0;
                for (int sign : new int[] {-1, 1}) {
                    net.minecraft.util.math.Vec3d base = origin.add(side.scale(sign * .7)),
                            tip =
                                    base.add(forward.scale(1.6 * extension))
                                            .add(side.scale(sign * sweep));
                    net.minecraft.util.math.Vec3d
                            shoulder = base.add(tip.subtract(base).scale(.38)),
                            edge = side.scale(.17 * extension);
                    vertex(blade, base.subtract(edge), .25F, 1, .6F, .7F);
                    vertex(blade, shoulder.subtract(edge), .25F, 1, .6F, .7F);
                    vertex(blade, tip, .8F, 1, .9F, 1);
                    vertex(blade, shoulder.add(edge), .25F, 1, .6F, .7F);
                    WulfrumRayRenderer.tube(blade, base, tip, .032, 0xE3FFF0, 1);
                    if (!thrown && q >= 26 && q < 60)
                        for (int trail = 1; trail <= 4; trail++) {
                            double old = Math.max(26, q - trail * 1.5),
                                    oldSweep = Math.sin((old - 26) / 34 * Math.PI) * .9;
                            net.minecraft.util.math.Vec3d tail =
                                    base.add(forward.scale(1.6 * extension - trail * .2))
                                            .add(side.scale(sign * oldSweep));
                            WulfrumRayRenderer.tube(blade, tail, tip, .025, 0x64FFB3, .22F / trail);
                        }
                    if (thrown && q >= 112) {
                        net.minecraft.util.math.Vec3d mount = side.scale(sign * .65);
                        for (int link = 0; link < 20; link++) {
                            double f = link / 20D, g = (link + .75) / 20D;
                            WulfrumRayRenderer.tube(
                                    blade,
                                    mount.add(base.subtract(mount).scale(f)),
                                    mount.add(base.subtract(mount).scale(g)),
                                    .04,
                                    link % 2 == 0 ? 0xA8C9AD : 0x496754,
                                    .9F);
                        }
                    }
                }
                Tessellator.getInstance().draw();
                WulfrumRayRenderer.finish();
                GlStateManager.popMatrix();
            }
            if (e.mode() == 6) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y, z);
                WulfrumRayRenderer.begin();
                BufferBuilder b = Tessellator.getInstance().getBuffer();
                b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                WulfrumRayRenderer.tube(
                        b,
                        net.minecraft.util.math.Vec3d.ZERO,
                        e.mark().subtract(e.getPositionVector()),
                        .06,
                        0xA8FFD4,
                        .9F);
                Tessellator.getInstance().draw();
                WulfrumRayRenderer.finish();
                GlStateManager.popMatrix();
            }
            if (e.mode() == 1 || e.mode() == 2 || e.mode() == 5) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y + .4, z);
                WulfrumRayRenderer.begin();
                BufferBuilder b = Tessellator.getInstance().getBuffer();
                b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                net.minecraft.util.math.Vec3d target = e.mark().subtract(e.getPositionVector());
                for (int i = -1; i <= 1; i++)
                    WulfrumRayRenderer.tube(
                            b,
                            net.minecraft.util.math.Vec3d.ZERO,
                            target.addVector(i * 1.5, 0, 0),
                            .015,
                            e.mode() == 2 ? 0xD5FFC0 : 0x4ACC85,
                            .45F);
                b.pos(0, 0, 0).color(.25F, 1F, .55F, .08F).endVertex();
                b.pos(target.x - 2, target.y, target.z).color(.25F, 1F, .55F, .18F).endVertex();
                b.pos(target.x + 2, target.y, target.z).color(.25F, 1F, .55F, .18F).endVertex();
                b.pos(0, 0, 0).color(.25F, 1F, .55F, .08F).endVertex();
                for (int i = 0; i < 4; i++) {
                    double dx = i == 0 || i == 3 ? -2 : 2, dz = i < 2 ? -2 : 2;
                    b.pos(target.x + dx, target.y - .7, target.z + dz)
                            .color(.3F, 1F, .6F, e.mode() == 2 ? .2F : .1F)
                            .endVertex();
                }
                Tessellator.getInstance().draw();
                WulfrumRayRenderer.finish();
                GlStateManager.popMatrix();
            }
        }
    }

    private static void vertex(
            BufferBuilder b,
            net.minecraft.util.math.Vec3d p,
            float r,
            float g,
            float blue,
            float alpha) {
        b.pos(p.x, p.y, p.z).color(r, g, blue, alpha).endVertex();
    }
}
