package com.sciaps.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author sgowen
 */
public final class JsonUtils
{
    public static <T> T deserializeJsonIntoType(String json, Class<T> c)
    {
        if (json == null)
        {
            return null;
        }

        Gson gson = new GsonBuilder().create();

        T deserializedObject = gson.fromJson(json, c);

        return deserializedObject;
    }

    public static <T> List<T> deserializeJsonIntoListOfType(String json, Class<T[]> c)
    {
        if (json == null)
        {
            return null;
        }

        Gson gson = new GsonBuilder().create();

        T[] array = gson.fromJson(json, c);
        if (array == null)
        {
            return null;
        }

        List<T> list = new ArrayList<T>();
        list.addAll(Arrays.asList(array));

        return list;
    }
}