package com.exoarsenal.client.model;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.item.ItemRBlade;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class RBladeModel extends AnimatedGeoModel<ItemRBlade> {
    @Override
    public ResourceLocation getModelLocation(ItemRBlade object) {
        return new ResourceLocation(ExoArsenal.MODID, "geo/rblade_refined.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(ItemRBlade object) {
        return new ResourceLocation(ExoArsenal.MODID, "textures/models/armor/rmor_refined_100.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(ItemRBlade object) {
        return new ResourceLocation(ExoArsenal.MODID, "animations/rblade.animation.json");
    }

    @Override
    public void setLivingAnimations(ItemRBlade blade, Integer id, AnimationEvent event) {
        super.setLivingAnimations(blade, id, event);
        ItemStack stack =
                event.getExtraDataOfType(ItemStack.class).isEmpty()
                        ? ItemStack.EMPTY
                        : (ItemStack) event.getExtraDataOfType(ItemStack.class).get(0);
        boolean thrown =
                !stack.isEmpty()
                        && stack.hasTagCompound()
                        && stack.getTagCompound().getBoolean("Thrown");
        for (String boneName : new String[] {"pommel", "handle", "guard", "emitter", "blade"})
            setVisible(boneName, !thrown);
        setVisible("discForm", thrown);
    }

    protected void setVisible(String name, boolean visible) {
        IBone bone = getBone(name);
        if (bone != null) bone.setHidden(!visible);
    }
}
