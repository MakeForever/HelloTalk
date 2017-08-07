package com.beakya.hellotalk.event;

import com.beakya.hellotalk.objs.Message;
import com.beakya.hellotalk.objs.User;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by goodlife on 2017. 5. 30..
 */

public class Events {

    public interface Event<T> {
        String getMessage();
        T getStorage();
    }

    public static class BooleanEvent implements Event<Boolean> {
        private Boolean storage;
        private String message;

        public BooleanEvent(Boolean storage, String message) {
            this.storage = storage;
            this.message = message;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public Boolean getStorage() {
            return storage;
        }
    }

    public static class FriendFindEvent implements Event<JSONObject> {
        private JSONObject storage;
        private String message;

        public FriendFindEvent(String message, JSONObject storage) {
            this.storage = storage;
            this.message = message;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public JSONObject getStorage() {
            return storage;
        }
    }
    public static class MessageEvent implements Event<Message> {
        private Message storage;
        private String message;

        public MessageEvent(String message, Message storage) {
            this.storage = storage;
            this.message = message;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public Message getStorage() {
            return storage;
        }
    }
    public static class UserLeaveEvent implements Event<String> {
        private String event;
        private String message;

        public UserLeaveEvent(String event, String message) {
            this.event = event;
            this.message = message;
        }

        @Override
        public String getMessage() {
            return event;
        }

        @Override
        public String getStorage() {
            return message;
        }
    }
    public static class UserInviteEvent implements Event<ArrayList<User>> {
        private ArrayList<User> users;
        private String message;

        public UserInviteEvent( String message, ArrayList<User> users) {
            this.users = users;
            this.message = message;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public ArrayList<User> getStorage() {
            return users;
        }
    }
}
