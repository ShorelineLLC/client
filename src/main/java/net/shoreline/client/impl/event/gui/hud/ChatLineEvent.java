package net.shoreline.client.impl.event.gui.hud;

import net.minecraft.client.gui.hud.ChatHudLine;
import net.shoreline.eventbus.event.Event;

public class ChatLineEvent extends Event
{

    private final ChatHudLine.Visible chatHudLine;
    private final double width;

    public ChatLineEvent(ChatHudLine.Visible chatHudLine, double width)
    {
        this.chatHudLine = chatHudLine;
        this.width = width;
    }

    public ChatHudLine.Visible getChatHudLine()
    {
        return chatHudLine;
    }

    public double getWidth()
    {
        return width;
    }
}
