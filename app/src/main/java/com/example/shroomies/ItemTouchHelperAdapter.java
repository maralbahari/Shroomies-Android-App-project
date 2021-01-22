package com.example.shroomies;

public interface ItemTouchHelperAdapter {
    void onItemMove(int fromPosition, int toPosition);
    void onItemSwiped(int position);
}
