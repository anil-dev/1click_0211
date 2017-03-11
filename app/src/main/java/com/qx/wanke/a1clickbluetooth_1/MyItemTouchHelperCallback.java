package com.qx.wanke.a1clickbluetooth_1;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by Administrator on 2017/3/1 0001.
 */

public class MyItemTouchHelperCallback extends ItemTouchHelper.Callback {
    private final ItemTouchHelperAdapter itemTouchHelperAdapter;
    public MyItemTouchHelperCallback(ItemTouchHelperAdapter adapter){
        itemTouchHelperAdapter=adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }
    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }
    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags=ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT;
        int swipeFlags=ItemTouchHelper.DOWN;
        return makeMovementFlags(dragFlags, swipeFlags);
    }
    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            itemTouchHelperAdapter.onItemMove(viewHolder.getAdapterPosition(),target.getAdapterPosition());
            return true;
    }
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        itemTouchHelperAdapter.onItemDismiss(viewHolder.getAdapterPosition());
    }
}
