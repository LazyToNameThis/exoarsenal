package com.scapeandrun.frostbite.client;

import com.scapeandrun.frostbite.Frostbite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Collections;
import java.util.function.Function;
import com.scapeandrun.frostbite.expedition.PixelArt;

@Mod.EventBusSubscriber(modid = Frostbite.MODID, value = Side.CLIENT)
public final class ControllerAtlasTexture extends TextureAtlasSprite {
    private ControllerAtlasTexture() {
        super(Frostbite.MODID + ":items/suspicious_controller");
    }

    @SubscribeEvent
    public static void stitch(TextureStitchEvent.Pre event) {
        event.getMap().setTextureEntry(new ControllerAtlasTexture());
    }

    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
        return true;
    }

    @Override
    public boolean load(
            IResourceManager manager,
            ResourceLocation location,
            Function<ResourceLocation, TextureAtlasSprite> getter) {
        try (InputStream stream =
                manager.getResource(
                                new ResourceLocation(
                                        Frostbite.MODID, "pixelart/suspicious_controller.pixels"))
                        .getInputStream()) {
            BufferedImage image = PixelArt.read(stream);
            if (image == null) throw new IOException("Invalid controller texture");
            setIconWidth(image.getWidth());
            setIconHeight(image.getHeight());
            int[][] levels =
                    new int
                            [net.minecraft.client.Minecraft.getMinecraft().gameSettings.mipmapLevels
                                    + 1]
                            [];
            levels[0] =
                    image.getRGB(
                            0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
            setFramesTextureData(Collections.singletonList(levels));
            return false;
        } catch (IOException failure) {
            throw new IllegalStateException("Cannot load controller model texture", failure);
        }
    }
}
