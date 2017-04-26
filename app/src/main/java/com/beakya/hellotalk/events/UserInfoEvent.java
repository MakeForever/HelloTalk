package com.beakya.hellotalk.events;

import org.json.JSONObject;

/**
 * Created by cheolho on 2017. 4. 12..
 */

public class UserInfoEvent extends MessageEvent<JSONObject> {

    public UserInfoEvent(String message, JSONObject storage) {
        super(message, storage);
    }
}
