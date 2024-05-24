package net.shoreline.loader.impl;

import net.shoreline.loader.Loader;
import net.shoreline.loader.impl.stage.LoadingStage;
import net.shoreline.loader.impl.stage.authentication.AuthenticationStage;

public final class ClientLoader
{
    public static void loadClient()
    {
        long startTime = System.currentTimeMillis();

        LoadingStage currentStage = AuthenticationStage.getInstance();

        while (currentStage != null)
        {
            try
            {
                currentStage.run();
            } catch (Throwable t)
            {
                currentStage.error(Loader.getContext(), t);
            }

            currentStage = currentStage.next();
        }

        double timeElapsedInSeconds = (System.currentTimeMillis() - startTime) / 1000.0D;
        Loader.LOGGER.info("Finished loading Shoreline in " + timeElapsedInSeconds + " seconds.");
    }
}
