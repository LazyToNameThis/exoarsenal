package com.scapeandrun.frostbite.client.render;

import com.scapeandrun.frostbite.entity.EntityScoutCut;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public final class ScoutCutRenderer extends Render<EntityScoutCut> {
    public ScoutCutRenderer(RenderManager manager) {
        super(manager);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityScoutCut e) {
        return null;
    }

    @Override
    public void doRender(EntityScoutCut e, double x, double y, double z, float yaw, float partial) {
        if (e.wall() != 0) {
            renderWalls(e, x, y, z, partial);
            return;
        }
        float time = e.age() + partial - e.delay();
        boolean resolved = time >= 0;
        float alpha =
                resolved
                        ? Math.max(0, 1 - time / 12F)
                        : .55F + .2F * (float) Math.sin((e.age() + partial) * .4F);
        double height = resolved ? 6 : .035;
        Vec3d d = e.offset();
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
        b.pos(0, -height, 0).color(.12F, .5F, 1F, resolved ? 0 : alpha).endVertex();
        b.pos(d.x, d.y - height, d.z).color(.12F, .5F, 1F, resolved ? 0 : alpha).endVertex();
        b.pos(d.x, d.y, d.z).color(.65F, 1F, 1F, alpha).endVertex();
        b.pos(0, 0, 0).color(.65F, 1F, 1F, alpha).endVertex();
        b.pos(0, 0, 0).color(.65F, 1F, 1F, alpha).endVertex();
        b.pos(d.x, d.y, d.z).color(.65F, 1F, 1F, alpha).endVertex();
        b.pos(d.x, d.y + height, d.z).color(.12F, .5F, 1F, resolved ? 0 : alpha).endVertex();
        b.pos(0, height, 0).color(.12F, .5F, 1F, resolved ? 0 : alpha).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.depthMask(true);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    private void renderWalls(EntityScoutCut e, double x, double y, double z, float partial) {
        Vec3d n = e.wallNormal(), u = n.crossProduct(new Vec3d(0, 0, 1));
        if (u.lengthSquared() < .1) u = new Vec3d(1, 0, 0);
        u = u.normalize();
        Vec3d v = n.crossProduct(u).normalize();
        double gap = e.wallGap(partial),
                fade = Math.max(0, 1 - Math.max(0, e.age() + partial - e.delay()) / 12D);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        bindTexture(net.minecraft.client.renderer.texture.TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.enableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
        net.minecraft.client.renderer.texture.TextureAtlasSprite tex =
                net.minecraft.client.Minecraft.getMinecraft()
                        .getBlockRendererDispatcher()
                        .getBlockModelShapes()
                        .getTexture(net.minecraft.init.Blocks.PACKED_ICE.getDefaultState());
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        for (int sign : new int[] {-1, 1})
            for (int a = -4; a < 4; a++)
                for (int c = -4; c < 4; c++) {
                    if (Math.abs(a) + Math.abs(c) > 6) continue;
                    Vec3d base = n.scale(sign * gap);
                    Vec3d[] points = {
                        base.add(u.scale(a)).add(v.scale(c)),
                        base.add(u.scale(a + 1)).add(v.scale(c)),
                        base.add(u.scale(a + 1)).add(v.scale(c + 1)),
                        base.add(u.scale(a)).add(v.scale(c + 1))
                    };
                    for (int i = 0; i < 4; i++)
                        b.pos(points[i].x, points[i].y, points[i].z)
                                .tex(
                                        i == 0 || i == 3 ? tex.getMinU() : tex.getMaxU(),
                                        i < 2 ? tex.getMinV() : tex.getMaxV())
                                .color(.65F, .85F, 1F, (float) fade * .85F)
                                .endVertex();
                }
        Tessellator.getInstance().draw();
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}
