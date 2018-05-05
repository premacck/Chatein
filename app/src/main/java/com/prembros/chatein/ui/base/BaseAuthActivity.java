package com.prembros.chatein.ui.base;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.prembros.chatein.data.viewmodel.AuthViewModel;

public abstract class BaseAuthActivity extends BaseActivity {

    protected AuthViewModel viewModel;
    protected ProgressDialog progressDialog;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = AuthViewModel.getInstance();
        progressDialog = new ProgressDialog(this);
    }

    public AuthViewModel getViewModel() {
        return viewModel;
    }
}