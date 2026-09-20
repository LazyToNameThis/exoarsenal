package com.scapeandrun.frostbite.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.*;

public final class PacketPrebossMana implements IMessage {
    private int current, maximum, focus;

    public PacketPrebossMana() {}

    public PacketPrebossMana(int current, int maximum, int focus) {
        this.current = current;
        this.maximum = maximum;
        this.focus = focus;
    }

    @Override
    public void fromBytes(ByteBuf data) {
        current = data.readUnsignedShort();
        maximum = data.readUnsignedShort();
        focus = data.readUnsignedByte();
    }

    @Override
    public void toBytes(ByteBuf data) {
        data.writeShort(current);
        data.writeShort(maximum);
        data.writeByte(focus);
    }

    public static final class Handler implements IMessageHandler<PacketPrebossMana, IMessage> {
        @Override
        public IMessage onMessage(PacketPrebossMana packet, MessageContext context) {
            net.minecraft.client.Minecraft.getMinecraft()
                    .addScheduledTask(
                            () ->
                                    com.scapeandrun.frostbite.expedition.client.PrebossClient
                                            .setMana(packet.current, packet.maximum, packet.focus));
            return null;
        }
    }
}
