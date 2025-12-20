package net.shoreline.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Formatting;
import net.shoreline.client.impl.render.ClientFormatting;

/**
 * @author linus
 * @since 2.0
 */
public class ShorelineMod implements ClientModInitializer
{
    public static final String MOD_NAME = "Shoreline";
    public static final String MOD_ID = "shoreline";
    public static final String MOD_VER = BuildConfig.VERSION;
    public static final String MOD_MC_VER = "1.21.4";

    /**
     * This code runs as soon as Minecraft is in a mod-load-ready state.
     * However, some things (like resources) may still be uninitialized.
     * Proceed with mild caution.
     */
    @Override
    public void onInitializeClient()
    {
        Shoreline.init();
    }

    public static String getFormattedVersion()
    {
        return String.format(ClientFormatting.THEME + "%s " + Formatting.WHITE + "%s %s-%s",
                ShorelineMod.MOD_NAME,
                ShorelineMod.MOD_VER,
                BuildConfig.BUILD_IDENTIFIER,
                BuildConfig.HASH);
    }

    public static boolean isBaritonePresent()
    {
        return FabricLoader.getInstance().getModContainer("baritone").isPresent();
    }
}
