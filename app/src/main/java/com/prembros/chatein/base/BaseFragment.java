package com.prembros.chatein.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.firebase.database.DatabaseReference;
import com.prembros.chatein.ui.main.MainActivity;

import butterknife.Unbinder;

public abstract class BaseFragment extends Fragment {

    protected Unbinder unbinder;
    public RequestManager glide;
    protected DatabaseReference root;
    protected String currentUserId;

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            glide = Glide.with(this);
            root = getParentActivity().getRootRef();
            currentUserId = getParentActivity().getCurrentUserId();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DatabaseReference getRoot() {
        return root;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    @NonNull public MainActivity getParentActivity() throws Exception {
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            return (MainActivity) getActivity();
        } else throw new IllegalAccessException("Fragment not attached to MainActivity");
    }

    public abstract void dataAvailable();

    protected void setVisibility(View view, int visibility) {
        if (view.getVisibility() != visibility) view.setVisibility(visibility);
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) unbinder.unbind();
    }

//    @Override public void onDestroy() {
//        try {
//            if (glide != null) glide.onDestroy();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            super.onDestroy();
//        }
//    }
}