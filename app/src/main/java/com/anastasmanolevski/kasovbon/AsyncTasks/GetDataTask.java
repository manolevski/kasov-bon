package com.anastasmanolevski.kasovbon.AsyncTasks;

import android.os.AsyncTask;

import com.anastasmanolevski.kasovbon.Listeners.GetDataListener;
import com.anastasmanolevski.kasovbon.Utils.Constants;

import java.io.IOException;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

public class GetDataTask extends AsyncTask<Void, Void, String> {

    private GetDataListener listener;
    private String mCookie;

    public GetDataTask(String cookie, GetDataListener listener) {
        mCookie = cookie;
        this.listener=listener;
    }

    @Override
    protected String doInBackground(Void... params) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(Constants.BASE_URL);
        httpget.setHeader("Cookie", "RAICHUADMSESSID="+mCookie);
        String responseString;
        try {
            HttpResponse response = httpclient.execute(httpget);
            responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (IOException e) {
            return "";
        }
        return responseString;
    }

    @Override
    protected void onPostExecute(final String result) {
        listener.onCompleted(result);
    }

    @Override
    protected void onCancelled() {
        listener.onCanceled();
    }
}
