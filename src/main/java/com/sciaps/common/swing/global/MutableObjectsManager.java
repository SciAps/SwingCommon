package com.sciaps.common.swing.global;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author sgowen
 * @param <T>
 */
public final class MutableObjectsManager<T>
{
    private final Map<String, T> _objects;
    private final Set<String> _objectsToDelete;
    private final Set<String> _objectsToUpdate;
    private final Set<String> _objectsToCreate;

    public MutableObjectsManager()
    {
        _objects = new HashMap();
        _objectsToDelete = new HashSet();
        _objectsToUpdate = new HashSet();
        _objectsToCreate = new HashSet();
    }

    public void reset()
    {
        _objects.clear();
        _objectsToDelete.clear();
        _objectsToUpdate.clear();
        _objectsToCreate.clear();
    }

    public String addObject(T object)
    {
        String temporaryUniqueId = java.util.UUID.randomUUID().toString();
        _objects.put(temporaryUniqueId, object);
        _objectsToCreate.add(temporaryUniqueId);

        return temporaryUniqueId;
    }

    public void markObjectAsModified(String objectId)
    {
        if (!_objectsToCreate.contains(objectId))
        {
            _objectsToUpdate.add(objectId);
        }
    }

    public void removeObject(String objectId)
    {
        _objects.remove(objectId);
        if (_objectsToCreate.contains(objectId))
        {
            _objectsToCreate.remove(objectId);
        }
        else
        {
            _objectsToDelete.add(objectId);
            _objectsToUpdate.remove(objectId);
        }
    }

    public boolean isValid()
    {
        return _objects != null;
    }

    public Map<String, T> getObjects()
    {
        return _objects;
    }

    public Set<String> getObjectsToDelete()
    {
        return _objectsToDelete;
    }

    public Set<String> getObjectsToUpdate()
    {
        return _objectsToUpdate;
    }

    public Set<String> getObjectsToCreate()
    {
        return _objectsToCreate;
    }
}