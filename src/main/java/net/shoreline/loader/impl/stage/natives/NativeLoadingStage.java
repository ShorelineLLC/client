package net.shoreline.loader.impl.stage.natives;

import net.shoreline.loader.Loader;
import net.shoreline.loader.Natives;
import net.shoreline.loader.context.UserContext;
import net.shoreline.loader.impl.stage.LoadingStage;
import net.shoreline.loader.impl.stage.authentication.AuthenticationStage;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;

public final class NativeLoadingStage extends LoadingStage
{
    private static final NativeLoadingStage instance = new NativeLoadingStage();

    public static NativeLoadingStage getInstance()
    {
        return instance;
    }

    @Override
    public void run() throws Throwable
    {
//        Loader.LOGGER.info("Loading Shoreline natives...");
//
//        OSType type = getOS();
//        URL url = new URL("https://api.shorelineclient.net/assets/" + type.getExt());
//
//        URLConnection urlConnection = url.openConnection();
//        urlConnection.addRequestProperty("User-Agent", "shoreline-client");
//        urlConnection.addRequestProperty("Secret-Key", "hockeyl8isaretard");
//
//        DataInputStream nativesInputStream = new DataInputStream(urlConnection.getInputStream());
//        byte[] buffer = new byte[urlConnection.getContentLength()];
//        for (int i = 0; i < buffer.length; i++)
//        {
//            buffer[i] = nativesInputStream.readByte();
//        }
//
//        File tmpdir = new File(System.getProperty("java.io.tmpdir"));
//        File natives = new File(tmpdir, "shoreline." + type.getExt());
//        natives.deleteOnExit();
//
//        FileOutputStream fos = new FileOutputStream(natives);
//        fos.write(buffer);
//        fos.flush();
//        fos.close();
//
//        System.load(natives.getAbsolutePath());
//
//        Loader.LOGGER.info("loading natives done");

        File dllFile = new File("C:/Users/user2/Desktop/shoreline/src/main/rust/target/debug/shoreline_loader.dll");

        if (!dllFile.exists()) {
            Loader.LOGGER.error("DLL file not found at specified location.");
            return;
        }

        try {
            System.load(dllFile.getAbsolutePath());
            Loader.LOGGER.info("loading natives done");
        } catch (UnsatisfiedLinkError e) {
            Loader.LOGGER.error("Failed to load native library: " + e.getMessage());
        }

        Natives.stop_decompiling_6(Loader.VERSION);
    }

    @Override
    public void error(UserContext context,
                      Throwable throwable)
    {
        Loader.LOGGER.info("Failed to load natives: " + throwable);
        throw new RuntimeException(throwable);
    }

    @Override
    public LoadingStage next()
    {
        return AuthenticationStage.getInstance();
    }

    private OSType getOS()
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

    public enum OSType
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
