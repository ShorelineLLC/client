package net.shoreline.eventbus.event;

import net.shoreline.eventbus.annotation.Cancelable;

public class Event
{
    private final boolean cancelable =
            getClass().isAnnotationPresent(Cancelable.class);

    private boolean canceled;

    public boolean isCancelable()
    {
        return cancelable;
    }

    public boolean isCanceled()
    {
        return canceled;
    }

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
