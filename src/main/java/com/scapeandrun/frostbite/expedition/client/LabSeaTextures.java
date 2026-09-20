package com.scapeandrun.frostbite.expedition.client;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.expedition.PixelArt;
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

@Mod.EventBusSubscriber(modid = Frostbite.MODID, value = Side.CLIENT)
public final class LabSeaTextures {
    public static final String[] NAMES = {
        "lab_plating", "lab_panel", "navystone", "eutrophic_sand", "sea_prism", "codebreaker"
    };

    @SubscribeEvent
    public static void stitch(TextureStitchEvent.Pre event) {
        for (String name : NAMES) event.getMap().setTextureEntry(new Sprite(name));
    }

    private static final class Sprite extends TextureAtlasSprite {
        private final String name;

        Sprite(String name) {
            super(Frostbite.MODID + ":blocks/" + name);
            this.name = name;
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
            try (InputStream input =
                    manager.getResource(
                                    new ResourceLocation(
                                            Frostbite.MODID, "pixelart/" + name + ".pixels"))
                            .getInputStream()) {
                BufferedImage art = PixelArt.read(input);
                setIconWidth(art.getWidth());
                setIconHeight(art.getHeight());
                int[][] levels =
                        new int
                                [net.minecraft.client.Minecraft.getMinecraft()
                                                .gameSettings
                                                .mipmapLevels
                                        + 1]
                                [];
                levels[0] =
                        art.getRGB(0, 0, art.getWidth(), art.getHeight(), null, 0, art.getWidth());
                setFramesTextureData(Collections.singletonList(levels));
                return false;
            } catch (IOException error) {
                throw new IllegalStateException("Invalid laboratory/sea texture " + name, error);
            }
        }
    }
}
