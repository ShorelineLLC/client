package net.shoreline.client.impl.event.gui.hud;

import net.minecraft.client.gui.hud.ChatHudLine;
import net.shoreline.eventbus.event.Event;

public class ChatLineEvent extends Event
{

    private final ChatHudLine chatHudLine;
    private final double width;

    public ChatLineEvent(ChatHudLine chatHudLine, double width)
    {
        this.chatHudLine = chatHudLine;
        this.width = width;
    }

    public ChatHudLine getChatHudLine()
    {
        return chatHudLine;
    }

    public double getWidth()
    {
        return width;
    }
}
