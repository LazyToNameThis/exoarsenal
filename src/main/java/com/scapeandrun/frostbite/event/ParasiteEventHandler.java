package com.scapeandrun.frostbite.event;

import com.scapeandrun.frostbite.Frostbite;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.reflect.Method;

public final class ParasiteEventHandler {
    private Method getData;
    private Method getPhase;
    private Method setPhase;
    private boolean unavailable;

    @SubscribeEvent
    public void synchronizePhase(TickEvent.WorldTickEvent event) {
        World world = event.world;
        if (event.phase != TickEvent.Phase.END
                || world.isRemote
                || world.provider.getDimension() != 0
                || world.getTotalWorldTime() % 200L != 0L
                || !Loader.isModLoaded("srparasites")) return;
        int wanted = phaseForDay(world.getTotalWorldTime() / 24000L);
        try {
            ensureReflection();
            if (unavailable) return;
            Object data = getData.invoke(null, world);
            int current = ((Number) getPhase.invoke(data, 0)).intValue();
            if (current != wanted) {
                setPhase.invoke(data, 0, (byte) wanted, true, world, true);
                Frostbite.LOGGER.info(
                        "Set SRP evolution phase to {} for wasteland day {}",
                        wanted,
                        world.getTotalWorldTime() / 24000L);
            }
        } catch (ReflectiveOperationException | RuntimeException error) {
            unavailable = true;
            Frostbite.LOGGER.error("Could not synchronize SRP evolution phases", error);
        }
    }

    public static int phaseForDay(long day) {
        if (day < 20L) return 0;
        if (day < 50L) return 1;
        if (day < 100L) return 2;
        return Math.min(10, 3 + (int) ((day - 100L) / 50L));
    }

    private void ensureReflection() throws ReflectiveOperationException {
        if (getData != null || unavailable) return;
        Class<?> data = Class.forName("com.dhanantry.scapeandrunparasites.world.SRPSaveData");
        getData = data.getMethod("get", World.class);
        getPhase = data.getMethod("getEvolutionPhase", int.class);
        setPhase =
                data.getMethod(
                        "setEvolutionPhase",
                        int.class,
                        byte.class,
                        boolean.class,
                        World.class,
                        boolean.class);
    }
}
