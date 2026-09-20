package com.scapeandrun.frostbite.client.render;

import com.scapeandrun.frostbite.entity.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

public final class WulfrumNovaRenderer extends Render<EntityWulfrumNova> {
    public WulfrumNovaRenderer(RenderManager m) {
        super(m);
    }

    @Override
    public void doRender(
            EntityWulfrumNova e, double x, double y, double z, float yaw, float partial) {
        double age = e.ticksExisted + partial, r = WulfrumNova.radius(age);
        float alpha = WulfrumNova.alpha(age);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        WulfrumRayRenderer.begin();
        GlStateManager.depthMask(false);
        GlStateManager.blendFunc(770, 1);
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(7, DefaultVertexFormats.POSITION_COLOR);

        double burst = Math.max(0, age - WulfrumNova.BURST);
        for (int i = 0; i < 24; i++) {
            double az = i * 2.399963,
                    vertical = 1 - 2 * (i + .5) / 24,
                    radial = Math.sqrt(1 - vertical * vertical);
            Vec3d axis = new Vec3d(Math.cos(az) * radial, vertical, Math.sin(az) * radial);
            if (age < WulfrumNova.BURST) {
                Vec3d previous = null;
                for (int j = 0; j <= 12; j++) {
                    double f = j / 12D, d = (1 - f) * 5 + f * r, twist = az + f * 5 - age * .15;
                    Vec3d p =
                            axis.scale(d)
                                    .addVector(
                                            Math.cos(twist) * (1 - f) * .45,
                                            0,
                                            Math.sin(twist) * (1 - f) * .45);
                    if (previous != null)
                        WulfrumRayRenderer.tube(b, previous, p, .028, 0x7BFFC4, alpha * (float) f);
                    previous = p;
                }
            } else {
                double speed = .72 + (i % 5) * .075,
                        rr = r * speed,
                        size =
                                (.3 + burst * .045)
                                        * (1 - .4 * burst / (WulfrumNova.END - WulfrumNova.BURST));
                Vec3d c = axis.scale(rr);
                WulfrumDetonation.puff(
                        b,
                        c,
                        size,
                        size * .65,
                        size,
                        i % 3 == 0 ? 0xD8FFE6 : 0x28B974,
                        alpha * .55F,
                        az + burst * .05);
                Vec3d tangent = axis.crossProduct(new Vec3d(0, 1, 0)).normalize();
                WulfrumRayRenderer.tube(
                        b,
                        c.subtract(axis.scale(1 + burst * .04)),
                        c.add(tangent.scale(Math.sin(burst * .12 + i) * .7)),
                        .045,
                        0xCBFFE7,
                        alpha);
            }
        }
        if (burst > 0 && burst < 20) {
            double shell = 1 + burst * .45;
            WulfrumDetonation.puff(
                    b,
                    Vec3d.ZERO,
                    shell,
                    shell,
                    shell,
                    0xBDFFD8,
                    (float) (.3 * (1 - burst / 20)),
                    burst * .08);
        }
        for (int ring = 0; ring < 3; ring++)
            for (int i = 0; i < 64; i++) {
                double a = i * Math.PI / 32, c = (i + 1) * Math.PI / 32, rr = r * (1 - ring * .08);
                Vec3d p = plane(ring, Math.cos(a) * rr, Math.sin(a) * rr),
                        q = plane(ring, Math.cos(c) * rr, Math.sin(c) * rr);
                WulfrumRayRenderer.tube(
                        b, p, q, .05 + alpha * .13, ring == 0 ? 0xD8FFE6 : 0x34EF83, alpha);
            }
        for (int i = 0; i < 48; i++) {
            double a = i * 2.399963, beta = Math.acos(1 - 2 * (i + .5) / 48);
            Vec3d d =
                    new Vec3d(
                            Math.cos(a) * Math.sin(beta),
                            Math.cos(beta),
                            Math.sin(a) * Math.sin(beta));
            double length = age < 20 ? r : Math.max(0, r * (.65 + .3 * Math.sin(i * 7)));
            WulfrumRayRenderer.tube(
                    b,
                    d.scale(length * .65),
                    d.scale(length),
                    age < 20 ? .045 : .025 + alpha * .07,
                    i % 3 == 0 ? 0xDAFFE9 : 0x39F395,
                    alpha);
        }
        double core = age < 20 ? r : Math.max(0, 3 - (age - 20) * .13);
        for (int lat = 0; lat < 12; lat++)
            for (int lon = 0; lon < 20; lon++)
                for (int corner = 0; corner < 4; corner++) {
                    double a = (lat + (corner >= 2 ? 1 : 0)) * Math.PI / 12 - Math.PI / 2,
                            c = (lon + (corner == 1 || corner == 2 ? 1 : 0)) * Math.PI / 10;
                    b.pos(
                                    Math.cos(a) * Math.cos(c) * core,
                                    Math.sin(a) * core,
                                    Math.cos(a) * Math.sin(c) * core)
                            .color(.65F, 1, .78F, alpha * .8F)
                            .endVertex();
                }
        Tessellator.getInstance().draw();
        GlStateManager.depthMask(true);
        GlStateManager.blendFunc(770, 771);
        WulfrumRayRenderer.finish();
        GlStateManager.popMatrix();
    }

    private static Vec3d plane(int i, double a, double b) {
        return i == 0
                ? new Vec3d(a, 0, b)
                : i == 1 ? new Vec3d(a, b * .5, b * .86) : new Vec3d(b * .5, a, b * .86);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityWulfrumNova e) {
        return null;
    }
}
