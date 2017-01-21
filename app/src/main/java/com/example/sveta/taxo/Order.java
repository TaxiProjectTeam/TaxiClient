package com.example.sveta.taxo;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Sveta on 03.01.2017.
 */
@IgnoreExtraProperties
public class Order {

    private String startingStreet;
    private String startingHouse;
    private String startingEntrance;
    private String destinationStreet;
    private String destinationHouse;

    public Order() {
    }

    public Order(String startingStreet, String startingHouse, String startingEntrance, String destinationStreet, String destinationHouse) {
        this.startingStreet = startingStreet;
        this.startingHouse = startingHouse;
        this.startingEntrance = startingEntrance;
        this.destinationStreet = destinationStreet;
        this.destinationHouse = destinationHouse;
    }

    public String getStartingStreet() {
        return startingStreet;
    }

    public String getStartingHouse() {
        return startingHouse;
    }

    public String getStartingEntrance() {
        return startingEntrance;
    }

    public String getDestinationStreet() {
        return destinationStreet;
    }

    public String getDestinationHouse() {
        return destinationHouse;
    }

    public void setStartingStreet(String startingStreet) {
        this.startingStreet = startingStreet;
    }

    public void setStartingHouse(String startingHouse) {
        this.startingHouse = startingHouse;
    }

    public void setStartingEntrance(String startingEntrance) {
        this.startingEntrance = startingEntrance;
    }

    public void setDestinationStreet(String destinationStreet) {
        this.destinationStreet = destinationStreet;
    }

    public void setDestinationHouse(String destinationHouse) {
        this.destinationHouse = destinationHouse;
    }
}
