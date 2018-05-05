package com.prembros.chatein.ui.main;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.prembros.chatein.R;
import com.prembros.chatein.base.BaseFragment;
import com.prembros.chatein.data.model.Request;
import com.prembros.chatein.ui.main.adapter.MainRequestsAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class RequestsFragment extends BaseFragment {

    @BindView(R.id.list) RecyclerView recyclerView;
    @BindView(R.id.no_data_placeholder) TextView noDataPlaceholder;

    private FirebaseRecyclerAdapter<Request, MainRequestsAdapter.RequestViewHolder> adapter;

    public RequestsFragment() {}

    @NonNull public static RequestsFragment newInstance() {
        return new RequestsFragment();
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        noDataPlaceholder.setText(R.string.friend_requests_appear_here);

        initializeRequestsList();

        return rootView;
    }

    private void initializeRequestsList() {
        try {
            recyclerView.setHasFixedSize(true);

            FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>()
                    .setQuery(getParentActivity().getMyFriendRequestsRef(), Request.class)
                    .build();

            adapter = new MainRequestsAdapter(options, this, currentUserId);
            recyclerView.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override public void dataAvailable() {
        setVisibility(noDataPlaceholder, GONE);
        setVisibility(recyclerView, VISIBLE);
    }

    @Override public void onStart() {
        super.onStart();
        if (adapter != null) adapter.startListening();
    }

    @Override public void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }

    @Override public void onDetach() {
        if (adapter != null) adapter = null;
        super.onDetach();
    }
}