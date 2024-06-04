package net.shoreline.eventbus.bus;

import net.shoreline.eventbus.Event;

public final class EventBus
{
    public static final EventBus INSTANCE = new EventBus();

    // Our null head invoker. Created natively in init
    private stop_decompiling a;

    public EventBus()
    {
        init();
    }

    public void dispatch(Event event)
    {
        stop_decompiling current = this.a.e;
        while (current != null)
        {
            if (event.getClass().equals(current.c))
            {
                dispatch_internal(current.a, current.b, new Object[] { event });
//                try
//                {
//                    ((Method) current.a).invoke(current.b, event);
//                } catch (Throwable t)
//                {
//                    t.printStackTrace();
//                }
            }

            current = current.e;
        }
    }

    public native void init();

    public native void subscribe(Object subscriber);

    public native void unsubscribe(Object subscriber);

    public native void dispatch_internal(Object method,
                                         Object instance,
                                         Object event);

    /**
     * Our invoker class
     *
     * It cannot be obfuscated, so we will use obscure names.
     *
     * a = Method the method to invoke
     * b = Object instance the class instance
     * c = Class the event param
     * d = priority
     * e = Invoker next the next invoker in the linked list
     */
    private static final class stop_decompiling
    {
        private final Object a;
        private final Object b;
        private final Object c;
        private final int d;
        private stop_decompiling e;

        private stop_decompiling(Object a,
                                 Object b,
                                 Object c,
                                 int d)
        {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
            this.e = null;
        }
    }
}
