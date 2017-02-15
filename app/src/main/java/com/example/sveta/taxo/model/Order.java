package com.example.sveta.taxo.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;

/**
 * Created by Sveta on 03.01.2017.
 */
@IgnoreExtraProperties
public class Order {

    private HashMap<String, Double> fromCoords;
    private HashMap<String, Double> driverPos;
    private String driverId;
    private String status;
    private int time;
    private HashMap<String, HashMap<String, Double>> toCoords;
    private int price;
    private String additionalComment;

    public Order() {
    }

    public Order(HashMap<String, Double> fromCoords, HashMap<String, HashMap<String, Double>> toCoords, int price, String additionalComment) {
        this.fromCoords = fromCoords;
        this.toCoords = toCoords;
        this.price = price;
        this.additionalComment = additionalComment;

        this.driverPos = new HashMap<>();
        this.driverPos.put("latitude", 0.0);
        this.driverPos.put("longitude", 0.0);

        this.driverId = "";
        this.time = 0;
        this.status = "";
    }

    public HashMap<String, Double> getFromCoords() {
        return fromCoords;
    }

    public HashMap<String, HashMap<String, Double>> getToCoords() {
        return toCoords;
    }

    public int getPrice() {
        return price;
    }

    public String getAdditionalComment() {
        return additionalComment;
    }

    public HashMap<String, Double> getDriverPos() {
        return driverPos;
    }

    public String getDriverId() {
        return driverId;
    }

    public String getStatus() {
        return status;
    }

    public int getTime() {
        return time;
    }
}
