package com.example.sveta.taxo.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
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
    private ArrayList<HashMap<String, Double>> toCoords;
    private String price;
    private String additionalComment;

    public Order() {
    }

    public Order(HashMap<String, Double> fromCoords, ArrayList<HashMap<String, Double>> toCoords, String price, String additionalComment) {
        this.fromCoords = fromCoords;
        this.toCoords = toCoords;
        this.price = price;
        this.additionalComment = additionalComment;

        this.driverPos = new HashMap<>();
        this.driverPos.put("latitude", 0.0);
        this.driverPos.put("longitude", 0.0);

        this.driverId = "";
        this.time = 0;
        this.status = "free";
    }

    public HashMap<String, Double> getFromCoords() {
        return fromCoords;
    }

    public ArrayList<HashMap<String, Double>> getToCoords() {
        return toCoords;
    }

    public String getPrice() {
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
