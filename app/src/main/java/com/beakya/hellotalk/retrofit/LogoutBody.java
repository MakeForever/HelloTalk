package com.beakya.hellotalk.retrofit;

import com.google.gson.annotations.SerializedName;

/**
 * Created by goodlife on 2017. 6. 20..
 */

public class LogoutBody {
    @SerializedName("token")
    String token;
    public LogoutBody( String token ) {
        this.token = token;
    }

}
