package net.shoreline.client;

import net.shoreline.client.impl.Managers;
import net.shoreline.loader.Loader;

/**
 * Client main class. Handles main client mod initializing of static handler
 * instances and client managers.
 *
 * @author linus
 * @see ShorelineMod
 * @since 2.0
 */
public class Shoreline
{
    // Client shutdown hooks which will run once when the MinecraftClient
    // game instance is shutdown.
    public static ShutdownHook SHUTDOWN;

    /**
     * Called during {@link ShorelineMod#onInitializeClient()}
     */
    public static void init()
    {
        // Debug information - required when submitting a crash / bug report
        info("This build of Shoreline is on Git hash {} and was compiled on {}", BuildConfig.HASH, BuildConfig.BUILD_TIME);
        info("Starting preInit ...");

        Managers.init();

        SHUTDOWN = new ShutdownHook();
        Runtime.getRuntime().addShutdownHook(SHUTDOWN);
    }

    public static void info(String message)
    {
        Loader.info(message);
    }

    public static void info(String message, Object... params)
    {
        Loader.info(message, params);
    }
}
