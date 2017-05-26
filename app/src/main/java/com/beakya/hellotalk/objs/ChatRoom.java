package com.beakya.hellotalk.objs;

<<<<<<< HEAD
import java.util.ArrayList;

=======
>>>>>>> 306bf88... 커스텀 asynctaskloader 추가
/**
 * Created by goodlife on 2017. 5. 11..
 */

public class ChatRoom {
<<<<<<< HEAD
    private ArrayList<User> userList;
    private String chatId;
    private String lastContent;
    private int lastContentType;
    private int chatRoomType;
    private int notReadChatCount;
    private String lastContentTimeMessage;
//    public ChatRoom(ArrayList<String>  usersCursor, String chatId, String lastContent, int lastContentType, int chatRoomType, int notReadChatCount) {
//        this.userList = usersCursor;
//        this.chatId = chatId;
//        this.lastContent = lastContent;
//        this.lastContentType = lastContentType;
//        this.chatRoomType = chatRoomType;
//        this.notReadChatCount = notReadChatCount;
//    }
    public ChatRoom(ArrayList<User>  userList, String chatId, String lastContent, int lastContentType, int chatRoomType, int notReadChatCount, String lastContentTimeMessage) {
        this.userList = userList;
        this.chatId = chatId;
        this.lastContent = lastContent;
        this.lastContentType = lastContentType;
        this.chatRoomType = chatRoomType;
        this.notReadChatCount = notReadChatCount;
        this.lastContentTimeMessage = lastContentTimeMessage;
    }

    public int getNotReadChatCount() {
        return notReadChatCount;
    }

    public void setNotReadChatCount(int notReadChatCount) {
        this.notReadChatCount = notReadChatCount;
    }

    public String getLastContentTimeMessage() {
        return lastContentTimeMessage;
    }

    public void setLastContentTimeMessage(String lastContentTimeMessage) {
        this.lastContentTimeMessage = lastContentTimeMessage;
    }

    public ArrayList<User> getUserList() {
        return userList;
    }

    public void setUserList(ArrayList<User> userList) {
        this.userList = userList;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getLastContent() {
        return lastContent;
    }

    public void setLastContent(String lastContent) {
        this.lastContent = lastContent;
    }

    public int getLastContentType() {
        return lastContentType;
    }

    public void setLastContentType(int lastContentType) {
        this.lastContentType = lastContentType;
    }

    public int getChatRoomType() {
        return chatRoomType;
    }

    public void setChatRoomType(int chatRoomType) {
        this.chatRoomType = chatRoomType;
    }
=======
>>>>>>> 306bf88... 커스텀 asynctaskloader 추가
}
