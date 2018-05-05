package com.prembros.chatein.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.prembros.chatein.base.ChateinApplication;
import com.prembros.chatein.data.viewmodel.DatabaseViewModel;

import static com.prembros.chatein.ui.auth.StartActivity.launchStartActivity;

/**
 * Activity that handles online status of the current user.
 */
public abstract class DatabaseActivity extends BaseActivity {

    protected DatabaseViewModel viewModel;
    protected FirebaseUser currentUser;
    protected String currentUserId;
    protected DatabaseReference currentUserRef;
    protected boolean started;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            viewModel = DatabaseViewModel.getInstance(ChateinApplication.get(this));
            currentUser = viewModel.getCurrentUser();
            if (currentUser != null) {
                currentUserId = currentUser.getUid();
                currentUserRef = viewModel.getCurrentUserRef();
            }
            else {
                launchStartActivity(this);
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override public void onStart() {
        super.onStart();
        started = true;
    }

    @Override protected void onStop() {
        started = false;
        super.onStop();
    }

    public DatabaseReference getRootRef() {
        return viewModel.getRootRef();
    }

    public DatabaseReference getUsersRef() {
        return viewModel.getUsersRef();
    }

    public DatabaseReference getCurrentUserRef() {
        return viewModel.getCurrentUserRef();
    }

    public DatabaseReference getChatRef() {
        return viewModel.getChatRef();
    }

    public DatabaseReference getMyChatRef() {
        return viewModel.getMyChatRef();
    }

    public DatabaseReference getChatRef(String friendsUserId) {
        return viewModel.getMyChatRef().child(friendsUserId);
    }

    public DatabaseReference getMyFriendRequestsRef() {
        return viewModel.getMyFriendRequestsRef();
    }

    public DatabaseReference getFriendsRef() {
        return viewModel.getFriendsRef();
    }

    public DatabaseReference getMyFriendsRef() {
        return viewModel.getMyFriendsRef();
    }

    public DatabaseReference getMyFriendsRef(String friendsUserId) {
        return viewModel.getMyFriendsRef().child(friendsUserId);
    }

    public DatabaseReference getMyMessagesRef() {
        return viewModel.getMyMessagesRef();
    }

    public DatabaseReference getMessagesRef(String friendsUserId) {
        return getMyMessagesRef().child(friendsUserId);
    }

    public DatabaseReference getNotificationsRef() {
        return viewModel.getNotificationRef();
    }

    public StorageReference getProfileImagesRef() {
        return viewModel.getProfileImagesRef();
    }

    public StorageReference getThumbImagesRef() {
        return viewModel.getThumbImagesRef();
    }
}