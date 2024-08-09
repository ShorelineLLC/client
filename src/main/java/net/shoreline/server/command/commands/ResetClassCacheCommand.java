package net.shoreline.server.command.commands;

import net.shoreline.server.ServerMain;
import net.shoreline.server.command.Command;
import net.shoreline.server.route.loader.LoaderEndpoint;
import net.shoreline.server.route.loader.classcache.ClassCache;

import java.io.File;

public final class ResetClassCacheCommand extends Command
{
    @Override
    public void execute(String[] args, boolean askConfirm) throws Throwable
    {
        if (args.length != 1)
        {
            throw new IllegalStateException("Usage: " + getUsage());
        }

        String userType = args[0];

        switch (userType.toLowerCase())
        {
            case "dev": {
                File devJar = new File("/home/container/assets/loader/dev/client.jar");
                LoaderEndpoint.DEV_CLASS_CACHE = new ClassCache(devJar);
                break;
            }
            case "beta": {
                File betaJar = new File("/home/container/assets/loader/beta/client.jar");
                LoaderEndpoint.BETA_CLASS_CACHE = new ClassCache(betaJar);
                break;
            }
            case "release": {
                throw new IllegalStateException("Release class cache is not yet implemented");
            }
            default: {
                throw new IllegalStateException("Unknown user type: " + userType);
            }
        }

        ServerMain.LOGGER.info("Successfully reset the class cache for {}", userType);
    }

    @Override
    public String getUsage()
    {
        return "resetclasscache <usertype>";
    }

    @Override
    public String getDescription()
    {
        return "Resets the class cache for a client jar of the given usertype without restarting the server.";
    }
}
