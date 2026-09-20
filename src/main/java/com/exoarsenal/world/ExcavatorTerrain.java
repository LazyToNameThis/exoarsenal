package com.exoarsenal.world;

import com.exoarsenal.ExoArsenal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.util.*;

@Mod.EventBusSubscriber(modid = ExoArsenal.MODID)
public final class ExcavatorTerrain extends WorldSavedData {
    private static final String KEY = "exoarsenal_excavator_terrain";

    private static final class Entry {
        IBlockState state;
        long expires;
        UUID owner;

        Entry(IBlockState s, long t, UUID o) {
            state = s;
            expires = t;
            owner = o;
        }
    }

    private final Map<BlockPos, Entry> cuts = new LinkedHashMap<>();

    public ExcavatorTerrain() {
        super(KEY);
    }

    public ExcavatorTerrain(String key) {
        super(key);
    }

    public static ExcavatorTerrain get(World world) {
        ExcavatorTerrain data =
                (ExcavatorTerrain)
                        world.getPerWorldStorage().getOrLoadData(ExcavatorTerrain.class, KEY);
        if (data == null) {
            data = new ExcavatorTerrain();
            world.getPerWorldStorage().setData(KEY, data);
        }
        return data;
    }

    public boolean cut(EntityLiving boss, BlockPos position) {
        World world = boss.world;
        BlockPos p = position.toImmutable();
        if (world.isRemote
                || cuts.size() >= 4096
                || cuts.containsKey(p)
                || !world.isBlockLoaded(p)
                || !ForgeEventFactory.getMobGriefingEvent(world, boss)) return false;
        IBlockState state = world.getBlockState(p);
        Material m = state.getMaterial();
        if (world.getTileEntity(p) != null
                || state.getBlock().hasTileEntity(state)
                || state.getBlockHardness(world, p) < 0
                || !(m == Material.ROCK
                        || m == Material.GROUND
                        || m == Material.GRASS
                        || m == Material.SAND
                        || m == Material.CLAY
                        || m == Material.SNOW
                        || m == Material.CRAFTED_SNOW)
                || !ForgeEventFactory.onEntityDestroyBlock(boss, p, state)) return false;
        cuts.put(p, new Entry(state, world.getTotalWorldTime() + 600, boss.getUniqueID()));
        markDirty();
        if (!world.setBlockState(p, Blocks.AIR.getDefaultState(), 3)) {
            cuts.remove(p);
            return false;
        }
        return true;
    }

    public void release(UUID owner) {
        for (Entry entry : cuts.values()) if (owner.equals(entry.owner)) entry.expires = 0;
        markDirty();
    }

    public void retain(UUID owner, long until) {
        boolean changed = false;
        for (Entry entry : cuts.values())
            if (owner.equals(entry.owner)) {
                entry.expires = until;
                changed = true;
            }
        if (changed) markDirty();
    }

    private void restore(World world) {
        int budget = 128;
        Iterator<Map.Entry<BlockPos, Entry>> it = cuts.entrySet().iterator();
        while (it.hasNext() && budget > 0) {
            Map.Entry<BlockPos, Entry> e = it.next();
            if (e.getValue().expires > world.getTotalWorldTime()
                    || !world.isBlockLoaded(e.getKey())) continue;
            budget--;
            if (world.isAirBlock(e.getKey()))
                world.setBlockState(e.getKey(), e.getValue().state, 3);
            it.remove();
            markDirty();
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.WorldTickEvent e) {
        if (!e.world.isRemote
                && e.phase == TickEvent.Phase.END
                && e.world.getTotalWorldTime() % 5 == 0) get(e.world).restore(e.world);
    }

    @Override
    public void readFromNBT(NBTTagCompound n) {
        cuts.clear();
        NBTTagList list = n.getTagList("Cuts", 10);
        for (int i = 0; i < Math.min(4096, list.tagCount()); i++) {
            NBTTagCompound e = list.getCompoundTagAt(i);
            cuts.put(
                    BlockPos.fromLong(e.getLong("Pos")),
                    new Entry(
                            NBTUtil.readBlockState(e.getCompoundTag("State")),
                            e.getLong("Until"),
                            e.getUniqueId("Owner")));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound n) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<BlockPos, Entry> cut : cuts.entrySet()) {
            NBTTagCompound e = new NBTTagCompound();
            e.setLong("Pos", cut.getKey().toLong());
            e.setLong("Until", cut.getValue().expires);
            e.setUniqueId("Owner", cut.getValue().owner);
            e.setTag("State", NBTUtil.writeBlockState(new NBTTagCompound(), cut.getValue().state));
            list.appendTag(e);
        }
        n.setTag("Cuts", list);
        return n;
    }
}
