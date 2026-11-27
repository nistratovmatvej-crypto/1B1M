package dev.onebym.worldgen;

import net.minecraft.world.level.levelgen.NoiseRouter;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Tracks the exact Overworld router instances created by vanilla settings.
 * Identity is intentional: NoiseRouter is a record, but the RandomState must
 * receive the very same router instance that the generator was constructed with.
 */
public final class TerrainRouterRegistry {
    private static final Set<NoiseRouter> OVERWORLD_ROUTERS =
            Collections.newSetFromMap(new IdentityHashMap<>());

    private TerrainRouterRegistry() {}

    public static synchronized void registerOverworld(NoiseRouter router) {
        OVERWORLD_ROUTERS.add(router);
    }

    public static synchronized boolean isOverworld(NoiseRouter router) {
        return OVERWORLD_ROUTERS.contains(router);
    }
}
