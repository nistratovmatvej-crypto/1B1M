package dev.onebym.core;

/** Seed-stable conceptual terrain sampler for prototype evaluation. This is not yet wired into Minecraft's generator. */
public final class TerrainSampler {
    public enum Region { PLAINS, FOREST, HILLS, MOUNTAINS }
    public record Sample(double elevation, double ridge, double valley, double forestDensity, Region region) {}
    private final long seed;
    private final WorldConfig config;
    public TerrainSampler(long seed, WorldConfig config) { this.seed=seed; this.config=config.validated(); }

    public Sample sample(int x, int z) {
        double continental = Noise2D.fractal(seed, x*config.continentalScale(), z*config.continentalScale(), 4, .52);
        double ridgeBase = Noise2D.fractal(seed ^ 0x4D4F554E5441494EL, x*config.ridgeScale(), z*config.ridgeScale(), 3, .55);
        // Ridged signal forms connected mountain belts rather than isolated peaks.
        double ridge = 1.0-Math.abs(ridgeBase);
        ridge = Math.pow(Math.max(0, ridge), 2.2);
        double valleySignal = Noise2D.fractal(seed ^ 0x56414C4C45594CL, x*config.valleyScale(), z*config.valleyScale(), 3, .58);
        double valley = Math.max(0, 1-Math.abs(valleySignal)*2.15);
        double detail = Noise2D.fractal(seed ^ 0x44455441494CL, x*.0031, z*.0031, 2, .48);
        double elevation = 68 + continental*92 + ridge*config.ridgeStrength()*210 - valley*config.valleyStrength()*58 + detail*config.plainsRoughness()*32;
        double mountainScore = clamp((ridge*config.ridgeStrength() + continental*.25 - .17) / .62);
        double forestPatch = (Noise2D.fractal(seed ^ 0x464F52455354L, x*config.forestPatchScale(), z*config.forestPatchScale(), 3, .55)+1)/2;
        double localClimate = (Noise2D.fractal(seed ^ 0x434C494D415445L, x*.00023, z*.00023, 3, .55)+1)/2;
        double forestDensity = clamp((forestPatch*.68 + localClimate*.32) * config.forestDensity());
        double plainness = clamp(1-mountainScore*1.45-valley*.22);
        Region region = mountainScore > .62 ? Region.MOUNTAINS : mountainScore > .34 ? Region.HILLS : forestDensity > .22 ? Region.FOREST : Region.PLAINS;
        // Smooth conceptual biome influence rather than abrupt boundaries.
        if (plainness > .78 && forestDensity < .22) region=Region.PLAINS;
        return new Sample(elevation, ridge, valley, forestDensity, region);
    }
    public boolean shouldPlaceTree(int x, int z) {
        Sample s=sample(x,z);
        if (s.region()==Region.MOUNTAINS) return false;
        double threshold = s.region()==Region.PLAINS ? .82 : .38;
        double treeNoise=(Noise2D.sample(seed ^ 0x54524545504C4143L,x*.12,z*.12)+1)/2;
        return treeNoise > threshold && treeNoise < threshold + s.forestDensity()*.5;
    }
    public int treeHeight(int x,int z) {
        double n=(Noise2D.sample(seed ^ 0x5452454548454947L,x*.19,z*.19)+1)/2;
        return config.treeMinHeight()+(int)Math.round(n*(config.treeMaxHeight()-config.treeMinHeight()));
    }
    private static double clamp(double d){return Math.max(0,Math.min(1,d));}
}
