package com.beakya.hellotalk.utils;

import com.beakya.hellotalk.objs.User;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;

/**
 * Created by goodlife on 2017. 6. 11..
 */

public class HashMapSerializer implements JsonSerializer<HashMap<String, User>> {
    @Override
    public JsonElement serialize(HashMap<String, User> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray array = new JsonArray();
        for ( User user : src.values() ) {
            JsonObject object = new JsonObject();
            object.addProperty("id", user.getId());
            object.addProperty("name", user.getName());
            object.addProperty("hasProfileImg", user.hasProfileImg());
            array.add(object);
        }
        return array ;
    }
}
