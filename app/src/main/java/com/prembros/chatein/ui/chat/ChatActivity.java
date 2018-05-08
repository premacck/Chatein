package com.prembros.chatein.ui.chat;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.prembros.chatein.R;
import com.prembros.chatein.data.model.Chat;
import com.prembros.chatein.data.model.UpdateRequest;
import com.prembros.chatein.data.model.User;
import com.prembros.chatein.data.service.VideoUploadReceiver;
import com.prembros.chatein.ui.base.DatabaseActivity;
import com.prembros.chatein.util.Annotations;
import com.prembros.chatein.util.Annotations.ChatType;
import com.prembros.chatein.util.CustomLinearLayoutManager;
import com.prembros.chatein.util.DateUtil;
import com.prembros.chatein.util.SharedPrefs;
import com.prembros.chatein.util.database.ChatEventListener;
import com.prembros.chatein.util.database.CustomValueEventListener;
import com.theartofdev.edmodo.cropper.CropImage;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.prembros.chatein.data.service.UploadService.DOWNLOAD_URL;
import static com.prembros.chatein.data.service.UploadService.launchUploadService;
import static com.prembros.chatein.ui.social.ProfileActivity.USER_ID;
import static com.prembros.chatein.ui.social.ProfileActivity.launchProfileActivity;
import static com.prembros.chatein.util.Annotations.ChatType.IMAGE;
import static com.prembros.chatein.util.Annotations.ChatType.TEXT;
import static com.prembros.chatein.util.Annotations.NotificationType.MESSAGE_NOTIFICATION;
import static com.prembros.chatein.util.CommonUtils.makeSnackBar;
import static com.prembros.chatein.util.Constants.CHAT_;
import static com.prembros.chatein.util.Constants.DEFAULT;
import static com.prembros.chatein.util.Constants.FROM;
import static com.prembros.chatein.util.Constants.FROM_USER_ID;
import static com.prembros.chatein.util.Constants.MESSAGE;
import static com.prembros.chatein.util.Constants.MESSAGES_;
import static com.prembros.chatein.util.Constants.NOTIFICATIONS_;
import static com.prembros.chatein.util.Constants.ONLINE;
import static com.prembros.chatein.util.Constants.SEEN;
import static com.prembros.chatein.util.Constants.TIME_STAMP;
import static com.prembros.chatein.util.Constants.TYPE;
import static com.prembros.chatein.util.FileUtil.isFileAnImage;
import static com.prembros.chatein.util.FileUtil.isFileSizeLegal;
import static com.prembros.chatein.util.FileUtil.isImageSizeLegal;
import static com.prembros.chatein.util.SharedPrefs.clearNotifications;
import static com.prembros.chatein.util.ViewUtils.disableView;
import static com.prembros.chatein.util.ViewUtils.enableView;
import static com.prembros.chatein.util.ViewUtils.showAlertDialog;

public class ChatActivity extends DatabaseActivity {

    private static final int TOTAL_ITEMS_TO_LOAD = 15;
    private static final int GALLERY_PICK = 101;
    private static final int CAMERA_PICK = 102;
    private static final int FILE_PICK = 104;
    private static final int RC_CAMERA = 202;
    private static final int RC_STORAGE = 204;

    @BindView(R.id.chat_toolbar) Toolbar toolbar;
    @BindView(R.id.dp) ImageView dp;
    @BindView(R.id.name) TextView name;
    @BindView(R.id.last_seen) TextView lastSeen;
    @BindView(R.id.upload_options_layout) LinearLayout uploadOptionsLayout;
    @BindView(R.id.progress_bar) ProgressBar progressBar;
    @BindView(R.id.chat_add) ImageView showUploadOptions;
    @BindView(R.id.chat_send) ImageView sendMessage;
    @BindView(R.id.chat_message) EditText chatMessageView;
    @BindView(R.id.chats_list) RecyclerView recyclerView;

    private boolean loading;
    private int itemPosition;
    private String friendUserId;
    private String lastKey;
    private String previousKey;
    private String friendName;

    private ChatAdapter adapter;
    private Query initialChatQuery;
    private Query moreChatsQuery;
    private ChatEventListener initialChatListener;
    private ChatEventListener moreChatListener;
    private CustomValueEventListener friendInfoListener;
    private CustomValueEventListener myChatListener;
    private final List<Chat> chatList = new ArrayList<>();
    private boolean isUploadOptionsLayoutOpen;

    public static void launchChatActivity(@NotNull Context from, String userId) {
        Intent intent = new Intent(from, ChatActivity.class);
        intent.putExtra(USER_ID, userId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        from.startActivity(intent);
    }

    @Override public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isUploadOptionsLayoutOpen) {
            Rect rect = new Rect();
            uploadOptionsLayout.getGlobalVisibleRect(rect);
            if (!rect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                toggleViewAnimator();
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        unbinder = ButterKnife.bind(this);
        clearNotifications(this);
        try {
            friendUserId = getIntent().getStringExtra(USER_ID);
            try {
//            FOR NOTIFICATIONS
                if (friendUserId == null)
                    friendUserId = Objects.requireNonNull(getIntent().getExtras()).getString(FROM_USER_ID);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String previouslyTypedMessage = SharedPrefs.getChat(this, friendUserId);
            if (previouslyTypedMessage != null && !previouslyTypedMessage.isEmpty()) {
                chatMessageView.setText(previouslyTypedMessage);
            }

            CustomLinearLayoutManager manager = new CustomLinearLayoutManager(this);
            manager.setStackFromEnd(true);
            recyclerView.setLayoutManager(manager);
            adapter = new ChatAdapter(chatList, currentUserId, friendUserId, glide, this);
            recyclerView.setAdapter(adapter);

            getMyChatRef().child(friendUserId).child(SEEN).setValue(true);

            loadChats();
            setupActionBar();
            updateChatValueInDatabase();

            setOnScrollChangedListener();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadChats() {
        initialChatQuery = getMessagesRef(friendUserId).limitToLast(TOTAL_ITEMS_TO_LOAD);
        initialChatListener = new ChatEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try {
                    if (started) {
                        Chat chat = dataSnapshot.getValue(Chat.class);
                        if (chat != null) {
                            chat.setKey(dataSnapshot.getKey());
                            chatList.add(chat);
                            if (chatList.size() == 1) {
                                lastKey = dataSnapshot.getKey();
                                previousKey = dataSnapshot.getKey();
                            }
                            adapter.notifyItemInserted(chatList.size() - 1);
                            recyclerView.scrollToPosition(chatList.size() - 1);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        initialChatQuery.addChildEventListener(initialChatListener);
    }

    private void setupActionBar() {
        try {
            setSupportActionBar(toolbar);
            friendInfoListener = new CustomValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        if (started && dataSnapshot != null && dp != null && name != null && lastSeen != null) {
                            User user = new User(dataSnapshot);
                            try {
                                if (!Objects.equals(user.getThumb_image(), DEFAULT)) {
                                    glide.load(user.getThumb_image())
                                            .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.ic_add_user_small))
                                            .transition(DrawableTransitionOptions.withCrossFade())
                                            .into(dp);
                                } else dp.setImageResource(R.drawable.ic_add_user_small);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            friendName = user.getName();
                            name.setText(friendName);

                            if (dataSnapshot.hasChild(ONLINE)) {
                                String lastSeenString = Objects.requireNonNull(dataSnapshot.child(ONLINE).getValue()).toString();
                                lastSeen.setVisibility(View.VISIBLE);
                                if (Objects.equals(lastSeenString, "true")) {
                                    lastSeen.setText(R.string.online);
                                } else if (lastSeenString != null && TextUtils.isDigitsOnly(lastSeenString)){
                                    String ago = DateUtil.getTimeAgo(Long.parseLong(lastSeenString));
                                    if (ago != null) {
                                        String lastSeenText = "Last seen " + ago;
                                        lastSeen.setText(lastSeenText);
                                    }
                                    else lastSeen.setVisibility(View.GONE);
                                }
                                else lastSeen.setVisibility(View.GONE);
                            }
                            else lastSeen.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            getUsersRef().child(friendUserId).addValueEventListener(friendInfoListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateChatValueInDatabase() {
        myChatListener = new CustomValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (started && !dataSnapshot.hasChild(friendUserId)) {
//                    Create chat
                    Map chatAddMap = UpdateRequest.forMapOnly()
                            .put(SEEN, false)
                            .put(TIME_STAMP, ServerValue.TIMESTAMP)
                            .get();

                    UpdateRequest.forDatabase(getRootRef())
                            .put(CHAT_ + getMyBranch(), chatAddMap)
                            .put(CHAT_ + getFriendsBranch(), chatAddMap)
                            .update(getCompletionListener());
                }
            }
        };
        getMyChatRef().addValueEventListener(myChatListener);
    }

    private void setOnScrollChangedListener() {
        if (recyclerView != null) {
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    try {
                        if (newState == SCROLL_STATE_IDLE &&
                                ((CustomLinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition() == 0
                                && !loading) {
//                            SCROLLED TO TOP
                            loading = true;
                            itemPosition = 0;
                            loadMoreChats();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void loadMoreChats() {
        moreChatsQuery = getMessagesRef(friendUserId).orderByKey().endAt(lastKey).limitToLast(TOTAL_ITEMS_TO_LOAD + 1);
        moreChatListener = new ChatEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try {
                    if (started) {
                        Chat chat = dataSnapshot.getValue(Chat.class);
                        if (chat != null) {
                            chat.setKey(dataSnapshot.getKey());
//                            Load everything except the duplicate last key
                            if (!previousKey.equals(dataSnapshot.getKey()))
                                chatList.add(itemPosition++, chat);
                            else
                                previousKey = lastKey;

                            adapter.notifyDataSetChanged();
                            if (Objects.equals(chat.getType(), IMAGE))
                            if (itemPosition == 1) lastKey = dataSnapshot.getKey();
                            ((CustomLinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(15, 0);

                            if (itemPosition == 9) loading = false;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    loading = false;
                }
            }
        };
        moreChatsQuery.addChildEventListener(moreChatListener);
    }

    @OnClick(R.id.back_btn) public void goBack() {
        onBackPressed();
    }

    @OnClick(R.id.bio) public void openProfile() {
        launchProfileActivity(this, friendUserId);
    }

    @OnClick(R.id.upload_gallery_btn) public void addGalleryMedia() {
        toggleViewAnimator();
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), GALLERY_PICK);
    }

    @AfterPermissionGranted(RC_CAMERA) @OnClick(R.id.upload_camera_btn) public void addCameraMedia() {
        toggleViewAnimator();
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_PICK);
        } else EasyPermissions.requestPermissions(this, getString(R.string.camera_permission_rationale), RC_CAMERA, perms);
    }

    @AfterPermissionGranted(RC_STORAGE) @OnClick(R.id.upload_file_btn) public void addFileMedia() {
        toggleViewAnimator();
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(Intent.createChooser(intent, "Select File"), FILE_PICK);
        } else EasyPermissions.requestPermissions(this, getString(R.string.storage_permission_rationale), RC_STORAGE, perms);
    }

    @OnClick(R.id.chat_send) public void sendTextMessage() {
        String message = chatMessageView.getText().toString();
        if (!TextUtils.isEmpty(message)) {
            DatabaseReference userMessagePush = getMessagesRef(friendUserId).push();
            String pushId = userMessagePush.getKey();

            updateFirebaseDatabase(pushId, message, TEXT);
            chatMessageView.setText(null);

            setSeenValues();
        }
    }

    @OnClick(R.id.chat_add) public void addMedia() {
        toggleViewAnimator();
    }

    private void toggleViewAnimator() {
        uploadOptionsLayout.setVisibility(View.INVISIBLE);
        int w = uploadOptionsLayout.getWidth();
        int h = uploadOptionsLayout.getHeight();

        int startRadius = isUploadOptionsLayoutOpen ? (int) Math.hypot(w, h) : 0;
        int endRadius = isUploadOptionsLayoutOpen ? 0 : (int) Math.hypot(w, h);

        int cx = (int) (showUploadOptions.getX());
        int cy = (int) (showUploadOptions.getY() + showUploadOptions.getHeight() * 3);
        Animator revealAnimator = ViewAnimationUtils.createCircularReveal(uploadOptionsLayout, cx, cy, startRadius, endRadius);
        revealAnimator.setInterpolator(new DecelerateInterpolator());
        revealAnimator.setDuration(280);
        if (isUploadOptionsLayoutOpen)
            revealAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    uploadOptionsLayout.setVisibility(View.GONE);
                    isUploadOptionsLayoutOpen = false;
                }
            });
        else isUploadOptionsLayoutOpen = true;

        uploadOptionsLayout.setVisibility(View.VISIBLE);
        revealAnimator.start();
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == RESULT_OK) {
                switch (requestCode) {
                    case GALLERY_PICK:
                        if (!isImageSizeLegal(this, Objects.requireNonNull(data.getData()))) return;
                        startImageCropper(data.getData());
                        break;
                    case CAMERA_PICK:
                        Bitmap photo = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                        startImageCropper(getPhotoUri(photo));
                        break;
                    case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                        CropImage.ActivityResult result = CropImage.getActivityResult(data);
                        if (!isImageSizeLegal(this, result.getUri())) return;

                        DatabaseReference userImageMessagePush = getMessagesRef(friendUserId).push();
                        String imagePushId = userImageMessagePush.getKey();
                        uploadFile(result.getUri(), imagePushId, ChatType.IMAGE, null);
                        break;
                    case FILE_PICK:
                        final Uri fileUri = data.getData();
                        String uriString;
                        if (fileUri != null) {
                            uriString = fileUri.toString();
                            File file = new File(uriString);
                            String displayName = null;

                            if (uriString.startsWith("content://")) {
                                Cursor cursor = null;
                                try {
                                    cursor = getContentResolver().query(fileUri,
                                            null, null, null, null);
                                    if (cursor != null && cursor.moveToFirst()) {
                                        displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

                                        long size = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
                                        if (size > 5000000) return;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    if (cursor != null) cursor.close();
                                }
                            } else if (uriString.startsWith("file://")) {
                                if (!isFileSizeLegal(this, file)) return;
                                displayName = file.getName();
                            }

                            if (!isFileSizeLegal(this, data.getData())) return;

                            if (isFileAnImage(displayName)) {
                                startImageCropper(fileUri);
                                return;
                            }
                            final String finalDisplayName = displayName;
                            showAlertDialog(this,
                                    "You want to send " + displayName + " to " + friendName + "?",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            DatabaseReference userFileMessagePush = getMessagesRef(friendUserId).push();
                                            String filePushId = userFileMessagePush.getKey();
                                            uploadFile(fileUri, filePushId, ChatType.FILE, finalDisplayName);
                                        }
                                    }
                            );
                        }
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Nullable private Uri getPhotoUri(Bitmap photo) {
        try {
            File tempDir = getCacheDir();
            tempDir = new File(tempDir.getAbsolutePath() + "/.sent/");
            tempDir.mkdir();
            File tempFile = File.createTempFile(String.valueOf(new Date().getTime()), ".jpg", tempDir);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            byte[] bitmapData = bytes.toByteArray();

            //write the bytes in file
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
            return Uri.fromFile(tempFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void uploadFile(Uri resultUri, String key, @ChatType final String chatType, String fileName) {
        actionInProgress();
        updateFirebaseDatabase(key, DEFAULT, chatType);
        launchUploadService(
                this, key, resultUri, friendName, chatType, currentUserId, friendUserId, fileName,
                new VideoUploadReceiver(new Handler()).setReceiver(new VideoUploadReceiver.Receiver() {
                    @Override
                    public void onReceiverResult(@Annotations.UploadCallback int resultCode, Bundle resultData) {
                        switch (resultCode) {
                            case Annotations.UploadCallback.UPLOAD_COMPLETE:
                                String downloadUrl = resultData.getString(DOWNLOAD_URL);
//                                Works only when more messages haven't arrived since uploading
                                chatList.get(chatList.size() - 1).setMessage(downloadUrl);
                                chatList.get(chatList.size() - 1).setType(chatType);
                                adapter.notifyItemChanged(chatList.size() - 1);
                                actionCompleted();
                                break;
                            case Annotations.UploadCallback.UPLOAD_ERROR:
//                                Works only when more messages haven't arrived since uploading
                                chatList.remove(chatList.size() - 1);
                                adapter.notifyItemRemoved(chatList.size() - 1);
                                actionCompleted();
                                break;
                            case Annotations.UploadCallback.UPLOAD_IN_PROGRESS:
                                break;
                        }
                    }
                })
        );
    }

    private void startImageCropper(Uri imageUri) {
        CropImage.activity(imageUri)
                .setActivityTitle("Edit Image")
                .setActivityMenuIconColor(Color.WHITE)
                .setAllowFlipping(true)
                .setBorderCornerOffset(0)
                .setAllowRotation(true)
                .setAutoZoomEnabled(true)
                .setCropMenuCropButtonTitle("Send")
                .setCropMenuCropButtonIcon(R.drawable.ic_send_primary)
                .start(this);
    }

    private void updateFirebaseDatabase(String pushId, String message, @ChatType String chatType) {
        Map messageMap = UpdateRequest.forMapOnly()
                .put(MESSAGE, message)
                .put(SEEN, false)
                .put(TYPE, chatType)
                .put(TIME_STAMP, ServerValue.TIMESTAMP)
                .put(FROM, currentUserId)
                .get();

        if (Objects.equals(chatType, TEXT)) {
            UpdateRequest.forDatabase(getRootRef())
                    .put(MESSAGES_ + getMyBranch() + "/" + pushId, messageMap)
                    .put(MESSAGES_ + getFriendsBranch() + "/" + pushId, messageMap)
                    .put(NOTIFICATIONS_ + friendUserId + "/" + pushId,
                            UpdateRequest.forMapOnly()
                                    .put(FROM, currentUserId)
                                    .put(TYPE, MESSAGE_NOTIFICATION)
                                    .get())
                    .update(getCompletionListener());
        } else {
            UpdateRequest.forDatabase(getRootRef())
                    .put(MESSAGES_ + getMyBranch() + "/" + pushId, messageMap)
                    .put(MESSAGES_ + getFriendsBranch() + "/" + pushId, messageMap)
                    .update(getCompletionListener());
        }
    }

    private void setSeenValues() {
        getChatRef(friendUserId).child(SEEN).setValue(true);
        getChatRef(friendUserId).child(TIME_STAMP).setValue(ServerValue.TIMESTAMP);

        getChatRef().child(friendUserId).child(currentUserId).child(SEEN).setValue(false);
        getChatRef().child(friendUserId).child(currentUserId).child(TIME_STAMP).setValue(ServerValue.TIMESTAMP);
    }

    @NonNull @Contract(pure = true) private DatabaseReference.CompletionListener getCompletionListener() {
        return new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null)
                    makeSnackBar(toolbar, databaseError.getMessage());
            }
        };
    }

    @NonNull @Contract(pure = true) private String getMyBranch() {
        return currentUserId + "/" + friendUserId;
    }

    @NonNull @Contract(pure = true) private String getFriendsBranch() {
        return friendUserId + "/" + currentUserId;
    }

    public void actionInProgress() {
        disableView(sendMessage, false);
        disableView(showUploadOptions, false);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void actionCompleted() {
        enableView(sendMessage);
        enableView(showUploadOptions);
        progressBar.setVisibility(View.GONE);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override protected void onPause() {
        if (chatMessageView != null && !chatMessageView.getText().toString().isEmpty()) {
            SharedPrefs.saveChat(this, friendUserId, chatMessageView.getText().toString());
        }
        super.onPause();
    }

    @Override protected void onDestroy() {
        if (initialChatQuery != null) initialChatQuery.removeEventListener(initialChatListener);
        if (moreChatsQuery != null) moreChatsQuery.removeEventListener(moreChatListener);
        getUsersRef().child(friendUserId).removeEventListener(friendInfoListener);
        getMyChatRef().removeEventListener(myChatListener);
        adapter = null;
        super.onDestroy();
    }

    @Override public void onBackPressed() {
        if (isUploadOptionsLayoutOpen) {
            toggleViewAnimator();
        } else {
            super.onBackPressed();
        }
    }
}