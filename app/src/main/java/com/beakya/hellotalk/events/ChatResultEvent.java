package com.beakya.hellotalk.events;

/**
 * Created by goodlife on 2017. 5. 9..
 */

public  class ChatResultEvent <T> {
    private String message;
    private T storage;
    private String tableName;
    public ChatResultEvent(String message, String tableName, T storage) {
        this.tableName = tableName;
        this.message = message;
        this.storage = storage;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
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
