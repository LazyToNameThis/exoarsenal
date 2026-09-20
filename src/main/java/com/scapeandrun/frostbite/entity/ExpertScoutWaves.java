package com.scapeandrun.frostbite.entity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import java.util.*;

public final class ExpertScoutWaves {
    private static final int[][] WAVES = {{0, 0, 0, 0, 1}, {0, 0, 3, 3, 2, 2}, {4}};
    private int wave, spawned, rest;
    private final List<UUID> members = new ArrayList<>();

    public boolean tick(EntityX20Scout boss) {
        if (wave >= WAVES.length) return true;
        if (rest > 0) {
            rest--;
            return false;
        }
        WorldServer world = (WorldServer) boss.world;
        com.scapeandrun.frostbite.world.ScoutEncounterLedger ledger =
                com.scapeandrun.frostbite.world.ScoutEncounterLedger.get(world);
        members.removeIf(ledger::consumeDeath);
        if (spawned < WAVES[wave].length) {
            if (boss.ticksExisted % 10 != 0) return false;
            for (int attempt = 0; attempt < 16; attempt++) {
                double a = (spawned * 2.4 + attempt * .4);
                BlockPos near =
                        new BlockPos(boss.arenaCenter()).add(Math.cos(a) * 8, 0, Math.sin(a) * 8);
                if (!world.isBlockLoaded(near)) continue;
                BlockPos p = world.getTopSolidOrLiquidBlock(near);
                EntityFrigidRobot mob = create(world, WAVES[wave][spawned]);
                mob.setPosition(
                        p.getX() + .5,
                        p.getY() + (mob.kind() == EntityFrigidRobot.Kind.DRONE ? 3 : 0),
                        p.getZ() + .5);
                if (!world.getCollisionBoxes(mob, mob.getEntityBoundingBox()).isEmpty()) continue;
                mob.enablePersistence();
                mob.setHomePosAndDistance(new BlockPos(boss.arenaCenter()), 11);
                mob.setAttackTarget(boss.getAttackTarget());
                mob.getEntityData().setUniqueId("ScoutWaveOwner", boss.getUniqueID());
                if (world.spawnEntity(mob)) {
                    members.add(mob.getUniqueID());
                    spawned++;
                }
                break;
            }
            return false;
        }
        for (UUID id : members) {
            Entity member = world.getEntityFromUuid(id);
            if (member == null || member.isEntityAlive()) return false;
        }

        if (!members.isEmpty()) return false;
        wave++;
        spawned = 0;
        rest = 60;
        return wave >= WAVES.length;
    }

    public void defeated(UUID id) {
        members.remove(id);
    }

    public void clear(EntityX20Scout boss) {
        if (boss.world instanceof WorldServer) {
            com.scapeandrun.frostbite.world.ScoutEncounterLedger.get(boss.world)
                    .close(boss.getUniqueID());
            for (UUID id : members) {
                Entity e = ((WorldServer) boss.world).getEntityFromUuid(id);
                if (e != null) e.setDead();
            }
        }
        members.clear();
    }

    public NBTTagCompound save() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("Wave", wave);
        tag.setInteger("Spawned", spawned);
        tag.setInteger("Rest", rest);
        NBTTagList list = new NBTTagList();
        for (UUID id : members) list.appendTag(new NBTTagString(id.toString()));
        tag.setTag("Members", list);
        return tag;
    }

    public void load(NBTTagCompound tag) {
        wave = Math.max(0, Math.min(3, tag.getInteger("Wave")));
        spawned = Math.max(0, tag.getInteger("Spawned"));
        rest = Math.max(0, tag.getInteger("Rest"));
        members.clear();
        NBTTagList list = tag.getTagList("Members", 8);
        for (int i = 0; i < list.tagCount(); i++)
            try {
                members.add(UUID.fromString(list.getStringTagAt(i)));
            } catch (IllegalArgumentException ignored) {
            }
    }

    public static EntityFrigidRobot create(WorldServer world, int kind) {
        switch (kind) {
            case 0:
                return new EntityFrigidRobot.Drone(world);
            case 1:
                return new EntityFrigidRobot.Amplifier(world);
            case 2:
                return new EntityFrigidRobot.Shielder(world);
            case 3:
                return new EntityFrigidRobot.Rover(world);
            default:
                return new EntityFrigidRobot.Arthropod(world);
        }
    }
}
