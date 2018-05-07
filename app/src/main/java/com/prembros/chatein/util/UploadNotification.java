package com.prembros.chatein.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.prembros.chatein.util.Annotations.ChatType.IMAGE;

public class UploadNotification {

    private static int NOTIFICATION_ID = 1111;

    private static NotificationManager mNotificationManager;
    private static NotificationCompat.Builder builder;
    private static String chatType;
    private static String name;
    private static UploadNotification uploadNotification;

    public static UploadNotification get(Context context, String to, String chatType) {
        UploadNotification.chatType = chatType;
        if(uploadNotification == null)
            uploadNotification = new UploadNotification(context, to);

        return uploadNotification;
    }

    public static void begin(Context context, String to, String chatType) {
        UploadNotification.chatType = chatType;
        if(uploadNotification == null)
            uploadNotification = new UploadNotification(context, to);
    }

    private UploadNotification(@NotNull Context context, String to) {
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        name = to;
        builder = new NotificationCompat.Builder(context, "Uploads")
                .setContentTitle("Sending " + (Objects.equals(chatType, IMAGE) ? "image" : "file") + " to " + to)
                .setSmallIcon(android.R.drawable.stat_sys_upload)
                .setProgress(100, 0, false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(false);
        createNotificationChannel();
    }

    public static void update(int percent) {
        try {
            builder
                    .setContentTitle("Sending " + (Objects.equals(chatType, IMAGE) ? "image" : "file") + " to " + name)
                    .setContentText(percent + "%")
                    .setSmallIcon(android.R.drawable.stat_sys_upload)
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setProgress(100, percent, false);

            mNotificationManager.notify(NOTIFICATION_ID, builder.build());

            if (percent == 100) finish();
        } catch (Exception e) {
            Log.e("Error Notification.", e.getMessage() + ".....");
            e.printStackTrace();
        }
    }

    private static void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "File uploads";
            String description = "File upload notifications";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel("Uploads", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(channel);
            }
        }
    }

//    public static void failUploadNotification(/*int percentage, String fileName*/) {
//        Log.e("downloadSize", "failed notification...");
//
//        if (builder != null) {
//            /* if (percentage < 100) {*/
//            builder.setContentText("Uploading Failed")
//                    //.setContentTitle(fileName)
//                    .setSmallIcon(android.R.drawable.stat_sys_upload_done)
//                    .setOngoing(false);
//            mNotificationManager.notify(NOTIFICATION_ID, builder.build());
//        /*} else {
//            mNotificationManager.cancel(NOTIFICATION_ID);
//            builder = null;
//        }*/
//        } else finish();
//    }

    public static void finish() {
        if (mNotificationManager != null) mNotificationManager.cancel(NOTIFICATION_ID);
        mNotificationManager = null;
        builder = null;
    }
}