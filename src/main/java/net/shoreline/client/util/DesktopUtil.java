package net.shoreline.client.util;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class DesktopUtil
{
    public static boolean open(File file)
    {
        if (openSystemSpecific(file.getPath()))
        {
            return true;
        }

        return openDesktop(file);
    }

    private static boolean openSystemSpecific(String what)
    {
        EnumOS os = getOs();
        if (os.isLinux())
        {
            if (runCommand("kde-open", "%s", what))
            {
                return true;
            }
            if (runCommand("gnome-open", "%s", what))
            {
                return true;
            }
            if (runCommand("xdg-open", "%s", what))
            {
                return true;
            }
        }

        if (os.isMac())
        {
            if (runCommand("open", "%s", what))
            {
                return true;
            }
        }

        if (os.isWindows())
        {
            return runCommand("explorer", "%s", what);
        }

        return false;
    }

    private static boolean openDesktop(File file)
    {
        try
        {
            if (!Desktop.isDesktopSupported())
            {
                return false;
            }

            if (!Desktop.getDesktop().isSupported(Desktop.Action.OPEN))
            {
                return false;
            }

            Desktop.getDesktop().open(file);
            return true;
        }
        catch (Throwable t)
        {
            return false;
        }
    }

    private static boolean runCommand(String command, String args, String file)
    {
        String[] parts = prepareCommand(command, args, file);
        try
        {
            Process p = Runtime.getRuntime().exec(parts);
            if (p == null)
            {
                return false;
            }
            try
            {
                int retval = p.exitValue();
                if (retval == 0)
                {
                    return false;
                }
                else
                {
                    return false;
                }
            }
            catch (IllegalThreadStateException itse)
            {
                return true;
            }
        }
        catch (IOException e)
        {
            return false;
        }
    }

    private static String[] prepareCommand(String command, String args, String file)
    {
        List<String> parts = new ArrayList<String>();
        parts.add(command);

        if (args != null)
        {
            for (String s : args.split(" "))
            {
                s = String.format(s, file); // put in the filename thing
                parts.add(s.trim());
            }
        }

        return parts.toArray(new String[parts.size()]);
    }

    public enum EnumOS
    {
        WINDOWS, LINUX, MAC_OS, LINUX_SOLARIS, OTHER;

        public boolean isLinux()
        {
            return this == LINUX || this == LINUX_SOLARIS;
        }

        public boolean isMac()
        {
            return this == MAC_OS;
        }

        public boolean isWindows()
        {
            return this == WINDOWS;
        }
    }

    public static EnumOS getOs()
    {
        String s = System.getProperty("os.name").toLowerCase();
        if (s.contains("win"))
        {
            return EnumOS.WINDOWS;
        }
        if (s.contains("mac"))
        {
            return EnumOS.MAC_OS;
        }
        if (s.contains("solaris"))
        {
            return EnumOS.LINUX_SOLARIS;
        }
        if (s.contains("sunos"))
        {
            return EnumOS.LINUX_SOLARIS;
        }
        if (s.contains("linux"))
        {
            return EnumOS.LINUX;
        }
        if (s.contains("unix"))
        {
            return EnumOS.LINUX;
        }
        else
        {
            return EnumOS.OTHER;
        }
    }
}