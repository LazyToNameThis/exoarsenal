package com.scapeandrun.frostbite.world;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ScoutEncounterLedger extends WorldSavedData {
    private static final String KEY = "sr_frostbite_scout_encounters";
    private final Set<UUID> defeated = new HashSet<>(), closed = new HashSet<>();

    public ScoutEncounterLedger() {
        super(KEY);
    }

    public ScoutEncounterLedger(String name) {
        super(name);
    }

    public static ScoutEncounterLedger get(World world) {
        ScoutEncounterLedger data =
                (ScoutEncounterLedger)
                        world.getPerWorldStorage().getOrLoadData(ScoutEncounterLedger.class, KEY);
        if (data == null) {
            data = new ScoutEncounterLedger();
            world.getPerWorldStorage().setData(KEY, data);
            data.markDirty();
        }
        return data;
    }

    public void recordDeath(UUID member) {
        if (defeated.add(member)) markDirty();
    }

    public boolean consumeDeath(UUID member) {
        boolean found = defeated.remove(member);
        if (found) markDirty();
        return found;
    }

    public void close(UUID owner) {
        if (closed.add(owner)) markDirty();
    }

    public boolean isClosed(UUID owner) {
        return closed.contains(owner);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        read(tag, "Defeated", defeated);
        read(tag, "Closed", closed);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        write(tag, "Defeated", defeated);
        write(tag, "Closed", closed);
        return tag;
    }

    private static void read(NBTTagCompound tag, String key, Set<UUID> result) {
        result.clear();
        NBTTagList list = tag.getTagList(key, 8);
        for (int i = 0; i < list.tagCount(); i++)
            try {
                result.add(UUID.fromString(list.getStringTagAt(i)));
            } catch (IllegalArgumentException ignored) {
            }
    }

    private static void write(NBTTagCompound tag, String key, Set<UUID> values) {
        NBTTagList list = new NBTTagList();
        for (UUID id : values) list.appendTag(new NBTTagString(id.toString()));
        tag.setTag(key, list);
    }
}
