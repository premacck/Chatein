package com.prembros.chatein.ui.base;

import android.support.annotation.NonNull;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.util.ArrayList;

public abstract class SelectableFirebaseAdapter<T, VH extends RecyclerView.ViewHolder>
        extends FirebaseRecyclerAdapter<T, VH> implements ActionMode.Callback {

    protected boolean multiSelect;
    protected ArrayList<Integer> selectedItems;

    public SelectableFirebaseAdapter(@NonNull FirebaseRecyclerOptions<T> options) {
        super(options);
        multiSelect = false;
        selectedItems = new ArrayList<>();
    }

    @Override public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override public void onDestroyActionMode(ActionMode mode) {
        multiSelect = false;
        selectedItems.clear();
        notifyDataSetChanged();
    }
}