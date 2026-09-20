package com.exoarsenal.world;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class GenLayerFrozenBiomes extends GenLayer {
    public GenLayerFrozenBiomes(long seed) {
        super(seed);
    }

    @Override
    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
        int[] out = IntCache.getIntCache(areaWidth * areaHeight);
        Biome[] land = {
            ModBiomes.FROZEN_PLAINS,
            ModBiomes.FROZEN_FOREST,
            ModBiomes.FROZEN_JUNGLE,
            ModBiomes.FROZEN_DESERT,
            ModBiomes.FROZEN_HILLS
        };
        for (int z = 0; z < areaHeight; z++) {
            for (int x = 0; x < areaWidth; x++) {
                int worldX = areaX + x;
                int worldZ = areaY + z;
                int riverSegment = Math.floorDiv(worldZ, 24);
                int localZ = Math.floorMod(worldZ, 24);
                int riverA = riverCenter(riverSegment);
                int riverB = riverCenter(riverSegment + 1);
                int riverX = riverA + (riverB - riverA) * localZ / 24;
                int repeatedX = Math.floorMod(worldX, 112);
                if (Math.abs(repeatedX - riverX) <= 2) {
                    out[x + z * areaWidth] = Biome.getIdForBiome(ModBiomes.FROZEN_RIVER);
                    continue;
                }
                int cellX = Math.floorDiv(worldX, 32);
                int cellZ = Math.floorDiv(worldZ, 32);
                initChunkSeed(cellX, cellZ);
                out[x + z * areaWidth] = Biome.getIdForBiome(land[nextInt(land.length)]);
            }
        }
        return out;
    }

    private int riverCenter(int segment) {
        initChunkSeed(0x5F3759DFL, segment);
        return 18 + nextInt(76);
    }
}
