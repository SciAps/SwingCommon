package com.sciaps.common.swing.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Type;

/**
 *
 * @author sgowen
 */
public final class JsonUtils
{
    public static <T> T deserializeJsonIntoType(String json, Type type)
    {
        if (json == null)
        {
            return null;
        }

        Gson gson = new GsonBuilder().create();

        T deserializedObject = gson.fromJson(json, type);

        return deserializedObject;
    }

    public static String serializeJson(Object jsonObject)
    {
        if (jsonObject == null)
        {
            return null;
        }

        Gson gson = new GsonBuilder().create();

        return gson.toJson(jsonObject);
    }

    private JsonUtils()
    {
        // Hide Constructor for Static Utility Class
    }
}