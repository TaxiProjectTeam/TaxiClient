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

    public SwipeHelper(int dragDirs, int swipeDirs) {
        super(dragDirs, swipeDirs);
    }

    public SwipeHelper(AddressLineAdapter adapter) {
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        adapter.deleteAddress(viewHolder.getAdapterPosition());
        ((AddressLineAdapter.EditTypeViewHolder) viewHolder).editText.setText("");
        if (MainActivity.markers.get(viewHolder) != null) {
            MainActivity.markers.get(viewHolder).remove();
            MainActivity.markers.remove(viewHolder);
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
