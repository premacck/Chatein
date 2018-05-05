package com.prembros.chatein.ui.main;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.prembros.chatein.R;
import com.prembros.chatein.base.BaseFragment;
import com.prembros.chatein.data.model.LastChat;
import com.prembros.chatein.ui.main.adapter.MainChatsAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.prembros.chatein.util.Constants.TIME_STAMP;

public class ChatsFragment extends BaseFragment {

    @BindView(R.id.list) RecyclerView recyclerView;
    @BindView(R.id.no_data_placeholder) TextView noDataPlaceholder;

    private FirebaseRecyclerAdapter<LastChat, MainChatsAdapter.ChatViewHolder> adapter;

    public ChatsFragment() {}

    @NonNull public static ChatsFragment newInstance() {
        return new ChatsFragment();
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        noDataPlaceholder.setText(R.string.your_chats_appear_here);
        initializeRecyclerView();
        return rootView;
    }

    private void initializeRecyclerView() {
        try {
            LinearLayoutManager manager = new LinearLayoutManager(getContext());
            manager.setReverseLayout(true);
            manager.setStackFromEnd(true);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(manager);

            FirebaseRecyclerOptions<LastChat> options = new FirebaseRecyclerOptions.Builder<LastChat>()
                    .setQuery(getParentActivity().getMyChatRef().orderByChild(TIME_STAMP), LastChat.class).build();

            adapter = new MainChatsAdapter(options, this);
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