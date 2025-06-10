package net.shoreline.eventbus;

import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.Event;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    private final Map<Class<?>, InvokerNode> event2InvokerMap = new ConcurrentHashMap<>();
    private final Map<Method, Invoker> invokerCache = new HashMap<>();

    private EventBus() {}

    /**
     * Iterate through the linked list for the event and invoke any entries matching the event type
     */
    public void dispatch(Event event)
    {
        InvokerNode head = event2InvokerMap.get(event.getClass());
        if (head == null)
        {
            return;
        }

        InvokerNode current = head.next;

        while (current != null)
        {
            current.invoker.invoke(event);
            current = current.next;
        }
    }

    public void subscribe(Object subscriber)
    {
        Class<?> clazz = subscriber.getClass();
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        for (Method method : clazz.getDeclaredMethods())
        {
            if (!method.isAnnotationPresent(EventListener.class))
            {
                continue;
            }

            method.setAccessible(true);
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length != 1 || !Event.class.isAssignableFrom(paramTypes[0])) continue;

            Class<?> eventType = paramTypes[0];
            int priority = method.getAnnotation(EventListener.class).priority();

            Invoker invoker = invokerCache.computeIfAbsent(method, m -> {
                try {
                    MethodHandle handle = lookup.unreflect(m);

                    MethodType factoryType = MethodType.methodType(Invoker.class, clazz);
                    MethodType interfaceType = MethodType.methodType(void.class, Object.class);
                    MethodType targetType = MethodType.methodType(void.class, eventType);

                    CallSite site = LambdaMetafactory.metafactory(
                            lookup,
                            "invoke",
                            factoryType,
                            interfaceType,
                            handle,
                            targetType
                    );

                    return (Invoker) site.getTarget().invoke(subscriber);
                } catch (Throwable t) {
                    throw new RuntimeException("Failed to create invoker for: " + m, t);
                }
            });

            Integer boxedPriority = priority;
            InvokerNode newNode = new InvokerNode(invoker, subscriber, boxedPriority);

            event2InvokerMap.compute(eventType, (key, head) -> {
                if (head == null) {
                    head = new InvokerNode(null, null, null); // dummy head
                }

                InvokerNode prev = head;
                InvokerNode curr = head.next;

                while (curr != null && curr.priority >= priority) {
                    prev = curr;
                    curr = curr.next;
                }

                prev.next = newNode;
                newNode.next = curr;

                return head;
            });
        }
    }

    public void unsubscribe(Object subscriber) {
        for (Map.Entry<Class<?>, InvokerNode> entry : event2InvokerMap.entrySet()) {
            InvokerNode head = entry.getValue();
            InvokerNode prev = head;
            InvokerNode curr = head.next;

            while (curr != null) {
                if (curr.subscriber == subscriber) {
                    prev.next = curr.next;
                } else {
                    prev = curr;
                }
                curr = curr.next;
            }
        }
    }

    public static final class InvokerNode {
        private InvokerNode next;
        private final Invoker invoker;
        private final Object subscriber;
        private final Integer priority;

        private InvokerNode(Invoker invoker, Object subscriber, Integer priority) {
            this.invoker = invoker;
            this.subscriber = subscriber;
            this.priority = priority;
        }
    }

    @FunctionalInterface
    public interface Invoker {
        void invoke(Object event);
    }
}
