package com.scapeandrun.frostbite.client.model;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.item.ItemRTool;
import net.minecraft.util.ResourceLocation;

public class KXToolModel extends X10ToolModel {
    @Override
    public ResourceLocation getTextureLocation(ItemRTool object) {
        return new ResourceLocation(Frostbite.MODID, "textures/models/armor/kx20_refined_100.png");
    }
}
