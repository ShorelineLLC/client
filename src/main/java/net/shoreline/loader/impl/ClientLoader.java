package net.shoreline.loader.impl;

import net.shoreline.loader.Loader;
import net.shoreline.loader.impl.antidump.Measure;
import net.shoreline.loader.impl.classloading.ClassLoader;

public final class ClientLoader
{
    public static void loadClient()
    {
        long startTime = System.currentTimeMillis();

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
        Loader.LOGGER.info("Finished loading Shoreline in " + timeElapsedInSeconds + " seconds.");
    }
}
