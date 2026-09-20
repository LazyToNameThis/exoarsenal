package com.exoarsenal.network;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.expedition.AccessoryInventory;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.*;

public final class PacketAccessories implements IMessage {
    @Override
    public void fromBytes(ByteBuf data) {}

    @Override
    public void toBytes(ByteBuf data) {}

    public static final class Open implements IMessageHandler<PacketAccessories, IMessage> {
        @Override
        public IMessage onMessage(PacketAccessories packet, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld()
                    .addScheduledTask(
                            () -> {
                                if (!player.isEntityAlive()
                                        || player.isSpectator()
                                        || player.openContainer != player.inventoryContainer)
                                    return;
                                AccessoryInventory inventory = AccessoryInventory.get(player);
                                if (inventory == null) return;
                                ModNetwork.CHANNEL.sendTo(
                                        new State(inventory.serializeNBT()), player);
                                player.openGui(ExoArsenal.INSTANCE, 0, player.world, 0, 0, 0);
                            });
            return null;
        }
    }

    public static final class State implements IMessage {
        private NBTTagCompound tag;

        public State() {}

        public State(NBTTagCompound tag) {
            this.tag = tag;
        }

        @Override
        public void fromBytes(ByteBuf data) {
            tag = ByteBufUtils.readTag(data);
        }

        @Override
        public void toBytes(ByteBuf data) {
            ByteBufUtils.writeTag(data, tag);
        }

        public static final class Handler implements IMessageHandler<State, IMessage> {
            @Override
            public IMessage onMessage(State packet, MessageContext context) {
                net.minecraft.client.Minecraft.getMinecraft()
                        .addScheduledTask(
                                () -> {
                                    net.minecraft.entity.player.EntityPlayer player =
                                            net.minecraft.client.Minecraft.getMinecraft().player;
                                    if (player != null && packet.tag != null) {
                                        AccessoryInventory inventory =
                                                AccessoryInventory.get(player);
                                        if (inventory != null) inventory.deserializeNBT(packet.tag);
                                    }
                                });
                return null;
            }
        }
    }
}
