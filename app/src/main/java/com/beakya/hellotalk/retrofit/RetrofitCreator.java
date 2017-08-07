package com.beakya.hellotalk.retrofit;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by cheolho on 2017. 3. 30..
 */

public class RetrofitCreator {

    public static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://192.168.0.100:8888/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(getRequestHeader())
            .build();

    private static OkHttpClient getRequestHeader() {
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }
}
