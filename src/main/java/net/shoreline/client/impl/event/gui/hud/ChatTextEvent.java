package net.shoreline.client.impl.event.gui.hud;

import net.minecraft.text.OrderedText;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class ChatTextEvent extends Event
{

    private OrderedText text;

    public ChatTextEvent(OrderedText text)
    {
        this.text = text;
    }

    public void setText(OrderedText text)
    {
        this.text = text;
    }

    public OrderedText getText()
    {
        return text;
    }
}
