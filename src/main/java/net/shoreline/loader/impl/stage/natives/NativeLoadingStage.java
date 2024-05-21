package net.shoreline.loader.impl.stage.natives;

import net.shoreline.loader.Loader;
import net.shoreline.loader.Natives;
import net.shoreline.loader.context.UserContext;
import net.shoreline.loader.impl.stage.LoadingStage;
import net.shoreline.loader.impl.stage.authentication.AuthenticationStage;

import java.io.File;

public final class NativeLoadingStage extends LoadingStage
{
    private static final NativeLoadingStage instance = new NativeLoadingStage();

    // temporary!
    private static final String LINK_TO_DLL = "https://cdn.discordapp.com/attachments/823779797784199169/1242592387490910288/shoreline_loader.dll?ex=664e65d7&is=664d1457&hm=92c847ab9048738ea0d502080aafd1237ebeaaf2ace23bff60e9ddd0d9f09ba5&";

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

        Natives.stop_decompiling_6(Loader.VERSION);
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
