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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.prembros.chatein.R;
import com.prembros.chatein.base.BaseActivity;
import com.prembros.chatein.data.model.Friend;
import com.prembros.chatein.data.model.User;
import com.prembros.chatein.util.ViewUtils;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.LayoutInflater.from;
import static com.prembros.chatein.util.Constants.USERS;

public class UsersActivity extends BaseActivity {

    @BindView(R.id.users_toolbar) Toolbar toolbar;
    @BindView(R.id.users_list) RecyclerView recyclerView;

    private FirebaseRecyclerAdapter<User, UsersViewHolder> adapter;

    public static void launchUsersActivity(@NotNull Context from) {
        from.startActivity(new Intent(from, UsersActivity.class));
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        unbinder = ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DatabaseReference usersDatabase = FirebaseDatabase.getInstance().getReference().child(USERS);

        recyclerView.setHasFixedSize(true);
        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(usersDatabase, User.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<User, UsersViewHolder>(options) {
            @Override protected void onBindViewHolder(@NotNull UsersViewHolder holder, int position, @NonNull User model) {
                holder.bind(model, getRef(position).getKey());
            }

            @NonNull @Override public UsersViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
                return new UsersViewHolder(
                        from(parent.getContext()).inflate(R.layout.item_user_list, parent, false),
                        glide, UsersActivity.this
                );
            }
        };
        recyclerView.setAdapter(adapter);
    }

    @Override protected void onStart() {
        super.onStart();
        if (adapter != null) adapter.startListening();
    }

    @Override protected void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }

    @Override protected void onDestroy() {
        if (adapter != null) adapter = null;
        super.onDestroy();
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.dp) ImageView dp;
        @BindView(R.id.name) TextView name;
        @BindView(R.id.status) TextView status;

        private final RequestManager glide;
        private String userId;
        private final WeakReference<Activity> activity;

        UsersViewHolder(View itemView, RequestManager glide, Activity activity) {
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

        public void bind(Friend user, String userId) {
            bind(user.getUser(), userId);
        }

        @OnClick(R.id.root_layout) public void openProfile() {
            ViewUtils.openProfile(activity.get(), userId);
        }
    }
}