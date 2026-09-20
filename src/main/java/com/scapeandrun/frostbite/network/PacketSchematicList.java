package com.scapeandrun.frostbite.network;

import com.scapeandrun.frostbite.client.gui.GuiRToolSchematics;
import com.scapeandrun.frostbite.schematic.SchematicLibrary;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

public class PacketSchematicList implements IMessage {
    private final List<SchematicLibrary.Summary> entries = new ArrayList<>();

    public PacketSchematicList() {}

    public PacketSchematicList(List<SchematicLibrary.Summary> entries) {
        this.entries.addAll(entries.subList(0, Math.min(128, entries.size())));
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entries.clear();
        int count = Math.min(128, buf.readUnsignedByte());
        for (int i = 0; i < count; i++) {
            entries.add(
                    new SchematicLibrary.Summary(
                            ByteBufUtils.readUTF8String(buf),
                            ByteBufUtils.readUTF8String(buf),
                            ByteBufUtils.readUTF8String(buf),
                            buf.readUnsignedShort(),
                            buf.readUnsignedShort(),
                            buf.readUnsignedShort(),
                            buf.readUnsignedShort()));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(entries.size());
        for (SchematicLibrary.Summary entry : entries) {
            ByteBufUtils.writeUTF8String(buf, entry.fileName);
            ByteBufUtils.writeUTF8String(buf, entry.displayName);
            ByteBufUtils.writeUTF8String(buf, entry.author);
            buf.writeShort(entry.blocks);
            buf.writeShort(entry.sizeX);
            buf.writeShort(entry.sizeY);
            buf.writeShort(entry.sizeZ);
        }
    }

    public static class Handler implements IMessageHandler<PacketSchematicList, IMessage> {
        @Override
        public IMessage onMessage(PacketSchematicList message, MessageContext ctx) {
            List<SchematicLibrary.Summary> copy = new ArrayList<>(message.entries);
            Minecraft.getMinecraft().addScheduledTask(() -> GuiRToolSchematics.receive(copy));
            return null;
        }
    }
}
