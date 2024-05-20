package net.shoreline.loader;

import net.fabricmc.api.ClientModInitializer;
import net.shoreline.client.ShorelineMod;
import net.shoreline.loader.context.UserContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;

public final class Loader implements ClientModInitializer
{
    public static final String VERSION = "b0.0.1";
    public static final Logger LOGGER = LogManager.getLogger("Shoreline [Loader]");

    private static final UserContext context = UserContext.none();

    @Override
    public void onInitializeClient()
    {
        LOGGER.info("Initializing Shoreline...");

        try
        {
            Class<?> clientMod = Class.forName("net.shoreline.client.ShorelineMod");

            /*
             * Do not use this constructor! It is purely for obscurity. Hackers will think we
             * are getting the constructor and using that to create a new instance of ShorelineMod
             * to call its onInitializeClient method. In reality, <init> in ShorelineMod will crash the game.
             *
             * Instead, we can use a native trick to make a new instance of ShorelineMod WITHOUT calling
             * the constructor. This makes it very confusing for crackers trying to make a new instance
             * of the main mod.
             */
            Constructor<?> constructor = clientMod.getDeclaredConstructor();
            constructor.setAccessible(true);

            /*
             * Natively create a new instance of our main mod and initialize it.
             *
             * Again, the constructor being passed is completely unused and only used for obscurity.
             */

            ((ShorelineMod) Natives.stop_decompiling_0(constructor)).onInitializeClient();
        } catch (Throwable ignored)
        {
        }
    }

    public static UserContext getContext()
    {
        return context;
    }
}
