package com.example.sveta.taxo.api;

import com.example.sveta.taxo.model.RouteResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Sveta on 02.03.2017.
 */

public interface ApiInterface {
    @GET("/maps/api/directions/json")
    Call<RouteResponse> getRoute(@Query("origin") String startLat,
                                 @Query("destination") String startLng);
}
