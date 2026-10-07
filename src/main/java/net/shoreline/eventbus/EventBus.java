package net.shoreline.eventbus;

import net.shoreline.eventbus.annotation.EventListener;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** @author bon gone but not forgotten **/
public final class EventBus
{
    public static final EventBus INSTANCE = new EventBus();

    private final Map<Class<?>, InvokerNode> event2InvokerMap = new ConcurrentHashMap<>();
    private final Map<Method, Invoker<?>> invokerCache = new HashMap<>();

    private EventBus() {}

    @SuppressWarnings("unchecked")
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
            if (event.isReceiveCanceled())
            {
                return;
            }

            Invoker<Event> invoker = (Invoker<Event>) current.invoker;
            invoker.invoke(event);

            current = current.next;
        }
    }

    public <E extends Event> void addListener(Class<E> eventType, Invoker<? super E> invoker)
    {
        addListener(eventType, invoker, 0);
    }

    @SuppressWarnings("unchecked")
    public <E extends Event> void addListener(Class<E> eventType, Invoker<? super E> invoker, int priority)
    {
        InvokerNode newNode = new InvokerNode(invoker, invoker, priority);
        event2InvokerMap.compute(eventType, (key, head) ->
        {
            if (head == null)
            {
                head = new InvokerNode(null, null, Integer.MIN_VALUE);
            }

            InvokerNode prev = head;
            InvokerNode curr = head.next;

            while (curr != null && curr.priority >= priority)
            {
                prev = curr;
                curr = curr.next;
            }

            prev.next = newNode;
            newNode.next = curr;

            return head;
        });
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
            if (paramTypes.length != 1 || !Event.class.isAssignableFrom(paramTypes[0]))
            {
                continue;
            }

            Class<?> eventType = paramTypes[0];
            int priority = method.getAnnotation(EventListener.class).priority();

            Invoker<?> invoker = invokerCache.computeIfAbsent(method, m ->
            {
                try
                {
                    MethodHandle handle = lookup.unreflect(m);

                    MethodType factoryType = MethodType.methodType(Invoker.class, clazz);
                    MethodType interfaceType = MethodType.methodType(void.class, Event.class);
                    MethodType targetType = MethodType.methodType(void.class, eventType);

                    CallSite site = LambdaMetafactory.metafactory(
                            lookup,
                            "invoke",
                            factoryType,
                            interfaceType,
                            handle,
                            targetType
                    );

                    return (Invoker<?>) site.getTarget().invoke(subscriber);
                }
                catch (Throwable t)
                {
                    throw new RuntimeException("Failed to create invoker for: " + m, t);
                }
            });

            InvokerNode newNode = new InvokerNode(invoker, subscriber, priority);

            event2InvokerMap.compute(eventType, (key, head) ->
            {
                if (head == null)
                {
                    head = new InvokerNode(null, null, Integer.MIN_VALUE);
                }

                InvokerNode prev = head;
                InvokerNode curr = head.next;

                while (curr != null && curr.priority >= priority)
                {
                    prev = curr;
                    curr = curr.next;
                }

                prev.next = newNode;
                newNode.next = curr;

                return head;
            });
        }
    }

    public void unsubscribe(Object subscriber)
    {
        for (Map.Entry<Class<?>, InvokerNode> entry : event2InvokerMap.entrySet())
        {
            InvokerNode head = entry.getValue();
            InvokerNode prev = head;
            InvokerNode curr = head.next;

            while (curr != null)
            {
                if (curr.subscriber == subscriber)
                {
                    prev.next = curr.next;
                }
                else
                {
                    prev = curr;
                }
                curr = curr.next;
            }
        }
    }

    public static final class InvokerNode
    {
        private InvokerNode next;
        private final Invoker<? extends Event> invoker;
        private final Object subscriber;
        private final int priority;

        private InvokerNode(Invoker<? extends Event> invoker, Object subscriber, int priority)
        {
            this.invoker = invoker;
            this.subscriber = subscriber;
            this.priority = priority;
        }
    }

    @FunctionalInterface
    public interface Invoker<E extends Event>
    {
        void invoke(E event);
    }
}
