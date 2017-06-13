package com.beakya.hellotalk.objs;

import android.os.Parcel;
import android.os.Parcelable;

import com.beakya.hellotalk.database.TalkContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by goodlife on 2017. 5. 11..
 */

public class GroupChatRoom extends ChatRoom implements Parcelable{
    public static final String TAG  = GroupChatRoom.class.getSimpleName();
    private String chatName;
    private HashMap<String, User> users;

    public GroupChatRoom(String chatName, HashMap<String, User> users, String chatId, int chatRoomType, boolean isSynchronized ) {
        super(chatId, chatRoomType, isSynchronized);
        this.chatName = chatName;
        this.users = users;
    }


    public GroupChatRoom(Parcel in) {
        super(in.readString(), in.readInt(), in.readByte() != 0);
        this.chatName = in.readString();
        users = new HashMap<>();
        final int N = in.readInt();
        for ( int i = 0; i < N; i++ ) {
            String key = in.readString();
            String id =  in.readString();
            String name = in.readString();
            boolean isAdded = in.readByte() != 0;
//            Log.d(TAG, "GroupChatRoom: " + key + " id : " + id + " name : " + name + " is Added " + isAdded);
            User user = new User(id, name, false);
            user.setAdded(isAdded);
            users.put(key, user);
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
        final int N = users.size();
        dest.writeString(getChatId());
        dest.writeInt(getChatRoomType());
        dest.writeByte((byte) (isSynchronized() ? 1 : 0));
        dest.writeString(chatName);
        dest.writeInt(N);
        if ( N > 0 ) {
            for ( HashMap.Entry<String, User> entry : users.entrySet() ) {
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
        return (users == null) ? 0 : users.size();
    }

    public HashMap<String, User> getUsers() {
        return users;
    }

    public void setUsers(HashMap<String, User> users) {
        this.users = users;
    }

    public String getChatName() {
        return chatName;
    }
    public void addUser( User user ) {
        if( users != null ) {
            users.put(user.getId(), user);
        }
    }
    @Override
    public void printAll () {
        System.out.println("GroupChatRoom Id : " + getChatId());
        System.out.println("Chat Type : " + getChatRoomType());
        for( User user : users.values() ) {
            System.out.println("User ID : " + user.getId());
            System.out.println("User Name : " + user.getName());
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

}
