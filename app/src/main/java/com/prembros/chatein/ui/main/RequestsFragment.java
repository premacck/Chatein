package com.prembros.chatein.ui.main;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.prembros.chatein.R;
import com.prembros.chatein.base.BaseFragment;
import com.prembros.chatein.data.model.Request;
import com.prembros.chatein.data.model.User;
import com.prembros.chatein.util.Annotations;
import com.prembros.chatein.util.CustomValueEventListener;
import com.prembros.chatein.util.SocialListener;
import com.prembros.chatein.util.SocialUtils;

import java.lang.ref.WeakReference;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.prembros.chatein.util.Annotations.SocialState.ARE_FRIENDS;
import static com.prembros.chatein.util.Annotations.SocialState.NOT_FRIENDS;
import static com.prembros.chatein.util.Annotations.SocialState.REQUEST_RECEIVED;
import static com.prembros.chatein.util.Annotations.SocialState.REQUEST_SENT;
import static com.prembros.chatein.util.CommonUtils.makeSnackBar;
import static com.prembros.chatein.util.Constants.FRIEND_REQUESTS;
import static com.prembros.chatein.util.Constants.USERS;
import static com.prembros.chatein.util.ViewUtils.disableView;
import static com.prembros.chatein.util.ViewUtils.enableView;
import static com.prembros.chatein.util.ViewUtils.loadProfilePic;

public class RequestsFragment extends BaseFragment {

    @BindView(R.id.list) RecyclerView recyclerView;
    @BindView(R.id.no_data_placeholder) TextView noDataPlaceholder;

    private FirebaseRecyclerAdapter<Request, RequestViewHolder> adapter;
    private DatabaseReference usersDatabase;

    public RequestsFragment() {}

    @NonNull public static RequestsFragment newInstance() {
        return new RequestsFragment();
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        noDataPlaceholder.setText(R.string.friend_requests_appear_here);

        initializeRecyclerView();

        return rootView;
    }

    private void initializeRecyclerView() {
        recyclerView.setHasFixedSize(true);
        DatabaseReference query = root.child(FRIEND_REQUESTS).child(currentUserId);
        query.keepSynced(true);
        usersDatabase = root.child(USERS);
        usersDatabase.keepSynced(true);

        FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(query, Request.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<Request, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull Request model) {
                final String userId = getRef(position).getKey();
                holder.bind(model, userId, currentUserId);

                usersDatabase.child(userId).addListenerForSingleValueEvent(new CustomValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        holder.bind(new User(dataSnapshot));
                    }
                });
            }

            @NonNull @Override public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                setVisibility(noDataPlaceholder, GONE);
                setVisibility(recyclerView, VISIBLE);
                return new RequestViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request, parent, false),
                        glide,
                        getActivity()
                );
            }
        };
        recyclerView.setAdapter(adapter);
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

    static class RequestViewHolder extends RecyclerView.ViewHolder implements SocialListener {

        @BindView(R.id.dp) ImageView dp;
        @BindView(R.id.name) TextView nameView;
        @BindView(R.id.accept) Button acceptButton;
        @BindView(R.id.decline) Button declineButton;
        @BindView(R.id.progress_bar) ProgressBar progressBar;

        private final RequestManager glide;
        private final WeakReference<Activity> activity;
        private SocialUtils socialUtils;

        RequestViewHolder(View itemView, RequestManager glide, Activity activity) {
            super(itemView);
            this.glide = glide;
            this.activity = new WeakReference<>(activity);
            ButterKnife.bind(this, itemView);
        }

        public void bind(Request model, String friendUserId, String currentUserId) {
            socialUtils = SocialUtils.get(currentUserId, friendUserId, this);
            try {
                switch (Objects.requireNonNull(model.getRequest_type())) {
                    case Annotations.RequestType.SENT:
                        declineButton.setVisibility(GONE);
                        acceptButton.setVisibility(VISIBLE);
                        acceptButton.setText(R.string.cancel_request);
                        break;
                    case Annotations.RequestType.RECEIVED:
                        declineButton.setVisibility(VISIBLE);
                        acceptButton.setVisibility(VISIBLE);
                        acceptButton.setText(R.string.accept);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void bind(User user) {
            try {
                loadProfilePic(glide, user.getThumb_image(), dp);
                nameView.setText(user.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @OnClick(R.id.accept) public void requestAccepted() {
            if (acceptButton.getText().equals(activity.get().getString(R.string.accept))) {
                socialUtils.acceptRequest();
            } else if (acceptButton.getText().equals(activity.get().getString(R.string.decline))) {
                socialUtils.cancelFriendRequest(activity.get(), true);
            } else if (acceptButton.getText().equals(activity.get().getString(R.string.friends))) {
                socialUtils.unFriend(activity.get());
            } else if (acceptButton.getText().equals(activity.get().getString(R.string.add_friend))) {
                socialUtils.sendFriendRequest();
            }
        }

        @OnClick(R.id.decline) public void requestDeclined() {
            socialUtils.cancelFriendRequest(activity.get(), false);
        }

        @Override public  void actionStarted() {
            disableView(acceptButton, false);
            disableView(declineButton, false);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override public void actionCompleted() {
            enableView(acceptButton);
            enableView(declineButton);
            progressBar.setVisibility(View.GONE);
        }

        @Override public void updateState(@Annotations.SocialState int state) {
            acceptButton.setVisibility(VISIBLE);
            switch (state) {
                case ARE_FRIENDS:
                    declineButton.setVisibility(GONE);
                    acceptButton.setText(R.string.friends);
                    break;
                case NOT_FRIENDS:
                    declineButton.setVisibility(GONE);
                    acceptButton.setText(R.string.add_friend);
                    break;
                case REQUEST_RECEIVED:
                    declineButton.setVisibility(VISIBLE);
                    acceptButton.setText(R.string.accept);
                    break;
                case REQUEST_SENT:
                    declineButton.setVisibility(GONE);
                    acceptButton.setText(R.string.cancel_request);
                    break;
            }
        }

        @Override public void error(String message) {
            makeSnackBar(acceptButton, message);
        }
    }
}