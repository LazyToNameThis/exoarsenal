package com.exoarsenal.client.render;

import com.exoarsenal.entity.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;
import com.exoarsenal.expedition.client.ExpeditionMesh;
import com.exoarsenal.expedition.client.WulfrumSocketModels;

public final class WulfrumEchoRenderer extends Render<EntityWulfrumEcho> {
    private final WulfrumEyeRenderer model;
    private EntityWulfrumEye.Seer phantom;

    public WulfrumEchoRenderer(RenderManager m) {
        super(m);
        model = new WulfrumEyeRenderer(m);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityWulfrumEcho e) {
        return null;
    }

    @Override
    public void doRender(
            EntityWulfrumEcho e, double x, double y, double z, float yaw, float partial) {
        EntityWulfrumEye source = e.source();
        if (e.phantom()) {
            if (phantom == null || phantom.world != e.world)
                phantom = new EntityWulfrumEye.Seer(e.world);
            source = phantom;
            phantom.ticksExisted = e.ticksExisted;
        }
        if (source != null && e.ready()) {
            model.renderEcho(source, e, x, y, z, partial);
            if (!e.phantom() && !source.seer()) cannon(e, x, y, z);
        }
    }

    private void cannon(EntityWulfrumEcho e, double x, double y, double z) {
        Vec3d forward = e.getLookVec(), side = forward.crossProduct(new Vec3d(0, 1, 0));
        if (side.lengthSquared() < .001) side = new Vec3d(1, 0, 0);
        side = side.normalize();
        Vec3d up = side.crossProduct(forward).normalize();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + 2.9, z);
        WulfrumRayRenderer.begin();
        GlStateManager.depthMask(false);
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(7, DefaultVertexFormats.POSITION_COLOR);
        for (ExpeditionMesh.Face face : WulfrumSocketModels.CANNON.faces)
            for (double[] v : face.p) {
                Vec3d p =
                        forward.scale(v[0] / 16)
                                .add(up.scale(v[1] / 16))
                                .add(side.scale(v[2] / 16));
                int c = face.color;
                b.pos(p.x, p.y, p.z)
                        .color((c >> 16 & 255) / 255F, (c >> 8 & 255) / 255F, (c & 255) / 255F, .3F)
                        .endVertex();
            }
        Tessellator.getInstance().draw();
        GlStateManager.depthMask(true);
        WulfrumRayRenderer.finish();
        GlStateManager.popMatrix();
    }
}
