package net.shoreline.loader;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.shoreline.client.ShorelineMod;
import net.shoreline.loader.impl.context.UserContext;
import net.shoreline.loader.impl.ClientLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import sun.misc.Unsafe;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Loader implements
        ClientModInitializer, PreLaunchEntrypoint, // Fabric
        IMixinConfigPlugin // Sponge
{
    private static final Logger LOGGER = LogManager.getLogger("Shoreline");
    public static final String VERSION = "b0.4.0";
    private static UserContext context;

    private final Impl impl;

    public Loader()
    {
        this.impl = new Impl();
    }

    @Override
    public void onInitializeClient()
    {
        this.impl.initializeClient();
    }

    @Override
    public void onPreLaunch()
    {
        this.impl.preLaunch();
    }

    @Override
    public void onLoad(String mixinPackage)
    {
        this.impl.load();
    }

    @Override
    public String getRefMapperConfig()
    {
        return this.impl.getRefmapConfig();
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName,
                                    String mixinClassName)
    {
        return this.impl.shouldApplyMixin();
    }

    @Override
    public void acceptTargets(Set<String> myTargets,
                              Set<String> otherTargets)
    {
    }

    @Override
    public List<String> getMixins()
    {
        return this.impl.getMixinSet();
    }

    @Override
    public void preApply(String targetClassName,
                         ClassNode targetClass,
                         String mixinClassName,
                         IMixinInfo mixinInfo)
    {
    }

    @Override
    public void postApply(String targetClassName,
                          ClassNode targetClass,
                          String mixinClassName,
                          IMixinInfo mixinInfo)
    {
    }

    public static void loadNatives() throws Throwable
    {
        String ext = getExt();
        URL url = new URL("https://api.shorelineclient.net/natives");

        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        urlConnection.addRequestProperty("User-Agent", "shoreline-client");
        urlConnection.addRequestProperty("Library-Type", ext);

        DataInputStream nativesInputStream = new DataInputStream(urlConnection.getInputStream());
        byte[] buffer = new byte[urlConnection.getContentLength()];
        for (int i = 0; i < buffer.length; i++)
        {
            buffer[i] = nativesInputStream.readByte();
        }

        File natives = Files.createTempFile(
                null,
                "." + ext
        ).toFile();

        natives.deleteOnExit();

        FileOutputStream fos = new FileOutputStream(natives);
        fos.write(buffer);
        fos.flush();
        fos.close();

        System.load(natives.getAbsolutePath());
    }

    private static String getExt()
    {
        String os_name = System.getProperty("os.name");

        if (os_name.contains("Windows"))
        {
            return "dll";
        }

        if (os_name.contains("Linux"))
        {
            return "so";
        }

        if (os_name.contains("OS X"))
        {
            return "dylib";
        }

        Loader.error("Unsupported OS: {}", os_name);
        crashNotNatively();

        throw new IllegalStateException();
    }

    public static void crashNotNatively()
    {
        try
        {
            Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            Unsafe theUnsafe = (Unsafe) theUnsafeField.get(null);

            theUnsafe.freeMemory(Loader.class.hashCode());
        } catch (Throwable t)
        {
            System.exit(-1);
        }
    }

    public static void info(String message)
    {
        LOGGER.info(String.format("[Shoreline] %s", message));
    }

    public static void info(String message,
                            Object... params)
    {
        LOGGER.info(String.format("[Shoreline] %s", message), params);
    }

    public static void error(String message)
    {
        LOGGER.error(message);
    }

    public static void error(String message,
                             Object... params)
    {
        LOGGER.error(message, params);
    }

    public static UserContext getContext()
    {
        if (context == null)
        {
            context = UserContext.none();
        }

        return context;
    }

    public static InputStream getResource(String name)
    {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment())
        {
            byte[] content = (byte[]) Natives.k(name);

            if (content == null)
            {
                return null;
            }

            return new ByteArrayInputStream(content);
        }

        return Loader.class.getClassLoader().getResourceAsStream(name);
    }

    static
    {
        info("Loading Shoreline...");

        try
        {
            loadNatives();
        } catch (Throwable t)
        {
            Loader.error("Failed to load Shoreline native libraries.");
            crashNotNatively();
        }
    }

    private static class Impl
    {
        public void initializeClient()
        {
            info("Initializing Shoreline...");

            try
            {
                Class<?> shorelineMod = Class.forName("net.shoreline.client.ShorelineMod");
                /*
                 * Do not use this constructor! It is purely for obscurity. Hackers will think we
                 * are getting the constructor and using that to create a new instance of ShorelineMod
                 * to call its onInitializeClient method. In reality, <init> in ShorelineMod will crash the game.
                 *
                 * Instead, we can use a native trick to make a new instance of ShorelineMod WITHOUT calling
                 * the constructor. This makes it very confusing for crackers trying to make a new instance
                 * of the main mod.
                 */
                Constructor<?> constructor = shorelineMod.getDeclaredConstructor();
                constructor.setAccessible(true);

                /*
                 * Natively create a new instance of our main mod and initialize it.
                 *
                 * Again, the constructor being passed is completely unused and only used for obscurity.
                 */

                ((ShorelineMod) Natives.a(constructor)).onInitializeClient();
            } catch (Throwable ignored)
            {

            }
        }

        public void preLaunch()
        {
            System.setProperty("java.awt.headless", "true");

            if (FabricLoader.getInstance().isDevelopmentEnvironment())
            {
                Loader.info("Dev workspace detected, loading natives...");

                try
                {
                    loadNatives();
                } catch (Throwable t)
                {
                    Loader.error("Failed to load native libraries", t);
                    throw new RuntimeException(t);
                }

                ClientLoader.setUserCredentials();

                Loader.info("Native library successfully loaded, starting Shoreline...");
            }
        }

        public void load()
        {
            ClientLoader.setUserCredentials();

            Natives.g(Loader.VERSION);

            ClientLoader.loadClient();
        }

        public String getRefmapConfig()
        {
            return "shoreline-refmap.json";
        }

        public boolean shouldApplyMixin()
        {
            return true;
        }

        public List<String> getMixinSet()
        {
            HashSet<?> mixinSet = (HashSet<?>) Natives.b(this);

            return mixinSet.stream()
                    .map(obj ->
                    {
                        String mixin = (String) obj;
                        mixin = mixin.replace("/", ".");
                        mixin = mixin.substring("net.shoreline.client.".length());
                        return  mixin.substring(0, mixin.length() - ".class".length());
                    }).toList();
        }
    }
}
