package com.beakya.hellotalk.retrofit;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Created by cheolho on 2017. 3. 29..
 */

public interface UserServices {

    @POST("api/login")
    Call<LoginResponseBody> login(@Body LoginRequestBody body );
    @POST("api/change_password")
    Call<GeneralResponseBody> changePassword(@Header("token") String token, @Body ChangePasswordBody body );
}
