package net.shoreline.client.impl.event.gui.screen;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.shoreline.eventbus.event.Event;

public class SuggestChatEvent extends Event
{

    private CommandDispatcher dispatcher;
    private CommandSource source;
    private String prefix;

    public String getPrefix()
    {
        return prefix;
    }

    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }

    public CommandSource getSource()
    {
        return source;
    }

    public void setSource(CommandSource source)
    {
        this.source = source;
    }

    public void setDispatcher(CommandDispatcher dispatcher)
    {
        this.dispatcher = dispatcher;
    }

    public CommandDispatcher getDispatcher()
    {
        return dispatcher;
    }
}
