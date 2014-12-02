package com.sciaps.global;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author sgowen
 */
public final class InstanceManager
{
    private static final Object LOCK = new Object();

    private static InstanceManager instance;

    private final Map<Class<?>, Object> manager;

    public static InstanceManager getInstance()
    {
        synchronized (LOCK)
        {
            if (instance == null)
            {
                instance = new InstanceManager();
            }

            return instance;
        }
    }

    public <T> void storeInstance(Class<?> clazz, T instance)
    {
        synchronized (LOCK)
        {
            manager.put(clazz, instance);
        }
    }

    public <T> T retrieveInstance(Class<?> clazz)
    {
        return (T) manager.get(clazz);
    }

    private InstanceManager()
    {
        // Hide Constructor
        manager = new HashMap<Class<?>, Object>();
    }
}