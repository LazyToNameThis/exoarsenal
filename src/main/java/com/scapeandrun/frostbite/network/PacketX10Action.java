package com.scapeandrun.frostbite.network;

import com.scapeandrun.frostbite.event.RmorEventHandler;
import com.scapeandrun.frostbite.event.X10Systems;
import com.scapeandrun.frostbite.item.ItemX10Blade;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketX10Action implements IMessage {
    public static final int BLADE_FORM = 0,
            BLADE_ABILITY = 1,
            DEFENSE_WEAPON = 2,
            BELT_STORE = 3,
            BELT_SWAP = 4,
            SYSTEM_TOGGLE = 5,
            JET = 6;
    private int action;
    private int value;

    public PacketX10Action() {}

    public PacketX10Action(int action, int value) {
        this.action = action;
        this.value = value;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        action = buf.readUnsignedByte();
        value = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(action);
        buf.writeByte(value);
    }

    public static class Handler implements IMessageHandler<PacketX10Action, IMessage> {
        @Override
        public IMessage onMessage(PacketX10Action message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld()
                    .addScheduledTask(() -> handle(player, message.action, message.value));
            return null;
        }

        private static void handle(EntityPlayerMP player, int action, int value) {
            if (com.scapeandrun.frostbite.combat.WeaponCombat.busy(player)
                    && (action == BLADE_FORM
                            || action == BLADE_ABILITY
                            || action == BELT_SWAP
                            || action == BELT_STORE)) return;
            ItemStack held = player.getHeldItemMainhand();
            if (!(held.getItem() instanceof ItemX10Blade)) held = player.getHeldItemOffhand();
            if (action == BLADE_FORM && held.getItem() instanceof ItemX10Blade) {
                ItemX10Blade.cycleForm(held);
                player.sendStatusMessage(
                        new TextComponentTranslation(
                                "status.exoarsenal.x10_blade_form", ItemX10Blade.formName(held)),
                        true);
            } else if (action == BLADE_ABILITY && held.getItem() instanceof ItemX10Blade) {
                ItemX10Blade.cycleAbility(held);
                player.sendStatusMessage(
                        new TextComponentTranslation(
                                "status.exoarsenal.x10_blade_ability",
                                ItemX10Blade.abilityName(held)),
                        true);
            } else if (action == DEFENSE_WEAPON) RmorEventHandler.cycleDefenseWeapon(player);
            else if (action == BELT_STORE) X10Systems.storeHeld(player);
            else if (action == BELT_SWAP) X10Systems.swapBelt(player, value);
            else if (action == SYSTEM_TOGGLE) X10Systems.toggle(player, value & 255);
            else if (action == JET) X10Systems.jet(player);
        }
    }
}
