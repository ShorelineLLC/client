package net.shoreline.client.impl.module.render;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class NoBobModule extends Toggleable
{
    public NoBobModule()
    {
        super("NoBob", "Prevents the bobbing animation", GuiCategory.RENDER);
    }

    @EventListener
    public void onTick(TickEvent.Post event)
    {
        if (!checkNull())
        {
            mc.player.distanceMoved = 4.0f;
        }
    }
}
