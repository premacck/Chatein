package com.prembros.chatein.ui.base;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;

import com.alexvasilkov.gestures.views.GestureImageView;
import com.prembros.chatein.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ViewImageActivity extends BaseActivity {

    private static final String IMAGE_PATH = "image_path";

    @BindView(R.id.gesture_image_view) GestureImageView imageView;

    public static void launchImageViewActivity(Activity from, String imagePath, View fromImageView) {
        Intent intent = new Intent(from, ViewImageActivity.class);
        intent.putExtra(IMAGE_PATH, imagePath);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(from, Pair.create(fromImageView, "imageTransition"));
        from.startActivity(intent, options.toBundle());
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);
        unbinder = ButterKnife.bind(this);
        String imagePath = getIntent().getStringExtra(IMAGE_PATH);
        glide.load(imagePath).into(imageView);
    }

    @OnClick(R.id.back_btn) public void exit() {
        onBackPressed();
    }
}