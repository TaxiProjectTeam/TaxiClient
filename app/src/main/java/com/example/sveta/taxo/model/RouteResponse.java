package com.example.sveta.taxo.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;


/**
 * Created by Sveta on 08.03.2017.
 */

public class RouteResponse {
    @SerializedName("routes")
    public List<Route> routes;

    public String getPoints() {
        return this.routes.get(0).overview_polyline.points;
    }

    private class Route {
        OverviewPolyline overview_polyline;
    }

    private class OverviewPolyline {
        String points;
    }
}

