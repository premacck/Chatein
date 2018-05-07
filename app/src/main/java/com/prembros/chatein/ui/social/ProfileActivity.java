package com.prembros.chatein.ui.social;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.prembros.chatein.R;
import com.prembros.chatein.data.model.User;
import com.prembros.chatein.ui.base.DatabaseActivity;
import com.prembros.chatein.util.Annotations;
import com.prembros.chatein.util.Annotations.SocialState;
import com.prembros.chatein.util.SocialListener;
import com.prembros.chatein.util.SocialUtils;
import com.prembros.chatein.util.database.CustomValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.prembros.chatein.ui.chat.ChatActivity.launchChatActivity;
import static com.prembros.chatein.ui.social.FriendsListActivity.launchFriendsActivity;
import static com.prembros.chatein.util.Annotations.SocialState.ARE_FRIENDS;
import static com.prembros.chatein.util.Annotations.SocialState.NOT_FRIENDS;
import static com.prembros.chatein.util.Annotations.SocialState.REQUEST_RECEIVED;
import static com.prembros.chatein.util.Annotations.SocialState.REQUEST_SENT;
import static com.prembros.chatein.util.CommonUtils.makeSnackBar;
import static com.prembros.chatein.util.Constants.DEFAULT;
import static com.prembros.chatein.util.Constants.FROM_USER_ID;
import static com.prembros.chatein.util.Constants.REQUEST_TYPE;
import static com.prembros.chatein.util.ViewUtils.disableView;
import static com.prembros.chatein.util.ViewUtils.enableView;
import static java.util.Objects.requireNonNull;

public class ProfileActivity extends DatabaseActivity implements SocialListener {

    public static final String USER_ID = "user_id";

    @BindView(R.id.layout_progress) ProgressBar progressBar;
    @BindView(R.id.dp) ImageView dp;
    @BindView(R.id.name) TextView name;
    @BindView(R.id.status) TextView status;
    @BindView(R.id.friend_count) Button friendCount;
    @BindView(R.id.action_btn) Button actionButton;
    @BindView(R.id.open_chat_btn) ImageButton openChatButton;
    @BindView(R.id.delete_request_btn) ImageView deleteRequestButton;

    @SocialState private int currentState;
    private String friendUserId;
    private SocialUtils socialUtils;
    private CustomValueEventListener userInfoListener;
    private CustomValueEventListener friendCountListener;
    private ValueEventListener actionButtonStateListener;

    public static void launchProfileActivity(@NotNull Context from, String userId) {
        Intent intent = new Intent(from, ProfileActivity.class);
        intent.putExtra(USER_ID, userId);
        from.startActivity(intent);
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        unbinder = ButterKnife.bind(this);

        friendUserId = getIntent().getStringExtra(USER_ID);
        try {
//            FOR NOTIFICATIONS
            if (friendUserId == null)
                friendUserId = Objects.requireNonNull(getIntent().getExtras()).getString(FROM_USER_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        socialUtils = SocialUtils.get(currentUserId, friendUserId, this);

        progressBar.setVisibility(View.VISIBLE);
        updateUI();
    }

    @Override protected void onDestroy() {
        getUsersRef().child(friendUserId).removeEventListener(userInfoListener);
        getFriendsRef().child(friendUserId).removeEventListener(friendCountListener);
        getMyFriendRequestsRef().removeEventListener(actionButtonStateListener);
        super.onDestroy();
    }

    private void updateUI() {
        try {
            userInfoListener = new CustomValueEventListener() {
                @Override public void onDataChange(DataSnapshot dataSnapshot) {
                    if (started && dataSnapshot != null && dp != null && name != null && status != null) {
                        User user = new User(dataSnapshot);
                        glide.load(user.getProfile_image())
                                .apply(RequestOptions.placeholderOf(R.drawable.ic_user)
                                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                                .transition(new DrawableTransitionOptions().crossFade())
                                .listener(new RequestListener<Drawable>() {
                                    @Override public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                                          Target<Drawable> target, boolean isFirstResource) {
                                        progressBar.setVisibility(View.GONE);
                                        return false;
                                    }

                                    @Override public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                                                                             DataSource dataSource, boolean isFirstResource) {
                                        progressBar.setVisibility(View.GONE);
                                        return false;
                                    }
                                })
                                .into(dp);

                        name.setText(user.getName());
                        String statusText = user.getStatus();
                        status.setText(!Objects.equals(statusText, DEFAULT) ? statusText : null);
                        status.setVisibility(View.VISIBLE);

                        updateActionButton();
                    }
                }
            };
            getUsersRef().child(friendUserId).addValueEventListener(userInfoListener);

            friendCountListener = new CustomValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String friendCountText = dataSnapshot.getChildrenCount() + " friends";
                    friendCount.setText(friendCountText);
                    getFriendsRef().child(friendUserId).removeEventListener(this);
                }
            };
            getFriendsRef().child(friendUserId).addValueEventListener(friendCountListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateActionButton() {
        try {
            actionButtonStateListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (started) {
                        progressBar.setVisibility(View.GONE);
                        if (dataSnapshot.hasChild(friendUserId)) {
                            String requestType = requireNonNull(dataSnapshot
                                    .child(friendUserId).child(REQUEST_TYPE).getValue()).toString();
                            if (Objects.equals(requestType, Annotations.RequestType.RECEIVED)) {
                                //                            We have recieved a request from the friend
                                updateState(REQUEST_RECEIVED);
                            } else if (Objects.equals(requestType, Annotations.RequestType.SENT)){
                                //                            We have sent a request to the friend
                                updateState(REQUEST_SENT);
                            }
                        } else {
                            //                        Check if we are already friends or not
                            getMyFriendsRef().addListenerForSingleValueEvent(new CustomValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    //                                check if friend if there in out friends list
                                    updateState(dataSnapshot.hasChild(friendUserId) ? ARE_FRIENDS : NOT_FRIENDS);
                                }
                            });
                        }
                    }
                }

                @Override public void onCancelled(DatabaseError databaseError) {
                    progressBar.setVisibility(View.GONE);
                }
            };
            getMyFriendRequestsRef().addListenerForSingleValueEvent(actionButtonStateListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.back_btn) public void exit() {
        onBackPressed();
    }

    @OnClick(R.id.action_btn) public void socialAction() {
        switch (currentState) {
            case NOT_FRIENDS:
                socialUtils.sendFriendRequest();
                break;
            case REQUEST_SENT:
                socialUtils.cancelFriendRequest(this, true);
                break;
            case REQUEST_RECEIVED:
                socialUtils.acceptRequest();
                break;
            case ARE_FRIENDS:
                socialUtils.unFriend(this);
                break;
        }
    }

    @OnClick(R.id.delete_request_btn) public void deleteFriendRequest() {
        socialUtils.cancelFriendRequest(this, false);
    }

    @OnClick(R.id.friend_count) public void viewFriendsOfUser() {
        launchFriendsActivity(this, friendUserId);
    }

    @OnClick(R.id.open_chat_btn) public void openChatOfUser() {
        launchChatActivity(this, friendUserId);
    }

    @Override public void updateState(@SocialState int state) {
        if (started && actionButton != null && deleteRequestButton != null) {
            if (actionButton.getVisibility() != View.VISIBLE) actionButton.setVisibility(View.VISIBLE);
            deleteRequestButton.setVisibility(state == REQUEST_RECEIVED ? View.VISIBLE : View.GONE);
            currentState = state;
            switch (state) {
                case ARE_FRIENDS:
                    actionButton.setText(R.string.friends);
                    openChatButton.setVisibility(View.VISIBLE);
                    actionButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check, 0, 0, 0);
                    break;
                case NOT_FRIENDS:
                    actionButton.setText(R.string.add_friend);
                    openChatButton.setVisibility(View.GONE);
                    actionButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person_add, 0, 0, 0);
                    break;
                case REQUEST_RECEIVED:
                    actionButton.setText(R.string.accept);
                    openChatButton.setVisibility(View.VISIBLE);
                    actionButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    break;
                case REQUEST_SENT:
                    actionButton.setText(R.string.cancel_request);
                    openChatButton.setVisibility(View.GONE);
                    actionButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    break;
            }
        }
    }

    @Override public void error(String message) {
        makeSnackBar(actionButton, message);
    }

    @Override public void actionStarted() {
        disableView(actionButton, false);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override public void actionCompleted() {
        enableView(actionButton);
        progressBar.setVisibility(View.GONE);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }
}