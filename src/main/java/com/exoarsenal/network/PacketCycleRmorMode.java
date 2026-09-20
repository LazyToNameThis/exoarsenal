package com.exoarsenal.network;

import com.exoarsenal.event.RmorEventHandler;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketCycleRmorMode implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}

    public static class Handler implements IMessageHandler<PacketCycleRmorMode, IMessage> {
        @Override
        public IMessage onMessage(PacketCycleRmorMode message, MessageContext ctx) {
            ctx.getServerHandler()
                    .player
                    .getServerWorld()
                    .addScheduledTask(
                            () -> RmorEventHandler.cycleMode(ctx.getServerHandler().player));
            return null;
        }
    }
}
