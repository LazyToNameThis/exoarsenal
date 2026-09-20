package com.scapeandrun.frostbite.client.model;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.item.ItemEnergyGun;
import net.minecraft.util.ResourceLocation;

public class KXEnergyGunModel extends EnergyGunModel {
    @Override
    public ResourceLocation getTextureLocation(ItemEnergyGun object) {
        return new ResourceLocation(Frostbite.MODID, "textures/models/armor/kx20_refined_100.png");
    }
}
