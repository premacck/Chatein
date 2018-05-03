package com.prembros.chatein.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.prembros.chatein.base.BaseActivity;

import java.util.Objects;

import static com.prembros.chatein.StartActivity.launchStartActivity;
import static com.prembros.chatein.util.Constants.ONLINE;
import static com.prembros.chatein.util.Constants.USERS;

/**
 * Activity that handles online status of the current user.
 */
public abstract class DatabaseActivity extends BaseActivity {

    protected FirebaseUser currentUser;
    protected DatabaseReference userDatabase;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                userDatabase = FirebaseDatabase.getInstance().getReference().child(USERS)
                        .child(Objects.requireNonNull(currentUser).getUid());
            }
            else {
                launchStartActivity(this);
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}