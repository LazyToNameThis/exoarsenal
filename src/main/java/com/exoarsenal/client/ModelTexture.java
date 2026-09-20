package com.exoarsenal.client;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.expedition.PixelArt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import java.io.*;
import java.util.*;

public final class ModelTexture {
    private static final Map<String, ResourceLocation> SKINS = new HashMap<>();
    private static boolean listening;

    private ModelTexture() {}

    public static ResourceLocation get(String name) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!listening && mc.getResourceManager() instanceof IReloadableResourceManager) {
            listening = true;
            ((IReloadableResourceManager) mc.getResourceManager())
                    .registerReloadListener(
                            manager -> {
                                for (ResourceLocation texture : SKINS.values())
                                    mc.getTextureManager().deleteTexture(texture);
                                SKINS.clear();
                            });
        }
        ResourceLocation result = SKINS.get(name);
        if (result != null) return result;
        try (InputStream input =
                mc.getResourceManager()
                        .getResource(
                                new ResourceLocation(
                                        ExoArsenal.MODID, "pixelart/" + name + ".pixels"))
                        .getInputStream()) {
            DynamicTexture skin = new DynamicTexture(PixelArt.read(input));
            skin.setBlurMipmap(false, false);
            result =
                    mc.getTextureManager()
                            .getDynamicTextureLocation("exoarsenal_skin_" + name, skin);
            SKINS.put(name, result);
            return result;
        } catch (IOException error) {
            throw new IllegalStateException("Cannot load whole model skin " + name, error);
        }
    }
}
