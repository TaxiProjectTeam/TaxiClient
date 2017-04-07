package com.ck.taxoteam.taxoclient.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Sveta on 08.03.2017.
 */

public class RouteApiClient {
    private final static String BASE_URL = "https://maps.googleapis.com";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit;
    }
}
