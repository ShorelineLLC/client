package net.shoreline.eventbus.dev;

import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.event.Event;
import net.shoreline.loader.Loader;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;

/**
 * This class is not exported with the loader or client.
 * It is kept as a dev environment loader ONLY, for the event bus to function.
 */
@SuppressWarnings("unused") // Called natively
public final class DevEventBusLoader
{
    /**
     * Loads all the event types into the event bus map
     */
    public static void load()
    {
        try
        {
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
            Loader.error("Failed to load Event types:", t);
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
            Object instance = EventBus.INSTANCE;
            Field eventMap = EventBus.class.getDeclaredField("event2InvokerMap");
            eventMap.setAccessible(true);

            Map<Class<? extends Event>, EventBus.InvokerNode> map = (Map<Class<? extends Event>, EventBus.InvokerNode>) eventMap.get(instance);

            Constructor<?> constructor = EventBus.InvokerNode.class.getDeclaredConstructor(Object.class, Object.class, Object.class);
            constructor.setAccessible(true);

            EventBus.InvokerNode headInvoker = (EventBus.InvokerNode) constructor.newInstance(null, null, null);

            map.put((Class<? extends Event>) clazz, headInvoker);
        }
    }

    private static void loadDirectory(File directory,
                                      String currPrefix) throws Throwable
    {
        String[] files = directory.list();
        for (String file : files)
        {
            File subFile = new File(directory, file);
            if (subFile.isDirectory())
            {
                loadDirectory(subFile, currPrefix + "." + subFile.getName());
            } else
            {
                loadSingleFile(subFile, currPrefix);
            }
        }
    }
}
