package com.prembros.chatein.data.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.prembros.chatein.R;

import java.lang.ref.WeakReference;
import java.util.Objects;

import static com.prembros.chatein.ui.social.ProfileActivity.USER_ID;
import static com.prembros.chatein.util.Annotations.ChatType.IMAGE;
import static com.prembros.chatein.util.Constants.DATA_URL;
import static com.prembros.chatein.util.Constants.FROM_USER_ID;
import static com.prembros.chatein.util.Constants.TYPE;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override public void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            String body = Objects.requireNonNull(remoteMessage.getNotification()).getBody();
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "BaseNotifications")
                    .setSmallIcon(R.drawable.ic_notification_icon)
                    .setContentTitle(Objects.requireNonNull(remoteMessage.getNotification()).getTitle())
                    .setContentText(body)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            if (body != null && body.length() > 30) {
                mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getNotification().getBody()));
            }

            if (Objects.equals(remoteMessage.getData().get(TYPE), IMAGE)) {
                new LoadImageTask(this, remoteMessage.getData().get(DATA_URL), mBuilder);
            }

            Intent resultIntent = new Intent(remoteMessage.getNotification().getClickAction());
            resultIntent.putExtra(USER_ID, remoteMessage.getData().get(FROM_USER_ID));
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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

    private static class LoadImageTask extends AsyncTask<Void, Void, Bitmap> {

        private WeakReference<MyFirebaseMessagingService> ref;
        private String dataUrl;
        private NotificationCompat.Builder mBuilder;

        private LoadImageTask(MyFirebaseMessagingService ref, String dataUrl, NotificationCompat.Builder mBuilder) {
            this.ref = new WeakReference<>(ref);
            this.dataUrl = dataUrl;
            this.mBuilder = mBuilder;
        }

        @Override protected Bitmap doInBackground(Void... voids) {
            try {
                return Glide.with(ref.get())
                        .asBitmap()
                        .load(dataUrl)
                        .submit(100, 100)
                        .get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                mBuilder.setLargeIcon(bitmap);
            }
        }
    }
}
