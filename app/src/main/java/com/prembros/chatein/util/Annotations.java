package com.prembros.chatein.util;

import android.support.annotation.IntDef;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.prembros.chatein.util.Annotations.ChatType.FILE;
import static com.prembros.chatein.util.Annotations.ChatType.IMAGE;
import static com.prembros.chatein.util.Annotations.ChatType.TEXT;
import static com.prembros.chatein.util.Annotations.NotificationType.ACCEPT;
import static com.prembros.chatein.util.Annotations.NotificationType.MESSAGE_NOTIFICATION;
import static com.prembros.chatein.util.Annotations.NotificationType.REQUEST;
import static com.prembros.chatein.util.Annotations.RequestType.RECEIVED;
import static com.prembros.chatein.util.Annotations.RequestType.SENT;
import static com.prembros.chatein.util.Annotations.SocialState.ARE_FRIENDS;
import static com.prembros.chatein.util.Annotations.SocialState.NOT_FRIENDS;
import static com.prembros.chatein.util.Annotations.SocialState.REQUEST_RECEIVED;
import static com.prembros.chatein.util.Annotations.SocialState.REQUEST_SENT;
import static com.prembros.chatein.util.Annotations.UploadCallback.UPLOAD_COMPLETE;
import static com.prembros.chatein.util.Annotations.UploadCallback.UPLOAD_ERROR;
import static com.prembros.chatein.util.Annotations.UploadCallback.UPLOAD_IN_PROGRESS;

public class Annotations {

    @Retention(RetentionPolicy.CLASS)
    @IntDef({NOT_FRIENDS, REQUEST_SENT, REQUEST_RECEIVED, ARE_FRIENDS})
    public @interface SocialState {
        int NOT_FRIENDS = 0;
        int REQUEST_SENT = 1;
        int REQUEST_RECEIVED = 2;
        int ARE_FRIENDS = 3;
    }

    @Retention(RetentionPolicy.CLASS)
    @StringDef({REQUEST, ACCEPT, MESSAGE_NOTIFICATION})
    public @interface NotificationType {
        String REQUEST = "request";
        String ACCEPT = "accept";
        String MESSAGE_NOTIFICATION = "message_notification";
    }

    @Retention(RetentionPolicy.CLASS)
    @StringDef({TEXT, IMAGE, FILE})
    public @interface ChatType {
        String TEXT = "text";
        String IMAGE = "image";
        String FILE = "file";
    }

    @Retention(RetentionPolicy.CLASS)
    @StringDef({SENT, RECEIVED})
    public @interface RequestType {
        String SENT = "sent";
        String RECEIVED = "received";
    }

    @Retention(RetentionPolicy.CLASS)
    @IntDef({UPLOAD_IN_PROGRESS, UPLOAD_ERROR, UPLOAD_COMPLETE})
    public @interface UploadCallback {
        int UPLOAD_IN_PROGRESS = 10;
        int UPLOAD_ERROR = 11;
        int UPLOAD_COMPLETE = 12;
    }
}