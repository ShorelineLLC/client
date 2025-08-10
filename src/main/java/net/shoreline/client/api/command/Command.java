package net.shoreline.client.api.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.Getter;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.LoggingFeature;

@Getter
public abstract class Command extends LoggingFeature
{
    private final String description;
    protected final LiteralArgumentBuilder<CommandSource> argumentBuilder;

    public Command(String name, String description)
    {
        super(name);
        this.description = description;
        this.argumentBuilder = LiteralArgumentBuilder.literal(name);
    }

    public abstract void buildCommand();

    protected <T> RequiredArgumentBuilder<CommandSource, T> buildArgument(String name, ArgumentType<T> type)
    {
        return RequiredArgumentBuilder.argument(name, type);
    }

    protected SuggestionProvider<CommandSource> buildSuggestions(String... suggestions)
    {
        return (context, builder) -> CommandSource.suggestMatching(Lists.newArrayList(suggestions), builder);
    }
}
