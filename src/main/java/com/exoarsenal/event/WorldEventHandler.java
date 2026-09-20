package com.exoarsenal.event;

import com.exoarsenal.config.ModConfig;
import com.exoarsenal.registry.ModContent;
import com.exoarsenal.world.ChunkBlockEditor;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.oredict.OreDictionary;

import java.util.IdentityHashMap;
import java.util.Map;

public class WorldEventHandler {
    private final Map<Block, Boolean> oreBlocks = new IdentityHashMap<>();
    private final Map<Block, Boolean> treeBlocks = new IdentityHashMap<>();

    @SubscribeEvent
    public void cancelTrees(DecorateBiomeEvent.Decorate event) {
        if (ModConfig.removeAllTreeGeneration
                && com.exoarsenal.world.FrozenWorldRules.isFrozenWasteland(event.getWorld())
                && event.getType() == DecorateBiomeEvent.Decorate.EventType.TREE) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void scrubChunk(PopulateChunkEvent.Post event) {
        World world = event.getWorld();
        if (!com.exoarsenal.world.FrozenWorldRules.isFrozenWasteland(world)) return;
        int startX = event.getChunkX() << 4;
        int startZ = event.getChunkZ() << 4;
        Chunk chunk = world.getChunkFromChunkCoords(event.getChunkX(), event.getChunkZ());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int localX = 0; localX < 16; localX++)
            for (int localZ = 0; localZ < 16; localZ++) {
                int x = startX + localX;
                int z = startZ + localZ;
                int top = Math.min(255, chunk.getHeightValue(localX, localZ));
                for (int y = 1; y <= top; y++) {
                    pos.setPos(x, y, z);
                    IBlockState state = chunk.getBlockState(pos);
                    Block block = state.getBlock();
                    if (ModConfig.replaceTerrain) {
                        if (block == Blocks.STONE)
                            ChunkBlockEditor.set(
                                    world, chunk, pos, ModContent.FROZEN_STONE.getDefaultState());
                        else if (block == Blocks.GRASS)
                            ChunkBlockEditor.set(
                                    world, chunk, pos, ModContent.PERMAFROST.getDefaultState());
                        else if (block == Blocks.DIRT)
                            ChunkBlockEditor.set(
                                    world,
                                    chunk,
                                    pos,
                                    isSurface(chunk, pos)
                                            ? ModContent.PERMAFROST.getDefaultState()
                                            : ModContent.FROZEN_STONE.getDefaultState());
                    }
                    if (ModConfig.removeAllOreGeneration && isOre(block))
                        ChunkBlockEditor.set(
                                world, chunk, pos, ModContent.FROZEN_STONE.getDefaultState());
                    if (ModConfig.removeAllTreeGeneration && block != ModContent.FROZEN_LOG) {
                        if (isTreeBlock(block))
                            ChunkBlockEditor.set(world, chunk, pos, Blocks.AIR.getDefaultState());
                    }
                }
            }
        chunk.generateSkylightMap();
        chunk.markDirty();
    }

    @SubscribeEvent
    public void permanentSnow(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || event.world.isRemote
                || !com.exoarsenal.world.FrozenWorldRules.isFrozenWasteland(event.world)
                || event.world.getTotalWorldTime() % 200L != 0L) return;
        event.world.getWorldInfo().setRaining(true);
        event.world.getWorldInfo().setRainTime(12000);
        event.world.setRainStrength(1.0F);
    }

    private boolean isSurface(Chunk chunk, BlockPos pos) {
        for (int y = 1; y <= 4; y++)
            if (chunk.getBlockState(pos.up(y)).getBlock() != Blocks.AIR) return false;
        return true;
    }

    private boolean isOre(Block block) {
        Boolean cached = oreBlocks.get(block);
        if (cached != null) return cached;
        boolean result = hasOrePrefix(block, "ore");
        oreBlocks.put(block, result);
        return result;
    }

    private boolean isTreeBlock(Block block) {
        Boolean cached = treeBlocks.get(block);
        if (cached != null) return cached;
        boolean result = hasOreName(block, "treeLeaves") || hasOreName(block, "logWood");
        treeBlocks.put(block, result);
        return result;
    }

    private boolean hasOreName(Block block, String exactName) {
        Item item = Item.getItemFromBlock(block);
        if (item == net.minecraft.init.Items.AIR) return false;
        for (int id :
                OreDictionary.getOreIDs(new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE))) {
            if (OreDictionary.getOreName(id).equals(exactName)) return true;
        }
        return false;
    }

    private boolean hasOrePrefix(Block block, String prefix) {
        Item item = Item.getItemFromBlock(block);
        if (item == net.minecraft.init.Items.AIR) return false;
        for (int id :
                OreDictionary.getOreIDs(new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE))) {
            if (OreDictionary.getOreName(id).startsWith(prefix)) return true;
        }
        return false;
    }
}
