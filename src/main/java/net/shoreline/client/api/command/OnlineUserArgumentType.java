package net.shoreline.client.api.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.impl.irc.IRCManager;
import net.shoreline.client.impl.irc.user.OnlineUser;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public final class OnlineUserArgumentType implements ArgumentType<OnlineUser>
{
    public static OnlineUserArgumentType user()
    {
        return new OnlineUserArgumentType();
    }

    @Override
    public OnlineUser parse(StringReader reader) throws CommandSyntaxException
    {
        String username = reader.readString();

        OnlineUser user = IRCManager.getInstance().findGlobalOnlineUser(username);

        if (user == null)
        {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().createWithContext(reader, null);
        }

        return user;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context,
                                                              final SuggestionsBuilder builder)
    {
        return CommandSource.suggestMatching(IRCManager.getInstance().getAllOnlineUsers().stream().map(OnlineUser::getName), builder);
    }

    @Override
    public Collection<String> getExamples()
    {
        return IRCManager.getInstance().getAllOnlineUsers().stream().map(OnlineUser::getName).limit(10).toList();
    }
}
