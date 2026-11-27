package dev.onebym.worldgen;

import dev.onebym.core.TerrainSampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;

/**
 * Applies the prototype's 2D terrain elevation as an additive term to the
 * compiled Overworld final-density sampler. This changes the scalar field
 * before aquifers and block filling, rather than editing generated blocks.
 */
public final class TerrainDensitySampler implements DensitySampler {
    private static final double DENSITY_PER_BLOCK = 1.0 / 128.0;
    private static final double REFERENCE_ELEVATION = 68.0;

    private final DensitySampler vanilla;
    private final TerrainSampler terrain;

    public TerrainDensitySampler(DensitySampler vanilla, TerrainSampler terrain) {
        this.vanilla = vanilla;
        this.terrain = terrain;
    }

    @Override
    public float sampleValue(SamplerContext context, int x, int y, int z) {
        return vanilla.sampleValue(context, x, y, z) + terrainOffset(terrain, x, z);
    }

    @Override
    public void sampleVolume(
            SamplerContext context,
            DensityBuffer buffer,
            DensityVolume volume
    ) {
        DensitySampler.sampleVolumeNaive(context, buffer, volume, this);
    }

    public static float terrainOffset(TerrainSampler terrain, int x, int z) {
        return (float) ((terrain.sample(x, z).elevation() - REFERENCE_ELEVATION) * DENSITY_PER_BLOCK);
    }
}
