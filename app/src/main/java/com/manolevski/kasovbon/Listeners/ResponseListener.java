package com.manolevski.kasovbon.Listeners;

/**
 * Created by Anastas Manolevski on 27.11.2018.
 */
public interface ResponseListener {
    void onCompleted(String result);
    void onCanceled();
}
