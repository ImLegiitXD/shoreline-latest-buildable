package net.shoreline.eventbus;

import net.shoreline.eventbus.event.Event;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class EventBus {
    public static final EventBus INSTANCE = new EventBus();

    private final Map<Class<?>, List<Invoker>> eventHandlers = new ConcurrentHashMap<>();
    private final Map<Object, List<RegisteredHandler>> subscriberHandlers = new ConcurrentHashMap<>();

    public void dispatch(Event event) {
        List<Invoker> handlers = eventHandlers.get(event.getClass());
        if (handlers != null) {
            for (Invoker handler : handlers) {
                handler.invoke(event);
            }
        }
    }

    public void subscribe(Object subscriber) {
        for (Method method : subscriber.getClass().getDeclaredMethods()) {
            if (method.getParameterCount() == 1 && Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
                Class<?> eventType = method.getParameterTypes()[0];
                method.setAccessible(true);

                Invoker invoker = event -> {
                    try {
                        method.invoke(subscriber, event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                };

                eventHandlers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(invoker);
                subscriberHandlers.computeIfAbsent(subscriber, k -> new ArrayList<>())
                        .add(new RegisteredHandler(eventType, invoker));
            }
        }
    }

    public void unsubscribe(Object subscriber) {
        List<RegisteredHandler> handlers = subscriberHandlers.remove(subscriber);
        if (handlers != null) {
            for (RegisteredHandler handler : handlers) {
                List<Invoker> invokers = eventHandlers.get(handler.eventType);
                if (invokers != null) {
                    invokers.remove(handler.invoker);
                    if (invokers.isEmpty()) {
                        eventHandlers.remove(handler.eventType);
                    }
                }
            }
        }
    }

    @FunctionalInterface
    public interface Invoker {
        void invoke(Event event);
    }

    private static class RegisteredHandler {
        final Class<?> eventType;
        final Invoker invoker;

        RegisteredHandler(Class<?> eventType, Invoker invoker) {
            this.eventType = eventType;
            this.invoker = invoker;
        }
    }
}