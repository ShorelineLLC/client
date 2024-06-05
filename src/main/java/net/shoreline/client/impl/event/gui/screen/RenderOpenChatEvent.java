package net.shoreline.client.impl.event.gui.screen;

import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class RenderOpenChatEvent extends Event
{

    private float animation;

    public void setAnimation(float animation)
    {
        this.animation = animation;
    }

    public float getAnimation()
    {
        return animation;
    }
}
