package com.scapeandrun.frostbite.expedition;

import net.minecraft.item.*;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraftforge.common.util.EnumHelper;
import java.util.List;

public final class FlinxFurCoat extends ItemArmor {
    private static final ArmorMaterial FUR =
            EnumHelper.addArmorMaterial(
                    "FROSTBITE_FLINX",
                    "minecraft:leather",
                    12,
                    new int[] {0, 0, 2, 0},
                    12,
                    net.minecraft.init.SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
                    0);

    public FlinxFurCoat() {
        super(FUR, 0, EntityEquipmentSlot.CHEST);
    }

    @Override
    public boolean getIsRepairable(ItemStack armor, ItemStack ingredient) {
        return ingredient.getItem() == PrebossContent.FLINX_FUR;
    }

    @Override
    public String getArmorTexture(
            ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return "minecraft:textures/models/armor/leather_layer_1.png";
    }

    @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
    @Override
    public net.minecraft.client.model.ModelBiped getArmorModel(
            net.minecraft.entity.EntityLivingBase entity,
            ItemStack stack,
            EntityEquipmentSlot slot,
            net.minecraft.client.model.ModelBiped original) {
        com.scapeandrun.frostbite.expedition.client.PrebossArmorModel model =
                com.scapeandrun.frostbite.expedition.client.PrebossArmorModel.shared();
        model.setModelAttributes(original);
        return model;
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> text, ITooltipFlag flag) {
        text.add("+1 summoned companion.");
        text.add("Summoned allies deal 5% more damage.");
    }
}
