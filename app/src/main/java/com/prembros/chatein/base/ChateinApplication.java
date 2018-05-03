package com.prembros.chatein.base;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.prembros.chatein.util.CustomValueEventListener;

import java.util.Objects;

import static com.prembros.chatein.util.Constants.ONLINE;
import static com.prembros.chatein.util.Constants.USERS;

public class ChateinApplication extends Application {

    private FirebaseAuth auth;
    private DatabaseReference userDatabase;

    @Override public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        try {
            auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null) {
//            Reference to current user
                userDatabase = FirebaseDatabase.getInstance().getReference().child(USERS)
                        .child(Objects.requireNonNull(auth.getCurrentUser()).getUid());
                userDatabase.addValueEventListener(new CustomValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null) {
    //                        on Firebase disconnection, automatically set this value to false.
                            userDatabase.child(ONLINE).onDisconnect().setValue(ServerValue.TIMESTAMP);
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}