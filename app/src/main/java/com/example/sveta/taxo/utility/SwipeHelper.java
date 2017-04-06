package com.example.sveta.taxo.utility;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.example.sveta.taxo.activity.MainActivity;
import com.example.sveta.taxo.adapter.AddressLineAdapter;
import com.example.sveta.taxo.model.ModelAddressLine;

/**
 * Created by Sveta on 02.02.2017.
 */

public class SwipeHelper extends ItemTouchHelper.SimpleCallback {

    AddressLineAdapter adapter;
    MainActivity activity;

    public SwipeHelper(int dragDirs, int swipeDirs) {
        super(dragDirs, swipeDirs);
    }

    public SwipeHelper(AddressLineAdapter adapter, MainActivity activity) {
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
        this.activity = activity;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int pos = viewHolder.getAdapterPosition();
        adapter.deleteAddress(pos);
        ((AddressLineAdapter.EditTypeViewHolder) viewHolder).editText.setText("");
        if (activity.markers.get(viewHolder) != null) {
            activity.markers.get(viewHolder).remove();
            activity.routes.get(viewHolder).remove();
            activity.getRoute();
        }
    }

    @Override
    public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if (viewHolder.getItemViewType() == ModelAddressLine.TEXT_TYPE
                || viewHolder.getAdapterPosition() == 0
                || viewHolder.getAdapterPosition() == adapter.getItemCount() - 2)
            return 0;
        return super.getSwipeDirs(recyclerView, viewHolder);
    }
}
