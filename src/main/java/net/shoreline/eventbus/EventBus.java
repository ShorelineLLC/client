package net.shoreline.eventbus;

import net.shoreline.eventbus.event.Event;

import java.util.Map;

public final class EventBus
{
    public static final EventBus INSTANCE = new EventBus();

    /**
     * A Map<Class<Event>, Invoker> where the keys are the linked list for that event type.
     *
     * So the list may look like...
     *
     * <PacketEvent:Invoker>,
     * <RenderEvent:Invoker>,
     * <JoinGameEvent:Invoker>
     *
     * This way, instead of a single linked list that we iterate down each time an event is posted,
     * we query the map to get the linked list associated with a certain event and invoke ALL the events
     * on that chain of invokers, without checking if the methodType matches the eventType.
     *
     * If we are in a development environment, use reflection to gather every Event class instance.
     * then put them in the map with a null invoker (stop_decompiling_1(null, null, null, null)).
     * (@see DevEventBusLoader)
     *
     * If we are loading the client dynamically, each time the native class loader encounters a class that
     * extends Event, it puts it on this map with a null invoker.
     *
     * So essentially there is no computeIfAbsent for this list, it is always filled when the DLL is loaded.
     */
    private Map<Class<Event>, InvokerNode> event2InvokerMap;


    private EventBus()
    {
    }

    /**
     * Iterate through the linked list for the event and invoke any entries matching the event type
     */
    public void dispatch(Event event)
    {
        InvokerNode head = this.event2InvokerMap.get(event.getClass());
        InvokerNode current = head.next;

        while (current != null)
        {
            current.invoker.invoke(event);
            current = current.next;
        }
    }

    public native Object subscribe(Object subscriber);

    public native Object unsubscribe(Object subscriber);

    @SuppressWarnings({"unused", "FieldCanBeLocal"}) // Used natively
    public final static class InvokerNode
    {
        private final InvokerNode next;
        private final Invoker invoker;
        private final Object subscriber;
        private final Integer priority; // not int, for objectfuscation in bonfuscator

        private InvokerNode(Object invoker,
                            Object subscriber,
                            Object priority)
        {
            this.next = null;
            this.invoker = (Invoker) invoker;
            this.subscriber = subscriber;
            this.priority = (Integer) priority;
        }
    }

    @FunctionalInterface
    public interface Invoker
    {
        void invoke(Object event);
    }
}
