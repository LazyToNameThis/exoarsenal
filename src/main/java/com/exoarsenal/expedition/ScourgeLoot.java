package com.exoarsenal.expedition;

import net.minecraft.item.*;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraft.nbt.NBTTagCompound;
import java.util.*;

public final class ScourgeLoot {
    public static List<ItemStack> roll(Random r, boolean expert) {
        List<ItemStack> loot = new ArrayList<>();
        loot.add(new ItemStack(net.minecraft.init.Items.GOLD_INGOT));
        for (Item i :
                new Item[] {
                    ExpeditionContent.PEARL_SHARD,
                    ExpeditionContent.CORAL,
                    ExpeditionContent.SEASHELL,
                    ExpeditionContent.STARFISH
                }) loot.add(new ItemStack(i, ScourgeLootRules.materialCount(r, expert)));
        loot.add(new ItemStack(Blocks.SAND, 5 + r.nextInt(11)));
        loot.add(new ItemStack(ExpeditionContent.HEALING_POTION, 5 + r.nextInt(11)));
        Item[] weapons = {
            ExpeditionContent.SAHARA_SLICERS,
            ExpeditionContent.BARINADE,
            ExpeditionContent.SANDSTREAM_SCEPTER,
            ExpeditionContent.BRITTLE_STAR_STAFF,
            ExpeditionContent.SCOURGE_OF_DESERT
        };
        int mask = ScourgeLootRules.weaponMask(r, expert);
        for (int i = 0; i < weapons.length; i++)
            if ((mask & (1 << i)) != 0) loot.add(new ItemStack(weapons[i]));
        if (r.nextInt(expert ? 3 : 4) == 0) loot.add(new ItemStack(ExpeditionContent.SAND_CLOAK));
        if (r.nextInt(7) == 0) loot.add(new ItemStack(ExpeditionContent.MASK));
        if (expert) loot.add(new ItemStack(ExpeditionContent.OCEAN_CREST));
        return loot;
    }

    public static final class Progress extends WorldSavedData {
        private boolean killed, kingRescued;

        public Progress() {
            super("exoarsenal_scourge_progress");
        }

        public Progress(String s) {
            super(s);
        }

        public static Progress get(World w) {
            Progress d =
                    (Progress)
                            w.getPerWorldStorage()
                                    .getOrLoadData(Progress.class, "exoarsenal_scourge_progress");
            if (d == null) {
                d = new Progress();
                w.getPerWorldStorage().setData("exoarsenal_scourge_progress", d);
            }
            return d;
        }

        public boolean firstKill() {
            if (killed) return false;
            killed = true;
            markDirty();
            return true;
        }

        public boolean killed() {
            return killed;
        }

        public boolean rescueKing() {
            if (kingRescued) return false;
            kingRescued = true;
            markDirty();
            return true;
        }

        public void loseKing() {
            kingRescued = false;
            markDirty();
        }

        public void readFromNBT(NBTTagCompound n) {
            killed = n.getBoolean("Killed");
            kingRescued = n.getBoolean("KingRescued");
        }

        public NBTTagCompound writeToNBT(NBTTagCompound n) {
            n.setBoolean("Killed", killed);
            n.setBoolean("KingRescued", kingRescued);
            return n;
        }
    }
}
