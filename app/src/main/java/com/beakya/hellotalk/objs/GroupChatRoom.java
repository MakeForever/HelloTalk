package com.beakya.hellotalk.objs;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

/**
 * Created by goodlife on 2017. 5. 11..
 */

public class GroupChatRoom extends ChatRoom implements Parcelable{
    public static final String TAG  = GroupChatRoom.class.getSimpleName();
    private HashMap<String, User> userList;

    public GroupChatRoom(HashMap<String, User> userList, String chatId, int chatRoomType, boolean isSynchronized ) {
        super(chatId, chatRoomType, isSynchronized);
        this.userList = userList;
    }


    public GroupChatRoom(Parcel in) {
        super(in.readString(), in.readInt(), in.readByte() != 0);
        userList = new HashMap<>();

        final int N = in.readInt();
        for ( int i = 0; i < N; i++ ) {
            String key = in.readString();
            String id =  in.readString();
            String name = in.readString();
            boolean isAdded = in.readByte() != 0;
//            Log.d(TAG, "GroupChatRoom: " + key + " id : " + id + " name : " + name + " is Added " + isAdded);
            User user = new User(id, name, false);
            user.setAdded(isAdded);
            userList.put(key, user);
        }
    }

    public static final Creator<GroupChatRoom> CREATOR = new Creator<GroupChatRoom>() {
        @Override
        public GroupChatRoom createFromParcel(Parcel in) {
            return new GroupChatRoom(in);
        }

        @Override
        public GroupChatRoom[] newArray(int size) {
            return new GroupChatRoom[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        final int N = userList.size();
        dest.writeString(getChatId());
        dest.writeInt(getChatRoomType());
        dest.writeByte((byte) (isSynchronized() ? 1 : 0));
        dest.writeInt(N);
        if ( N > 0 ) {
            for ( HashMap.Entry<String, User> entry : userList.entrySet() ) {
                dest.writeString(entry.getKey());
                User user = entry.getValue();
                dest.writeString(user.getId());
                dest.writeString(user.getName());
                dest.writeByte( ( byte) ( user.isAdded() ? 1 : 0 ));
                System.out.println("user id " + user.getId());
            }
        }

    }

    public int getMembersCount () {
        return (userList == null) ? 0 : userList.size();
    }

    public HashMap<String, User> getUserList() {
        return userList;
    }

    public void setUserList(HashMap<String, User> userList) {
        this.userList = userList;
    }

    @Override
    public void printAll () {
        System.out.println("GroupChatRoom Id : " + getChatId());
        System.out.println("Chat Type : " + getChatRoomType());
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
