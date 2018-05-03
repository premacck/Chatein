package com.prembros.chatein.ui.account;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.prembros.chatein.R;
import com.prembros.chatein.data.model.User;
import com.prembros.chatein.data.model.UserUpdater;
import com.prembros.chatein.ui.base.DatabaseActivity;
import com.prembros.chatein.util.CustomValueEventListener;
import com.theartofdev.edmodo.cropper.CropImage;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;

import static com.prembros.chatein.util.CommonUtils.makeSnackBar;
import static com.prembros.chatein.util.Constants.DEFAULT;
import static com.prembros.chatein.util.Constants.ONLINE;
import static com.prembros.chatein.util.Constants.PROFILE_IMAGES;
import static com.prembros.chatein.util.Constants.THUMBS;
import static com.prembros.chatein.util.ViewUtils.compressImage;

public class AccountSettingsActivity extends DatabaseActivity {

    private static final int GALLERY_PICK = 1;

    @BindView(R.id.dp) ImageView dp;
    @BindView(R.id.edit_dp) ImageButton editDp;
    @BindView(R.id.name) TextView name;
    @BindView(R.id.status) TextView status;
    @BindView(R.id.progress_bar) CircularProgressBar progressBar;

    private StorageReference storageReference;
    private String currentUserId;
    private boolean started;

    public static void launchAccountSettingsActivity(@NotNull Context from) {
        from.startActivity(new Intent(from, AccountSettingsActivity.class));
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        unbinder = ButterKnife.bind(this);
        progressBar.setVisibility(View.VISIBLE);
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            userDatabase.keepSynced(true);
            storageReference = FirebaseStorage.getInstance().getReference();
            updateUI();
        }
    }

    @Override public void onStart() {
        super.onStart();
        started = true;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) userDatabase.child(ONLINE).setValue(true);
    }

    @Override protected void onStop() {
        super.onStop();
        started = false;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) userDatabase.child(ONLINE).setValue(false);
    }

    private void updateUI() {
        try {
            if (currentUser != null) {
                userDatabase.addValueEventListener(new CustomValueEventListener() {
                    @Override public void onDataChange(DataSnapshot dataSnapshot) {
                        if (started) {
                            User user = new User(dataSnapshot);
                            glide.load(user.getProfile_image())
                                    .apply(RequestOptions.circleCropTransform()
                                            .placeholder(R.drawable.ic_add_user)
                                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE))
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
                                    .into(dp);
                            glide.load(!Objects.equals(user.getProfile_image(), DEFAULT) ?
                                    R.drawable.ic_edit_dp :
                                    R.drawable.ic_add_dp)
                                    .into(editDp);

                            name.setText(user.getName());
                            String statusText = user.getStatus();
                            status.setText(!Objects.equals(statusText, DEFAULT) ? statusText : null);
                            status.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.back_btn) public void exit() {
        finish();
    }

    @OnClick(R.id.status) public void editStatus() {
        EditStatusDialogFragment.newInstance(status.getText().toString()).show(getSupportFragmentManager(), EditStatusDialogFragment.TAG);
    }

    @OnClick(R.id.dp) public void editProfilePic1() {
        editDp();
    }

    @OnClick(R.id.edit_dp) public void editProfilePic2() {
        editDp();
    }

    private void editDp() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent, "Select Profile Pic"), GALLERY_PICK);
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
                Uri imageUri = data.getData();
                CropImage.activity(imageUri)
                        .setAspectRatio(1, 1)
                        .start(this);
            }
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    progressBar.setVisibility(View.VISIBLE);
//                    get image URI
                    Uri resultUri = result.getUri();

//                    Compress the image to make thumb image
                    final byte[] thumbByteData = compressImage(this, resultUri);

//                    Database paths of original image
                    StorageReference imagePath = storageReference.child(PROFILE_IMAGES).child(currentUserId + ".jpg");

//                    First upload the original image
                    imagePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                final String imageUrl = Objects.requireNonNull(task.getResult().getDownloadUrl()).toString();
//                                if original image is uploaded, upload thumbnail
                                if (thumbByteData != null)
                                    uploadThumbnailAndUpdateDatabase(thumbByteData, imageUrl);
                                else
                                    makeSnackBar(dp, R.string.failed_to_compress_image);
                            } else
                                makeSnackBar(dp, R.string.failed_to_upload_photo);
                        }
                    });
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
                    result.getError().printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadThumbnailAndUpdateDatabase(byte[] thumbByteData, final String imageUrl) {
//        Database paths of original image
        StorageReference thumbPath = storageReference.child(PROFILE_IMAGES).child(THUMBS).child(currentUserId + ".jpg");
//        Uploading thumbnail
        thumbPath.putBytes(thumbByteData).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    final String thumbUrl = Objects.requireNonNull(task.getResult().getDownloadUrl()).toString();

//                    if both the images are uploaded, update the corresponding values in database.
                    userDatabase.updateChildren(
                            UserUpdater.Companion.getNew()
                                    .setProfileImage(imageUrl)
                                    .setThumbImage(thumbUrl)
                                    .getMap()
                    );
                } else makeSnackBar(dp, R.string.failed_to_upload_thumbnail);
            }
        });
    }
}