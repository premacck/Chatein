package com.prembros.chatein.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.prembros.chatein.R;
import com.prembros.chatein.data.model.User;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;

import static com.prembros.chatein.util.Constants.USERS;

public class CommonUtils {

    private boolean isConnected(@NotNull Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo.isConnected();
        }
        return false;
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context context, @StringRes int message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showErrorToast(Context context) {
        Toast.makeText(context, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
    }

    public static void makeSnackBar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    public static void makeSnackBar(View view, @StringRes int message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    public static void makeErrorSnackBar(View view) {
        Snackbar.make(view, R.string.something_went_wrong, Snackbar.LENGTH_SHORT).show();
    }

    @NonNull public static String randomString() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(20);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    public static void saveUserLocally(final Context context) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        SharedPrefs.saveUserId(context, userId);
        FirebaseDatabase.getInstance().getReference().child(USERS).child(userId).addValueEventListener(new CustomValueEventListener() {
            @Override public void onDataChange(DataSnapshot dataSnapshot) {
                User user = new User(dataSnapshot);
                SharedPrefs.saveUser(context, user);
            }
        });
    }
}