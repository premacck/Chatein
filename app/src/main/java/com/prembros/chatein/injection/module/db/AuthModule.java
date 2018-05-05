package com.prembros.chatein.injection.module.db;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.prembros.chatein.injection.scope.DbScope;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

import static com.prembros.chatein.util.Constants.AFTER_AUTH;

@DbScope
@Module
public class AuthModule {

    @Provides @DbScope @Named(AFTER_AUTH)
    public FirebaseAuth provideFirebaseAuth() {
        return FirebaseAuth.getInstance();
    }

    @Provides @DbScope @Named(AFTER_AUTH)
    public FirebaseUser provideFirebaseUser(@Named(AFTER_AUTH) FirebaseAuth auth) {
        return auth.getCurrentUser();
    }

/*    @Provides @DbScope @Named(CURRENT_USER_ID)
    public String provideCurrentUserId(@Named(AFTER_AUTH) FirebaseUser user) {
        return user.getUid();
    }*/
}