package com.prembros.chatein.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ServerValue;
import com.prembros.chatein.R;
import com.prembros.chatein.data.viewmodel.DatabaseViewModel;
import com.prembros.chatein.ui.base.DatabaseActivity;
import com.prembros.chatein.ui.main.adapter.SectionsPagerAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.prembros.chatein.ui.account.AccountSettingsActivity.launchAccountSettingsActivity;
import static com.prembros.chatein.ui.auth.StartActivity.launchStartActivity;
import static com.prembros.chatein.ui.social.UsersListActivity.launchUsersActivity;
import static com.prembros.chatein.util.CommonUtils.saveUserLocally;
import static com.prembros.chatein.util.Constants.ONLINE;
import static com.prembros.chatein.util.SharedPrefs.removeSavedUser;

public class MainActivity extends DatabaseActivity {

    @BindView(R.id.main_activity_toolbar) Toolbar toolbar;
    @BindView(R.id.view_pager) ViewPager viewPager;
    @BindView(R.id.main_tabs) TabLayout tabLayout;
//    @BindView(R.id.action_delete) ImageButton deleteBtn;

    public static void launchMainActivity(@NotNull Context from) {
        saveUserLocally(from);
        Intent intent = new Intent(from, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        from.startActivity(intent);
    }

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            unbinder = ButterKnife.bind(this);
            setSupportActionBar(toolbar);
            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.app_name);

            SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
            viewPager.setAdapter(sectionsPagerAdapter);
            tabLayout.setupWithViewPager(viewPager);
            viewPager.setCurrentItem(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override public void onStart() {
        super.onStart();
        if (currentUser != null) {
            currentUserRef.child(ONLINE).setValue("true");
        }
    }

    @Override public void onDetachedFromWindow() {
        if (currentUser != null && currentUserRef != null) {
            currentUserRef.child(ONLINE).setValue(ServerValue.TIMESTAMP);
        }
        super.onDetachedFromWindow();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                DatabaseViewModel.destroyInstance();
                FirebaseAuth.getInstance().signOut();
                removeSavedUser(this);
                launchStartActivity(this);
                finish();
                return true;
            case R.id.action_settings:
                launchAccountSettingsActivity(this);
                return true;
            case R.id.action_all_users:
                launchUsersActivity(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    public void showDeleteButton() {
//        setVisibility(deleteBtn, VISIBLE);
//    }
//
//    public void hideDeleteButton() {
//        setVisibility(deleteBtn, GONE);
//    }

    @Override public void onBackPressed() {
        if (viewPager.getCurrentItem() != 1)
            viewPager.setCurrentItem(1, true);
        else
            super.onBackPressed();
    }
}