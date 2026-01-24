package net.shoreline.client.api.command.argtype;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.ConfigGroup;
import net.shoreline.client.api.module.Module;

import java.util.concurrent.CompletableFuture;

public class ConfigArgumentType implements ArgumentType<Config<?>>
{
    private final Module module;

    /** Use factory method. */
    private ConfigArgumentType(Module module)
    {
        this.module = module;
    }

    public static ConfigArgumentType config(Module module)
    {
        return new ConfigArgumentType(module);
    }

    public static Config<?> getConfig(CommandContext<?> context, String name)
    {
        return context.getArgument(name, Config.class);
    }

    @Override
    public Config<?> parse(StringReader reader) throws CommandSyntaxException
    {
        String name = reader.readString();
        Config<?> result = null;
        for (Config<?> config : module.getConfigs())
        {
            if (config instanceof ConfigGroup)
            {
                continue;
            }

            if (config.getName().equalsIgnoreCase(name))
            {
                result = config;
                break;
            }
        }

        if (result == null)
        {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().createWithContext(reader, null);
        }

        return result;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context,
                                                              SuggestionsBuilder builder)
    {
        for (Config<?> config : module.getConfigs())
        {
            if (config instanceof ConfigGroup)
            {
                continue;
            }

            builder.suggest(config.getName().toLowerCase());
        }

        return builder.buildFuture();
    }
}
