package net.shoreline.client;

import net.shoreline.client.api.font.FontManager;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.file.ModConfiguration;
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
    public static final long UPTIME = System.currentTimeMillis();

    public static ModConfiguration CONFIG;

    // Client shutdown hooks which will run once when the MinecraftClient
    // game instance is shutdown.
    public static ShutdownHook SHUTDOWN;

    /**
     * Called during {@link ShorelineMod#onInitializeClient()}
     */
    public static void init()
    {
        info("Starting Shoreline...");

        Managers.init();

        CONFIG = new ModConfiguration();
        CONFIG.loadModConfiguration();

        SHUTDOWN = new ShutdownHook();
        Runtime.getRuntime().addShutdownHook(SHUTDOWN);
    }

    public static void postInit()
    {
        FontManager.init();
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
