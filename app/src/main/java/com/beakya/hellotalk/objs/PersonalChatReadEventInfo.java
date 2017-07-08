package com.beakya.hellotalk.objs;

/**
 * Created by goodlife on 2017. 7. 8..
 */

public class PersonalChatReadEventInfo {
    User receiver;
    int chatType;
    String chatId;
    String myIdInfo;

    public PersonalChatReadEventInfo(User receiver, int chatType, String chatId, String myIdInfo) {
        this.receiver = receiver;
        this.chatType = chatType;
        this.chatId = chatId;
        this.myIdInfo = myIdInfo;
    }

    public User getReceiver() {
        return receiver;
    }

    public int getChatType() {
        return chatType;
    }

    public String getChatId() {
        return chatId;
    }

    public String getMyIdInfo() {
        return myIdInfo;
    }
}
