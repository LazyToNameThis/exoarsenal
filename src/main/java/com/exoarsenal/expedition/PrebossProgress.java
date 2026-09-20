package com.exoarsenal.expedition;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.player.*;
import net.minecraft.nbt.NBTTagCompound;
import java.util.UUID;

public final class PrebossProgress {
    public static final String KEY = "ExoArsenalPreboss";
    private static final UUID HEALTH = UUID.fromString("06cd393d-e605-423f-945e-4f95d8099e5f");

    public static int minionLimit(EntityPlayer player) {
        return 1
                + (WulfrumArmor.full(player)
                                || VictideArmor.summoner(player)
                                || player.getItemStackFromSlot(
                                                        net.minecraft.inventory.EntityEquipmentSlot
                                                                .CHEST)
                                                .getItem()
                                        == PrebossContent.FLINX_COAT
                        ? 1
                        : 0);
    }

    public static NBTTagCompound data(EntityPlayer player) {
        NBTTagCompound root = player.getEntityData();
        if (!root.hasKey(EntityPlayer.PERSISTED_NBT_TAG, 10))
            root.setTag(EntityPlayer.PERSISTED_NBT_TAG, new NBTTagCompound());
        NBTTagCompound persistent = root.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        if (!persistent.hasKey(KEY, 10)) {
            NBTTagCompound initial = new NBTTagCompound();
            initial.setInteger("Mana", 20);
            persistent.setTag(KEY, initial);
        }
        return persistent.getCompoundTag(KEY);
    }

    public static int maximum(EntityPlayer player) {
        return PrebossRules.manaMaximum(
                data(player).getInteger("ManaCrystals"),
                AccessoryInventory.has(player, PrebossContent.STARPOWER_BAND));
    }

    public static boolean spend(EntityPlayer player, int amount) {
        if (amount <= 0 || player.isCreative()) return true;
        NBTTagCompound data = data(player);
        if (data.getInteger("Mana") < amount) return false;
        data.setInteger("Mana", data.getInteger("Mana") - amount);
        data.setInteger("RegenDelay", 40);
        sync(player);
        return true;
    }

    public static boolean manaCrystal(EntityPlayer player) {
        NBTTagCompound data = data(player);
        int count = data.getInteger("ManaCrystals");
        if (count >= 9) return false;
        data.setInteger("ManaCrystals", count + 1);
        data.setInteger("Mana", Math.min(maximum(player), data.getInteger("Mana") + 20));
        sync(player);
        return true;
    }

    public static boolean lifeCrystal(EntityPlayer player) {
        NBTTagCompound data = data(player);
        int count = data.getInteger("LifeCrystals");
        if (count >= 15) return false;
        data.setInteger("LifeCrystals", count + 1);
        applyHealth(player);
        player.heal(2);
        return true;
    }

    public static void applyHealth(EntityPlayer player) {
        double amount = PrebossRules.lifeBonus(data(player).getInteger("LifeCrystals"));
        IAttributeInstance attribute =
                player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        AttributeModifier old = attribute.getModifier(HEALTH);
        if (old != null && old.getAmount() != amount) {
            attribute.removeModifier(HEALTH);
            old = null;
        }
        if (amount > 0 && old == null)
            attribute.applyModifier(
                    new AttributeModifier(HEALTH, "Life Crystal upgrades", amount, 0)
                            .setSaved(false));
    }

    public static void sync(EntityPlayer player) {
        if (player instanceof EntityPlayerMP)
            com.exoarsenal.network.ModNetwork.CHANNEL.sendTo(
                    new com.exoarsenal.network.PacketPrebossMana(
                            data(player).getInteger("Mana"),
                            maximum(player),
                            player.getEntityData().getInteger("ExoArsenalRogueFocus")),
                    (EntityPlayerMP) player);
    }
}
