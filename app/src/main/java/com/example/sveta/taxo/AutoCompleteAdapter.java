package com.example.sveta.taxo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;

import java.util.concurrent.TimeUnit;

/**
 * Created by Sveta on 12.02.2017.
 */

public class AutoCompleteAdapter extends ArrayAdapter<AutoCompletePlace> implements Filterable{
    private GoogleApiClient googleApiClient;

    public AutoCompleteAdapter(Context context) {
        super(context, 0);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            holder.text = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.text.setText(getItem(position).getDescription());

        return convertView;
    }

    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
    }

    private class ViewHolder {
        TextView text;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                if (googleApiClient == null || !googleApiClient.isConnected()) {
                    Toast.makeText(getContext(), "Not connected", Toast.LENGTH_SHORT).show();
                    return null;
                }

                clear();

                displayPredictiveResults(constraint.toString());

                return null;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                notifyDataSetChanged();
            }
        };
    }

    private void displayPredictiveResults(String query)
    {
        Places.GeoDataApi.getAutocompletePredictions(
                googleApiClient,
                query,
                null,
                new AutocompleteFilter.Builder().setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS).build())
                .setResultCallback (new ResultCallback<AutocompletePredictionBuffer>() {
                            @Override
                            public void onResult(@NonNull AutocompletePredictionBuffer buffer) {
                                if (buffer == null)
                                    return;

                                if (buffer.getStatus().isSuccess()) {
                                    for (AutocompletePrediction prediction : buffer) {
                                        add (new AutoCompletePlace(prediction.getPlaceId(), prediction.toString()));
                                    }
                                }

                                buffer.release();
                            }
                        }, 60, TimeUnit.SECONDS);
    }
}
