package com.example.sveta.taxo.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Sveta on 03.01.2017.
 */
@IgnoreExtraProperties
public class Order implements Parcelable {

    private String id;
    private HashMap<String, Double> fromCoords;
    private HashMap<String, Double> driverPos;
    private String driverId;
    private String status;
    private int time;
    private ArrayList<HashMap<String, Double>> toCoords;
    private String price;
    private String additionalComment;

    public static final Parcelable.Creator<Order> CREATOR = new Parcelable.Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel source) {
            return new Order(source);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };

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

    protected Order(Parcel in) {
        this.id = in.readString();
        this.additionalComment = in.readString();
        this.fromCoords = in.readHashMap(HashMap.class.getClassLoader());
        this.driverPos = in.readHashMap(HashMap.class.getClassLoader());
        this.toCoords = new ArrayList<>();
        in.readList(this.toCoords, HashMap.class.getClassLoader());
        this.price = in.readString();
        this.driverId = in.readString();
        this.status = in.readString();
        this.time = in.readInt();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.additionalComment);
        dest.writeMap(this.fromCoords);
        dest.writeMap(this.driverPos);
        dest.writeList(this.toCoords);
        dest.writeString(this.price);
        dest.writeString(this.driverId);
        dest.writeString(this.status);
        dest.writeInt(this.time);
    }
}
