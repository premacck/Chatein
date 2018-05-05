package com.prembros.chatein.ui.auth;

import android.app.ProgressDialog;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.prembros.chatein.R;
import com.prembros.chatein.data.model.UserBuilder;
import com.prembros.chatein.ui.base.BaseAuthActivity;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.prembros.chatein.ui.main.MainActivity.launchMainActivity;
import static com.prembros.chatein.util.CommonUtils.makeSnackBar;
import static com.prembros.chatein.util.Constants.DEFAULT;
import static com.prembros.chatein.util.ViewUtils.hideKeyboard;

public class RegisterActivity extends BaseAuthActivity {

    @BindView(R.id.reg_name) TextInputLayout fullName;
    @BindView(R.id.reg_email) TextInputLayout email;
    @BindView(R.id.reg_password) TextInputLayout password;
    @BindView(R.id.reg_create_account) Button registerBtn;
    @BindView(R.id.register_toolbar) Toolbar toolbar;

    public static void launchRegisterActivity(@NotNull Context from) {
        from.startActivity(new Intent(from, RegisterActivity.class));
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        try {
            unbinder = ButterKnife.bind(this);
            setSupportActionBar(toolbar);
            Objects.requireNonNull(getSupportActionBar()).setTitle("Create Account");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            progressDialog = new ProgressDialog(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.reg_create_account) public void register() {
        try {
            hideKeyboard(this, registerBtn);
            String name = Objects.requireNonNull(fullName.getEditText()).getText().toString();
            String emailText = Objects.requireNonNull(email.getEditText()).getText().toString();
            String pass = Objects.requireNonNull(password.getEditText()).getText().toString();

            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(emailText) && !TextUtils.isEmpty(pass)) {
                progressDialog.setTitle("Registering...");
                progressDialog.setMessage("Please wait while we create your account.");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                registerUser(name, emailText, pass);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerUser(final String name, String emailText, String pass) {
        getViewModel().getAuth().createUserWithEmailAndPassword(emailText, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override public void onComplete(@NonNull Task<AuthResult> task) {
                        try {
                            if (task.isSuccessful()) {
                                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                if (currentUser != null) {
                                    String userId = currentUser.getUid();
                                    getViewModel().getUsers().child(userId).setValue(
                                            UserBuilder.Companion.getNew()
                                                    .setToken(Objects.requireNonNull(FirebaseInstanceId.getInstance().getToken()))
                                                    .setName(name)
                                                    .setStatus(getString(R.string.default_status))
                                                    .setProfileImage(DEFAULT)
                                                    .setThumbImage(DEFAULT)
                                                    .getMap()
                                    ).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                progressDialog.dismiss();
                                                launchMainActivity(RegisterActivity.this);
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
                                makeSnackBar(registerBtn, error);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}