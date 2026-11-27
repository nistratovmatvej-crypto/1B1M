package dev.onebym.core;

/** Tunable prototype parameters; values are deliberately conservative. */
public record WorldConfig(
        double continentalScale,
        double ridgeScale,
        double ridgeStrength,
        double valleyScale,
        double valleyStrength,
        double plainsRoughness,
        double forestDensity,
        double forestPatchScale,
        double biomeTransitionWidth,
        int treeMinHeight,
        int treeMaxHeight
) {
    public static WorldConfig defaults() {
        return new WorldConfig(0.00055, 0.00019, 0.76, 0.00042, 0.52,
                0.12, 0.34, 0.00031, 0.18, 18, 42);
    }

    public WorldConfig validated() {
        if (continentalScale <= 0 || ridgeScale <= 0 || valleyScale <= 0 || forestPatchScale <= 0)
            throw new IllegalArgumentException("Noise scales must be positive");
        if (ridgeStrength < 0 || valleyStrength < 0 || plainsRoughness < 0 || forestDensity < 0 || forestDensity > 1)
            throw new IllegalArgumentException("Strengths must be non-negative and forestDensity must be 0..1");
        if (biomeTransitionWidth <= 0 || treeMinHeight < 1 || treeMaxHeight < treeMinHeight)
            throw new IllegalArgumentException("Invalid transition width or tree height range");
        return this;
    }
}
