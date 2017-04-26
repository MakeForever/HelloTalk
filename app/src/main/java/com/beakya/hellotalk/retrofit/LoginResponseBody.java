package com.beakya.hellotalk.retrofit;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cheolho on 2017. 3. 29..
 */

public class LoginResponseBody {
    @SerializedName("message")
    private String message;
    @SerializedName("token")
    private String token;
    @SerializedName("login")
    private int login;
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getLogin() {
        return login;
    }

    public void setLogin(int login) {
        this.login = login;
    }
}
