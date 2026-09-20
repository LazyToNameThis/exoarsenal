package com.scapeandrun.frostbite.expedition.client;

import com.scapeandrun.frostbite.expedition.CodebreakerContainer;
import com.scapeandrun.frostbite.network.*;
import com.scapeandrun.frostbite.client.WulfrumSelectionArtwork;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.BlockPos;
import java.io.IOException;

public final class CodebreakerScreen extends GuiContainer {
    private final BlockPos pos;
    private final float[] scales = {.8F, .8F, .8F};
    private int hover = -1;

    public CodebreakerScreen(BlockPos pos) {
        super(new CodebreakerContainer(pos));
        this.pos = pos;
        xSize = 256;
        ySize = 96;
    }

    private int cx(int icon) {
        return width / 2 + (icon - 1) * 78;
    }

    private int cy(int icon) {
        return height / 2 - 32 - (icon == 1 ? 16 : 0);
    }

    private int hovered(int x, int y) {
        for (int i = 0; i < 3; i++)
            if (Math.abs(x - cx(i)) < 34 * scales[i] && Math.abs(y - cy(i)) < 30 * scales[i])
                return i;
        return -1;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        for (int i = 0; i < 3; i++)
            scales[i] = Math.max(.8F, Math.min(1.05F, scales[i] + (i == hover ? .025F : -.04F)));
    }

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        int i = hovered(x, y);
        if (button == 0 && i >= 0) {
            int[] choices = {1, 2, 0};
            ModNetwork.CHANNEL.sendToServer(new PacketCodebreaker(pos, choices[i]));
            mc.player.closeScreen();
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partial, int x, int y) {}

    @Override
    public void drawScreen(int x, int y, float partial) {
        int next = hovered(x, y);
        if (next != hover && next >= 0)
            mc.player.playSound(
                    SoundEvents.BLOCK_NOTE_HAT, .35F, next == 0 ? .7F : next == 1 ? .9F : 1.2F);
        hover = next;
        for (int i = 0; i < 3; i++) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(cx(i), cy(i), 0);
            GlStateManager.scale(scales[i], scales[i], 1);
            WulfrumSelectionArtwork.draw(
                    i, (x1, y1, x2, y2, color) -> drawRect(x1, y1, x2, y2, color));
            GlStateManager.popMatrix();
        }
        if (hover >= 0) {
            String[] names = {"X-04 \"Excavator\"", "X-05 \"Brawler\"", "Seer & Observer"};
            String[] descriptions = {
                "Repurposed Mining Machine", "Wulfrum War Weapon", "Coordinated Sightseers"
            };
            drawCenteredString(fontRenderer, names[hover], cx(hover), cy(hover) + 43, 0xE5D5AA);
            drawCenteredString(
                    fontRenderer, descriptions[hover], width / 2, cy(hover) + 56, 0xD4C5A2);
        }
    }
}
