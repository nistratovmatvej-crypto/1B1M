package dev.onebym.core;

import java.nio.file.Path;

/** CLI used for deterministic smoke checks and rough sampler profiling. */
public final class PrototypeRunner {
    public static void main(String[] args) throws Exception {
        long seed=args.length>0?Long.parseLong(args[0]):123456789L;
        int samples=args.length>1?Integer.parseInt(args[1]):250_000;
        WorldConfig config=ConfigIO.loadOrCreate(Path.of("config/1b1m.properties"));
        TerrainSampler sampler=new TerrainSampler(seed,config);
        long start=System.nanoTime(); double elevationSum=0; long treeCandidates=0;
        for(int i=0;i<samples;i++) {
            int x=(i*37)%60_000-30_000, z=(i*91)%60_000-30_000;
            TerrainSampler.Sample s=sampler.sample(x,z); elevationSum+=s.elevation();
            if(sampler.shouldPlaceTree(x,z)) treeCandidates++;
        }
        long elapsed=System.nanoTime()-start;
        System.out.printf("1B1M sampler | seed=%d | samples=%d | avgElevation=%.2f | treeCandidates=%d | elapsed=%.2f ms | %.0f samples/s%n",
                seed,samples,elevationSum/samples,treeCandidates,elapsed/1_000_000.0,samples/(elapsed/1_000_000_000.0));
    }
}
