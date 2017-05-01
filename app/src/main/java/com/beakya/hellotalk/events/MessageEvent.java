package com.beakya.hellotalk.events;

/**
 * Created by cheolho on 2017. 4. 10..
 */

public class MessageEvent<T> {
    private String message;
    private T storage;
    public MessageEvent(String message, T storage) {
        this.message = message;
        this.storage = storage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getStorage() {
        return storage;
    }

    public void setStorage(T storage) {
        this.storage = storage;
    }
}
