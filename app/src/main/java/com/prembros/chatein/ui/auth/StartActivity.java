package com.prembros.chatein.ui.auth;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.prembros.chatein.R;
import com.prembros.chatein.data.model.UserBuilder;
import com.prembros.chatein.ui.base.BaseAuthActivity;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.prembros.chatein.ui.auth.LoginActivity.launchLoginActivity;
import static com.prembros.chatein.ui.auth.RegisterActivity.launchRegisterActivity;
import static com.prembros.chatein.ui.main.MainActivity.launchMainActivity;
import static com.prembros.chatein.util.CommonUtils.makeSnackBar;
import static com.prembros.chatein.util.Constants.USERS;

public class StartActivity extends BaseAuthActivity {

    private static final int RC_SIGN_IN = 101;

    @BindView(R.id.root_layout) CoordinatorLayout layout;

    private AnimationDrawable anim;

    public static void launchStartActivity(@NotNull Context from) {
        from.startActivity(new Intent(from, StartActivity.class));
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        unbinder = ButterKnife.bind(this);

        anim = (AnimationDrawable) layout.getBackground();
        anim.setEnterFadeDuration(2000);
        anim.setExitFadeDuration(4000);
    }

    @Override protected void onResume() {
        super.onResume();
        if (anim != null && !anim.isRunning()) anim.start();
    }

    @Override protected void onPause() {
        super.onPause();
        if (anim != null && anim.isRunning()) anim.stop();
    }

    @OnClick(R.id.register_btn) public void startRegistration() {
        launchRegisterActivity(this);
    }

    @OnClick(R.id.login_btn) public void startLogin() {
        launchLoginActivity(this);
    }

    @OnClick(R.id.google_sign_in_btn) public void signInWithGoogle() {
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .requestProfile()
                .build();
        Intent signInIntent = GoogleSignIn.getClient(this, googleSignInOptions).getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                if (account != null)
                    handleGoogleSignIn(account);
                else Log.d("GOOGLE_SIGN_IN: ", "account is null!!!!");
            }
            else makeSnackBar(layout, "Google sign in failed!");
        }
    }

    private void handleGoogleSignIn(@NotNull final GoogleSignInAccount account) {
//        saveUserAccount(getApplicationContext(), getAccount(account));
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        viewModel.getAuth().signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            makeSnackBar(layout, "Authentication Failed");
                            Log.e("handleGoogleSignIn", "signInWithCredential:failure", task.getException());
                        } else {
//                            Sign in Successful
                            progressDialog = new ProgressDialog(StartActivity.this);
                            progressDialog.setTitle("Just a moment");
                            progressDialog.setMessage("Connecting with Google..");
                            progressDialog.setCanceledOnTouchOutside(false);
                            progressDialog.show();
                            registerUser(account);
                        }
                    }
                });
    }

    private void registerUser(@NotNull final GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        viewModel.getAuth().signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override public void onComplete(@NonNull Task<AuthResult> task) {
                        try {
                            if (task.isSuccessful()) {
                                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                if (currentUser != null) {
                                    String userId = currentUser.getUid();
                                    FirebaseDatabase.getInstance().getReference().child(USERS).child(userId).setValue(
                                            UserBuilder.Companion.getNew()
                                                    .setToken(Objects.requireNonNull(FirebaseInstanceId.getInstance().getToken()))
                                                    .setName(Objects.requireNonNull(account.getDisplayName()))
                                                    .setStatus(getString(R.string.default_status))
                                                    .setProfileImage(Objects.requireNonNull(account.getPhotoUrl()).toString())
                                                    .setThumbImage(Objects.requireNonNull(account.getPhotoUrl()).toString())
                                                    .getMap()
                                    ).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                progressDialog.dismiss();
                                                launchMainActivity(StartActivity.this);
                                                finish();
                                            }
                                        }
                                    });
                                }
                            } else {
                                String error;
                                try {
                                    throw Objects.requireNonNull(task.getException());
                                } catch (FirebaseAuthWeakPasswordException e) {
                                    error = "Password must be more than 6 characters";
                                    e.printStackTrace();
                                } catch (FirebaseAuthInvalidCredentialsException e) {
                                    error = "Invalid Email";
                                    e.printStackTrace();
                                } catch (FirebaseAuthUserCollisionException e) {
                                    error = "Account already exists! Login instead";
                                    e.printStackTrace();
                                } catch (Exception e) {
                                    error = getString(R.string.something_went_wrong);
                                    e.printStackTrace();
                                }
                                progressDialog.hide();
                                makeSnackBar(layout, error);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}