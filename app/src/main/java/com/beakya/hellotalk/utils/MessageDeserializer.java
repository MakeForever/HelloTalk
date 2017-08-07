package com.beakya.hellotalk.utils;

import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.objs.Message;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Created by goodlife on 2017. 6. 26..
 */

public class MessageDeserializer implements JsonDeserializer<Message> {
    @Override
    public Message deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        String chatId = object.get("chat_id").getAsString();
        String messageId = object.get("message_id").getAsString();
        String creatorId = object.get("creator_id").getAsString();
        String messageContent = object.get("message_content").getAsString();
        int messageType = object.get("message_type").getAsInt();
        int readCount = object.get("read_count").getAsInt();
        String createdTime = object.get("created_time").getAsString();
        return new Message(messageId, creatorId, messageContent, chatId, messageType, createdTime, true, readCount);
    }
}
