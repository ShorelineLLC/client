package net.shoreline.client;

import net.shoreline.client.api.Identifiable;
import net.shoreline.client.api.file.ClientConfiguration;
import net.shoreline.client.impl.irc.IRCManager;
import net.shoreline.client.init.Managers;
import net.shoreline.loader.Loader;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Client main class. Handles main client mod initializing of static handler
 * instances and client managers.
 *
 * @author linus
 * @see ShorelineMod
 * @since 1.0
 */
public class Shoreline
{
    // Client configuration handler. This master saves/loads the client
    // configuration files which have been saved locally.
    public static ClientConfiguration CONFIG;
    // Client shutdown hooks which will run once when the MinecraftClient
    // game instance is shutdown.
    public static ShutdownHook SHUTDOWN;
    public static Executor EXECUTOR;

    /**
     * Called during {@link ShorelineMod#onInitializeClient()}
     */
    public static void init()
    {
        // Debug information - required when submitting a crash / bug report
        info("This build of Shoreline is on Git hash {} and was compiled on {}", BuildConfig.HASH, BuildConfig.BUILD_TIME);
        info("Starting preInit ...");

        EXECUTOR = Executors.newFixedThreadPool(1);
        info("Starting init ...");
        Managers.init();
        // Commands.init();
        info("Starting postInit ...");
        CONFIG = new ClientConfiguration();
        Managers.postInit();
        SHUTDOWN = new ShutdownHook();
        Runtime.getRuntime().addShutdownHook(SHUTDOWN);
        // load configs AFTER everything has been initialized
        // this is to prevent configs loading before certain aspects of managers are available
        CONFIG.loadClient();

        IRCManager.getInstance(); // Create new IRC manager
    }

    public static void info(String message)
    {
        Loader.info(message);
    }

    public static void info(String message,
                            Object... params)
    {
        Loader.info(message, params);
    }

    public static void info(Identifiable feature,
                            String message)
    {
        Loader.info(String.format("[%s] %s", feature.getId(), message));
    }

    public static void info(Identifiable feature,
                            String message,
                            Object... params)
    {
        Loader.info(String.format("[%s] %s", feature.getId(), message), params);
    }

    public static void error(String message)
    {
        Loader.error(message);
    }

    public static void error(String message,
                             Object... params)
    {
        Loader.error(message, params);
    }

    public static void error(Identifiable feature, String message)
    {
        Loader.error(String.format("[%s] %s", feature.getId(), message));
    }

    public static void error(Identifiable feature,
                             String message,
                             Object... params)
    {
        Loader.error(String.format("[%s] %s", feature.getId(), message), params);
    }
}
