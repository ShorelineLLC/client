package net.shoreline.client.impl.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import net.shoreline.client.Shoreline;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.preset.AbstractPreset;
import net.shoreline.client.api.preset.ModulePreset;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.module.client.ThemeModule;

import java.io.IOException;

public class PresetCommand extends Command
{
    public PresetCommand()
    {
        super("preset", "save/load presets");
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> argumentBuilder)
    {
        argumentBuilder.then(buildArgument("modules/renders/combat", StringArgumentType.string())
                .suggests(buildSuggestions("modules", "renders", "combat"))
                .then(buildArgument("save/load", StringArgumentType.string())
                .suggests(buildSuggestions("save", "load"))
                .then(buildArgument("name", StringArgumentType.string())
                           .executes(context ->
                           {
                               String preset = StringArgumentType.getString(context, "modules/renders/combat");
                               String action = StringArgumentType.getString(context, "save/load");
                               String name   = StringArgumentType.getString(context, "name");

                               try
                               {
                                   AbstractPreset<?> containerPreset = parseString(preset, name);
                                   if (containerPreset == null)
                                   {
                                       sendErrorChatMessage("Failed to load/save preset.");
                                       return 0;
                                   }

                                   if (action.equalsIgnoreCase("save"))
                                   {
                                       containerPreset.saveFile();
                                       sendClientChatMessage("Successfully saved preset!");
                                       return 1;
                                   }

                                   if (action.equalsIgnoreCase("load"))
                                   {
                                       containerPreset.loadFile();
                                       sendClientChatMessage("Successfully loaded preset!");
                                       return 1;
                                   }
                               }
                               catch (IOException e)
                               {
                                   sendErrorChatMessage("Failed to load/save preset.");
                               }

                               return 0;
                           }))));
    }

    // Will fix when im not lazy
    private AbstractPreset<?> parseString(String string, String name)
            throws IOException
    {
        if (string.equalsIgnoreCase("modules"))
        {
            return new ModulePreset<>(name, Managers.MODULES.getModules());
        }
        else if (string.equalsIgnoreCase("renders"))
        {
            return new ModulePreset<>(name, Managers.MODULES.getModules(
                    module ->  module.getCategory() == GuiCategory.RENDER));
        }
        else if (string.equalsIgnoreCase("combat"))
        {
            return new ModulePreset<>(name, Managers.MODULES.getModules(
                    module -> module.getCategory() != GuiCategory.RENDER && !(module instanceof ThemeModule)));
        }

        return null;
    }
}
