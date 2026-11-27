package dev.onebym.core;

import dev.onebym.worldgen.TerrainDensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;

public final class PrototypeTests {
    public static void main(String[] args) {
        WorldConfig cfg = WorldConfig.defaults();
        TerrainSampler a = new TerrainSampler(777, cfg);
        TerrainSampler b = new TerrainSampler(777, cfg);
        TerrainSampler c = new TerrainSampler(778, cfg);

        for (int x = -5000; x <= 5000; x += 137) {
            for (int z = -5000; z <= 5000; z += 251) {
                TerrainSampler.Sample sa = a.sample(x, z);
                TerrainSampler.Sample sb = b.sample(x, z);
                TerrainSampler.Sample sc = c.sample(x, z);
                assert Math.abs(sa.elevation() - sb.elevation()) < 1e-9 : "same seed must be stable";
                assert Math.abs(sa.elevation() - sc.elevation()) > 1e-6 || sa.region() != sc.region()
                        : "different seeds should diverge";
                assert sa.forestDensity() >= 0 && sa.forestDensity() <= 1 : "density bounds";
                int height = a.treeHeight(x, z);
                assert height >= cfg.treeMinHeight() && height <= cfg.treeMaxHeight() : "tree height range";
            }
        }

        try {
            new WorldConfig(0, 1, 1, 1, 1, 1, .5, 1, .1, 3, 2).validated();
            throw new AssertionError("bad config accepted");
        } catch (IllegalArgumentException expected) {
            // Expected validation failure.
        }

        DensitySampler verticalLinear = new DensitySampler() {
            @Override
            public float sampleValue(SamplerContext context, int x, int y, int z) {
                return 0.5f - y * 0.01f;
            }

            @Override
            public void sampleVolume(
                    SamplerContext context,
                    DensityBuffer buffer,
                    DensityVolume volume
            ) {
                DensitySampler.sampleVolumeNaive(context, buffer, volume, this);
            }
        };

        TerrainDensitySampler integrated = new TerrainDensitySampler(verticalLinear, a);
        int sampleX = 1200;
        int sampleZ = -840;
        int displacement = TerrainDensitySampler.terrainDisplacement(a, sampleX, sampleZ);
        assert displacement >= -24 && displacement <= 24 : "vertical displacement must be bounded";
        assert displacement != 0 : "test point must exercise terrain displacement";

        float actual = integrated.sampleValue(
                SamplerContext.EMPTY_UNCACHED, sampleX, 70, sampleZ);
        float expected = verticalLinear.sampleValue(
                SamplerContext.EMPTY_UNCACHED, sampleX, 70 - displacement, sampleZ);
        assert Math.abs(actual - expected) < 1e-6
                : "terrain must translate the vanilla density vertically, not add a density bias";

        DensityVolume volume = new DensityVolume(
                1, 3, 1, sampleX, 69, sampleZ, 1, 1, 1);
        DensityBuffer buffer = DensityBuffer.createUnpooled(volume.size());
        integrated.sampleVolume(SamplerContext.EMPTY_UNCACHED, buffer, volume);
        float volumeActual = buffer.get(volume.indexUnchecked(0, 1, 0));
        float volumeExpected = verticalLinear.sampleValue(
                SamplerContext.EMPTY_UNCACHED, sampleX, 70 - displacement, sampleZ);
        assert Math.abs(volumeActual - volumeExpected) < 1e-5
                : "bulk sampling must preserve vertical displacement";

        System.out.println("PASS: seed determinism, seed variation, density bounds, tree heights, config validation, bounded vertical density translation, optimized bulk sampling");
    }
}
