package net.shoreline.loader;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.shoreline.client.ShorelineMod;
import net.shoreline.loader.context.UserContext;
import net.shoreline.loader.impl.natives.NativeLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;

public final class Loader implements ClientModInitializer, PreLaunchEntrypoint
{
    public static final String VERSION = "b0.2.0";
    private static final Logger LOGGER = LogManager.getLogger("Shoreline");

    private static final UserContext context = UserContext.none();

    @Override
    public void onInitializeClient()
    {
        info("Initializing Shoreline...");

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
            ignored.printStackTrace();
        }
    }

    @Override
    public void onPreLaunch()
    {
        System.setProperty("java.awt.headless", "true");
        GraphicsEnvironment.isHeadless();

        if (FabricLoader.getInstance().isDevelopmentEnvironment())
        {
            Loader.info("Dev workspace detected, loading natives...");

            try
            {
                NativeLoader.load();
            } catch (Throwable t)
            {
                Loader.error("Failed to load native libraries", t);
                NativeLoader.crashNotNatively();
            }

            NativeLoader.setUserCredentials();

            Loader.info("Native library successfully loaded, starting Shoreline...");
        }
    }

    public static UserContext getContext()
    {
        return context;
    }

    public static InputStream getResource(String name)
    {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment())
        {
            byte[] content = (byte[]) Natives.stop_decompiling_10(name);

            if (content == null)
            {
                return null;
            }

            return new ByteArrayInputStream(content);
        }

        return Loader.class.getClassLoader().getResourceAsStream(name);
    }

    public static void info(String message)
    {
        Loader.LOGGER.info(String.format("[Shoreline] %s", message));
    }

    public static void info(String message,
                            Object... params)
    {
        Loader.LOGGER.info(String.format("[Shoreline] %s", message), params);
    }

    public static void error(String message)
    {
        Loader.LOGGER.error(message);
    }

    public static void error(String message,
                             Object... params)
    {
        Loader.LOGGER.error(message, params);
    }
}
