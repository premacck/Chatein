package com.prembros.chatein.injection.component;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.prembros.chatein.base.ChateinApplication;
import com.prembros.chatein.injection.module.app.ApplicationModule;
import com.prembros.chatein.injection.module.app.ContextModule;
import com.prembros.chatein.injection.module.app.UserModule;
import com.prembros.chatein.injection.scope.AppScope;

import javax.inject.Named;

import dagger.Component;

import static com.prembros.chatein.util.Constants.BEFORE_AUTH;
import static com.prembros.chatein.util.Constants.CURRENT_USER_ID;
import static com.prembros.chatein.util.Constants.USERS;

@AppScope
@Component(
        modules = {
                ContextModule.class,
                ApplicationModule.class,
                UserModule.class
        }
)
public interface AppComponent {

    ChateinApplication application();

    @Named(BEFORE_AUTH) FirebaseUser currentUser();
    @Named(CURRENT_USER_ID) String currentUserId();
    @Named(USERS) DatabaseReference usersRef();

    DbComponent.Builder dbComponent();
}