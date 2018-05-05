package com.prembros.chatein.injection.module.app;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.prembros.chatein.injection.scope.AppScope;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

import static com.prembros.chatein.util.Constants.BEFORE_AUTH;
import static com.prembros.chatein.util.Constants.CURRENT_USER_ID;
import static com.prembros.chatein.util.Constants.ROOT;
import static com.prembros.chatein.util.Constants.USERS;

@AppScope
@Module
public class UserModule {

    @Provides @AppScope @Named(BEFORE_AUTH)
    public FirebaseAuth provideFirebaseAuth() {
        return FirebaseAuth.getInstance();
    }

    @Provides @AppScope @Named(BEFORE_AUTH)
    public FirebaseUser provideFirebaseUser(@Named(BEFORE_AUTH) FirebaseAuth auth) {
        return auth.getCurrentUser();
    }

    @Provides @AppScope @Named(CURRENT_USER_ID)
    public String provideCurrentUserId(@Named(BEFORE_AUTH) FirebaseUser user) {
        return user.getUid();
    }

    @Provides @AppScope
    public FirebaseDatabase provideFirebaseDatabase() {
        return FirebaseDatabase.getInstance();
    }

    @Provides @AppScope @Named(ROOT)
    public DatabaseReference provideRootRef(FirebaseDatabase database) {
        return database.getReference();
    }

    @Provides @AppScope @Named(USERS)
    public DatabaseReference provideUsersRef(@Named(ROOT) DatabaseReference reference) {
        return reference.child(USERS);
    }

/*    @Provides @AppScope @Named(CURRENT_USER)
    public DatabaseReference provideCurrentUserRef(@Named(USERS) DatabaseReference reference, @Named(CURRENT_USER_ID) String userId) {
        return reference.child(userId);
    }*/
}