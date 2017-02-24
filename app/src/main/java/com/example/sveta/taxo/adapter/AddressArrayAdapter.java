package com.example.sveta.taxo.adapter;

import android.content.Context;
import android.location.Address;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * Created by Sveta on 24.02.2017.
 */

public class AddressArrayAdapter extends ArrayAdapter<String> implements Filterable {
    private GoogleApiClient googleApiClient;
    private AutocompleteFilter autocompleteFilter;
    private LatLngBounds bounds;
    private ArrayList<String> resultList;

    public AddressArrayAdapter(Context context, int resource, LatLngBounds bounds, AutocompleteFilter autocompleteFilter) {
        super(context, resource);
        this.bounds = bounds;
        this.autocompleteFilter = autocompleteFilter;
    }

    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
        if (googleApiClient == null || !googleApiClient.isConnected())
            this.googleApiClient = null;
        else
            this.googleApiClient = googleApiClient;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return resultList.get(position);
    }

    private ArrayList<String> getPredictions (CharSequence constraint) {
        if (googleApiClient != null) {
            PendingResult<AutocompletePredictionBuffer> results =
                    Places.GeoDataApi
                            .getAutocompletePredictions(googleApiClient, constraint.toString(),
                                    bounds, autocompleteFilter);
            AutocompletePredictionBuffer autocompletePredictions = results
                    .await(60, TimeUnit.SECONDS);
            final Status status = autocompletePredictions.getStatus();
            if (!status.isSuccess()) {
                autocompletePredictions.release();
                return null;
            }

            Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
            ArrayList resultList = new ArrayList<>(autocompletePredictions.getCount());
            while (iterator.hasNext()) {
                AutocompletePrediction prediction = iterator.next();
                String[] array = prediction.getSecondaryText(null).toString().split(", ");
                if (array[0].equals("Черкаси")) {
                    resultList.add(prediction.getPrimaryText(null));
                }
            }
            // Buffer release
            autocompletePredictions.release();
            return resultList;
        }
        return null;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint != null) {
                    resultList = getPredictions(constraint);
                    if (resultList != null) {
                        results.values = resultList;
                        results.count = resultList.size();
                    }
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }
}
