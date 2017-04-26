package com.beakya.hellotalk.retrofit;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cheolho on 2017. 3. 30..
 */

public class GeneralResponseBody {
    @SerializedName("message")
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
