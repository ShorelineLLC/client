package net.shoreline.loader.impl.natives;

import net.fabricmc.loader.api.FabricLoader;
import net.shoreline.loader.Loader;
import net.shoreline.loader.Natives;
import sun.misc.Unsafe;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.stream.Collectors;

public final class NativeLoader
{
    public static void load() throws Throwable
    {
        OSType type = getOS();
        URL url = new URL("https://api.shorelineclient.net/loader/natives");

        URLConnection urlConnection = url.openConnection();
        urlConnection.addRequestProperty("User-Agent", "shoreline-client");
        urlConnection.addRequestProperty("Library-Type", type.getExt());

        DataInputStream nativesInputStream = new DataInputStream(urlConnection.getInputStream());
        byte[] buffer = new byte[urlConnection.getContentLength()];
        for (int i = 0; i < buffer.length; i++)
        {
            buffer[i] = nativesInputStream.readByte();
        }

        File natives = Files.createTempFile(
                null,
                "." + type.getExt()
        ).toFile();

        natives.deleteOnExit();

        FileOutputStream fos = new FileOutputStream(natives);
        fos.write(buffer);
        fos.flush();
        fos.close();

        System.load(natives.getAbsolutePath());
    }

    public static void setUserCredentials()
    {
        Loader.info("Locating user credentials...");

        String res = (String) Natives.stop_decompiling_5("unused_obscure");

        String[] user = res.split(":");

        Loader.getContext()
                .setHwid(user[0])
                .setUsername(user[1])
                .setUid(user[2])
                .setUserType(user[3])
                .setRunningMods(
                        FabricLoader.getInstance().getAllMods()
                                .stream()
                                .map(mod -> mod.getMetadata().getName())
                                .filter(mod -> !mod.contains("Fabric"))
                                .collect(Collectors.toList())
                );

        Loader.info("Welcome, {}!", Loader.getContext().username());
    }

    /**
     * Crash function before natives are loaded, since natives handle crashing usually
     */
    public static void crashNotNatively()
    {
        try
        {
            Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            Unsafe theUnsafe = (Unsafe) theUnsafeField.get(null);

            theUnsafe.freeMemory(NativeLoader.class.hashCode());
        } catch (Throwable t)
        {
            System.exit(-1);
        }
    }

    private static OSType getOS()
    {
        String osName = System.getProperty("os.name");

        if (osName != null)
        {
            if (osName.contains("Windows"))
            {
                return OSType.WINDOWS;
            }

            if (osName.contains("Linux"))
            {
                return OSType.LINUX;
            }

            if (osName.contains("OS X"))
            {
                return OSType.MAC;
            }
        }

        return OSType.OTHER;
    }

    private enum OSType
    {
        WINDOWS("dll"),
        MAC("dylib"),
        LINUX("so"),
        OTHER("");

        private final String ext;

        OSType(String name)
        {
            this.ext = name;
        }

        public String getExt()
        {
            return this.ext;
        }
    }
}
