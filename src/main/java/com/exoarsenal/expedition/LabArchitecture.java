package com.exoarsenal.expedition;

import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import java.util.function.BiConsumer;

public final class LabArchitecture {
    public enum Theme {
        MARINE,
        ORBITAL,
        PLAGUE,
        THERMAL
    }

    public static final class Materials {
        public final IBlockState plating, panel, thermal, prism, cluster, mud, spore;

        public Materials(
                IBlockState plating,
                IBlockState panel,
                IBlockState thermal,
                IBlockState prism,
                IBlockState cluster,
                IBlockState mud,
                IBlockState spore) {
            this.plating = plating;
            this.panel = panel;
            this.thermal = thermal;
            this.prism = prism;
            this.cluster = cluster;
            this.mud = mud;
            this.spore = spore;
        }
    }

    private final BiConsumer<BlockPos, IBlockState> sink;
    private final BlockPos origin;
    private final Theme theme;
    private final Materials materials;
    private final IBlockState metal, trim, light, glass;

    private LabArchitecture(
            BiConsumer<BlockPos, IBlockState> sink,
            BlockPos origin,
            Theme theme,
            Materials materials) {
        this.sink = sink;
        this.origin = origin;
        this.theme = theme;
        this.materials = materials;
        metal = theme == Theme.THERMAL ? materials.thermal : materials.plating;
        trim = materials.plating;
        light = materials.panel;
        glass =
                Blocks.STAINED_GLASS
                        .getDefaultState()
                        .withProperty(
                                BlockStainedGlass.COLOR,
                                theme == Theme.THERMAL
                                        ? net.minecraft.item.EnumDyeColor.ORANGE
                                        : theme == Theme.PLAGUE
                                                ? net.minecraft.item.EnumDyeColor.LIME
                                                : net.minecraft.item.EnumDyeColor.LIGHT_BLUE);
    }

    public static void build(BiConsumer<BlockPos, IBlockState> sink, BlockPos origin, Theme theme) {
        build(
                sink,
                origin,
                theme,
                new Materials(
                        ExpeditionContent.LAB_PLATING.getDefaultState(),
                        ExpeditionContent.LAB_PANEL.getDefaultState(),
                        DeepContent.THERMAL_PLATING.getDefaultState(),
                        ExpeditionContent.SEA_PRISM.getDefaultState(),
                        ExpeditionContent.PRISM_CLUSTER.getDefaultState(),
                        WildContent.MUD.getDefaultState(),
                        WildContent.SPORE_PLANT.getDefaultState()));
    }

    public static void build(
            BiConsumer<BlockPos, IBlockState> sink,
            BlockPos origin,
            Theme theme,
            Materials materials) {
        new LabArchitecture(sink, origin, theme, materials).build();
    }

    private void p(int x, int y, int z, IBlockState state) {
        sink.accept(origin.add(x, y, z), state);
    }

    private void box(int x0, int y0, int z0, int x1, int y1, int z1, IBlockState state) {
        for (int x = x0; x <= x1; x++)
            for (int y = y0; y <= y1; y++) for (int z = z0; z <= z1; z++) p(x, y, z, state);
    }

    private void room(int x0, int z0, int x1, int z1, int floor, int height, boolean windows) {
        for (int x = x0; x <= x1; x++)
            for (int z = z0; z <= z1; z++)
                for (int y = floor - 1; y <= floor + height; y++) {
                    boolean side = x == x0 || x == x1 || z == z0 || z == z1,
                            cap = y == floor - 1 || y == floor + height;
                    IBlockState state = side || cap ? metal : Blocks.AIR.getDefaultState();
                    int along = x == x0 || x == x1 ? z - z0 : x - x0;
                    if (side && windows && y >= floor + 1 && y <= floor + 3 && along % 5 != 0)
                        state = glass;
                    if (side && along % 5 == 0) state = trim;
                    if (y == floor + height - 1
                            && !side
                            && (x - x0) % 5 == 2
                            && (z - z0 == 2 || z1 - z == 2)) state = light;
                    p(x, y, z, state);
                }
    }

    private void doorX(int x, int y, int z) {
        box(x, y, z - 1, x, y + 2, z + 1, Blocks.AIR.getDefaultState());
        box(x, y + 3, z - 1, x, y + 3, z + 1, light);
    }

    private void doorZ(int x, int y, int z) {
        box(x - 1, y, z, x + 1, y + 2, z, Blocks.AIR.getDefaultState());
        box(x - 1, y + 3, z, x + 1, y + 3, z, light);
    }

    private void desk(int x, int y, int z, EnumFacing facing) {
        box(x - 1, y, z, x + 1, y, z, metal);
        p(x, y + 1, z, light);
        p(
                x - 1,
                y + 1,
                z,
                Blocks.STONE_BUTTON
                        .getDefaultState()
                        .withProperty(BlockButton.FACING, EnumFacing.UP));
        p(
                x + 1,
                y + 1,
                z,
                Blocks.LEVER
                        .getDefaultState()
                        .withProperty(BlockLever.FACING, BlockLever.EnumOrientation.UP_X));
        p(
                x,
                y,
                z + (facing == EnumFacing.NORTH ? 2 : -2),
                Blocks.QUARTZ_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, facing));
    }

    private void tank(int x, int y, int z, int radius, IBlockState contents) {
        for (int dx = -radius; dx <= radius; dx++)
            for (int dz = -radius; dz <= radius; dz++)
                for (int dy = 0; dy <= 5; dy++) {
                    boolean rim = Math.abs(dx) == radius || Math.abs(dz) == radius;
                    p(x + dx, y + dy, z + dz, dy == 0 || dy == 5 ? metal : rim ? glass : contents);
                }
        p(x, y + 5, z, light);
    }

    private void stairs(int x, int z, int rise) {
        for (int step = 0; step < rise; step++)
            for (int width = 0; width < 2; width++) {
                box(
                        x + width,
                        step,
                        z - step,
                        x + width,
                        step + 3,
                        z - step,
                        Blocks.AIR.getDefaultState());
                p(
                        x + width,
                        step,
                        z - step,
                        Blocks.QUARTZ_STAIRS
                                .getDefaultState()
                                .withProperty(BlockStairs.FACING, EnumFacing.NORTH));
            }
    }

    private void build() {
        switch (theme) {
            case MARINE:
                room(-3, -12, 3, 12, 0, 6, false);
                room(-18, -10, -3, 10, 0, 7, true);
                room(3, -10, 18, 10, 0, 7, true);
                room(-8, -7, 8, 7, 7, 5, true);
                doorX(-3, 0, -6);
                doorX(-3, 0, 6);
                doorX(3, 0, -6);
                doorX(3, 0, 6);
                tank(-11, 0, -5, 3, Blocks.WATER.getDefaultState());
                tank(-11, 0, 5, 3, Blocks.WATER.getDefaultState());
                p(-11, 1, -5, materials.prism);
                p(-11, 1, 5, materials.cluster);
                desk(11, 0, -6, EnumFacing.NORTH);
                desk(11, 0, 6, EnumFacing.SOUTH);
                desk(0, 7, -5, EnumFacing.NORTH);
                stairs(0, 5, 7);
                box(0, 7, -1, 1, 10, 0, Blocks.AIR.getDefaultState());

                box(-1, -5, 9, 1, -2, 11, metal);
                box(0, -5, 10, 0, -1, 10, Blocks.WATER.getDefaultState());
                p(0, -1, 10, Blocks.IRON_TRAPDOOR.getDefaultState());
                p(
                        1,
                        0,
                        10,
                        Blocks.LEVER
                                .getDefaultState()
                                .withProperty(BlockLever.FACING, BlockLever.EnumOrientation.UP_X));

                box(9, 0, -3, 15, 0, 3, metal);
                box(11, 1, -1, 13, 3, 1, materials.prism);
                p(12, 4, 0, materials.cluster);
                for (int x : new int[] {9, 15}) {
                    box(x, 1, -3, x, 5, -3, trim);
                    box(x, 1, 3, x, 5, 3, trim);
                    box(x, 5, -3, x, 5, 3, metal);
                }
                box(9, 5, -3, 15, 5, -3, metal);
                box(9, 5, 3, 15, 5, 3, metal);
                for (int z : new int[] {-3, 3}) {
                    p(12, 5, z, light);
                    p(12, 1, z, light);
                }

                box(5, 4, -8, 17, 4, -5, metal);
                for (int x = 5; x <= 17; x++) p(x, 5, -5, glass);

                stairs(5, -1, 4);
                box(5, 4, -5, 6, 7, -4, Blocks.AIR.getDefaultState());
                break;
            case ORBITAL:
                room(-19, -14, 19, -3, 0, 7, true);
                room(-19, -3, 19, 4, 0, 5, false);
                room(-19, 4, -4, 14, 0, 6, true);
                room(4, 4, 19, 14, 0, 6, true);
                doorZ(-10, 0, -3);
                doorZ(10, 0, -3);
                doorZ(-10, 0, 4);
                doorZ(10, 0, 4);
                for (int x = -15; x <= 15; x++)
                    for (int z = -11; z <= -6; z++) {
                        p(
                                x,
                                0,
                                z,
                                z == -9
                                        ? Blocks.WATER.getDefaultState()
                                        : Blocks.FARMLAND
                                                .getDefaultState()
                                                .withProperty(BlockFarmland.MOISTURE, 7));
                        if (z != -9)
                            p(
                                    x,
                                    1,
                                    z,
                                    Blocks.WHEAT.getDefaultState().withProperty(BlockCrops.AGE, 7));
                    }
                box(-16, 7, -12, 16, 7, -5, glass);
                desk(-11, 0, 11, EnumFacing.SOUTH);
                desk(11, 0, 11, EnumFacing.SOUTH);
                tank(11, 0, 7, 2, Blocks.REDSTONE_BLOCK.getDefaultState());
                box(-2, 0, 1, 2, 2, 4, Blocks.AIR.getDefaultState());

                box(-3, 0, -2, 3, 0, 3, metal);
                box(-1, 1, -1, 1, 3, 1, materials.thermal);
                for (int y = 1; y <= 4; y++) {
                    p(-3, y, 0, trim);
                    p(3, y, 0, trim);
                }
                box(-3, 4, -1, 3, 4, 1, metal);
                p(0, 4, 0, light);
                break;
            case PLAGUE:
                room(-24, -10, 24, 10, 0, 9, false);
                for (int x : new int[] {-14, 0, 15}) desk(x, 0, 8, EnumFacing.SOUTH);
                box(-22, 6, 5, 22, 6, 9, metal);
                for (int x = -22; x <= 22; x++) p(x, 7, 5, Blocks.IRON_BARS.getDefaultState());
                stairs(-21, 8, 6);
                doorX(-24, 0, 7);
                doorX(24, 0, 7);

                for (int x : new int[] {-17, -10, -3, 4}) {
                    tank(x, 0, -4, 2, Blocks.AIR.getDefaultState());
                    p(x, 1, -4, materials.mud);
                    p(x, 2, -4, materials.spore);
                }
                box(-19, 6, -7, 6, 6, -1, metal);
                box(-17, 7, -6, -15, 8, -4, trim);
                box(0, 7, -6, 2, 8, -4, trim);
                for (int x = 18; x <= 24; x++)
                    for (int z = -5; z <= 4; z++)
                        for (int y = 1; y <= 7; y++)
                            if ((x - 24) * (x - 24) + z * z + (y - 4) * (y - 4) < 37)
                                p(x, y, z, Blocks.AIR.getDefaultState());
                break;
            case THERMAL:
                room(-24, -10, 24, 10, 0, 10, false);
                box(-6, 0, -3, 6, 0, 3, metal);
                for (int x : new int[] {-6, 6})
                    for (int z : new int[] {-3, 3}) box(x, 1, z, x, 6, z, trim);
                box(-6, 6, -3, 6, 6, 3, metal);
                box(
                        -1,
                        4,
                        -1,
                        1,
                        5,
                        1,
                        Blocks.PISTON
                                .getDefaultState()
                                .withProperty(BlockPistonBase.FACING, EnumFacing.DOWN));
                p(0, 1, 0, Blocks.ANVIL.getDefaultState());
                desk(-7, 0, 7, EnumFacing.SOUTH);
                desk(7, 0, 7, EnumFacing.SOUTH);
                box(-14, 7, -7, 14, 7, -5, metal);
                for (int x = -14; x <= 14; x++) p(x, 8, -5, Blocks.IRON_BARS.getDefaultState());
                stairs(8, 1, 7);
                tank(-17, 0, -2, 3, Blocks.LAVA.getDefaultState());
                tank(17, 0, -2, 3, Blocks.LAVA.getDefaultState());
                for (int x : new int[] {-17, 17}) {
                    box(x, 6, -2, x, 9, -2, trim);
                    box(Math.min(x, 0), 9, -2, Math.max(x, 0), 9, -2, metal);
                }

                for (int x = -24; x <= 24; x++)
                    for (int z = -10; z <= 10; z++) {
                        double r = x * x / 576D + z * z / 100D;
                        if (r > 1.08) box(x, -1, z, x, 10, z, Blocks.AIR.getDefaultState());
                        else if (r > .88) {
                            p(x, -1, z, trim);
                            p(x, 10, z, trim);
                            for (int y = 0; y < 10; y++)
                                p(x, y, z, y >= 2 && y <= 4 && x % 6 != 0 ? glass : metal);
                        }
                    }
                doorX(-24, 0, 0);
                doorX(24, 0, 0);
                doorX(-23, 0, 0);
                doorX(23, 0, 0);
                break;
        }
    }
}
