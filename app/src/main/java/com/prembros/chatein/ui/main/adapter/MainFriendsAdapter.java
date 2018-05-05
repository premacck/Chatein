package com.prembros.chatein.ui.main.adapter;

import android.content.DialogInterface;
import android.graphics.Color;
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
import com.google.firebase.database.DatabaseReference;
import com.prembros.chatein.R;
import com.prembros.chatein.base.BaseFragment;
import com.prembros.chatein.data.model.User;
import com.prembros.chatein.ui.base.SelectableFirebaseAdapter;
import com.prembros.chatein.util.CustomValueEventListener;
import com.prembros.chatein.util.ViewUtils;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

import static com.prembros.chatein.ui.chat.ChatActivity.launchChatActivity;
import static com.prembros.chatein.util.Constants.ONLINE;
import static com.prembros.chatein.util.ViewUtils.loadProfilePic;

public class MainFriendsAdapter extends SelectableFirebaseAdapter<User, MainFriendsAdapter.FriendsViewHolder> {

    private final BaseFragment fragment;

    public MainFriendsAdapter(@NonNull FirebaseRecyclerOptions<User> options, BaseFragment fragment) {
        super(options);
        this.fragment = fragment;
    }

    @Override protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull User model) {
        try {
            final String userId = getRef(position).getKey();

            DatabaseReference userRef = fragment.getParentActivity().getUsersRef().child(userId);

            userRef.addListenerForSingleValueEvent(new CustomValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    holder.bind(new User(dataSnapshot), userId);
                }
            });

            userRef.addValueEventListener(new CustomValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        if (dataSnapshot.hasChild(ONLINE)) {
                            Object isOnline = dataSnapshot.child(ONLINE).getValue();
                            holder.onlineStatus.setVisibility(
                                    !(isOnline instanceof Long) && Objects.equals(isOnline, "true") ?
                                            View.VISIBLE : View.GONE);
                        } else holder.onlineStatus.setVisibility(View.GONE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull @Override public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        fragment.dataAvailable();
        return new MainFriendsAdapter.FriendsViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_list, parent, false),
                this);
    }

    @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        multiSelect = true;
        menu.add(0, R.id.action_un_friend, 0, R.string.un_friend);
        return true;
    }

    @Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        try {
            String suffix = selectedItems.size() <=1 ? " this person?\n" : " selected people?\n";
            new AlertDialog.Builder(fragment.getParentActivity())
                    .setTitle("Alert")
                    .setMessage("Are you sure you want to unfriend" + suffix + "This cannot be undone.")
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            for (int index : selectedItems) {
                                try {
                                    fragment.getParentActivity()
                                            .getMyFriendsRef(getRef(index).getKey())
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

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.root_layout) LinearLayout layout;
        @BindView(R.id.dp) ImageView dp;
        @BindView(R.id.online) ImageView onlineStatus;
        @BindView(R.id.name) TextView name;
        @BindView(R.id.status) TextView status;

        private String userId;
        private final MainFriendsAdapter adapter;

        FriendsViewHolder(View itemView, MainFriendsAdapter adapter) {
            super(itemView);
            this.adapter = adapter;
            ButterKnife.bind(this, itemView);
        }

        public void bind(User user, String userId) {
            this.userId = userId;
            loadProfilePic(adapter.fragment.glide, user.getThumb_image(), dp);
            name.setText(user.getName());
            status.setText(user.getStatus());
        }

        @OnClick(R.id.root_layout) public void openChat() {
            try {
                if (userId != null) launchChatActivity(adapter.fragment.getParentActivity(), userId);
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
