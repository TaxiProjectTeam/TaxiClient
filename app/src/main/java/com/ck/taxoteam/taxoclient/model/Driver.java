package com.ck.taxoteam.taxoclient.model;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Sveta on 20.02.2017.
 */
@IgnoreExtraProperties
public class Driver {
    private String name;
    private String phoneNumber;
    private String carModel;
    private String carNumber;
    private String carColor;

    public Driver() {
    }

    public Driver(String name, String phoneNumber, String carModel, String carNumber, String carColor) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.carModel = carModel;
        this.carNumber = carNumber;
        this.carColor = carColor;
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

    public String getCarColor() {
        return carColor;
    }

    public void setName(String name) {
        this.name = name;
    }

}
