package com.beakya.hellotalk.utils;

import com.beakya.hellotalk.objs.PersonalChatRoom;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Created by goodlife on 2017. 6. 13..
 */

public class PersonalChatTypeAdapter extends TypeAdapter<PersonalChatRoom> {
    @Override
    public void write(JsonWriter out, PersonalChatRoom value) throws IOException {

    }

    @Override
    public PersonalChatRoom read(JsonReader in) throws IOException {
        return null;
    }
}
