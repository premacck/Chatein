package com.prembros.chatein.ui.main.adapter;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.prembros.chatein.R;
import com.prembros.chatein.base.BaseFragment;
import com.prembros.chatein.data.model.LastChat;
import com.prembros.chatein.data.model.UpdateRequest;
import com.prembros.chatein.data.model.User;
import com.prembros.chatein.ui.base.SelectableFirebaseAdapter;
import com.prembros.chatein.ui.main.ChatsFragment;
import com.prembros.chatein.util.Annotations;
import com.prembros.chatein.util.FileUtil;
import com.prembros.chatein.util.ViewUtils;
import com.prembros.chatein.util.database.ChatEventListener;
import com.prembros.chatein.util.database.CustomValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.prembros.chatein.ui.chat.ChatActivity.launchChatActivity;
import static com.prembros.chatein.util.Annotations.ChatType.TEXT;
import static com.prembros.chatein.util.Constants.CHAT_;
import static com.prembros.chatein.util.Constants.MESSAGE;
import static com.prembros.chatein.util.Constants.MESSAGES_;
import static com.prembros.chatein.util.Constants.MESSAGE_IMAGES;
import static com.prembros.chatein.util.Constants.ONLINE;
import static com.prembros.chatein.util.Constants.TYPE;
import static com.prembros.chatein.util.DateUtil.getTime;
import static com.prembros.chatein.util.ViewUtils.loadProfilePic;
import static java.util.Objects.requireNonNull;

public class MainChatsAdapter extends SelectableFirebaseAdapter<LastChat, MainChatsAdapter.ChatViewHolder> {

    private final BaseFragment fragment;

    public MainChatsAdapter(@NonNull FirebaseRecyclerOptions<LastChat> options, ChatsFragment fragment) {
        super(options);
        this.fragment = fragment;
    }

    @Override protected void onBindViewHolder(@NonNull final ChatViewHolder holder, int position, @NonNull final LastChat model) {
        try {
            final String userId = getRef(position).getKey();
            holder.setUserId(userId);
            Query lastMessageQuery = fragment.getParentActivity().getMessagesRef(userId).limitToLast(1);

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

            fragment.getParentActivity().getUsersRef().child(userId).addListenerForSingleValueEvent(new CustomValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    holder.bind(new User(dataSnapshot));
                }
            });

            fragment.getParentActivity().getUsersRef().child(userId).addValueEventListener(new CustomValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        if (dataSnapshot.hasChild(ONLINE)) {
                            Object isOnline = dataSnapshot.child(ONLINE).getValue();
                            holder.onlineStatus.setVisibility(
                                    !(isOnline instanceof Long) && Objects.equals(isOnline, "true") ?
                                            VISIBLE : GONE);
                        } else holder.onlineStatus.setVisibility(GONE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull @Override public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        fragment.dataAvailable();
        return new MainChatsAdapter.ChatViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_list, parent, false),
                this
        );
    }

    @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        this.mode = mode;
        multiSelect = true;
        mode.getMenuInflater().inflate(R.menu.menu_chat_multiple, menu);
        return true;
    }

    @Override public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
        try {
            String suffix = selectedItems.size() <=1 ? " this conversation?\n\n" : " selected conversations?\n\n";
            new AlertDialog.Builder(fragment.getParentActivity())
                    .setTitle("Attention!")
                    .setMessage("Are you sure you want to delete" + suffix + "This cannot be undone.")
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(MESSAGE_IMAGES);
                                String currentUserId = fragment.getCurrentUserId();
                                for (final int index : selectedItems.keySet()) {
                                    String friendUserBranch = currentUserId + "/" + selectedItems.get(index);
                                    UpdateRequest.forDatabase(fragment.getRoot())
                                            .put(MESSAGES_ + friendUserBranch, null)
                                            .put(CHAT_ + friendUserBranch, null)
                                            .update(new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                    getRef(index).removeValue();
                                                    notifyItemRemoved(index);
                                                }
                                            });
                                    storageRef.child(currentUserId).child(selectedItems.get(index)).delete();
                                }
                                multiSelect = false;
                                selectedItems.clear();
                                mode.finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {

        private static final String SELECTED_MASK = "#1500bfa5";

        @BindView(R.id.root_layout) LinearLayout layout;
        @BindView(R.id.dp) ImageView dp;
        @BindView(R.id.selected_tick) ImageView selectedTick;
        @BindView(R.id.online) ImageView onlineStatus;
        @BindView(R.id.name) TextView name;
        @BindView(R.id.time) TextView time;
        @BindView(R.id.status) TextView status;

        private String userId;
        private final MainChatsAdapter adapter;

        ChatViewHolder(View itemView, MainChatsAdapter adapter) {
            super(itemView);
            this.adapter = adapter;
            ButterKnife.bind(this, itemView);
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        private void bind(@NotNull User user) {
            try {
                loadProfilePic(adapter.fragment.glide, user.getThumb_image(), dp);
                name.setText(user.getName());
                boolean isItemSelected = adapter.selectedItems.containsValue(userId);
                layout.setBackgroundColor(isItemSelected ?
                        Color.parseColor(SELECTED_MASK) : Color.WHITE);
                selectedTick.setVisibility(isItemSelected ? VISIBLE : GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void setMessage(String data, String type, LastChat lastChat) {
            try {
                status.setText(
                        Objects.equals(type, Annotations.ChatType.IMAGE) ?
                                "Image" :
                                Objects.equals(type, TEXT) ?
                                        data :
                                        FileUtil.getExtension(data) + " File"
                );
                status.setCompoundDrawablesWithIntrinsicBounds(
                        Objects.equals(type, Annotations.ChatType.IMAGE) ? R.drawable.ic_image : 0, 0, 0, 0);
                status.setTypeface(status.getTypeface(), lastChat.getSeen() ? Typeface.NORMAL : Typeface.BOLD);
                status.setTextColor(Color.parseColor(lastChat.getSeen() ? "#9e9e9e" : "#333333"));

                String timeString = getTime(lastChat.getTime_stamp());
                time.setVisibility(timeString != null ? VISIBLE : GONE);
                time.setText(timeString);
                status.setCompoundDrawablesWithIntrinsicBounds(
                        Objects.equals(adapter.fragment.getCurrentUserId(), userId) ?
                                requireNonNull(lastChat.getSeen()) ? R.drawable.ic_double_tick :
                                        R.drawable.ic_tick : 0, 0, 0, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @OnClick(R.id.dp) public void openProfile() {
            try {
                if (!adapter.multiSelect) ViewUtils.openProfile(adapter.fragment.getParentActivity(), userId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @OnClick(R.id.root_layout) public void openChat() {
            try {
                if (!adapter.multiSelect && adapter.selectedItems.isEmpty()) {
                    if (userId != null) launchChatActivity(adapter.fragment.getParentActivity(), userId);
                } else
                    selectItem(getAdapterPosition());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @OnLongClick(R.id.root_layout) public boolean itemSelected(View view) {
            if (!adapter.multiSelect) ((AppCompatActivity)view.getContext()).startSupportActionMode(adapter);
            selectItem(getAdapterPosition());
            return true;
        }

        private void selectItem(int position) {
            if (adapter.multiSelect) {
                if (adapter.selectedItems.containsValue(userId)) {
                    adapter.selectedItems.remove(position);
                    layout.setBackgroundColor(Color.WHITE);
                    selectedTick.setVisibility(GONE);
                } else {
                    adapter.selectedItems.put(position, userId);
                    layout.setBackgroundColor(Color.parseColor(SELECTED_MASK));
                    selectedTick.setVisibility(VISIBLE);
                }

                if (adapter.selectedItems.isEmpty()) {
                    adapter.multiSelect = false;
                    adapter.mode.finish();
                } else adapter.mode.setTitle(adapter.selectedItems.size() +
                        (adapter.selectedItems.size() <= 1 ? " Conversation" : " Conversations"));
            }
        }
    }
}