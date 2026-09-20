package com.scapeandrun.frostbite.client.gui;

import com.scapeandrun.frostbite.network.ModNetwork;
import com.scapeandrun.frostbite.network.PacketRequestSchematics;
import com.scapeandrun.frostbite.network.PacketSelectSchematic;
import com.scapeandrun.frostbite.schematic.SchematicLibrary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiRToolSchematics extends GuiScreen {
    private static final int PANEL_WIDTH = 420;
    private static final int PANEL_HEIGHT = 232;
    private static final int ROW_HEIGHT = 27;
    private static final int VISIBLE_ROWS = 5;
    private final List<SchematicLibrary.Summary> entries = new ArrayList<>();
    private int selected = -1;
    private int scroll;
    private boolean loading = true;
    private GuiButton loadButton;

    public static void receive(List<SchematicLibrary.Summary> entries) {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen instanceof GuiRToolSchematics) ((GuiRToolSchematics) screen).setEntries(entries);
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int left = panelLeft();
        int top = panelTop();
        loadButton = addButton(new GuiButton(0, left + 10, top + 204, 150, 20, "§lLoad Schematic"));
        addButton(new GuiButton(1, left + 166, top + 204, 80, 20, "Refresh"));
        addButton(new GuiButton(2, left + 340, top + 204, 70, 20, "Close"));
        loadButton.enabled = selected >= 0;
        requestList();
    }

    private void requestList() {
        loading = true;
        ModNetwork.CHANNEL.sendToServer(new PacketRequestSchematics());
    }

    private void setEntries(List<SchematicLibrary.Summary> incoming) {
        String previous =
                selected >= 0 && selected < entries.size() ? entries.get(selected).fileName : "";
        entries.clear();
        entries.addAll(incoming);
        selected = -1;
        for (int i = 0; i < entries.size(); i++)
            if (entries.get(i).fileName.equals(previous)) selected = i;
        scroll = Math.min(scroll, maxScroll());
        loading = false;
        if (loadButton != null) loadButton.enabled = selected >= 0;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0 && selected >= 0 && selected < entries.size()) {
            ModNetwork.CHANNEL.sendToServer(
                    new PacketSelectSchematic(entries.get(selected).fileName));
            mc.displayGuiScreen(null);
        } else if (button.id == 1) requestList();
        else if (button.id == 2) mc.displayGuiScreen(null);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int left = panelLeft();
        int top = panelTop();
        drawRect(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xF014181D);
        drawRect(left, top, left + PANEL_WIDTH, top + 2, 0xFFFF3544);
        drawRect(left, top + 37, left + PANEL_WIDTH, top + 38, 0xFF5C1B23);
        drawRect(left + 276, top + 47, left + 277, top + 196, 0xFF3B434A);

        drawCenteredString(
                fontRenderer,
                "§l§cR-Tool Schematic Library",
                left + PANEL_WIDTH / 2,
                top + 13,
                0xFFF4F7F6);
        fontRenderer.drawString("Available Schematics", left + 10, top + 48, 0xFF9BA9AE);
        fontRenderer.drawString("Selected Schematic", left + 289, top + 48, 0xFF9BA9AE);

        drawLibrary(left, top, mouseX, mouseY);
        drawDetails(left, top);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawLibrary(int left, int top, int mouseX, int mouseY) {
        int listTop = top + 60;
        if (loading) {
            fontRenderer.drawString(
                    "§7Scanning config/exoarsenal/schematics...",
                    left + 14,
                    listTop + 12,
                    0xFFB8C1C4);
            return;
        }
        if (entries.isEmpty()) {
            fontRenderer.drawString(
                    "§cNo valid schematics found.", left + 14, listTop + 10, 0xFFFF6873);
            fontRenderer.drawString(
                    "Place .json or .schematic files in:", left + 14, listTop + 30, 0xFFAAB4B8);
            fontRenderer.drawString(
                    "§oconfig/exoarsenal/schematics", left + 14, listTop + 43, 0xFF6ED4DA);
            fontRenderer.drawString(
                    "Maximum size: 3,000 non-air blocks", left + 14, listTop + 64, 0xFF717E84);
            return;
        }
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int index = scroll + row;
            if (index >= entries.size()) break;
            SchematicLibrary.Summary entry = entries.get(index);
            int y = listTop + row * ROW_HEIGHT;
            boolean hover =
                    mouseX >= left + 10 && mouseX < left + 266 && mouseY >= y && mouseY < y + 24;
            int background = index == selected ? 0xFF5B2028 : hover ? 0xFF293139 : 0xFF1B2127;
            drawRect(left + 10, y, left + 266, y + 24, background);
            if (index == selected) drawRect(left + 10, y, left + 13, y + 24, 0xFFFF3544);
            String name = fontRenderer.trimStringToWidth(entry.displayName, 164);
            fontRenderer.drawString(
                    name, left + 18, y + 3, index == selected ? 0xFFFFD7D9 : 0xFFE7EDEC);
            fontRenderer.drawString(entry.blocks + " blocks", left + 204, y + 3, 0xFFFF6975);
            fontRenderer.drawString(
                    entry.sizeX + "×" + entry.sizeY + "×" + entry.sizeZ + "  •  " + entry.author,
                    left + 18,
                    y + 14,
                    0xFF849298);
        }
        if (entries.size() > VISIBLE_ROWS) {
            int trackTop = listTop;
            int trackBottom = listTop + VISIBLE_ROWS * ROW_HEIGHT - 4;
            drawRect(left + 269, trackTop, left + 272, trackBottom, 0xFF232A30);
            int thumb = Math.max(18, (trackBottom - trackTop) * VISIBLE_ROWS / entries.size());
            int travel = trackBottom - trackTop - thumb;
            int thumbY = trackTop + (maxScroll() == 0 ? 0 : travel * scroll / maxScroll());
            drawRect(left + 269, thumbY, left + 272, thumbY + thumb, 0xFFFF4653);
        }
    }

    private void drawDetails(int left, int top) {
        if (selected < 0 || selected >= entries.size()) {
            fontRenderer.drawString("Choose a schematic", left + 289, top + 70, 0xFFD5DBDC);
            fontRenderer.drawString("from the list.", left + 289, top + 83, 0xFF758187);
            drawLoadedName(left, top + 116);
            return;
        }
        SchematicLibrary.Summary entry = entries.get(selected);
        fontRenderer.drawString(
                fontRenderer.trimStringToWidth(entry.displayName, 116),
                left + 289,
                top + 70,
                0xFFFFD7D9);
        fontRenderer.drawString("✦ " + entry.blocks + " blocks", left + 289, top + 92, 0xFFFF5967);
        fontRenderer.drawString(
                "◇ " + entry.sizeX + " × " + entry.sizeY + " × " + entry.sizeZ,
                left + 289,
                top + 107,
                0xFF70D7DD);
        fontRenderer.drawString(
                "By " + fontRenderer.trimStringToWidth(entry.author, 100),
                left + 289,
                top + 122,
                0xFFAFB9BC);
        fontRenderer.drawString("Sneak + Right Click", left + 289, top + 161, 0xFFFFC663);
        fontRenderer.drawString("a block to begin printing.", left + 289, top + 174, 0xFF8B979B);
    }

    private void drawLoadedName(int left, int y) {
        ItemStack tool = heldTool();
        NBTTagCompound tag = tool.isEmpty() ? null : tool.getTagCompound();
        fontRenderer.drawString("Loaded Schematic", left + 289, y, 0xFF59656A);
        String loaded =
                tag != null && tag.hasKey("SchematicName", 8)
                        ? tag.getString("SchematicName")
                        : "None";
        fontRenderer.drawString(
                fontRenderer.trimStringToWidth(loaded, 116),
                left + 289,
                y + 15,
                "None".equals(loaded) ? 0xFF6A7478 : 0xFF70D7DD);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        int left = panelLeft();
        int listTop = panelTop() + 60;
        if (mouseButton == 0
                && mouseX >= left + 10
                && mouseX < left + 266
                && mouseY >= listTop
                && mouseY < listTop + VISIBLE_ROWS * ROW_HEIGHT) {
            int index = scroll + (mouseY - listTop) / ROW_HEIGHT;
            if (index >= 0 && index < entries.size()) {
                selected = index;
                loadButton.enabled = true;
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            scroll += wheel < 0 ? 1 : -1;
            scroll = Math.max(0, Math.min(maxScroll(), scroll));
        }
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

    private int maxScroll() {
        return Math.max(0, entries.size() - VISIBLE_ROWS);
    }

    private int panelLeft() {
        return (width - PANEL_WIDTH) / 2;
    }

    private int panelTop() {
        return Math.max(4, (height - PANEL_HEIGHT) / 2);
    }

    private ItemStack heldTool() {
        if (mc.player == null) return ItemStack.EMPTY;
        ItemStack main = mc.player.getHeldItemMainhand();
        if (main.getItem() instanceof com.scapeandrun.frostbite.item.ItemRTool) return main;
        ItemStack off = mc.player.getHeldItemOffhand();
        return off.getItem() instanceof com.scapeandrun.frostbite.item.ItemRTool
                ? off
                : ItemStack.EMPTY;
    }
}
