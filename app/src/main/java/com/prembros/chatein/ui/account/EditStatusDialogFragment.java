package com.prembros.chatein.ui.account;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.prembros.chatein.R;
import com.prembros.chatein.base.BaseDialogFragment;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.prembros.chatein.util.CommonUtils.showErrorToast;
import static com.prembros.chatein.util.Constants.DEFAULT;
import static com.prembros.chatein.util.ViewUtils.disableView;
import static com.prembros.chatein.util.ViewUtils.enableView;
import static com.prembros.chatein.util.ViewUtils.hideKeyboard;
import static com.prembros.chatein.util.ViewUtils.showKeyboard;

public class EditStatusDialogFragment extends BaseDialogFragment {

    public static final String TAG = "EditStatusDialogFragment";
    private static final String OLD_STATUS = "oldStatus";

    @BindView(R.id.new_status) TextInputLayout newStatus;
    @BindView(R.id.progress_bar) ProgressBar progressBar;

    private String oldStatus;

    private AccountSettingsActivity getParentActivity() {
        return (AccountSettingsActivity) getActivity();
    }

    @NonNull public static EditStatusDialogFragment newInstance(String status) {
        EditStatusDialogFragment fragment = new EditStatusDialogFragment();
        Bundle args = new Bundle();
        args.putString(OLD_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            oldStatus = getArguments().getString(OLD_STATUS);
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_status, container);
        unbinder = ButterKnife.bind(this, rootView);
        setCancelable(false);
        try {
            Objects.requireNonNull(newStatus.getEditText()).setText(oldStatus);
            newStatus.getEditText().requestFocus();
            showKeyboard(getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rootView;
    }

    @OnClick(R.id.save_status) public void saveNewStatus() {
        try {
            disableView(newStatus, true);
            progressBar.setVisibility(View.VISIBLE);
            String status = Objects.requireNonNull(newStatus.getEditText()).getText().toString();
            if (status.isEmpty()) status = DEFAULT;
            getParentActivity().getCurrentUserRef().child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override public void onComplete(@NonNull Task<Void> task) {
                    enableView(newStatus);
                    progressBar.setVisibility(View.GONE);
                    if (!task.isSuccessful()) showErrorToast(getContext());
                    dismiss();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.discard_status) public void discardStatus() {
        hideKeyboard(getActivity(), newStatus);
        dismiss();
    }
}