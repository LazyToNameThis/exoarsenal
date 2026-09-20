package com.exoarsenal.client;

import com.exoarsenal.ExoArsenal;
import net.minecraft.client.model.ModelPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import java.lang.reflect.Method;

@Mod.EventBusSubscriber(modid = ExoArsenal.MODID, value = Side.CLIENT)
public final class ObfuscatePlayerCompat {
    private static final CombatPlayerModel POSE = new CombatPlayerModel(false);
    private static Method model, partial;
    private static boolean logged, failed;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void modelEvent(PlayerEvent event) {
        String name = event.getClass().getName();
        boolean angles =
                name.equals(
                        "com.mrcrayfish.obfuscate.client.event.ModelPlayerEvent$SetupAngles$Post");
        boolean render =
                name.equals("com.mrcrayfish.obfuscate.client.event.ModelPlayerEvent$Render$Pre");
        if ((!angles && !render) || failed) return;
        try {
            if (model == null) {
                Class<?> base =
                        Class.forName("com.mrcrayfish.obfuscate.client.event.ModelPlayerEvent");
                model = base.getMethod("getModelPlayer");
                partial = base.getMethod("getPartialTicks");
            }
            if (render) PlayerSkinState.prepare(event.getEntityPlayer());
            else
                POSE.applyTo(
                        (ModelPlayer) model.invoke(event),
                        event.getEntityPlayer(),
                        ((Number) partial.invoke(event)).floatValue());
            if (!logged) {
                logged = true;
                ExoArsenal.LOGGER.info(
                        "Obfuscate player bridge active: post-angle combat poses and pre-model skin state");
            }
        } catch (ReflectiveOperationException error) {
            failed = true;
            ExoArsenal.LOGGER.error("Cannot attach Obfuscate player animation bridge", error);
        }
    }

    private ObfuscatePlayerCompat() {}
}
