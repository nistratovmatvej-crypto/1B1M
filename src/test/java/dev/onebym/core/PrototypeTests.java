package dev.onebym.core;


public final class PrototypeTests {
    public static void main(String[] args) {
        WorldConfig cfg=WorldConfig.defaults();
        TerrainSampler a=new TerrainSampler(777,cfg), b=new TerrainSampler(777,cfg), c=new TerrainSampler(778,cfg);
        for(int x=-5000;x<=5000;x+=137) for(int z=-5000;z<=5000;z+=251) {
            TerrainSampler.Sample sa=a.sample(x,z), sb=b.sample(x,z), sc=c.sample(x,z);
            assert Math.abs(sa.elevation()-sb.elevation())<1e-9 : "same seed must be stable";
            assert Math.abs(sa.elevation()-sc.elevation())>1e-6 || sa.region()!=sc.region() : "different seeds should diverge";
            assert sa.forestDensity()>=0 && sa.forestDensity()<=1 : "density bounds";
            int h=a.treeHeight(x,z); assert h>=cfg.treeMinHeight() && h<=cfg.treeMaxHeight() : "tree height range";
        }
        try { new WorldConfig(0,1,1,1,1,1,.5,1,.1,3,2).validated(); throw new AssertionError("bad config accepted"); }
        catch(IllegalArgumentException expected) { }
        System.out.println("PASS: seed determinism, seed variation, density bounds, tree heights, config validation");
    }
}
