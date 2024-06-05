package net.shoreline.client.impl.event.network;

import net.shoreline.client.impl.manager.client.cape.CapeType;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class CapesEvent extends Event
{

    private CapeType capeType;

    public void setCapeType(CapeType capeType)
    {
        this.capeType = capeType;
    }

    public CapeType getCapeType()
    {
        return capeType;
    }
}
