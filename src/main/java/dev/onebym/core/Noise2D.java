package dev.onebym.core;


/** Deterministic value-noise utility. No shared mutable state: safe for parallel sampling. */
final class Noise2D {
    private Noise2D() {}
    static double sample(long seed, double x, double z) {
        int x0 = (int)Math.floor(x), z0 = (int)Math.floor(z);
        double fx = fade(x - x0), fz = fade(z - z0);
        double a = hash(seed, x0, z0), b = hash(seed, x0 + 1, z0);
        double c = hash(seed, x0, z0 + 1), d = hash(seed, x0 + 1, z0 + 1);
        return lerp(lerp(a,b,fx), lerp(c,d,fx), fz) * 2 - 1;
    }
    static double fractal(long seed, double x, double z, int octaves, double persistence) {
        double sum=0, amp=1, norm=0, freq=1;
        for(int i=0;i<octaves;i++) { sum += sample(seed + i*0x9E3779B97F4A7C15L,x*freq,z*freq)*amp; norm+=amp; amp*=persistence; freq*=2; }
        return sum/norm;
    }
    private static double hash(long seed, int x, int z) {
        long n=seed ^ (x*0x632BE59BD9B4E019L) ^ (z*0x9E3779B97F4A7C15L);
        n=(n^(n>>>30))*0xBF58476D1CE4E5B9L; n=(n^(n>>>27))*0x94D049BB133111EBL; n^=n>>>31;
        return (n >>> 11) * 0x1.0p-53;
    }
    private static double fade(double t){return t*t*t*(t*(t*6-15)+10);}
    private static double lerp(double a,double b,double t){return a+(b-a)*t;}
}
