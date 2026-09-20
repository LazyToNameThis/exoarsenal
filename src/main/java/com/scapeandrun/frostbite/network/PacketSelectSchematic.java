package com.scapeandrun.frostbite.network;

import com.scapeandrun.frostbite.item.ItemRTool;
import com.scapeandrun.frostbite.schematic.SchematicLibrary;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSelectSchematic implements IMessage {
    private String fileName = "";

    public PacketSelectSchematic() {}

    public PacketSelectSchematic(String fileName) {
        this.fileName = fileName == null ? "" : fileName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        fileName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, fileName);
    }

    public static class Handler implements IMessageHandler<PacketSelectSchematic, IMessage> {
        @Override
        public IMessage onMessage(PacketSelectSchematic message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> select(player, message.fileName));
            return null;
        }

        private static void select(EntityPlayerMP player, String fileName) {
            ItemStack tool = player.getHeldItemMainhand();
            if (!(tool.getItem() instanceof ItemRTool)) tool = player.getHeldItemOffhand();
            if (!(tool.getItem() instanceof ItemRTool)
                    || ItemRTool.getForm(tool) != ItemRTool.BUILD) return;
            SchematicLibrary.Data schematic = SchematicLibrary.load(player.getServer(), fileName);
            if (schematic == null) {
                player.sendStatusMessage(
                        new TextComponentTranslation("status.exoarsenal.rtool_schematic_failed"),
                        true);
                return;
            }
            ItemRTool.setSchematic(tool, schematic);
            player.inventory.markDirty();
            player.inventoryContainer.detectAndSendChanges();
            player.sendStatusMessage(
                    new TextComponentTranslation(
                            "status.exoarsenal.rtool_schematic_loaded",
                            schematic.summary.displayName,
                            schematic.summary.blocks),
                    true);
        }
    }
}
