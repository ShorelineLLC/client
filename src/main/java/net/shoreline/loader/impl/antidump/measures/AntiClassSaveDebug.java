package net.shoreline.loader.impl.antidump.measures;

import net.shoreline.loader.impl.antidump.Measure;

import java.lang.management.ManagementFactory;
import java.util.List;

public final class AntiClassSaveDebug extends Measure
{
    @Override
    public void execute() throws Throwable
    {
        boolean debugClassLoading = Boolean.parseBoolean(System.getProperty("legacy.debugClassLoading", "false"));
        boolean debugClassLoadingFiner = Boolean.parseBoolean(System.getProperty("legacy.debugClassLoadingFiner", "false"));
        boolean debugClassLoadingSave = Boolean.parseBoolean(System.getProperty("legacy.debugClassLoadingSave", "false"));

        if (debugClassLoading || debugClassLoadingFiner || debugClassLoadingSave)
        {
            throw new Throwable("Class debug saving property enabled.");
        }
    }
}
