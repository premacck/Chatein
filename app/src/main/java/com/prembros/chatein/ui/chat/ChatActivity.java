package com.prembros.chatein.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alexvasilkov.gestures.animation.ViewPositionAnimator;
import com.alexvasilkov.gestures.transition.GestureTransitions;
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator;
import com.alexvasilkov.gestures.transition.tracker.SimpleTracker;
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
import com.prembros.chatein.ui.main.adapter.ImagePagerAdapter;
import com.prembros.chatein.util.Annotations.ChatType;
import com.prembros.chatein.util.Annotations.UploadCallback;
import com.prembros.chatein.util.ChatEventListener;
import com.prembros.chatein.util.CustomLinearLayoutManager;
import com.prembros.chatein.util.CustomValueEventListener;
import com.prembros.chatein.util.DateUtil;
import com.theartofdev.edmodo.cropper.CropImage;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.prembros.chatein.data.service.UploadService.DOWNLOAD_URL;
import static com.prembros.chatein.data.service.UploadService.launchUploadService;
import static com.prembros.chatein.ui.social.ProfileActivity.USER_ID;
import static com.prembros.chatein.ui.social.ProfileActivity.launchProfileActivity;
import static com.prembros.chatein.util.CommonUtils.makeSnackBar;
import static com.prembros.chatein.util.Constants.CHAT_;
import static com.prembros.chatein.util.Constants.DEFAULT;
import static com.prembros.chatein.util.Constants.FROM;
import static com.prembros.chatein.util.Constants.MESSAGE;
import static com.prembros.chatein.util.Constants.MESSAGES_;
import static com.prembros.chatein.util.Constants.ONLINE;
import static com.prembros.chatein.util.Constants.SEEN;
import static com.prembros.chatein.util.Constants.TIME_STAMP;
import static com.prembros.chatein.util.Constants.TYPE;
import static com.prembros.chatein.util.ViewUtils.disableView;
import static com.prembros.chatein.util.ViewUtils.enableView;

public class ChatActivity extends DatabaseActivity implements ChatAdapter.ViewImageListener {

    private static final int TOTAL_ITEMS_TO_LOAD = 15;
    private static final int GALLERY_PICK = 101;

    @BindView(R.id.chat_toolbar) Toolbar toolbar;
    @BindView(R.id.dp) ImageView dp;
    @BindView(R.id.name) TextView name;
    @BindView(R.id.last_seen) TextView lastSeen;
    @BindView(R.id.chat_add) ImageView addMedia;
    @BindView(R.id.chat_send) ImageView sendMessage;
    @BindView(R.id.chat_message) EditText chatMessageView;
    @BindView(R.id.chats_list) RecyclerView recyclerView;
    @BindView(R.id.transition_full_background) View background;
    @BindView(R.id.transition_pager) ViewPager viewPager;

    private String friendUserId;
    private ChatAdapter adapter;
    private ImagePagerAdapter pagerAdapter;
    private ViewsTransitionAnimator<Integer> animator;
    private CustomValueEventListener friendInfoListener;
    private CustomValueEventListener myChatListener;
    private final List<Chat> chatList = new ArrayList<>();
//    private final SparseArray<String> chatImageList = new SparseArray<>();

    private boolean loading;
    private int itemPosition;
    private String lastKey;
    private String previousKey;
    private String friendName;

    public static void launchChatActivity(@NotNull Context from, String userId) {
        Intent intent = new Intent(from, ChatActivity.class);
        intent.putExtra(USER_ID, userId);
        from.startActivity(intent);
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        unbinder = ButterKnife.bind(this);
        try {
            friendUserId = getIntent().getStringExtra(USER_ID);

            pagerAdapter = new ImagePagerAdapter(viewPager, chatList, glide);
            viewPager.setAdapter(pagerAdapter);
            CustomLinearLayoutManager manager = new CustomLinearLayoutManager(this);
            manager.setStackFromEnd(true);
            recyclerView.setLayoutManager(manager);
            adapter = new ChatAdapter(chatList, currentUserId, glide, this);
            recyclerView.setAdapter(adapter);

            getMyChatRef().child(friendUserId).child(SEEN).setValue(true);

            loadChats();
            setupActionBar();
            updateChatValueInDatabase();

            initializeImageAnimator();

            setOnScrollChangedListener();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadChats() {
        final Query initialChatQuery = getMessagesRef(friendUserId).limitToLast(TOTAL_ITEMS_TO_LOAD);
        initialChatQuery.addChildEventListener(new ChatEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try {
                    if (started) {
                        Chat chat = dataSnapshot.getValue(Chat.class);
                        chatList.add(chat);
                        if (chatList.size() == 1) {
                            lastKey = dataSnapshot.getKey();
                            previousKey = dataSnapshot.getKey();
                        }
                        adapter.notifyItemInserted(chatList.size() - 1);
                        pagerAdapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(chatList.size() - 1);
//                        updateImageListIfAvailable(chat, chatList.indexOf(chat));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    initialChatQuery.removeEventListener(this);
                }
            }
        });
    }

    private void setupActionBar() {
        try {
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

    private void initializeImageAnimator() {
        final SimpleTracker listTracker = new SimpleTracker() {
            @Nullable
            @Override
            public View getViewAt(int position) {
                CustomLinearLayoutManager manager = (CustomLinearLayoutManager) recyclerView.getLayoutManager();
                int first = manager.findFirstVisibleItemPosition();
                int last = manager.findLastVisibleItemPosition();
                if (position < first || position > last) {
                    return null;
                } else {
                    View itemView = recyclerView.getChildAt(position - first);
                    return ChatAdapter.getImage(itemView);
                }
            }
        };

        final SimpleTracker pagerTracker = new SimpleTracker() {
            @Nullable @Override
            public View getViewAt(int position) {
                ImagePagerAdapter.ImageViewHolder holder = pagerAdapter.getViewHolder(position);
                return holder == null ? null : ImagePagerAdapter.getImage(holder);
            }
        };

        animator = GestureTransitions.from(recyclerView, listTracker).into(viewPager, pagerTracker);
        animator.addPositionUpdateListener(new ViewPositionAnimator.PositionUpdateListener() {
            @Override
            public void onPositionUpdate(float position, boolean isLeaving) {
                background.setVisibility(position == 0f ? View.INVISIBLE : View.VISIBLE);
                background.getBackground().setAlpha((int) (255 * position));
            }
        });
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
        final Query moreChatsQuery = getMessagesRef(friendUserId).orderByKey().endAt(lastKey).limitToLast(TOTAL_ITEMS_TO_LOAD + 1);
        moreChatsQuery.addChildEventListener(new ChatEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try {
                    if (started) {
                        Chat chat = dataSnapshot.getValue(Chat.class);
//                    Load everything except the duplicate last key
                        if (!previousKey.equals(dataSnapshot.getKey()))
                            chatList.add(itemPosition++, chat);
                        else
                            previousKey = lastKey;

                        adapter.notifyDataSetChanged();
                        pagerAdapter.notifyDataSetChanged();
                        if (itemPosition == 1) lastKey = dataSnapshot.getKey();
                        ((CustomLinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(15, 0);

//                    updateImageListIfAvailable(chat, chatList.indexOf(chat));

                        if (itemPosition == 9) loading = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    loading = false;
                } finally {
                    moreChatsQuery.removeEventListener(this);
                }
            }
        });
    }

//    private void updateImageListIfAvailable(Chat chat, int position) {
//        if (Objects.equals(Objects.requireNonNull(chat).getType(), Annotations.ChatType.IMAGE)) {
//            chatImageList.put(position, chat.getMessage());
//            pagerAdapter.notifyDataSetChanged();
//        }
//    }

    @Override public void viewImage(int position) {
        animator.enter(position, true);
    }

    @OnClick(R.id.back_btn) public void goBack() {
        onBackPressed();
    }

    @OnClick(R.id.bio) public void openProfile() {
        launchProfileActivity(this, friendUserId);
    }

    @OnClick(R.id.chat_add) public void addMedia() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select image"), GALLERY_PICK);
    }

    @OnClick(R.id.chat_send) public void sendTextMessage() {
        String message = chatMessageView.getText().toString();
        if (!TextUtils.isEmpty(message)) {
            DatabaseReference userMessagePush = getMessagesRef(friendUserId).push();
            String pushId = "/" + userMessagePush.getKey();

            updateFirebaseDatabase(pushId, message, ChatType.TEXT);
            chatMessageView.setText(null);

            setSeenValues();
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            CropImage.activity(data.getData())
                    .setActivityTitle("Edit Image")
                    .setActivityMenuIconColor(Color.parseColor("#00acc1"))
                    .setAllowFlipping(true)
                    .setAllowRotation(true)
                    .setAutoZoomEnabled(true)
                    .setCropMenuCropButtonTitle("Send")
                    .start(this);
        }
        else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                DatabaseReference userMessagePush = getMessagesRef(friendUserId).push();
                final String pushId = userMessagePush.getKey();

                updateFirebaseDatabase(pushId, DEFAULT, ChatType.IMAGE);

                disableView(sendMessage, false);

                launchUploadService(
                        this, pushId, resultUri, friendName, currentUserId, friendUserId,
                        new VideoUploadReceiver(new Handler()).setReceiver(new VideoUploadReceiver.Receiver() {
                            @Override
                            public void onReceiverResult(@UploadCallback int resultCode, Bundle resultData) {
                                switch (resultCode) {
                                    case UploadCallback.UPLOAD_COMPLETE:
                                        String downloadUrl = resultData.getString(DOWNLOAD_URL);
//                                        Works only when more messages haven't arrived since uploading
                                        chatList.get(chatList.size() - 1).setMessage(downloadUrl);
                                        chatList.get(chatList.size() - 1).setType(ChatType.IMAGE);
                                        adapter.notifyItemChanged(chatList.size() - 1);
                                        enableView(sendMessage);
                                        break;
                                    case UploadCallback.UPLOAD_ERROR:
//                                        Works only when more messages haven't arrived since uploading
                                        chatList.remove(chatList.size() - 1);
                                        adapter.notifyItemRemoved(chatList.size() - 1);
                                        enableView(sendMessage);
                                        break;
                                    case UploadCallback.UPLOAD_IN_PROGRESS:
                                        break;
                                }
                            }
                        })
                );
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
                result.getError().printStackTrace();
        }
    }

    private void updateFirebaseDatabase(String pushId, String message, @ChatType String chatType) {
        Map messageMap = UpdateRequest.forMapOnly()
                .put(MESSAGE, message)
                .put(SEEN, false)
                .put(TYPE, chatType)
                .put(TIME_STAMP, ServerValue.TIMESTAMP)
                .put(FROM, currentUserId)
                .get();

        UpdateRequest.forDatabase(getRootRef())
                .put(MESSAGES_ + getMyBranch() + "/" + pushId, messageMap)
                .put(MESSAGES_ + getFriendsBranch() + "/" + pushId, messageMap)
                .update(getCompletionListener());
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

    @Override protected void onDestroy() {
        getUsersRef().child(friendUserId).removeEventListener(friendInfoListener);
        getMyChatRef().removeEventListener(myChatListener);
        adapter = null;
        pagerAdapter = null;
        super.onDestroy();
    }

    @Override public void onBackPressed() {
        if (!animator.isLeaving()) {
            animator.exit(true);
        } else {
            super.onBackPressed();
        }
    }
}