package com.exoarsenal.client;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.entity.EntityExcavator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = ExoArsenal.MODID, value = Side.CLIENT)
public final class ExcavatorTutorialOverlay {
    @SubscribeEvent
    public static void draw(RenderGameOverlayEvent.Post e) {
        if (e.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null || mc.player == null) return;
        for (EntityExcavator boss :
                mc.world.getEntitiesWithinAABB(
                        EntityExcavator.class, mc.player.getEntityBoundingBox().grow(160)))
            if (boss.finale() > 0) {
                int w = e.getResolution().getScaledWidth(), h = e.getResolution().getScaledHeight();
                String text =
                        boss.finale() == 1 && boss.finaleTick() >= 90 && boss.finaleTick() < 135
                                ? "CRITICAL VELOCI"
                                : boss.finale() == 2
                                        ? "BURNOUT  "
                                                + Math.max(0, (600 - boss.finaleTick() + 19) / 20)
                                                + "s"
                                        : boss.finale() == 4
                                                ? "MASH CTRL + X  —  " + boss.mash() + " / 24"
                                                : "";
                if (!text.isEmpty()) {
                    mc.fontRenderer.drawStringWithShadow(
                            "\u00a7l" + text,
                            (w - mc.fontRenderer.getStringWidth("\u00a7l" + text)) / 2F,
                            h * .38F,
                            0xFFFFCE81);
                    if (boss.finale() == 4) {
                        Gui.drawRect(
                                w / 2 - 80,
                                (int) (h * .38F) + 17,
                                w / 2 + 80,
                                (int) (h * .38F) + 23,
                                0xC016211B);
                        Gui.drawRect(
                                w / 2 - 80,
                                (int) (h * .38F) + 17,
                                w / 2 - 80 + 160 * boss.mash() / 24,
                                (int) (h * .38F) + 23,
                                0xFF9AF0BC);
                    }
                }
                return;
            }
        for (EntityExcavator boss :
                mc.world.getEntitiesWithinAABB(
                        EntityExcavator.class, mc.player.getEntityBoundingBox().grow(32)))
            if (boss.expertStage() == 12) {
                int w = e.getResolution().getScaledWidth(), h = e.getResolution().getScaledHeight();
                Gui.drawRect(0, 0, w, h, 0xAA737373);
                String text = "Press ctrl+X to parry!";
                mc.fontRenderer.drawStringWithShadow(
                        text, (w - mc.fontRenderer.getStringWidth(text)) / 2F, h * .42F, 0xFFFFFF);
                return;
            }
        for (EntityExcavator boss :
                mc.world.getEntitiesWithinAABB(
                        EntityExcavator.class, mc.player.getEntityBoundingBox().grow(20)))
            if (boss.chargeParryWindow()
                    && boss.drillTip().squareDistanceTo(mc.player.getPositionVector()) < 144) {
                int w = e.getResolution().getScaledWidth(), h = e.getResolution().getScaledHeight();
                String text = "\u00a7lCTRL + X  •  PARRY";
                mc.fontRenderer.drawStringWithShadow(
                        text, (w - mc.fontRenderer.getStringWidth(text)) / 2F, h * .58F, 0xFFE7A2);
                break;
            }
        for (EntityExcavator boss :
                mc.world.getEntitiesWithinAABB(
                        EntityExcavator.class, mc.player.getEntityBoundingBox().grow(100)))
            if (boss.introTick() == -1 && boss.parryCount() > 0) {
                if (com.exoarsenal.entity.EntityDraedon.trialActor(boss)) continue;
                float alpha =
                        boss.expertStage() == 6
                                ? Math.min(1, (40 - boss.attackTick()) / 12F)
                                : Math.min(1, boss.attackTick() / 10F);
                EncounterTitle.draw(
                        com.exoarsenal.entity.SeerObserverIntro.TITLE,
                        com.exoarsenal.entity.SeerObserverIntro.NAME,
                        e.getResolution().getScaledWidth(),
                        e.getResolution().getScaledHeight(),
                        Math.max(0, alpha),
                        boss.attackTick());
                return;
            }
    }
}
