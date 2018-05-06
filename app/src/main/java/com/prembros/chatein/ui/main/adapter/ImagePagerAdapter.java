package com.prembros.chatein.ui.main.adapter;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.views.GestureImageView;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.prembros.chatein.data.model.Chat;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
        ImageViewHolder holder = new ImageViewHolder(container);
        holder.image.getController().enableScrollInViewPager(viewPager);
        return holder;
    }

    @Override public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        holder.image.getController().getSettings()
                .setRotationEnabled(true)
                .setRestrictRotation(true)
                .setAnimationsDuration(200);

        glide.load(chatList.get(position).getMessage())
                .apply(RequestOptions.placeholderOf(android.R.drawable.progress_indeterminate_horizontal))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.image);
    }

    @Override public int getCount() {
        return chatList.size();
    }

    @Contract(pure = true) public static GestureImageView getImage(RecyclePagerAdapter.ViewHolder holder) {
        return ((ImageViewHolder) holder).image;
    }

    public static class ImageViewHolder extends RecyclePagerAdapter.ViewHolder {

        final GestureImageView image;

        ImageViewHolder(@NotNull View itemView) {
            super(new GestureImageView(itemView.getContext()));
            image = (GestureImageView) this.itemView;
        }
    }
}