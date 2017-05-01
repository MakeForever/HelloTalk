package com.beakya.hellotalk.retrofit;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by cheolho on 2017. 3. 30..
 */

public interface RegisterService {
    @POST("api/user")
    Call<GeneralResponseBody> register(@Body RegisterRequestBody body);

}
