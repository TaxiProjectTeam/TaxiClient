package com.ck.taxoteam.taxoclient.model;

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

    public int getDistance() {
        return this.routes.get(0).legs.get(0).distance.value;
    }

    public int getDuration() {
        return this.routes.get(0).legs.get(0).duration.value;
    }

    private class Legs {
        Distance distance;
        Duration duration;
    }

    private class Route {
        List<Legs> legs;
        OverviewPolyline overview_polyline;
    }

    private class OverviewPolyline {
        String points;
    }

    private class Distance {
        int value;
    }

    private class Duration {
        int value;
    }
}

