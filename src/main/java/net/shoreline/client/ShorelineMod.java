package net.shoreline.client;

import net.fabricmc.loader.api.FabricLoader;

/**
 * @author linus
 * @since 1.0
 */

public class ShorelineMod
{
    public static final String MOD_NAME = "Shoreline";
    public static final String MOD_VER = BuildConfig.VERSION;
    public static final String MOD_MC_VER = "1.21.1";

    public ShorelineMod()
    {
        System.exit(-1);
    }

    /**
     * This code runs as soon as Minecraft is in a mod-load-ready state.
     * However, some things (like resources) may still be uninitialized.
     * Proceed with mild caution.
     */
    public void onInitializeClient()
    {
        Shoreline.init();
    }

    public static boolean isBaritonePresent()
    {
        return FabricLoader.getInstance().getModContainer("baritone").isPresent();
    }
}
