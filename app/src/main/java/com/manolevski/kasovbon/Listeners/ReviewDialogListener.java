package com.manolevski.kasovbon.Listeners;

import android.content.DialogInterface;

/**
 * Created by Anastas Manolevski on 29.05.2019.
 */
public interface ReviewDialogListener {
    void onAccept(DialogInterface dialog);
    void onDismiss(DialogInterface dialog);
}
