package com.scapeandrun.frostbite.command;

import com.scapeandrun.frostbite.entity.EntityDraedon;
import net.minecraft.command.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import java.util.UUID;

public final class DraedonReplyCommand extends CommandBase {
    @Override
    public String getName() {
        return "draedonreply";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/draedonreply <contact> <choice>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
            throws CommandException {
        if (args.length != 2) throw new WrongUsageException(getUsage(sender));
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        try {
            Entity contact = player.getServerWorld().getEntityFromUuid(UUID.fromString(args[0]));
            if (contact instanceof EntityDraedon) ((EntityDraedon) contact).reply(player, args[1]);
        } catch (IllegalArgumentException ignored) {
        }
    }
}
