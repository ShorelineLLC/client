package net.shoreline.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

/**
 * Fabric {@link ModInitializer}.
 *
 * @author linus
 * @since 1.0
 */
public class ShorelineMod implements ClientModInitializer {
    public static final String MOD_NAME = "Shoreline";
    public static final String MOD_VER = BuildConfig.VERSION;
    public static final String MOD_BUILD_NUMBER = BuildConfig.BUILD_IDENTIFIER + "-" +  BuildConfig.BUILD_NUMBER;
    public static final String MOD_MC_VER = "1.20.4";

    /**
     * This code runs as soon as Minecraft is in a mod-load-ready state.
     * However, some things (like resources) may still be uninitialized.
     * Proceed with mild caution.
     */
    @Override
    public void onInitializeClient() {
        Shoreline.init();
    }

    public static boolean isBaritonePresent() {
        return FabricLoader.getInstance().getModContainer("baritone").isPresent();
    }
}
