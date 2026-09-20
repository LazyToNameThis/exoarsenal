package com.exoarsenal.expedition;

import net.minecraft.item.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;
import java.util.List;

public final class WulfrumArmor extends ItemArmor {
    private static final ArmorMaterial MATERIAL =
            EnumHelper.addArmorMaterial(
                    "EXOARSENAL_WULFRUM",
                    "minecraft:leather",
                    12,
                    new int[] {0, 1, 2, 1},
                    12,
                    SoundEvents.ITEM_ARMOR_EQUIP_IRON,
                    0);

    public WulfrumArmor(EntityEquipmentSlot slot) {
        super(MATERIAL, 0, slot);
    }

    public static boolean full(EntityPlayer p) {
        return p.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == ExpeditionContent.HAT
                && p.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem()
                        == ExpeditionContent.JACKET
                && p.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem()
                        == ExpeditionContent.OVERALLS;
    }

    @Override
    public String getArmorTexture(ItemStack s, Entity e, EntityEquipmentSlot slot, String type) {
        return "minecraft:textures/models/armor/leather_layer_"
                + (slot == EntityEquipmentSlot.LEGS ? 2 : 1)
                + ".png";
    }

    @Override
    public int getColor(ItemStack s) {
        return 0x7D9464;
    }

    @Override
    public boolean hasColor(ItemStack s) {
        return true;
    }

    @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
    @Override
    public net.minecraft.client.model.ModelBiped getArmorModel(
            net.minecraft.entity.EntityLivingBase e,
            ItemStack stack,
            EntityEquipmentSlot slot,
            net.minecraft.client.model.ModelBiped original) {
        com.exoarsenal.expedition.client.WulfrumArmorModel model =
                com.exoarsenal.expedition.client.WulfrumArmorModel.shared();
        model.slot = slot;
        model.mask = false;
        model.bastion = e instanceof EntityPlayer && ExpeditionEvents.active((EntityPlayer) e);
        model.setModelAttributes(original);
        return model;
    }

    @Override
    public boolean getIsRepairable(ItemStack a, ItemStack b) {
        return b.getItem() == ExpeditionContent.SCRAP;
    }

    @Override
    public void addInformation(ItemStack s, World w, List<String> t, ITooltipFlag f) {
        t.add("Full set: +1 minion; +10% minion damage.");
        t.add("Double-tap Down Arrow: Wulfrum Bastion.");
        t.add("Costs 1 scrap. Lasts 30 seconds; each hit removes 2 seconds.");
        t.add("+12 armor; 10% damage reduction. Use to fire energy bursts.");
    }
}
