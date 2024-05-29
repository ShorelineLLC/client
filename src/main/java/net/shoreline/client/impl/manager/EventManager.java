package net.shoreline.client.impl.manager;

import net.shoreline.client.impl.event.FinishLoadingEvent;
import net.shoreline.client.init.Fonts;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.bus.EventBus;

public class EventManager
{
    public EventManager()
    {
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onGameFinishedInit(FinishLoadingEvent event)
    {
        Fonts.init();
    }
}
