package com.scapeandrun.frostbite.client;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.entity.EntityBrawler;
import com.scapeandrun.frostbite.entity.SeerObserverIntro;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Frostbite.MODID, value = Side.CLIENT)
public final class BrawlerTitleOverlay {
    @SubscribeEvent
    public static void render(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;
        for (EntityBrawler boss :
                mc.world.getEntitiesWithinAABB(
                        EntityBrawler.class, mc.player.getEntityBoundingBox().grow(80))) {
            int w = event.getResolution().getScaledWidth(),
                    h = event.getResolution().getScaledHeight();
            if (boss.introTick() >= 140 && boss.introTick() <= 160) {
                String prompt = "\u00a7lCTRL + X — PARRY";
                mc.fontRenderer.drawStringWithShadow(
                        prompt,
                        (w - mc.fontRenderer.getStringWidth(prompt)) / 2F,
                        h * .62F,
                        0xFFF1BA);
                return;
            }
        }
    }
}
