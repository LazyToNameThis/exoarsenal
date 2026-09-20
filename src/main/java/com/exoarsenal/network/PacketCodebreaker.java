package com.exoarsenal.network;

import com.exoarsenal.entity.EntityDraedon;
import com.exoarsenal.expedition.CodebreakerContainer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.*;

public final class PacketCodebreaker implements IMessage {
    private BlockPos pos;
    private int choice;

    public PacketCodebreaker() {}

    public PacketCodebreaker(BlockPos p, int c) {
        pos = p;
        choice = c;
    }

    @Override
    public void toBytes(ByteBuf b) {
        b.writeLong(pos.toLong());
        b.writeByte(choice);
    }

    @Override
    public void fromBytes(ByteBuf b) {
        pos = BlockPos.fromLong(b.readLong());
        choice = b.readUnsignedByte();
    }

    public static final class Handler implements IMessageHandler<PacketCodebreaker, IMessage> {
        @Override
        public IMessage onMessage(PacketCodebreaker m, MessageContext c) {
            EntityPlayerMP p = c.getServerHandler().player;
            p.getServerWorld()
                    .addScheduledTask(
                            () -> {
                                if (m.choice < 3
                                        && p.openContainer instanceof CodebreakerContainer
                                        && ((CodebreakerContainer) p.openContainer)
                                                .pos.equals(m.pos)
                                        && p.openContainer.canInteractWith(p))
                                    EntityDraedon.select(p.world, m.pos, p, m.choice);
                            });
            return null;
        }
    }
}
