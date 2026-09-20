package com.exoarsenal.client.render;

import com.exoarsenal.entity.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public final class ScoutCoreRayRenderer extends Render<EntityScoutCoreRay> {
    public ScoutCoreRayRenderer(RenderManager m) {
        super(m);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityScoutCoreRay e) {
        return null;
    }

    @Override
    public void doRender(
            EntityScoutCoreRay e, double x, double y, double z, float yaw, float partial) {
        float age = e.age() + partial, charge = ScoutCoreMotion.charge(age);
        Vec3d forward = e.direction();
        Vec3d right = forward.crossProduct(new Vec3d(0, 1, 0));
        if (right.lengthSquared() < .001) right = new Vec3d(1, 0, 0);
        right = right.normalize();
        Vec3d up = right.crossProduct(forward).normalize();
        Vec3d focus = forward.scale(Math.min(4, e.length()));
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GlStateManager.depthMask(false);
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (int orb = 0; orb < 6; orb++) {
            double angle = ScoutCoreMotion.angle(orb, age), radius = ScoutCoreMotion.orbit(age);
            Vec3d at =
                    forward.scale(2.2)
                            .add(right.scale(Math.cos(angle) * radius))
                            .add(up.scale(Math.sin(angle) * radius));
            sphere(b, at, .25, .65F);
            sphere(b, at, .31, .12F);
            tube(b, at, focus, .018 + .09 * charge, .28F + .25F * charge);
        }
        if (age >= ScoutCoreMotion.CHARGE && e.length() > 4) {
            double radius = ScoutCoreMotion.beamRadius(age);
            Vec3d end = forward.scale(e.length());
            tube(b, focus, end, radius, .48F);
            tube(b, focus, end, radius * 1.28, .12F);
            double length = e.length() - 4;
            int steps = Math.min(64, (int) Math.ceil(length));
            for (int helix = 0; helix < 2; helix++)
                for (int i = 0; i < steps; i++) {
                    double a = age * .13 + i * .5 + helix * Math.PI,
                            next = age * .13 + (i + 1) * .5 + helix * Math.PI;
                    Vec3d p =
                            focus.add(forward.scale(length * i / steps))
                                    .add(right.scale(Math.cos(a) * radius * 1.45))
                                    .add(up.scale(Math.sin(a) * radius * 1.45));
                    Vec3d q =
                            focus.add(forward.scale(length * (i + 1) / steps))
                                    .add(right.scale(Math.cos(next) * radius * 1.45))
                                    .add(up.scale(Math.sin(next) * radius * 1.45));
                    tube(b, p, q, .045 + .03 * ScoutCoreMotion.power(age), .7F);
                }
        }
        Tessellator.getInstance().draw();
        GlStateManager.depthMask(true);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    private static void sphere(BufferBuilder b, Vec3d center, double radius, float alpha) {
        for (int band = 0; band < 6; band++)
            for (int slice = 0; slice < 12; slice++) {
                double low = -Math.PI / 2 + band * Math.PI / 6,
                        high = low + Math.PI / 6,
                        a = slice * Math.PI / 6,
                        c = a + Math.PI / 6;
                spherical(b, center, radius, low, a, alpha);
                spherical(b, center, radius, low, c, alpha);
                spherical(b, center, radius, high, c, alpha);
                spherical(b, center, radius, high, a, alpha);
            }
    }

    private static void spherical(
            BufferBuilder b, Vec3d p, double r, double latitude, double angle, float alpha) {
        b.pos(
                        p.x + r * Math.cos(latitude) * Math.cos(angle),
                        p.y + r * Math.sin(latitude),
                        p.z + r * Math.cos(latitude) * Math.sin(angle))
                .color(.36F, .82F, 1F, alpha)
                .endVertex();
    }

    private static void tube(BufferBuilder b, Vec3d from, Vec3d to, double radius, float alpha) {
        Vec3d d = to.subtract(from).normalize(), u = d.crossProduct(new Vec3d(0, 1, 0));
        if (u.lengthSquared() < .001) u = new Vec3d(1, 0, 0);
        u = u.normalize();
        Vec3d v = u.crossProduct(d).normalize();
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4, c = a + Math.PI / 4;
            Vec3d p = u.scale(Math.cos(a) * radius).add(v.scale(Math.sin(a) * radius)),
                    q = u.scale(Math.cos(c) * radius).add(v.scale(Math.sin(c) * radius));
            vertex(b, from.add(p), alpha);
            vertex(b, to.add(p), alpha);
            vertex(b, to.add(q), alpha);
            vertex(b, from.add(q), alpha);
        }
    }

    private static void vertex(BufferBuilder b, Vec3d p, float a) {
        b.pos(p.x, p.y, p.z).color(.63F, .9F, 1F, a).endVertex();
    }
}
