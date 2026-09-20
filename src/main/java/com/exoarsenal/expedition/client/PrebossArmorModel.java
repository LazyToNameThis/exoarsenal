package com.exoarsenal.expedition.client;

import net.minecraft.client.model.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

public final class PrebossArmorModel extends ModelBiped {
    private static final PrebossArmorModel MODEL = new PrebossArmorModel();

    public static PrebossArmorModel shared() {
        return MODEL;
    }

    @Override
    public void render(
            Entity entity,
            float limb,
            float amount,
            float age,
            float yaw,
            float pitch,
            float scale) {
        setRotationAngles(limb, amount, age, yaw, pitch, scale, entity);
        GlStateManager.pushMatrix();
        if (isSneak) GlStateManager.translate(0, .2, 0);
        part(bipedBody, 6, "flinx_fur_coat", scale);
        part(bipedLeftArm, 4, "flinx_sleeve", scale);
        part(bipedRightArm, 4, "flinx_sleeve", scale);
        GlStateManager.popMatrix();
    }

    private static void part(ModelRenderer bone, float y, String id, float scale) {
        GlStateManager.pushMatrix();
        bone.postRender(scale);
        GlStateManager.translate(0, y * scale, 0);
        GlStateManager.scale(1.04, -1.04, 1.04);
        ExpeditionRender.mesh(PrebossModels.item(id));
        GlStateManager.popMatrix();
    }
}
