package com.beakya.hellotalk.retrofit;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cheolho on 2017. 3. 29..
 */

public class LoginRequestBody {

    @SerializedName("id")
    private String name;

    @SerializedName("password")
    private String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
