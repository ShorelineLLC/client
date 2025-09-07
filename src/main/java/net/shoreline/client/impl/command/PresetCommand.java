package net.shoreline.client.impl.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.util.Formatting;
import net.shoreline.client.Shoreline;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.api.preset.ModulePreset;
import net.shoreline.client.impl.Managers;

import java.io.IOException;

public class PresetCommand extends Command
{
    public PresetCommand()
    {
        super("preset", "save/load presets");
    }

    @Override
    public void buildCommand()
    {
        argumentBuilder.then(buildArgument("save/load", StringArgumentType.string())
                .suggests(buildSuggestions("save", "load"))
                .then(buildArgument("preset", StringArgumentType.string())
                           .executes(context ->
                           {
                               String action = StringArgumentType.getString(context, "save/load");
                               String preset = StringArgumentType.getString(context, "preset");
                               try
                               {
                                   ModulePreset<Module> containerPreset = new ModulePreset<>(preset, Managers.MODULES.getModules());
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
                                   sendClientChatMessage(Formatting.RED + "Failed to load/save preset.");
                               }

                               return 0;
                           })));
    }
}
