package com.ck.taxoteam.taxoclient.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.ck.taxoteam.taxoclient.R;
import com.ck.taxoteam.taxoclient.model.ModelAddressLine;

import java.util.ArrayList;

/**
 * Created by Sveta on 01.02.2017.
 */

public class AddressLineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<ModelAddressLine> dataSet;
    private Context context;
    private OnFocusItemListener onFocusItemListener;

    public class TextTypeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView textView;

        public TextTypeViewHolder(View itemView) {
            super(itemView);

            this.textView = (TextView) itemView.findViewById(R.id.type_text);
            textView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            dataSet.add(getItemCount() - 1, new ModelAddressLine(ModelAddressLine.EDIT_TYPE, context.getString(R.string.destination_address)));
            notifyItemInserted(getItemCount() - 2);
        }
    }

    public class EditTypeViewHolder extends RecyclerView.ViewHolder implements View.OnFocusChangeListener {
        public AutoCompleteTextView editText;
        public OnFocusItemListener listener;

        public EditTypeViewHolder(View itemView, OnFocusItemListener onFocusItemListener) {
            super(itemView);

            this.listener = onFocusItemListener;
            this.editText = (AutoCompleteTextView) itemView.findViewById(R.id.type_edit);
            editText.setOnFocusChangeListener(this);
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (listener != null)
                listener.onItemFocus(getAdapterPosition());
        }
    }

    public AddressLineAdapter(ArrayList<ModelAddressLine> data, Context context) {
        this.dataSet = data;
        this.context = context;
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
}

