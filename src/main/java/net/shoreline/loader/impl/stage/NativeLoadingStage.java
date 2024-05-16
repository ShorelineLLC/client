package net.shoreline.loader.impl.stage;

import net.shoreline.loader.Loader;
import net.shoreline.loader.context.UserContext;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;

public final class NativeLoadingStage extends LoadingStage
{
    private static final NativeLoadingStage instance = new NativeLoadingStage();

    // temporary!
    private static final String LINK_TO_DLL = "https://cdn.discordapp.com/attachments/823779797784199169/1240563729431658536/shoreline_loader.dll?ex=66470481&is=6645b301&hm=a492861dc0f82553e4a5d84aae083b6cb02264080b20cd359029270074127091&";

    public static NativeLoadingStage getInstance()
    {
        return instance;
    }

    @Override
    public void run() throws Throwable
    {
//        Loader.LOGGER.info("Loading Shoreline natives...");
//
//        URL url = new URL(LINK_TO_DLL);
//
//        URLConnection urlConnection = url.openConnection();
//        urlConnection.addRequestProperty("User-Agent", "shoreline-client");
//        // urlConnection.addRequestProperty("Secret-Key", "iwantmydll");
//
//        DataInputStream nativesInputStream = new DataInputStream(urlConnection.getInputStream());
//        byte[] buffer = new byte[urlConnection.getContentLength()];
//        for (int i = 0; i < buffer.length; i++)
//        {
//            buffer[i] = nativesInputStream.readByte();
//        }
//
//        File tmpdir = new File(System.getProperty("java.io.tmpdir"));
//        File dll = new File(tmpdir, "shoreline.dll");
//        dll.deleteOnExit();
//
//        FileOutputStream fos = new FileOutputStream(dll);
//        fos.write(buffer);
//        fos.flush();
//        fos.close();
//
//        System.load(dll.getAbsolutePath());
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
    }

    @Override
    public void error(UserContext context,
                      Throwable throwable)
    {
        Loader.LOGGER.info("Failed to load natives: " + throwable);
    }

    @Override
    public LoadingStage next()
    {
        return AuthenticationStage.getInstance();
    }
}
