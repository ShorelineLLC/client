package net.shoreline.loader.impl.stage.authentication;

import net.shoreline.loader.Loader;
import net.shoreline.loader.Natives;
import net.shoreline.loader.context.UserContext;
import net.shoreline.loader.impl.stage.LoadingStage;
import net.shoreline.loader.impl.stage.antidump.AntiDumpStage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class AuthenticationStage extends LoadingStage
{
    private static final AuthenticationStage instance = new AuthenticationStage();

    @Override
    public void run() throws Throwable
    {
        Loader.LOGGER.info("Locating user credentials...");

        String res = (String) Natives.stop_decompiling_5(this);

        String[] user = res.split(":");

        Loader.getContext()
                .setHwid(user[0])
                .setUsername(user[1])
                .setUid(user[2])
                .setRunningMods(collectMods());


        Loader.LOGGER.info("Welcome, {}!", Loader.getContext().username());
    }

    @Override
    public void error(UserContext context,
                      Throwable throwable)
    {
        throw new RuntimeException(
                "Please report this to the Shoreline development team!",
                throwable
        );
    }

    @Override
    public LoadingStage next()
    {
        return AntiDumpStage.getInstance();
    }

    private List<String> collectMods()
    {
        List<String> modList = new ArrayList<>();
        String minecraftPath = getMinecraftModsPath();

        if (minecraftPath != null)
        {
            try
            {
                Files.walk(Paths.get(minecraftPath), 1)
                        .filter(it -> !it.toFile().isDirectory() && it.toFile().getName().endsWith(".jar"))
                        .forEach(jar -> modList.add(jar.toFile().getName()));
            } catch (IOException ignored) // it didn't work, whatever
            {
            }
        }

        return modList;
    }

    private static String getMinecraftModsPath()
    {
        String os = System.getProperty("os.name").toLowerCase();
        String minecraftPath = null;

        if (os.contains("win"))
        {
            minecraftPath = System.getenv("APPDATA") + File.separator + ".minecraft" + File.separator + "mods";
        } else if (os.contains("mac"))
        {
            minecraftPath = System.getProperty("user.home") + File.separator + "Library" + File.separator + "Application Support" + File.separator + "minecraft" + File.separator + "mods";
        } else if (os.contains("nix") || os.contains("nux"))
        {
            minecraftPath = System.getProperty("user.home") + File.separator + ".minecraft" + File.separator + "mods";
        }

        return minecraftPath;
    }

    public static AuthenticationStage getInstance()
    {
        return instance;
    }
}
