package com.prembros.chatein.injection.module.db;

import com.google.firebase.database.DatabaseReference;
import com.prembros.chatein.injection.scope.DbScope;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

import static com.prembros.chatein.util.Constants.CHAT;
import static com.prembros.chatein.util.Constants.CURRENT_USER;
import static com.prembros.chatein.util.Constants.CURRENT_USER_ID;
import static com.prembros.chatein.util.Constants.FRIENDS;
import static com.prembros.chatein.util.Constants.FRIEND_REQUESTS;
import static com.prembros.chatein.util.Constants.MESSAGES;
import static com.prembros.chatein.util.Constants.MY_CHAT;
import static com.prembros.chatein.util.Constants.MY_FRIENDS;
import static com.prembros.chatein.util.Constants.NOTIFICATIONS;
import static com.prembros.chatein.util.Constants.ROOT;
import static com.prembros.chatein.util.Constants.USERS;

@DbScope
@Module(includes = {AuthModule.class, StorageModule.class})
public class DatabaseModule {

//    @Provides @DbScope
//    public FirebaseDatabase provideFirebaseDatabase() {
//        return FirebaseDatabase.getInstance();
//    }
//
//    @Provides @DbScope @Named(ROOT)
//    public DatabaseReference provideRootRef(FirebaseDatabase database) {
//        return database.getReference();
//    }
//
//    /**
//     * @return All users references.
//     */
//    @Provides @DbScope @Named(USERS)
//    public DatabaseReference provideUsersRef(@Named(ROOT) DatabaseReference reference) {
//        return reference.child(USERS);
//    }

    /**
     * @return Current user reference.
     */
    @Provides @DbScope @Named(CURRENT_USER)
    public DatabaseReference provideCurrentUserRef(@Named(USERS) DatabaseReference reference, @Named(CURRENT_USER_ID) String userId) {
        return reference.child(userId);
    }

    /**
     * @return All Chat references for updating in "seen" field.
     */
    @Provides @DbScope @Named(CHAT)
    public DatabaseReference provideChatRef(@Named(ROOT) DatabaseReference reference, @Named(CURRENT_USER_ID) String userId) {
        return reference.child(CHAT);
    }

    /**
     * @return Current user's chat reference.
     */
    @Provides @DbScope @Named(MY_CHAT)
    public DatabaseReference provideMyChatRef(@Named(CHAT) DatabaseReference reference, @Named(CURRENT_USER_ID) String userId) {
        return reference.child(userId);
    }

    /**
     * @return Current user's friend requests reference.
     */
    @Provides @DbScope @Named(FRIEND_REQUESTS)
    public DatabaseReference provideFriendRequestsRef(@Named(ROOT) DatabaseReference reference, @Named(CURRENT_USER_ID) String userId) {
        return reference.child(FRIEND_REQUESTS).child(userId);
    }


    /**
     * @return All friend references.
     */
    @Provides @DbScope @Named(FRIENDS)
    public DatabaseReference provideFriendsRef(@Named(ROOT) DatabaseReference reference) {
        return reference.child(FRIENDS);
    }

    /**
     * @return Current user's friends reference.
     */
    @Provides @DbScope @Named(MY_FRIENDS)
    public DatabaseReference provideMyFriendsRef(@Named(FRIENDS) DatabaseReference reference, @Named(CURRENT_USER_ID) String userId) {
        return reference.child(userId);
    }

    /**
     * @return Current user's messages reference.
     */
    @Provides @DbScope @Named(MESSAGES)
    public DatabaseReference provideMessagesRef(@Named(ROOT) DatabaseReference reference, @Named(CURRENT_USER_ID) String userId) {
        return reference.child(MESSAGES).child(userId);
    }

    /**
     * @return All notification references.
     */
    @Provides @DbScope @Named(NOTIFICATIONS)
    public DatabaseReference provideNotificationsRef(@Named(ROOT) DatabaseReference reference) {
        return reference.child(NOTIFICATIONS);
    }
}