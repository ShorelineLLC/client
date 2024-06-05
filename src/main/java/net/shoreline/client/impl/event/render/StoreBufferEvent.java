package net.shoreline.client.impl.event.render;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.shoreline.eventbus.event.Event;

public class StoreBufferEvent extends Event
{

    private final Object2ObjectLinkedOpenHashMap map;

    public StoreBufferEvent(Object2ObjectLinkedOpenHashMap map)
    {
        this.map = map;
    }

    public Object2ObjectLinkedOpenHashMap getMap()
    {
        return map;
    }
}
