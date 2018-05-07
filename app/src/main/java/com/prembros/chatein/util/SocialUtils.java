package com.prembros.chatein.util;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.prembros.chatein.R;
import com.prembros.chatein.data.model.UpdateRequest;

import org.jetbrains.annotations.Contract;

import java.text.DateFormat;
import java.util.Date;

import static com.prembros.chatein.util.Annotations.NotificationType.ACCEPT;
import static com.prembros.chatein.util.Annotations.NotificationType.REQUEST;
import static com.prembros.chatein.util.Annotations.SocialState.ARE_FRIENDS;
import static com.prembros.chatein.util.Annotations.SocialState.NOT_FRIENDS;
import static com.prembros.chatein.util.Annotations.SocialState.REQUEST_SENT;
import static com.prembros.chatein.util.Constants.FRIENDS_;
import static com.prembros.chatein.util.Constants.FRIEND_REQUESTS_;
import static com.prembros.chatein.util.Constants.FROM;
import static com.prembros.chatein.util.Constants.NOTIFICATIONS;
import static com.prembros.chatein.util.Constants.NOTIFICATIONS_;
import static com.prembros.chatein.util.Constants.TYPE;
import static com.prembros.chatein.util.Constants._DATE;
import static com.prembros.chatein.util.Constants._REQUEST_TYPE;

public class SocialUtils {

    private String currentUserId;
    private String friendUserId;
    private SocialListener listener;
    private DatabaseReference root;
    private DatabaseReference notificationRef;
    private String notificationId;

    private SocialUtils(String currentUserId, String friendUserId, SocialListener listener) {
        this.currentUserId = currentUserId;
        this.friendUserId = friendUserId;
        this.listener = listener;
        root = FirebaseDatabase.getInstance().getReference();
        notificationRef = root.child(NOTIFICATIONS).child(friendUserId).push();
        notificationId = notificationRef.getKey();
    }

    @NonNull public static SocialUtils get(String currentUserId, String friendUserId, SocialListener socialListener) {
        return new SocialUtils(currentUserId, friendUserId, socialListener);
    }

    public void sendFriendRequest() {
        listener.actionStarted();
        UpdateRequest.forDatabase(root)
                .put(FRIEND_REQUESTS_ + getMyBranch() + _REQUEST_TYPE, Annotations.RequestType.SENT)
                .put(FRIEND_REQUESTS_ + getFriendsBranch() + _REQUEST_TYPE, Annotations.RequestType.RECEIVED)
                .put(NOTIFICATIONS_ + friendUserId + "/" + notificationId,
                        UpdateRequest.forMapOnly()
                                .put(FROM, currentUserId)
                                .put(TYPE, REQUEST)
                                .get())
                .update(getListener(REQUEST_SENT));
    }

    public void acceptRequest() {
        listener.actionStarted();
        final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
        UpdateRequest.forDatabase(root)
                .put(FRIENDS_ + getMyBranch() + _DATE, currentDate)
                .put(FRIENDS_ + getFriendsBranch() + _DATE, currentDate)
                .put(FRIEND_REQUESTS_ + getMyBranch(), null)
                .put(FRIEND_REQUESTS_ + getFriendsBranch(), null)
                .put(NOTIFICATIONS_ + friendUserId + "/" + notificationId,
                        UpdateRequest.forMapOnly()
                                .put(FROM, currentUserId)
                                .put(TYPE, ACCEPT)
                                .get())
                .update(getListener(ARE_FRIENDS));
    }

    public void unFriend(Context context) {
        showAlertDialog(context, R.string.un_friend_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.actionStarted();
                UpdateRequest.forDatabase(root)
                        .put(FRIENDS_ + getMyBranch(), null)
                        .put(FRIENDS_ + getFriendsBranch(), null)
                        .update(getListener(NOT_FRIENDS));
            }
        });
    }

    public void cancelFriendRequest(Context context, boolean isCancel) {
        showAlertDialog(context, isCancel ? R.string.cancel_request_confirm :
                        R.string.delete_request_confirm,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.actionStarted();
                        UpdateRequest.forDatabase(root)
                                .put(FRIEND_REQUESTS_ + getMyBranch(), null)
                                .put(FRIEND_REQUESTS_ + getFriendsBranch(), null)
                                .update(getListener(NOT_FRIENDS));
                    }
                });
    }

    private void showAlertDialog(Context context, @StringRes int message, DialogInterface.OnClickListener positiveListener) {
        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setMessage(message)
                .setPositiveButton(R.string.yes, positiveListener)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.actionCompleted();
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @NonNull @Contract(pure = true)
    private DatabaseReference.CompletionListener getListener(final int state) {
        return new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                try {
                    listener.actionCompleted();
                    if (databaseError != null) {
                        listener.error(databaseError.getMessage());
                        Log.e("DATABASE ERROR", databaseError.getDetails());
                    } else
                        listener.updateState(state);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @NonNull @Contract(pure = true)
    private String getMyBranch() {
        return currentUserId + "/" + friendUserId;
    }

    @NonNull @Contract(pure = true)
    private String getFriendsBranch() {
        return friendUserId + "/" + currentUserId;
    }
}