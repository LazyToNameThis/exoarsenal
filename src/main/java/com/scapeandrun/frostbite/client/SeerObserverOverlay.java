package com.scapeandrun.frostbite.client;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.entity.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Frostbite.MODID, value = Side.CLIENT)
public final class SeerObserverOverlay {
    @SubscribeEvent
    public static void render(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.world == null || mc.gameSettings.hideGUI) return;
        EntityWulfrumEye boss = null;
        double nearest = 128 * 128;
        for (Entity e : mc.world.loadedEntityList)
            if (e instanceof EntityWulfrumEye.Observer
                    && ((EntityWulfrumEye) e).introTick() > 0
                    && e.getDistanceSq(mc.player) < nearest) {
                boss = (EntityWulfrumEye) e;
                nearest = e.getDistanceSq(mc.player);
            }
        if (boss == null || EntityDraedon.trialActor(boss)) return;
        float tick = boss.introTick() + event.getPartialTicks(),
                alpha = SeerObserverIntro.titleAlpha(tick);
        if (alpha < .02F) return;
        EncounterTitle.draw(
                SeerObserverIntro.TITLE,
                SeerObserverIntro.NAME,
                event.getResolution().getScaledWidth(),
                event.getResolution().getScaledHeight(),
                alpha,
                tick - 248);
    }
}
