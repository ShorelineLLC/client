package net.shoreline.eventbus;

import lombok.Getter;
import lombok.Setter;
import net.shoreline.eventbus.annotation.Cancelable;

@Getter
public class Event
{
    private final boolean cancelable =
            getClass().isAnnotationPresent(Cancelable.class);

    private boolean canceled;
    private boolean receiveCanceled;

    public void setCanceled(boolean cancel)
    {
        if (isCancelable())
        {
            canceled = cancel;
            return;
        }
        throw new IllegalStateException("Cannot set event canceled");
    }

    public void receiveCanceled()
    {
        receiveCanceled = true;
    }

    public void cancel()
    {
        setCanceled(true);
    }
}
