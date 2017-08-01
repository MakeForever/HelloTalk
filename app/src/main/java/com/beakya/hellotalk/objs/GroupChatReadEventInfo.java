package com.beakya.hellotalk.objs;

/**
 * Created by goodlife on 2017. 7. 21..
 */

public class GroupChatReadEventInfo {
    private User myInfo;
    private String chatId;
    private int chatType;

    public GroupChatReadEventInfo(User myInfo, String chatId, int chatType) {
        this.myInfo = myInfo;
        this.chatId = chatId;
        this.chatType = chatType;
    }

    public User getMyInfo() {
        return myInfo;
    }

    public String getChatId() {
        return chatId;
    }

    public int getChatType() {
        return chatType;
    }
}
