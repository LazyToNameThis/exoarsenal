package com.scapeandrun.frostbite.world;

import com.scapeandrun.frostbite.Frostbite;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;

public final class FrozenWorldRules {
    private FrozenWorldRules() {}

    public static boolean isFrozenWasteland(World world) {
        if (world == null || world.provider.getDimension() != 0) return false;
        WorldType type = world.getWorldInfo().getTerrainType();
        return type == Frostbite.WORLD_TYPE
                || (type != null && "frozen_wasteland".equals(type.getName()));
    }
}
