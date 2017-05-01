package com.beakya.hellotalk.retrofit;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by cheolho on 2017. 4. 13..
 */

public interface PhotoProfileService {

    @Multipart
    @POST("api/upload/photo")
    Call<ResponseBody> upload (
            @Header("Authorization") String authorization,
            @Part MultipartBody.Part photo );
}
