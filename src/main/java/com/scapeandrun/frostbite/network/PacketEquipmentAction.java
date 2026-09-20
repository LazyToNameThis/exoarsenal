package com.scapeandrun.frostbite.network;

import com.scapeandrun.frostbite.event.RmorEventHandler;
import com.scapeandrun.frostbite.item.ItemRTool;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketEquipmentAction implements IMessage {
    public static final int TOGGLE_DEFENSE = 0;
    public static final int SHOTGUN_LEFT = 1;
    public static final int SHOTGUN_RIGHT = 2;
    public static final int DEFENSE_JUMP = 3;
    public static final int RTOOL_FORM = 4;
    public static final int RTOOL_MODE = 5;
    public static final int RTOOL_USE = 6;
    public static final int RTOOL_STOP = 7;
    private int action;

    public PacketEquipmentAction() {}

    public PacketEquipmentAction(int action) {
        this.action = action;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        action = buf.readUnsignedByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(action);
    }

    public static class Handler implements IMessageHandler<PacketEquipmentAction, IMessage> {
        @Override
        public IMessage onMessage(PacketEquipmentAction message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> handle(player, message.action));
            return null;
        }

        private static void handle(EntityPlayerMP player, int action) {
            if (action == 40 || action == 41) {
                com.scapeandrun.frostbite.expedition.WulfrumCombat.request(player, action - 40);
                return;
            }
            if (action == 8) {
                com.scapeandrun.frostbite.expedition.ExpeditionEvents.fire(player);
                return;
            }
            if (action == TOGGLE_DEFENSE
                    && com.scapeandrun.frostbite.expedition.WulfrumArmor.full(player)) {
                com.scapeandrun.frostbite.expedition.ExpeditionEvents.toggle(player);
                return;
            }
            if (com.scapeandrun.frostbite.combat.WeaponCombat.busy(player) && action != RTOOL_STOP)
                return;
            if (action == TOGGLE_DEFENSE) {
                RmorEventHandler.toggleDefenseForm(player);
                ModNetwork.CHANNEL.sendTo(
                        new PacketRmorDefenseState(RmorEventHandler.isDefenseForm(player)), player);
                return;
            }
            if (action >= SHOTGUN_LEFT && action <= DEFENSE_JUMP) {
                RmorEventHandler.defenseAction(player, action - SHOTGUN_LEFT);
                return;
            }
            ItemStack tool = player.getHeldItemMainhand();
            if (!(tool.getItem() instanceof ItemRTool)) tool = player.getHeldItemOffhand();
            if (!(tool.getItem() instanceof ItemRTool)) return;
            if (action == RTOOL_FORM) {
                ItemRTool.cycleForm(tool);
                player.sendStatusMessage(
                        new TextComponentTranslation(
                                "status.exoarsenal.rtool_form", ItemRTool.formName(tool)),
                        true);
            } else if (action == RTOOL_MODE) {
                ItemRTool.cycleMode(tool);
                player.sendStatusMessage(
                        new TextComponentTranslation(
                                "status.exoarsenal.rtool_mode", ItemRTool.modeName(tool)),
                        true);
            } else if (action == RTOOL_USE) ItemRTool.usePulse(player);
            else if (action == RTOOL_STOP) ItemRTool.stopUse(tool);
        }
    }
}
