package com.prembros.chatein.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.prembros.chatein.R;

import butterknife.Unbinder;

public abstract class BaseActivity extends AppCompatActivity {

    protected Unbinder unbinder;
    protected RequestManager glide;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        glide = Glide.with(this);
    }

    protected void setVisibility(View view, int visibility) {
        if (view.getVisibility() != visibility) view.setVisibility(visibility);
    }

    @Override protected void onDestroy() {
        try {
            if (unbinder != null) unbinder.unbind();
//            if (glide != null) glide.onDestroy();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            super.onDestroy();
        }
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.home) onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}