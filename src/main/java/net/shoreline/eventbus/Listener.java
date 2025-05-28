package net.shoreline.eventbus;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import net.shoreline.client.Shoreline;
import net.shoreline.client.api.Invokable;
import net.shoreline.eventbus.event.Event;

public class Listener implements Comparable<Listener> {
    private static final Map<Method, Invokable<Object>> INVOKE_CACHE = new HashMap<Method, Invokable<Object>>();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private final Method method;
    private final Object subscriber;
    private final boolean receiveCanceled;
    private final int priority;
    private Invokable<Object> invoker;

    public Listener(Method method, Object subscriber, boolean receiveCanceled, int priority) {
        this.method = method;
        this.subscriber = subscriber;
        this.receiveCanceled = receiveCanceled;
        this.priority = priority;
        try {
            if (!INVOKE_CACHE.containsKey(method)) {
                MethodType methodType = MethodType.methodType(Invokable.class);
                CallSite callSite = LambdaMetafactory.metafactory(LOOKUP, "invoke", methodType.appendParameterTypes(subscriber.getClass()), MethodType.methodType(Void.TYPE, Object.class), LOOKUP.unreflect(method), MethodType.methodType(Void.TYPE, method.getParameterTypes()[0]));
                this.invoker = (Invokable<Object>) callSite.getTarget().invoke(subscriber);
                INVOKE_CACHE.put(method, this.invoker);
            } else {
                this.invoker = INVOKE_CACHE.get(method);
            }
        }
        catch (Throwable e) {
            Shoreline.error("Failed to build invoker for {}!", method.getName());
            e.printStackTrace();
        }
    }

    public void invokeSubscriber(Event event) {
        if (event != null) {
            this.invoker.invoke(event);
        }
    }

    @Override
    public int compareTo(Listener other) {
        return Integer.compare(other.getPriority(), this.getPriority());
    }

    public Method getMethod() {
        return this.method;
    }

    public Object getSubscriber() {
        return this.subscriber;
    }

    public boolean isReceiveCanceled() {
        return this.receiveCanceled;
    }

    public int getPriority() {
        return this.priority;
    }
}
