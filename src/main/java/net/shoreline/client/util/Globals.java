package net.shoreline.client.util;

import net.minecraft.client.MinecraftClient;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * TERRIBLE CODING PRACTICE
 *
 * @author linus
 * @since 1.0
 */
public interface Globals
{
    // Minecraft game instance
    MinecraftClient mc = MinecraftClient.getInstance();
    //
    Random RANDOM = ThreadLocalRandom.current();
}
