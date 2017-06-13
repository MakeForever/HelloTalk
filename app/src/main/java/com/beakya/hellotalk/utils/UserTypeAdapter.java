package com.beakya.hellotalk.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.objs.User;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Created by goodlife on 2017. 6. 13..
 */

public class UserTypeAdapter extends TypeAdapter<User> {
    private final String jsonId = "id";
    private final String jsonName = "name";
    private final String JsonHasPic = "hasProfileImg";
    private final String img = "img";
    @Override
    public void write(JsonWriter out, User value) throws IOException {
        out.beginObject();
        out.name(jsonId).value(value.getId());
        out.name(jsonName).value(value.getName());
        out.name(JsonHasPic).value(value.hasProfileImg());
        out.endObject();
    }

    @Override
    public User read(JsonReader in) throws IOException {
        String id = null;
        String name = null;
        boolean hasPic = false;
        Bitmap bitmap = null;
        in.beginObject();
        while( in.hasNext() ) {
            switch ( in.nextName() ) {
                case jsonId:
                    id = in.nextString();
                    break;
                case jsonName:
                    name = in.nextString();
                    break;
                case JsonHasPic:
                    hasPic = in.nextBoolean();
                    break;
//                case img :
//                    String imgStringData = in.nextString();
//                    byte[] imageBytes = Base64.decode(imgStringData, Base64.DEFAULT);
//                    bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
//                    break;
                default:
                    break;
            }
        }
        User user = new User(id, name, hasPic);
//        user.setProfileImage(bitmap);
        return user;
    }
}
