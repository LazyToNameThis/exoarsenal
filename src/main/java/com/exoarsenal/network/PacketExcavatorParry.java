package com.exoarsenal.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.*;

public final class PacketExcavatorParry implements IMessage {
    private int dimension, player, type;
    private double x, y, z, dx, dy, dz;

    public PacketExcavatorParry() {}

    public static void send(Entity boss, EntityPlayer player, Vec3d at, Vec3d direction, int type) {
        PacketExcavatorParry p = new PacketExcavatorParry();
        p.dimension = boss.dimension;
        p.player = player.getEntityId();
        p.type = type;
        p.x = at.x;
        p.y = at.y;
        p.z = at.z;
        p.dx = direction.x;
        p.dy = direction.y;
        p.dz = direction.z;
        ModNetwork.CHANNEL.sendToAllAround(
                p, new NetworkRegistry.TargetPoint(boss.dimension, at.x, at.y, at.z, 96));
    }

    @Override
    public void toBytes(ByteBuf b) {
        b.writeInt(dimension);
        b.writeInt(player);
        b.writeByte(type);
        b.writeDouble(x);
        b.writeDouble(y);
        b.writeDouble(z);
        b.writeDouble(dx);
        b.writeDouble(dy);
        b.writeDouble(dz);
    }

    @Override
    public void fromBytes(ByteBuf b) {
        dimension = b.readInt();
        player = b.readInt();
        type = b.readUnsignedByte();
        x = b.readDouble();
        y = b.readDouble();
        z = b.readDouble();
        dx = b.readDouble();
        dy = b.readDouble();
        dz = b.readDouble();
    }

    public static final class Handler implements IMessageHandler<PacketExcavatorParry, IMessage> {
        @Override
        public IMessage onMessage(PacketExcavatorParry p, MessageContext ctx) {
            Minecraft.getMinecraft()
                    .addScheduledTask(
                            () -> {
                                Minecraft mc = Minecraft.getMinecraft();
                                if (mc.world == null
                                        || mc.world.provider.getDimension() != p.dimension
                                        || p.type > 1
                                        || !Double.isFinite(p.x + p.y + p.z + p.dx + p.dy + p.dz))
                                    return;
                                Entity player = mc.world.getEntityByID(p.player);
                                if (player != null && p.type == 0)
                                    player.getEntityData()
                                            .setLong(
                                                    "ExcavatorParryBrace",
                                                    mc.world.getTotalWorldTime());
                                if (player == mc.player && p.type == 0)
                                    com.exoarsenal.client.ExcavatorParryScene.begin(
                                            new Vec3d(p.x, p.y, p.z));
                                com.exoarsenal.client.ExcavatorParryEffects.add(
                                        new Vec3d(p.x, p.y, p.z),
                                        new Vec3d(p.dx, p.dy, p.dz),
                                        p.type);
                            });
            return null;
        }
    }
}
