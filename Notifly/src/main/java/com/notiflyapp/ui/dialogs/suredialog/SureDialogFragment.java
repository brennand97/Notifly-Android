/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.ui.dialogs.suredialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.notiflyapp.R;

import java.io.Serializable;

/**
 * Created by Brennan on 5/11/2016.
 */
public class SureDialogFragment extends DialogFragment {

    public static final String MESSAGE = "message";
    public static final String CALLBACK = "callback";

    public interface SureCallback extends Serializable{
        void onPositive();
        void onNegative();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String message = getArguments().getString(MESSAGE);
        final SureCallback callback = (SureCallback) getArguments().get(CALLBACK);
        String title = message;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(title)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(callback != null) {
                            callback.onPositive();
                        }
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(callback != null) {
                            callback.onNegative();
                        }
                    }
                });

        return builder.create();

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }
}
