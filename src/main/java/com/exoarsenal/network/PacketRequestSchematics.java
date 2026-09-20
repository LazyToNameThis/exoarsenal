package com.exoarsenal.network;

import com.exoarsenal.schematic.SchematicLibrary;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import com.exoarsenal.item.ItemRTool;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketRequestSchematics implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}

    public static class Handler implements IMessageHandler<PacketRequestSchematics, IMessage> {
        @Override
        public IMessage onMessage(PacketRequestSchematics message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld()
                    .addScheduledTask(
                            () -> {
                                ItemStack tool = player.getHeldItemMainhand();
                                if (!(tool.getItem() instanceof ItemRTool))
                                    tool = player.getHeldItemOffhand();
                                if (!(tool.getItem() instanceof ItemRTool)
                                        || ItemRTool.getForm(tool) != ItemRTool.BUILD) return;
                                ModNetwork.CHANNEL.sendTo(
                                        new PacketSchematicList(
                                                SchematicLibrary.list(player.getServer())),
                                        player);
                            });
            return null;
        }
    }
}
