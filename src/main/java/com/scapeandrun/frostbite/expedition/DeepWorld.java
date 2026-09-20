package com.scapeandrun.frostbite.expedition;

import com.scapeandrun.frostbite.world.ChunkBlockEditor;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.IWorldGenerator;
import java.util.*;

public final class DeepWorld implements IWorldGenerator {
    public static boolean dungeonAt(World world, BlockPos pos) {
        if (pos.getY() > 58) return false;
        int found = 0;
        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos p = pos.add(dx, -1, dz);
                if (world.isBlockLoaded(p)
                        && world.getBlockState(p).getBlock() == DeepContent.DUNGEON_BRICK) found++;
            }
        return found >= 4;
    }

    @Override
    public void generate(
            Random random,
            int cx,
            int cz,
            World world,
            net.minecraft.world.gen.IChunkGenerator generator,
            net.minecraft.world.chunk.IChunkProvider provider) {
        if (world.getWorldType() == WorldType.FLAT
                || world.provider.getDimension() != 0 && world.provider.getDimension() != -1)
            return;
        Slice slice = new Slice(world, world.getChunkFromChunkCoords(cx, cz));
        boolean hell = world.provider.getDimension() == -1;
        int cellX = Math.floorDiv(cx, 32), cellZ = Math.floorDiv(cz, 32);
        BlockPos center =
                new BlockPos(
                        DeepWorldRules.center(world.getSeed(), cellX, cellZ, true),
                        hell ? 42 : 30,
                        DeepWorldRules.center(world.getSeed(), cellX, cellZ, false));
        if (hell) {
            lab(slice, center, true);
            ruin(
                    slice,
                    new BlockPos(
                            Math.floorDiv(cx, 8) * 128 + 64, 38, Math.floorDiv(cz, 8) * 128 + 64));
            for (int i = 0; i < 9; i++) {
                BlockPos ore =
                        new BlockPos(
                                cx * 16 + random.nextInt(16),
                                12 + random.nextInt(78),
                                cz * 16 + random.nextInt(16));
                for (int j = 0; j < 4; j++) {
                    BlockPos p = ore.add(j % 2, j / 2, 0);
                    if (slice.contains(p)
                            && slice.chunk.getBlockState(p).getBlock() == Blocks.NETHERRACK)
                        slice.set(p, DeepContent.HELLSTONE_ORE.getDefaultState());
                }
            }
        } else {
            Biome biome = world.getBiomeProvider().getBiome(center);
            if (WildBiomes.jungle(biome)) lab(slice, center, false);
            BlockPos dungeon = center.add(128, 6, 128);

            dungeon =
                    new BlockPos(
                            Math.floorDiv(dungeon.getX(), 16) * 16 + 8,
                            dungeon.getY(),
                            Math.floorDiv(dungeon.getZ(), 16) * 16 + 8);
            Biome b = world.getBiomeProvider().getBiome(dungeon);
            if (!BiomeDictionary.hasType(b, BiomeDictionary.Type.OCEAN)
                    && !BiomeDictionary.hasType(b, BiomeDictionary.Type.RIVER)
                    && !BiomeDictionary.hasType(b, BiomeDictionary.Type.SANDY)
                    && !WildBiomes.jungle(b)
                    && b.getBaseHeight() < .4F) dungeon(slice, dungeon);
        }
        slice.finish();
    }

    private static void dungeon(Slice s, BlockPos c) {
        if (!s.overlaps(c, 27, 20)) return;

        room(s, c, 10, 6, 6, DeepContent.DUNGEON_BRICK);
        room(s, c.add(-19, 0, 0), 7, 6, 8, DeepContent.DUNGEON_BRICK);
        room(s, c.add(19, 0, 0), 7, 6, 8, DeepContent.DUNGEON_BRICK);
        room(s, c.add(0, -9, 13), 9, 6, 6, DeepContent.DUNGEON_BRICK);

        for (int x = -19; x <= 19; x++)
            for (int y = -1; y <= 3; y++)
                for (int z = -2; z <= 2; z++)
                    if (y == -1 || y == 3 || Math.abs(z) == 2)
                        s.set(c.add(x, y, z), DeepContent.DUNGEON_BRICK.getDefaultState());
        for (int x = -19; x <= 19; x++)
            for (int y = 0; y <= 2; y++)
                for (int z = -1; z <= 1; z++) s.set(c.add(x, y, z), Blocks.AIR.getDefaultState());
        for (int step = 0; step <= 9; step++)
            for (int x = -1; x <= 1; x++) {
                s.set(c.add(x, -step, 4 + step), DeepContent.DUNGEON_BRICK.getDefaultState());
                for (int y = 1; y <= 3; y++)
                    s.set(c.add(x, -step + y, 4 + step), Blocks.AIR.getDefaultState());
            }
        for (int side : new int[] {-1, 1}) {
            s.set(c.add(side * 22, 0, -5), DeepContent.DUNGEON_CACHE.getDefaultState());
            for (int z = -5; z <= 5; z += 5) {
                s.set(c.add(side * 25, 0, z), Blocks.BOOKSHELF.getDefaultState());
                s.set(c.add(side * 25, 1, z), Blocks.BOOKSHELF.getDefaultState());
            }
        }
        s.set(c.add(0, -9, 16), DeepContent.DUNGEON_CACHE.getDefaultState());

        for (int side : new int[] {-1, 1})
            for (int z : new int[] {-6, 6}) {
                for (int y = 0; y < 5; y++)
                    s.set(c.add(side * 14, y, z), Blocks.STONEBRICK.getDefaultState());
                for (int x = 14; x <= 24; x++)
                    s.set(c.add(side * x, 5, z), Blocks.STONEBRICK.getDefaultState());
                s.set(c.add(side * 19, 4, z), Blocks.SEA_LANTERN.getDefaultState());
                s.set(c.add(side * 19, 3, z), Blocks.IRON_BARS.getDefaultState());
            }
        for (int x : new int[] {-7, 7})
            for (int z : new int[] {10, 15}) {
                for (int y = -9; y <= -7; y++)
                    s.set(c.add(x, y, z), Blocks.IRON_BARS.getDefaultState());
                s.set(c.add(x, -6, z), Blocks.STONE_SLAB.getDefaultState());
            }
        if (s.contains(c)) {
            int top =
                    Math.max(
                            65,
                            Math.min(
                                    105, s.chunk.getHeightValue(c.getX() & 15, c.getZ() & 15) + 4));
            for (int y = c.getY(); y <= top; y++)
                for (int dx = -2; dx <= 2; dx++)
                    for (int dz = -2; dz <= 2; dz++)
                        s.set(
                                new BlockPos(c.getX() + dx, y, c.getZ() + dz),
                                Math.abs(dx) == 2 || Math.abs(dz) == 2
                                        ? DeepContent.DUNGEON_BRICK.getDefaultState()
                                        : Blocks.AIR.getDefaultState());
            for (int y = c.getY(); y <= top; y++)
                s.set(
                        new BlockPos(c.getX(), y, c.getZ() + 1),
                        Blocks.LADDER
                                .getDefaultState()
                                .withProperty(BlockLadder.FACING, EnumFacing.NORTH));
            for (int y = top - 2; y <= top; y++)
                for (int x = -1; x <= 1; x++)
                    s.set(
                            new BlockPos(c.getX() + x, y, c.getZ() - 2),
                            Blocks.AIR.getDefaultState());
        }
    }

    private static void room(Slice s, BlockPos c, int rx, int height, int rz, Block brick) {
        for (int x = -rx; x <= rx; x++)
            for (int z = -rz; z <= rz; z++)
                for (int y = -1; y <= height; y++) {
                    boolean wall = Math.abs(x) == rx || Math.abs(z) == rz || y == -1 || y == height;
                    s.set(
                            c.add(x, y, z),
                            wall ? brick.getDefaultState() : Blocks.AIR.getDefaultState());
                }
    }

    private static void lab(Slice s, BlockPos c, boolean hell) {
        if (!s.overlaps(c, 25, 12)) return;

        Map<BlockPos, IBlockState> plan = new LinkedHashMap<>();
        LabArchitecture.build(
                plan::put, c, hell ? LabArchitecture.Theme.THERMAL : LabArchitecture.Theme.PLAGUE);
        plan.forEach(s::set);
        s.chest(c.add(-22, 0, 7), hell ? 2 : 1);
        s.chest(c.add(22, 0, 7), hell ? 2 : 1);
    }

    private static void ruin(Slice s, BlockPos c) {
        if (!s.overlaps(c, 8, 7)) return;
        room(s, c, 7, 11, 6, DeepContent.OBSIDIAN_BRICK);
        for (int x = -5; x <= 5; x++)
            for (int z = -4; z <= 4; z++) s.set(c.add(x, -2, z), DeepContent.ASH.getDefaultState());

        for (int x = -6; x <= 6; x++)
            for (int z = -5; z <= 5; z++)
                if (Math.abs(x) >= 4 || Math.abs(z) >= 3)
                    s.set(c.add(x, 4, z), DeepContent.OBSIDIAN_BRICK.getDefaultState());
        for (int step = 0; step < 5; step++)
            for (int x = -6; x <= -5; x++) {
                for (int y = step; y <= step + 3; y++)
                    s.set(c.add(x, y, -4 + step), Blocks.AIR.getDefaultState());
                s.set(
                        c.add(x, step, -4 + step),
                        Blocks.NETHER_BRICK_STAIRS
                                .getDefaultState()
                                .withProperty(BlockStairs.FACING, EnumFacing.SOUTH));
            }
        for (int z = -2; z <= 2; z++) {
            s.set(c.add(-4, 5, z), Blocks.NETHER_BRICK_FENCE.getDefaultState());
            s.set(c.add(4, 5, z), Blocks.NETHER_BRICK_FENCE.getDefaultState());
        }
        for (int x = -3; x <= 3; x++) {
            s.set(c.add(x, 5, -3), Blocks.NETHER_BRICK_FENCE.getDefaultState());
            s.set(c.add(x, 5, 3), Blocks.NETHER_BRICK_FENCE.getDefaultState());
        }
        for (int y = 0; y < 3; y++)
            for (int x = -1; x <= 1; x++) s.set(c.add(x, y, -6), Blocks.AIR.getDefaultState());
        for (int x : new int[] {-4, 4})
            for (int y : new int[] {2, 3, 7, 8})
                s.set(c.add(x, y, 6), Blocks.IRON_BARS.getDefaultState());

        for (int x = 3; x <= 7; x++)
            for (int z = -6; z <= -2; z++)
                if (x - z > 7) s.set(c.add(x, 11, z), Blocks.AIR.getDefaultState());
        for (int y = 7; y <= 10; y++)
            for (int z = -4; z <= -2; z++)
                if (y + z > 4) s.set(c.add(-7, y, z), Blocks.AIR.getDefaultState());
        s.set(c.add(5, 0, 1), Blocks.ANVIL.getDefaultState());
        s.set(
                c.add(5, 0, 3),
                Blocks.FURNACE
                        .getDefaultState()
                        .withProperty(BlockFurnace.FACING, EnumFacing.WEST));
        s.set(c.add(4, 5, 4), DeepContent.SHADOW_CACHE.getDefaultState());
        for (int x = -7; x <= 7; x += 14)
            for (int z = -6; z <= 6; z += 12)
                for (int y = -2; y >= -12; y--)
                    s.set(c.add(x, y, z), DeepContent.OBSIDIAN_BRICK.getDefaultState());
    }

    private static final class Slice {
        final World world;
        final Chunk chunk;
        boolean changed;
        final Map<BlockPos, Integer> chests = new LinkedHashMap<>();

        Slice(World w, Chunk c) {
            world = w;
            chunk = c;
        }

        boolean contains(BlockPos p) {
            return Math.floorDiv(p.getX(), 16) == chunk.x
                    && Math.floorDiv(p.getZ(), 16) == chunk.z
                    && p.getY() > 5
                    && p.getY() < 120;
        }

        boolean overlaps(BlockPos c, int rx, int rz) {
            return c.getX() + rx >= chunk.x * 16
                    && c.getX() - rx < chunk.x * 16 + 16
                    && c.getZ() + rz >= chunk.z * 16
                    && c.getZ() - rz < chunk.z * 16 + 16;
        }

        void set(BlockPos p, IBlockState state) {
            if (!contains(p)) return;
            IBlockState old = chunk.getBlockState(p);
            if (old.getBlock() == Blocks.BEDROCK || old.getBlock().hasTileEntity(old)) return;
            Block b = old.getBlock();
            boolean allowed =
                    old.getMaterial() == Material.AIR
                            || b == Blocks.STONE
                            || b == Blocks.DIRT
                            || b == Blocks.GRASS
                            || b == Blocks.NETHERRACK
                            || b == Blocks.SOUL_SAND
                            || b == Blocks.MAGMA
                            || b == Blocks.GRAVEL
                            || b == Blocks.LAVA
                            || b == Blocks.FLOWING_LAVA
                            || b == WildContent.MUD
                            || b == WildContent.SPORE_PLANT
                            || b == Blocks.LEAVES
                            || b == Blocks.LOG
                            || b == DeepContent.DUNGEON_BRICK
                            || b == DeepContent.OBSIDIAN_BRICK
                            || b == DeepContent.ASH
                            || b == DeepContent.PLAGUEPLATE
                            || b == DeepContent.THERMAL_PLATING
                            || b == ExpeditionContent.LAB_PLATING
                            || b == Blocks.GLASS
                            || b == Blocks.LADDER;
            if (!allowed) return;
            ChunkBlockEditor.set(world, chunk, p, state);
            changed = true;
        }

        void chest(BlockPos p, int type) {
            if (!contains(p)) return;
            set(p, Blocks.CHEST.getDefaultState());
            if (chunk.getBlockState(p).getBlock() == Blocks.CHEST) chests.put(p, type);
        }

        void finish() {
            for (Map.Entry<BlockPos, Integer> entry : chests.entrySet()) {
                BlockPos p = entry.getKey();
                if (chunk.getTileEntity(p, Chunk.EnumCreateEntityType.CHECK) != null) continue;
                TileEntityChest chest = new TileEntityChest();
                chest.setWorld(world);
                chest.setPos(p);
                chunk.addTileEntity(p, chest);
                boolean hell = entry.getValue() == 2;
                chest.setInventorySlotContents(
                        4,
                        new ItemStack(
                                hell ? DeepContent.HELL_SCHEMATIC : DeepContent.JUNGLE_SCHEMATIC));
                chest.setInventorySlotContents(
                        6, new ItemStack(hell ? DeepContent.HELL_LOG : DeepContent.JUNGLE_LOG));
                chest.setInventorySlotContents(12, new ItemStack(ExpeditionContent.CIRCUITRY, 12));
                chest.setInventorySlotContents(
                        14, new ItemStack(ExpeditionContent.DUBIOUS_PLATING, 16));
                chest.setInventorySlotContents(22, new ItemStack(ExpeditionContent.POWER_CELL, 8));
                chest.markDirty();
            }
            if (changed) {
                chunk.generateSkylightMap();
                chunk.markDirty();
            }
        }
    }
}
