package com.exoarsenal.network;

import com.exoarsenal.event.RmorEventHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketRmorDefenseState implements IMessage {
    private boolean enabled;

    public PacketRmorDefenseState() {}

    public PacketRmorDefenseState(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        enabled = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(enabled);
    }

    public static class Handler implements IMessageHandler<PacketRmorDefenseState, IMessage> {
        @Override
        public IMessage onMessage(PacketRmorDefenseState message, MessageContext ctx) {
            Minecraft.getMinecraft()
                    .addScheduledTask(
                            () -> {
                                if (Minecraft.getMinecraft().player != null) {
                                    RmorEventHandler.setDefenseForm(
                                            Minecraft.getMinecraft().player, message.enabled);
                                }
                            });
            return null;
        }
    }
}
