package com.manolevski.kasovbon.Managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.manolevski.kasovbon.Utils.User;

public class SharedPreferencesManager {

    private static final String SHARED_PREFERENCES_NAME = "com.manolevski.kasovbon.preferences";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REGISTERED_COUNT = "registered_count";
    private static final String KEY_REVIEW_SHOWN = "review_shown";
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

    public void setRegisteredCount(int count) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_REGISTERED_COUNT, count);
        editor.apply();
    }

    public int getRegisteredCount() {
        return sharedPreferences.getInt(KEY_REGISTERED_COUNT, 0);
    }

    public void setReviewShown(boolean shown) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_REVIEW_SHOWN, shown);
        editor.apply();
    }

    public boolean getReviewShown() {
        return sharedPreferences.getBoolean(KEY_REVIEW_SHOWN, false);
    }
}
