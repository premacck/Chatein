package com.prembros.chatein.util;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;

/**
 *
 * Created by Prem$ on 2/12/2018.
 */

public class CustomLinearLayoutManager extends LinearLayoutManager {

    private boolean isScrollEnabled = true;
//    private int extraLayoutSpace;

    /**
     * Disable predictive animations. There is a bug in RecyclerView which causes views that
     * are being reloaded to pull invalid ViewHolders from the internal recycler stack if the
     * adapter size has decreased since the ViewHolder was recycled.
     */
    @Override public boolean supportsPredictiveItemAnimations() {
        return false;
    }

//    @Override protected int getExtraLayoutSpace(RecyclerView.State state) {
//        return extraLayoutSpace != -1 ? extraLayoutSpace : super.getExtraLayoutSpace(state);
//    }

    public CustomLinearLayoutManager(Context context) {
        super(context);
//        extraLayoutSpace =  getDeviceHeight(context);
    }

    public CustomLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
//        extraLayoutSpace =  getDeviceHeight(context);
    }

    public CustomLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
//        extraLayoutSpace =  getDeviceHeight(context);
    }

    public boolean isScrollEnabled() {
        return isScrollEnabled;
    }

    public void setScrollEnabled(boolean flag) {
        this.isScrollEnabled = flag;
    }

    @Override public boolean canScrollVertically() {
        return isScrollEnabled && super.canScrollVertically();
    }
}
