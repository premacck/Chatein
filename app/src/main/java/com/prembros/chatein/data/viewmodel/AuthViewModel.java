package com.prembros.chatein.data.viewmodel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.prembros.chatein.base.ChateinApplication;

import static com.prembros.chatein.util.Constants.USERS;

public class AuthViewModel {

    private static volatile AuthViewModel INSTANCE;

    private FirebaseAuth auth;
    private DatabaseReference users;

    private AuthViewModel() {
        if (INSTANCE != null)
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        else {
            auth = FirebaseAuth.getInstance();
            users = FirebaseDatabase.getInstance().getReference().child(USERS);
        }
    }

    public static AuthViewModel getInstance() {
        if (INSTANCE == null) {
            synchronized (AuthViewModel.class) {
                if (INSTANCE == null) INSTANCE = new AuthViewModel();
            }
        }
        return INSTANCE;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public FirebaseAuth refreshAuth() {
        auth = FirebaseAuth.getInstance();
        return auth;
    }

    public DatabaseReference getUsers() {
        return users;
    }

    /**
     * Making {@link AuthViewModel} safe from serialize and deserialize operation.
     */
    @SuppressWarnings("unused") protected AuthViewModel readResolve(ChateinApplication app) {
        return getInstance();
    }
}