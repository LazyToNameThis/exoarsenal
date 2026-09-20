package com.scapeandrun.frostbite.world;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

public final class FrostbiteWorldSettings extends WorldSavedData {
    private static final String KEY = "sr_frostbite_difficulty";
    private boolean expert;
    private boolean master;
    private int blizzardKills;
    private long lastRobotKill;

    public int recordRobotKill(long time) {
        lastRobotKill = time;
        blizzardKills = Math.min(12, blizzardKills + 1);
        markDirty();
        return blizzardKills;
    }

    public void consumeRobotKills() {
        if (blizzardKills != 0) {
            blizzardKills = 0;
            markDirty();
        }
    }

    public FrostbiteWorldSettings() {
        super(KEY);
    }

    public FrostbiteWorldSettings(String name) {
        super(name);
    }

    public static FrostbiteWorldSettings get(World world) {
        World overworld = world.getMinecraftServer().getWorld(0);
        FrostbiteWorldSettings data =
                (FrostbiteWorldSettings)
                        overworld.getMapStorage().getOrLoadData(FrostbiteWorldSettings.class, KEY);
        if (data == null) {
            data = new FrostbiteWorldSettings();
            overworld.getMapStorage().setData(KEY, data);
            data.markDirty();
        }
        return data;
    }

    public boolean isExpert() {
        return expert;
    }

    public boolean isMaster() {
        return master;
    }

    public int difficulty() {
        return master ? 2 : expert ? 1 : 0;
    }

    public void setDifficulty(int mode) {
        master = mode == 2;
        expert = mode >= 1;
        markDirty();
    }

    public void setExpert(boolean enabled) {
        setDifficulty(enabled ? 1 : 0);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        master = tag.getBoolean("Master");
        expert = master || tag.getBoolean("Expert");
        blizzardKills = tag.getInteger("BlizzardRobotKills");
        lastRobotKill = tag.getLong("LastRobotKill");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setBoolean("Master", master);
        tag.setBoolean("Expert", expert);
        tag.setInteger("BlizzardRobotKills", blizzardKills);
        tag.setLong("LastRobotKill", lastRobotKill);
        return tag;
    }
}
