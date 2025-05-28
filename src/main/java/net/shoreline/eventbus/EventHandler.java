package net.shoreline.eventbus;

import net.shoreline.eventbus.event.Event;

public interface EventHandler {
    public void subscribe(Object var1);

    public void unsubscribe(Object var1);

    public boolean dispatch(Event var1);
}
