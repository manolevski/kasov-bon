package com.manolevski.kasovbon;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.manolevski.kasovbon.Listeners.LoginListener;
import com.manolevski.kasovbon.AsyncTasks.UserLoginTask;
import com.manolevski.kasovbon.Listeners.ErrorDialogClickListener;
import com.manolevski.kasovbon.Managers.DialogManager;
import com.manolevski.kasovbon.Managers.SharedPreferencesManager;
import com.manolevski.kasovbon.Utils.User;
import com.google.android.material.snackbar.Snackbar;

public class LoginActivity extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    private LoginListener loginListener;

    private SharedPreferencesManager preferences;
    private ProgressDialog progressDialog;

    private AutoCompleteTextView emailView;
    private EditText passwordView;
    private CheckBox rememberMe;
//    private View progressView;
//    private View loginFormView;
    private TextView registerText;
    private Button emailSignInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupActionBar();
        initLayout();
        setListeners();

        preferences = new SharedPreferencesManager(this);

        rememberMe.setChecked(preferences.getUser().getRememberMe());
        if(rememberMe.isChecked())
        {
            emailView.setText(preferences.getUser().getUserName());
            passwordView.setText(preferences.getUser().getPassword());
        }

        registerText.setText(Html.fromHtml(getString(R.string.register_link)));
        registerText.setMovementMethod(LinkMovementMethod.getInstance());

        progressDialog = DialogManager.initProgressDialog(this, getString(R.string.please_wait_message), false);
    }

    @Override
    protected void onStop() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        super.onStop();
    }

    private void initLayout() {
        // Set up the login form.
        emailView = findViewById(R.id.email);
        rememberMe = findViewById(R.id.remember_me);
        passwordView = findViewById(R.id.password);
        registerText = findViewById(R.id.register);
        emailSignInButton = findViewById(R.id.email_sign_in_button);

//        loginFormView = findViewById(R.id.login_form);
//        progressView = findViewById(R.id.login_progress);
    }

    private void setListeners() {
        passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        emailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        loginListener = new LoginListener() {
            @Override
            public void onCompleted(String result) {
                mAuthTask = null;
                progressDialog.dismiss();

                if (!result.equals("error") && !result.equals("fail")) {
                    preferences.setCookie(result);

                    User user = new User();
                    if(rememberMe.isChecked()){
                        user.setUserName(emailView.getText().toString());
                        user.setPassword(passwordView.getText().toString());
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
                    DialogManager.errorDialog(LoginActivity.this, getString(R.string.error), getString(R.string.error_incorrect_password), new ErrorDialogClickListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            dialog.dismiss();
                        }
                    });
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

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        emailView.setError(null);
        passwordView.setError(null);

        // Store values at the time of the login attempt.
        String email = emailView.getText().toString();
        String password = passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for empty password.
        if (TextUtils.isEmpty(password)) {
            passwordView.setError(getString(R.string.error_field_required));
            focusView = passwordView;
            cancel = true;
        }

        // Check for empty email address.
        if (TextUtils.isEmpty(email)) {
            emailView.setError(getString(R.string.error_field_required));
            focusView = emailView;
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