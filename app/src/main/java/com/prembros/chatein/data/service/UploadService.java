package com.prembros.chatein.data.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.prembros.chatein.data.model.UpdateRequest;
import com.prembros.chatein.util.Annotations;
import com.prembros.chatein.util.UploadNotification;

import java.util.Map;
import java.util.Objects;

import static com.prembros.chatein.util.CommonUtils.showToast;
import static com.prembros.chatein.util.Constants.CHAT;
import static com.prembros.chatein.util.Constants.FROM;
import static com.prembros.chatein.util.Constants.MESSAGE;
import static com.prembros.chatein.util.Constants.MESSAGES_;
import static com.prembros.chatein.util.Constants.MESSAGE_IMAGES;
import static com.prembros.chatein.util.Constants.SEEN;
import static com.prembros.chatein.util.Constants.TIME_STAMP;
import static com.prembros.chatein.util.Constants.TYPE;

/**
 *
 * Created by Prem $ on 12/7/2017.
 */

public class UploadService extends IntentService implements UploadCallbacks {

    private static final String VIDEO_UPLOAD_RECEIVER = "videoUploadReceiver";
    public static final String DOWNLOAD_URL = "downloadUrl";
    public static final String UPLOAD_URL = "uploadUrl";
    private static final String PUSH_ID = "pushId";
    private static final String CURRENT_USER_ID = "currentUserId";
    private static final String FRIEND_USER_ID = "friendUserId";

    private Bundle bundle;
    private ResultReceiver receiver;

    public static void launchUploadService(Context context, String pushId, Uri filePath, String personName,
                                           String currentUserId, String friendUserId, VideoUploadReceiver videoUploadReceiver) {
        Intent intent = new Intent(context, UploadService.class);
        intent.putExtra(PUSH_ID, pushId);
        intent.putExtra(UPLOAD_URL, filePath);
        intent.putExtra(FROM, personName);
        intent.putExtra(CURRENT_USER_ID, currentUserId);
        intent.putExtra(FRIEND_USER_ID, friendUserId);
        intent.putExtra(FROM, personName);
        intent.putExtra(VIDEO_UPLOAD_RECEIVER, videoUploadReceiver);
        context.startService(intent);
    }

    public UploadService() {
        super("uploadService");
    }

    @Override public void onCreate() {
        super.onCreate();
    }

    @Override protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            bundle = new Bundle();
            receiver = intent.getParcelableExtra(VIDEO_UPLOAD_RECEIVER);
            final String pushId = intent.getStringExtra(PUSH_ID);
            Uri filePath = intent.getParcelableExtra(UPLOAD_URL);
            String personName = intent.getStringExtra(FROM);
            final String currentUserId = intent.getStringExtra(CURRENT_USER_ID);
            final String friendUserId = intent.getStringExtra(FRIEND_USER_ID);
            try {
                UploadNotification.begin(this, personName, friendUserId);

                StorageReference fileReference = FirebaseStorage.getInstance().getReference()
                        .child(MESSAGE_IMAGES).child(pushId + ".jpg");

                fileReference.putFile(filePath).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        int percent = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                        Log.d("***TOTAL BYTES***", String.valueOf(taskSnapshot.getTotalByteCount()));
                        Log.d("***BYTES TRANSFERRED***", String.valueOf(taskSnapshot.getBytesTransferred()));
                        Log.d("***UPLOAD PERCENT***", String.valueOf(percent));
                        onProgressUpdate(percent);
                    }
                }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            DatabaseReference root = FirebaseDatabase.getInstance().getReference();
                            final String downloadUrl = Objects.requireNonNull(task.getResult().getDownloadUrl()).toString();
                            Map messageMap = UpdateRequest.forMapOnly()
                                    .put(MESSAGE, downloadUrl)
                                    .put(SEEN, false)
                                    .put(TYPE, Annotations.ChatType.IMAGE)
                                    .put(TIME_STAMP, ServerValue.TIMESTAMP)
                                    .put(FROM, currentUserId)
                                    .get();

                            UpdateRequest.forDatabase(root)
                                    .put(MESSAGES_ + currentUserId + "/" + friendUserId + "/" + pushId, messageMap)
                                    .put(MESSAGES_ + friendUserId + "/" + currentUserId + "/" + pushId, messageMap)
                                    .update(new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                            if (databaseError != null) {
                                                showToast(getApplicationContext(), databaseError.getMessage());
                                            }
                                            onUploadFinish(downloadUrl);
                                        }
                                    });

                            root.child(CHAT).child(currentUserId).child(friendUserId).child(SEEN).setValue(true);
                            root.child(CHAT).child(currentUserId).child(friendUserId).child(TIME_STAMP).setValue(ServerValue.TIMESTAMP);

                            root.child(CHAT).child(friendUserId).child(currentUserId).child(SEEN).setValue(false);
                            root.child(CHAT).child(friendUserId).child(currentUserId).child(TIME_STAMP).setValue(ServerValue.TIMESTAMP);
                        } else onUploadError(task.getException());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                onUploadError(e);
            }
        }
    }

    @Override public void onProgressUpdate(int percentage) {
        UploadNotification.update(percentage);
    }

    @Override public void onUploadError(Exception exception) {
        exception.printStackTrace();
        showToast(getApplicationContext(), exception.getMessage());
        receiver.send(Annotations.UploadCallback.UPLOAD_ERROR, null);
        UploadNotification.finish();
    }

    @Override public void onUploadFinish(String downloadUrl) {
        bundle.clear();
        bundle.putString(DOWNLOAD_URL, downloadUrl);
        receiver.send(Annotations.UploadCallback.UPLOAD_COMPLETE, bundle);
    }
}