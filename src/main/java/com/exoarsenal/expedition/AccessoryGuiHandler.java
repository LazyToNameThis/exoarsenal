package com.exoarsenal.expedition;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public final class AccessoryGuiHandler implements IGuiHandler {
    @Override
    public Object getServerGuiElement(
            int id, EntityPlayer player, World world, int x, int y, int z) {
        return id == 0
                ? new AccessoryContainer(player)
                : id == 1
                        ? new CodebreakerContainer(new net.minecraft.util.math.BlockPos(x, y, z))
                        : null;
    }

    @Override
    public Object getClientGuiElement(
            int id, EntityPlayer player, World world, int x, int y, int z) {
        return id == 0
                ? new com.exoarsenal.expedition.client.AccessoryScreen(player)
                : id == 1
                        ? new com.exoarsenal.expedition.client.CodebreakerScreen(
                                new net.minecraft.util.math.BlockPos(x, y, z))
                        : null;
    }
}
