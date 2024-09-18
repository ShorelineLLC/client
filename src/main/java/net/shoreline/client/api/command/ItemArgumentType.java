package net.shoreline.client.api.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class ItemArgumentType implements ArgumentType<Object>
{

    public static ItemArgumentType item()
    {
        return new ItemArgumentType();
    }

    public static Object getItem(final CommandContext<?> context, final String name)
    {
        return context.getArgument(name, Object.class);
    }

    @Override
    public Object parse(StringReader reader) throws CommandSyntaxException
    {
        String string = reader.readString();
        Item item = Registries.ITEM.get(Identifier.of("minecraft", string));
        if (item != Items.AIR)
        {
            return item;
        }
        Block block = Registries.BLOCK.get(Identifier.of("minecraft", string));
        if (block != Blocks.AIR)
        {
            return block;
        }
        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().createWithContext(reader, null);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
    {
        for (Item item : Registries.ITEM)
        {
            builder.suggest(Registries.ITEM.getId(item).getPath());
        }
        for (Block block : Registries.BLOCK)
        {
            builder.suggest(Registries.BLOCK.getId(block).getPath());
        }
        return builder.buildFuture();
    }
}
