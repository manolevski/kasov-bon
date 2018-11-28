package com.anastasmanolevski.kasovbon.AsyncTasks;

import android.os.AsyncTask;

import com.anastasmanolevski.kasovbon.Listeners.SendDataListener;
import com.anastasmanolevski.kasovbon.Utils.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

public class SendDataTask extends AsyncTask<Void, Void, Boolean> {

    private SendDataListener listener;
    private final String date;
    private final String time;
    private final String price;
    private final String posTime;
    private String cookie;

    public SendDataTask(String date, String time, String price, String posTime, String cookie, SendDataListener listener) {
        this.date = date;
        this.time = time;
        this.price = price;
        this.posTime = posTime;
        this.cookie = cookie;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(Constants.RECEIPT_URL);
        httppost.setHeader("Cookie", "RAICHUADMSESSID="+ cookie);

        HttpResponse response;
        try {
            List<NameValuePair> nameValuePairs = new ArrayList<>(2);
            nameValuePairs.add(new BasicNameValuePair("bon_date", date));
            nameValuePairs.add(new BasicNameValuePair("bon_hour_minutes", time));
            nameValuePairs.add(new BasicNameValuePair("bon_value", price));
            if (!posTime.equals("")) {
                nameValuePairs.add(new BasicNameValuePair("pos_hour_minutes", time));
            }
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            response = httpclient.execute(httppost);
        } catch (IOException e) {
            return false;
        }
        if(response == null) return false;
        Header location = response.getFirstHeader("Location");
        if(location == null) return false;
        return location.getValue().contains("#receipt");
    }

    @Override
    protected void onPostExecute(final Boolean result) {
        listener.onCompleted(result);
    }

    @Override
    protected void onCancelled() {
        listener.onCanceled();

    }
}
