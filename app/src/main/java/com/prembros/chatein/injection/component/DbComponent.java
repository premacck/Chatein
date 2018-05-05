package com.prembros.chatein.injection.component;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.prembros.chatein.data.viewmodel.DatabaseViewModel;
import com.prembros.chatein.injection.module.db.DatabaseModule;
import com.prembros.chatein.injection.scope.DbScope;

import javax.inject.Named;

import dagger.Subcomponent;

import static com.prembros.chatein.util.Constants.AFTER_AUTH;
import static com.prembros.chatein.util.Constants.CHAT;
import static com.prembros.chatein.util.Constants.CURRENT_USER;
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

@DbScope
@Subcomponent(modules = {DatabaseModule.class})
public interface DbComponent {

    @Named(AFTER_AUTH) FirebaseAuth auth();
    @Named(AFTER_AUTH) FirebaseUser currentUser();
//    @Named(CURRENT_USER_ID) String currentUserId();

    @Named(ROOT) DatabaseReference rootRef();

    @Named(USERS) DatabaseReference usersRef();
    @Named(CURRENT_USER) DatabaseReference currentUserRef();

    @Named(CHAT) DatabaseReference chatRef();
    @Named(MY_CHAT) DatabaseReference myChatRef();

    @Named(FRIEND_REQUESTS) DatabaseReference friendRequestsRef();

    @Named(FRIENDS) DatabaseReference friendsRef();
    @Named(MY_FRIENDS) DatabaseReference myFriendsRef();

    @Named(MESSAGES) DatabaseReference messagesRef();

    @Named(NOTIFICATIONS) DatabaseReference notificationRef();

    @Named(PROFILE_IMAGES) StorageReference profileImagesRef();
    @Named(THUMBS) StorageReference thumbImagesRef();

    void inject(DatabaseViewModel databaseViewModel);

    @Subcomponent.Builder interface Builder {
        DbComponent build();
    }
}