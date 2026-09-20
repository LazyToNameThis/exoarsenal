package com.exoarsenal.expedition.client;

import net.minecraft.client.model.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;

public final class WulfrumArmorModel extends ModelBiped {
    public EntityEquipmentSlot slot;
    public boolean mask, bastion;
    private static final WulfrumArmorModel SHARED = new WulfrumArmorModel();

    public static WulfrumArmorModel shared() {
        return SHARED;
    }

    private static final ExpeditionMesh TORSO =
            new ExpeditionMesh()
                    .box(-4.3, -6, -2.4, 8.6, 12, 4.8, 0x65725B)
                    .box(-.4, -6, -2.6, .8, 12, .3, 0x27312D)
                    .box(1, 1, -2.7, 2.5, 2, .4, 0xC5F36C);
    private static final ExpeditionMesh LEG =
            new ExpeditionMesh()
                    .box(-2, -5, -2, 4, 10, 4, 0x526349)
                    .box(-2.2, -1, -2.3, 4.4, 3, .6, 0xA8B591);
    private static final ExpeditionMesh CUFF =
            new ExpeditionMesh()
                    .box(-2.3, -4, -2.3, 4.6, 8, 4.6, 0x65725B)
                    .box(-2.5, 1, -2.5, 5, 2, 5, 0xB8C5A1);

    public WulfrumArmorModel() {
        super(0);
    }

    @Override
    public void render(
            Entity e, float limb, float amount, float age, float yaw, float pitch, float scale) {
        setRotationAngles(limb, amount, age, yaw, pitch, scale, e);
        GlStateManager.pushMatrix();
        if (isSneak) GlStateManager.translate(0, .2, 0);
        if (slot == EntityEquipmentSlot.HEAD)
            part(
                    bipedHead,
                    -4,
                    ExpeditionModels.item(mask ? "desert_scourge_mask" : "wulfrum_hat"),
                    scale,
                    1.05F);
        if (slot == EntityEquipmentSlot.CHEST) {
            part(bipedBody, 6, TORSO, scale, bastion ? 1.2F : 1);
            part(bipedLeftArm, 4, CUFF, scale, bastion ? 1.4F : 1);
            part(bipedRightArm, 4, CUFF, scale, bastion ? 1.4F : 1);
        }
        if (slot == EntityEquipmentSlot.LEGS) {
            part(bipedLeftLeg, 6, LEG, scale, 1.05F);
            part(bipedRightLeg, 6, LEG, scale, 1.05F);
        }
        GlStateManager.popMatrix();
    }

    private void part(ModelRenderer bone, float cy, ExpeditionMesh mesh, float scale, float size) {
        GlStateManager.pushMatrix();
        bone.postRender(scale);
        GlStateManager.translate(0, cy * scale, 0);
        GlStateManager.scale(size, -size, size);
        ExpeditionRender.mesh(mesh);
        GlStateManager.popMatrix();
    }
}
