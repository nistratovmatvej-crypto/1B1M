package dev.onebym.mixin;

import dev.onebym.OneByM;
import dev.onebym.core.TerrainSampler;
import dev.onebym.worldgen.TerrainDensitySampler;
import dev.onebym.worldgen.TerrainRouterRegistry;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RandomState.class)
abstract class RandomStateMixin {
    @Shadow @Final private NoiseRouter router;

    @Inject(method = "getSampler", at = @At("RETURN"), cancellable = true)
    private void onebym$applyTerrainSampler(
            DensityFunction function,
            CallbackInfoReturnable<DensitySampler> cir
    ) {
        if (function != router.finalDensity() || !TerrainRouterRegistry.isOverworld(router)) {
            return;
        }

        DensitySampler current = cir.getReturnValue();
        if (current instanceof TerrainDensitySampler) {
            return;
        }

        var config = OneByM.CONFIG != null ? OneByM.CONFIG : dev.onebym.core.WorldConfig.defaults();
        long seed = ((RandomState) (Object) this).seed();
        cir.setReturnValue(new TerrainDensitySampler(
                current,
                new TerrainSampler(seed, config)
        ));
    }
}
