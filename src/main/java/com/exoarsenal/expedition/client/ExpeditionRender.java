package com.exoarsenal.expedition.client;

import com.exoarsenal.expedition.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

public final class ExpeditionRender {
    private static final net.minecraft.client.renderer.vertex.VertexFormat FORMAT =
            DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL;

    public static void mesh(ExpeditionMesh mesh) {
        mesh(mesh, -1);
    }

    public static void mesh(ExpeditionMesh mesh, int texture) {
        GlStateManager.enableTexture2D();
        GlStateManager.enableNormalize();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableCull();
        int materials = PixelTextures.count(), present = 0;
        for (ExpeditionMesh.Face face : mesh.faces)
            present |=
                    1
                            << ((texture < 0 ? PixelTextures.material(face.color) : texture)
                                    + (face.alpha < 1 ? materials : 0));
        for (int pass = 0; pass < 2; pass++)
            for (int material = 0; material < materials; material++) {
                if ((present & (1 << (material + pass * materials))) == 0) continue;
                GlStateManager.depthMask(pass == 0);
                PixelTextures.bind(material);
                Tessellator t = Tessellator.getInstance();
                BufferBuilder b = t.getBuffer();
                b.begin(GL11.GL_QUADS, FORMAT);
                for (ExpeditionMesh.Face f : mesh.faces) {
                    if ((f.alpha < 1) != (pass == 1)
                            || (texture < 0 ? PixelTextures.material(f.color) : texture)
                                    != material) continue;
                    for (int i = 0; i < 4; i++) {
                        double[] p = f.p[i];
                        b.pos(p[0] / 16, p[1] / 16, p[2] / 16)
                                .tex(f.uv[i][0], f.uv[i][1])
                                .color(
                                        (f.color >> 16 & 255) / 255F,
                                        (f.color >> 8 & 255) / 255F,
                                        (f.color & 255) / 255F,
                                        f.alpha)
                                .normal(f.nx, f.ny, f.nz)
                                .endVertex();
                    }
                }
                t.draw();
            }
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.disableNormalize();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
    }

    public static final class ItemRenderer extends TileEntityItemStackRenderer {
        @Override
        public void renderByItem(ItemStack stack, float partial) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(.5, .5, .5);
            if (WulfrumArsenalModels.owns(stack.getItem().getRegistryName().getResourcePath())) {
                GlStateManager.scale(.65, .65, .65);
                WulfrumArsenalClient.mesh(stack.getItem().getRegistryName().getResourcePath());
            } else
                mesh(
                        stack.getItem() == GeologyContent.MATERIAL
                                ? GeologyClient.material(stack.getMetadata())
                                : DeepContent.ITEMS.contains(stack.getItem())
                                        ? DeepModels.item(
                                                stack.getItem().getRegistryName().getResourcePath())
                                        : WildContent.MATERIALS.contains(stack.getItem())
                                                ? WildModels.material(
                                                        stack.getItem()
                                                                .getRegistryName()
                                                                .getResourcePath())
                                                : ExpeditionModels.item(
                                                        stack.getItem()
                                                                .getRegistryName()
                                                                .getResourcePath()));
            GlStateManager.popMatrix();
        }
    }

    public static final class Robot extends Render<EntityWulfrum> {
        public Robot(RenderManager m) {
            super(m);
            shadowSize = .5F;
        }

        protected ResourceLocation getEntityTexture(EntityWulfrum e) {
            return null;
        }

        public void doRender(
                EntityWulfrum e, double x, double y, double z, float yaw, float partial) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.rotate(180 - yaw, 0, 1, 0);
            float t = e.ticksExisted + partial;
            switch (e.kind()) {
                case AMPLIFIER:
                    mesh(ExpeditionModels.robot("amplifier"));
                    if (e.awake()) {
                        GlStateManager.translate(0, .55, 0);
                        GlStateManager.rotate(t * 3, 0, 1, 0);
                        mesh(ExpeditionModels.robot("duct"));
                    }
                    break;
                case MINE:
                    GlStateManager.rotate(t * (e.charged() ? 4 : 1.5F), 0, 1, 0);
                    if (e.fuse() > 120 && ((int) t / 3) % 2 == 0)
                        GlStateManager.color(1, .25F, .15F, 1);
                    mesh(ExpeditionModels.robot("mine"));
                    break;
                case GYRATOR:
                    GlStateManager.rotate(t * 9, 0, 0, 1);
                    mesh(ExpeditionModels.robot("gyrator"));
                    break;
                case SLIME:
                    if (!e.awake()) {
                        GlStateManager.scale(.5, .5, .5);
                        mesh(ExpeditionModels.robot("gyrator"));
                    } else {
                        double squash = 1 + Math.sin(t * .3) * .1;
                        GlStateManager.scale(1 / squash, squash, 1 / squash);
                        mesh(ExpeditionModels.robot("slime"));
                    }
                    break;
                case DRONE:
                case HOVERCRAFT:
                    GlStateManager.translate(0, Math.sin(t * .12) * .04, 0);
                    mesh(ExpeditionModels.robot("chassis"));
                    for (int s : new int[] {-1, 1}) {
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(s * .48, .38, 0);
                        GlStateManager.rotate(t * 15, 0, 1, 0);
                        mesh(ExpeditionModels.robot("duct"));
                        GlStateManager.popMatrix();
                    }
                    break;
                case ROVER:
                    mesh(ExpeditionModels.robot("chassis"));
                    for (int sx : new int[] {-1, 1})
                        for (int sz : new int[] {-1, 1}) {
                            GlStateManager.pushMatrix();
                            GlStateManager.translate(sx * .27, .18, sz * .3);
                            GlStateManager.rotate(t * 8, 0, 0, 1);
                            mesh(ExpeditionModels.robot("wheel"));
                            GlStateManager.popMatrix();
                        }
                    break;
            }
            GlStateManager.popMatrix();
            super.doRender(e, x, y, z, yaw, partial);
        }
    }

    public static final class Shot extends Render<EntityExpeditionShot> {
        public Shot(RenderManager m) {
            super(m);
        }

        protected ResourceLocation getEntityTexture(EntityExpeditionShot e) {
            return null;
        }

        public void doRender(
                EntityExpeditionShot e, double x, double y, double z, float yaw, float partial) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.rotate(
                    e.prevRotationYaw + (e.rotationYaw - e.prevRotationYaw) * partial, 0, 1, 0);
            GlStateManager.rotate(-e.rotationPitch, 1, 0, 0);
            GlStateManager.scale(.5, .5, .5);
            if (e.type() == EntityExpeditionShot.PG_DAGGER
                    || e.type() == EntityExpeditionShot.PG_LASER) {
                GlStateManager.rotate(-90, 0, 1, 0);
                if (e.type() == EntityExpeditionShot.PG_DAGGER)
                    GlStateManager.rotate((e.ticksExisted + partial) * 30, 0, 1, 0);
                WulfrumArsenalClient.mesh(
                        e.type() == EntityExpeditionShot.PG_DAGGER ? "pg_c_striker" : "laser");
                GlStateManager.popMatrix();
                return;
            }
            String id =
                    e.type() == 0
                            ? "sahara_slicers"
                            : e.type() == 1
                                    ? "prism_shard"
                                    : e.type() == 2
                                            ? "pearl_shard"
                                            : e.type() == 3
                                                    ? "scourge_of_the_desert"
                                                    : e.type() == 4
                                                            ? "wulfrum_screwdriver"
                                                            : "wulfrum_energy_core";
            if (e.type() >= 6) {
                switch (e.type()) {
                    case EntityExpeditionShot.WATER:
                        id = "water_bolt";
                        break;
                    case EntityExpeditionShot.BUBBLE:
                        id = "bubble";
                        break;
                    case EntityExpeditionShot.CORAL:
                        id = "coral_dart";
                        break;
                    case EntityExpeditionShot.URCHIN:
                        id = "urchin";
                        break;
                    case EntityExpeditionShot.FISHBONE:
                        id = "fishbone_boomerang";
                        break;
                    case EntityExpeditionShot.SHELL:
                        id = "seashell";
                        break;
                }
                GlStateManager.scale(.65, .65, .65);
            }
            if (e.type() == 0
                    || e.type() == 4
                    || e.type() == EntityExpeditionShot.FISHBONE
                    || e.type() == EntityExpeditionShot.SHELL)
                GlStateManager.rotate((e.ticksExisted + partial) * 35, 0, 0, 1);
            else GlStateManager.rotate(90, 1, 0, 0);
            if (e.type() == 5) GlStateManager.scale(.3, .3, .3);
            mesh(ExpeditionModels.item(id));
            GlStateManager.popMatrix();
        }
    }

    public static final class Minion extends Render<EntityExpeditionMinion> {
        private static final ExpeditionMesh STAR = new ExpeditionMesh().star(0, 0, 0, 5, 0x7EBBBD);

        public Minion(RenderManager m) {
            super(m);
        }

        protected ResourceLocation getEntityTexture(EntityExpeditionMinion e) {
            return null;
        }

        public void doRender(
                EntityExpeditionMinion e, double x, double y, double z, float yaw, float partial) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y + (e.variant() >= 4 ? 0 : .25), z);
            GlStateManager.rotate(
                    e.variant() >= 2 ? 180 - yaw : (e.ticksExisted + partial) * 5,
                    0,
                    e.variant() == 0 ? 0 : 1,
                    e.variant() == 0 ? 1 : 0);
            if (e.variant() == 0) mesh(STAR);
            else if (e.variant() == 2) SeaRender.drawBell(e.ticksExisted + partial);
            else if (e.variant() == 3) mesh(SeaModels.item("snail"));
            else if (e.variant() >= 4) PrebossClient.companion(e, partial);
            else {
                GlStateManager.scale(.6, .6, .6);
                mesh(ExpeditionModels.robot("chassis"));
            }
            GlStateManager.popMatrix();
        }
    }
}
