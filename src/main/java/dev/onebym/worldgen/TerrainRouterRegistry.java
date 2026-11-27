package dev.onebym.worldgen;

import dev.onebym.OneByM;
import net.minecraft.world.level.levelgen.NoiseRouter;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public final class TerrainRouterRegistry {
    private static final Set<NoiseRouter> OVERWORLD_ROUTERS =
            Collections.newSetFromMap(new IdentityHashMap<>());
    private static boolean samplerActivationLogged;

    private TerrainRouterRegistry() {}

    public static synchronized void registerOverworld(NoiseRouter router) {
        OVERWORLD_ROUTERS.add(router);
        OneByM.LOGGER.info("Registered Overworld NoiseRouter for 1B1M density integration.");
    }

    public static synchronized boolean isOverworld(NoiseRouter router) {
        return OVERWORLD_ROUTERS.contains(router);
    }

    public static synchronized void logSamplerActivation() {
        if (!samplerActivationLogged) {
            samplerActivationLogged = true;
            OneByM.LOGGER.info("1B1M TerrainSampler is now modifying the compiled Overworld final_density sampler.");
        }
    }
}
