package com.scapeandrun.frostbite.client.render;

import com.scapeandrun.frostbite.entity.EntityX20Scout;
import net.minecraft.util.math.Vec3d;
import java.util.Map;
import java.util.WeakHashMap;

public final class ScoutRenderSockets {
    private static final Map<EntityX20Scout, SocketSet> CACHE = new WeakHashMap<>();

    private ScoutRenderSockets() {}

    static void put(EntityX20Scout entity, String name, Vec3d point) {
        SocketSet set = CACHE.get(entity);
        if (set == null || set.tick != entity.ticksExisted) {
            set = new SocketSet(entity.ticksExisted);
            CACHE.put(entity, set);
        }
        set.points.put(name, point);
    }

    public static Vec3d get(EntityX20Scout entity, String name, Vec3d fallback) {
        SocketSet set = CACHE.get(entity);
        return set == null || entity.ticksExisted - set.tick > 1
                ? fallback
                : set.points.getOrDefault(name, fallback);
    }

    private static final class SocketSet {
        final int tick;
        final Map<String, Vec3d> points = new java.util.HashMap<>();

        SocketSet(int tick) {
            this.tick = tick;
        }
    }
}
