package com.exoarsenal.network;

import com.exoarsenal.event.KXSystems;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketKXAction implements IMessage {
    public static final int OVERLOAD = 0,
            SELF_DESTRUCT = 1,
            CHAINSAW = 2,
            EDITOR_BEGIN = 3,
            EDITOR_END = 4,
            MUTATION = 5;
    private int action, value;

    public PacketKXAction() {}

    public PacketKXAction(int action, int value) {
        this.action = action;
        this.value = value;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        action = buf.readUnsignedByte();
        value = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(action);
        buf.writeInt(value);
    }

    public static class Handler implements IMessageHandler<PacketKXAction, IMessage> {
        @Override
        public IMessage onMessage(PacketKXAction message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> handle(player, message));
            return null;
        }

        private static void handle(EntityPlayerMP player, PacketKXAction message) {
            switch (message.action) {
                case OVERLOAD:
                    KXSystems.toggleOverload(player);
                    break;
                case SELF_DESTRUCT:
                    KXSystems.selfDestruct(player, message.value != 0);
                    break;
                case CHAINSAW:
                    KXSystems.chainsaw(player);
                    break;
                case EDITOR_BEGIN:
                    KXSystems.beginEditor(player, message.value);
                    break;
                case EDITOR_END:
                    KXSystems.endEditor(player);
                    break;
                case MUTATION:
                    KXSystems.applyMutation(player, message.value);
                    break;
                default:
            }
        }
    }
}
