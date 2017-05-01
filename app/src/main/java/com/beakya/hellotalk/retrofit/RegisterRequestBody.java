package com.beakya.hellotalk.retrofit;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cheolho on 2017. 3. 30..
 */

public class RegisterRequestBody {
    @SerializedName("id")
    private String email;

    @SerializedName("name")
    private String name;

    @SerializedName("password")
    private String password;

    @SerializedName("gender")
    private int gender;

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {

        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
