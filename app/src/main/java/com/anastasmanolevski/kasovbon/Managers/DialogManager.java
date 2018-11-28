package com.anastasmanolevski.kasovbon.Managers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;

import com.anastasmanolevski.kasovbon.Listeners.ErrorDialogClickListener;

/**
 * Created by Anastas Manolevski on 27.11.2018.
 */
public class DialogManager {

    public static ProgressDialog initProgressDialog(Activity activity, String message, boolean isCancelable) {
        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(isCancelable);
        return progressDialog;
    }

    public static void errorDialog(Activity activity, String title, String message, final ErrorDialogClickListener listener) {
        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, activity.getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDismiss(dialog);
                    }
                });
        alertDialog.show();
    }
}
