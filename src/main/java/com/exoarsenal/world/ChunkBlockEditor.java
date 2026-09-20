package com.exoarsenal.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public final class ChunkBlockEditor {
    private ChunkBlockEditor() {}

    public static void set(World world, Chunk chunk, BlockPos pos, IBlockState state) {
        int y = pos.getY();
        if (y < 0 || y >= 256) return;
        ExtendedBlockStorage[] storageArray = chunk.getBlockStorageArray();
        int section = y >> 4;
        ExtendedBlockStorage storage = storageArray[section];
        if (storage == Chunk.NULL_BLOCK_STORAGE) {
            if (state.getBlock() == Blocks.AIR) return;
            storage = new ExtendedBlockStorage(section << 4, world.provider.hasSkyLight());
            storageArray[section] = storage;
        }
        int localX = pos.getX() & 15;
        int localZ = pos.getZ() & 15;
        IBlockState oldState = storage.get(localX, y & 15, localZ);
        if (oldState.getBlock().hasTileEntity(oldState)) chunk.removeTileEntity(pos);
        storage.set(localX, y & 15, localZ, state);
    }
}
