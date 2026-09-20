package com.scapeandrun.frostbite.world;

import com.scapeandrun.frostbite.Frostbite;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraft.util.ResourceLocation;

@Mod.EventBusSubscriber(modid = Frostbite.MODID)
public final class ModBiomes {
    public static final Biome FROZEN_PLAINS = biome("frozen_plains", "Frozen Plains", 0.10F, 0.20F);
    public static final Biome FROZEN_FOREST = biome("frozen_forest", "Frozen Forest", 0.18F, 0.25F);
    public static final Biome FROZEN_JUNGLE = biome("frozen_jungle", "Frozen Jungle", 0.22F, 0.35F);
    public static final Biome FROZEN_DESERT = biome("frozen_desert", "Frozen Desert", 0.12F, 0.02F);
    public static final Biome FROZEN_RIVER = biome("frozen_river", "Frozen River", -0.45F, 0.0F);
    public static final Biome FROZEN_HILLS = biome("frozen_hills", "Frozen Hills", 0.85F, 0.45F);

    private static Biome biome(String id, String name, float height, float variation) {
        Biome.BiomeProperties p =
                new Biome.BiomeProperties(name)
                        .setBaseHeight(height)
                        .setHeightVariation(variation)
                        .setTemperature(-0.5F)
                        .setRainfall(0.9F)
                        .setSnowEnabled();
        Biome biome = new Biome(p) {};
        biome.setRegistryName(Frostbite.MODID, id);
        biome.topBlock = com.scapeandrun.frostbite.registry.ModContent.PERMAFROST.getDefaultState();
        biome.fillerBlock =
                com.scapeandrun.frostbite.registry.ModContent.FROZEN_STONE.getDefaultState();
        biome.decorator.treesPerChunk = -999;
        biome.decorator.flowersPerChunk = 0;
        biome.decorator.grassPerChunk = 0;
        return biome;
    }

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Biome> event) {
        event.getRegistry()
                .registerAll(
                        FROZEN_PLAINS,
                        FROZEN_FOREST,
                        FROZEN_JUNGLE,
                        FROZEN_DESERT,
                        FROZEN_RIVER,
                        FROZEN_HILLS);
        BiomeDictionary.addTypes(
                FROZEN_PLAINS,
                BiomeDictionary.Type.COLD,
                BiomeDictionary.Type.SNOWY,
                BiomeDictionary.Type.PLAINS,
                BiomeDictionary.Type.DRY);
        BiomeDictionary.addTypes(
                FROZEN_FOREST,
                BiomeDictionary.Type.COLD,
                BiomeDictionary.Type.SNOWY,
                BiomeDictionary.Type.FOREST);
        BiomeDictionary.addTypes(
                FROZEN_JUNGLE,
                BiomeDictionary.Type.COLD,
                BiomeDictionary.Type.SNOWY,
                BiomeDictionary.Type.JUNGLE,
                BiomeDictionary.Type.DENSE);
        BiomeDictionary.addTypes(
                FROZEN_DESERT,
                BiomeDictionary.Type.COLD,
                BiomeDictionary.Type.SNOWY,
                BiomeDictionary.Type.SANDY,
                BiomeDictionary.Type.DRY);
        BiomeDictionary.addTypes(
                FROZEN_RIVER,
                BiomeDictionary.Type.COLD,
                BiomeDictionary.Type.SNOWY,
                BiomeDictionary.Type.RIVER);
        BiomeDictionary.addTypes(
                FROZEN_HILLS,
                BiomeDictionary.Type.COLD,
                BiomeDictionary.Type.SNOWY,
                BiomeDictionary.Type.HILLS);
    }

    public static Biome[] all() {
        return new Biome[] {
            FROZEN_PLAINS, FROZEN_FOREST, FROZEN_JUNGLE, FROZEN_DESERT, FROZEN_RIVER, FROZEN_HILLS
        };
    }
}
