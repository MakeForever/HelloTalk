package com.beakya.hellotalk.retrofit;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by cheolho on 2017. 3. 29..
 */

public interface LoginService {

    @POST("api/login")
    Call<LoginResponseBody> repoTest(@Body LoginRequestBody body);


}
