package com.prembros.chatein.ui.chat;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.prembros.chatein.R;
import com.prembros.chatein.data.model.Chat;
import com.prembros.chatein.data.model.UpdateRequest;
import com.prembros.chatein.util.FileUtil;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

import static com.prembros.chatein.ui.base.ViewImageActivity.launchImageViewActivity;
import static com.prembros.chatein.util.Annotations.ChatType.FILE;
import static com.prembros.chatein.util.Annotations.ChatType.IMAGE;
import static com.prembros.chatein.util.Annotations.ChatType.TEXT;
import static com.prembros.chatein.util.CommonUtils.showToast;
import static com.prembros.chatein.util.Constants.DEFAULT;
import static com.prembros.chatein.util.Constants.MESSAGES_;
import static com.prembros.chatein.util.Constants.MESSAGE_IMAGES;
import static com.prembros.chatein.util.DateUtil.getTime;
import static java.util.Objects.requireNonNull;

@SuppressLint("UseSparseArrays")
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> implements ActionMode.Callback {

    private static final int SELF_MESSAGE_FIRST = 0;
    private static final int SELF_MESSAGE_OTHERS = 1;
    private static final int FRIENDS_MESSAGE_FIRST = 2;
    private static final int FRIENDS_MESSAGE_OTHERS = 3;

    private List<Chat> chatList;
    private String currentUserId;
    private String friendUserId;
    private RequestManager glide;
    private ChatActivity activity;

    private boolean multiSelect;
    private Map<Integer, Chat> selectedItems;
    private ActionMode mode;

    ChatAdapter(List<Chat> chatList, String currentUserId, String friendUserId, RequestManager glide, ChatActivity activity) {
        this.chatList = chatList;
        this.currentUserId = currentUserId;
        this.friendUserId = friendUserId;
        this.glide = glide;
        this.activity = activity;
        multiSelect = false;
        selectedItems = new HashMap<>();
    }

    @NonNull @Override public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        switch (viewType) {
            case FRIENDS_MESSAGE_FIRST:
                view =  inflater.inflate(R.layout.item_chat_friends_first, parent, false);
                break;
            case FRIENDS_MESSAGE_OTHERS:
                view =  inflater.inflate(R.layout.item_chat_friends_others, parent, false);
                break;
            case SELF_MESSAGE_FIRST:
                view =  inflater.inflate(R.layout.item_chat_self_first, parent, false);
                break;
            case SELF_MESSAGE_OTHERS:
                view =  inflater.inflate(R.layout.item_chat_self_others, parent, false);
                break;
            default:
                view =  inflater.inflate(R.layout.item_chat_friends_first, parent, false);
                break;
        }
        view.setTag(R.id.tag_holder);
        return new MessageViewHolder(view, this);
    }

    @Override public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        try {
            Chat chat = chatList.get(position);
            switch (requireNonNull(chat.getType())) {
                case FILE:
                case TEXT:
                    holder.bind(chat);
                    break;
                case IMAGE:
                    holder.bind(chat, glide, chat.getTime_stamp());
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override public int getItemCount() {
        return chatList.size();
    }

    @Override public int getItemViewType(int position) {
        if (currentUserId.equals(chatList.get(position).getFrom())) {
            return getChatType(position, true);
        } else {
            return getChatType(position, false);
        }
    }

    private int getChatType(int position, boolean isSelf) {
        try {
            if (position == 0) return isSelf ? SELF_MESSAGE_FIRST : FRIENDS_MESSAGE_FIRST;
            else if (Objects.equals(chatList.get(position).getFrom(), chatList.get(position - 1).getFrom()))
                return isSelf ? SELF_MESSAGE_OTHERS : FRIENDS_MESSAGE_OTHERS;
            else return isSelf ? SELF_MESSAGE_FIRST : FRIENDS_MESSAGE_FIRST;
        } catch (Exception e) {
            e.printStackTrace();
            return isSelf ? SELF_MESSAGE_FIRST : FRIENDS_MESSAGE_FIRST;
        }
    }

    @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        this.mode = mode;
        multiSelect = true;
        return true;
    }

    @Override public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        this.mode = mode;
        menu.clear();
        if (selectedItems.size() <= 1) {
            mode.getMenuInflater().inflate(R.menu.menu_chat_single, menu);
        } else {
            mode.getMenuInflater().inflate(R.menu.menu_chat_multiple, menu);
        }
        return true;
    }

    @Override public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
        try {
            switch (item.getItemId()) {
                case R.id.action_copy:
                    Chat chat = selectedItems.get(0);
                    if (Objects.equals(chat.getType(), TEXT)) {
                        StringBuilder text = new StringBuilder();
                        text.append(chat.getMessage());
                        ClipData clip = ClipData.newPlainText(text.toString(), text.toString());
                        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                        if (clipboard != null)
                            clipboard.setPrimaryClip(clip);
                        showToast(activity, R.string.copied_to_clipboard);
                        finishModeAndNotify();
                    } else if (Objects.equals(chat.getType(), IMAGE)) {
                        showToast(activity, R.string.cannot_copy_image);
                    } else
                        showToast(activity, R.string.cannot_copy_file);
                    break;
                case R.id.action_delete:
                    String suffix = selectedItems.size() <=1 ? " this message?\n\n" : " selected messages?\n\n";
                    new AlertDialog.Builder(activity)
                            .setMessage("Are you sure you want to delete" + suffix + "This cannot be undone.")
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String chatBranch = currentUserId + "/" + friendUserId;
                                    activity.actionInProgress();
                                    for (int index : selectedItems.keySet()) {
                                        String key = selectedItems.get(index).getKey();
                                        String friendUserBranch = chatBranch + "/" + key;
                                        try {
                                            if (Objects.equals(selectedItems.get(index).getType(), IMAGE)) {
                                                deleteImageFromStorage(friendUserBranch);
                                            }
                                            else deleteChat(friendUserBranch);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    chatList.removeAll(selectedItems.values());
                                }
                            })
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private void deleteChat(String friendUserBranch) {
        UpdateRequest.forDatabase(activity.getRootRef())
                .put(MESSAGES_ + friendUserBranch, null)
                .update(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError error, DatabaseReference ref) {
                        finishModeAndNotify();
                        activity.actionCompleted();
                    }
                });
    }

    private void deleteImageFromStorage(final String friendUserBranch) {
        final StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child(MESSAGE_IMAGES).child(friendUserBranch + ".JPG");
        try {
            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    storageRef.delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    try {
                                        activity.actionCompleted();
                                        if (!task.isSuccessful()) Objects.requireNonNull(task.getException()).printStackTrace();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                }
            });
            deleteChat(friendUserBranch);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override public void onDestroyActionMode(ActionMode mode) {
        this.mode = mode;
        finishModeAndNotify();
    }

    private void finishModeAndNotify() {
        multiSelect = false;
        selectedItems.clear();
        notifyDataSetChanged();
        mode.finish();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {

        private static final String SELECTED_MASK = "#20333333";

        @BindView(R.id.root_layout) FrameLayout layout;
        @BindView(R.id.text_message_layout) FrameLayout textMessageLayout;
        @BindView(R.id.image_message_layout) FrameLayout imageMessageLayout;
        @BindView(R.id.chat_image) ImageView chatImageView;
        @BindView(R.id.chat) TextView chatView;
        @BindView(R.id.time) TextView timeView;
        @BindView(R.id.image_progress) ProgressBar progressBar;

        private final ChatAdapter adapter;
        private Chat chat;

        MessageViewHolder(View itemView, ChatAdapter adapter) {
            super(itemView);
            this.adapter = adapter;
            ButterKnife.bind(this, itemView);
        }

        private void bind(@NotNull Chat chat) {
            this.chat = chat;
            try {
                textMessageLayout.setVisibility(View.VISIBLE);
                imageMessageLayout.setVisibility(View.GONE);
                chatView.setText(
                        Objects.equals(chat.getType(), TEXT) ?
                                chat.getMessage() :
                                "*** " + FileUtil.getExtension(chat.getMessage()) + " file ***"
                );
                timeView.setText(getTime(requireNonNull(chat.getTime_stamp())));
                maybeSetSeen();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void bind(Chat chat, @NotNull RequestManager glide, Long time) {
            try {
                this.chat = chat;
                progressBar.setVisibility(View.VISIBLE);
                textMessageLayout.setVisibility(View.GONE);
                imageMessageLayout.setVisibility(View.VISIBLE);
                if (!Objects.equals(chat.getMessage(), DEFAULT)) {
                    glide.load(chat.getMessage())
                            .listener(new RequestListener<Drawable>() {
                                @Override public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                            Target<Drawable> target, boolean isFirstResource) {
                                    progressBar.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                                                               DataSource dataSource, boolean isFirstResource) {
                                    progressBar.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(chatImageView);
                }
                timeView.setText(getTime(time));
                maybeSetSeen();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void maybeSetSeen() {
            layout.setBackgroundColor(adapter.selectedItems.containsValue(chat) ? Color.parseColor(SELECTED_MASK) : Color.TRANSPARENT);
            timeView.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                    Objects.equals(adapter.currentUserId, chat.getFrom()) ?
                            requireNonNull(chat.getSeen()) ? R.drawable.ic_double_tick :
                                    R.drawable.ic_tick : 0, 0);
        }

        @OnClick(R.id.chat) public void openFile() {
            if (Objects.equals(chat.getType(), FILE))showToast(adapter.activity, "File handler coming soon");

            /*PackageManager pm = adapter.activity.getPackageManager();
//            This is the file you want to show
            File file = new File("filename" + FileUtil.getExtension(chat.getMessage()));
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.fromFile(file));
//            Determine if Android can resolve this implicit Intent
            ResolveInfo info = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);

            if (info != null) {
                // There is something installed that can VIEW this file)
            } else {
                // Offer to download a viewer here
            }*/
        }

        @OnClick(R.id.chat_image) public void viewImage() {
            if (!adapter.multiSelect) {
//                if (listener != null) listener.viewImage(getAdapterPosition());
                launchImageViewActivity(adapter.activity, chat.getMessage(), chatImageView);
            }
            else selectItem(getAdapterPosition());
        }

        @OnClick(R.id.root_layout) public void openChat() {
            try {
                if (adapter.multiSelect && !adapter.selectedItems.isEmpty()) {
                    selectItem(getAdapterPosition());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @OnLongClick(R.id.chat_image) public boolean imageSelected(View view) {
            return startActionMode(view);
        }

        @OnLongClick(R.id.root_layout) public boolean itemSelected(View view) {
            return startActionMode(view);
        }

        private boolean startActionMode(@NotNull View view) {
            if (!adapter.multiSelect) ((AppCompatActivity)view.getContext()).startSupportActionMode(adapter);
            selectItem(getAdapterPosition());
            return true;
        }

        private void selectItem(int position) {
            if (adapter.multiSelect) {
                if (adapter.selectedItems.containsValue(chat)) {
                    adapter.selectedItems.remove(position);
                    layout.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    adapter.selectedItems.put(position, chat);
                    layout.setBackgroundColor(Color.parseColor(SELECTED_MASK));
                }

                if (adapter.selectedItems.isEmpty()) {
                    adapter.multiSelect = false;
                    adapter.mode.finish();
                } else adapter.mode.setTitle(adapter.selectedItems.size() +
                        (adapter.selectedItems.size() <= 1 ? " Chat" : " Chats"));
                adapter.mode.invalidate();
            }
        }
    }
}