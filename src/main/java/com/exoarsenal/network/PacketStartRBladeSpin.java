package com.exoarsenal.network;

import com.exoarsenal.item.ItemRBlade;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketStartRBladeSpin implements IMessage {
    private int hand;

    public PacketStartRBladeSpin() {}

    public PacketStartRBladeSpin(EnumHand hand) {
        this.hand = hand.ordinal();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        hand = buf.readUnsignedByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(hand);
    }

    public static class Handler implements IMessageHandler<PacketStartRBladeSpin, IMessage> {
        @Override
        public IMessage onMessage(PacketStartRBladeSpin message, MessageContext ctx) {
            ctx.getServerHandler()
                    .player
                    .getServerWorld()
                    .addScheduledTask(
                            () -> {
                                if (com.exoarsenal.combat.WeaponCombat.busy(
                                        ctx.getServerHandler().player)) return;
                                EnumHand hand =
                                        message.hand == EnumHand.OFF_HAND.ordinal()
                                                ? EnumHand.OFF_HAND
                                                : EnumHand.MAIN_HAND;
                                ItemRBlade.beginSpinFromInput(ctx.getServerHandler().player, hand);
                            });
            return null;
        }
    }
}
