package com.exoarsenal.client;

import net.minecraft.client.model.ModelPlayer;
import net.minecraft.entity.player.EntityPlayer;

public final class PlayerLocomotion {
    public static void apply(ModelPlayer m, EntityPlayer p, float limb, float amount) {
        if (p.isRiding() || p.isElytraFlying() || p.isInWater() || p.capabilities.isFlying) return;
        float weight = Math.min(1, Math.max(0, amount * 2)),
                run = p.isSprinting() ? 1 : 0,
                crouch = p.isSneaking() ? 1 : 0;
        float swing = (float) Math.sin(limb * .6662), lift = (float) Math.cos(limb * 1.3324);
        float stride = (.62F + run * .28F) * (1 - crouch * .55F) * weight;
        m.bipedRightLeg.rotateAngleX = swing * stride;
        m.bipedLeftLeg.rotateAngleX = -swing * stride;
        m.bipedBody.rotateAngleX += weight * (.06F + run * .14F);
        m.bipedBody.rotateAngleY += (float) Math.sin(limb * .6662) * weight * .06F * (1 - crouch);
        if (p.getHeldItemMainhand().isEmpty() && !p.isHandActive()) {
            m.bipedRightArm.rotateAngleX = -swing * stride * .8F - run * .18F * weight;
            m.bipedLeftArm.rotateAngleX = swing * stride * .8F - run * .18F * weight;
        }

        m.bipedBody.rotationPointY += Math.abs(lift) * weight * .25F;
        if (crouch > 0) {
            m.bipedRightLeg.rotateAngleX -= .12F;
            m.bipedLeftLeg.rotateAngleX -= .12F;
            m.bipedRightArm.rotateAngleZ += .06F;
            m.bipedLeftArm.rotateAngleZ -= .06F;
        }
    }

    private PlayerLocomotion() {}
}
