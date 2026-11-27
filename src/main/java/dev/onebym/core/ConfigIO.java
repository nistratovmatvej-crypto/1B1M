package dev.onebym.core;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public final class ConfigIO {
    private ConfigIO() {}
    public static WorldConfig loadOrCreate(Path path) throws IOException {
        WorldConfig defaults=WorldConfig.defaults();
        if (!Files.exists(path)) { save(path,defaults); return defaults; }
        Properties p=new Properties(); try(Reader r=Files.newBufferedReader(path)){p.load(r);}
        return new WorldConfig(d(p,"continentalScale",defaults.continentalScale()),d(p,"ridgeScale",defaults.ridgeScale()),d(p,"ridgeStrength",defaults.ridgeStrength()),d(p,"valleyScale",defaults.valleyScale()),d(p,"valleyStrength",defaults.valleyStrength()),d(p,"plainsRoughness",defaults.plainsRoughness()),d(p,"forestDensity",defaults.forestDensity()),d(p,"forestPatchScale",defaults.forestPatchScale()),d(p,"biomeTransitionWidth",defaults.biomeTransitionWidth()),i(p,"treeMinHeight",defaults.treeMinHeight()),i(p,"treeMaxHeight",defaults.treeMaxHeight())).validated();
    }
    public static void save(Path path, WorldConfig c) throws IOException {
        Files.createDirectories(path.toAbsolutePath().getParent()); Properties p=new Properties();
        p.setProperty("continentalScale",Double.toString(c.continentalScale())); p.setProperty("ridgeScale",Double.toString(c.ridgeScale())); p.setProperty("ridgeStrength",Double.toString(c.ridgeStrength())); p.setProperty("valleyScale",Double.toString(c.valleyScale())); p.setProperty("valleyStrength",Double.toString(c.valleyStrength())); p.setProperty("plainsRoughness",Double.toString(c.plainsRoughness())); p.setProperty("forestDensity",Double.toString(c.forestDensity())); p.setProperty("forestPatchScale",Double.toString(c.forestPatchScale())); p.setProperty("biomeTransitionWidth",Double.toString(c.biomeTransitionWidth())); p.setProperty("treeMinHeight",Integer.toString(c.treeMinHeight())); p.setProperty("treeMaxHeight",Integer.toString(c.treeMaxHeight()));
        try(Writer w=Files.newBufferedWriter(path)){p.store(w,"1B1M generation config");}
    }
    private static double d(Properties p,String k,double f){return Double.parseDouble(p.getProperty(k,Double.toString(f)));}
    private static int i(Properties p,String k,int f){return Integer.parseInt(p.getProperty(k,Integer.toString(f)));}
}
