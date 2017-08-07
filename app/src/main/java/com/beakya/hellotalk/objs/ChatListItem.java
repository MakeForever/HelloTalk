package com.beakya.hellotalk.objs;

import android.support.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by goodlife on 2017. 6. 1..
 */

public class ChatListItem implements Comparable<String> {
    private ChatRoom chatRoom;
    private String lastMessage;
    private int lastMessageType;
    private String lastMessageCreatedTime;
    private int notReadCount;
    public ChatListItem(ChatRoom chatRoom, String lastMessage, int lastMessageType, String lastMessageCreatedTime, int notReadCount) {
        this.chatRoom = chatRoom;
        this.lastMessage = lastMessage;
        this.lastMessageType = lastMessageType;
        this.lastMessageCreatedTime = lastMessageCreatedTime;
        this.notReadCount = notReadCount;
    }

    public Integer getChatType () {
        if( chatRoom != null ) {
            return chatRoom.getChatRoomType();
        } else {
            return null;
        }
    }
    public ChatRoom getChatRoom () {
        return chatRoom;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public int getLastMessageType() {
        return lastMessageType;
    }

    public String getLastMessageCreatedTime() {
        return lastMessageCreatedTime;
    }

    public int getNotReadCount() {
        return notReadCount;
    }

    @Override
    public int compareTo(@NonNull String o) {
        Calendar target = Calendar.getInstance();
        Calendar current = Calendar.getInstance();
        try {
            target.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(o));
            current.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(lastMessageCreatedTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return target.compareTo(current);
    }
}
