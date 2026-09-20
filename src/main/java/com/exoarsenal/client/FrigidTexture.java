package com.exoarsenal.client;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.expedition.PixelArt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public final class FrigidTexture {
    private static ResourceLocation location;
    private static boolean listening;

    private FrigidTexture() {}

    public static ResourceLocation location() {
        Minecraft mc = Minecraft.getMinecraft();
        if (!listening && mc.getResourceManager() instanceof IReloadableResourceManager) {
            listening = true;
            ((IReloadableResourceManager) mc.getResourceManager())
                    .registerReloadListener(
                            manager -> {
                                if (location != null) {
                                    mc.getTextureManager().deleteTexture(location);
                                    location = null;
                                }
                            });
        }
        if (location == null) {
            try (InputStream input =
                    mc.getResourceManager()
                            .getResource(
                                    new ResourceLocation(
                                            ExoArsenal.MODID, "textures/entity/x20_scout_v2.png"))
                            .getInputStream()) {
                BufferedImage atlas = ImageIO.read(input);
                java.util.List<BufferedImage> tiles = new java.util.ArrayList<>();
                for (String name : FrigidMaterials.TILES)
                    try (InputStream pixels =
                            mc.getResourceManager()
                                    .getResource(
                                            new ResourceLocation(
                                                    ExoArsenal.MODID,
                                                    "pixelart/frigid_" + name + ".pixels"))
                                    .getInputStream()) {
                        tiles.add(PixelArt.read(pixels));
                    }
                DynamicTexture texture = new DynamicTexture(FrigidMaterials.compose(atlas, tiles));
                texture.setBlurMipmap(false, false);
                location =
                        mc.getTextureManager()
                                .getDynamicTextureLocation("exoarsenal_frigid_materials", texture);
            } catch (IOException error) {
                throw new IllegalStateException(
                        "Cannot load authored Frigid material atlas", error);
            }
        }
        return location;
    }
}
