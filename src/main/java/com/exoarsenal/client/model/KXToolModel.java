package com.exoarsenal.client.model;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.item.ItemRTool;
import net.minecraft.util.ResourceLocation;

public class KXToolModel extends X10ToolModel {
    @Override
    public ResourceLocation getTextureLocation(ItemRTool object) {
        return new ResourceLocation(ExoArsenal.MODID, "textures/models/armor/kx20_refined_100.png");
    }
}
