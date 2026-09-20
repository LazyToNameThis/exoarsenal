package com.scapeandrun.frostbite.client.tooltip;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.item.EquipmentTooltip;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = Frostbite.MODID, value = Side.CLIENT)
public final class RgbTooltipLibrary {
    private static final List<Provider> PROVIDERS = new ArrayList<>();

    static {
        register(EquipmentTooltip::supports, EquipmentTooltip::build);
    }

    private RgbTooltipLibrary() {}

    public static void register(
            Predicate<ItemStack> matcher, Function<ItemStack, List<EquipmentTooltip.Line>> lines) {
        if (matcher != null && lines != null) PROVIDERS.add(new Provider(matcher, lines));
    }

    public static List<EquipmentTooltip.Line> lines(ItemStack stack) {
        for (Provider provider : PROVIDERS) {
            if (provider.matcher.test(stack)) {
                List<EquipmentTooltip.Line> value = provider.lines.apply(stack);
                return value == null ? Collections.emptyList() : value;
            }
        }
        return Collections.emptyList();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void render(RenderTooltipEvent.Pre event) {
        List<EquipmentTooltip.Line> lines = lines(event.getStack());
        if (lines.isEmpty()) return;
        event.setCanceled(true);

        FontRenderer font = event.getFontRenderer();
        int contentWidth = 0;
        int contentHeight = 0;
        for (EquipmentTooltip.Line line : lines) {
            if (!line.gap) contentWidth = Math.max(contentWidth, font.getStringWidth(line.text));
            contentHeight += line.gap ? 5 : 11;
        }

        int boxWidth = contentWidth + 18;
        int boxHeight = contentHeight + 12;
        int x = event.getX() + 12;
        int y = event.getY() - 12;
        if (x + boxWidth > event.getScreenWidth() - 4) x = event.getX() - boxWidth - 12;
        if (y + boxHeight > event.getScreenHeight() - 4)
            y = event.getScreenHeight() - boxHeight - 4;
        if (y < 4) y = 4;
        if (x < 4) x = 4;

        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.translate(0.0F, 0.0F, 400.0F);

        Gui.drawRect(x - 2, y - 2, x + boxWidth + 2, y + boxHeight + 2, 0xA0000000);
        Gui.drawRect(x - 1, y - 1, x + boxWidth + 1, y + boxHeight + 1, 0xFF41151C);
        Gui.drawRect(x, y, x + boxWidth, y + boxHeight, 0xF211161B);
        Gui.drawRect(x, y, x + 3, y + boxHeight, 0xFFFF3F4E);
        Gui.drawRect(x + 3, y, x + boxWidth, y + 1, 0xFF77313A);
        Gui.drawRect(x + 3, y + boxHeight - 1, x + boxWidth, y + boxHeight, 0xFF252E34);

        int drawY = y + 7;
        for (EquipmentTooltip.Line line : lines) {
            if (line.gap) {
                drawY += 5;
                continue;
            }
            font.drawStringWithShadow(line.text, x + 10, drawY, 0xFF000000 | line.rgb);
            drawY += 11;
        }

        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private static final class Provider {
        private final Predicate<ItemStack> matcher;
        private final Function<ItemStack, List<EquipmentTooltip.Line>> lines;

        private Provider(
                Predicate<ItemStack> matcher,
                Function<ItemStack, List<EquipmentTooltip.Line>> lines) {
            this.matcher = matcher;
            this.lines = lines;
        }
    }
}
