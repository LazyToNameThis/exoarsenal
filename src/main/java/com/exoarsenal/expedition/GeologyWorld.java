package com.exoarsenal.expedition;

import com.exoarsenal.world.ChunkBlockEditor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import java.util.Random;

public final class GeologyWorld implements IWorldGenerator {
    public static final int MARBLE = 0, GRANITE = 1, MUSHROOM = 2, SPIDER = 3, DESERT = 4;

    private static int regionKind(World world, int x, int z) {
        int kind = GeologyRules.cave(world.getSeed(), x, z);
        if (kind == DESERT
                && !net.minecraftforge.common.BiomeDictionary.hasType(
                        world.getBiomeProvider()
                                .getBiome(
                                        new BlockPos(
                                                GeologyRules.caveX(world.getSeed(), x, z),
                                                64,
                                                GeologyRules.caveZ(world.getSeed(), x, z))),
                        net.minecraftforge.common.BiomeDictionary.Type.SANDY)) return GRANITE;
        return kind;
    }

    public static int caveAt(World world, BlockPos pos) {
        if (world.provider.getDimension() != 0 || ExpeditionWorldGenerator.inSea(world, pos))
            return -1;
        net.minecraft.world.biome.Biome biome = world.getBiome(pos);
        if (WildBiomes.jungle(biome)
                || biome == WildBiomes.CORRUPTION
                || biome == WildBiomes.CRIMSON) return -1;
        int cx = GeologyRules.cell(pos.getX()), cz = GeologyRules.cell(pos.getZ());
        long seed = world.getSeed();
        double dx = (pos.getX() - GeologyRules.caveX(seed, cx, cz)) / 22D,
                dz = (pos.getZ() - GeologyRules.caveZ(seed, cx, cz)) / 19D,
                dy = (pos.getY() - GeologyRules.caveY(seed, cx, cz)) / 9D;
        return dx * dx + dy * dy + dz * dz < .9 ? regionKind(world, cx, cz) : -1;
    }

    @Override
    public void generate(
            Random random,
            int chunkX,
            int chunkZ,
            World world,
            IChunkGenerator generator,
            net.minecraft.world.chunk.IChunkProvider provider) {
        if (world.provider.getDimension() != 0
                || world.getWorldInfo().getTerrainType() == net.minecraft.world.WorldType.FLAT)
            return;
        Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
        int minX = chunkX * 16, minZ = chunkZ * 16;
        int cx = GeologyRules.cell(minX + 8), cz = GeologyRules.cell(minZ + 8);
        long seed = world.getSeed();
        int centerX = GeologyRules.caveX(seed, cx, cz),
                centerZ = GeologyRules.caveZ(seed, cx, cz),
                centerY = GeologyRules.caveY(seed, cx, cz),
                kind = regionKind(world, cx, cz);
        boolean changed = false;
        net.minecraft.world.biome.Biome biome =
                world.getBiomeProvider().getBiome(new BlockPos(minX + 8, 64, minZ + 8));
        if (!WildBiomes.jungle(biome)
                && biome != WildBiomes.CORRUPTION
                && biome != WildBiomes.CRIMSON
                && Math.abs(centerX - (minX + 8)) < 31
                && Math.abs(centerZ - (minZ + 8)) < 28) {
            for (int x = minX; x < minX + 16; x++)
                for (int z = minZ; z < minZ + 16; z++)
                    for (int y = centerY - 10; y <= centerY + 10; y++) {
                        BlockPos at = new BlockPos(x, y, z);
                        IBlockState old = chunk.getBlockState(at);
                        if (old.getBlock() != Blocks.STONE
                                && old.getBlock() != Blocks.DIRT
                                && old.getBlock()
                                        != com.exoarsenal.registry.ModContent.FROZEN_STONE)
                            continue;
                        double dx = (x - centerX) / 22D,
                                dz = (z - centerZ) / 19D,
                                dy = (y - centerY) / 9D,
                                d = dx * dx + dy * dy + dz * dz;
                        if (d > 1) continue;
                        IBlockState replacement = Blocks.AIR.getDefaultState();
                        if (d > .78 || y <= centerY - 5) {
                            switch (kind) {
                                case MARBLE:
                                    replacement = Blocks.STONE.getStateFromMeta(3);
                                    break;
                                case GRANITE:
                                    replacement = Blocks.STONE.getStateFromMeta(1);
                                    break;
                                case MUSHROOM:
                                    replacement =
                                            y == centerY - 5
                                                    ? Blocks.MYCELIUM.getDefaultState()
                                                    : Blocks.DIRT.getDefaultState();
                                    break;
                                case SPIDER:
                                    replacement = Blocks.STONE.getDefaultState();
                                    break;
                                default:
                                    replacement = Blocks.SANDSTONE.getDefaultState();
                                    break;
                            }
                        }
                        if (kind == SPIDER
                                && d < .78
                                && Math.floorMod(GeologyRules.mix(at.toLong() ^ seed), 17) == 0)
                            replacement = Blocks.WEB.getDefaultState();
                        if (kind == MUSHROOM
                                && y == centerY - 4
                                && d < .7
                                && chunk.getBlockState(at.down()).getBlock() == Blocks.MYCELIUM
                                && Math.floorMod(GeologyRules.mix(at.toLong() ^ seed), 9) == 0)
                            replacement = GeologyContent.MUSHROOM.getDefaultState();
                        ChunkBlockEditor.set(world, chunk, at, replacement);
                        changed = true;
                    }
        }
        changed |=
                veins(
                        world,
                        chunk,
                        random,
                        GeologyContent.ore(
                                GeologyRules.alternate(seed, 0)
                                        ? GeologyContent.Ore.TIN
                                        : GeologyContent.Ore.COPPER),
                        12,
                        8,
                        8,
                        76);
        changed |=
                veins(
                        world,
                        chunk,
                        random,
                        GeologyRules.alternate(seed, 1)
                                ? GeologyContent.ore(GeologyContent.Ore.LEAD)
                                : Blocks.IRON_ORE.getDefaultState(),
                        9,
                        7,
                        6,
                        58);
        changed |=
                veins(
                        world,
                        chunk,
                        random,
                        GeologyContent.ore(
                                GeologyRules.alternate(seed, 2)
                                        ? GeologyContent.Ore.TUNGSTEN
                                        : GeologyContent.Ore.SILVER),
                        7,
                        6,
                        5,
                        44);
        changed |=
                veins(
                        world,
                        chunk,
                        random,
                        GeologyRules.alternate(seed, 3)
                                ? GeologyContent.ore(GeologyContent.Ore.PLATINUM)
                                : Blocks.GOLD_ORE.getDefaultState(),
                        5,
                        5,
                        5,
                        32);
        changed |=
                veins(
                        world,
                        chunk,
                        random,
                        GeologyContent.ore(GeologyContent.Ore.AMETHYST),
                        3,
                        4,
                        8,
                        55);
        changed |=
                veins(
                        world,
                        chunk,
                        random,
                        GeologyContent.ore(GeologyContent.Ore.TOPAZ),
                        3,
                        4,
                        8,
                        48);
        changed |=
                veins(
                        world,
                        chunk,
                        random,
                        GeologyContent.ore(GeologyContent.Ore.SAPPHIRE),
                        2,
                        3,
                        6,
                        40);
        changed |= veins(world, chunk, random, Blocks.EMERALD_ORE.getDefaultState(), 2, 3, 6, 36);
        changed |=
                veins(
                        world,
                        chunk,
                        random,
                        GeologyContent.ore(GeologyContent.Ore.RUBY),
                        2,
                        3,
                        5,
                        28);
        changed |= veins(world, chunk, random, Blocks.DIAMOND_ORE.getDefaultState(), 1, 3, 5, 22);
        changed |=
                veins(
                        world,
                        chunk,
                        random,
                        GeologyRules.alternate(seed, 4)
                                ? GeologyContent.ore(GeologyContent.Ore.CRIMTANE)
                                : GeologyContent.ore(GeologyContent.Ore.DEMONITE),
                        1,
                        2,
                        5,
                        22);
        changed |= veins(world, chunk, random, GeologyContent.SILT.getDefaultState(), 4, 10, 8, 50);
        if (com.exoarsenal.world.FrigidSpawnBiomes.isCold(
                world.getBiome(new BlockPos(minX + 8, 64, minZ + 8))))
            changed |=
                    veins(
                            world,
                            chunk,
                            random,
                            GeologyContent.SLUSH.getDefaultState(),
                            5,
                            10,
                            10,
                            55);
        if (kind == DESERT) {
            changed |=
                    veins(
                            world,
                            chunk,
                            random,
                            GeologyContent.FOSSIL.getDefaultState(),
                            5,
                            8,
                            10,
                            45);
            changed |=
                    veins(
                            world,
                            chunk,
                            random,
                            GeologyContent.ore(GeologyContent.Ore.AMBER),
                            1,
                            2,
                            10,
                            35);
        }
        if (changed) {
            chunk.generateSkylightMap();
            chunk.markDirty();
        }
    }

    private static boolean veins(
            World world,
            Chunk chunk,
            Random random,
            IBlockState ore,
            int attempts,
            int size,
            int minY,
            int maxY) {
        boolean changed = false;
        for (int vein = 0; vein < attempts; vein++) {
            int x = 1 + random.nextInt(14),
                    z = 1 + random.nextInt(14),
                    y = minY + random.nextInt(maxY - minY + 1);
            for (int step = 0; step < size; step++) {
                BlockPos at = new BlockPos(chunk.x * 16 + x, y, chunk.z * 16 + z);
                IBlockState old = chunk.getBlockState(at);
                if (old.getBlock() == Blocks.STONE
                        || old.getBlock() == Blocks.SANDSTONE
                        || old.getBlock() == WildContent.MUD
                        || old.getBlock() == WildContent.EBONSTONE
                        || old.getBlock() == WildContent.CRIMSTONE
                        || old.getBlock() == com.exoarsenal.registry.ModContent.FROZEN_STONE) {
                    ChunkBlockEditor.set(world, chunk, at, ore);
                    changed = true;
                }
                x = Math.max(0, Math.min(15, x + random.nextInt(3) - 1));
                z = Math.max(0, Math.min(15, z + random.nextInt(3) - 1));
                y = Math.max(minY, Math.min(maxY, y + random.nextInt(3) - 1));
            }
        }
        return changed;
    }
}
