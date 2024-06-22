package net.shoreline.loader.impl.natives;

import java.io.File;

public final class NativeLoader
{
    public static void load() throws Throwable
    {
//        OSType type = getOS();
//        URL url = new URL("https://api.shorelineclient.net/natives");
//
//        URLConnection urlConnection = url.openConnection();
//        urlConnection.addRequestProperty("User-Agent", "shoreline-client");
//        urlConnection.addRequestProperty("Library-Type", type.getExt());
//
//        DataInputStream nativesInputStream = new DataInputStream(urlConnection.getInputStream());
//        byte[] buffer = new byte[urlConnection.getContentLength()];
//        for (int i = 0; i < buffer.length; i++)
//        {
//            buffer[i] = nativesInputStream.readByte();
//        }
//
//        File natives = Files.createTempFile(
//                null,
//                "." + type.getExt()
//        ).toFile();
//
//        natives.deleteOnExit();
//
//        FileOutputStream fos = new FileOutputStream(natives);
//        fos.write(buffer);
//        fos.flush();
//        fos.close();

        File natives = new File("C:\\Users\\user2\\Desktop\\shoreline\\src\\main\\rust\\target\\debug\\shoreline_loader.dll");

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
