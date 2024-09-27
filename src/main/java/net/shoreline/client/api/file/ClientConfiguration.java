package net.shoreline.client.api.file;

import net.shoreline.client.Shoreline;
import net.shoreline.client.api.font.FontFile;
import net.shoreline.client.api.macro.MacroFile;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.api.module.file.ModuleConfigFile;
import net.shoreline.client.api.module.file.ModuleFile;
import net.shoreline.client.api.social.SocialFile;
import net.shoreline.client.api.social.SocialRelation;
import net.shoreline.client.api.waypoint.WaypointFile;
import net.shoreline.client.impl.gui.click.ClickGuiFile;
import net.shoreline.client.impl.module.combat.AutoRegearModule;
import net.shoreline.client.impl.module.misc.InvCleanerModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.Globals;
import net.shoreline.client.util.chat.ChatUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * @author linus
 * @see ConfigFile
 * @since 1.0
 */
public class ClientConfiguration implements Globals
{
    // Set of configuration files that must be saved and loaded. This can be
    // modified after init.
    private final Set<ConfigFile> files = new HashSet<>();
    // Main client directory. This folder will contain all locally saved
    // configurations for the client.
    private Path clientDir;
    //
    private ModuleConfigFile file;
    private final ClickGuiFile clickGuiFile;
    private final FontFile fontFile;

    /**
     *
     */
    public ClientConfiguration()
    {
        final Path runningDir = mc.runDirectory.toPath();
        try
        {
            File homeDir = new File(System.getProperty("user.home"));
            clientDir = homeDir.toPath();
        }
        // will resort to running dir if client does not have access to the 
        // user home dir
        catch (Exception e)
        {
            Shoreline.error("Could not access home dir, defaulting to running dir");
            e.printStackTrace();
            clientDir = runningDir;
        }
        finally
        {
            // cannot write, minecraft always has access to the running dir
            if (clientDir == null || !Files.exists(clientDir)
                    || !Files.isWritable(clientDir))
            {
                clientDir = runningDir;
            }
            clientDir = clientDir.resolve("Shoreline");
            // create client directory
            if (!Files.exists(clientDir))
            {
                try
                {
                    Files.createDirectory(clientDir);
                }
                // write error
                catch (IOException e)
                {
                    Shoreline.error("Could not create client dir");
                    e.printStackTrace();
                }
            }
            Path configDir = clientDir.resolve("Configs");
            if (!Files.exists(configDir))
            {
                try
                {
                    Files.createDirectory(configDir);
                }
                // write error
                catch (IOException e)
                {
                    Shoreline.error("Could not create config dir");
                    e.printStackTrace();
                }
            }
        }
        files.add(new MacroFile(clientDir));
        for (Module module : Managers.MODULE.getModules())
        {
            // files.add(new ModulePreset(clientDir.resolve("Defaults"), module));
            files.add(new ModuleFile(clientDir.resolve("Modules"), module));
        }
        files.add(InvCleanerModule.getInstance().getBlacklistFile(clientDir));
        files.add(new WaypointFile(clientDir));
        for (SocialRelation relation : SocialRelation.values())
        {
            files.add(new SocialFile(clientDir, relation));
        }
        this.clickGuiFile = new ClickGuiFile(clientDir);
        this.fontFile = new FontFile(clientDir);
    }

    /**
     *
     */
    public void saveClient()
    {
        for (ConfigFile file : files)
        {
            file.save();
        }
    }

    public void saveClientModules()
    {
        for (ConfigFile file : files)
        {
            if (file instanceof ModuleFile)
            {
                file.save();
            }
        }
    }

    /**
     *
     */
    public void loadClient()
    {
        for (ConfigFile file : files)
        {
            file.load();
        }
    }

    public void loadClientModules()
    {
        for (ConfigFile file : files)
        {
            if (file instanceof ModuleFile)
            {
                file.load();
            }
        }
    }

    public void saveModuleConfiguration(String configFile)
    {
        file = new ModuleConfigFile(clientDir.resolve("Configs"), configFile);
        file.save();
    }

    public boolean loadModuleConfiguration(String configFile)
    {
        Path configDir = clientDir.resolve("Configs");
        file = new ModuleConfigFile(configDir, configFile);
        if (!Files.exists(configDir.resolve(configFile + ".json")))
        {
            ChatUtil.error("Could not find config file: " + configFile);
            return false;
        }
        file.load();
        return true;
    }

    public void saveClickGui()
    {
        clickGuiFile.save();
    }

    public void loadClickGui()
    {
        clickGuiFile.load();
    }

    public void saveFonts()
    {
        fontFile.save();
    }

    public void loadFonts()
    {
        fontFile.load();
    }

    public Set<ConfigFile> getFiles()
    {
        return files;
    }

    public void addFile(final ConfigFile configFile)
    {
        files.add(configFile);
    }

    public void removeFile(final ConfigFile configFile)
    {
        files.remove(configFile);
    }

    /**
     * @return
     */
    public Path getClientDirectory()
    {
        return clientDir;
    }
}
