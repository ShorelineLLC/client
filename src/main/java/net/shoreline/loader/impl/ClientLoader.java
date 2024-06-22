package net.shoreline.loader.impl;

import net.fabricmc.loader.api.FabricLoader;
import net.shoreline.loader.Loader;
import net.shoreline.loader.Natives;
import net.shoreline.loader.impl.antidump.Measure;
import net.shoreline.loader.impl.classloading.ClassLoader;

import java.util.stream.Collectors;

public final class ClientLoader
{
    public static void loadClient()
    {
        long startTime = System.currentTimeMillis();

        // Check for Fabric API, warn user if not found
        Natives.l(startTime);

        // Run anti dump measures
        Measure.runAllMeasures();

        try
        {
            ClassLoader.loadAllClasses();
        } catch (Throwable t)
        {
            throw new RuntimeException(t);
        }

        double timeElapsedInSeconds = (System.currentTimeMillis() - startTime) / 1000.0D;
        Loader.info("Finished loading Shoreline in " + timeElapsedInSeconds + " seconds.");
    }

    public static void setUserCredentials()
    {
        Loader.info("Locating user credentials...");

        String res = (String) Natives.f("unused_obscure");

        String[] user = res.split(":");

        Loader.getContext()
                .setHwid(user[0])
                .setUsername(user[1])
                .setUid(user[2])
                .setUserType(user[3])
                .setRunningMods(
                        FabricLoader.getInstance().getAllMods()
                                .stream()
                                .map(mod -> mod.getMetadata().getName())
                                .filter(mod -> !mod.contains("Fabric"))
                                .collect(Collectors.toList())
                );

        Loader.info("Welcome, {}!", Loader.getContext().username());
    }
}
