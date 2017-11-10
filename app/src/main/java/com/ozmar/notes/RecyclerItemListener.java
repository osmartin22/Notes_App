package com.ozmar.notes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;


public class RecyclerItemListener implements RecyclerView.OnItemTouchListener {

    private final GestureDetector gd;

    public interface RecyclerTouchListener {
        void onClickItem(View view, int position);

        void onLongClickItem(View view, int position);
    }

    public RecyclerItemListener(Context context, final RecyclerView rv,
                                final RecyclerTouchListener listener) {
        gd = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public void onLongPress(MotionEvent e) {
                        View v = rv.findChildViewUnder(e.getX(), e.getY());
                        listener.onLongClickItem(v, rv.getChildAdapterPosition(v));
                    }

                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        View v = rv.findChildViewUnder(e.getX(), e.getY());
                        listener.onClickItem(v, rv.getChildAdapterPosition(v));
                        return false;
                    }
                });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        View child = rv.findChildViewUnder(e.getX(), e.getY());
        return (child != null && gd.onTouchEvent(e));
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}
