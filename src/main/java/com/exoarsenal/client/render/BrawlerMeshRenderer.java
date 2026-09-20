package com.exoarsenal.client.render;

import com.exoarsenal.client.BrawlerJetpack;
import com.exoarsenal.client.BrawlerSkin;
import com.exoarsenal.client.ModelTexture;
import com.exoarsenal.expedition.client.ExpeditionMesh;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public final class BrawlerMeshRenderer {
    private BrawlerMeshRenderer() {}

    public static void draw(ExpeditionMesh mesh, int part, boolean jetpack) {
        if (jetpack) {
            GlStateManager.enableTexture2D();
            Minecraft.getMinecraft()
                    .getTextureManager()
                    .bindTexture(ModelTexture.get("brawler_thrusters"));
            BufferBuilder jetBuffer = Tessellator.getInstance().getBuffer();
            jetBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            for (ExpeditionMesh.Face face : mesh.faces) {
                float shade =
                        BrawlerSkin.emissive(face.color)
                                ? 1
                                : .64F + .23F * Math.max(0, face.ny) + .13F * Math.max(0, face.nz);
                for (int vertex = 0; vertex < 4; vertex++) {
                    double[] point = face.p[vertex], uv = BrawlerJetpack.uv(face, vertex);
                    jetBuffer
                            .pos(point[0], point[1], point[2])
                            .tex(uv[0], uv[1])
                            .color(shade, shade, shade, 1)
                            .endVertex();
                }
            }
            Tessellator.getInstance().draw();
            GlStateManager.disableTexture2D();
            return;
        }
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        if (part >= 0) {
            GlStateManager.enableTexture2D();
            Minecraft.getMinecraft().getTextureManager().bindTexture(ModelTexture.get("brawler"));
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            for (ExpeditionMesh.Face face : mesh.faces)
                if (!BrawlerSkin.emissive(face.color)) {
                    float shade =
                            (.64F + .23F * Math.max(0, face.ny) + .13F * Math.max(0, face.nz))
                                    * BrawlerSkin.materialShade(face.color);
                    for (int vertex = 0; vertex < 4; vertex++) {
                        double[] point = face.p[vertex], uv = BrawlerSkin.uv(part, face, vertex);
                        buffer.pos(point[0], point[1], point[2])
                                .tex(uv[0], uv[1])
                                .color(shade, shade, shade, 1)
                                .endVertex();
                    }
                }
            Tessellator.getInstance().draw();
        }
        GlStateManager.disableTexture2D();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (ExpeditionMesh.Face face : mesh.faces)
            if (part < 0 || BrawlerSkin.emissive(face.color)) {
                float shade =
                        BrawlerSkin.emissive(face.color)
                                ? 1
                                : .62F + .25F * Math.max(0, face.ny) + .13F * Math.max(0, face.nz);
                for (double[] point : face.p)
                    buffer.pos(point[0], point[1], point[2])
                            .color(
                                    (face.color >> 16 & 255) / 255F * shade,
                                    (face.color >> 8 & 255) / 255F * shade,
                                    (face.color & 255) / 255F * shade,
                                    1)
                            .endVertex();
            }
        Tessellator.getInstance().draw();
    }

    public static void drawGhost(ExpeditionMesh mesh, float opacity) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.depthMask(false);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (ExpeditionMesh.Face face : mesh.faces) {
            float light = .55F + .45F * Math.max(0, face.nz);
            for (double[] point : face.p)
                buffer.pos(point[0], point[1], point[2])
                        .color(.22F * light, .95F * light, .60F * light, opacity)
                        .endVertex();
        }
        Tessellator.getInstance().draw();
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
    }
}
