package com.exoarsenal.network;

import com.exoarsenal.ExoArsenal;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class ModNetwork {
    public static final SimpleNetworkWrapper CHANNEL =
            NetworkRegistry.INSTANCE.newSimpleChannel(ExoArsenal.MODID);

    private ModNetwork() {}

    public static void init() {
        CHANNEL.registerMessage(
                PacketCodebreaker.Handler.class, PacketCodebreaker.class, 23, Side.SERVER);
        CHANNEL.registerMessage(
                PacketExcavatorParry.Handler.class, PacketExcavatorParry.class, 22, Side.CLIENT);
        CHANNEL.registerMessage(
                PacketWulfrumState.Handler.class, PacketWulfrumState.class, 21, Side.CLIENT);
        CHANNEL.registerMessage(
                PacketCycleRmorMode.Handler.class, PacketCycleRmorMode.class, 0, Side.SERVER);
        CHANNEL.registerMessage(
                PacketStartRBladeSpin.Handler.class, PacketStartRBladeSpin.class, 1, Side.SERVER);
        CHANNEL.registerMessage(
                PacketStopRBladeSpin.Handler.class, PacketStopRBladeSpin.class, 2, Side.SERVER);
        CHANNEL.registerMessage(
                PacketRBladeAirAttack.Handler.class, PacketRBladeAirAttack.class, 3, Side.SERVER);
        CHANNEL.registerMessage(
                PacketEquipmentAction.Handler.class, PacketEquipmentAction.class, 4, Side.SERVER);
        CHANNEL.registerMessage(
                PacketRToolEffect.Handler.class, PacketRToolEffect.class, 5, Side.CLIENT);
        CHANNEL.registerMessage(
                PacketRmorDefenseState.Handler.class, PacketRmorDefenseState.class, 6, Side.CLIENT);
        CHANNEL.registerMessage(
                PacketRequestSchematics.Handler.class,
                PacketRequestSchematics.class,
                7,
                Side.SERVER);
        CHANNEL.registerMessage(
                PacketSchematicList.Handler.class, PacketSchematicList.class, 8, Side.CLIENT);
        CHANNEL.registerMessage(
                PacketSelectSchematic.Handler.class, PacketSelectSchematic.class, 9, Side.SERVER);
        CHANNEL.registerMessage(
                PacketX10Action.Handler.class, PacketX10Action.class, 10, Side.SERVER);
        CHANNEL.registerMessage(
                PacketKXAction.Handler.class, PacketKXAction.class, 11, Side.SERVER);
        CHANNEL.registerMessage(
                PacketScoutImpact.Handler.class, PacketScoutImpact.class, 12, Side.CLIENT);
        CHANNEL.registerMessage(
                PacketCombatAction.Handler.class, PacketCombatAction.class, 13, Side.SERVER);
        CHANNEL.registerMessage(
                PacketCombatState.Handler.class, PacketCombatState.class, 14, Side.CLIENT);
        CHANNEL.registerMessage(
                PacketPrebossMana.Handler.class, PacketPrebossMana.class, 15, Side.CLIENT);
        CHANNEL.registerMessage(
                PacketBottleJump.Handler.class, PacketBottleJump.class, 16, Side.SERVER);
        CHANNEL.registerMessage(
                PacketAccessories.Open.class, PacketAccessories.class, 17, Side.SERVER);
        CHANNEL.registerMessage(
                PacketAccessories.State.Handler.class,
                PacketAccessories.State.class,
                18,
                Side.CLIENT);
        CHANNEL.registerMessage(
                PacketDifficulty.Handler.class, PacketDifficulty.class, 19, Side.SERVER);
        CHANNEL.registerMessage(
                PacketDifficulty.State.Handler.class,
                PacketDifficulty.State.class,
                20,
                Side.CLIENT);
    }
}
