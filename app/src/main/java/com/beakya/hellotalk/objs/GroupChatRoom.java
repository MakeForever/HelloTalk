package com.beakya.hellotalk.objs;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by goodlife on 2017. 5. 11..
 */

public class ChatRoom implements Parcelable {
    public static final String TAG  = ChatRoom.class.getSimpleName();
    private HashMap<String, User> userList;
    private String chatId;
    private int chatRoomType;
    private boolean isSynchronized;
    public ChatRoom(HashMap<String, User> userList, String chatId, int chatRoomType, boolean isSynchronized ) {
        this.userList = userList;
        this.chatId = chatId;
        this.chatRoomType = chatRoomType;
        this.isSynchronized = isSynchronized;
    }


//    public ChatRoom(Parcel in) {
//        userList = new HashMap<>();
//        chatId = in.readString();
//        chatRoomType = in.readInt();
//        isSynchronized = in.readByte() != 0;
//        final int N = in.readInt();
//        for ( int i = 0; i < N; i++ ) {
//            String key = in.readString();
//            String id =  in.readString();
//            String name = in.readString();
//            boolean isAdded = in.readByte() != 0;
////            Log.d(TAG, "ChatRoom: " + key + " id : " + id + " name : " + name + " is Added " + isAdded);
//            User user = new User(id, name);
//            user.setAdded(isAdded);
//            userList.put(key, user);
//        }
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        final int N = userList.size();
//        dest.writeString(chatId);
//        dest.writeInt(chatRoomType);
//        dest.writeByte((byte) (isSynchronized ? 1 : 0));
//        dest.writeInt(N);
//        if ( N > 0 ) {
//            for ( HashMap.Entry<String, User> entry : userList.entrySet() ) {
//                dest.writeString(entry.getKey());
//                User user = entry.getValue();
//                dest.writeString(user.getId());
//                dest.writeString(user.getName());
//                dest.writeByte( ( byte) ( user.isAdded() ? 1 : 0 ));
//                System.out.println("user id " + user.getId());
//            }
//        }
//
//    }
    public static final Creator<ChatRoom> CREATOR = new Creator<ChatRoom>() {
        @Override
        public ChatRoom createFromParcel(Parcel in) {
            return new ChatRoom(in);
        }

        @Override
        public ChatRoom[] newArray(int size) {
            return new ChatRoom[size];
        }
    };

    public int getMembersCount () {
        return (userList == null) ? 0 : userList.size();
    }
    public HashMap<String, User> getUserList() {
        return userList;
    }

    public void setUserList(HashMap<String, User> userList) {
        this.userList = userList;
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


    public void printAll () {
        System.out.println("ChatRoom Id : " + chatId);
        System.out.println("Chat Type : " + chatRoomType);
        for( User user : userList.values() ) {
            System.out.println("User ID : " + user.getId());
            System.out.println("User Name : " + user.getName());
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }


}
