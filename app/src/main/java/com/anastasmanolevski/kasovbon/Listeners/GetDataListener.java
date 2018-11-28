package com.anastasmanolevski.kasovbon.Listeners;

/**
 * Created by Anastas Manolevski on 27.11.2018.
 */
public interface GetDataListener {
    void onCompleted(String result);
    void onCanceled();
}
