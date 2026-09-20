package com.scapeandrun.frostbite.client.render;

import com.scapeandrun.frostbite.entity.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import com.scapeandrun.frostbite.expedition.client.ExpeditionMesh;
import com.scapeandrun.frostbite.expedition.client.WulfrumSocketModels;

public final class WulfrumAppendageRenderer extends Render<EntityWulfrumAppendage> {
    public WulfrumAppendageRenderer(RenderManager m) {
        super(m);
    }

    @Override
    public void doRender(
            EntityWulfrumAppendage e, double x, double y, double z, float yaw, float partial) {
        EntityWulfrumEye eye = e.owner();
        if (eye == null) return;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        WulfrumRayRenderer.begin();
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(7, DefaultVertexFormats.POSITION_COLOR);
        Vec3d p =
                new Vec3d(
                        e.lastTickPosX + (e.posX - e.lastTickPosX) * partial,
                        e.lastTickPosY + (e.posY - e.lastTickPosY) * partial,
                        e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * partial);
        net.minecraft.entity.Entity tether = e.tether();
        Vec3d
                base =
                        new Vec3d(
                                        tether.lastTickPosX
                                                + (tether.posX - tether.lastTickPosX) * partial,
                                        tether.lastTickPosY
                                                + (tether.posY - tether.lastTickPosY) * partial
                                                + (tether instanceof EntityWulfrumEye ? 1.5 : 0),
                                        tether.lastTickPosZ
                                                + (tether.posZ - tether.lastTickPosZ) * partial)
                                .subtract(p),
                last = base;
        int color =
                java.awt.Color.HSBtoRGB((eye.ticksExisted + partial) * .003F % 1, .5F, 1)
                        & 0xFFFFFF;
        if (eye.seer() && !e.ghost() && eye.attack() != 38)
            for (int i = 1; i <= 24; i++) {
                double f = i / 24D;
                double slack =
                        eye.attack() == 37 || eye.attack() == 39
                                ? .12
                                : Math.min(2, base.lengthVector() * .12);
                Vec3d q = base.scale(1 - f).addVector(0, Math.sin(f * Math.PI) * slack, 0);
                if (eye.attack() == 39 && eye.attackTick() < 55) {
                    double fold = 1 - WulfrumSurvivorScore.ease((eye.attackTick() - 45) / 10D),
                            a = f * Math.PI * 2 + e.getEntityId() * Math.PI / 3;
                    q =
                            q.addVector(
                                    Math.sin(f * Math.PI) * Math.cos(a) * 1.8 * fold,
                                    Math.sin(f * Math.PI) * Math.sin(a) * 1.8 * fold,
                                    Math.sin(f * Math.PI) * 1.2 * fold);
                }
                WulfrumRayRenderer.tube(b, last, q, .13, 0x536256, 1);
                WulfrumRayRenderer.tube(b, last, q, .045, color, 1);
                last = q;
            }
        Vec3d d = e.getLookVec(), side = d.crossProduct(new Vec3d(0, 1, 0));
        if (side.lengthSquared() < .001) side = new Vec3d(1, 0, 0);
        side = side.normalize();
        Vec3d up = side.crossProduct(d).normalize();
        if (eye.attack() == 40) {
            double roll = (e.ticksExisted + partial) * .4;
            Vec3d old = up;
            up = up.scale(Math.cos(roll)).add(side.scale(Math.sin(roll)));
            side = side.scale(Math.cos(roll)).subtract(old.scale(Math.sin(roll)));
        }
        ExpeditionMesh mesh = eye.seer() ? WulfrumSocketModels.BLADE : WulfrumSocketModels.CANNON;
        double recoil =
                e.firing() ? -.12 * Math.abs(Math.sin((e.ticksExisted + partial) * 1.4)) : 0;
        for (ExpeditionMesh.Face face : mesh.faces) {
            boolean energy = face.color == 0x53DDA0 || face.color == 0xDCFFE7;
            int c = energy ? (e.firing() || e.hot() ? 0xE5FFF0 : color) : face.color;
            float shade = energy ? 1 : .76F + .16F * face.ny - .08F * face.nz;
            for (double[] v : face.p) {
                Vec3d q =
                        d.scale(v[0] / 16 + recoil)
                                .add(up.scale(v[1] / 16))
                                .add(side.scale(v[2] / 16));
                b.pos(q.x, q.y, q.z)
                        .color(
                                (c >> 16 & 255) / 255F * shade,
                                (c >> 8 & 255) / 255F * shade,
                                (c & 255) / 255F * shade,
                                e.ghost() ? .3F : 1)
                        .endVertex();
            }
        }
        if (!eye.seer() && (e.chargeTicks() > 0 || e.firing())) {
            Vec3d muzzle = d.scale(.93);
            WulfrumRayRenderer.tube(
                    b,
                    muzzle,
                    muzzle.add(d.scale(e.firing() ? .5 : .08)),
                    e.firing() ? .23 : .08,
                    0xD7FFE9,
                    .7F);
        }
        if (!eye.seer() && eye.attack() == 51) {
            Vec3d[] corners = new Vec3d[4];
            for (int i = 0; i < 4; i++)
                corners[i] =
                        up.scale(i < 2 ? 1.2 : -1.2).add(side.scale(i == 0 || i == 3 ? -1.2 : 1.2));
            for (int i = 0; i < 4; i++)
                WulfrumRayRenderer.tube(b, corners[i], corners[(i + 1) % 4], .04, 0xA2FFC8, .7F);
            for (Vec3d q : corners) b.pos(q.x, q.y, q.z).color(.35F, .9F, .65F, .16F).endVertex();
        }
        Tessellator.getInstance().draw();
        WulfrumRayRenderer.finish();
        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityWulfrumAppendage e) {
        return null;
    }
}
