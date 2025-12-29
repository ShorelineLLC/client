package net.shoreline.client.api;

import net.shoreline.eventbus.Event;
import net.shoreline.eventbus.EventBus;

public class ListeningFeature extends GenericFeature
{
    public ListeningFeature(String name)
    {
        super(name);
    }

    public ListeningFeature(String name, String[] nameAliases)
    {
        super(name, nameAliases);
    }

    protected void runOnThread(Runnable runnable)
    {
        if (mc.isOnThread())
        {
            runnable.run();
        } else
        {
            mc.execute(runnable);
        }
    }

    protected <E extends Event> void addListener(Class<E> eventType, EventBus.Invoker<? super E> invoker)
    {
        EventBus.INSTANCE.addListener(eventType, invoker);
    }

    protected <E extends Event> void addListener(Class<E> eventType, EventBus.Invoker<? super E> invoker, int priority)
    {
        EventBus.INSTANCE.addListener(eventType, invoker, priority);
    }
}
