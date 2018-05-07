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
import com.prembros.chatein.util.SharedPrefs;

import java.util.List;
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
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            String fromUserId = remoteMessage.getData().get(FROM_USER_ID);
            String type = remoteMessage.getData().get(TYPE);
            String dataUrl = remoteMessage.getData().get(DATA_URL);
            String clickAction = remoteMessage.getData().get("click_action");

            SharedPrefs.saveNotification(getApplicationContext(), fromUserId, body);

            final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "BaseNotifications")
                    .setSmallIcon(R.drawable.ic_notification_icon)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            if (body != null && body.length() > 30) {
                mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(body));
            }

            if (Objects.equals(type, IMAGE)) {
                Bitmap bitmap = Glide.with(this)
                        .asBitmap()
                        .load(dataUrl)
                        .submit(200, 200)
                        .get();
                mBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap).bigLargeIcon(null));
                mBuilder.setLargeIcon(bitmap);
            } else if (Objects.equals(type, TEXT)) {
                NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle()
                        .setBigContentTitle(title);
                List<String> allNotifications = SharedPrefs.getAllNotifications(getApplicationContext(), fromUserId);
                if (allNotifications != null) {
                    for (String s : allNotifications) {
                        style.addLine(s);
                    }
                    mBuilder.setStyle(style);
                }
            }

            Intent resultIntent = new Intent(clickAction);
            resultIntent.putExtra(USER_ID, fromUserId);
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
                    resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            mBuilder.setContentIntent(resultPendingIntent);

            int notificationId = NotificationUtil.getId(fromUserId);
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