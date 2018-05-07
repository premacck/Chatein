package com.prembros.chatein.data.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.prembros.chatein.R;
import com.prembros.chatein.util.NotificationUtil;

import java.util.Objects;

import static com.prembros.chatein.ui.social.ProfileActivity.USER_ID;
import static com.prembros.chatein.util.Annotations.ChatType.IMAGE;
import static com.prembros.chatein.util.Annotations.ChatType.TEXT;
import static com.prembros.chatein.util.Constants.DATA_URL;
import static com.prembros.chatein.util.Constants.FROM_USER_ID;
import static com.prembros.chatein.util.Constants.TYPE;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private NotificationManager manager;

    @Override public void onMessageReceived(final RemoteMessage remoteMessage) {
        try {
//            String body = Objects.requireNonNull(remoteMessage.getNotification()).getBody();
            String body = remoteMessage.getData().get("body");

            final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "BaseNotifications")
                    .setSmallIcon(R.drawable.ic_notification_icon)
                    .setContentTitle(remoteMessage.getData().get("title"))
//                    .setContentTitle(Objects.requireNonNull(remoteMessage.getNotification()).getTitle())
                    .setContentText(body)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            if (body != null && body.length() > 30) {
                mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getData().get("body")));
            }

            if (Objects.equals(remoteMessage.getData().get(TYPE), IMAGE)) {
                Bitmap bitmap = Glide.with(this)
                        .asBitmap()
                        .load(remoteMessage.getData().get(DATA_URL))
                        .submit(200, 200)
                        .get();
                mBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap).bigLargeIcon(null));
                mBuilder.setLargeIcon(bitmap);
            } else if (Objects.equals(remoteMessage.getData().get(TYPE), TEXT)) {
                mBuilder.setStyle(new NotificationCompat.InboxStyle()
                        .setBigContentTitle(remoteMessage.getData().get("title"))
                        .addLine(body));
            }

            Intent resultIntent = new Intent(remoteMessage.getData().get("click_action"));
            resultIntent.putExtra(USER_ID, remoteMessage.getData().get(FROM_USER_ID));
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
                    resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            mBuilder.setContentIntent(resultPendingIntent);

            int notificationId = NotificationUtil.getId(remoteMessage.getData().get(FROM_USER_ID));
            Log.i("NOTIFICATION_ID", String.valueOf(notificationId));
            manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            createNotificationChannel();
            if (manager != null) {
                manager.notify(notificationId, mBuilder.build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Messages";
            String description = "Message notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("BaseNotifications", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}