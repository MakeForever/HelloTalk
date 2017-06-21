package com.beakya.hellotalk.retrofit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by goodlife on 2017. 6. 20..
 */

public interface LogoutService {
    @POST("api/logout")
    Call<ResponseBody> logout(@Body LogoutBody body );
}
