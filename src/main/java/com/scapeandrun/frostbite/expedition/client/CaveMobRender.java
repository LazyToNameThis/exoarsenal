package com.scapeandrun.frostbite.expedition.client;

import com.scapeandrun.frostbite.expedition.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public final class CaveMobRender extends Render<EntityLivingBase> {
    public CaveMobRender(RenderManager manager) {
        super(manager);
        shadowSize = .55F;
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityLivingBase entity) {
        return null;
    }

    private static void mesh(String part) {
        ExpeditionRender.mesh(CaveModels.part(part));
    }

    @Override
    public void doRender(
            EntityLivingBase entity, double x, double y, double z, float yaw, float partial) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(180 - yaw, 0, 1, 0);
        float time = entity.ticksExisted + partial,
                walk = entity.limbSwing,
                amount = entity.limbSwingAmount;
        int kind = entity instanceof EntityCaveMob ? ((EntityCaveMob) entity).kind() : 4;
        if (kind == 4) {
            mesh("spider");
            for (int side : new int[] {-1, 1})
                for (int leg = 0; leg < 4; leg++) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(side * .2, .35, (leg - 1.5) * .18);
                    GlStateManager.scale(side, 1, 1);
                    GlStateManager.rotate(
                            (leg - 1.5F) * 22 + (float) Math.sin(walk + leg * 1.5) * amount * 25,
                            0,
                            1,
                            0);
                    mesh("spider_leg");
                    GlStateManager.popMatrix();
                }
        } else if (kind == 3) {
            GlStateManager.translate(0, .35, 0);
            mesh("bat");
            for (int side : new int[] {-1, 1}) {
                GlStateManager.pushMatrix();
                GlStateManager.scale(side, 1, 1);
                GlStateManager.rotate((float) Math.sin(time * .7) * 35, 0, 0, 1);
                mesh("wing");
                GlStateManager.popMatrix();
            }
        } else if (kind == 1) {
            GlStateManager.translate(0, .7, 0);
            mesh("elemental");
            for (int i = 0; i < 4; i++) {
                GlStateManager.pushMatrix();
                double angle = time * .025 + i * Math.PI * .5;
                GlStateManager.translate(
                        Math.cos(angle) * .5, Math.sin(angle * 2) * .2, Math.sin(angle) * .5);
                GlStateManager.rotate(time * 2 + i * 30, 1, 1, 0);
                mesh("fragment");
                GlStateManager.popMatrix();
            }
        } else {
            boolean human = kind == 2;
            mesh(human ? "hoplite" : "golem");
            float attack = ((EntityCaveMob) entity).attackTicks();
            float arm = 0;
            if (attack > 0) {
                float progress = Math.max(0, Math.min(1, (24 - attack) / 16));
                arm =
                        attack >= 8
                                ? -30 + 120 * progress * progress * (3 - 2 * progress)
                                : 90 * (attack / 8) * (attack / 8);
            }
            for (int side : new int[] {-1, 1}) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(side * (human ? .14 : .2), 0, 0);
                GlStateManager.rotate((float) Math.sin(walk * .8 + side) * amount * 25, 1, 0, 0);
                mesh(human ? "bone_leg" : "stone_leg");
                GlStateManager.popMatrix();
                GlStateManager.pushMatrix();
                GlStateManager.translate(side * (human ? .24 : .4), human ? 1.13 : 1.02, 0);
                GlStateManager.rotate(
                        side == -1 ? arm : (float) Math.sin(walk * .8) * amount * -18, 1, 0, 0);
                mesh(human ? "bone_arm" : "stone_arm");
                if (human) {
                    GlStateManager.translate(0, -.57, -.05);
                    if (side == -1) {
                        if (attack >= 8 || attack == 0)
                            ExpeditionRender.mesh(PrebossModels.item("cave_javelin"));
                    } else mesh("shield");
                }
                GlStateManager.popMatrix();
            }
        }
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, yaw, partial);
    }
}
