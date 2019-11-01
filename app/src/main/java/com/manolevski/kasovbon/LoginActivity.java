package com.manolevski.kasovbon;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.snackbar.Snackbar;
import com.manolevski.kasovbon.AsyncTasks.UserLoginTask;
import com.manolevski.kasovbon.Listeners.ResponseListener;
import com.manolevski.kasovbon.Managers.DialogManager;
import com.manolevski.kasovbon.Managers.SharedPreferencesManager;
import com.manolevski.kasovbon.Utils.User;
import com.manolevski.kasovbon.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    private ResponseListener loginListener;

    private SharedPreferencesManager preferences;
    private ProgressDialog progressDialog;

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        setupActionBar();
        setListeners();

        preferences = new SharedPreferencesManager(this);

        binding.rememberMeCheckbox.setChecked(preferences.getUser().getRememberMe());
        if(binding.rememberMeCheckbox.isChecked())
        {
            binding.emailEdit.setText(preferences.getUser().getUserName());
            binding.passwordEdit.setText(preferences.getUser().getPassword());
        }

        binding.registerText.setText(Html.fromHtml(getString(R.string.register_link)));
        binding.registerText.setMovementMethod(LinkMovementMethod.getInstance());

        progressDialog = DialogManager.initProgressDialog(this, getString(R.string.please_wait_message), false);
    }

    @Override
    protected void onStop() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        super.onStop();
    }

    private void setListeners() {
        loginListener = new ResponseListener() {
            @Override
            public void onCompleted(String result) {
                mAuthTask = null;
                progressDialog.dismiss();

                if (!result.equals("error") && !result.equals("fail")) {
                    preferences.setCookie(result);

                    User user = new User();
                    if(binding.rememberMeCheckbox.isChecked()){
                        user.setUserName(binding.emailEdit.getText().toString());
                        user.setPassword(binding.passwordEdit.getText().toString());
                        user.setRememberMe(true);
                    } else {
                        user.setUserName("");
                        user.setPassword("");
                        user.setRememberMe(false);
                    }
                    preferences.setUser(user);

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                } else if(result.equals("error")) {
                    DialogManager.errorDialog(LoginActivity.this, getString(R.string.error), getString(R.string.error_incorrect_password), DialogInterface::dismiss);
                }
                else {
                    Snackbar.make(findViewById(android.R.id.content), R.string.error_common, Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCanceled() {
                mAuthTask = null;
                progressDialog.dismiss();
            }
        };
    }

    private void setupActionBar() {
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    public void attemptLogin(View v) {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        binding.emailEdit.setError(null);
        binding.passwordEdit.setError(null);

        // Store values at the time of the login attempt.
        String email = binding.emailEdit.getText().toString();
        String password = binding.passwordEdit.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for empty password.
        if (TextUtils.isEmpty(password)) {
            binding.passwordEdit.setError(getString(R.string.error_field_required));
            focusView = binding.passwordEdit;
            cancel = true;
        }

        // Check for empty email address.
        if (TextUtils.isEmpty(email)) {
            binding.emailEdit.setError(getString(R.string.error_field_required));
            focusView = binding.emailEdit;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            progressDialog.show();

            mAuthTask = new UserLoginTask(email, password, loginListener);
            mAuthTask.execute((Void) null);
        }
    }
}