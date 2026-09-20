package com.scapeandrun.frostbite.client.render;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.entity.*;
import com.scapeandrun.frostbite.client.WholeModelTexture;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public final class WulfrumEyeRenderer extends GeoEntityRenderer<EntityWulfrumEye> {
    public static boolean foreground;
    private float echoAlpha = 1;
    private int phaseTint = 0xFFFFFF;

    public WulfrumEyeRenderer(RenderManager manager) {
        super(manager, new EyeModel());
        shadowSize = 1.5F;
    }

    @Override
    public void doRender(
            EntityWulfrumEye e, double x, double y, double z, float yaw, float partial) {
        if (e.ticksExisted < 2) return;
        if (!e.changing() && e.attackTick() > 0 && e.attackTick() < 24) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y + 1.5, z);
            WulfrumRayRenderer.begin();
            BufferBuilder warning = Tessellator.getInstance().getBuffer();
            warning.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            double radius = 2.1 - (e.attackTick() + partial) / 24D * .6;
            int color = e.seer() ? 0xF2DB8D : 0x8DE6F2;
            for (int i = 0; i < 32; i++) {
                double a = i * Math.PI / 16, c = (i + 1) * Math.PI / 16;
                WulfrumRayRenderer.tube(
                        warning,
                        new Vec3d(Math.cos(a) * radius, .15, Math.sin(a) * radius),
                        new Vec3d(Math.cos(c) * radius, .15, Math.sin(c) * radius),
                        .035,
                        color,
                        .7F);
            }
            Tessellator.getInstance().draw();
            WulfrumRayRenderer.finish();
            GlStateManager.popMatrix();
        }
        phaseTint =
                e.overclocked()
                        ? java.awt.Color.HSBtoRGB(
                                        (e.ticksExisted + partial) * .003F % 1,
                                        (e.attack() == 44
                                                                && e.attackTick() > 35
                                                                && e.attackTick() < 280
                                                        || e.attack() == 54
                                                                && e.attackTick() > 260
                                                                && e.attackTick() < 320)
                                                ? .04F
                                                : .32F,
                                        (e.attack() == 44 || e.attack() == 54)
                                                        && e.attackTick() > 315
                                                ? .6F
                                                : 1)
                                & 0xFFFFFF
                        : 0xFFFFFF;
        if (e.attack() == WulfrumDuetScore.FAILURE
                && e.attackTick() > 40
                && e.attackTick() < 235
                && e.attackTick() % 22 < 5) phaseTint = 0x52695C;
        if (!foreground && e.survivorTick() >= 25 && e.survivorTick() < 135) return;

        double collapse = e.getHealth() <= 0 ? Math.max(.03, 1 - (e.deathTime + partial) / 20D) : 1;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + 1.5, z);
        GlStateManager.scale(24D / 7 * collapse, 24D / 7 * collapse, 24D / 7 * collapse);
        GlStateManager.translate(0, -.5, 0);
        super.doRender(e, 0, 0, 0, yaw, partial);
        GlStateManager.popMatrix();
        if (echoAlpha == 1 && e.getHealth() > 0) opticGlow(e, x, y, z, partial);
        if (echoAlpha == 1 && e.getHealth() > 0) exhaust(e, x, y, z, partial);
        if (e.overclocked() && e.getHealth() > 0) appendages(e, x, y, z, partial);
        EntityWulfrumEye other = e.partner();
        if (echoAlpha < 1
                || e.seer()
                || other == null
                || !e.chainAttached() && e.chainAnchor() == null) return;
        Vec3d mount = e.getLookVec().scale(-1.5),
                a = position(e, partial).add(mount),
                b = position(other, partial).subtract(other.getLookVec().scale(1.5)),
                delta = b.subtract(a);
        double length = delta.lengthVector();
        int links = Math.min(72, Math.max(8, (int) (length * 2.5)));
        double sag = Math.min(3, length * .12);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + mount.x, y + 1.5 + mount.y, z + mount.z);
        WulfrumRayRenderer.begin();
        BufferBuilder mesh = Tessellator.getInstance().getBuffer();
        mesh.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        Vec3d anchor = e.chainAnchor();
        Vec3d[] chain =
                anchor == null
                        ? new Vec3d[] {Vec3d.ZERO, delta}
                        : !e.chainAttached()
                                ? new Vec3d[] {delta, anchor.subtract(a).addVector(0, -1.5, 0)}
                                : new Vec3d[] {
                                    Vec3d.ZERO, anchor.subtract(a).addVector(0, -1.5, 0), delta
                                };
        boolean driving =
                e.attack() == WulfrumDuetScore.DRIVE
                        && e.attackTick() >= 32
                        && e.attackTick() < 208;
        boolean dragging = e.attack() == WulfrumDuetScore.SANDER;
        double beltOffset =
                driving ? ((e.ticksExisted + partial) * (.35 + e.attackTick() * .004)) % 1 : 0;
        for (int segment = 1; segment < chain.length; segment++) {
            Vec3d start = chain[segment - 1], span = chain[segment].subtract(start);
            int count = Math.min(80, Math.max(8, (int) (span.lengthVector() * 2.5)));
            for (int i = 0; i < count; i++) {
                double t = (i + beltOffset) / count,
                        u = Math.min(1, (i + .82 + beltOffset) / count);
                double droop = driving ? 0 : dragging ? .12 : anchor == null ? sag : .15;
                Vec3d p = start.add(span.scale(t)).addVector(0, -Math.sin(t * Math.PI) * droop, 0),
                        q =
                                start.add(span.scale(u))
                                        .addVector(0, -Math.sin(u * Math.PI) * droop, 0);
                WulfrumRayRenderer.tube(
                        mesh, p, q, i % 2 == 0 ? .18 : .13, i % 2 == 0 ? 0x747869 : 0x303A31, 1);
                if (i % 3 == 0) WulfrumRayRenderer.tube(mesh, p, q, .185, 0x54C986, .75F);
            }
        }
        Tessellator.getInstance().draw();
        WulfrumRayRenderer.finish();
        GlStateManager.popMatrix();
    }

    private void appendages(EntityWulfrumEye e, double x, double y, double z, float partial) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + 1.5, z);
        WulfrumRayRenderer.begin();
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        float age = e.ticksExisted + partial;
        int glow = java.awt.Color.HSBtoRGB((age * .003F) % 1, .45F, 1) & 0xFFFFFF;
        if (e.attack() < 35)
            for (int i = 0; i < 6; i++) {
                double[] q = WulfrumDuetScore.appendage(age, i, e.seer());
                Vec3d tip = new Vec3d(q[0], q[1], q[2]), last = tip.scale(.2);
                for (int j = 1; j <= 8; j++) {
                    double f = j / 8D;
                    Vec3d p =
                            tip.scale(.2 + .8 * f)
                                    .addVector(0, 0, Math.sin(f * Math.PI) * (e.seer() ? 1.2 : .3));
                    WulfrumRayRenderer.tube(b, last, p, e.seer() ? .13 : .22, 0x626B57, 1);
                    WulfrumRayRenderer.tube(b, last, p, .065, glow, 1);
                    last = p;
                }
                Vec3d end = tip.add(e.getLookVec().scale(e.seer() ? 1.5 : 1));
                WulfrumRayRenderer.tube(
                        b, tip, end, e.seer() ? .1 : .27, e.seer() ? glow : 0x303B32, 1);
                WulfrumRayRenderer.tube(b, end, end.add(e.getLookVec().scale(.18)), .16, glow, 1);
            }
        if (e.energyShield() > 0)
            for (int i = 0; i < 36; i++) {
                double a = i * Math.PI / 18, c = (i + 1) * Math.PI / 18;
                WulfrumRayRenderer.tube(
                        b,
                        new Vec3d(Math.cos(a) * 1.9, Math.sin(a) * 1.9, 0),
                        new Vec3d(Math.cos(c) * 1.9, Math.sin(c) * 1.9, 0),
                        .035,
                        0xB8FFD5,
                        .3F + .5F * e.energyShield() / 50);
            }
        if (e.attack() == 53 && e.attackTick() > 30 && e.attackTick() < 232) {
            double[] focus = WulfrumSurvivorScore.focus(e.attackTick() + partial);
            double radius = .18 + Math.min(1, (e.attackTick() - 30) / 150D) * .75;
            Vec3d center = new Vec3d(focus[0], focus[1], focus[2]);
            for (int layer = 0; layer < 2; layer++)
                for (int lat = 0; lat < 12; lat++)
                    for (int lon = 0; lon < 20; lon++)
                        for (int corner = 0; corner < 4; corner++) {
                            double a = (lat + (corner >= 2 ? 1 : 0)) * Math.PI / 12 - Math.PI / 2,
                                    c = (lon + (corner == 1 || corner == 2 ? 1 : 0)) * Math.PI / 10,
                                    r = radius * (layer == 0 ? 1 : 1.3);
                            Vec3d p =
                                    center.addVector(
                                            Math.cos(a) * Math.cos(c) * r,
                                            Math.sin(a) * r,
                                            Math.cos(a) * Math.sin(c) * r);
                            b.pos(p.x, p.y, p.z)
                                    .color(
                                            layer == 0 ? .85F : .25F,
                                            1F,
                                            .7F,
                                            layer == 0 ? .95F : .16F)
                                    .endVertex();
                        }
        }
        Tessellator.getInstance().draw();
        WulfrumRayRenderer.finish();
        GlStateManager.popMatrix();
    }

    private void exhaust(EntityWulfrumEye e, double x, double y, double z, float partial) {
        Vec3d forward = e.getLook(partial).normalize(),
                right = forward.crossProduct(new Vec3d(0, 1, 0));
        if (right.lengthSquared() < .001) right = new Vec3d(1, 0, 0);
        right = right.normalize();
        Vec3d up = right.crossProduct(forward).normalize();
        double tick = e.attackTick() + partial,
                roll =
                        e.attack() >= 16
                                ? WulfrumDuetScore.spin(e.attack(), (float) tick)
                                : e.attack() == SeerObserverPattern.DOUBLE_PENDULUM
                                        ? Math.sin(tick * .093) * 1.4
                                        : 0;
        Vec3d lateral = right.scale(Math.cos(roll)).add(up.scale(Math.sin(roll)));
        double speed =
                Math.sqrt(
                        (e.posX - e.prevPosX) * (e.posX - e.prevPosX)
                                + (e.posY - e.prevPosY) * (e.posY - e.prevPosY)
                                + (e.posZ - e.prevPosZ) * (e.posZ - e.prevPosZ));
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + 1.5, z);
        WulfrumRayRenderer.begin();
        GlStateManager.depthMask(false);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (int side : new int[] {-1, 1}) {
            Vec3d nozzle = forward.scale(-.98).add(lateral.scale(side * .52)),
                    direction = forward.scale(-1).add(lateral.scale(side * .28));
            WulfrumFire.emit(
                    buffer,
                    nozzle,
                    direction,
                    .8 + Math.min(3.6, speed * 1.5),
                    .3,
                    e.ticksExisted + partial + side * 5);
        }
        Tessellator.getInstance().draw();
        GlStateManager.depthMask(true);
        WulfrumRayRenderer.finish();
        GlStateManager.popMatrix();
    }

    private void opticGlow(EntityWulfrumEye e, double x, double y, double z, float partial) {
        if (e.transformTick() > 0) return;
        Vec3d forward = e.getLookVec(), right = forward.crossProduct(new Vec3d(0, 1, 0));
        if (right.lengthSquared() < .001) right = new Vec3d(1, 0, 0);
        right = right.normalize();
        Vec3d up = right.crossProduct(forward).normalize();
        double distance = (e.upgraded() ? (e.seer() ? 1.19 : 3.43) : .84) * 8 / 7,
                width = (e.upgraded() ? .25 : .15) * 8 / 7,
                height = (e.upgraded() ? .25 : .31) * 8 / 7;
        Vec3d center = forward.scale(distance).addVector(0, 1.5, 0);
        float pulse = .16F + .06F * (float) Math.sin((e.ticksExisted + partial) * .12),
                oldX = OpenGlHelper.lastBrightnessX,
                oldY = OpenGlHelper.lastBrightnessY;
        boolean firing =
                !e.seer()
                        && e.upgraded()
                        && e.attack() == SeerObserverPattern.MACHINEGUN
                        && e.attackTick() > 36
                        && e.attackTick() < 162
                        && e.attackTick() % 6 < 2;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        WulfrumRayRenderer.begin();
        GlStateManager.depthMask(false);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < 4; i++) {
            double a = i == 0 || i == 3 ? -1 : 1, c = i < 2 ? -1 : 1;
            Vec3d p = center.add(right.scale(a * width)).add(up.scale(c * height));
            b.pos(p.x, p.y, p.z)
                    .color(firing ? .8F : .25F, 1F, .55F, firing ? .65F : pulse)
                    .endVertex();
        }
        Tessellator.getInstance().draw();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, oldX, oldY);
        GlStateManager.depthMask(true);
        WulfrumRayRenderer.finish();
        GlStateManager.popMatrix();
    }

    public void renderEcho(
            EntityWulfrumEye source,
            EntityWulfrumEcho echo,
            double x,
            double y,
            double z,
            float partial) {
        float yaw = source.rotationYaw,
                oldYaw = source.prevRotationYaw,
                pitch = source.rotationPitch,
                oldPitch = source.prevRotationPitch,
                body = source.renderYawOffset,
                oldBody = source.prevRenderYawOffset,
                head = source.rotationYawHead,
                oldHead = source.prevRotationYawHead;
        try {
            echoAlpha = .24F;
            source.rotationYaw =
                    source.prevRotationYaw =
                            source.renderYawOffset =
                                    source.prevRenderYawOffset =
                                            source.rotationYawHead =
                                                    source.prevRotationYawHead = echo.rotationYaw;
            source.rotationPitch = source.prevRotationPitch = echo.rotationPitch;
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.depthMask(false);
            doRender(source, x, y, z, echo.rotationYaw, partial);
        } finally {
            source.rotationYaw = yaw;
            source.prevRotationYaw = oldYaw;
            source.rotationPitch = pitch;
            source.prevRotationPitch = oldPitch;
            source.renderYawOffset = body;
            source.prevRenderYawOffset = oldBody;
            source.rotationYawHead = head;
            source.prevRotationYawHead = oldHead;
            echoAlpha = 1;
            GlStateManager.depthMask(true);
            GlStateManager.disableBlend();
            GlStateManager.color(1, 1, 1, 1);
        }
    }

    @Override
    public void renderRecursively(
            BufferBuilder b,
            software.bernie.geckolib3.geo.render.built.GeoBone bone,
            float r,
            float g,
            float blue,
            float alpha) {
        super.renderRecursively(
                b,
                bone,
                echoAlpha < 1 ? .4F : phaseTint == 0xFFFFFF ? r : (phaseTint >> 16 & 255) / 255F,
                phaseTint == 0xFFFFFF ? g : (phaseTint >> 8 & 255) / 255F,
                echoAlpha < 1 ? .65F : phaseTint == 0xFFFFFF ? blue : (phaseTint & 255) / 255F,
                echoAlpha < 1 ? echoAlpha : alpha);
    }

    private static Vec3d position(EntityWulfrumEye e, float p) {
        return new Vec3d(
                e.lastTickPosX + (e.posX - e.lastTickPosX) * p,
                e.lastTickPosY + (e.posY - e.lastTickPosY) * p,
                e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * p);
    }

    private static final class EyeModel extends AnimatedGeoModel<EntityWulfrumEye> {
        public ResourceLocation getModelLocation(EntityWulfrumEye e) {
            return new ResourceLocation(Frostbite.MODID, "geo/wulfrum_eyes.geo.json");
        }

        public ResourceLocation getTextureLocation(EntityWulfrumEye e) {
            return WholeModelTexture.get("wulfrum_eyes");
        }

        public ResourceLocation getAnimationFileLocation(EntityWulfrumEye e) {
            return new ResourceLocation(Frostbite.MODID, "animations/wulfrum_eyes.animation.json");
        }

        @Override
        public void setLivingAnimations(EntityWulfrumEye e, Integer id, AnimationEvent event) {
            super.setLivingAnimations(e, id, event);
            float t = e.attackTick() + event.getPartialTick(),
                    age = e.ticksExisted + event.getPartialTick();
            boolean guard = SeerObserverPattern.guard(e.attack(), e.attackTick());
            boolean catchPose = guard || e.counterTick() > 0 && e.counterTick() < 18;
            getBone("seerKeel").setHidden(!e.seer());
            getBone("observerCooling").setHidden(e.seer());
            getBone("blade").setHidden(!e.seer() && !e.solo());
            getBone("blade").setRotationZ(catchPose ? .7F : !e.seer() ? .22F : 0);
            getBone("blade").setRotationY(catchPose ? .7F : !e.seer() ? .12F : 0);
            float extend =
                    e.attack() == SeerObserverPattern.SYNCHRONIZATION && t > 38 && t < 296
                            ? 1.65F
                            : e.attack() == SeerObserverPattern.PASSING && t > 38 && t < 80
                                    ? 1.35F
                                    : e.solo() ? 1.2F : 1;
            getBone("blade").setScaleZ(extend);
            if (e.seer() && e.attack() == SeerObserverPattern.RICOCHET) {
                float recoil = Math.max(Math.max(pulse(t, 38), pulse(t, 76)), pulse(t, 114));
                getBone("blade").setRotationY(-recoil * .65F);
                getBone("blade").setRotationZ(recoil * .5F);
                if (t >= 148 && t < 188) {
                    getBone("blade").setRotationY(.45F + (t - 168) * .016F);
                    getBone("blade").setRotationZ(.65F);
                }
            }
            getBone("eye").setRotationX((float) Math.toRadians(e.rotationPitch));
            getBone("eye").setRotationZ((float) Math.sin(age * .035) * .045F);
            if (e.attack() == SeerObserverPattern.FLAIL)
                getBone("eye").setRotationZ(e.seer() ? t * .08F : 0);
            if (e.attack() == SeerObserverPattern.SYNCHRONIZATION && t > 296)
                getBone("eye").setRotationX(.4F);
            getBone("lens").setScaleY(guard ? .65F : 1);
            getBone("iris").setScaleY(.92F + .08F * (float) Math.sin(age * .09));
            for (String name :
                    new String[] {"leftEmitter", "rightEmitter", "upperEmitter", "lowerEmitter"})
                getBone(name).setHidden(e.seer());
            for (String name : new String[] {"leftFin", "rightFin", "lowerFin"})
                getBone(name).setHidden(!e.seer());
            float open = .18F + .12F * (float) Math.sin(t * .055);
            getBone("leftEmitter").setRotationY(open);
            getBone("rightEmitter").setRotationY(-open);
            getBone("upperEmitter").setRotationX(open);
            getBone("lowerEmitter").setRotationX(-open);
            getBone("leftFin").setRotationZ(-.35F);
            getBone("rightFin").setRotationZ(.35F);
            getBone("lowerFin").setRotationX(.2F);
            float deployment =
                    e.upgraded()
                            ? (e.transformTick() > 0
                                    ? SeerObserverPattern.deployment(
                                            e.transformTick() + event.getPartialTick())
                                    : 1)
                            : 0;
            float retract =
                    e.upgraded()
                            ? (e.transformTick() > 0
                                    ? Math.min(1, (e.transformTick() + event.getPartialTick()) / 18)
                                    : 1)
                            : 0;
            if (e.coordinating() && e.seer()) {
                boolean saw = e.attack() == SeerObserverPattern.SAW_ORBIT;
                deployment = saw ? 1 : 0;
                retract = saw ? 1 : 0;
            }
            getBone("lens").setPositionZ(retract * 3);
            getBone("lens").setHidden(retract >= 1);
            getBone("blade").setHidden(retract >= 1 || !e.seer() && !e.solo());
            getBone("blade").setScaleZ(extend * Math.max(.01F, 1 - retract));
            getBone("sawRotor").setHidden(!e.seer() || deployment <= 0);
            getBone("machinegun").setHidden(e.seer() || deployment <= 0);
            getBone("sawRotor").setScaleX(Math.max(.01F, deployment));
            getBone("sawRotor").setScaleY(Math.max(.01F, deployment));
            getBone("sawRotor").setPositionZ((1 - deployment) * 2);
            getBone("sawRotor").setRotationZ(age * .36F * deployment);
            if (e.attack() == WulfrumDuetScore.DRIVE)
                getBone("sawRotor").setRotationZ((t * .36F + t * t * .003F) * deployment);
            getBone("machinegun").setScaleZ(Math.max(.01F, deployment));
            getBone("machinegun").setPositionZ((1 - deployment) * 3);
            getBone("barrels")
                    .setRotationZ(
                            age * (e.attack() == SeerObserverPattern.MACHINEGUN ? .7F : .12F));
            getBone("barrels")
                    .setPositionZ(
                            e.attack() == SeerObserverPattern.MACHINEGUN && t > 36
                                    ? .6F * Math.max(0, (float) Math.cos(t * Math.PI / 3))
                                    : 0);
            if (e.transformTick() > 0)
                getBone("eye").setRotationZ((float) Math.sin(e.transformTick() * .3) * .035F);
            if (e.attack() == SeerObserverPattern.SHARD_WHEEL) {
                getBone("eye")
                        .setRotationZ(
                                e.seer()
                                        ? t * .035F
                                        : -(float) SeerObserverPattern.wheelAngle((int) t));
                if (e.seer() && !e.upgraded())
                    getBone("blade").setRotationY(SeerObserverPattern.bladeWheel(t));
            }
            if (e.attack() == SeerObserverPattern.DOUBLE_PENDULUM)
                getBone("eye").setRotationZ((float) Math.sin(t * .093) * 1.4F);
            if (e.attack() == SeerObserverPattern.CRASH_FLAIL) {
                getBone("eye").setRotationZ(e.seer() && t >= 42 && t < 112 ? t * .14F : 0);
                if (!e.seer() && t >= 156 && t < 190)
                    getBone("eye").setRotationX((190 - t) / 34 * 1.2F);
            }
            if (e.attack() == SeerObserverPattern.ZIGZAG && !e.seer() && t >= 248 && t < 304)
                getBone("eye").setRotationZ(t * .18F);
            getBone("blade").setPositionZ(0);
            if (e.attack() >= 16) {
                getBone("blade").setRotationY(WulfrumDuetScore.bladeTilt(e.attack(), t));
                getBone("eye").setRotationZ(WulfrumDuetScore.spin(e.attack(), t));
                if (!e.seer()) getBone("barrels").setRotationZ(age * .7F);
            }
            if (e.attack() >= 35) {
                boolean saw = WulfrumSurvivorScore.saw(e.attack());
                if (e.seer()) {
                    getBone("blade").setHidden(saw);
                    getBone("blade").setScaleZ(1.2F);
                    getBone("sawRotor").setHidden(!saw);
                }
                if (e.attack() == 36) getBone("sawRotor").setRotationZ(age * .6F);
                if (e.attack() == 36)
                    getBone("eye")
                            .setRotationZ(
                                    t < 225
                                            ? t * .08F
                                            : 18 + (float) Math.sin((t - 225) * .18) * .25F);
                if (e.attack() == 43 && t >= 25 && t < 45)
                    getBone("eye")
                            .setRotationZ(
                                    (float)
                                            (Math.PI
                                                    * 2
                                                    * WulfrumSurvivorScore.ease((t - 25) / 20D)));
                if (e.attack() == 35 && t >= 25 && t < 60)
                    getBone("blade")
                            .setScaleZ(
                                    1.2F
                                            + .4F
                                                    * (float)
                                                            Math.sin(
                                                                    Math.PI
                                                                            * Math.max(
                                                                                    0,
                                                                                    Math.min(
                                                                                            1,
                                                                                            (t - 25)
                                                                                                    / 35F))));
                if (e.attack() == 38 && t >= 212 && t < 228)
                    getBone("blade")
                            .setRotationY((float) Math.sin((t - 212) * Math.PI / 16) * .75F);
                if (e.attack() == 43 && t >= 225 && t < 240)
                    getBone("blade").setRotationZ((t - 225) * .55F);
                if (e.attack() == 54 && t >= 215 && t < 235)
                    getBone("eye").setRotationZ((t - 215) * (float) Math.PI / 10);
                if ((e.attack() == 44 || e.attack() == 54) && t > 270) {
                    getBone("eye").setRotationZ((float) Math.sin(t * 1.2) * .09F);
                    getBone("barrels").setRotationZ(age * .06F);
                    getBone("sawRotor").setRotationZ(age * .02F);
                }
            }
            if (e.attack() == WulfrumDuetScore.FAILURE) {
                getBone("sawRotor").setRotationZ(age * (t < 235 ? 1.4F : .2F));
                if (t < 235 && t % 50 < 10)
                    getBone("barrels").setRotationZ((float) Math.floor(age * .15) * .2F);
                getBone("eye").setRotationX((float) Math.sin(t * 1.4) * (t < 235 ? .08F : .02F));
            }
            if (!e.coordinating() && e.transformTick() == 0 && e.introTick() == 0) {
                WulfrumWeaponPose accents = WulfrumWeaponPose.sample(e.attack(), t, e.seer());
                float bank =
                        net.minecraft.util.math.MathHelper.clamp(
                                net.minecraft.util.math.MathHelper.wrapDegrees(
                                                e.rotationYaw - e.prevRotationYaw)
                                        * .008F,
                                -.22F,
                                .22F);
                getBone("blade").setRotationX(accents.pitch);
                getBone("blade").setPositionZ(accents.slide);
                getBone("leftFin").setRotationZ(-.35F - accents.fold * .4F - bank);
                getBone("rightFin").setRotationZ(.35F + accents.fold * .4F - bank);
                getBone("lowerFin").setRotationX(.2F + accents.fold * .3F);
                getBone("iris").setScaleY(1 - accents.squint);
                if (!e.seer()) {
                    getBone("leftEmitter").setRotationY(open + accents.recoil * .16F);
                    getBone("rightEmitter").setRotationY(-open - accents.recoil * .16F);
                    getBone("upperEmitter").setRotationX(open + accents.recoil * .12F);
                    getBone("lowerEmitter").setRotationX(-open - accents.recoil * .12F);
                }
            } else getBone("blade").setRotationX(0);
            if (e.coordinating()) {
                float bank =
                        net.minecraft.util.math.MathHelper.clamp(
                                net.minecraft.util.math.MathHelper.wrapDegrees(
                                                e.rotationYaw - e.prevRotationYaw)
                                        * .012F,
                                -.3F,
                                .3F);
                getBone("leftFin").setRotationZ(-.35F - bank);
                getBone("rightFin").setRotationZ(.35F - bank);
                getBone("lowerFin").setRotationX(.2F + Math.abs(bank));
                if (e.seer() && e.attack() == SeerObserverPattern.FENCING) {
                    double q = t % 72,
                            thrust = WulfrumPairPose.hit(q, 25),
                            cut = WulfrumPairPose.hit(q, 48);
                    getBone("blade").setPositionZ((float) (-1.7 * thrust));
                    getBone("blade")
                            .setRotationY(
                                    (float) (-.45 * thrust + 1.1 * Math.sin((q - 35) * .12) * cut));
                    getBone("blade").setRotationZ((float) (.35 * cut));
                    getBone("eye").setRotationZ(bank + (float) (-.2 * thrust + .3 * cut));
                }
                if (!e.seer()) {
                    float recoil = (float) ExcavatorActuation.recoil(t % 12);
                    getBone("leftEmitter").setRotationY(open + recoil * .16F);
                    getBone("rightEmitter").setRotationY(-open - recoil * .16F);
                    getBone("upperEmitter").setRotationX(open + recoil * .12F);
                    getBone("lowerEmitter").setRotationX(-open - recoil * .12F);
                }
            }
            if (e.introTick() > 0) {
                float intro = e.introTick() + event.getPartialTick(),
                        weapon = SeerObserverIntro.weapon(intro),
                        fold = 1 - weapon;
                getBone("eye")
                        .setRotationZ(
                                (e.seer() ? 1 : -1)
                                        * (float) (1 - SeerObserverIntro.smooth((intro - 30) / 74))
                                        * .85F);
                getBone("blade").setHidden(!e.seer() || weapon <= .01F);
                getBone("blade").setScaleZ(Math.max(.01F, weapon));
                getBone("blade").setPositionZ(fold * 3);
                float spread = .18F + fold * .72F;
                getBone("leftEmitter").setRotationY(spread);
                getBone("rightEmitter").setRotationY(-spread);
                getBone("upperEmitter").setRotationX(spread);
                getBone("lowerEmitter").setRotationX(-spread);
            }
        }

        private static float pulse(float t, float strike) {
            float d = (t - strike) / 8;
            return d < -1 || d > 1 ? 0 : (float) Math.cos(d * Math.PI / 2);
        }
    }
}
