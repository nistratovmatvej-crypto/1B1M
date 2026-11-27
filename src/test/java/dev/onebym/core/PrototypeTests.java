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

        DensitySampler flatVanilla = new DensitySampler() {
            @Override
            public float sampleValue(SamplerContext context, int x, int y, int z) {
                return 0.25f;
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
        TerrainDensitySampler integrated = new TerrainDensitySampler(flatVanilla, a);
        int sampleX = 1200;
        int sampleZ = -840;
        float actual = integrated.sampleValue(SamplerContext.EMPTY_UNCACHED, sampleX, 70, sampleZ);
        float expected = 0.25f + TerrainDensitySampler.terrainOffset(a, sampleX, sampleZ);
        assert Math.abs(actual - expected) < 1e-6 : "TerrainSampler must modify the density field";
        assert Float.isFinite(actual) : "integrated density must remain finite";

        System.out.println("PASS: seed determinism, seed variation, density bounds, tree heights, config validation, density integration");
    }
}
