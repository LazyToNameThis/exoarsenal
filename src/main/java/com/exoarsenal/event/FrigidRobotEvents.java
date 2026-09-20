package com.exoarsenal.event;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.entity.EntityFrigidRobot;
import com.exoarsenal.world.ExoArsenalWorldSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = ExoArsenal.MODID)
public final class FrigidRobotEvents {
    @SubscribeEvent
    public static void weather(
            net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent event) {
        if (event.phase == net.minecraftforge.fml.common.gameevent.TickEvent.Phase.END
                && !event.world.isRemote
                && event.world.provider.getDimension() == 0
                && event.world.getTotalWorldTime() % 200 == 0
                && !event.world.isThundering())
            ExoArsenalWorldSettings.get(event.world).consumeRobotKills();
    }

    @SubscribeEvent(priority = net.minecraftforge.fml.common.eventhandler.EventPriority.LOWEST)
    public static void defeated(LivingDeathEvent event) {
        if (!event.getEntityLiving().world.isRemote
                && event.getEntityLiving().getEntityData().hasUniqueId("ScoutWaveOwner")) {
            net.minecraft.entity.Entity owner =
                    ((net.minecraft.world.WorldServer) event.getEntityLiving().world)
                            .getEntityFromUuid(
                                    event.getEntityLiving()
                                            .getEntityData()
                                            .getUniqueId("ScoutWaveOwner"));
            if (owner instanceof com.exoarsenal.entity.EntityX20Scout)
                ((com.exoarsenal.entity.EntityX20Scout) owner)
                        .waveMemberDefeated(event.getEntityLiving().getUniqueID());
            else
                com.exoarsenal.world.ScoutEncounterLedger.get(event.getEntityLiving().world)
                        .recordDeath(event.getEntityLiving().getUniqueID());
            return;
        }
        if (event.getEntityLiving().world.isRemote
                || !(event.getEntityLiving() instanceof EntityFrigidRobot)
                || !(event.getSource().getTrueSource() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        if (!player.world.isThundering()) return;
        if (!com.exoarsenal.world.FrigidSpawnBiomes.isCold(
                player.world.getBiome(player.getPosition()))) return;
        ExoArsenalWorldSettings settings = ExoArsenalWorldSettings.get(player.world);
        if (settings.recordRobotKill(player.world.getTotalWorldTime()) < 12
                || !player.world
                        .getEntitiesWithinAABB(
                                EntityFrigidRobot.Arthropod.class,
                                player.getEntityBoundingBox().grow(128))
                        .isEmpty()) return;
        for (int i = 0; i < 16; i++) {
            double a = i * Math.PI / 8;
            BlockPos near = player.getPosition().add(Math.cos(a) * 18, 0, Math.sin(a) * 18);
            if (!player.world.isBlockLoaded(near)) continue;
            BlockPos p = player.world.getTopSolidOrLiquidBlock(near);
            EntityFrigidRobot.Arthropod mob = new EntityFrigidRobot.Arthropod(player.world);
            mob.setPosition(p.getX() + .5, p.getY(), p.getZ() + .5);
            if (!player.world.getCollisionBoxes(mob, mob.getEntityBoundingBox()).isEmpty())
                continue;
            mob.setAttackTarget(player);
            if (player.world.spawnEntity(mob)) {
                settings.consumeRobotKills();
                player.sendStatusMessage(
                        new TextComponentString(
                                "An X-01 Arthropod has traced the fallen robots' signal."),
                        false);
            }
            break;
        }
    }
}
