package com.prembros.chatein.data.viewmodel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.prembros.chatein.base.ChateinApplication;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static com.prembros.chatein.util.Constants.AFTER_AUTH;
import static com.prembros.chatein.util.Constants.CHAT;
import static com.prembros.chatein.util.Constants.CURRENT_USER;
import static com.prembros.chatein.util.Constants.CURRENT_USER_ID;
import static com.prembros.chatein.util.Constants.FRIENDS;
import static com.prembros.chatein.util.Constants.FRIEND_REQUESTS;
import static com.prembros.chatein.util.Constants.MESSAGES;
import static com.prembros.chatein.util.Constants.MY_CHAT;
import static com.prembros.chatein.util.Constants.MY_FRIENDS;
import static com.prembros.chatein.util.Constants.NOTIFICATIONS;
import static com.prembros.chatein.util.Constants.PROFILE_IMAGES;
import static com.prembros.chatein.util.Constants.ROOT;
import static com.prembros.chatein.util.Constants.THUMBS;
import static com.prembros.chatein.util.Constants.USERS;

@Singleton
public class DatabaseViewModel {

    private static volatile DatabaseViewModel INSTANCE;

    @Inject @Named(AFTER_AUTH) FirebaseAuth auth;
    @Inject @Named(AFTER_AUTH) FirebaseUser currentUser;
    @Inject @Named(CURRENT_USER_ID) String currentUserId;

    @Inject @Named(ROOT) DatabaseReference rootRef;

    @Inject @Named(USERS) DatabaseReference usersRef;
    @Inject @Named(CURRENT_USER) DatabaseReference currentUserRef;

    @Inject @Named(CHAT) DatabaseReference chatRef;
    @Inject @Named(MY_CHAT) DatabaseReference myChatRef;

    @Inject @Named(FRIEND_REQUESTS) DatabaseReference myFriendRequestsRef;

    @Inject @Named(FRIENDS) DatabaseReference friendsRef;
    @Inject @Named(MY_FRIENDS) DatabaseReference myFriendsRef;

    @Inject @Named(MESSAGES) DatabaseReference myMessagesRef;

    @Inject @Named(NOTIFICATIONS) DatabaseReference notificationRef;

    @Inject @Named(PROFILE_IMAGES) StorageReference profileImagesRef;
    @Inject @Named(THUMBS) StorageReference thumbImagesRef;

    //region Singleton handlers
    private DatabaseViewModel(ChateinApplication application) {
        if (INSTANCE != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        } else {
            application.getAppComponent().dbComponent().build().inject(this);
            usersRef.keepSynced(true);
            currentUserRef.keepSynced(true);
        }
    }

    public static DatabaseViewModel getInstance(ChateinApplication application) {
        if (INSTANCE == null) {
            synchronized (DatabaseViewModel.class) {
                if (INSTANCE == null) INSTANCE = new DatabaseViewModel(application);
            }
        }
        return INSTANCE;
    }

    /**
     * Making {@link DatabaseViewModel} safe from serialize and deserialize operation.
     */
    @SuppressWarnings("unused") protected DatabaseViewModel readResolve(ChateinApplication app) {
        return getInstance(app);
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
    //endregion

    public FirebaseAuth getAuth() {
        return auth;
    }

    public FirebaseUser getCurrentUser() {
        return currentUser;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public DatabaseReference getRootRef() {
        return rootRef;
    }

    public DatabaseReference getUsersRef() {
        return usersRef;
    }

    public DatabaseReference getCurrentUserRef() {
        return currentUserRef;
    }

    public DatabaseReference getChatRef() {
        return chatRef;
    }

    public DatabaseReference getMyChatRef() {
        return myChatRef;
    }

    public DatabaseReference getMyFriendRequestsRef() {
        return myFriendRequestsRef;
    }

    public DatabaseReference getFriendsRef() {
        return friendsRef;
    }

    public DatabaseReference getMyFriendsRef() {
        return myFriendsRef;
    }

    public DatabaseReference getMyMessagesRef() {
        return myMessagesRef;
    }

    public DatabaseReference getNotificationRef() {
        return notificationRef;
    }

    public StorageReference getProfileImagesRef() {
        return profileImagesRef;
    }

    public StorageReference getThumbImagesRef() {
        return thumbImagesRef;
    }
}