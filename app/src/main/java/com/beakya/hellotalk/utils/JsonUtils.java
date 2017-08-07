package com.beakya.hellotalk.utils;

import android.content.Context;

import com.google.gson.JsonObject;

/**
 * Created by goodlife on 2017. 8. 6..
 */

public class JsonUtils {
    public static JsonObject makeLeaveRoomObj (Context context, String chatId, int chatType, String userId) {
        JsonObject object = new JsonObject();
        JsonObject chatRoomObj = new JsonObject();
        chatRoomObj.addProperty("chatId", chatId);
        chatRoomObj.addProperty("chatType", chatType);
        object.add("chatRoom", chatRoomObj );
        object.addProperty("userId", userId);
        return object;
    }
}
