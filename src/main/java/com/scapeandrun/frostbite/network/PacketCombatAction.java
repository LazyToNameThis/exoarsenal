package com.scapeandrun.frostbite.network;

import com.scapeandrun.frostbite.combat.WeaponCombat;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.*;

public final class PacketCombatAction implements IMessage {
    private int action;
    private float forward, strafe;

    public PacketCombatAction() {}

    public PacketCombatAction(int action, float forward, float strafe) {
        this.action = action;
        this.forward = forward;
        this.strafe = strafe;
    }

    @Override
    public void fromBytes(ByteBuf b) {
        action = b.readUnsignedByte();
        forward = b.readFloat();
        strafe = b.readFloat();
    }

    @Override
    public void toBytes(ByteBuf b) {
        b.writeByte(action);
        b.writeFloat(forward);
        b.writeFloat(strafe);
    }

    public static final class Handler implements IMessageHandler<PacketCombatAction, IMessage> {
        @Override
        public IMessage onMessage(PacketCombatAction p, MessageContext c) {
            c.getServerHandler()
                    .player
                    .getServerWorld()
                    .addScheduledTask(
                            () -> {
                                if (p.action >= 1
                                        && p.action <= 4
                                        && Float.isFinite(p.forward)
                                        && Float.isFinite(p.strafe))
                                    WeaponCombat.request(
                                            c.getServerHandler().player,
                                            p.action,
                                            p.forward,
                                            p.strafe);
                            });
            return null;
        }
    }
}
