package com.exoarsenal.client.render;

import com.exoarsenal.entity.EntityWulfrumCut;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

public final class WulfrumCutRenderer extends Render<EntityWulfrumCut> {
    public WulfrumCutRenderer(RenderManager manager) {
        super(manager);
    }

    @Override
    public void doRender(
            EntityWulfrumCut e, double x, double y, double z, float yaw, float partial) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        if (e.plane() == 1) GlStateManager.rotate(90, 1, 0, 0);
        if (e.plane() == 2) GlStateManager.rotate(90, 0, 0, 1);
        WulfrumRayRenderer.begin();
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(7, DefaultVertexFormats.POSITION_COLOR);
        double rotation = (e.ticksExisted + partial) * .15, r = e.radius();
        float alpha = Math.max(0, e.fade()) * (e.active() ? .8F : .28F);
        if (e.spherical())
            for (int petal = 0; petal < 6; petal++)
                for (int segment = 0; segment < 32; segment++) {
                    double a = segment * Math.PI / 16,
                            c = (segment + 1) * Math.PI / 16,
                            azimuth = petal * Math.PI / 6;
                    Vec3d
                            p =
                                    new Vec3d(
                                            Math.cos(a) * Math.cos(azimuth) * r,
                                            Math.sin(a) * r,
                                            Math.cos(a) * Math.sin(azimuth) * r),
                            q =
                                    new Vec3d(
                                            Math.cos(c) * Math.cos(azimuth) * r,
                                            Math.sin(c) * r,
                                            Math.cos(c) * Math.sin(azimuth) * r);
                    WulfrumRayRenderer.tube(b, p, q, e.active() ? .09 : .025, 0xCBFFE1, alpha);
                }
        for (int i = 0; i < 48; i++) {
            double a = i * Math.PI / 24 + rotation, c = (i + 1) * Math.PI / 24 + rotation;
            Vec3d p = new Vec3d(Math.cos(a) * r, 0, Math.sin(a) * r),
                    q = new Vec3d(Math.cos(c) * r, 0, Math.sin(c) * r);
            WulfrumRayRenderer.tube(b, p, q, e.active() ? .065 : .025, 0xA7FFCF, alpha);
            if (e.saw() && i % 2 == 0) {
                Vec3d inner = p.scale(.65), tooth = q.scale(1.12);
                WulfrumRayRenderer.tube(b, inner, tooth, .09, 0x6DAB7C, alpha);
                WulfrumRayRenderer.tube(b, tooth, q, .045, 0xD8FFE1, alpha);
            }
        }
        Tessellator.getInstance().draw();
        WulfrumRayRenderer.finish();
        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityWulfrumCut e) {
        return null;
    }
}
