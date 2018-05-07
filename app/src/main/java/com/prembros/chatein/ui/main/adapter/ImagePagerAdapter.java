package com.prembros.chatein.ui.main.adapter;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.views.GestureImageView;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.prembros.chatein.R;
import com.prembros.chatein.data.model.Chat;
import com.prembros.chatein.util.FileUtil;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.prembros.chatein.util.Annotations.ChatType.IMAGE;
import static com.prembros.chatein.util.Annotations.ChatType.TEXT;

public class ImagePagerAdapter extends RecyclePagerAdapter<ImagePagerAdapter.ImageViewHolder> {

    private final ViewPager viewPager;
    private final List<Chat> chatList;
    private final RequestManager glide;

    public ImagePagerAdapter(ViewPager viewPager, List<Chat> chatList, RequestManager glide) {
        this.viewPager = viewPager;
        this.chatList = chatList;
        this.glide = glide;
    }

    @Override public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup container) {
        View view = LayoutInflater.from(viewPager.getContext()).inflate(R.layout.item_chat_pager, container, false);
        return new ImageViewHolder(view);
    }

    @Override public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        try {
            holder.image.getController().getSettings()
                    .setRotationEnabled(true)
                    .setRestrictRotation(true)
                    .setAnimationsDuration(200);

            if (Objects.equals(chatList.get(position).getType(), IMAGE)) {
                holder.image.setVisibility(View.VISIBLE);
                holder.message.setVisibility(View.GONE);
                glide.load(chatList.get(position).getMessage())
                        .apply(RequestOptions.placeholderOf(android.R.drawable.progress_indeterminate_horizontal))
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(holder.image);
            } else {
                holder.image.setVisibility(View.GONE);
                holder.message.setVisibility(View.VISIBLE);
                holder.message.setText(
                        Objects.equals(chatList.get(position).getType(), TEXT) ?
                                chatList.get(position).getMessage() :
                                FileUtil.getExtension(chatList.get(position).getMessage()) + " file"
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override public int getCount() {
        return chatList.size();
    }

    @Contract(pure = true) public static GestureImageView getImage(RecyclePagerAdapter.ViewHolder holder) {
        return ((ImageViewHolder) holder).image;
    }

    public static class ImageViewHolder extends RecyclePagerAdapter.ViewHolder {

        @BindView(R.id.gesture_image_view) GestureImageView image;
        @BindView(R.id.message) TextView message;

        ImageViewHolder(@NotNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}