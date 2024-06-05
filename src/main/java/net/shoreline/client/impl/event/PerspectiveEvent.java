package net.shoreline.client.impl.event;

import net.minecraft.client.render.Camera;
import net.shoreline.eventbus.annotation.Cancelable;
import net.shoreline.eventbus.event.Event;

@Cancelable
public class PerspectiveEvent extends Event
{

    public Camera camera;

    public PerspectiveEvent(Camera camera)
    {
        this.camera = camera;
    }

    public Camera getCamera()
    {
        return camera;
    }

}
