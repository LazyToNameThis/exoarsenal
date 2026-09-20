package com.exoarsenal.client.gui;

import com.exoarsenal.event.KXSystems;
import com.exoarsenal.network.ModNetwork;
import com.exoarsenal.network.PacketKXAction;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiKXMobEditor extends GuiScreen {
    private final int targetId;
    private int page;

    public GuiKXMobEditor(int targetId) {
        this.targetId = targetId;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int first = page * 10;
        KXSystems.Mutation[] mutations = KXSystems.Mutation.values();
        for (int i = 0; i < 10 && first + i < mutations.length; i++) {
            int column = i / 5, row = i % 5;
            buttonList.add(
                    new GuiButton(
                            first + i,
                            width / 2 - 204 + column * 208,
                            height / 2 - 76 + row * 25,
                            200,
                            20,
                            mutations[first + i].display));
        }
        buttonList.add(new GuiButton(100, width / 2 - 100, height / 2 + 62, 96, 20, "Previous"));
        buttonList.add(new GuiButton(101, width / 2 + 4, height / 2 + 62, 96, 20, "Next"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id < 20)
            ModNetwork.CHANNEL.sendToServer(new PacketKXAction(PacketKXAction.MUTATION, button.id));
        else {
            page = button.id == 100 ? Math.max(0, page - 1) : Math.min(1, page + 1);
            initGui();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partial) {
        drawDefaultBackground();
        drawCenteredString(
                fontRenderer, "\u00a7bKX-20 GENETIC EDITOR", width / 2, height / 2 - 112, 0xFFFFFF);
        drawCenteredString(
                fontRenderer,
                "Target suspended \u2022 no idle drain",
                width / 2,
                height / 2 - 98,
                0x72E8E5);
        drawCenteredString(
                fontRenderer,
                "Apply or remove a mutation \u2022 250,000 RF",
                width / 2,
                height / 2 - 87,
                0xAAAAAA);
        super.drawScreen(mouseX, mouseY, partial);
    }

    @Override
    protected void keyTyped(char typed, int key) throws IOException {
        if (key == Keyboard.KEY_LBRACKET && GuiScreen.isShiftKeyDown()) {
            mc.displayGuiScreen(null);
            return;
        }
        super.keyTyped(typed, key);
    }

    @Override
    public void onGuiClosed() {
        ModNetwork.CHANNEL.sendToServer(new PacketKXAction(PacketKXAction.EDITOR_END, 0));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
