package com.prembros.chatein.ui.chat;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
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
import com.prembros.chatein.R;
import com.prembros.chatein.data.model.Chat;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.prembros.chatein.util.Annotations.ChatType.IMAGE;
import static com.prembros.chatein.util.Annotations.ChatType.TEXT;
import static com.prembros.chatein.util.Constants.DEFAULT;
import static com.prembros.chatein.util.DateUtil.getTime;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private static final int SELF_MESSAGE_FIRST = 0;
    private static final int SELF_MESSAGE_OTHERS = 1;
    private static final int FRIENDS_MESSAGE_FIRST = 2;
    private static final int FRIENDS_MESSAGE_OTHERS = 3;

    private List<Chat> chatList;
    private String currentUserId;
    private RequestManager glide;
    private ViewImageListener listener;

    ChatAdapter(List<Chat> chatList, String currentUserId, RequestManager glide, ViewImageListener listener) {
        this.chatList = chatList;
        this.currentUserId = currentUserId;
        this.glide = glide;
        this.listener = listener;
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
        return new MessageViewHolder(view, listener);
    }

    @Override public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        try {
            Chat chat = chatList.get(position);
            switch (Objects.requireNonNull(chat.getType())) {
                case TEXT:
                    holder.bind(chat, currentUserId);
                    break;
                case IMAGE:
                    holder.bind(chat.getMessage(), glide, chat.getTime_stamp());
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

    @Nullable static ImageView getImage(@NotNull View itemView) {
//        MessageViewHolder holder = (MessageViewHolder) itemView.getTag(R.id.tag_holder);
//        return holder == null ? null : holder.chatImageView;
        return (ImageView) itemView.findViewById(R.id.chat_image);
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {

        private final ViewImageListener listener;
        @BindView(R.id.root_layout) FrameLayout rootLayout;
        @BindView(R.id.text_message_layout) FrameLayout textMessageLayout;
        @BindView(R.id.image_message_layout) FrameLayout imageMessageLayout;
        @BindView(R.id.chat_image) ImageView chatImageView;
        @BindView(R.id.chat) TextView chatView;
        @BindView(R.id.time) TextView timeView;
        @BindView(R.id.image_progress) ProgressBar progressBar;

        MessageViewHolder(View itemView, ViewImageListener listener) {
            super(itemView);
            this.listener = listener;
            ButterKnife.bind(this, itemView);
        }

        private void bind(@NotNull Chat chat, String currentUserId) {
            try {
                textMessageLayout.setVisibility(View.VISIBLE);
                imageMessageLayout.setVisibility(View.GONE);
                chatView.setText(chat.getMessage());
                timeView.setText(getTime(Objects.requireNonNull(chat.getTime_stamp())));
                timeView.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                        Objects.equals(currentUserId, chat.getFrom()) ?
                                Objects.requireNonNull(chat.getSeen()) ? R.drawable.ic_double_tick : 0 : 0, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void bind(String imageUrl, @NotNull RequestManager glide, Long time) {
            try {
                progressBar.setVisibility(View.VISIBLE);
                textMessageLayout.setVisibility(View.GONE);
                imageMessageLayout.setVisibility(View.VISIBLE);
                if (!Objects.equals(imageUrl, DEFAULT)) {
                    glide.load(imageUrl)
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @OnClick(R.id.chat_image) public void viewImage() {
            if (listener != null) listener.viewImage(getAdapterPosition());
        }
    }

    public interface ViewImageListener {
        void viewImage(int position);
    }
}