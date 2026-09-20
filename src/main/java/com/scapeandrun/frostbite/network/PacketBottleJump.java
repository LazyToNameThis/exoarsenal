package com.scapeandrun.frostbite.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.*;

public final class PacketBottleJump implements IMessage {
    @Override
    public void fromBytes(ByteBuf bytes) {}

    @Override
    public void toBytes(ByteBuf bytes) {}

    public static final class Handler implements IMessageHandler<PacketBottleJump, IMessage> {
        @Override
        public IMessage onMessage(PacketBottleJump packet, MessageContext context) {
            net.minecraft.entity.player.EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld()
                    .addScheduledTask(
                            () ->
                                    com.scapeandrun.frostbite.expedition.ExplorationEvents.jump(
                                            player));
            return null;
        }
    }
}
