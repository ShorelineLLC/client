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
    private Object stop_decompiling_0;

    private EventBus()
    {
    }

    /**
     * Iterate through the linked list for the event and invoke any entries matching the event type
     */
    public void dispatch(Event event)
    {
        if (this.stop_decompiling_0 == null)
        {
            return;
        }

        stop_decompiling_1 head = (stop_decompiling_1) ((Map) this.stop_decompiling_0).get(event.getClass());
        stop_decompiling_1 current = (stop_decompiling_1) head.stop_decompiling_0;

        while (current != null)
        {
            ((stop_decompiling_0) current.stop_decompiling_1).stop_decompiling_0(event);

            current = (stop_decompiling_1) current.stop_decompiling_0;
        }
    }

    public native void subscribe(Object subscriber);

    public native void unsubscribe(Object subscriber);

    @FunctionalInterface
    private interface stop_decompiling_0
    {
        /**
         * invoke(Event event)
         */


        void stop_decompiling_0(Object object);
    }

    /**
     * Linked list entry node for Invokers
     */
    private static final class stop_decompiling_1
    {
        /**
         * Next linked list entry (stop_decompiling_1)
         */
        private final Object stop_decompiling_0;

        /**
         * Invoker (stop_decompiling_0)
         */
        private final Object stop_decompiling_1;

        /**
         * The subscriber instance
         */
        private final Object stop_decompiling_2;

        /**
         * Priority (int) probably boxed as Integer
         */
        private final Object stop_decompiling_3;

        private stop_decompiling_1(Object stop_decompiling_0,
                                   Object stop_decompiling_1,
                                   Object stop_decompiling_2)
        {
            this.stop_decompiling_0 = null;
            this.stop_decompiling_1 = stop_decompiling_0;
            this.stop_decompiling_2 = stop_decompiling_1;
            this.stop_decompiling_3 = stop_decompiling_2;
        }
    }
}
