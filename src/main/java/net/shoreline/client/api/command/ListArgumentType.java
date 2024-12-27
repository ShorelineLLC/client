package net.shoreline.client.api.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BlockListConfig;
import net.shoreline.client.api.config.setting.EntityListConfig;
import net.shoreline.client.api.config.setting.ItemListConfig;

import java.util.concurrent.CompletableFuture;

public class ListArgumentType implements ArgumentType<Object>
{

    public static ListArgumentType list()
    {
        return new ListArgumentType();
    }

    public static Object getListItem(final CommandContext<?> context, final String name)
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
        EntityType<?> entityType = Registries.ENTITY_TYPE.get(Identifier.of("minecraft", string));
        if (string.equalsIgnoreCase("pig") || entityType != EntityType.PIG)
        {
            return entityType;
        }
        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().createWithContext(reader, null);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
    {
        Config<?> config = ConfigArgumentType.getConfig(context, "setting");
        String value = StringArgumentType.getString(context, "value");

        if (!value.equalsIgnoreCase("add") && !value.equalsIgnoreCase("remove") && !value.equalsIgnoreCase("del"))
        {
            return builder.buildFuture();
        }

        if (config instanceof ItemListConfig<?>)
        {
            for (Item item : Registries.ITEM)
            {
                builder.suggest(Registries.ITEM.getId(item).getPath());
            }
        }

        if (config instanceof BlockListConfig<?>)
        {
            for (Block block : Registries.BLOCK)
            {
                builder.suggest(Registries.BLOCK.getId(block).getPath());
            }
        }

        if (config instanceof EntityListConfig<?>)
        {
            for (EntityType<?> entityType : Registries.ENTITY_TYPE)
            {
                builder.suggest(Registries.ENTITY_TYPE.getId(entityType).getPath());
            }
        }

        return builder.buildFuture();
    }
}
