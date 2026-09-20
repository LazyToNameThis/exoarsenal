package com.exoarsenal.client.render;

import com.exoarsenal.entity.EntityExcavatorPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.*;
import org.lwjgl.opengl.GL11;

public final class ExcavatorPayloadRenderer extends Render<EntityExcavatorPayload> {
    public ExcavatorPayloadRenderer(RenderManager m) {
        super(m);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityExcavatorPayload e) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }

    @Override
    public void doRender(
            EntityExcavatorPayload e, double x, double y, double z, float yaw, float partial) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        if (e.kind() == 2 || e.kind() == 4) {
            bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            int radius = e.kind() == 2 ? (int) e.size() : 0;
            double scale = e.kind() == 4 ? .45 : 1;
            GlStateManager.rotate(e.tilt(), 1, 0, 0);
            GlStateManager.scale(scale, scale * 1.2, scale);
            for (int i = -radius; i < Math.max(1, radius); i++)
                for (int j = -radius; j < Math.max(1, radius); j++) {
                    if (e.kind() == 2 && !e.hasTile(i, j)) continue;
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(i, 0, j);
                    Minecraft.getMinecraft()
                            .getBlockRendererDispatcher()
                            .renderBlockBrightness(e.block(i, j), 1);
                    GlStateManager.popMatrix();
                }
            GlStateManager.popMatrix();
            return;
        }
        WulfrumRayRenderer.begin();
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        Vec3d end = e.end().subtract(e.getPositionVector());
        if (e.kind() == 0) {
            RayTraceResult wall =
                    e.world.rayTraceBlocks(e.getPositionVector(), e.end(), false, true, false);
            if (wall != null) end = wall.hitVec.subtract(e.getPositionVector());
            if (e.active()) {
                WulfrumRayRenderer.tube(b, Vec3d.ZERO, end, e.size() * 2.4, 0x25D985, .12F);
                WulfrumRayRenderer.tube(b, Vec3d.ZERO, end, e.size(), 0x69FFAF, .55F);
                WulfrumRayRenderer.tube(b, Vec3d.ZERO, end, e.size() * .32, 0xF0FFF1, 1F);
            } else {
                WulfrumRayRenderer.tube(b, Vec3d.ZERO, end, .025, 0x9AFFBB, .8F);
                double radius =
                        .3 + .5 * Math.max(0, 1 - (e.age() + partial) / Math.max(1, e.warning()));
                for (int i = 0; i < 12; i++) {
                    double a = i * Math.PI / 6, c = (i + 1) * Math.PI / 6;
                    WulfrumRayRenderer.tube(
                            b,
                            end.addVector(Math.cos(a) * radius, .05, Math.sin(a) * radius),
                            end.addVector(Math.cos(c) * radius, .05, Math.sin(c) * radius),
                            .035,
                            0xAAFFD0,
                            .9F);
                }
            }
        }
        if (e.kind() == EntityExcavatorPayload.SURVEY_LINE) {
            WulfrumRayRenderer.tube(
                    b, new Vec3d(0, .1, 0), end.addVector(0, .1, 0), .035, 0x77E9B1, .65F);
            int marks = Math.max(1, (int) (end.lengthVector() / 3));
            for (int i = 0; i <= marks; i++) {
                Vec3d p = end.scale(i / (double) marks).addVector(0, .1, 0);
                WulfrumRayRenderer.tube(
                        b,
                        p.addVector(-.25, 0, -.25),
                        p.addVector(.25, 0, .25),
                        .03,
                        0xBCFFDA,
                        .7F);
            }
        }
        if (e.kind() == EntityExcavatorPayload.TETHER) {
            for (int i = 0; i < 12; i++) {
                Vec3d a = e.tetherPoint(i / 12D).subtract(e.getPositionVector()),
                        c = e.tetherPoint((i + 1) / 12D).subtract(e.getPositionVector());
                WulfrumRayRenderer.tube(b, a, c, e.active() ? .09 : .025, 0x81FFC1, .85F);
                WulfrumRayRenderer.tube(b, a, c, .2, 0x2ED782, .16F);
            }
        }
        if (e.kind() == EntityExcavatorPayload.LASER_WALL) {
            int nodes = Math.min(24, Math.max(2, (int) (end.lengthVector() / 2)));
            for (int i = 0; i < nodes; i++) {
                Vec3d p = end.scale(i / (double) nodes).addVector(0, .06, 0),
                        q = end.scale((i + .65) / nodes).addVector(0, .06, 0);
                WulfrumRayRenderer.tube(b, p, q, .035, e.active() ? 0xB9FFD7 : 0xE4C788, .85F);
                if (e.active()) discharge(b, p, e.size(), e.age() + partial, i);
                else {
                    WulfrumRayRenderer.tube(
                            b,
                            p.addVector(-.25, 0, -.25),
                            p.addVector(.25, 0, .25),
                            .025,
                            0xE4C788,
                            .7F);
                }
            }
        }
        if (e.kind() == 1) {
            double r = Math.max(.1, (e.age() + partial - e.warning()) * e.size());
            for (int i = 0; i < 6; i++) {
                Vec3d a = EntityExcavatorPayload.ringPoint(r, i).addVector(0, .15, 0),
                        c = EntityExcavatorPayload.ringPoint(r, i + 1).addVector(0, .15, 0);
                WulfrumRayRenderer.tube(b, a, c, .42, 0x20BF78, .16F);
                WulfrumRayRenderer.tube(b, a, c, .12, 0xC4FFD8, .95F);
                for (int j = 0; j < 4; j++) {
                    Vec3d p = a.add(c.subtract(a).scale(j / 4D));
                    double h = .4 + .7 * (.5 + .5 * Math.sin(i * 3 + j * 2 - e.age() * .7));
                    WulfrumRayRenderer.tube(b, p, p.addVector(0, h, 0), .065, 0x68FFAD, .75F);
                }
                if (r > 1)
                    WulfrumRayRenderer.tube(
                            b,
                            EntityExcavatorPayload.ringPoint(r - .7, i).addVector(0, .08, 0),
                            EntityExcavatorPayload.ringPoint(r - .7, i + 1).addVector(0, .08, 0),
                            .06,
                            0x49C896,
                            .4F);
            }
        }
        if (e.kind() == 3) {
            int count = Math.min(40, Math.max(2, (int) end.lengthVector()));
            Vec3d old = new Vec3d(0, .08, 0);
            for (int i = 1; i <= count; i++) {
                double f = i / (double) count;
                Vec3d p = end.scale(f).addVector(i == count ? 0 : Math.sin(i * 4.1) * .3, .08, 0);
                WulfrumRayRenderer.tube(
                        b, old, p, e.active() ? .05 : .025, e.active() ? 0xD8FFE6 : 0xCDB67E, .8F);
                if (e.active() && i % 2 == 0) discharge(b, p, 6, e.age() + partial, i);
                old = p;
            }
        }
        Tessellator.getInstance().draw();
        WulfrumRayRenderer.finish();
        GlStateManager.popMatrix();
    }

    private static void discharge(BufferBuilder b, Vec3d base, double height, float age, int seed) {
        Vec3d previous = base;
        double phase = age * .75 + seed * 2.4;
        for (int j = 1; j <= 7; j++) {
            double f = j / 7D, spread = Math.sin(f * Math.PI) * .42;
            Vec3d point =
                    base.addVector(
                            Math.sin(phase + j * 2.7) * spread,
                            height * f,
                            Math.cos(phase * .8 + j * 3.1) * spread);
            WulfrumRayRenderer.tube(b, previous, point, .07, 0x3DB888, .12F);
            WulfrumRayRenderer.tube(b, previous, point, .018, 0xDCFFEC, .9F);
            if (j == 3 || j == 5)
                WulfrumRayRenderer.tube(
                        b,
                        point,
                        point.addVector(Math.sin(seed + j) * .65, .35, Math.cos(seed - j) * .65),
                        .012,
                        0x94E7BC,
                        .5F);
            previous = point;
        }
    }
}
