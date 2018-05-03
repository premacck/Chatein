package com.prembros.chatein.data.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.prembros.chatein.R;

import java.util.Objects;

import static com.prembros.chatein.ui.social.ProfileActivity.USER_ID;
import static com.prembros.chatein.util.Constants.FROM_USER_ID;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override public void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "BaseNotifications")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(Objects.requireNonNull(remoteMessage.getNotification()).getTitle())
                    .setContentText(remoteMessage.getNotification().getBody())
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getNotification().getBody()))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            Intent resultIntent = new Intent(remoteMessage.getNotification().getClickAction());
            resultIntent.putExtra(USER_ID, remoteMessage.getData().get(FROM_USER_ID));
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
                    resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            mBuilder.setContentIntent(resultPendingIntent);

            int notificationId = (int) System.currentTimeMillis();
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.notify(notificationId, mBuilder.build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
