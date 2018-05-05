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
import com.google.firebase.database.Query;
import com.prembros.chatein.R;
import com.prembros.chatein.base.BaseFragment;
import com.prembros.chatein.data.model.LastChat;
import com.prembros.chatein.data.model.User;
import com.prembros.chatein.ui.base.SelectableFirebaseAdapter;
import com.prembros.chatein.ui.main.ChatsFragment;
import com.prembros.chatein.util.Annotations;
import com.prembros.chatein.util.ChatEventListener;
import com.prembros.chatein.util.CustomValueEventListener;
import com.prembros.chatein.util.ViewUtils;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

import static android.view.View.GONE;
import static com.prembros.chatein.ui.chat.ChatActivity.launchChatActivity;
import static com.prembros.chatein.util.Constants.MESSAGE;
import static com.prembros.chatein.util.Constants.ONLINE;
import static com.prembros.chatein.util.Constants.TYPE;
import static com.prembros.chatein.util.DateUtil.getTime;
import static com.prembros.chatein.util.ViewUtils.loadProfilePic;

public class MainChatsAdapter extends SelectableFirebaseAdapter<LastChat, MainChatsAdapter.ChatViewHolder> {

    private final BaseFragment fragment;

    public MainChatsAdapter(@NonNull FirebaseRecyclerOptions<LastChat> options, ChatsFragment fragment) {
        super(options);
        this.fragment = fragment;
    }

    @Override protected void onBindViewHolder(@NonNull final ChatViewHolder holder, int position, @NonNull final LastChat model) {
        try {
            final String userId = getRef(position).getKey();
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
                    holder.bind(new User(dataSnapshot), userId);
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
                                            View.VISIBLE : GONE);
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
        multiSelect = true;
        menu.add(0, R.id.action_delete, 0, R.string.delete);
        return true;
    }

    @Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        try {
            String suffix = selectedItems.size() <=1 ? " this conversation?\n" : " selected conversations?\n";
            new AlertDialog.Builder(fragment.getParentActivity())
                    .setTitle("Alert")
                    .setMessage("Are you sure you want to delete" + suffix + "This cannot be undone.")
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            for (int index : selectedItems) {
                                try {
                                    fragment.getParentActivity()
                                            .getMessagesRef(getRef(index).getKey())
                                            .removeValue();
                                    getRef(index).removeValue();
                                    notifyItemRemoved(index);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
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

        @BindView(R.id.root_layout) LinearLayout layout;
        @BindView(R.id.dp) ImageView dp;
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

        private void bind(@NotNull User user, String userId) {
            try {
                this.userId = userId;
                loadProfilePic(adapter.fragment.glide, user.getThumb_image(), dp);
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
                status.setTypeface(status.getTypeface(), lastChat.isSeen() ? Typeface.NORMAL : Typeface.BOLD);
                status.setTextColor(Color.parseColor(lastChat.isSeen() ? "#9e9e9e" : "#333333"));

                String timeString = getTime(lastChat.getTime_stamp());
                time.setVisibility(timeString != null ? View.VISIBLE : GONE);
                time.setText(timeString);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @OnClick(R.id.dp) public void openProfile() {
            try {
                ViewUtils.openProfile(adapter.fragment.getParentActivity(), userId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @OnClick(R.id.root_layout) public void openChat() {
            try {
                if (adapter.selectedItems.isEmpty()) {
                    if (userId != null) launchChatActivity(adapter.fragment.getParentActivity(), userId);
                } else
                    selectItem(getAdapterPosition());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @OnLongClick(R.id.root_layout) public boolean itemSelected(View view) {
            ((AppCompatActivity)view.getContext()).startSupportActionMode(adapter);
            selectItem(getAdapterPosition());
            return true;
        }

        private void selectItem(int position) {
            if (adapter.multiSelect) {
                if (adapter.selectedItems.contains(position)) {
                    adapter.selectedItems.remove(position);
                    layout.setBackgroundColor(Color.WHITE);
                } else {
                    adapter.selectedItems.add(position);
                    layout.setBackgroundColor(Color.LTGRAY);
                }
            }
        }
    }
}