package com.prembros.chatein.ui.main;


import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.prembros.chatein.R;
import com.prembros.chatein.base.BaseFragment;
import com.prembros.chatein.data.model.LastChat;
import com.prembros.chatein.data.model.User;
import com.prembros.chatein.util.Annotations;
import com.prembros.chatein.util.ChatEventListener;
import com.prembros.chatein.util.CustomValueEventListener;
import com.prembros.chatein.util.ViewUtils;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.prembros.chatein.ui.chat.ChatActivity.launchChatActivity;
import static com.prembros.chatein.util.Constants.CHAT;
import static com.prembros.chatein.util.Constants.MESSAGE;
import static com.prembros.chatein.util.Constants.MESSAGES;
import static com.prembros.chatein.util.Constants.ONLINE;
import static com.prembros.chatein.util.Constants.TIME_STAMP;
import static com.prembros.chatein.util.Constants.TYPE;
import static com.prembros.chatein.util.Constants.USERS;
import static com.prembros.chatein.util.DateUtil.getTime;
import static com.prembros.chatein.util.ViewUtils.loadProfilePic;

public class ChatsFragment extends BaseFragment {

    @BindView(R.id.list) RecyclerView recyclerView;
    @BindView(R.id.no_data_placeholder) TextView noDataPlaceholder;

    private DatabaseReference chatDatabase;
    private DatabaseReference messageDatabase;
    private DatabaseReference usersDatabase;
    private FirebaseRecyclerAdapter<LastChat, ChatViewHolder> adapter;

    public ChatsFragment() {}

    @NonNull public static ChatsFragment newInstance() {
        return new ChatsFragment();
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        noDataPlaceholder.setText(R.string.your_chats_appear_here);
        try {
            chatDatabase = root.child(CHAT).child(currentUserId);
            chatDatabase.keepSynced(true);
            messageDatabase = root.child(MESSAGES).child(currentUserId);
            usersDatabase = root.child(USERS);
            usersDatabase.keepSynced(true);

            initializeRecyclerView();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rootView;
    }

    private void initializeRecyclerView() {
        try {
            LinearLayoutManager manager = new LinearLayoutManager(getContext());
            manager.setReverseLayout(true);
            manager.setStackFromEnd(true);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(manager);

            Query conversationQuery = chatDatabase.orderByChild(TIME_STAMP);

            FirebaseRecyclerOptions<LastChat> options = new FirebaseRecyclerOptions.Builder<LastChat>()
                    .setQuery(conversationQuery, LastChat.class).build();

            adapter = new FirebaseRecyclerAdapter<LastChat, ChatViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull final ChatViewHolder holder, int position, @NonNull final LastChat model) {
                    final String userId = getRef(position).getKey();
                    Query lastMessageQuery = messageDatabase.child(userId).limitToLast(1);

                    lastMessageQuery.addChildEventListener(new ChatEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            try {
                                String message = Objects.requireNonNull(dataSnapshot.child(MESSAGE).getValue()).toString();
                                String type = Objects.requireNonNull(dataSnapshot.child(TYPE).getValue()).toString();
                                holder.setMessage(message, type, model);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    usersDatabase.child(userId).addListenerForSingleValueEvent(new CustomValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            holder.bind(new User(dataSnapshot), userId);
                        }
                    });

                    usersDatabase.child(userId).addValueEventListener(new CustomValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            try {
                                if (dataSnapshot.hasChild(ONLINE)) {
                                    Object isOnline = dataSnapshot.child(ONLINE).getValue();
                                    holder.onlineStatus.setVisibility(
                                            !(isOnline instanceof Long) && Objects.equals(isOnline, "true") ?
                                                    View.VISIBLE : GONE);
                                } else holder.onlineStatus.setVisibility(GONE);
//                                holder.bind(new User(dataSnapshot), userId);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                @NonNull @Override public ChatViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
                    setVisibility(noDataPlaceholder, GONE);
                    setVisibility(recyclerView, VISIBLE);
                    return new ChatViewHolder(
                            LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_list, parent, false),
                            glide, getActivity()
                    );
                }
            };
            recyclerView.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override public void onStart() {
        super.onStart();
        if (adapter != null) adapter.startListening();
    }

    @Override public void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }

    @Override public void onDetach() {
        if (adapter != null) adapter = null;
        super.onDetach();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.root_layout) LinearLayout layout;
        @BindView(R.id.dp) ImageView dp;
        @BindView(R.id.online) ImageView onlineStatus;
        @BindView(R.id.name) TextView name;
        @BindView(R.id.time) TextView time;
        @BindView(R.id.status) TextView status;

        private final RequestManager glide;
        private String userId;
        private final WeakReference<Activity> activity;

        ChatViewHolder(View itemView, RequestManager glide, Activity activity) {
            super(itemView);
            this.glide = glide;
            this.activity = new WeakReference<>(activity);
            ButterKnife.bind(this, itemView);
        }

        private void bind(@NotNull User user, String userId) {
            try {
                this.userId = userId;
                loadProfilePic(glide, user.getThumb_image(), dp);
                name.setText(user.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void setMessage(String data, String type, LastChat lastChat) {
            try {
                status.setText(Objects.equals(type, Annotations.ChatType.TEXT) ? data : "Image");
                status.setCompoundDrawablesWithIntrinsicBounds(
                        Objects.equals(type, Annotations.ChatType.TEXT) ? 0 : R.drawable.ic_image, 0, 0, 0);
//                status.setCompoundDrawablesWithIntrinsicBounds(
//                        0, 0, lastChat.isSeen() ? R.drawable.ic_double_tick : 0, 0);
                status.setTypeface(status.getTypeface(), lastChat.isSeen() ? Typeface.NORMAL : Typeface.BOLD);
                status.setTextColor(Color.parseColor(lastChat.isSeen() ? "#9e9e9e" : "#333333"));

                String timeString = getTime(lastChat.getTime_stamp());
                time.setVisibility(timeString != null ? View.VISIBLE : GONE);
                time.setText(timeString);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @OnClick(R.id.root_layout) public void openChat() {
            if (userId != null) launchChatActivity(activity.get(), userId);
        }

        @OnClick(R.id.dp) public void openProfile() {
            ViewUtils.openProfile(activity.get(), userId);
        }
    }
}