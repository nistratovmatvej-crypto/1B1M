package dev.onebym.worldgen;

import dev.onebym.core.TerrainSampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;

/**
 * Applies terrain elevation as a bounded vertical displacement of the vanilla
 * density field. It does not add a constant density bias, which can create
 * disconnected floating terrain and destroy caves.
 */
public final class TerrainDensitySampler implements DensitySampler {
    private static final double REFERENCE_ELEVATION = 68.0;
    private static final double DISPLACEMENT_SCALE = 0.25;
    private static final int MAX_DISPLACEMENT = 24;

    private final DensitySampler vanilla;
    private final TerrainSampler terrain;
    private final ThreadLocal<ColumnCache> columnCache =
            ThreadLocal.withInitial(ColumnCache::new);

    public TerrainDensitySampler(DensitySampler vanilla, TerrainSampler terrain) {
        this.vanilla = vanilla;
        this.terrain = terrain;
    }

    @Override
    public float sampleValue(SamplerContext context, int x, int y, int z) {
        int displacement = displacementAt(x, z);
        return vanilla.sampleValue(context, x, y - displacement, z);
    }

    /**
     * Preserve the vanilla sampler's optimized bulk path. The vertical
     * displacement is applied as a first-order shift using the density
     * gradient already present in the sampled volume.
     */
    @Override
    public void sampleVolume(
            SamplerContext context,
            DensityBuffer buffer,
            DensityVolume volume
    ) {
        vanilla.sampleVolume(context, buffer, volume);

        int sizeX = volume.sizeX();
        int sizeY = volume.sizeY();
        int sizeZ = volume.sizeZ();
        int stepY = volume.stepBlockY();

        if (sizeY == 1 || stepY <= 0) {
            for (int z = 0; z < sizeZ; z++) {
                int worldZ = volume.minBlockZ() + z * volume.stepBlockZ();
                for (int x = 0; x < sizeX; x++) {
                    int worldX = volume.minBlockX() + x * volume.stepBlockX();
                    int displacement = displacementAt(worldX, worldZ);
                    if (displacement == 0) continue;
                    int index = volume.indexUnchecked(x, 0, z);
                    float shifted = vanilla.sampleValue(
                            context,
                            worldX,
                            volume.minBlockY() - displacement,
                            worldZ
                    );
                    buffer.set(index, shifted);
                }
            }
            return;
        }

        // Compute the expensive 2D terrain sample once per vertical column.
        int[][] displacements = new int[sizeZ][sizeX];
        for (int z = 0; z < sizeZ; z++) {
            int worldZ = volume.minBlockZ() + z * volume.stepBlockZ();
            for (int x = 0; x < sizeX; x++) {
                int worldX = volume.minBlockX() + x * volume.stepBlockX();
                displacements[z][x] = displacementAt(worldX, worldZ);
            }
        }

        for (int z = 0; z < sizeZ; z++) {
            for (int y = 0; y < sizeY; y++) {
                for (int x = 0; x < sizeX; x++) {
                    int displacement = displacements[z][x];
                    if (displacement == 0) continue;

                    int index = volume.indexUnchecked(x, y, z);
                    float current = buffer.get(index);
                    float derivative;

                    if (y == 0) {
                        int above = volume.indexUnchecked(x, y + 1, z);
                        derivative = (buffer.get(above) - current) / stepY;
                    } else if (y == sizeY - 1) {
                        int below = volume.indexUnchecked(x, y - 1, z);
                        derivative = (current - buffer.get(below)) / stepY;
                    } else {
                        int above = volume.indexUnchecked(x, y + 1, z);
                        int below = volume.indexUnchecked(x, y - 1, z);
                        derivative = (buffer.get(above) - buffer.get(below)) / (2.0f * stepY);
                    }

                    buffer.addTo(index, -displacement * derivative);
                }
            }
        }
    }

    /** Signed vertical displacement in blocks, bounded to protect world limits. */
    public static int terrainDisplacement(TerrainSampler terrain, int x, int z) {
        double requested = (terrain.sample(x, z).elevation() - REFERENCE_ELEVATION)
                * DISPLACEMENT_SCALE;
        return (int) Math.round(Math.max(-MAX_DISPLACEMENT,
                Math.min(MAX_DISPLACEMENT, requested)));
    }

    private int displacementAt(int x, int z) {
        ColumnCache cache = columnCache.get();
        if (cache.valid && cache.x == x && cache.z == z) {
            return cache.displacement;
        }

        int displacement = terrainDisplacement(terrain, x, z);
        cache.x = x;
        cache.z = z;
        cache.displacement = displacement;
        cache.valid = true;
        return displacement;
    }

    private static final class ColumnCache {
        private int x;
        private int z;
        private int displacement;
        private boolean valid;
    }
}
