package com.example.sveta.taxo.utility;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Sveta on 18.03.2017.
 */

public class AddressesConverter {

    public static String getAddressFromLocation(Context context, LatLng latLng) {
        Geocoder geocoder = new Geocoder(context);
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (Exception e) {}
        return addresses.get(0).getAddressLine(0);
    }

    public static LatLng getLocationFromAddress(Context context, String address) {
        Geocoder geocoder = new Geocoder(context);
        try {
            List<Address> location = geocoder.getFromLocationName(address, 5);
            LatLng latLng = new LatLng(location.get(0).getLatitude(), location.get(0).getLongitude());
            return latLng;

        } catch (Exception e) {
        }
        return null;
    }
}
