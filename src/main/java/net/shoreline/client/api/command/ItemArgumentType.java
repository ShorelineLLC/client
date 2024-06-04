package net.shoreline.client.api.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class ItemArgumentType implements ArgumentType<Item>
{

    public static ItemArgumentType item()
    {
        return new ItemArgumentType();
    }

    public static Item getItem(final CommandContext<?> context, final String name)
    {
        return context.getArgument(name, Item.class);
    }

    @Override
    public Item parse(StringReader reader) throws CommandSyntaxException
    {
        String string = reader.readString();
        Item item = Registries.ITEM.get(new Identifier("minecraft", string));
        if (item == null)
        {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().createWithContext(reader, null);
        }
        return item;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
    {
        for (Item item : Registries.ITEM)
        {
            builder.suggest(Registries.ITEM.getId(item).getPath());
        }
        return builder.buildFuture();
    }
}
