package com.exoarsenal.expedition;

import com.exoarsenal.world.ChunkBlockEditor;
import com.exoarsenal.world.FrigidSpawnBiomes;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.*;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.IWorldGenerator;
import java.util.*;

public final class ExpeditionWorldGenerator implements IWorldGenerator {
    public static final int SEA_CELL = 192, SPACE_CELL = 512;

    public static BlockPos seaCenter(long seed, int cellX, int cellZ) {
        Random r = new Random(seed ^ cellX * 341873128712L ^ cellZ * 132897987541L ^ 0x5EA71L);
        return new BlockPos(
                cellX * SEA_CELL + 64 + r.nextInt(64), 27, cellZ * SEA_CELL + 64 + r.nextInt(64));
    }

    public static BlockPos spaceCenter(long seed, int cellX, int cellZ) {
        Random r = new Random(seed ^ cellX * 341873128712L ^ cellZ * 132897987541L ^ 0x57ACE1L);
        return new BlockPos(
                cellX * SPACE_CELL + 128 + r.nextInt(256),
                194,
                cellZ * SPACE_CELL + 128 + r.nextInt(256));
    }

    private static boolean desert(World w, BlockPos p) {
        Biome b = w.getBiomeProvider().getBiome(p);
        return BiomeDictionary.hasType(b, BiomeDictionary.Type.SANDY)
                && !FrigidSpawnBiomes.isCold(b);
    }

    public static boolean seaSite(World w, BlockPos c) {
        return desert(w, c)
                && desert(w, c.add(35, 0, 0))
                && desert(w, c.add(-35, 0, 0))
                && desert(w, c.add(0, 0, 35))
                && desert(w, c.add(0, 0, -35));
    }

    public static boolean inSea(World w, BlockPos p) {
        if (w.provider.getDimension() != 0 || p.getY() < 8 || p.getY() > 48) return false;
        int cx = Math.floorDiv(p.getX(), SEA_CELL), cz = Math.floorDiv(p.getZ(), SEA_CELL);
        for (int x = cx - 1; x <= cx + 1; x++)
            for (int z = cz - 1; z <= cz + 1; z++) {
                BlockPos c = seaCenter(w.getSeed(), x, z);
                if (seaShape(p, c) < 1 && seaSite(w, c)) return true;
            }
        return false;
    }

    public static double seaShape(BlockPos p, BlockPos c) {
        double x = (p.getX() - c.getX()) / 64D, z = (p.getZ() - c.getZ()) / 52D;
        double ridge =
                Math.sin((p.getX() - c.getX()) * .085) * 1.4
                        + Math.cos((p.getZ() - c.getZ()) * .1) * 1.3;
        double y = (p.getY() - c.getY() - ridge) / 17;
        return x * x + z * z + y * y;
    }

    public static BlockPos nearestSite(World w, BlockPos p, boolean sea) {
        if (w.provider.getDimension() != 0) return null;
        int cell = sea ? SEA_CELL : SPACE_CELL, range = sea ? 11 : 2;
        BlockPos best = null;
        double distance = Double.MAX_VALUE;
        int bx = Math.floorDiv(p.getX(), cell), bz = Math.floorDiv(p.getZ(), cell);
        for (int x = bx - range; x <= bx + range; x++)
            for (int z = bz - range; z <= bz + range; z++) {
                BlockPos c = sea ? seaCenter(w.getSeed(), x, z) : spaceCenter(w.getSeed(), x, z);
                if (sea && !seaSite(w, c)) continue;
                double d = p.distanceSq(c);
                if (d < distance) {
                    best = sea ? c.add(22, -5, 0) : c;
                    distance = d;
                }
            }
        return best;
    }

    @Override
    public void generate(
            Random random,
            int chunkX,
            int chunkZ,
            World w,
            IChunkGenerator generator,
            IChunkProvider provider) {
        if (w.provider.getDimension() != 0
                || w.getWorldInfo().getTerrainType() == net.minecraft.world.WorldType.FLAT) return;
        Chunk chunk = w.getChunkFromChunkCoords(chunkX, chunkZ);
        Slice slice = new Slice(w, chunk);
        int x = chunkX * 16 + 8, z = chunkZ * 16 + 8;
        int sx = Math.floorDiv(x, SEA_CELL), sz = Math.floorDiv(z, SEA_CELL);
        for (int cx = sx - 1; cx <= sx + 1; cx++)
            for (int cz = sz - 1; cz <= sz + 1; cz++) {
                BlockPos c = seaCenter(w.getSeed(), cx, cz);
                if (Math.abs(c.getX() - x) < 80 && Math.abs(c.getZ() - z) < 68 && seaSite(w, c))
                    sea(slice, c);
            }
        int px = Math.floorDiv(x, SPACE_CELL), pz = Math.floorDiv(z, SPACE_CELL);
        for (int cx = px - 1; cx <= px + 1; cx++)
            for (int cz = pz - 1; cz <= pz + 1; cz++) {
                BlockPos c = spaceCenter(w.getSeed(), cx, cz);
                if (Math.abs(c.getX() - x) < 88 && Math.abs(c.getZ() - z) < 88) {
                    planetoid(slice, c, 22, true);
                    planetoid(slice, c.add(47, 20, -24), 9, false);
                    planetoid(slice, c.add(-43, -13, 29), 11, false);
                }
            }
        lifeCrystal(slice, random);
        slice.finish();
    }

    private static void lifeCrystal(Slice slice, Random random) {
        if (random.nextInt(5) != 0) return;
        for (int attempt = 0; attempt < 8; attempt++) {
            int x = slice.minX + 1 + random.nextInt(14), z = slice.minZ + 1 + random.nextInt(14);
            int start = 8 + random.nextInt(40);
            for (int step = 0; step < 40; step++) {
                BlockPos at = new BlockPos(x, 8 + (start - 8 + step) % 40, z);
                if (slice.chunk.getBlockState(at).getBlock() != Blocks.AIR
                        || slice.chunk.getBlockState(at.up()).getBlock() != Blocks.AIR) continue;
                Block below = slice.chunk.getBlockState(at.down()).getBlock();
                if (below != Blocks.STONE && below != Blocks.DIRT) continue;
                slice.set(at, PrebossContent.LIFE_FORMATION.getDefaultState());
                LifeCrystalBlock.CrystalTile tile = new LifeCrystalBlock.CrystalTile();
                tile.setWorld(slice.w);
                tile.setPos(at);
                slice.chunk.addTileEntity(at, tile);
                return;
            }
        }
    }

    private static void sea(Slice s, BlockPos c) {
        for (int x = s.minX; x <= s.maxX; x++)
            for (int z = s.minZ; z <= s.maxZ; z++)
                for (int y = 7; y <= 49; y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    double d = seaShape(p, c);
                    if (d > 1.12) continue;

                    if (s.chunk.getBlockState(p).getBlock().hasTileEntity(s.chunk.getBlockState(p)))
                        continue;
                    if (d > .88)
                        s.set(
                                p,
                                (y < c.getY() - 4
                                                ? ExpeditionContent.EUTROPHIC_SAND
                                                : ExpeditionContent.NAVYSTONE)
                                        .getDefaultState());
                    else
                        s.set(
                                p,
                                y <= 35
                                        ? Blocks.WATER.getDefaultState()
                                        : Blocks.AIR.getDefaultState());
                }

        geode(s, c.add(-8, 0, 0), 11);
        geode(s, c.add(-30, -2, -20), 6);
        geode(s, c.add(-7, 4, 27), 6);
        geode(s, c.add(31, 5, 22), 5);

        seaArch(s, c.add(-31, -8, 3), false);
        seaArch(s, c.add(3, -9, -25), true);
        for (int x = s.minX; x <= s.maxX; x++)
            for (int z = s.minZ; z <= s.maxZ; z++)
                for (int y = 9; y < 43; y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    long hash = x * 73428767L ^ z * 912931L ^ y * 73856093L ^ s.w.getSeed();
                    Block base = s.chunk.getBlockState(p).getBlock();
                    if ((base == ExpeditionContent.NAVYSTONE
                                    || base == ExpeditionContent.EUTROPHIC_SAND)
                            && s.chunk.getBlockState(p.up()).getMaterial()
                                    == net.minecraft.block.material.Material.WATER) {
                        if (Math.floorMod(hash, 83) == 0)
                            s.set(p.up(), ExpeditionContent.PRISM_CLUSTER.getDefaultState());

                        if (Math.floorMod(hash, 317) == 0) {
                            for (int dx = -2; dx <= 2; dx++)
                                for (int dz = -2; dz <= 2; dz++)
                                    if (dx * dx + dz * dz < 6)
                                        s.set(
                                                p.add(dx, 1, dz),
                                                ExpeditionContent.NAVYSTONE.getDefaultState());
                            s.set(p.up(2), ExpeditionContent.PRISM_CLUSTER.getDefaultState());
                        }
                    }
                }
        lab(s, c.add(22, -5, 0), false);
    }

    private static void seaArch(Slice s, BlockPos c, boolean turn) {
        for (int along = -8; along <= 8; along++) {
            int top = 7 - (int) Math.floor(Math.abs(along) * .45);
            for (int wide = -1; wide <= 1; wide++)
                for (int y = 0; y <= top + 1; y++) {
                    if (Math.abs(along) < 6 && y < top) continue;
                    BlockPos p = c.add(turn ? wide : along, y, turn ? along : wide);
                    if (!s.contains(p)) continue;
                    Block old = s.chunk.getBlockState(p).getBlock();
                    if (old != Blocks.WATER
                            && old != ExpeditionContent.NAVYSTONE
                            && old != ExpeditionContent.EUTROPHIC_SAND) continue;
                    s.set(
                            p,
                            (y == top + 1 && Math.abs(along) % 3 == 0
                                            ? ExpeditionContent.SEA_PRISM
                                            : ExpeditionContent.NAVYSTONE)
                                    .getDefaultState());
                }
        }
    }

    private static void geode(Slice s, BlockPos c, int radius) {
        for (int x = Math.max(s.minX, c.getX() - radius);
                x <= Math.min(s.maxX, c.getX() + radius);
                x++)
            for (int z = Math.max(s.minZ, c.getZ() - radius);
                    z <= Math.min(s.maxZ, c.getZ() + radius);
                    z++)
                for (int y = c.getY() - radius; y <= c.getY() + radius; y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    double d = p.distanceSq(c);
                    if (d > radius * radius) continue;
                    if (d > (radius - 1.2) * (radius - 1.2))
                        s.set(p, ExpeditionContent.NAVYSTONE.getDefaultState());
                    else if (d > (radius - 2.3) * (radius - 2.3))
                        s.set(p, ExpeditionContent.SEA_PRISM.getDefaultState());
                    else s.set(p, Blocks.WATER.getDefaultState());

                    if (Math.abs(x - c.getX() + z - c.getZ()) <= 1 && y < c.getY() + 2)
                        s.set(p, Blocks.WATER.getDefaultState());
                }
    }

    private static void planetoid(Slice s, BlockPos c, int radius, boolean main) {
        if (s.maxX < c.getX() - radius
                || s.minX > c.getX() + radius
                || s.maxZ < c.getZ() - radius
                || s.minZ > c.getZ() + radius) return;
        for (int x = Math.max(s.minX, c.getX() - radius);
                x <= Math.min(s.maxX, c.getX() + radius);
                x++)
            for (int z = Math.max(s.minZ, c.getZ() - radius);
                    z <= Math.min(s.maxZ, c.getZ() + radius);
                    z++)
                for (int y = c.getY() - radius; y <= c.getY() + radius; y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    double d = p.distanceSq(c);
                    if (d > radius * radius) continue;
                    if (s.chunk.getBlockState(p).getBlock().hasTileEntity(s.chunk.getBlockState(p)))
                        continue;
                    long ore =
                            Math.floorMod(
                                    x * 73856093L ^ y * 19349663L ^ z * 83492791L ^ s.w.getSeed(),
                                    97);
                    Block b =
                            d < (radius - 3) * (radius - 3)
                                    ? (ore < 5
                                            ? Blocks.IRON_ORE
                                            : ore < 9
                                                    ? Blocks.COAL_ORE
                                                    : ore == 10 ? Blocks.GOLD_ORE : Blocks.STONE)
                                    : y > c.getY() + radius * .55 ? Blocks.GRASS : Blocks.DIRT;
                    s.set(p, b.getDefaultState());
                }

        if (!main)
            for (int dx = -radius + 2; dx <= radius - 2; dx++)
                for (int dz = -radius + 2; dz <= radius - 2; dz++) {
                    double remaining = radius * radius - dx * dx - dz * dz;
                    if (remaining < 9) continue;
                    int top = (int) Math.floor(Math.sqrt(remaining));
                    BlockPos ground = c.add(dx, top, dz);
                    long detail = GeologyRules.mix(s.w.getSeed() ^ ground.toLong());
                    if (Math.floorMod(detail, 19) == 0) {
                        s.set(
                                ground.up(),
                                Blocks.TALLGRASS
                                        .getDefaultState()
                                        .withProperty(
                                                BlockTallGrass.TYPE,
                                                BlockTallGrass.EnumType.GRASS));
                    }
                }
        if (!main) {
            BlockPos trunk = c.add(-2, radius - 1, 1);
            for (int dy = 0; dy < 5; dy++) s.set(trunk.up(dy), Blocks.LOG.getDefaultState());
            for (int dx = -2; dx <= 2; dx++)
                for (int dz = -2; dz <= 2; dz++)
                    for (int dy = 3; dy <= 5; dy++)
                        if (Math.abs(dx) + Math.abs(dz) + (dy == 5 ? 1 : 0) <= 3
                                && (dx != 0 || dz != 0 || dy == 5))
                            s.set(
                                    trunk.add(dx, dy, dz),
                                    Blocks.LEAVES
                                            .getDefaultState()
                                            .withProperty(BlockLeaves.CHECK_DECAY, false)
                                            .withProperty(BlockLeaves.DECAYABLE, false));
        }
        if (main) lab(s, c, true);
    }

    private static void lab(Slice s, BlockPos c, boolean space) {
        LabArchitecture.build(
                s::set, c, space ? LabArchitecture.Theme.ORBITAL : LabArchitecture.Theme.MARINE);
        s.chest(c.add(space ? -17 : 6, space ? 0 : 7, space ? 11 : 4), space);
        s.chest(c.add(space ? 17 : 15, 0, space ? 11 : 7), space);
    }

    static final class Slice {
        final World w;
        final Chunk chunk;
        final int minX, maxX, minZ, maxZ;
        boolean changed;
        final Map<BlockPos, Boolean> chests = new LinkedHashMap<>();

        Slice(World w, Chunk chunk) {
            this.w = w;
            this.chunk = chunk;
            minX = chunk.x * 16;
            maxX = minX + 15;
            minZ = chunk.z * 16;
            maxZ = minZ + 15;
        }

        boolean contains(BlockPos p) {
            return p.getX() >= minX
                    && p.getX() <= maxX
                    && p.getZ() >= minZ
                    && p.getZ() <= maxZ
                    && p.getY() > 0
                    && p.getY() < 256;
        }

        void set(BlockPos p, IBlockState state) {
            if (contains(p)) {
                ChunkBlockEditor.set(w, chunk, p, state);
                changed = true;
            }
        }

        void chest(BlockPos p, boolean space) {
            if (contains(p)) {
                set(p, Blocks.CHEST.getDefaultState());
                chests.put(p, space);
            }
        }

        void finish() {
            if (!changed) return;
            for (Map.Entry<BlockPos, Boolean> entry : chests.entrySet()) {
                BlockPos p = entry.getKey();
                TileEntityChest chest = new TileEntityChest();
                chest.setWorld(w);
                chest.setPos(p);
                chunk.addTileEntity(p, chest);
                Random r = new Random(w.getSeed() ^ p.toLong());
                boolean space = entry.getValue();
                chest.setInventorySlotContents(
                        2,
                        new ItemStack(
                                space
                                        ? ExpeditionContent.SPACE_SCHEMATIC
                                        : ExpeditionContent.SEA_SCHEMATIC));
                chest.setInventorySlotContents(
                        4,
                        new ItemStack(
                                space ? ExpeditionContent.SPACE_LOG : ExpeditionContent.SEA_LOG));
                chest.setInventorySlotContents(
                        10, new ItemStack(ExpeditionContent.CIRCUITRY, 8 + r.nextInt(9)));
                chest.setInventorySlotContents(
                        12, new ItemStack(ExpeditionContent.DUBIOUS_PLATING, 12 + r.nextInt(13)));
                chest.setInventorySlotContents(
                        14, new ItemStack(ExpeditionContent.POWER_CELL, 4 + r.nextInt(5)));
                chest.setInventorySlotContents(22, new ItemStack(ExpeditionContent.SEEKER));
                chest.setInventorySlotContents(
                        24,
                        new ItemStack(
                                space ? Items.WHEAT_SEEDS : ExpeditionContent.PRISM_SHARD, 8));
                chest.markDirty();
            }
            chunk.generateSkylightMap();
            chunk.markDirty();
        }
    }
}
