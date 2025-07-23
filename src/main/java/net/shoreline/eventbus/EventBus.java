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
    private final Map<Method, Invoker> invokerCache = new HashMap<>();

    private EventBus() {}

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
            if (paramTypes.length != 1 || !Event.class.isAssignableFrom(paramTypes[0]))
            {
                continue;
            }

            Class<?> eventType = paramTypes[0];
            int priority = method.getAnnotation(EventListener.class).priority();

            Invoker invoker = invokerCache.computeIfAbsent(method, m ->
            {
                try
                {
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
                } catch (Throwable t)
                {
                    throw new RuntimeException("Failed to create invoker for: " + m, t);
                }
            });

            Integer boxedPriority = priority;
            InvokerNode newNode = new InvokerNode(invoker, subscriber, boxedPriority);

            event2InvokerMap.compute(eventType, (key, head) ->
            {
                if (head == null)
                {
                    head = new InvokerNode(null, null, null); // dummy head
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

            while (curr != null) {
                if (curr.subscriber == subscriber)
                {
                    prev.next = curr.next;
                } else
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
        private final Invoker invoker;
        private final Object subscriber;
        private final Integer priority;

        private InvokerNode(Invoker invoker,
                            Object subscriber,
                            Integer priority)
        {
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
