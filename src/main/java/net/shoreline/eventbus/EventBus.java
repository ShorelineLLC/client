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
     * So how does this list get instantiated?
     *
     * 1) if we are in a development environment, use reflection to gather every Event class instance.
     * then put them in the map with a null invoker (stop_decompiling_1(null, null, null, null)).
     * (@see DevEventBusLoader)
     *
     * 2) if we are loading the client dynamically, each time the native class loader encounters a class that
     * extends Event, it puts it on this map with a null invoker.
     *
     * So essentially there is no computeIfAbsent for this list, it is always filled when the DLL is loaded.
     * This might not be efficient but its definitely obscure, and since the core security measure of this client
     * is the event bus, we can't have people figuring out how to recreate it.
     */
    private Object event2InvokerMap;

    private EventBus()
    {
    }

    /**
     * Iterate through the linked list for the event and invoke any entries matching the event type
     */
    @SuppressWarnings({"rawtypes"})
    public void dispatch(Event event)
    {
        if (this.event2InvokerMap == null)
        {
            return;
        }

        InvokerNode head = (InvokerNode) ((Map) this.event2InvokerMap).get(event.getClass());
        InvokerNode current = (InvokerNode) head.next;

        while (current != null)
        {
            ((Invoker) current.invoker).invoke(event);

            current = (InvokerNode) current.next;
        }
    }

    public native void subscribe(Object subscriber);

    public native void unsubscribe(Object subscriber);

    @SuppressWarnings({"unused", "FieldCanBeLocal"}) // Used natively
    public final static class InvokerNode
    {
        private final /* InvokerNode */ Object next;
        private final /* Invoker */ Object invoker;
        private final Object subscriber;
        private final /* Integer */ Object priority;

        private InvokerNode(Object invoker,
                            Object subscriber,
                            Object priority)
        {
            this.next = null;
            this.invoker = invoker;
            this.subscriber = subscriber;
            this.priority = priority;
        }
    }

    @FunctionalInterface
    public interface Invoker
    {
        void invoke(Object event);
    }
}
