package com.scapeandrun.frostbite.expedition.client;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.expedition.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import java.io.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = Frostbite.MODID, value = Side.CLIENT)
public final class GeologyClient {
    private static final Map<GeologyContent.Ore, ExpeditionMesh> MODELS =
            new EnumMap<>(GeologyContent.Ore.class);

    @SubscribeEvent
    public static void stitch(TextureStitchEvent.Pre event) {
        for (GeologyContent.Ore ore : GeologyContent.Ore.values())
            event.getMap().setTextureEntry(new Pixels(ore.id));
        for (String id :
                new String[] {
                    "ebonstone", "crimstone", "dungeon_brick", "obsidian_brick", "hellstone"
                }) event.getMap().setTextureEntry(new Pixels(id));
    }

    @SubscribeEvent(priority = net.minecraftforge.fml.common.eventhandler.EventPriority.LOWEST)
    public static void models(ModelRegistryEvent event) {
        for (WildModule.Entry entry : WildModule.ENTRIES)
            net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(
                    entry.type, WildRender::new);
        net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(
                EntityWildBolt.class, WildRender.Bolt::new);
        net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(
                EntityCaveMob.GraniteGolem.class, CaveMobRender::new);
        net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(
                EntityCaveMob.GraniteElemental.class, CaveMobRender::new);
        net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(
                EntityCaveMob.Hoplite.class, CaveMobRender::new);
        net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(
                EntityCaveMob.SporeBat.class, CaveMobRender::new);
        net.minecraftforge.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(
                EntityCaveMob.WallCreeper.class, CaveMobRender::new);
        for (GeologyContent.Ore ore : GeologyContent.Ore.values()) {
            ModelLoader.setCustomModelResourceLocation(
                    Item.getItemFromBlock(GeologyContent.ORE),
                    ore.ordinal(),
                    new ModelResourceLocation(
                            GeologyContent.ORE.getRegistryName(), "type=" + ore.id));
            ModelLoader.setCustomModelResourceLocation(
                    GeologyContent.MATERIAL,
                    ore.ordinal(),
                    new ModelResourceLocation(Frostbite.MODID + ":expedition_item", "inventory"));
        }
    }

    public static ExpeditionMesh material(int meta) {
        GeologyContent.Ore ore = GeologyContent.Ore.at(meta);
        return MODELS.computeIfAbsent(
                ore,
                key -> {
                    ExpeditionMesh mesh = new ExpeditionMesh();
                    if (key.gem) {
                        mesh.tube(0, -5, 0, 0, 1, 0, .15, 3.7, key.color)
                                .tube(0, 1, 0, 0, 4, 0, 3.7, 2.2, key.color)
                                .box(-1, 3, -1, 2, 1, 2, 0xDCEAF0)
                                .tube(-2, -2, -1.8, -1, 2, -3, .3, .2, 0xDCEAF0);
                    } else
                        mesh.box(-5, -3, -2.5, 10, 2, 5, 0x41494F)
                                .box(-4.5, -1, -2, 9, 3, 4, key.color)
                                .box(-3.5, 2, -1.5, 7, 1, 3, key.color)
                                .box(-4, -.5, -2.2, 7, .5, .3, 0xD4DCDD);
                    return mesh;
                });
    }

    private static final class Pixels extends TextureAtlasSprite {
        private final String id;

        Pixels(String id) {
            super(Frostbite.MODID + ":blocks/ore_" + id);
            this.id = id;
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
                                            Frostbite.MODID, "pixelart/ore_" + id + ".pixels"))
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
                throw new IllegalStateException("Missing authored ore texture " + id, error);
            }
        }
    }
}
