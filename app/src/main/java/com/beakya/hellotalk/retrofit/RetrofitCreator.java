package com.beakya.hellotalk.retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by cheolho on 2017. 3. 30..
 */

public class RetrofitCreator {

    public static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://192.168.0.102:8888/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
