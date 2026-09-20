package com.exoarsenal.expedition;

import com.exoarsenal.world.ChunkBlockEditor;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.IWorldGenerator;
import java.util.Random;

public final class WildWorld implements IWorldGenerator {
    public static boolean natural(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.STONE
                || block == Blocks.DIRT
                || block == Blocks.GRASS
                || block == WildContent.MUD
                || block == WildContent.EBONSTONE
                || block == WildContent.CRIMSTONE;
    }

    @Override
    public void generate(
            Random random,
            int chunkX,
            int chunkZ,
            World world,
            net.minecraft.world.gen.IChunkGenerator generator,
            net.minecraft.world.chunk.IChunkProvider provider) {
        if (world.provider.getDimension() != 0 || world.getWorldType() == WorldType.FLAT) return;
        Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
        boolean changed = false;
        long seed = world.getSeed();
        for (int lx = 0; lx < 16; lx++)
            for (int lz = 0; lz < 16; lz++) {
                int x = chunkX * 16 + lx, z = chunkZ * 16 + lz;
                Biome biome = world.getBiomeProvider().getBiome(new BlockPos(x, 64, z));
                boolean jungle = WildBiomes.jungle(biome),
                        red = biome == WildBiomes.CRIMSON,
                        evil = red || biome == WildBiomes.CORRUPTION;
                if (!jungle && !evil) continue;
                int surface = chunk.getHeightValue(lx, lz) - 1;
                for (int y = 7; y <= Math.min(110, evil ? surface : surface - 1); y++) {
                    BlockPos at = new BlockPos(x, y, z);
                    IBlockState old = chunk.getBlockState(at);
                    if (!natural(old)
                            || chunk.getTileEntity(at, Chunk.EnumCreateEntityType.CHECK) != null)
                        continue;
                    IBlockState state = old;
                    if (jungle && y < 53) {
                        double tunnel = WildWorldRules.jungleField(x, y, z, seed);
                        if (tunnel < .24) state = Blocks.AIR.getDefaultState();
                        else if (tunnel < .53) state = WildContent.MUD.getDefaultState();
                    } else if (evil) {
                        state =
                                y == surface
                                        ? old
                                        : (red ? WildContent.CRIMSTONE : WildContent.EBONSTONE)
                                                .getDefaultState();
                        if (WildWorldRules.evilCavity(x, y, z, surface, seed, red))
                            state = Blocks.AIR.getDefaultState();
                    }
                    if (state != old) {
                        ChunkBlockEditor.set(world, chunk, at, state);
                        changed = true;
                    }
                }
                if (jungle)
                    for (int y = 8; y < 52; y++) {
                        BlockPos at = new BlockPos(x, y, z);
                        if (!chunk.getBlockState(at).getMaterial().isReplaceable()) continue;
                        if (chunk.getBlockState(at).getMaterial().isLiquid()) continue;
                        long hash = GeologyRules.mix(at.toLong() ^ seed);
                        if (chunk.getBlockState(at.down()).getBlock() == WildContent.MUD
                                && Math.floorMod(hash, 39) == 0) {
                            ChunkBlockEditor.set(
                                    world, chunk, at, WildContent.SPORE_PLANT.getDefaultState());
                            changed = true;
                        } else if (chunk.getBlockState(at.up()).getBlock() == WildContent.MUD
                                && Math.floorMod(hash, 7) == 0) {
                            ChunkBlockEditor.set(
                                    world,
                                    chunk,
                                    at,
                                    Blocks.LEAVES
                                            .getDefaultState()
                                            .withProperty(
                                                    net.minecraft.block.BlockLeaves.CHECK_DECAY,
                                                    false)
                                            .withProperty(
                                                    net.minecraft.block.BlockLeaves.DECAYABLE,
                                                    false));
                            changed = true;
                        }
                    }
            }
        if (WildBiomes.jungle(
                        world.getBiomeProvider()
                                .getBiome(new BlockPos(chunkX * 16 + 8, 64, chunkZ * 16 + 8)))
                && random.nextInt(8) == 0) changed |= shrine(world, chunk, random);
        if (changed) {
            chunk.generateSkylightMap();
            chunk.markDirty();
        }
    }

    private static boolean shrine(World world, Chunk chunk, Random random) {
        int x = chunk.x * 16 + 8, z = chunk.z * 16 + 8;
        for (int y = 44; y >= 12; y--) {
            BlockPos center = new BlockPos(x, y, z);
            if (chunk.getBlockState(center.down()).getBlock() != WildContent.MUD
                    || chunk.getBlockState(center).getBlock() != Blocks.AIR) continue;
            boolean clear = true;
            for (int dx = -3; dx <= 3; dx++)
                for (int dz = -3; dz <= 3; dz++)
                    for (int dy = -1; dy <= 5; dy++) {
                        BlockPos p = center.add(dx, dy, dz);
                        IBlockState state = chunk.getBlockState(p);
                        if (state.getBlock() != Blocks.AIR
                                && !natural(state)
                                && state.getBlock() != WildContent.SPORE_PLANT) clear = false;
                        if (chunk.getTileEntity(p, Chunk.EnumCreateEntityType.CHECK) != null)
                            clear = false;
                    }
            if (!clear) continue;
            for (int dx = -3; dx <= 3; dx++)
                for (int dz = -3; dz <= 3; dz++) {
                    ChunkBlockEditor.set(
                            world,
                            chunk,
                            center.add(dx, -1, dz),
                            Blocks.MOSSY_COBBLESTONE.getDefaultState());
                    for (int dy = 0; dy <= 4; dy++)
                        ChunkBlockEditor.set(
                                world,
                                chunk,
                                center.add(dx, dy, dz),
                                Math.abs(dx) == 3 && Math.abs(dz) == 3
                                        ? Blocks.LOG.getStateFromMeta(3)
                                        : Blocks.AIR.getDefaultState());
                    int roof = 4 + (Math.abs(dx) <= 1 ? 1 : 0);
                    ChunkBlockEditor.set(
                            world,
                            chunk,
                            center.add(dx, roof, dz),
                            Blocks.LEAVES
                                    .getDefaultState()
                                    .withProperty(
                                            net.minecraft.block.BlockLeaves.CHECK_DECAY, false)
                                    .withProperty(
                                            net.minecraft.block.BlockLeaves.DECAYABLE, false));
                }

            for (int dx = -2; dx <= 2; dx++) {
                ChunkBlockEditor.set(
                        world,
                        chunk,
                        center.add(dx, 0, 2),
                        Blocks.STONEBRICK
                                .getDefaultState()
                                .withProperty(
                                        net.minecraft.block.BlockStoneBrick.VARIANT,
                                        net.minecraft.block.BlockStoneBrick.EnumType.MOSSY));
                ChunkBlockEditor.set(
                        world, chunk, center.add(dx, 3, -3), Blocks.LOG.getStateFromMeta(7));
            }
            for (int dx : new int[] {-2, 2}) {
                ChunkBlockEditor.set(
                        world,
                        chunk,
                        center.add(dx, 1, 2),
                        WildContent.SPORE_PLANT.getDefaultState());
                ChunkBlockEditor.set(
                        world,
                        chunk,
                        center.add(dx, 0, -2),
                        Blocks.STONE_BRICK_STAIRS
                                .getDefaultState()
                                .withProperty(
                                        net.minecraft.block.BlockStairs.FACING,
                                        net.minecraft.util.EnumFacing.SOUTH));
            }
            ChunkBlockEditor.set(world, chunk, center, Blocks.CHEST.getDefaultState());
            net.minecraft.tileentity.TileEntityChest chest =
                    new net.minecraft.tileentity.TileEntityChest();
            chest.setWorld(world);
            chest.setPos(center);
            chunk.addTileEntity(center, chest);
            chest.setInventorySlotContents(
                    4,
                    new net.minecraft.item.ItemStack(
                            random.nextBoolean()
                                    ? PrebossContent.FERAL_CLAWS
                                    : PrebossContent.WIND_ANKLET));
            chest.setInventorySlotContents(
                    10,
                    new net.minecraft.item.ItemStack(WildContent.SPORES, 3 + random.nextInt(5)));
            chest.setInventorySlotContents(
                    12, new net.minecraft.item.ItemStack(ExpeditionContent.HEALING_POTION, 2));
            chest.setInventorySlotContents(
                    14,
                    new net.minecraft.item.ItemStack(
                            PrebossContent.RECALL_POTION, 1 + random.nextInt(2)));
            chest.setInventorySlotContents(22, new net.minecraft.item.ItemStack(Blocks.TORCH, 8));
            chest.markDirty();
            return true;
        }
        return false;
    }
}
