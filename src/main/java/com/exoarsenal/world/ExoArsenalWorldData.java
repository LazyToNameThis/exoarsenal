package com.exoarsenal.world;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

public final class ExoArsenalWorldData extends WorldSavedData {
    private static final String NAME = "exoarsenal_world";
    private long lastBlizzardDay = -100L;

    public ExoArsenalWorldData() {
        super(NAME);
    }

    public ExoArsenalWorldData(String name) {
        super(name);
    }

    public static ExoArsenalWorldData get(World world) {
        MapStorage storage = world.getPerWorldStorage();
        ExoArsenalWorldData data =
                (ExoArsenalWorldData) storage.getOrLoadData(ExoArsenalWorldData.class, NAME);
        if (data == null) {
            data = new ExoArsenalWorldData();
            storage.setData(NAME, data);
        }
        return data;
    }

    public long getLastBlizzardDay() {
        return lastBlizzardDay;
    }

    public void setLastBlizzardDay(long day) {
        lastBlizzardDay = day;
        markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        lastBlizzardDay = nbt.getLong("LastBlizzardDay");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setLong("LastBlizzardDay", lastBlizzardDay);
        return nbt;
    }
}
