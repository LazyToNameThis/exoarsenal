package com.scapeandrun.frostbite.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.*;

public final class PacketWulfrumState implements IMessage {
    public int id, move, age, combo, target;
    public boolean android;

    public PacketWulfrumState() {}

    public static void send(
            EntityPlayer p, boolean android, int move, int age, int combo, int target) {
        PacketWulfrumState packet = new PacketWulfrumState();
        packet.id = p.getEntityId();
        packet.android = android;
        packet.move = move;
        packet.age = age;
        packet.combo = combo;
        packet.target = target;
        ModNetwork.CHANNEL.sendToAllAround(
                packet, new NetworkRegistry.TargetPoint(p.dimension, p.posX, p.posY, p.posZ, 96));
    }

    public void toBytes(ByteBuf b) {
        b.writeInt(id);
        b.writeBoolean(android);
        b.writeInt(move);
        b.writeInt(age);
        b.writeInt(combo);
        b.writeInt(target);
    }

    public void fromBytes(ByteBuf b) {
        id = b.readInt();
        android = b.readBoolean();
        move = b.readInt();
        age = b.readInt();
        combo = b.readInt();
        target = b.readInt();
    }

    public static final class Handler implements IMessageHandler<PacketWulfrumState, IMessage> {
        public IMessage onMessage(PacketWulfrumState p, MessageContext c) {
            net.minecraft.client.Minecraft.getMinecraft()
                    .addScheduledTask(
                            () ->
                                    com.scapeandrun.frostbite.expedition.client.WulfrumArsenalClient
                                            .receive(p));
            return null;
        }
    }
}
