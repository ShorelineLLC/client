package net.shoreline.client.api.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import lombok.Getter;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.LoggingFeature;

import java.util.HashSet;
import java.util.Set;

@Getter
public abstract class Command extends LoggingFeature
{
    private final String description;
    private final Set<LiteralArgumentBuilder<CommandSource>> argumentBuilders = new HashSet<>();

    public Command(String name, String description)
    {
        super(name);
        this.description = description;
        this.argumentBuilders.add(LiteralArgumentBuilder.literal(name));
    }

    public Command(String name, String[] aliases, String description)
    {
        super(name, aliases);
        this.description = description;
        this.argumentBuilders.add(LiteralArgumentBuilder.literal(name));
        for (String alias : aliases)
        {
            argumentBuilders.add(LiteralArgumentBuilder.literal(alias));
        }
    }

    public abstract void buildCommand(LiteralArgumentBuilder<CommandSource> argumentBuilder);

    protected <T> RequiredArgumentBuilder<CommandSource, T> buildArgument(String name, ArgumentType<T> type)
    {
        return RequiredArgumentBuilder.argument(name, type);
    }

    protected SuggestionProvider<CommandSource> buildSuggestions(String... suggestions)
    {
        return (context, builder) -> CommandSource.suggestMatching(Lists.newArrayList(suggestions), builder);
    }
}
