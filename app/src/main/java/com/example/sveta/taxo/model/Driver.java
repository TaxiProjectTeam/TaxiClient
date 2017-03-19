package com.example.sveta.taxo.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;

/**
 * Created by Sveta on 20.02.2017.
 */
@IgnoreExtraProperties
public class Driver {
    private String name;
    private String phoneNumber;
    private String carModel;
    private String carNumber;
    private HashMap<String, Double> driverPos;

    public Driver() {
    }

    public Driver(String name, String phoneNumber, String carModel, String carNumber, HashMap<String, Double> driverPos) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.carModel = carModel;
        this.carNumber = carNumber;
        this.driverPos = driverPos;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getCarModel() {
        return carModel;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public HashMap<String, Double> getDriverPos() {
        return driverPos;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public void setDriverPos(HashMap<String, Double> driverPos) {
        this.driverPos = driverPos;
    }
}
