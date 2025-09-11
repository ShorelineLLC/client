package net.shoreline.client.api.command.argtype;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.impl.Managers;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class ModuleArgumentType implements ArgumentType<Module>
{
    public static ModuleArgumentType module()
    {
        return new ModuleArgumentType();
    }

    public static Module getModule(final CommandContext<?> context, final String name)
    {
        return context.getArgument(name, Module.class);
    }

    @Override
    public Module parse(StringReader reader) throws CommandSyntaxException
    {
        String string = reader.readString();
        String id = String.format(Module.ID_FORMAT, string.toLowerCase());
        Module module = Managers.MODULES.getModule(id);
        if (module == null)
        {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().createWithContext(reader, null);
        }

        return module;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context,
                                                              final SuggestionsBuilder builder)
    {
        return CommandSource.suggestMatching(Managers.MODULES.getModuleNames(), builder);
    }

    @Override
    public Collection<String> getExamples()
    {
        return Managers.MODULES.getModuleNames().stream().limit(10).toList();
    }
}