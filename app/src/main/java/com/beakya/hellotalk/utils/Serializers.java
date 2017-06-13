package com.beakya.hellotalk.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.beakya.hellotalk.objs.User;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.json.JSONObject;

import java.lang.reflect.Type;

/**
 * Created by goodlife on 2017. 6. 13..
 */

public class Serializers {
    public static class UserSerializer implements JsonSerializer<User> {
        @Override
        public JsonElement serialize(User src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.addProperty("id", src.getId());
            object.addProperty("name", src.getName());
            object.addProperty("hasProfileImg", src.hasProfileImg());
            return object;
        }
    }
    public static class UserDeserializer implements JsonDeserializer<User> {
        @Override
        public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = json.getAsJsonObject();
            String name = object.get("name").getAsString();
            String id = object.get("id").getAsString();
            boolean hasProfileImg = object.get("hasProfileImg").getAsBoolean();
            Bitmap bitmap = null;
            if( object.has("img") ) {
                String bitmapBase64 = object.get("img").getAsString();
                byte[] imageBytes = Base64.decode(bitmapBase64, Base64.DEFAULT);
                bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            }
            User user = new User( id, name, hasProfileImg);
            user.setProfileImage(bitmap);
            return user;
        }
    }
}
