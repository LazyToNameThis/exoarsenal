package com.scapeandrun.frostbite.network;

import com.scapeandrun.frostbite.item.ItemRBlade;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketRBladeAirAttack implements IMessage {
    private int hand;

    public PacketRBladeAirAttack() {}

    public PacketRBladeAirAttack(EnumHand hand) {
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

    public static class Handler implements IMessageHandler<PacketRBladeAirAttack, IMessage> {
        @Override
        public IMessage onMessage(PacketRBladeAirAttack message, MessageContext ctx) {
            ctx.getServerHandler()
                    .player
                    .getServerWorld()
                    .addScheduledTask(
                            () -> {
                                EnumHand hand =
                                        message.hand == EnumHand.OFF_HAND.ordinal()
                                                ? EnumHand.OFF_HAND
                                                : EnumHand.MAIN_HAND;
                                if (hand == EnumHand.MAIN_HAND)
                                    com.scapeandrun.frostbite.combat.WeaponCombat.request(
                                            ctx.getServerHandler().player, 1, 0, 0);
                            });
            return null;
        }
    }
}
