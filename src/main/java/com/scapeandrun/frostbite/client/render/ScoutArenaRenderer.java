package com.scapeandrun.frostbite.client.render;

import com.scapeandrun.frostbite.entity.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public final class ScoutArenaRenderer extends Render<EntityScoutArena> {
    private static final class Face {
        final double[][] p;
        final float shade;
        final boolean ice;
        final int cellX;

        Face(double[][] p, float shade, boolean ice, int cellX) {
            this.p = p;
            this.shade = shade;
            this.ice = ice;
            this.cellX = cellX;
        }
    }

    private static final java.util.List<Face> ISLAND = buildIsland();

    private static java.util.List<Face> buildIsland() {
        java.util.List<Face> faces = new java.util.ArrayList<>();
        for (int bx = -ScoutArenaLayout.RADIUS; bx <= ScoutArenaLayout.RADIUS; bx++)
            for (int bz = -ScoutArenaLayout.RADIUS; bz <= ScoutArenaLayout.RADIUS; bz++)
                if (ScoutArenaLayout.tile(bx, bz)) {
                    double x = bx - .5,
                            z = bz - .5,
                            d =
                                    2
                                            + (ScoutArenaLayout.RADIUS
                                                            - Math.max(Math.abs(bx), Math.abs(bz)))
                                                    * .22;
                    faces.add(
                            new Face(
                                    new double[][] {
                                        {x, 0, z}, {x + 1, 0, z}, {x + 1, 0, z + 1}, {x, 0, z + 1}
                                    },
                                    .82F,
                                    true,
                                    bx));
                    faces.add(
                            new Face(
                                    new double[][] {
                                        {x, -d, z + 1},
                                        {x + 1, -d, z + 1},
                                        {x + 1, -d, z},
                                        {x, -d, z}
                                    },
                                    .32F,
                                    false,
                                    bx));
                    if (!ScoutArenaLayout.tile(bx - 1, bz) || bx == 1)
                        faces.add(
                                new Face(
                                        new double[][] {
                                            {x, 0, z}, {x, 0, z + 1}, {x, -d, z + 1}, {x, -d, z}
                                        },
                                        .55F,
                                        true,
                                        bx));
                    if (!ScoutArenaLayout.tile(bx + 1, bz) || bx == -1)
                        faces.add(
                                new Face(
                                        new double[][] {
                                            {x + 1, 0, z + 1},
                                            {x + 1, 0, z},
                                            {x + 1, -d, z},
                                            {x + 1, -d, z + 1}
                                        },
                                        .68F,
                                        true,
                                        bx));
                    if (!ScoutArenaLayout.tile(bx, bz - 1))
                        faces.add(
                                new Face(
                                        new double[][] {
                                            {x + 1, 0, z}, {x, 0, z}, {x, -d, z}, {x + 1, -d, z}
                                        },
                                        .5F,
                                        false,
                                        bx));
                    if (!ScoutArenaLayout.tile(bx, bz + 1))
                        faces.add(
                                new Face(
                                        new double[][] {
                                            {x, 0, z + 1},
                                            {x + 1, 0, z + 1},
                                            {x + 1, -d, z + 1},
                                            {x, -d, z + 1}
                                        },
                                        .62F,
                                        true,
                                        bx));
                }
        return java.util.Collections.unmodifiableList(faces);
    }

    public ScoutArenaRenderer(RenderManager m) {
        super(m);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityScoutArena e) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }

    @Override
    public void doRender(
            EntityScoutArena e, double x, double y, double z, float yaw, float partial) {
        if (e.mode() == 2) return;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        if (e.raised()) {
            bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            net.minecraft.client.renderer.BlockModelShapes shapes =
                    Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes();
            TextureAtlasSprite
                    ice = shapes.getTexture(net.minecraft.init.Blocks.PACKED_ICE.getDefaultState()),
                    stone = shapes.getTexture(net.minecraft.init.Blocks.STONE.getDefaultState());
            b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            for (int island = 0; island < 9; island++) {
                net.minecraft.util.math.Vec3d p = e.islandPosition(island, partial);
                double xx = p.x - e.posX, zz = p.z - e.posZ, top = p.y - e.posY;
                double angle = e.islandYaw(island, partial);
                for (Face face : ISLAND) {
                    if (e.split(island) && face.cellX == 0) continue;
                    quad(
                            b,
                            face.ice ? ice : stone,
                            face.shade,
                            face.p,
                            xx,
                            top,
                            zz,
                            angle,
                            e.split(island) ? Math.signum(face.cellX) * .4 : 0);
                }
            }
            Tessellator.getInstance().draw();
        }
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GlStateManager.depthMask(false);
        b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        double h = ScoutArenaLayout.HEIGHT, half = e.half(partial);
        for (int side = 0; side < 4; side++)
            for (double u = -half; u < half; u += 2.5)
                for (double v = 0; v < h; v += 3) {
                    float alpha =
                            .035F
                                    + .025F
                                            * (float)
                                                    Math.sin(
                                                            e.ticksExisted * .06 + v * .2 + u * .2);
                    double u2 = Math.min(half, u + 2.5), v2 = Math.min(h, v + 3);
                    wall(b, side, u, v, u2, v2, half, alpha);
                    wall(b, side, u, v, u + .025, v2, half, .24F);
                    wall(b, side, u, v, u2, v + .025, half, .24F);
                }
        Tessellator.getInstance().draw();
        GlStateManager.depthMask(true);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private static void wall(
            BufferBuilder b,
            int side,
            double u,
            double y,
            double end,
            double top,
            double half,
            float a) {
        wallVertex(b, side, u, y, half, a);
        wallVertex(b, side, end, y, half, a);
        wallVertex(b, side, end, top, half, a);
        wallVertex(b, side, u, top, half, a);
    }

    private static void wallVertex(
            BufferBuilder b, int side, double u, double y, double half, float a) {
        b.pos(
                        side < 2 ? u : (side == 2 ? -half : half),
                        y,
                        side < 2 ? (side == 0 ? -half : half) : u)
                .color(.12F, .65F, 1F, a)
                .endVertex();
    }

    private static void quad(
            BufferBuilder b,
            TextureAtlasSprite t,
            float light,
            double[][] p,
            double x,
            double y,
            double z,
            double angle,
            double split) {
        double c = Math.cos(angle), s = Math.sin(angle);
        for (int i = 0; i < 4; i++) {
            double px = p[i][0] + split, pz = p[i][2];
            b.pos(x + px * c - pz * s, y + p[i][1], z + px * s + pz * c)
                    .tex(
                            i == 0 || i == 3 ? t.getMinU() : t.getMaxU(),
                            i < 2 ? t.getMinV() : t.getMaxV())
                    .color(light, light, light, 1)
                    .endVertex();
        }
    }
}
