package net.shoreline.eventbus;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.shoreline.eventbus.EventHandler;
import net.shoreline.eventbus.Listener;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.Event;

public class EventBus implements EventHandler {
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

    public static final EventBus INSTANCE = new EventBus();
    private final Set<Object> subscribers = Collections.synchronizedSet(new HashSet());
    private final Map<Object, PriorityQueue<Listener>> listeners = new ConcurrentHashMap<Object, PriorityQueue<Listener>>();

    @Override
    public void subscribe(Object obj) {
        if (this.subscribers.contains(obj)) {
            return;
        }
        this.subscribers.add(obj);
        for (Method method : obj.getClass().getMethods()) {
            Class<?>[] params;
            method.trySetAccessible();
            if (!method.isAnnotationPresent(EventListener.class)) continue;
            EventListener listener = method.getAnnotation(EventListener.class);
            if (method.getReturnType() != Void.TYPE || (params = method.getParameterTypes()).length != 1) continue;
            PriorityQueue active = this.listeners.computeIfAbsent(params[0], v -> new PriorityQueue());
            active.add(new Listener(method, obj, listener.receiveCanceled(), listener.priority()));
        }
    }

    @Override
    public void unsubscribe(Object obj) {
        if (this.subscribers.remove(obj)) {
            this.listeners.values().forEach(set -> set.removeIf(l -> l.getSubscriber() == obj));
            this.listeners.entrySet().removeIf(e -> ((PriorityQueue)e.getValue()).isEmpty());
        }
    }

    @Override
    public boolean dispatch(Event event) {
        if (event == null) {
            return false;
        }
        PriorityQueue<Listener> active = this.listeners.get(event.getClass());
        if (active == null || active.isEmpty()) {
            return false;
        }
        for (Listener listener : new ArrayList<Listener>(active)) {
            if (event.isCanceled() && !listener.isReceiveCanceled()) continue;
            listener.invokeSubscriber(event);
        }
        return event.isCanceled();
    }
}