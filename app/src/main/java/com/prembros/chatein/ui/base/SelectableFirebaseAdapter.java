package com.prembros.chatein.ui.base;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.util.HashMap;
import java.util.Map;

@SuppressLint("UseSparseArrays")
public abstract class SelectableFirebaseAdapter<T, VH extends RecyclerView.ViewHolder>
        extends FirebaseRecyclerAdapter<T, VH> implements ActionMode.Callback {

    protected boolean multiSelect;
    protected Map<Integer, String> selectedItems;
    protected ActionMode mode;

    public SelectableFirebaseAdapter(@NonNull FirebaseRecyclerOptions<T> options) {
        super(options);
        multiSelect = false;
        selectedItems = new HashMap<>();
    }

    @Override public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        this.mode = mode;
        return true;
    }

    @Override public void onDestroyActionMode(ActionMode mode) {
        this.mode = mode;
        multiSelect = false;
        selectedItems.clear();
        notifyDataSetChanged();
        mode.finish();
    }
}