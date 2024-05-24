package net.shoreline.loader.impl.natives;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;

public final class NativeLoader
{
    public static void load() throws Throwable
    {
        OSType type = getOS();
        URL url = new URL("https://api.shorelineclient.net/assets/" + type.getExt());

        URLConnection urlConnection = url.openConnection();
        urlConnection.addRequestProperty("User-Agent", "shoreline-client");
        urlConnection.addRequestProperty("Secret-Key", "hockeyl8isaretard");

        DataInputStream nativesInputStream = new DataInputStream(urlConnection.getInputStream());
        byte[] buffer = new byte[urlConnection.getContentLength()];
        for (int i = 0; i < buffer.length; i++)
        {
            buffer[i] = nativesInputStream.readByte();
        }

        File tmpdir = new File(System.getProperty("java.io.tmpdir"));
        File natives = new File(tmpdir, "shoreline." + type.getExt());
        natives.deleteOnExit();

        FileOutputStream fos = new FileOutputStream(natives);
        fos.write(buffer);
        fos.flush();
        fos.close();

        System.load(natives.getAbsolutePath());
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
