package net.shoreline.eventbus.event;

import lombok.Getter;
import net.shoreline.eventbus.annotation.Cancelable;

@Getter
public class Event
{
    private final boolean cancelable =
            getClass().isAnnotationPresent(Cancelable.class);

    private boolean canceled;

    public void setCanceled(boolean cancel)
    {
        if (isCancelable())
        {
            canceled = cancel;
            return;
        }
        throw new IllegalStateException("Cannot set event canceled");
    }

    public void cancel()
    {
        setCanceled(true);
    }
}
