package com.exoarsenal.client.model;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.item.ItemRmorArmor;
import com.exoarsenal.event.RmorEventHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class RmorArmorModel extends AnimatedGeoModel<ItemRmorArmor> {
    private boolean defenseTexture;
    protected int chargeTexture = 2;

    @Override
    public ResourceLocation getModelLocation(ItemRmorArmor object) {
        return new ResourceLocation(ExoArsenal.MODID, "geo/rmor_refined.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(ItemRmorArmor object) {
        return new ResourceLocation(
                ExoArsenal.MODID, "textures/models/armor/rmor_refined_" + chargeName() + ".png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(ItemRmorArmor object) {
        return new ResourceLocation(ExoArsenal.MODID, "animations/rmor.animation.json");
    }

    @Override
    public void setLivingAnimations(ItemRmorArmor armor, Integer id, AnimationEvent event) {
        super.setLivingAnimations(armor, id, event);
        if (event.getExtraDataOfType(EntityLivingBase.class).isEmpty()) return;
        EntityLivingBase wearer =
                (EntityLivingBase) event.getExtraDataOfType(EntityLivingBase.class).get(0);
        defenseTexture = RmorEventHandler.isDefenseForm(wearer);
        int power = RmorEventHandler.getPowerLevel(wearer);
        chargeTexture = power;
        setGroup("drained", !defenseTexture && power == 0);
        setGroup("half", !defenseTexture && power == 1);
        setGroup("full", !defenseTexture && power == 2);
        setGroup("defense", defenseTexture);
        IBone wristBlade = optionalBone("wristBlade");
        if (wristBlade != null) wristBlade.setHidden(power == 0 || defenseTexture);
        IBone leftShotgun = optionalBone("leftShotgun");
        if (leftShotgun != null) leftShotgun.setHidden(!defenseTexture);
        IBone rightShotgun = optionalBone("rightShotgun");
        if (rightShotgun != null) rightShotgun.setHidden(!defenseTexture);
        setX10Bones(false);
    }

    protected String chargeName() {
        return chargeTexture == 0 ? "0" : chargeTexture == 1 ? "50" : "100";
    }

    private void setGroup(String prefix, boolean visible) {
        for (String suffix :
                new String[] {
                    "Helmet", "Chest", "Back", "LeftArm", "RightArm", "LeftLeg", "RightLeg"
                }) {
            IBone bone = optionalBone(prefix + suffix);
            if (bone != null) bone.setHidden(!visible);
        }
    }

    protected void setX10Bones(boolean visible) {
        for (String name :
                new String[] {
                    "x10Analyzer",
                    "x10Chest",
                    "x10WingLeft",
                    "x10WingRight",
                    "x10BeltLeft",
                    "x10BeltRight",
                    "x10ShotgunLeft",
                    "x10ShotgunRight",
                    "x10BlasterLeft",
                    "x10BlasterRight",
                    "x10KatanaLeft",
                    "x10KatanaRight"
                }) {
            IBone bone = optionalBone(name);
            if (bone != null) bone.setHidden(!visible);
        }
    }

    protected void setBone(String name, boolean visible) {
        IBone bone = optionalBone(name);
        if (bone != null) bone.setHidden(!visible);
    }

    private IBone optionalBone(String name) {
        try {
            return getBone(name);
        } catch (RuntimeException missing) {
            return null;
        }
    }
}
