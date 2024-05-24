package net.shoreline.client.impl.event.network;

import net.shoreline.eventbus.Cancelable;
import net.shoreline.eventbus.Event;
import net.shoreline.client.impl.manager.client.cape.CapeType;

@Cancelable
public class CapesEvent extends Event {

    private CapeType capeType;

    public void setCapeType(CapeType capeType) {
        this.capeType = capeType;
    }

    public CapeType getCapeType() {
        return capeType;
    }
}
