package com.prembros.chatein.ui.social;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.prembros.chatein.R;
import com.prembros.chatein.data.model.Friend;
import com.prembros.chatein.data.model.User;
import com.prembros.chatein.ui.base.DatabaseActivity;
import com.prembros.chatein.util.CustomValueEventListener;
import com.prembros.chatein.util.ViewUtils;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.LayoutInflater.from;
import static com.prembros.chatein.ui.social.ProfileActivity.USER_ID;

public class FriendsListActivity extends DatabaseActivity {

    @BindView(R.id.main_toolbar) Toolbar toolbar;
    @BindView(R.id.users_list) RecyclerView recyclerView;

    private FirebaseRecyclerAdapter<Friend, FriendsViewHolder> adapter;
    private String friendUserId;

    public static void launchFriendsActivity(@NotNull Context from, String friendUserId) {
        Intent intent = new Intent(from, FriendsListActivity.class);
        intent.putExtra(USER_ID, friendUserId);
        from.startActivity(intent);
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        unbinder = ButterKnife.bind(this);

        if (getIntent() != null) friendUserId = getIntent().getStringExtra(USER_ID);

        if (friendUserId != null) {
            setSupportActionBar(toolbar);
            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.friends);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            recyclerView.setHasFixedSize(true);
            FirebaseRecyclerOptions<Friend> options = new FirebaseRecyclerOptions.Builder<Friend>()
                    .setQuery(viewModel.getFriendsRef().child(friendUserId), Friend.class)
                    .build();
            adapter = new FirebaseRecyclerAdapter<Friend, FriendsViewHolder>(options) {
                @Override protected void onBindViewHolder(@NotNull final FriendsViewHolder holder, int position, @NonNull final Friend model) {
                    final String userId = getRef(position).getKey();
                    getUsersRef().child(userId).addValueEventListener(new CustomValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            holder.bind(new User(dataSnapshot), userId);
                        }
                    });
                }

                @NonNull @Override public FriendsViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
                    return new FriendsViewHolder(
                            from(parent.getContext()).inflate(R.layout.item_user_list, parent, false),
                            glide, FriendsListActivity.this
                    );
                }
            };
            recyclerView.setAdapter(adapter);
        }
    }

    @Override public void onStart() {
        super.onStart();
        if (friendUserId != null && adapter != null) adapter.startListening();
    }

    @Override protected void onStop() {
        if (friendUserId != null && adapter != null) adapter.stopListening();
        super.onStop();
    }

    @Override protected void onDestroy() {
        if (adapter != null) adapter = null;
        super.onDestroy();
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.dp) ImageView dp;
        @BindView(R.id.name) TextView name;
        @BindView(R.id.status) TextView status;

        private final RequestManager glide;
        private String userId;
        private final WeakReference<Activity> activity;

        FriendsViewHolder(View itemView, RequestManager glide, Activity activity) {
            super(itemView);
            this.glide = glide;
            this.activity = new WeakReference<>(activity);
            ButterKnife.bind(this, itemView);
        }

        public void bind(User user, String userId) {
            this.userId = userId;
            glide.load(user.getThumb_image() != null ? user.getThumb_image() : user.getProfile_image())
                    .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.ic_add_user_small))
                    .into(dp);
            name.setText(user.getName());
            status.setText(user.getStatus());
        }

        @OnClick(R.id.root_layout) public void openProfile() {
            ViewUtils.openProfile(activity.get(), userId);
        }
    }
}