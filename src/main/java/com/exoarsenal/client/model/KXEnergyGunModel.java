package com.exoarsenal.client.model;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.item.ItemEnergyGun;
import net.minecraft.util.ResourceLocation;

public class KXEnergyGunModel extends EnergyGunModel {
    @Override
    public ResourceLocation getTextureLocation(ItemEnergyGun object) {
        return new ResourceLocation(ExoArsenal.MODID, "textures/models/armor/kx20_refined_100.png");
    }
}
