package net.shoreline.client.impl.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.api.command.argtype.ConfigArgumentType;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.impl.command.util.ConfigParser;
import net.shoreline.client.impl.render.ClientFormatting;

public class ModuleCommand extends Command
{
    private final Module module;

    public ModuleCommand(Module module)
    {
        super(module.getName().toLowerCase(), module.getAliases(), module.getDescription());
        this.module = module;
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> argumentBuilder)
    {
        argumentBuilder.then(buildArgument("config", ConfigArgumentType.config(module))
            .then(buildArgument("value", StringArgumentType.greedyString())
                .suggests((context, builder) ->
                {
                    Config<?> config = ConfigArgumentType.getConfig(context, "config");
                    return ConfigParser.getSuggestions(builder, config);
                })
                .executes(context ->
                {
                    Config<?> config = ConfigArgumentType.getConfig(context, "config");
                    String value     = StringArgumentType.getString(context, "value");

                    if (config != null)
                    {
                        try
                        {
                            if (value.equalsIgnoreCase("reset"))
                            {
                                config.reset();
                            }
                            else if (!ConfigParser.parseString(config, value))
                            {
                                sendErrorChatMessage("Failed to set value! " + config.getName() + " : " + value);
                                return 0;
                            }

                            String finalValue = config instanceof BooleanConfig ? config.getValue().toString() : value;
                            sendClientChatMessage(module.getName()
                                                          + "-"
                                                          + config.getName()
                                                          + ": "
                                                          + ClientFormatting.THEME
                                                          + finalValue);
                            return 1;
                        }
                        catch (Exception e)
                        {
                            sendErrorChatMessage("Failed to set value! " + config.getName() + " : " + value);
                            e.printStackTrace();
                            return 0;
                        }
                    }

                    sendErrorChatMessage("Failed to find config");
                    return 0;
                }))
        );
    }
}
