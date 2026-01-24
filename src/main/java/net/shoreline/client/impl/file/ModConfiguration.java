package net.shoreline.client.impl.file;

import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.shoreline.client.Shoreline;
import net.shoreline.client.api.config.ColorConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.file.ConfigContainerFile;
import net.shoreline.client.api.file.JsonConfigFile;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.module.client.ThemeModule;
import net.shoreline.loader.Loader;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Getter
public class ModConfiguration
{
    private Path saveDirectory;
    private Path configsDirectory;
    private Path modulesDirectory;

    private final List<JsonConfigFile> saveFiles = new LinkedList<>();

    public ModConfiguration()
    {
        final Path runningDir = MinecraftClient.getInstance().runDirectory.toPath();

        try
        {
            File homeDir = new File(System.getProperty("user.home"));
            saveDirectory = homeDir.toPath();
        } catch (Exception e)
        {
            Loader.error("Could not access home directory!");
            e.printStackTrace();
            saveDirectory = runningDir;
        } finally
        {
            if (saveDirectory != null && Files.isWritable(saveDirectory))
            {
                saveDirectory = saveDirectory.resolve("Shoreline");
            } else
            {
                saveDirectory = runningDir.resolve("Shoreline");
            }

            modulesDirectory = saveDirectory.resolve("Modules");
            configsDirectory = saveDirectory.resolve("Configs");
            try
            {
                if (!Files.exists(saveDirectory))
                {
                    Files.createDirectory(saveDirectory);
                }
                if (!Files.exists(modulesDirectory))
                {
                    Files.createDirectory(modulesDirectory);
                }
                if (!Files.exists(configsDirectory))
                {
                    Files.createDirectories(configsDirectory);
                }
            } catch (IOException e)
            {
                Loader.info("Failed to create save directories!");
                e.printStackTrace();
            }
        }

        try
        {
            registerFiles(
                    new KeybindFile(saveDirectory),
                    new PvpKitFile(saveDirectory),
                    new ToggleStateFile(saveDirectory),
                    new SocialsFile(saveDirectory),
                    new MacroFile(saveDirectory)
            );

            for (Module module : Managers.MODULES.getAllModules())
            {
                registerFile(new ConfigContainerFile(modulesDirectory, module));
            }
        } catch (IOException e)
        {
            Loader.info("Failed to create mod config files!");
            e.printStackTrace();
        }
    }

    public void saveModConfiguration()
    {
        for (JsonConfigFile file : saveFiles)
        {
            try
            {
                file.saveFile();
            } catch (IOException e)
            {
                Loader.info("Failed to save configuration for " + file.getFilepath().toString());
                e.printStackTrace();
            }
        }
    }

    public void loadModConfiguration()
    {
        for (JsonConfigFile file : saveFiles)
        {
            try
            {
                file.loadFile();
            } catch (IOException e)
            {
                Loader.info("Failed to load configuration for " + file.getFilepath().toString());
                e.printStackTrace();
            }
        }
    }

    public void registerFile(JsonConfigFile file)
    {
        saveFiles.add(file);
    }

    public void registerFiles(JsonConfigFile... files)
    {
        Arrays.stream(files).forEach(this::registerFile);
    }

    public void unregisterFile(JsonConfigFile file)
    {
        saveFiles.remove(file);
    }
}
