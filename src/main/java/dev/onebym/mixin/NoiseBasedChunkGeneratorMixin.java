package dev.onebym.mixin;

import dev.onebym.worldgen.TerrainRouterRegistry;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoiseBasedChunkGenerator.class)
abstract class NoiseBasedChunkGeneratorMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onebym$registerOverworldRouter(
            BiomeSource biomeSource,
            Holder<NoiseGeneratorSettings> settings,
            CallbackInfo ci
    ) {
        NoiseGeneratorSettings value = settings.value();
        if (value.defaultBlock().is(Blocks.STONE)) {
            TerrainRouterRegistry.registerOverworld(value.noiseRouter());
        }
    }
}
