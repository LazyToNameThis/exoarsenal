package com.exoarsenal.world;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class FrigidSpawnBiomes {
    private FrigidSpawnBiomes() {}

    public static boolean isCold(Biome biome) {
        return biome != null
                && (BiomeDictionary.hasType(biome, BiomeDictionary.Type.COLD)
                        || BiomeDictionary.hasType(biome, BiomeDictionary.Type.SNOWY)
                        || biome.getDefaultTemperature() <= .15F);
    }

    public static Biome[] all() {
        java.util.List<Biome> result = new java.util.ArrayList<>();
        for (Biome biome : ForgeRegistries.BIOMES) if (isCold(biome)) result.add(biome);
        return result.toArray(new Biome[0]);
    }

    public static void register() {
        Biome[] biomes = all();
        net.minecraftforge.fml.common.registry.EntityRegistry.addSpawn(
                com.exoarsenal.entity.EntityFrigidRobot.Drone.class,
                12,
                1,
                2,
                net.minecraft.entity.EnumCreatureType.MONSTER,
                biomes);
        net.minecraftforge.fml.common.registry.EntityRegistry.addSpawn(
                com.exoarsenal.entity.EntityFrigidRobot.Amplifier.class,
                4,
                1,
                1,
                net.minecraft.entity.EnumCreatureType.MONSTER,
                biomes);
        net.minecraftforge.fml.common.registry.EntityRegistry.addSpawn(
                com.exoarsenal.entity.EntityFrigidRobot.Shielder.class,
                8,
                1,
                2,
                net.minecraft.entity.EnumCreatureType.MONSTER,
                biomes);
        net.minecraftforge.fml.common.registry.EntityRegistry.addSpawn(
                com.exoarsenal.entity.EntityFrigidRobot.Rover.class,
                8,
                1,
                2,
                net.minecraft.entity.EnumCreatureType.MONSTER,
                biomes);
    }
}
