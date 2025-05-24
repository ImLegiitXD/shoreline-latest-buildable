package net.shoreline.eventbus.dev;

import net.shoreline.client.Shoreline;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.event.Event;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public final class DevEventBusLoader
{
    public static void load()
    {
        try
        {
            Field eventMap = EventBus.class.getDeclaredField("eventHandlers");
            eventMap.setAccessible(true);
            if (eventMap.get(EventBus.INSTANCE) == null) {
                eventMap.set(EventBus.INSTANCE, new ConcurrentHashMap<>());
            }

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources("net/shoreline/client/impl/event");
            while (resources.hasMoreElements())
            {
                URL resource = resources.nextElement();
                File directory = new File(resource.getFile());
                loadDirectory(directory, "net.shoreline.client.impl.event");
            }
        } catch (Throwable t)
        {
            Shoreline.error("Failed to load Event types:", t);
        }
    }

    @SuppressWarnings("unchecked")
    private static void loadSingleFile(File file,
                                       String prefix) throws Throwable
    {
        String className = prefix + '.' + file.getName().substring(0, file.getName().length() - 6);
        Class<?> clazz = Class.forName(className);

        if (Event.class.isAssignableFrom(clazz))
        {
            Field eventMap = EventBus.class.getDeclaredField("eventHandlers");
            eventMap.setAccessible(true);
            Map<Class<? extends Event>, List<EventBus.Invoker>> map =
                    (Map<Class<? extends Event>, List<EventBus.Invoker>>) eventMap.get(EventBus.INSTANCE);
            map.putIfAbsent((Class<? extends Event>) clazz, new ArrayList<>());
        }
    }

    private static void loadDirectory(File directory,
                                      String currPrefix) throws Throwable
    {
        String[] files = directory.list();
        if (files == null) return;

        for (String file : files)
        {
            File subFile = new File(directory, file);
            if (subFile.isDirectory())
            {
                loadDirectory(subFile, currPrefix + "." + subFile.getName());
            } else if (file.endsWith(".class"))
            {
                loadSingleFile(subFile, currPrefix);
            }
        }
    }
}