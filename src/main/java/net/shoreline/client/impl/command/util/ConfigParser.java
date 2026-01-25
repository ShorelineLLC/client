package net.shoreline.client.impl.command.util;

import com.google.common.collect.Lists;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.config.*;
import net.shoreline.client.impl.command.util.impl.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ConfigParser
{
    private static final Map<Class<? extends Config>,
            IConfigParser<?, ?>> PARSERS = new HashMap<>();
    private static final Map<Class<? extends Config>,
            Function<Config<?>, Collection<String>>> SUGGESTIONS = new HashMap<>();

    static
    {
        PARSERS.put(BooleanConfig.class,         new BooleanConfigParser());
        PARSERS.put(ToggleableConfigGroup.class, new BooleanConfigParser());
        PARSERS.put(ColorConfig.class,           new ColorConfigParser());
        PARSERS.put(NumberConfig.class,          new NumberConfigParser<>());
        PARSERS.put(EnumConfig.class,            new EnumConfigParser<>());
        PARSERS.put(StringConfig.class,          new StringConfigParser());
        PARSERS.put(MacroConfig.class,           new MacroConfigParser());
        PARSERS.put(RegistryConfig.class,        new RegistryConfigParser<>());
        registerSuggestions();
    }

    @SuppressWarnings("unchecked")
    public static <T> boolean parseString(Config<T> config, String string)
    {
        IConfigParser<T, Config<T>> parser =
                (IConfigParser<T, Config<T>>) PARSERS.get(config.getClass());

        if (parser != null)
        {
            return parser.parseString(config, string);
        }

        return false;
    }

    public static CompletableFuture<Suggestions> getSuggestions(SuggestionsBuilder builder, Config<?> config)
    {
        Function<Config<?>, Collection<String>> provider = SUGGESTIONS.get(config.getClass());
        if (provider == null)
        {
            return CommandSource.suggestMatching(Lists.newArrayList(), builder);
        }

        List<String> result = new ArrayList<>(provider.apply(config));
        return CommandSource.suggestMatching(result, builder);
    }

    private static void registerSuggestions()
    {
        registerSuggestion(BooleanConfig.class, "true", "false", "toggle");
        registerSuggestion(ToggleableConfigGroup.class, "true", "false", "toggle");
        registerSuggestion(RegistryConfig.class, "add", "remove", "clear");
        registerSuggestion(EnumConfig.class, config ->
                Arrays.stream(((EnumConfig) config).getValues()).map(Enum::name).toList());
    }

    public static void registerSuggestion(Class<? extends Config> clazz,
                                          Function<Config<?>, Collection<String>> suggestionFunction)
    {
        SUGGESTIONS.putIfAbsent(clazz, suggestionFunction);
    }

    public static void registerSuggestion(Class<? extends Config> clazz,
                                          String...suggestions)
    {
        registerSuggestion(clazz, config ->  Arrays.stream(suggestions).toList());
    }
}