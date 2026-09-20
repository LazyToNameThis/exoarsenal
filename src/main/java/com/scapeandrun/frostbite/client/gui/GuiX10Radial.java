package com.scapeandrun.frostbite.client.gui;

import com.scapeandrun.frostbite.event.X10Systems;
import com.scapeandrun.frostbite.network.ModNetwork;
import com.scapeandrun.frostbite.network.PacketX10Action;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

import java.io.IOException;

public class GuiX10Radial extends GuiScreen {
    public enum Page {
        WEAPONS,
        SYSTEMS
    }

    private static final int[] SYSTEMS = {
        X10Systems.ANALYZER,
        X10Systems.NIGHT,
        X10Systems.WINGS,
        X10Systems.JETPACK,
        X10Systems.SWIM,
        X10Systems.SPRINT,
        X10Systems.ALL
    };
    private static final String[] SYSTEM_NAMES = {
        "Analyzer",
        "Night Vision",
        "Energy Wings",
        "Jetpack",
        "Swim Boost",
        "Sprint Boost",
        "All Systems"
    };
    private final Page page;
    private int hovered = -1;

    public GuiX10Radial(Page page) {
        this.page = page;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int cx = width / 2;
        int cy = height / 2;
        int count = page == Page.WEAPONS ? 5 : SYSTEMS.length;
        hovered = nearest(mouseX, mouseY, cx, cy, count);

        drawRect(cx - 45, cy - 16, cx + 45, cy + 16, 0xE6111519);
        drawCenteredString(
                fontRenderer,
                page == Page.WEAPONS ? "§lWeapon Belt" : "§lArmor Systems",
                cx,
                cy - 10,
                0xFFFFE56B);
        drawCenteredString(fontRenderer, "Click an option", cx, cy + 3, 0xFF9BA7AB);

        for (int i = 0; i < count; i++) {
            double angle = -Math.PI / 2.0D + Math.PI * 2.0D * i / count;
            int x = cx + (int) Math.round(Math.cos(angle) * 92.0D);
            int y = cy + (int) Math.round(Math.sin(angle) * 70.0D);
            boolean selected = i == hovered;
            drawRect(x - 38, y - 16, x + 38, y + 16, selected ? 0xF06C5419 : 0xE51B2228);
            drawRect(x - 38, y - 16, x + 38, y - 14, selected ? 0xFFFFE56B : 0xFF536067);

            if (page == Page.WEAPONS) drawWeapon(i, x, y, selected);
            else drawSystem(i, x, y, selected);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawWeapon(int index, int x, int y, boolean selected) {
        ItemStack stack =
                mc.player == null ? ItemStack.EMPTY : X10Systems.beltItem(mc.player, index);
        if (!stack.isEmpty()) {
            GlStateManager.pushMatrix();
            mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x - 34, y - 10);
            GlStateManager.popMatrix();
        }
        String name = stack.isEmpty() ? "Empty Slot" : stack.getDisplayName();
        name = fontRenderer.trimStringToWidth(name, 54);
        fontRenderer.drawString(
                (index + 1) + ". " + name, x - 15, y - 4, selected ? 0xFFFFF3B0 : 0xFFD8E0E0);
    }

    private void drawSystem(int index, int x, int y, boolean selected) {
        boolean enabled =
                mc.player != null
                        && (SYSTEMS[index] == X10Systems.ALL
                                ? X10Systems.mask(mc.player) != 0
                                : X10Systems.enabled(mc.player, SYSTEMS[index]));
        drawCenteredString(
                fontRenderer, SYSTEM_NAMES[index], x, y - 8, selected ? 0xFFFFF3B0 : 0xFFD8E0E0);
        drawCenteredString(
                fontRenderer,
                enabled ? "Enabled" : "Disabled",
                x,
                y + 4,
                enabled ? 0xFF79D991 : 0xFFFF6873);
    }

    private int nearest(int mouseX, int mouseY, int cx, int cy, int count) {
        int best = -1;
        double bestDistance = 34.0D * 34.0D;
        for (int i = 0; i < count; i++) {
            double angle = -Math.PI / 2.0D + Math.PI * 2.0D * i / count;
            double x = cx + Math.cos(angle) * 92.0D;
            double y = cy + Math.sin(angle) * 70.0D;
            double dx = mouseX - x;
            double dy = mouseY - y;
            double distance = dx * dx + dy * dy;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = i;
            }
        }
        return best;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton != 0 || hovered < 0) return;
        if (page == Page.WEAPONS) {
            ModNetwork.CHANNEL.sendToServer(
                    new PacketX10Action(PacketX10Action.BELT_SWAP, hovered));
        } else {
            ModNetwork.CHANNEL.sendToServer(
                    new PacketX10Action(PacketX10Action.SYSTEM_TOGGLE, SYSTEMS[hovered]));
        }
        mc.displayGuiScreen(null);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) mc.displayGuiScreen(null);
        else super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
