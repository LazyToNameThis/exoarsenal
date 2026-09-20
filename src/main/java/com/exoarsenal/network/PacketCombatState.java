package com.exoarsenal.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.*;

public final class PacketCombatState implements IMessage {
    private int id, dimension, action, profile, age, combo;
    private float stamina;

    public PacketCombatState() {}

    public static void send(
            EntityPlayerMP player, int action, int profile, int age, int combo, float stamina) {
        PacketCombatState p = new PacketCombatState();
        p.id = player.getEntityId();
        p.dimension = player.dimension;
        p.action = action;
        p.profile = profile;
        p.age = age;
        p.combo = combo;
        p.stamina = stamina;
        ModNetwork.CHANNEL.sendToAllAround(
                p,
                new NetworkRegistry.TargetPoint(
                        player.dimension, player.posX, player.posY, player.posZ, 64));
    }

    @Override
    public void fromBytes(ByteBuf b) {
        id = b.readInt();
        dimension = b.readInt();
        action = b.readUnsignedByte();
        profile = b.readUnsignedByte();
        age = b.readUnsignedByte();
        combo = b.readUnsignedByte();
        stamina = b.readFloat();
    }

    @Override
    public void toBytes(ByteBuf b) {
        b.writeInt(id);
        b.writeInt(dimension);
        b.writeByte(action);
        b.writeByte(profile);
        b.writeByte(age);
        b.writeByte(combo);
        b.writeFloat(stamina);
    }

    public static final class Handler implements IMessageHandler<PacketCombatState, IMessage> {
        @Override
        public IMessage onMessage(PacketCombatState p, MessageContext c) {
            Minecraft.getMinecraft()
                    .addScheduledTask(
                            () -> {
                                if (Minecraft.getMinecraft().world == null
                                        || Minecraft.getMinecraft().world.provider.getDimension()
                                                != p.dimension) return;
                                Entity e = Minecraft.getMinecraft().world.getEntityByID(p.id);
                                if (e == null) return;
                                NBTTagCompound n = e.getEntityData();
                                n.setInteger("FrostCombatAction", p.action);
                                n.setInteger("FrostCombatProfile", p.profile);
                                n.setLong("FrostCombatStart", e.world.getTotalWorldTime() - p.age);
                                n.setInteger("FrostCombatCombo", p.combo);
                                n.setFloat("FrostStamina", p.stamina);
                            });
            return null;
        }
    }
}
