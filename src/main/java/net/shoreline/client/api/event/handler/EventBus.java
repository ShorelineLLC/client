package net.shoreline.client.api.event.handler;

import net.shoreline.client.api.event.Event;
import net.shoreline.client.api.event.listener.EventListener;
import net.shoreline.client.api.event.listener.Listener;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linus
 * @see Event
 * @see EventHandler
 * @see net.shoreline.client.api.event.listener.EventListener
 * @see Listener
 * @since 1.0
 */
public class EventBus implements EventHandler {

    // Client Event handler (aka Event bus) which handles event dispatching
    // and listening for client events.
    public static EventHandler EVENT_HANDLER;
    // Active subscriber cache. Used to check if a class is already
    // subscribed to this EventHandler.
    private final Set<Object> subscribers = Collections.synchronizedSet(new HashSet<>());
    // Map of events and their associated listeners. All listeners in a class
    // will be added when the class is subscribed to this EventHandler.
    private final Map<Object, PriorityQueue<Listener>> listeners = new ConcurrentHashMap<>();

    /**
     * Subscribes a {@link Object} to the EventHandler and adds all
     * {@link net.shoreline.client.api.event.listener.EventListener} in the class to the active listener map.
     *
     * @param obj The subscriber object
     */
    @Override
    public void subscribe(Object obj) {
        if (subscribers.contains(obj)) {
            return;
        }
        subscribers.add(obj);
        for (Method method : obj.getClass().getMethods()) {
            method.trySetAccessible();
            if (method.isAnnotationPresent(EventListener.class)) {
                EventListener listener = method.getAnnotation(EventListener.class);
                if (method.getReturnType() == Void.TYPE) {
                    Class<?>[] params = method.getParameterTypes();
                    if (params.length == 1) {
                        PriorityQueue<Listener> active = listeners.computeIfAbsent(params[0],
                                v -> new PriorityQueue<>());
                        active.add(new Listener(method, obj,
                                listener.receiveCanceled(), listener.priority()));
                    }
                }
            }
        }
    }


    /**
     * Unsubscribes the subscriber {@link Class} and all associated
     * {@link EventListener} from the listener map.
     *
     * @param obj The subscriber object
     */
    @Override
    public void unsubscribe(Object obj) {
        if (subscribers.remove(obj)) {
            listeners.values().forEach(set ->
                    set.removeIf(l -> l.getSubscriber() == obj));
            listeners.entrySet().removeIf(e -> e.getValue().isEmpty());
        }
    }

    /**
     * Runs {@link Listener#invokeSubscriber(Event)} on all
     * active {@link Listener} for the param {@link Event}
     *
     * @param event The event to dispatch listeners
     * @return <tt>true</tt> if {@link Event#isCanceled()}
     */
    @Override
    public boolean dispatch(Event event) {
        if (event == null) {
            return false;
        }
        PriorityQueue<Listener> active = listeners.get(event.getClass());
        // if there are no items to dispatch to, just early return
        if (active == null || active.isEmpty()) {
            return false;
        }
        for (Listener listener : new ArrayList<>(active)) {
            if (event.isCanceled() && !listener.isReceiveCanceled()) {
                continue;
            }
            listener.invokeSubscriber(event);
        }
        return event.isCanceled();
    }

    static {
        // Create event handler instance
        EVENT_HANDLER = new EventBus();
    }
}
