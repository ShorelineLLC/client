package net.shoreline.client;

import net.shoreline.client.api.file.ClientConfiguration;

/**
 * @author linus
 * @since 1.0
 */
public class ShutdownHook extends Thread
{
    /**
     *
     */
    public ShutdownHook()
    {
        setName("Shoreline-ShutdownHook");
    }

    /**
     * This runs when the game is shutdown and saves the
     * {@link ClientConfiguration} files.
     *
     * @see ClientConfiguration#saveClient()
     */
    @Override
    public void run()
    {
        Shoreline.info("Saving configurations and shutting down!");
        Shoreline.CONFIG.saveClient();
        Shoreline.CONFIG.saveClickGui();
        Shoreline.CONFIG.saveFonts();
    }
}
