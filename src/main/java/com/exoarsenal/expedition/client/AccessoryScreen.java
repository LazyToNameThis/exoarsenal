package com.exoarsenal.expedition.client;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.expedition.AccessoryContainer;
import com.exoarsenal.network.*;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = ExoArsenal.MODID, value = Side.CLIENT)
public final class AccessoryScreen extends GuiContainer {
    private static final int BUTTON = 7305;
    private static final net.minecraft.client.settings.KeyBinding OPEN =
            new net.minecraft.client.settings.KeyBinding(
                    "key.exoarsenal.accessories",
                    org.lwjgl.input.Keyboard.KEY_NONE,
                    "key.categories.inventory");

    @SubscribeEvent
    public static void registerKey(net.minecraftforge.client.event.ModelRegistryEvent event) {
        net.minecraftforge.fml.client.registry.ClientRegistry.registerKeyBinding(OPEN);
    }

    @SubscribeEvent
    public static void key(net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent event) {
        if (OPEN.isPressed() && net.minecraft.client.Minecraft.getMinecraft().currentScreen == null)
            ModNetwork.CHANNEL.sendToServer(new PacketAccessories());
    }

    public AccessoryScreen(EntityPlayer player) {
        super(new AccessoryContainer(player));
        xSize = 176;
        ySize = 166;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partial) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partial);
        renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partial, int mouseX, int mouseY) {
        drawRect(guiLeft - 2, guiTop - 2, guiLeft + xSize + 2, guiTop + ySize + 2, 0xFF0C1820);
        drawRect(guiLeft, guiTop, guiLeft + xSize, guiTop + ySize, 0xFF263842);
        drawRect(guiLeft + 5, guiTop + 5, guiLeft + xSize - 5, guiTop + 24, 0xFF152A35);
        for (net.minecraft.inventory.Slot slot : inventorySlots.inventorySlots) {
            int x = guiLeft + slot.xPos, y = guiTop + slot.yPos;
            drawRect(x - 1, y - 1, x + 17, y + 17, 0xFF101E26);
            drawRect(x, y, x + 16, y + 16, 0xFF52636A);
            drawRect(x, y, x + 16, y + 1, 0xFF8CABB2);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString("Accessories", 8, 10, 0xCAE3E9);
        fontRenderer.drawString("One of each accessory", 24, 59, 0xAABDC5);
        fontRenderer.drawString("Inventory", 8, 71, 0xCAE3E9);
    }

    @SubscribeEvent
    public static void inventoryButton(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof GuiInventory)
            event.getButtonList()
                    .add(
                            new GuiButton(
                                    BUTTON,
                                    event.getGui().width / 2 + 90,
                                    event.getGui().height / 2 - 83,
                                    80,
                                    20,
                                    "Accessories"));
    }

    @SubscribeEvent
    public static void click(GuiScreenEvent.ActionPerformedEvent.Post event) {
        if (event.getGui() instanceof GuiInventory && event.getButton().id == BUTTON)
            ModNetwork.CHANNEL.sendToServer(new PacketAccessories());
    }
}
