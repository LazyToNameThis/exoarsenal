package com.scapeandrun.frostbite.client;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.entity.EntityX20Scout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Frostbite.MODID, value = Side.CLIENT)
public final class ScoutSceneOverlay {
    private ScoutSceneOverlay() {}

    @SubscribeEvent
    public static void render(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null || mc.player == null || mc.gameSettings.hideGUI) return;
        EntityX20Scout closest = null;
        double distance = 96 * 96;
        for (Entity entity : mc.world.loadedEntityList) {
            if (!(entity instanceof EntityX20Scout)) continue;
            EntityX20Scout scout = (EntityX20Scout) entity;
            if (scout.getScene() != 1
                    && scout.getScene() != 2
                    && scout.getScene() != 3
                    && scout.getScene() != 6) continue;
            double d = scout.getDistanceSq(mc.player);
            if (d < distance) {
                closest = scout;
                distance = d;
            }
        }
        if (closest == null) return;
        float tick = closest.getSceneTick() + event.getPartialTicks();
        if (closest.getScene() == 3 || closest.getScene() == 6) {
            int start = closest.getScene() == 3 ? 80 : 130,
                    end = closest.getScene() == 3 ? 232 : 220;
            if (tick < start || tick > end) return;
            String text =
                    closest.getScene() == 3
                            ? "You piss me the FUCK off. Who the hell do you think you are, summoning me, only to then attack me with no negotiation?"
                            : "Alright, you want to throw hands? Lets throw hands, then, bitch.";
            ScaledResolution r = event.getResolution();
            java.util.List<String> lines =
                    mc.fontRenderer.listFormattedStringToWidth(
                            text, Math.min(420, r.getScaledWidth() - 40));
            int y = r.getScaledHeight() - 54 - lines.size() * 11;
            mc.fontRenderer.drawStringWithShadow(
                    "\u00a7bTundra Trekker",
                    r.getScaledWidth() / 2F - mc.fontRenderer.getStringWidth("Tundra Trekker") / 2F,
                    y - 14,
                    0xFFFFFF);
            for (String line : lines) {
                mc.fontRenderer.drawStringWithShadow(
                        line,
                        r.getScaledWidth() / 2F - mc.fontRenderer.getStringWidth(line) / 2F,
                        y,
                        0xE5F8FF);
                y += 11;
            }
            return;
        }
        float alpha =
                closest.getScene() == 1 ? Math.min(1, tick / 15F) : Math.max(0, 1 - tick / 35F);
        if (alpha <= .02F) return;
        ScaledResolution res = event.getResolution();
        GlStateManager.pushMatrix();
        float scale =
                Math.min(
                        1.4F,
                        (res.getScaledWidth() - 24F)
                                / mc.fontRenderer.getStringWidth("FROSTBITTEN EXPLORER"));
        GlStateManager.translate(res.getScaledWidth() / 2F, res.getScaledHeight() * .2F, 0);
        GlStateManager.scale(scale, scale, 1);
        title(mc, "FROSTBITTEN EXPLORER", 0, tick, alpha);
        title(mc, "Tundra Trekker", 17, tick + 8, alpha);
        GlStateManager.popMatrix();
        GlStateManager.color(1, 1, 1, 1);
    }

    private static void title(Minecraft mc, String text, int y, float tick, float alpha) {
        int x = -mc.fontRenderer.getStringWidth(text) / 2;
        int glow = (Math.max(4, (int) (alpha * 90)) << 24) | 0x287EBC;
        mc.fontRenderer.drawString(text, x - 1, y, glow, false);
        mc.fontRenderer.drawString(text, x + 1, y, glow, false);
        mc.fontRenderer.drawString(text, x, y - 1, glow, false);
        mc.fontRenderer.drawString(text, x, y + 1, glow, false);
        for (int i = 0; i < text.length(); i++) {
            int blue = 220 + (int) (35 * (.5 + .5 * Math.sin(tick * .12 - i * .35)));
            int color = ((int) (alpha * 255) << 24) | (180 << 16) | (225 << 8) | blue;
            String glyph = text.substring(i, i + 1);
            mc.fontRenderer.drawString(glyph, x, y, color, false);
            x += mc.fontRenderer.getStringWidth(glyph);
        }
    }
}
