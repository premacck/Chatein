package com.prembros.chatein.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.prembros.chatein.data.model.User;

import org.jetbrains.annotations.NotNull;

import static com.prembros.chatein.util.Constants.BLA_BLA;
import static com.prembros.chatein.util.Constants.CURRENT_USER;
import static com.prembros.chatein.util.Constants.CURRENT_USER_ID;

public class SharedPrefs {

//    private SharedPreferences preferences;
//
//    public SharedPrefs(@NotNull Context context) {
//        preferences = context.getSharedPreferences(BLA_BLA, Context.MODE_PRIVATE);
//    }

    private static SharedPreferences getPreferences(@NotNull Context context) {
        return context.getSharedPreferences(BLA_BLA, Context.MODE_PRIVATE);
    }

    public static void saveUser(@NotNull Context context, User user) {
        getPreferences(context)
                .edit()
                .putString(CURRENT_USER, new Gson().toJson(user, new TypeToken<User>(){}.getType()))
                .apply();
    }

    public static void removeSavedUser(Context context) {
        removeSavedUserId(context);
        getPreferences(context).edit().remove(CURRENT_USER).apply();
    }

    @Nullable public static User getUser(@NotNull Context context) {
        String user = getPreferences(context).getString(CURRENT_USER, null);
        if (user != null) return new Gson().fromJson(user, new TypeToken<User>() {}.getType());
        return null;
    }

    public static void saveUserId(@NotNull Context context, String userId) {
        getPreferences(context)
                .edit()
                .putString(CURRENT_USER_ID, userId)
                .apply();
    }

    private static void removeSavedUserId(Context context) {
        getPreferences(context).edit().remove(CURRENT_USER_ID).apply();
    }

    @Nullable public static String getUserId(@NotNull Context context) {
        return getPreferences(context).getString(CURRENT_USER_ID, null);
    }
}