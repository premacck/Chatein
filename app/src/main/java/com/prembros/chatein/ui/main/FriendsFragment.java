package com.prembros.chatein.ui.main;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.prembros.chatein.R;
import com.prembros.chatein.base.BaseFragment;
import com.prembros.chatein.data.model.User;
import com.prembros.chatein.util.CustomValueEventListener;
import com.prembros.chatein.util.ViewUtils;

import java.lang.ref.WeakReference;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.prembros.chatein.ui.chat.ChatActivity.launchChatActivity;
import static com.prembros.chatein.util.Constants.FRIENDS;
import static com.prembros.chatein.util.Constants.ONLINE;
import static com.prembros.chatein.util.Constants.USERS;
import static com.prembros.chatein.util.ViewUtils.loadProfilePic;

public class FriendsFragment extends BaseFragment {

    @BindView(R.id.list) RecyclerView recyclerView;
    @BindView(R.id.no_data_placeholder) TextView noDataPlaceholder;

    private DatabaseReference usersDatabase;
    private FirebaseRecyclerAdapter<User, FriendsViewHolder> adapter;

    public FriendsFragment() {}

    @NonNull public static FriendsFragment newInstance() {
        return new FriendsFragment();
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        noDataPlaceholder.setText(R.string.your_friends_appear_here);

        initializeFriendsList();

        return rootView;
    }

    private void initializeFriendsList() {
        try {
            recyclerView.setHasFixedSize(true);
            DatabaseReference friendsDatabase = root.child(FRIENDS).child(currentUserId);
            friendsDatabase.keepSynced(true);
            usersDatabase = root.child(USERS);
            usersDatabase.keepSynced(true);

            FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                    .setQuery(friendsDatabase, User.class)
                    .build();
            adapter = new FirebaseRecyclerAdapter<User, FriendsViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull final User model) {
                    final String userId = getRef(position).getKey();

                    usersDatabase.child(userId).addListenerForSingleValueEvent(new CustomValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            holder.bind(new User(dataSnapshot), userId);
                        }
                    });

                    usersDatabase.child(userId).addValueEventListener(new CustomValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            try {
                                if (dataSnapshot.hasChild(ONLINE)) {
                                    Object isOnline = dataSnapshot.child(ONLINE).getValue();
                                    holder.onlineStatus.setVisibility(
                                            !(isOnline instanceof Long) && Objects.equals(isOnline, "true") ?
                                                    View.VISIBLE : View.GONE);
                                } else holder.onlineStatus.setVisibility(View.GONE);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                @NonNull @Override public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    setVisibility(noDataPlaceholder, GONE);
                    setVisibility(recyclerView, VISIBLE);
                    return new FriendsViewHolder(
                            LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_list, parent, false),
                            glide, getActivity()
                    );
                }
            };
            recyclerView.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.root_layout) LinearLayout layout;
        @BindView(R.id.dp) ImageView dp;
        @BindView(R.id.online) ImageView onlineStatus;
        @BindView(R.id.name) TextView name;
        @BindView(R.id.status) TextView status;

        private final RequestManager glide;
        private final WeakReference<Activity> activity;
        private String userId;

        FriendsViewHolder(View itemView, RequestManager glide, Activity activity) {
            super(itemView);
            this.glide = glide;
            this.activity = new WeakReference<>(activity);
            ButterKnife.bind(this, itemView);
        }

        public void bind(User user, String userId) {
            this.userId = userId;
            loadProfilePic(glide, user.getThumb_image(), dp);
            name.setText(user.getName());
            status.setText(user.getStatus());
        }

        @OnClick(R.id.root_layout) public void openChat() {
            if (userId != null) launchChatActivity(activity.get(), userId);
        }

        @OnClick(R.id.dp) public void openProfile() {
            ViewUtils.openProfile(activity.get(), userId);
        }
    }
}