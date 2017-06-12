package com.beakya.hellotalk.objs;

import android.content.Context;

import org.json.JSONObject;

/**
 * Created by goodlife on 2017. 5. 31..
 */

public abstract class ChatRoom {

    public static final int PERSONAL_CHAT_TYPE = 1;
    public static final int GROUP_CHAT_TYPE = 2;
    private String chatId;
    private int chatRoomType;
    private boolean isSynchronized;


    public ChatRoom(String chatId, int chatRoomType, boolean isSynchronized) {
        this.chatId = chatId;
        this.chatRoomType = chatRoomType;
        this.isSynchronized = isSynchronized;
    }
    public void printAll() {

    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public int getChatRoomType() {
        return chatRoomType;
    }

    public void setChatRoomType(int chatRoomType) {
        this.chatRoomType = chatRoomType;
    }

    public boolean isSynchronized() {
        return isSynchronized;
    }

    public void setSynchronized(boolean aSynchronized) {
        isSynchronized = aSynchronized;
    }

    public abstract String toJson(Message message, User myInfo, String event);
}
