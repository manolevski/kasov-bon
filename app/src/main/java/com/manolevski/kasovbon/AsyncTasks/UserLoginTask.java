package com.manolevski.kasovbon.AsyncTasks;

import android.os.AsyncTask;

import com.manolevski.kasovbon.Listeners.LoginListener;
import com.manolevski.kasovbon.Utils.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HeaderElement;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

public class UserLoginTask extends AsyncTask<Void, Void, String> {

    private final String mEmail;
    private final String mPassword;
    private LoginListener listener;

    public UserLoginTask(String email, String password, LoginListener listener) {
        mEmail = email;
        mPassword = password;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(Void... params) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(Constants.LOGIN_URL);
        HttpResponse response;
        try {
            List<NameValuePair> nameValuePairs = new ArrayList<>(2);
            nameValuePairs.add(new BasicNameValuePair("username", mEmail));
            nameValuePairs.add(new BasicNameValuePair("password", mPassword));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            response = httpclient.execute(httppost);
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
