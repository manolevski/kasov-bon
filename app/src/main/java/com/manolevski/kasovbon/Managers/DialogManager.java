package com.manolevski.kasovbon.Managers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.manolevski.kasovbon.Listeners.ErrorDialogClickListener;
import com.manolevski.kasovbon.Listeners.ReviewDialogListener;
import com.manolevski.kasovbon.R;

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

    public static void reviewDialog(Activity activity, String title, String message, final ReviewDialogListener listener) {
        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, activity.getString(R.string.no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDismiss(dialog);
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, activity.getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onAccept(dialog);
                    }
                });
        alertDialog.show();
    }

    public static void datePickerDialog(Context context, int year, int month, int day, DatePickerDialog.OnDateSetListener listener) {
        DatePickerDialog datePicker = new DatePickerDialog(context, listener, year, month, day);
        datePicker.show();
    }

    public static void timePickerDialog(Context context, int hour, int minute, TimePickerDialog.OnTimeSetListener listener) {
        TimePickerDialog timePicker = new TimePickerDialog(context, listener, hour, minute, true);
        timePicker.show();
    }
}
