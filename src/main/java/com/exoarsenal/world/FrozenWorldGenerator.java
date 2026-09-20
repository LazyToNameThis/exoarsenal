package com.exoarsenal.world;

import com.exoarsenal.config.ModConfig;
import com.exoarsenal.registry.ModContent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import java.util.Random;

public class FrozenWorldGenerator implements IWorldGenerator {
    @Override
    public void generate(
            Random random,
            int chunkX,
            int chunkZ,
            World world,
            IChunkGenerator chunkGenerator,
            IChunkProvider chunkProvider) {
        if (!FrozenWorldRules.isFrozenWasteland(world)) return;
        net.minecraft.world.chunk.Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
        int veinSize = 100 + random.nextInt(21);
        new WorldGenFrostSoilVein()
                .generate(
                        world,
                        chunk,
                        random,
                        new BlockPos(
                                (chunkX << 4) + random.nextInt(16),
                                8 + random.nextInt(48),
                                (chunkZ << 4) + random.nextInt(16)),
                        veinSize,
                        chunkX,
                        chunkZ);
        for (int attempt = 0; attempt < ModConfig.stumpChancePerChunk; attempt++) {
            int localX = random.nextInt(16);
            int localZ = random.nextInt(16);
            int x = (chunkX << 4) + localX;
            int z = (chunkZ << 4) + localZ;
            BlockPos ground =
                    new BlockPos(x, Math.max(1, chunk.getHeightValue(localX, localZ) - 1), z);
            if (ground.getY() < 2 || !chunk.getBlockState(ground).getMaterial().isSolid()) continue;
            int height = 3 + random.nextInt(3);
            for (int y = 1; y <= height; y++)
                ChunkBlockEditor.set(
                        world, chunk, ground.up(y), ModContent.FROZEN_LOG.getDefaultState());
        }
        chunk.generateSkylightMap();
        chunk.markDirty();
    }
}
