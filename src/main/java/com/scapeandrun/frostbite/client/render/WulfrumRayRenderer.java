package com.scapeandrun.frostbite.client.render;

import com.scapeandrun.frostbite.entity.EntityWulfrumRay;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public final class WulfrumRayRenderer extends Render<EntityWulfrumRay> {
    public WulfrumRayRenderer(RenderManager m) {
        super(m);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityWulfrumRay e) {
        return null;
    }

    @Override
    public void doRender(
            EntityWulfrumRay e, double x, double y, double z, float yaw, float partial) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        begin();
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        Vec3d end = e.end().subtract(e.getPositionVector());
        double radius = e.firing() ? e.radius() : .015 + .015 * e.charge();
        Vec3d[] path = e.path();
        for (int i = 1; i < path.length; i++) {
            Vec3d a = path[i - 1].subtract(e.getPositionVector()),
                    c = path[i].subtract(e.getPositionVector());
            tube(b, a, c, radius, e.firing() ? 0xB8FFD1 : 0x53D788, e.firing() ? .95F : .45F);
            if (e.firing()) tube(b, a, c, radius * 2, 0x24F886, .18F);
        }
        if (!e.firing()) {
            Vec3d axis = end.normalize(), u = axis.crossProduct(new Vec3d(0, 1, 0));
            if (u.lengthSquared() < .01) u = new Vec3d(1, 0, 0);
            u = u.normalize();
            Vec3d v = u.crossProduct(axis);
            double r = .3 + (1 - e.charge()) * .9;
            for (int i = 0; i < 24; i++) {
                double a = i * Math.PI / 12, c = (i + 1) * Math.PI / 12;
                tube(
                        b,
                        end.add(u.scale(Math.cos(a) * r)).add(v.scale(Math.sin(a) * r)),
                        end.add(u.scale(Math.cos(c) * r)).add(v.scale(Math.sin(c) * r)),
                        .022,
                        0xFFE69A,
                        .8F);
            }
            tube(b, end.subtract(u.scale(.22)), end.add(u.scale(.22)), .025, 0xFFE9B5, .9F);
            tube(b, end.subtract(v.scale(.22)), end.add(v.scale(.22)), .025, 0xFFE9B5, .9F);
        }
        if (e.firing() && e.helix()) {
            Vec3d d = end.normalize(), u = d.crossProduct(new Vec3d(0, 1, 0));
            if (u.lengthSquared() < .001) u = new Vec3d(1, 0, 0);
            u = u.normalize();
            Vec3d v = u.crossProduct(d).normalize();
            for (int strand = 0; strand < 6; strand++) {
                Vec3d old = null;
                for (int i = 0; i <= 48; i++) {
                    double f = i / 48D,
                            a =
                                    f * Math.PI * 8
                                            + strand * Math.PI / 3
                                            + (e.ticksExisted + partial) * .16;
                    Vec3d p =
                            end.scale(f)
                                    .add(u.scale(Math.cos(a) * radius * 1.8))
                                    .add(v.scale(Math.sin(a) * radius * 1.8));
                    if (old != null) tube(b, old, p, .08, 0xDEFFEB, .75F);
                    old = p;
                }
            }
        }
        Tessellator.getInstance().draw();
        finish();
        GlStateManager.popMatrix();
    }

    public static void begin() {
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    public static void finish() {
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
    }

    public static void tube(
            BufferBuilder b, Vec3d from, Vec3d to, double radius, int color, float alpha) {
        Vec3d d = to.subtract(from).normalize(), u = d.crossProduct(new Vec3d(0, 1, 0));
        if (u.lengthSquared() < .001) u = new Vec3d(1, 0, 0);
        u = u.normalize();
        Vec3d v = u.crossProduct(d).normalize();
        for (int i = 0; i < 4; i++) {
            double a = Math.PI / 4 + i * Math.PI / 2, c = a + Math.PI / 2;
            Vec3d p = u.scale(Math.cos(a) * radius).add(v.scale(Math.sin(a) * radius)),
                    q = u.scale(Math.cos(c) * radius).add(v.scale(Math.sin(c) * radius));
            vertex(b, from.add(p), color, alpha);
            vertex(b, to.add(p), color, alpha);
            vertex(b, to.add(q), color, alpha);
            vertex(b, from.add(q), color, alpha);
        }
    }

    private static void vertex(BufferBuilder b, Vec3d p, int c, float a) {
        b.pos(p.x, p.y, p.z)
                .color((c >> 16 & 255) / 255F, (c >> 8 & 255) / 255F, (c & 255) / 255F, a)
                .endVertex();
    }
}
