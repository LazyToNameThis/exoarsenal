package com.exoarsenal.client.model;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.item.ItemRTool;
import com.exoarsenal.item.ItemX10Multitool;
import com.exoarsenal.item.ItemKXMultitool;
import com.exoarsenal.client.ClientEquipmentHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class RToolModel extends AnimatedGeoModel<ItemRTool> {
    @Override
    public ResourceLocation getModelLocation(ItemRTool object) {
        return new ResourceLocation(ExoArsenal.MODID, "geo/rtool_refined.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(ItemRTool object) {
        return new ResourceLocation(ExoArsenal.MODID, "textures/models/armor/rmor_refined_100.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(ItemRTool object) {
        return new ResourceLocation(ExoArsenal.MODID, "animations/rtool.animation.json");
    }

    @Override
    public void setLivingAnimations(ItemRTool tool, Integer id, AnimationEvent event) {
        super.setLivingAnimations(tool, id, event);
        ItemStack stack =
                event.getExtraDataOfType(ItemStack.class).isEmpty()
                        ? ItemStack.EMPTY
                        : (ItemStack) event.getExtraDataOfType(ItemStack.class).get(0);
        int form = ItemRTool.getForm(stack);
        setVisible("drillForm", form == ItemRTool.DRILL);
        setVisible("drillEnergy", form == ItemRTool.DRILL);
        setVisible("sawForm", form == ItemRTool.SAW);
        setVisible("sawEnergy", form == ItemRTool.SAW);
        setVisible("scytheForm", form == ItemRTool.SCYTHE);
        setVisible("buildForm", form == ItemRTool.BUILD);
        setVisible("buildEnergy", form == ItemRTool.BUILD);
        setVisible("buildLaserA", form == ItemRTool.BUILD);
        setVisible("buildLaserB", form == ItemRTool.BUILD);
        setVisible("buildLaserC", form == ItemRTool.BUILD);
        setVisible("buildLaserD", form == ItemRTool.BUILD);
        setVisible(
                "drillSingleEnergy",
                form == ItemRTool.DRILL && ItemRTool.getMode(stack) == ItemRTool.SINGLE);
        setVisible(
                "drillAreaEnergy",
                form == ItemRTool.DRILL && ItemRTool.getMode(stack) == ItemRTool.AREA);
        setVisible(
                "drillVeinEnergy",
                form == ItemRTool.DRILL && ItemRTool.getMode(stack) == ItemRTool.VEIN);
        setVisible("x10Trim", tool instanceof ItemX10Multitool);
        setVisible("kxTrim", tool instanceof ItemKXMultitool);
        double travel =
                ClientEquipmentHandler.isRToolUsing(stack)
                        ? (Minecraft.getSystemTime() % 1000000L) * 0.055
                        : 0;
        for (int i = 0; i < SawChainPath.LINKS; i++) {
            IBone link = getBone("sawLinkEnergy" + i);
            if (link == null) continue;
            double[] point =
                    SawChainPath.sample(travel + i * SawChainPath.LENGTH / SawChainPath.LINKS);
            link.setPositionX((float) point[0]);
            link.setPositionY((float) point[1]);
            link.setRotationZ((float) point[2]);
        }
    }

    protected void setVisible(String name, boolean visible) {
        IBone bone = getBone(name);
        if (bone != null) bone.setHidden(!visible);
    }
}
