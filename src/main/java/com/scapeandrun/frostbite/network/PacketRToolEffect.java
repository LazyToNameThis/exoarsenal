package com.scapeandrun.frostbite.network;

import com.scapeandrun.frostbite.client.ClientEquipmentHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

public class PacketRToolEffect implements IMessage {
    public static final int HELIX = 0;
    public static final int SAW = 1;
    public static final int BUILD = 2;
    public static final int SHOTGUN = 3;
    public static final int GUN_LASER = 4;
    public static final int GUN_LASER_X10 = 5;
    public static final int GUN_SHOTGUN = 6;
    public static final int GUN_SHOTGUN_X10 = 7;
    public static final int GUN_CANNON = 8;
    public static final int GUN_CANNON_X10 = 9;
    public static final int GUN_NET = 10;
    public static final int SCOUT_RAILGUN = 11;
    public static final int KX_SCYTHE = 12;
    public static final int GUN_KX = 13;
    public static final int GUN_KX_SPREAD = 14;
    public static final int GUN_KX_RAIL = 15;
    private int sourceId;
    private int type;
    private final List<Vec3d> targets = new ArrayList<>();

    public PacketRToolEffect() {}

    private PacketRToolEffect(int sourceId, int type, List<Vec3d> targets) {
        this.sourceId = sourceId;
        this.type = type;
        this.targets.addAll(targets.subList(0, Math.min(12, targets.size())));
    }

    public static void send(EntityPlayer source, int type, List<Vec3d> targets) {
        PacketRToolEffect packet = new PacketRToolEffect(source.getEntityId(), type, targets);
        ModNetwork.CHANNEL.sendToAllAround(
                packet,
                new NetworkRegistry.TargetPoint(
                        source.dimension, source.posX, source.posY, source.posZ, 96.0D));
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        sourceId = buf.readInt();
        type = buf.readUnsignedByte();
        int count = Math.min(12, buf.readUnsignedByte());
        targets.clear();
        for (int i = 0; i < count; i++)
            targets.add(new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble()));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(sourceId);
        buf.writeByte(type);
        buf.writeByte(targets.size());
        for (Vec3d target : targets) {
            buf.writeDouble(target.x);
            buf.writeDouble(target.y);
            buf.writeDouble(target.z);
        }
    }

    public static class Handler implements IMessageHandler<PacketRToolEffect, IMessage> {
        @Override
        public IMessage onMessage(PacketRToolEffect message, MessageContext ctx) {
            Minecraft.getMinecraft()
                    .addScheduledTask(
                            () ->
                                    ClientEquipmentHandler.acceptEffect(
                                            message.sourceId,
                                            message.type,
                                            new ArrayList<>(message.targets)));
            return null;
        }
    }
}
