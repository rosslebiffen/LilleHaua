package com.example.jonet.lillehaua.Helper;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import com.example.jonet.lillehaua.ViewHolder.CartViewHolder;

import com.example.jonet.lillehaua.Interface.RecyclerItemTouchHelperListener;

public class RecyclerItemTouchHelper extends ItemTouchHelper.SimpleCallback{

    private RecyclerItemTouchHelperListener listener;


    public RecyclerItemTouchHelper(int dragDirs, int swipeDirs, RecyclerItemTouchHelperListener listener) {
        super(dragDirs, swipeDirs);
        this.listener = listener;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        if(listener != null)
            listener.onSwiped(viewHolder,direction,viewHolder.getAdapterPosition());
    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        View foregroundView = ((CartViewHolder)viewHolder).
        super.clearView(recyclerView, viewHolder);
    }
}
