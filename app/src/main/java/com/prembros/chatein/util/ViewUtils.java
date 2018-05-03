package com.prembros.chatein.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.prembros.chatein.R;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import id.zelory.compressor.Compressor;

import static com.prembros.chatein.ui.account.AccountSettingsActivity.launchAccountSettingsActivity;
import static com.prembros.chatein.ui.social.ProfileActivity.launchProfileActivity;
import static com.prembros.chatein.util.Constants.DEFAULT;

public class ViewUtils {

    public static void disableView(@NotNull View view, boolean setAlpha) {
        view.setEnabled(false);
        view.setClickable(false);
        if (setAlpha) view.setAlpha(0.5f);
    }

    public static void enableView(@NotNull View view) {
        view.setEnabled(true);
        view.setClickable(true);
        view.setAlpha(1.0f);
    }

    public static void hideKeyboard(Activity activity, View view) {
        try {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                //            imm.toggleSoftInput(InputMethodManager.RESULT_UNCHANGED_SHOWN, InputMethodManager.RESULT_HIDDEN);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable public static byte[] compressImage(@NotNull Context context, @NotNull Uri resultUri) {
        try {
            File thumbFile = new File(resultUri.getPath());
            Bitmap thumbBitmap = new Compressor(context)
                    .setMaxWidth(200)
                    .setMaxHeight(200)
                    .setQuality(75)
                    .compressToBitmap(thumbFile);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            return stream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void openProfile(Activity activity, String userId) {
        if (userId != null) {
            if (!Objects.equals(SharedPrefs.getUserId(activity), userId))
                launchProfileActivity(activity, userId);
            else launchAccountSettingsActivity(activity);
        }
    }

    public static void loadProfilePic(RequestManager glide, String url, ImageView imageView) {
        if (url != null && !Objects.equals(url, DEFAULT)) {
            glide.load(url)
                    .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.ic_add_user_small))
                    .into(imageView);
        } else imageView.setImageResource(R.drawable.ic_add_user_small);
    }
}