package com.anastasmanolevski.kasovbon.Managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.anastasmanolevski.kasovbon.Utils.User;

public class SharedPreferencesManager {

    private static final String SHARED_PREFERENCES_NAME = "com.anastasmanolevski.kasovbon.preferences";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER_ME = "rememberMe";
    private static final String KEY_COOKIE = "cookie"; //RAICHUADMSESSID

    private SharedPreferences sharedPreferences;

    public SharedPreferencesManager(Context context){
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public void setUser(User user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_NAME, user.getUserName());
        editor.putString(KEY_PASSWORD, user.getPassword());
        editor.putBoolean(KEY_REMEMBER_ME, user.getRememberMe());
        editor.apply();
    }

    public User getUser() {
        User user = new User();
        user.setUserName(sharedPreferences.getString(KEY_USER_NAME, ""));
        user.setPassword(sharedPreferences.getString(KEY_PASSWORD, ""));
        user.setRememberMe(sharedPreferences.getBoolean(KEY_REMEMBER_ME, false));
        return user;
    }

    public void setCookie(String cookie) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_COOKIE, cookie);
        editor.apply();
    }

    public String getCookie() {
        return sharedPreferences.getString(KEY_COOKIE, "");
    }
}
