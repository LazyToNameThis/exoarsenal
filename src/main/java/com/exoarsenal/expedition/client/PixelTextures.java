package com.exoarsenal.expedition.client;

import com.exoarsenal.expedition.PixelArt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import java.io.*;

public final class PixelTextures {
    private static final String[] NAMES = {
        "wulfrum_plate",
        "marine_bone",
        "sea_crystal",
        "woven_grip",
        "coral_shell",
        "flinx_fur",
        "chitin",
        "wild_hide",
        "gel_skin",
        "jungle_leaf"
    };
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[NAMES.length];

    public static int count() {
        return NAMES.length;
    }

    public static int material(int color) {
        int r = color >> 16 & 255, g = color >> 8 & 255, b = color & 255;
        if (Math.min(r, Math.min(g, b)) > 200
                && Math.max(r, Math.max(g, b)) - Math.min(r, Math.min(g, b)) < 20) return 5;
        if (b > r * 1.12 || g > r * 1.25) return 2;
        if (r > g * 1.2 && b > g * .8) return 4;
        if (r > g * 1.15 && g > b * 1.15) return 3;
        if (r > 130 && r >= g && b < r) return 1;
        return 0;
    }

    public static ResourceLocation location(int index) {
        bind(index);
        return TEXTURES[index];
    }

    public static void bind(int index) {
        Minecraft mc = Minecraft.getMinecraft();
        if (TEXTURES[index] == null) {
            try (InputStream stream =
                    mc.getResourceManager()
                            .getResource(
                                    new ResourceLocation(
                                            "exoarsenal", "pixelart/" + NAMES[index] + ".pixels"))
                            .getInputStream()) {
                DynamicTexture texture = new DynamicTexture(PixelArt.read(stream));
                texture.setBlurMipmap(false, false);
                TEXTURES[index] =
                        mc.getTextureManager()
                                .getDynamicTextureLocation("exoarsenal_" + NAMES[index], texture);
            } catch (IOException error) {
                throw new IllegalStateException(
                        "Invalid hand-authored pixel asset " + NAMES[index], error);
            }
        }
        mc.getTextureManager().bindTexture(TEXTURES[index]);
    }
}
