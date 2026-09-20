package com.scapeandrun.frostbite.world;

import com.scapeandrun.frostbite.registry.ModContent;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class WorldGenFrostSoilVein {
    public int generate(
            World world,
            Chunk chunk,
            Random random,
            BlockPos origin,
            int targetSize,
            int chunkX,
            int chunkZ) {
        if (chunk.getBlockState(origin).getBlock() != Blocks.STONE) return 0;
        final int minX = chunkX << 4;
        final int minZ = chunkZ << 4;
        final int maxX = minX + 15;
        final int maxZ = minZ + 15;
        List<BlockPos> frontier = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        frontier.add(origin);
        visited.add(origin.toLong());
        int placed = 0;
        while (placed < targetSize && !frontier.isEmpty()) {
            BlockPos pos = frontier.remove(random.nextInt(frontier.size()));
            if (chunk.getBlockState(pos).getBlock() != Blocks.STONE) continue;
            ChunkBlockEditor.set(world, chunk, pos, ModContent.FROSTBITTEN_SOIL.getDefaultState());
            placed++;
            EnumFacing[] directions = EnumFacing.values();
            for (int i = directions.length - 1; i > 0; i--) {
                int swap = random.nextInt(i + 1);
                EnumFacing temp = directions[i];
                directions[i] = directions[swap];
                directions[swap] = temp;
            }
            for (EnumFacing direction : directions) {
                BlockPos next = pos.offset(direction);
                if (next.getX() >= minX
                        && next.getX() <= maxX
                        && next.getZ() >= minZ
                        && next.getZ() <= maxZ
                        && next.getY() > 1
                        && next.getY() < 64
                        && visited.add(next.toLong())) frontier.add(next);
            }
        }
        return placed;
    }
}
