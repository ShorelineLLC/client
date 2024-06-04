package net.shoreline.client.impl.event;

import net.shoreline.eventbus.Event;

public class ClientColorEvent extends Event
{

    private int rgb;

    public void setRgb(int rgb)
    {
        this.rgb = rgb;
    }

    public int getClientRgb()
    {
        return rgb;
    }
}
