package com.prembros.chatein.base;

import android.support.v4.app.DialogFragment;

import butterknife.Unbinder;

/**
 *
 * Created by Prem$ on 3/19/2018.
 */

public class BaseDialogFragment extends DialogFragment {

    protected Unbinder unbinder;

    @Override public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) unbinder.unbind();
    }
}