package com.exoarsenal.expedition;

import com.exoarsenal.ExoArsenal;
import net.minecraft.world.biome.*;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.*;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = ExoArsenal.MODID)
public final class WildBiomes {
    public static final Biome JUNGLE =
            new BiomeJungle(
                    false,
                    new Biome.BiomeProperties("Lush Jungle")
                            .setTemperature(.95F)
                            .setRainfall(.95F)
                            .setBaseHeight(.15F)
                            .setHeightVariation(.3F)) {
                {
                    setRegistryName(ExoArsenal.MODID, "lush_jungle");
                }
            };
    public static final Biome CORRUPTION = new Evil(false), CRIMSON = new Evil(true);

    private static final class Evil extends Biome {
        private final boolean red;

        Evil(boolean red) {
            super(
                    new BiomeProperties(red ? "The Crimson" : "The Corruption")
                            .setTemperature(.7F)
                            .setRainfall(.35F)
                            .setBaseHeight(.17F)
                            .setHeightVariation(.25F));
            this.red = red;
            setRegistryName(ExoArsenal.MODID, red ? "crimson" : "corruption");
            topBlock = Blocks.GRASS.getDefaultState();
            fillerBlock = (red ? WildContent.CRIMSTONE : WildContent.EBONSTONE).getDefaultState();
            decorator.treesPerChunk = -999;
            decorator.grassPerChunk = 4;
            decorator.flowersPerChunk = 0;
            spawnableCreatureList.clear();
            spawnableMonsterList.clear();
        }

        @Override
        public int getGrassColorAtPos(net.minecraft.util.math.BlockPos pos) {
            return red ? 0xA64145 : 0x745282;
        }

        @Override
        public int getFoliageColorAtPos(net.minecraft.util.math.BlockPos pos) {
            return red ? 0xAD4D47 : 0x665278;
        }

        @Override
        public int getSkyColorByTemp(float temperature) {
            return red ? 0xA47877 : 0x77728D;
        }
    }

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Biome> event) {
        event.getRegistry().registerAll(JUNGLE, CORRUPTION, CRIMSON);
        BiomeDictionary.addTypes(
                JUNGLE,
                BiomeDictionary.Type.JUNGLE,
                BiomeDictionary.Type.WET,
                BiomeDictionary.Type.HOT,
                BiomeDictionary.Type.DENSE);
        BiomeDictionary.addTypes(
                CORRUPTION, BiomeDictionary.Type.SPOOKY, BiomeDictionary.Type.WASTELAND);
        BiomeDictionary.addTypes(
                CRIMSON, BiomeDictionary.Type.SPOOKY, BiomeDictionary.Type.WASTELAND);
        BiomeManager.addBiome(BiomeManager.BiomeType.WARM, new BiomeManager.BiomeEntry(JUNGLE, 6));
        BiomeManager.addBiome(
                BiomeManager.BiomeType.WARM, new BiomeManager.BiomeEntry(CORRUPTION, 5));
        BiomeManager.addBiome(BiomeManager.BiomeType.COOL, new BiomeManager.BiomeEntry(CRIMSON, 5));
        BiomeManager.addStrongholdBiome(JUNGLE);
    }

    public static boolean jungle(Biome biome) {
        return BiomeDictionary.hasType(biome, BiomeDictionary.Type.JUNGLE)
                && !BiomeDictionary.hasType(biome, BiomeDictionary.Type.COLD);
    }
}
