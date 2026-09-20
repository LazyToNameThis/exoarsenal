package com.scapeandrun.frostbite.network;

import com.scapeandrun.frostbite.world.FrostbiteWorldSettings;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.*;

public final class PacketDifficulty implements IMessage {
    private int action;

    public PacketDifficulty() {}

    public PacketDifficulty(int action) {
        this.action = action;
    }

    public void fromBytes(ByteBuf b) {
        action = b.readUnsignedByte();
    }

    public void toBytes(ByteBuf b) {
        b.writeByte(action);
    }

    public static final class Handler implements IMessageHandler<PacketDifficulty, IMessage> {
        public IMessage onMessage(PacketDifficulty message, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld()
                    .addScheduledTask(
                            () -> {
                                if (message.action > 3 || !player.isEntityAlive()) return;
                                MinecraftServer server = player.getServer();
                                boolean owner =
                                        server.isSinglePlayer()
                                                && player.getName()
                                                        .equalsIgnoreCase(server.getServerOwner());
                                boolean allowed =
                                        owner || player.canUseCommand(2, "frostbiteexpert");
                                boolean fighting = false;
                                for (WorldServer world : server.worlds)
                                    for (Entity e : world.loadedEntityList)
                                        if (e instanceof EntityLivingBase
                                                && !((EntityLivingBase) e).isNonBoss()
                                                && !e.isDead) {
                                            fighting = true;
                                            break;
                                        }
                                FrostbiteWorldSettings settings =
                                        FrostbiteWorldSettings.get(player.world);
                                if (message.action > 0
                                        && allowed
                                        && !fighting
                                        && player.openContainer == player.inventoryContainer)
                                    settings.setDifficulty(message.action - 1);
                                ModNetwork.CHANNEL.sendTo(
                                        new State(settings.difficulty(), allowed, fighting),
                                        player);
                            });
            return null;
        }
    }

    public static final class State implements IMessage {
        private int difficulty;
        private boolean allowed, fighting;

        public State() {}

        public State(int difficulty, boolean allowed, boolean fighting) {
            this.difficulty = difficulty;
            this.allowed = allowed;
            this.fighting = fighting;
        }

        public void fromBytes(ByteBuf b) {
            difficulty = b.readUnsignedByte();
            allowed = b.readBoolean();
            fighting = b.readBoolean();
        }

        public void toBytes(ByteBuf b) {
            b.writeByte(difficulty);
            b.writeBoolean(allowed);
            b.writeBoolean(fighting);
        }

        public static final class Handler implements IMessageHandler<State, IMessage> {
            public IMessage onMessage(State message, MessageContext context) {
                net.minecraft.client.Minecraft.getMinecraft()
                        .addScheduledTask(
                                () ->
                                        com.scapeandrun.frostbite.client.DifficultySelector.receive(
                                                message.difficulty,
                                                message.allowed,
                                                message.fighting));
                return null;
            }
        }
    }
}
