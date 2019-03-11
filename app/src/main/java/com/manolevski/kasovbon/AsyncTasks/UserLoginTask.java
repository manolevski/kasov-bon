package com.manolevski.kasovbon.AsyncTasks;

import android.os.AsyncTask;

import com.manolevski.kasovbon.Listeners.ResponseListener;
import com.manolevski.kasovbon.Utils.Constants;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HeaderElement;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

public class UserLoginTask extends AsyncTask<Void, Void, String> {

    private final String mEmail;
    private final String mPassword;
    private ResponseListener listener;

    public UserLoginTask(String email, String password, ResponseListener listener) {
        mEmail = email;
        mPassword = password;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(Void... params) {
        HttpPost httppost = new HttpPost(Constants.LOGIN_URL);
        HttpResponse response;

        List<NameValuePair> nameValuePairs = new ArrayList<>(2);
        nameValuePairs.add(new BasicNameValuePair("username", mEmail));
        nameValuePairs.add(new BasicNameValuePair("password", mPassword));

        try {
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        } catch (UnsupportedEncodingException e) {
            return "fail";
        }

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            response = httpClient.execute(httppost);
        } catch (IOException e) {
            return "fail";
        }

        if(response == null) return "fail";
        Header location = response.getFirstHeader("Location");
        if(location == null) return "fail";
        if(location.getValue().contains("#profile")){
            Header cookie = response.getFirstHeader("Set-Cookie");
            if(cookie == null) return "fail";
            HeaderElement value = cookie.getElements()[0];
            if(value == null) return "fail";
            return value.getValue();
        }
        else {
            return "error";
        }
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
