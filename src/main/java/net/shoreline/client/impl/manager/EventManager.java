package net.shoreline.client.impl.manager;

import net.shoreline.client.impl.event.FinishLoadingEvent;
import net.shoreline.client.impl.event.render.RenderWorldEvent;
import net.shoreline.client.init.Fonts;
import net.shoreline.client.init.Managers;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

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

    @EventListener
    public void onReloadShader(RenderWorldEvent.Hand event)
    {
        Managers.SHADER.reloadShaders();
    }
}
