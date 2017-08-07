package com.beakya.hellotalk.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.beakya.hellotalk.objs.User;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.HashMap;

/**
 * Created by goodlife on 2017. 6. 11..
 */

public class HashMapDeserializer implements JsonDeserializer<HashMap<String, User>> {
    @Override
    public HashMap<String, User> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        HashMap<String, User> users = new HashMap<>();
        JsonArray array = json.getAsJsonArray();
        for ( int i = 0; i < array.size(); i++ ) {
            JsonObject object = array.get(i).getAsJsonObject();
            String id = object.get("id").getAsString();
            String name = object.get("name").getAsString();
            boolean hasProfileImg = object.get("hasProfileImg").getAsBoolean();
            Bitmap bitmap = null;
            if ( hasProfileImg ) {
                String imgStringData = object.get("img").getAsString();
                byte[] imageBytes = Base64.decode(imgStringData, Base64.DEFAULT);
                bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            }
            User user = new User(id, name, hasProfileImg);
            user.setProfileImage(bitmap);
            users.put(id, user);
        }

        return users;
    }
}
