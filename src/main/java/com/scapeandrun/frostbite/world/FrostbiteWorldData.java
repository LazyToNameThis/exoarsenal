package com.scapeandrun.frostbite.world;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

public final class FrostbiteWorldData extends WorldSavedData {
    private static final String NAME = "sr_frostbite_world";
    private long lastBlizzardDay = -100L;

    public FrostbiteWorldData() {
        super(NAME);
    }

    public FrostbiteWorldData(String name) {
        super(name);
    }

    public static FrostbiteWorldData get(World world) {
        MapStorage storage = world.getPerWorldStorage();
        FrostbiteWorldData data =
                (FrostbiteWorldData) storage.getOrLoadData(FrostbiteWorldData.class, NAME);
        if (data == null) {
            data = new FrostbiteWorldData();
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
