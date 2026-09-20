package com.scapeandrun.frostbite.client;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.network.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Mouse;
import java.util.Arrays;

@Mod.EventBusSubscriber(modid = Frostbite.MODID, value = Side.CLIENT)
public final class DifficultySelector {
    private static final ResourceLocation SHEET =
            new ResourceLocation(Frostbite.MODID, "textures/gui/difficulty_icons.png");
    private static boolean open, ready, allowed, fighting;
    private static int difficulty;
    private static long lastQuery;

    public static void receive(int mode, boolean a, boolean f) {
        difficulty = Math.max(0, Math.min(2, mode));
        allowed = a;
        fighting = f;
        ready = true;
    }

    private static int x(GuiInventory gui) {
        return Math.min(gui.width - 48, gui.getGuiLeft() + gui.getXSize() + 12);
    }

    private static int y(GuiInventory gui) {
        return Math.max(4, Math.min(gui.height - 192, gui.getGuiTop() + 28));
    }

    @SubscribeEvent
    public static void init(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!(event.getGui() instanceof GuiInventory)) return;
        open = false;
        ready = false;
        query();
    }

    private static void query() {
        lastQuery = Minecraft.getSystemTime();
        ModNetwork.CHANNEL.sendToServer(new PacketDifficulty(0));
    }

    @SubscribeEvent
    public static void draw(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!(event.getGui() instanceof GuiInventory)) return;
        GuiInventory gui = (GuiInventory) event.getGui();
        Minecraft mc = Minecraft.getMinecraft();
        if (Minecraft.getSystemTime() - lastQuery > 1000) query();
        int x = x(gui), y = y(gui), mx = event.getMouseX(), my = event.getMouseY();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.color(1, 1, 1, 1);
        mc.getTextureManager().bindTexture(SHEET);
        mc.getTextureManager().getTexture(SHEET).setBlurMipmap(false, false);
        icon(x, y, difficulty);
        if (open) {
            for (int i = 0; i < 3; i++) {
                int row = y + 48 + i * 48;
                if (inside(mx, my, x, row))
                    Gui.drawRect(x - 2, row - 2, x + 38, row + 46, 0x665896B5);
                GlStateManager.color(1, 1, 1, 1);
                mc.getTextureManager().bindTexture(SHEET);
                icon(x, row, i);
            }
        } else {
            mc.getTextureManager().bindTexture(SHEET);
            Gui.drawScaledCustomSizeModalRect(
                    x - 5, y + 45, difficulty * 362, 505, 362, 120, 46, 15, 2172, 724);
            if (difficulty == 0)
                mc.fontRenderer.drawStringWithShadow(
                        "Standard",
                        x + 18 - mc.fontRenderer.getStringWidth("Standard") / 2F,
                        y + 49,
                        0xD8E5EC);
        }
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.color(1, 1, 1, 1);
        int hover = inside(mx, my, x, y) ? difficulty : -1;
        if (open) for (int i = 0; i < 3; i++) if (inside(mx, my, x, y + 48 + i * 48)) hover = i;
        if (hover >= 0) {
            String status =
                    !ready
                            ? "Waiting for world settings…"
                            : !allowed
                                    ? "Only the world owner or an operator may change this."
                                    : fighting
                                            ? "Defeat the active boss before changing difficulty."
                                            : "Click to "
                                                    + (open ? "select." : "choose difficulty.");

            gui.drawHoveringText(
                    Arrays.asList(
                            hover == 2
                                    ? "Expert rules. Mobs have 50% more health."
                                    : hover == 1
                                            ? "Enhanced boss patterns and treasure bags."
                                            : "Standard encounter rules.",
                            hover == 2 ? "Some bosses fight on after a lethal hit." : "",
                            status),
                    mx,
                    my);
        }
    }

    private static void icon(int x, int y, int mode) {
        Gui.drawScaledCustomSizeModalRect(x, y, mode * 362, 0, 362, 500, 36, 44, 2172, 724);
    }

    private static boolean inside(int mx, int my, int x, int y) {
        return mx >= x && mx < x + 36 && my >= y && my < y + 44;
    }

    @SubscribeEvent
    public static void mouse(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!(event.getGui() instanceof GuiInventory)
                || Mouse.getEventButton() != 0
                || !Mouse.getEventButtonState()) return;
        GuiInventory gui = (GuiInventory) event.getGui();
        Minecraft mc = Minecraft.getMinecraft();
        int mx = Mouse.getEventX() * gui.width / mc.displayWidth,
                my = gui.height - Mouse.getEventY() * gui.height / mc.displayHeight - 1,
                x = x(gui),
                y = y(gui);
        if (inside(mx, my, x, y)) {
            open = !open;
            event.setCanceled(true);
            return;
        }
        if (open) {
            for (int i = 0; i < 3; i++)
                if (inside(mx, my, x, y + 48 + i * 48)) {
                    if (ready && allowed && !fighting) {
                        ModNetwork.CHANNEL.sendToServer(new PacketDifficulty(i + 1));
                        ready = false;
                        open = false;
                    }
                    event.setCanceled(true);
                    return;
                }
            open = false;
        }
    }
}
