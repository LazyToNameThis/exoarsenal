package com.exoarsenal.expedition.client;

import com.exoarsenal.expedition.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;

public final class SeaRender {
    private static void part(String id) {
        ExpeditionRender.mesh(SeaModels.item(id));
    }

    public static final class Creature extends Render<EntitySeaCreature> {
        public Creature(RenderManager manager) {
            super(manager);
            shadowSize = .35F;
        }

        @Override
        protected ResourceLocation getEntityTexture(EntitySeaCreature entity) {
            return null;
        }

        @Override
        public void doRender(
                EntitySeaCreature entity, double x, double y, double z, float yaw, float partial) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.rotate(180 - yaw, 0, 1, 0);
            float time = entity.ticksExisted + partial;
            switch (entity.kind()) {
                case GIANT:
                    GlStateManager.scale(3.6, 3.6, 3.6);
                case CLAM:
                    part("clam_lower");
                    GlStateManager.translate(0, .2, .28);
                    float opening =
                            entity.angry()
                                    ? (entity.kind() == EntitySeaCreature.Kind.GIANT
                                            ? (entity.action() < 25
                                                    ? entity.action() / 25F
                                                    : entity.action() < 42 ? 0 : .35F)
                                            : entity.action() / 15F)
                                    : 0;
                    GlStateManager.rotate(-opening * 38, 1, 0, 0);
                    GlStateManager.translate(0, 0, -.28);
                    part("clam_upper");
                    break;
                case RAY:
                    part("ray_body");
                    for (int side : new int[] {-1, 1}) {
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(side * .17, .05, 0);
                        GlStateManager.scale(side, 1, 1);
                        GlStateManager.rotate((float) Math.sin(time * .14) * 18, 0, 0, 1);
                        part("ray_wing");
                        GlStateManager.popMatrix();
                    }
                    break;
                case BABY_BELL:
                    GlStateManager.scale(.5, .5, .5);
                case BELL:
                    drawBell(time);
                    break;
                case PRISM:
                    part("prism_back");
                    break;
                case FLOATY:
                    GlStateManager.rotate((float) Math.sin(time * .09) * 8, 0, 0, 1);
                    part("floaty");
                    break;
                case MINNOW:
                    GlStateManager.rotate((float) Math.sin(time * .45) * 12, 0, 1, 0);
                    part("minnow");
                    break;
            }
            GlStateManager.popMatrix();
            super.doRender(entity, x, y, z, yaw, partial);
        }
    }

    public static void drawBell(float time) {
        GlStateManager.pushMatrix();
        double pulse = 1 + Math.sin(time * .12) * .06;
        GlStateManager.scale(pulse, 1 / pulse, pulse);
        part("bell");
        GlStateManager.popMatrix();
        for (int i = 0; i < 4; i++) {
            GlStateManager.pushMatrix();
            GlStateManager.translate((i % 2 == 0 ? -.16 : .16), 0, i < 2 ? -.16 : .16);
            GlStateManager.rotate((float) Math.sin(time * .12 + i) * 12, 1, 0, 1);
            part("tentacle");
            GlStateManager.popMatrix();
        }
    }

    public static final class Cnidrion extends Render<EntityCnidrion> {
        public Cnidrion(RenderManager manager) {
            super(manager);
            shadowSize = .6F;
        }

        @Override
        protected ResourceLocation getEntityTexture(EntityCnidrion entity) {
            return null;
        }

        @Override
        public void doRender(
                EntityCnidrion entity, double x, double y, double z, float yaw, float partial) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.rotate(180 - yaw, 0, 1, 0);
            float time = entity.ticksExisted + partial;
            GlStateManager.rotate((float) Math.sin(time * .07) * 4, 0, 0, 1);
            part("cnidrion_body");
            for (int side : new int[] {-1, 1}) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(side * .17, 1, 0);
                GlStateManager.scale(side * .5, .5, .5);
                GlStateManager.rotate((float) Math.sin(time * .6) * 30, 0, 0, 1);
                part("ray_wing");
                GlStateManager.popMatrix();
            }
            GlStateManager.translate(0, 1.34, 0);
            GlStateManager.rotate(
                    entity.cast() > 0 ? (float) Math.sin(time * 1.5) * 5 : -8, 1, 0, 0);
            part("cnidrion_head");
            GlStateManager.popMatrix();
            super.doRender(entity, x, y, z, yaw, partial);
        }
    }

    public static final class King extends Render<EntitySeaKing> {
        public King(RenderManager manager) {
            super(manager);
            shadowSize = .4F;
        }

        @Override
        protected ResourceLocation getEntityTexture(EntitySeaKing entity) {
            return null;
        }

        @Override
        public void doRender(
                EntitySeaKing entity, double x, double y, double z, float yaw, float partial) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.rotate(180 - yaw, 0, 1, 0);
            part("king_body");
            float walk = (float) Math.sin(entity.limbSwing * .65F) * entity.limbSwingAmount * 35;
            for (int side : new int[] {-1, 1}) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(side * .32, 1.37, 0);
                GlStateManager.rotate(side * walk, 1, 0, 0);
                part("king_arm");
                GlStateManager.popMatrix();
                GlStateManager.pushMatrix();
                GlStateManager.translate(side * .13, .75, 0);
                GlStateManager.rotate(-side * walk, 1, 0, 0);
                part("king_leg");
                GlStateManager.popMatrix();
            }
            GlStateManager.translate(0, 1.65, 0);
            GlStateManager.rotate(entity.rotationYawHead - entity.renderYawOffset, 0, 1, 0);
            GlStateManager.rotate(entity.rotationPitch, 1, 0, 0);
            part("king_head");
            GlStateManager.popMatrix();
            super.doRender(entity, x, y, z, yaw, partial);
        }
    }
}
