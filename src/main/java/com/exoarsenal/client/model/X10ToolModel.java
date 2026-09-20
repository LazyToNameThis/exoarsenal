package com.exoarsenal.client.model;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.item.ItemRTool;
import net.minecraft.util.ResourceLocation;

public class X10ToolModel extends RToolModel {
    @Override
    public ResourceLocation getTextureLocation(ItemRTool object) {
        return new ResourceLocation(ExoArsenal.MODID, "textures/models/armor/x10_refined_100.png");
    }
}
