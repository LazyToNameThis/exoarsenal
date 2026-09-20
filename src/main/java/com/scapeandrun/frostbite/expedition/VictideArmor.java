package com.scapeandrun.frostbite.expedition;

import net.minecraft.item.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.init.SoundEvents;
import net.minecraft.world.World;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraftforge.common.util.EnumHelper;
import java.util.List;

public final class VictideArmor extends ItemArmor {
    private static final ArmorMaterial[] MATERIALS = {
        material("SHELL", 4),
        material("CORAL", 3),
        material("HERMIT", 2),
        material("MASK", 1),
        material("CRAB", 3)
    };
    public final int style;

    private static ArmorMaterial material(String name, int head) {
        return EnumHelper.addArmorMaterial(
                "FROSTBITE_VICTIDE_" + name,
                "minecraft:iron",
                18,
                new int[] {0, 4, 5, head},
                16,
                SoundEvents.ITEM_ARMOR_EQUIP_IRON,
                0);
    }

    public VictideArmor(EntityEquipmentSlot slot, int style) {
        super(MATERIALS[style], 0, slot);
        this.style = style;
    }

    public static boolean full(EntityPlayer player) {
        return player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem()
                        instanceof VictideArmor
                && player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem()
                        == SeaContent.CHEST
                && player.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem()
                        == SeaContent.LEGS;
    }

    public static int style(EntityPlayer player) {
        Item head = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem();
        return head instanceof VictideArmor ? ((VictideArmor) head).style : -1;
    }

    public static boolean summoner(EntityPlayer player) {
        return full(player) && style(player) == 3;
    }

    @Override
    public boolean getIsRepairable(ItemStack armor, ItemStack ingredient) {
        return ingredient.getItem() == SeaContent.REMAINS;
    }

    @Override
    public String getArmorTexture(
            ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return "minecraft:textures/models/armor/iron_layer_1.png";
    }

    @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
    @Override
    public net.minecraft.client.model.ModelBiped getArmorModel(
            net.minecraft.entity.EntityLivingBase entity,
            ItemStack stack,
            EntityEquipmentSlot slot,
            net.minecraft.client.model.ModelBiped original) {
        com.scapeandrun.frostbite.expedition.client.VictideArmorModel model =
                com.scapeandrun.frostbite.expedition.client.VictideArmorModel.shared();
        model.slot = slot;
        model.style = style;
        model.setModelAttributes(original);
        return model;
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> text, ITooltipFlag flag) {
        if (armorType == EntityEquipmentSlot.HEAD) {
            String[] roles = {
                "Melee strikes",
                "Ranged shots",
                "Magic attacks",
                "Summoned allies",
                "Returning weapons"
            };
            text.add(roles[style] + ": +" + (style == 3 ? 10 : 5) + "% damage.");
            if (style == 3) text.add("Full set: +1 minion and a protective sea snail.");
        }
        if (armorType == EntityEquipmentSlot.CHEST)
            text.add("5% damage reduction; 15% while submerged.");
        if (armorType == EntityEquipmentSlot.LEGS)
            text.add("8% faster movement; 30% while submerged.");
        text.add("Full set: swim freely and regenerate underwater.");
        text.add("Hits have a 10% chance to release a returning seashell.");
    }
}
