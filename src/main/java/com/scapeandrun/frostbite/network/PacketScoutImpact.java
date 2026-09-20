package com.scapeandrun.frostbite.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class PacketScoutImpact implements IMessage {
    private int dimension, type;
    private double x, y, z;

    public PacketScoutImpact() {}

    public static void send(Entity source, Vec3d at, int type) {
        if (source.world.isRemote) return;
        PacketScoutImpact p = new PacketScoutImpact();
        p.dimension = source.dimension;
        p.type = type;
        p.x = at.x;
        p.y = at.y;
        p.z = at.z;
        ModNetwork.CHANNEL.sendToAllAround(
                p, new NetworkRegistry.TargetPoint(source.dimension, at.x, at.y, at.z, 64));
    }

    @Override
    public void fromBytes(ByteBuf b) {
        dimension = b.readInt();
        type = b.readUnsignedByte();
        x = b.readDouble();
        y = b.readDouble();
        z = b.readDouble();
    }

    @Override
    public void toBytes(ByteBuf b) {
        b.writeInt(dimension);
        b.writeByte(type);
        b.writeDouble(x);
        b.writeDouble(y);
        b.writeDouble(z);
    }

    public static final class Handler implements IMessageHandler<PacketScoutImpact, IMessage> {
        @Override
        public IMessage onMessage(PacketScoutImpact p, MessageContext ctx) {
            Minecraft.getMinecraft()
                    .addScheduledTask(
                            () -> {
                                Minecraft mc = Minecraft.getMinecraft();
                                if (mc.world != null
                                        && mc.world.provider.getDimension() == p.dimension
                                        && p.type <= 3
                                        && Double.isFinite(p.x)
                                        && Double.isFinite(p.y)
                                        && Double.isFinite(p.z))
                                    com.scapeandrun.frostbite.client.particle.ScoutParticles.burst(
                                            p.type, new Vec3d(p.x, p.y, p.z));
                            });
            return null;
        }
    }
}
