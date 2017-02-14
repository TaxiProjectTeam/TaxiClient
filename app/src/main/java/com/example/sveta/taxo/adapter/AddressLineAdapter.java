package com.example.sveta.taxo.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.example.sveta.taxo.AutoCompleteAdapter;
import com.example.sveta.taxo.AutoCompletePlace;
import com.example.sveta.taxo.R;
import com.example.sveta.taxo.model.ModelAddressLine;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;

import java.util.ArrayList;

/**
 * Created by Sveta on 01.02.2017.
 */

public class AddressLineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<ModelAddressLine> dataSet;
    private Context context;
    private AutoCompleteAdapter googleAdapter;
    private OnFocusItemListener onFocusItemListener;
    private GoogleApiClient googleApiClient;

    public class TextTypeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView textView;

        public TextTypeViewHolder(View itemView) {
            super(itemView);

            this.textView = (TextView) itemView.findViewById(R.id.type_text);
            textView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            dataSet.add(getItemCount() - 1, new ModelAddressLine(ModelAddressLine.EDIT_TYPE, "Куди"));
            notifyItemInserted(getItemCount() - 2);
        }
    }

    public class EditTypeViewHolder extends RecyclerView.ViewHolder implements View.OnFocusChangeListener, AdapterView.OnItemClickListener{
        public AutoCompleteTextView editText;
        public OnFocusItemListener listener;

        public EditTypeViewHolder(View itemView, OnFocusItemListener onFocusItemListener) {
            super(itemView);

            this.listener = onFocusItemListener;
            this.editText = (AutoCompleteTextView) itemView.findViewById(R.id.type_edit);

            googleAdapter = new AutoCompleteAdapter(context);
            editText.setAdapter(googleAdapter);
            editText.setOnFocusChangeListener(this);
            editText.setOnItemClickListener(this);
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (listener != null)
                listener.onItemFocus(getAdapterPosition());
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            AutoCompletePlace place = (AutoCompletePlace) parent.getItemAtPosition( position );
            findPlaceById(place.getId());
        }
    }

    public AddressLineAdapter(ArrayList<ModelAddressLine> data, GoogleApiClient googleApiClient, AutoCompleteAdapter adapter, Context context) {
        this.dataSet = data;
        this.googleAdapter = adapter;
        this.context = context;
        this.googleApiClient = googleApiClient;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case ModelAddressLine.TEXT_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.text_view, parent, false);
                return new TextTypeViewHolder(view);
            case ModelAddressLine.EDIT_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.edit_text, parent, false);
                return new EditTypeViewHolder(view, onFocusItemListener);
        }
        return null;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemViewType(int position) {

        switch (dataSet.get(position).type) {
            case 0:
                return ModelAddressLine.TEXT_TYPE;
            case 1:
                return ModelAddressLine.EDIT_TYPE;
            default:
                return -1;
        }


    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int listPosition) {
        ModelAddressLine object = dataSet.get(listPosition);
        if (object != null) {
            switch (object.type) {
                case ModelAddressLine.TEXT_TYPE:
                    TextView textView = ((TextTypeViewHolder) holder).textView;
                    textView.setText(object.text);
                    break;
                case ModelAddressLine.EDIT_TYPE:
                    AutoCompleteTextView editText = ((EditTypeViewHolder) holder).editText;
                    editText.setHint(object.text);
                    editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            onFocusItemListener.onItemFocus(listPosition);
                        }
                    });
                    break;

            }
        }

    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public void setOnFocusItemListener(OnFocusItemListener listener) {
        this.onFocusItemListener = listener;
    }

    public void deleteAddress(final int position){
        dataSet.remove(position);
        notifyItemRemoved(position);
    }

    private void findPlaceById(String id) {
        if (TextUtils.isEmpty(id) || googleApiClient == null || !googleApiClient.isConnected())
            return;

        Places.GeoDataApi.getPlaceById(googleApiClient, id).setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {
                if(places.getStatus().isSuccess()) {
                    Place place = places.get(0);
                    Log.d("TAG", place.getAddress().toString());
                    googleAdapter.clear();
                }

                //Release the PlaceBuffer to prevent a memory leak
                places.release();
            }
        } );
    }
}

