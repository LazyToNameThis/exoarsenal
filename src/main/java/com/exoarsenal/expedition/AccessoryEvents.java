package com.exoarsenal.expedition;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.network.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Mod.EventBusSubscriber(modid = ExoArsenal.MODID)
public final class AccessoryEvents {
    private static List<Bonus> bonuses;

    private static final class Bonus {
        final Item item;
        final String attribute;
        final UUID id;
        final double amount;
        final int operation;

        Bonus(Item item, String attribute, AttributeModifier source) {
            this.item = item;
            this.attribute = attribute;
            amount = source.getAmount();
            operation = source.getOperation();
            id =
                    UUID.nameUUIDFromBytes(
                            ("exoarsenal.accessory:" + item.getRegistryName() + ":" + attribute)
                                    .getBytes(StandardCharsets.UTF_8));
        }
    }

    private static List<Bonus> bonuses() {
        if (bonuses == null) {
            bonuses = new ArrayList<>();
            for (Item item : ExpeditionContent.ITEMS)
                if (item instanceof ExplorationAccessory)
                    for (Map.Entry<String, AttributeModifier> entry :
                            item.getItemAttributeModifiers(EntityEquipmentSlot.OFFHAND).entries())
                        bonuses.add(new Bonus(item, entry.getKey(), entry.getValue()));
        }
        return bonuses;
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (event.phase != TickEvent.Phase.END || player.world.isRemote) return;
        AccessoryInventory inventory = AccessoryInventory.get(player);
        if (inventory == null) return;

        for (Bonus bonus : bonuses()) {
            boolean active =
                    AccessoryInventory.has(player, bonus.item)
                            && player.getHeldItemOffhand().getItem() != bonus.item;
            IAttributeInstance attribute =
                    player.getAttributeMap().getAttributeInstanceByName(bonus.attribute);
            if (attribute == null) continue;
            AttributeModifier old = attribute.getModifier(bonus.id);
            if (!active && old != null) attribute.removeModifier(bonus.id);
            else if (active && old == null)
                attribute.applyModifier(
                        new AttributeModifier(
                                        bonus.id,
                                        "Equipped accessory",
                                        bonus.amount,
                                        bonus.operation)
                                .setSaved(false));
        }
        if (player instanceof EntityPlayerMP && player.ticksExisted % 10 == 0) {
            NBTTagCompound tag = inventory.serializeNBT();
            if (!tag.equals(player.getEntityData().getCompoundTag("LastAccessorySync"))
                    || player.ticksExisted < 30) {
                ModNetwork.CHANNEL.sendTo(
                        new PacketAccessories.State(tag), (EntityPlayerMP) player);
                player.getEntityData().setTag("LastAccessorySync", tag.copy());
            }
        }
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone event) {
        AccessoryInventory old = AccessoryInventory.get(event.getOriginal()),
                next = AccessoryInventory.get(event.getEntityPlayer());
        if (old != null && next != null) next.deserializeNBT(old.serializeNBT());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void drops(PlayerDropsEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player.world.isRemote || player.world.getGameRules().getBoolean("keepInventory"))
            return;
        AccessoryInventory inventory = AccessoryInventory.get(player);
        if (inventory == null) return;
        for (int i = 0; i < inventory.getSlots(); i++) {
            net.minecraft.item.ItemStack stack = inventory.extractItem(i, 1, false);
            if (!stack.isEmpty())
                event.getDrops()
                        .add(
                                new EntityItem(
                                        player.world,
                                        player.posX,
                                        player.posY,
                                        player.posZ,
                                        stack));
        }
    }
}
