package com.prembros.chatein.base;

import android.app.Activity;
import android.app.Application;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.prembros.chatein.injection.component.AppComponent;
import com.prembros.chatein.injection.component.DaggerAppComponent;
import com.prembros.chatein.injection.module.app.ApplicationModule;
import com.prembros.chatein.util.database.CustomValueEventListener;

import org.jetbrains.annotations.NotNull;

import static com.prembros.chatein.util.Constants.ONLINE;

public class ChateinApplication extends Application {

    private AppComponent appComponent;

    @Override public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        try {
            appComponent = DaggerAppComponent.builder()
                    .applicationModule(new ApplicationModule(this))
                    .build();

            if (appComponent.currentUser() != null) {
//            Reference to current user
                appComponent.usersRef().child(appComponent.currentUserId())
                        .addValueEventListener(new CustomValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null) {
    //                        on Firebase disconnection, automatically set this value to the last seen time.
                            appComponent.usersRef().child(appComponent.currentUserId()).child(ONLINE)
                                    .onDisconnect().setValue(ServerValue.TIMESTAMP);
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

    public static ChateinApplication get(@NotNull Activity activity) {
        return (ChateinApplication) activity.getApplication();
    }
}