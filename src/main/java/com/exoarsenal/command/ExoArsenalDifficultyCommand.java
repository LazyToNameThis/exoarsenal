package com.exoarsenal.command;

import com.exoarsenal.world.ExoArsenalWorldSettings;
import net.minecraft.command.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public final class ExoArsenalDifficultyCommand extends CommandBase {
    @Override
    public String getName() {
        return "exoexpert";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/exoexpert <on|off|master|status>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
            throws CommandException {
        if (args.length != 1) throw new WrongUsageException(getUsage(sender));
        if (!args[0].equalsIgnoreCase("status"))
            for (net.minecraft.world.WorldServer world : server.worlds)
                for (net.minecraft.entity.Entity entity : world.loadedEntityList)
                    if (entity instanceof net.minecraft.entity.EntityLivingBase
                            && !((net.minecraft.entity.EntityLivingBase) entity).isNonBoss()
                            && !entity.isDead)
                        throw new CommandException(
                                "Defeat the active boss before changing difficulty.");
        ExoArsenalWorldSettings settings = ExoArsenalWorldSettings.get(sender.getEntityWorld());
        if (args[0].equalsIgnoreCase("on")) settings.setExpert(true);
        else if (args[0].equalsIgnoreCase("off")) settings.setExpert(false);
        else if (args[0].equalsIgnoreCase("master")) settings.setDifficulty(2);
        else if (!args[0].equalsIgnoreCase("status"))
            throw new WrongUsageException(getUsage(sender));
        sender.sendMessage(
                new TextComponentString(
                        "Exo Arsenal difficulty: "
                                + (settings.isMaster()
                                        ? "Master"
                                        : settings.isExpert() ? "Expert" : "Standard")
                                + ". Applies to newly spawned encounters; existing fights keep their difficulty."));
    }
}
