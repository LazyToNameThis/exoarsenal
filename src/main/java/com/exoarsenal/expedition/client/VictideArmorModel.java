package com.exoarsenal.expedition.client;

import net.minecraft.client.model.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;

public final class VictideArmorModel extends ModelBiped {
    private static final VictideArmorModel MODEL = new VictideArmorModel();
    public EntityEquipmentSlot slot;
    public int style;

    public static VictideArmorModel shared() {
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
        if (slot == EntityEquipmentSlot.HEAD) {
            String[] ids = {
                "victide_shellmet",
                "victide_coral_turban",
                "victide_hermit_helmet",
                "victide_mask",
                "victide_headcrab"
            };
            part(bipedHead, -4, ids[style], scale);
        }
        if (slot == EntityEquipmentSlot.CHEST) {
            part(bipedBody, 6, "victide_breastplate", scale);
            part(bipedLeftArm, 4, "armor_arm", scale);
            part(bipedRightArm, 4, "armor_arm", scale);
        }
        if (slot == EntityEquipmentSlot.LEGS) {
            part(bipedLeftLeg, 6, "armor_leg", scale);
            part(bipedRightLeg, 6, "armor_leg", scale);
        }
        GlStateManager.popMatrix();
    }

    private void part(ModelRenderer bone, float y, String id, float scale) {
        GlStateManager.pushMatrix();
        bone.postRender(scale);
        GlStateManager.translate(0, y * scale, 0);
        GlStateManager.scale(1.04, -1.04, 1.04);
        ExpeditionRender.mesh(SeaModels.item(id));
        GlStateManager.popMatrix();
    }
}
