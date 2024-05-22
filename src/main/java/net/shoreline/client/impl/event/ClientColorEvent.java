package net.shoreline.client.impl.event;

import net.shoreline.client.api.event.Event;
import net.shoreline.client.impl.module.client.ColorsModule;

public class ClientColorEvent extends Event {

    private int rgb;

    public void setRgb(int rgb) {
        this.rgb = rgb;
    }

    public int getClientRgb()
    {
        return rgb;
    }
}
