package dev.onebym;

import dev.onebym.core.ConfigIO;
import dev.onebym.core.WorldConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Path;

public final class OneByM implements ModInitializer {
    public static final String MOD_ID = "onebym";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static WorldConfig CONFIG;
    @Override public void onInitialize() {
        Path file=FabricLoader.getInstance().getConfigDir().resolve("1b1m.properties");
        try { CONFIG=ConfigIO.loadOrCreate(file); LOGGER.info("1B1M config loaded from {}",file); }
        catch(Exception e) { CONFIG=WorldConfig.defaults(); LOGGER.error("Config load failed; defaults active",e); }
        LOGGER.info("1B1M prototype loaded. Minecraft terrain integration is not active in this build.");
    }
}
