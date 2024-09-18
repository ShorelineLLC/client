package net.shoreline.client.api.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class BlockArgumentType implements ArgumentType<Block>
{
    public static BlockArgumentType block()
    {
        return new BlockArgumentType();
    }

    public static Block getBlock(final CommandContext<?> context, final String name)
    {
        return context.getArgument(name, Block.class);
    }

    @Override
    public Block parse(StringReader reader) throws CommandSyntaxException
    {
        String string = reader.readString();
        Block block = Registries.BLOCK.get(Identifier.of("minecraft", string));
        if (block == null)
        {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().createWithContext(reader, null);
        }
        return block;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
    {
        for (Block block : Registries.BLOCK)
        {
            builder.suggest(Registries.BLOCK.getId(block).getPath());
        }
        return builder.buildFuture();
    }
}
