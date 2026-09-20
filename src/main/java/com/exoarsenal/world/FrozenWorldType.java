package com.exoarsenal.world;

import net.minecraft.world.WorldType;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraft.world.gen.layer.GenLayer;

public class FrozenWorldType extends WorldType {

    public FrozenWorldType() {
        super("frozen_wasteland");
    }

    @Override
    public boolean canBeCreated() {
        return false;
    }

    @Override
    public GenLayer getBiomeLayer(
            long worldSeed, GenLayer parentLayer, ChunkGeneratorSettings chunkSettings) {
        GenLayer layer = new GenLayerFrozenBiomes(1000L);
        layer.initWorldGenSeed(worldSeed);
        return layer;
    }
}
