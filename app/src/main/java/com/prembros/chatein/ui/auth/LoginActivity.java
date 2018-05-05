package com.prembros.chatein.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.iid.FirebaseInstanceId;
import com.prembros.chatein.R;
import com.prembros.chatein.ui.base.BaseAuthActivity;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.prembros.chatein.ui.main.MainActivity.launchMainActivity;
import static com.prembros.chatein.util.CommonUtils.makeSnackBar;
import static com.prembros.chatein.util.Constants.DEVICE_TOKEN;
import static com.prembros.chatein.util.ViewUtils.hideKeyboard;

public class LoginActivity extends BaseAuthActivity {

    @BindView(R.id.login_email) TextInputLayout email;
    @BindView(R.id.login_password) TextInputLayout password;
    @BindView(R.id.login_btn) Button loginBtn;
    @BindView(R.id.login_toolbar) Toolbar toolbar;

    public static void launchLoginActivity(@NotNull Context from) {
        from.startActivity(new Intent(from, LoginActivity.class));
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        try {
            unbinder = ButterKnife.bind(this);
            setSupportActionBar(toolbar);
            Objects.requireNonNull(getSupportActionBar()).setTitle("Login");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.login_btn) public void performLogin() {
        try {
            hideKeyboard(this, loginBtn);
            String emailText = Objects.requireNonNull(email.getEditText()).getText().toString();
            String pass = Objects.requireNonNull(password.getEditText()).getText().toString();

            if (!TextUtils.isEmpty(emailText) && !TextUtils.isEmpty(pass)) {
                progressDialog.setMessage("Logging in...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                loginUser(emailText, pass);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loginUser(String email, String pass) {
        try {
            getViewModel().getAuth().signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        String currentUserId = Objects.requireNonNull(getViewModel().getAuth().getCurrentUser()).getUid();
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();
                        getViewModel().getUsers().child(currentUserId).child(DEVICE_TOKEN).setValue(deviceToken)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        progressDialog.dismiss();
                                        launchMainActivity(LoginActivity.this);
                                        finish();
                                    }
                                });

                    } else {
                        String error;
                        try {
                            throw Objects.requireNonNull(task.getException());
                        } catch (FirebaseAuthInvalidUserException e) {
                            error = "Invalid Email!";
                            e.printStackTrace();
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            error = "Invalid Password!";
                            e.printStackTrace();
                        } catch (Exception e) {
                            error = getString(R.string.something_went_wrong);
                            e.printStackTrace();
                        }
                        progressDialog.hide();
                        makeSnackBar(loginBtn, error);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
