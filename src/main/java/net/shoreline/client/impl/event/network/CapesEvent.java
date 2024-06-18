package net.shoreline.client.impl.event.network;

import net.minecraft.util.Identifier;
import net.shoreline.client.impl.manager.client.cape.CapeType;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class CapesEvent extends Event
{
    private Identifier texture;

    public void setTexture(Identifier texture)
    {
        this.texture = texture;
    }

    public Identifier getTexture()
    {
        return texture;
    }
}
