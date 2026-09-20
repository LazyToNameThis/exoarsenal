package com.scapeandrun.frostbite.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;

public final class EncounterTitle {
    private static final ResourceLocation FONT =
            new ResourceLocation("exoarsenal", "font/infernum.png");

    private EncounterTitle() {}

    public static void draw(
            String heading, String name, int width, int height, float alpha, float age) {
        if (alpha < .02F) return;
        heading = heading.toUpperCase(java.util.Locale.ROOT);
        name = name.toUpperCase(java.util.Locale.ROOT);
        Minecraft mc = Minecraft.getMinecraft();
        float scale =
                Math.min(
                        1.15F,
                        (width - 32F)
                                / Math.max(
                                        EncounterFont.measure(name) * 2.1F,
                                        EncounterFont.measure(heading)));
        GlStateManager.pushMatrix();
        GlStateManager.translate(width / 2F, height * .23F, 0);
        GlStateManager.scale(scale, scale, 1);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        mc.getTextureManager().bindTexture(FONT);
        line(heading, 0, 1, alpha, age, 0xB8D7C7);
        line(name, 17, 2.1F, alpha, age, 0xDCFFE4);
        GlStateManager.popMatrix();
        GlStateManager.disableBlend();
        GlStateManager.color(1, 1, 1, 1);
    }

    private static void line(String text, float y, float scale, float alpha, float age, int color) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, y, 0);
        GlStateManager.scale(scale, scale, 1);
        float x = -EncounterFont.measure(text) / 2,
                progress = CombatMotion.smooth(age / 18),
                spread = (1 - progress) * 3;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            EncounterFont.Glyph glyph = EncounterFont.glyph(c);
            float reveal = CombatMotion.smooth((age - i * .13F) / 6), opacity = alpha * reveal;
            if (opacity > .02F) {
                float echo = opacity * (1 - progress) * .25F;
                if (echo > .02F)
                    for (int k = 0; k < 4; k++) {
                        double a = k * Math.PI / 2;
                        draw(
                                glyph,
                                x + (float) Math.cos(a) * spread,
                                (float) Math.sin(a) * spread,
                                color,
                                echo);
                    }
                draw(glyph, x + .7F, .7F, 0x07150F, opacity * .8F);
                draw(glyph, x, 0, color, opacity);
            }
            x += glyph.advance(c);
        }
        GlStateManager.popMatrix();
    }

    private static void draw(EncounterFont.Glyph glyph, float x, float y, int color, float alpha) {
        GlStateManager.color(
                (color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F, alpha);
        GlStateManager.pushMatrix();
        GlStateManager.translate(
                x + (glyph.left + glyph.cropX) * EncounterFont.UNIT,
                y + glyph.cropY * EncounterFont.UNIT,
                0);
        GlStateManager.scale(EncounterFont.UNIT, EncounterFont.UNIT, 1);
        Gui.drawModalRectWithCustomSizedTexture(
                0, 0, glyph.x, glyph.y, glyph.width, glyph.height, 512, 512);
        GlStateManager.popMatrix();
    }
}
