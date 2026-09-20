package com.scapeandrun.frostbite.expedition;

import net.minecraft.item.*;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;

public final class ScourgeMask extends ItemArmor {
    public ScourgeMask() {
        super(ArmorMaterial.LEATHER, 0, EntityEquipmentSlot.HEAD);
    }

    @Override
    public int getColor(ItemStack s) {
        return 0xA98650;
    }

    @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
    @Override
    public net.minecraft.client.model.ModelBiped getArmorModel(
            net.minecraft.entity.EntityLivingBase e,
            ItemStack stack,
            EntityEquipmentSlot slot,
            net.minecraft.client.model.ModelBiped original) {
        com.scapeandrun.frostbite.expedition.client.WulfrumArmorModel model =
                com.scapeandrun.frostbite.expedition.client.WulfrumArmorModel.shared();
        model.slot = slot;
        model.mask = true;
        model.bastion = false;
        model.setModelAttributes(original);
        return model;
    }

    @Override
    public String getArmorTexture(ItemStack s, Entity e, EntityEquipmentSlot slot, String type) {
        return "minecraft:textures/models/armor/leather_layer_1.png";
    }
}
