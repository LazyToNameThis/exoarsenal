package com.exoarsenal.client.render;

import com.exoarsenal.item.ItemRmorArmor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

import java.util.Locale;

public class PoweredArmorRenderer extends GeoArmorRenderer<ItemRmorArmor> {
    private final int tier;
    private final com.exoarsenal.client.CombatPlayerModel combatPose =
            new com.exoarsenal.client.CombatPlayerModel(false);

    protected PoweredArmorRenderer(AnimatedGeoModel<ItemRmorArmor> model, int tier) {
        super(model);
        this.tier = tier;
        headBone = "helmet";
        bodyBone = "chestplate";
        rightArmBone = "rightArm";
        leftArmBone = "leftArm";
        rightLegBone = "rightLeg";
        leftLegBone = "leftLeg";
        rightBootBone = "rightBoot";
        leftBootBone = "leftBoot";
    }

    @Override
    public void setRotationAngles(
            float limb,
            float amount,
            float age,
            float yaw,
            float pitch,
            float scale,
            net.minecraft.entity.Entity entity) {
        super.setRotationAngles(limb, amount, age, yaw, pitch, scale, entity);
        if (!(entity instanceof net.minecraft.entity.player.EntityPlayer)) return;
        net.minecraft.entity.player.EntityPlayer p =
                (net.minecraft.entity.player.EntityPlayer) entity;
        if (com.exoarsenal.event.RmorEventHandler.isDefenseForm(p)) return;
        combatPose.setModelAttributes(this);
        combatPose.setRotationAngles(limb, amount, age, yaw, pitch, scale, entity);
        copyModelAngles(combatPose.bipedRightArm, bipedRightArm);
        copyModelAngles(combatPose.bipedLeftArm, bipedLeftArm);
        copyModelAngles(combatPose.bipedBody, bipedBody);
        copyModelAngles(combatPose.bipedHead, bipedHead);
        copyModelAngles(combatPose.bipedRightLeg, bipedRightLeg);
        copyModelAngles(combatPose.bipedLeftLeg, bipedLeftLeg);
    }

    @Override
    public void renderRecursively(
            BufferBuilder buffer, GeoBone bone, float red, float green, float blue, float alpha) {
        IGeoRenderer.MATRIX_STACK.push();
        IGeoRenderer.MATRIX_STACK.translate(bone);
        IGeoRenderer.MATRIX_STACK.moveToPivot(bone);
        IGeoRenderer.MATRIX_STACK.rotate(bone);
        IGeoRenderer.MATRIX_STACK.scale(bone);
        IGeoRenderer.MATRIX_STACK.moveBackFromPivot(bone);
        String n = bone.getName().toLowerCase(Locale.ROOT);
        boolean live =
                n.contains("energy")
                        || n.contains("glow")
                        || n.contains("wristblade")
                        || n.contains("katanablade");
        float pulse = (float) ((Math.sin(Minecraft.getSystemTime() * .0025D) + 1D) * .5D);
        float r = tier == 0 ? 1F : tier == 1 ? 1F : .12F,
                g = tier == 0 ? .14F : tier == 1 ? .64F : .78F,
                b = tier == 0 ? .08F : tier == 1 ? .08F : .86F;
        if (!bone.isHidden())
            for (GeoCube cube : bone.childCubes) {
                IGeoRenderer.MATRIX_STACK.push();
                GlStateManager.pushMatrix();
                renderCube(buffer, cube, red, green, blue, alpha);
                if (live) {
                    GlStateManager.disableTexture2D();
                    GlStateManager.disableLighting();
                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(
                            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
                    GlStateManager.depthMask(false);
                    renderCube(
                            buffer,
                            cube,
                            r,
                            g + pulse * .18F,
                            b + pulse * .12F,
                            .12F + pulse * (tier == 2 ? .24F : .17F));
                    GlStateManager.depthMask(true);
                    GlStateManager.disableBlend();
                    GlStateManager.blendFunc(
                            GlStateManager.SourceFactor.SRC_ALPHA,
                            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                    GlStateManager.enableLighting();
                    GlStateManager.enableTexture2D();
                }
                GlStateManager.popMatrix();
                IGeoRenderer.MATRIX_STACK.pop();
            }
        if (!bone.childBonesAreHiddenToo())
            for (GeoBone child : bone.childBones)
                renderRecursively(buffer, child, red, green, blue, alpha);
        IGeoRenderer.MATRIX_STACK.pop();
    }
}
