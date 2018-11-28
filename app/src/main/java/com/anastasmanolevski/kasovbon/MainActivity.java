package com.anastasmanolevski.kasovbon;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.anastasmanolevski.kasovbon.AsyncTasks.GetDataTask;
import com.anastasmanolevski.kasovbon.Listeners.GetDataListener;
import com.anastasmanolevski.kasovbon.Listeners.LoginListener;
import com.anastasmanolevski.kasovbon.Listeners.SendDataListener;
import com.anastasmanolevski.kasovbon.AsyncTasks.SendDataTask;
import com.anastasmanolevski.kasovbon.AsyncTasks.UserLoginTask;
import com.anastasmanolevski.kasovbon.Listeners.ErrorDialogClickListener;
import com.anastasmanolevski.kasovbon.Managers.DialogManager;
import com.anastasmanolevski.kasovbon.Utils.Constants;
import com.anastasmanolevski.kasovbon.Utils.DatePickerFragment;
import com.anastasmanolevski.kasovbon.Managers.SharedPreferencesManager;
import com.anastasmanolevski.kasovbon.Utils.TimePickerFragment;
import com.anastasmanolevski.kasovbon.Utils.User;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

public class MainActivity extends AppCompatActivity {


    private EditText dateEdit;
    private EditText timeEdit;
    private EditText priceEdit;
    private EditText timePosEdit;
    private CheckBox posCheckBox;
    private String cookie;
    private User user;

    private SendDataTask sendDataTask = null;
    private GetDataTask getDataTask = null;
    private UserLoginTask authTask = null;

    private ProgressDialog progressDialog;

    private View dataForm;
    private TextView registeredWeek;
    private TextView registeredMonth;
    private TextView registeredYear;

    private SharedPreferencesManager preferences;
    private SendDataListener sendDataListener;
    private GetDataListener getDataListener;
    private LoginListener loginListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLayout();
        setListeners();

        progressDialog = DialogManager.initProgressDialog(this, getString(R.string.please_wait_message), false);

        preferences = new SharedPreferencesManager(this);
        cookie = preferences.getCookie();
        user = preferences.getUser();

        if (cookie.equals("")) {
            logOut();
        } else {
            if (getDataTask == null) {
                progressDialog.show();
                getDataTask = new GetDataTask(cookie, getDataListener);
                getDataTask.execute((Void) null);
            }
        }
    }

    @Override
    protected void onStop() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        super.onStop();
    }

    private void initLayout() {
        dateEdit = findViewById(R.id.date_edit);
        timeEdit = findViewById(R.id.time_edit);
        priceEdit = findViewById(R.id.price_edit);
        posCheckBox = findViewById(R.id.pos_check);
        timePosEdit = findViewById(R.id.time_pos_edit);

        dataForm = findViewById(R.id.data_form);
        registeredWeek = findViewById(R.id.registered_week);
        registeredMonth = findViewById(R.id.registered_month);
        registeredYear = findViewById(R.id.registered_year);

    }

    private void setListeners() {
        dateEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showDatePickerDialog();
                }
            }
        });

        timeEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showTimePickerDialog();
                }
            }
        });

        posCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                timePosEdit.setEnabled(checked);
                timePosEdit.setText(checked ? timeEdit.getText().toString() : "");
            }
        });

        sendDataListener = new SendDataListener() {
            @Override
            public void onCompleted(Boolean result) {
                sendDataTask = null;
                progressDialog.dismiss();

                if (result) {
                    if (getDataTask == null) {
                        progressDialog.show();
                        getDataTask = new GetDataTask(cookie, getDataListener);
                        getDataTask.execute((Void) null);
                    }
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.send_fail, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

            @Override
            public void onCanceled() {
                sendDataTask = null;
                progressDialog.dismiss();
            }
        };

        getDataListener = new GetDataListener() {
            @Override
            public void onCompleted(String result) {
                getDataTask = null;
                progressDialog.dismiss();
                if (!result.equals(""))
                    parseHTML(result);
            }

            @Override
            public void onCanceled() {
                getDataTask = null;
                progressDialog.dismiss();
            }
        };

        loginListener = new LoginListener() {
            @Override
            public void onCompleted(String result) {
                authTask = null;
                progressDialog.dismiss();

                if (!result.equals("error") && !result.equals("fail")) {
                    preferences.setCookie(result);
                    cookie = result;
                    if (getDataTask == null) {
                        progressDialog.show();
                        getDataTask = new GetDataTask(cookie, getDataListener);
                        getDataTask.execute((Void) null);
                    }
                } else if (result.equals("error")) {
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.error_common, Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.error_common, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

            @Override
            public void onCanceled() {
                authTask = null;
                progressDialog.dismiss();
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                logOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showDatePickerDialog() {
        dateEdit.setError(null);
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void showTimePickerDialog() {
        timeEdit.setError(null);
        TimePickerFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public void sendData(View v) {
        // Reset errors.
        timeEdit.setError(null);
        priceEdit.setError(null);
        dateEdit.setError(null);

        String time = timeEdit.getText().toString();
        String posTime = timePosEdit.getText().toString();
        String price = priceEdit.getText().toString();
        String date = dateEdit.getText().toString();
        boolean cancel = false;
        View focusView = null;
        if (TextUtils.isEmpty(time)) {
            timeEdit.setError(getString(R.string.error_field_required));
            focusView = timeEdit;
            cancel = true;
        }
        if (TextUtils.isEmpty(date)) {
            dateEdit.setError(getString(R.string.error_field_required));
            focusView = dateEdit;
            cancel = true;
        }
        if (TextUtils.isEmpty(price)) {
            priceEdit.setError(getString(R.string.error_field_required));
            focusView = priceEdit;
            cancel = true;
        }
        if (TextUtils.isEmpty(posTime) && posCheckBox.isChecked()) {
            timePosEdit.setError(getString(R.string.error_field_required));
            focusView = timePosEdit;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            if (sendDataTask == null) {
                progressDialog.show();
                sendDataTask = new SendDataTask(date, time, price, posTime, cookie, sendDataListener);
                sendDataTask.execute((Void) null);
            }
        }
    }

    private void parseHTML(String getDataString) {
        if (getDataString.isEmpty())
            return;
        Document doc = Parser.parse(getDataString, Constants.BASE_URL);
        Elements section = doc.select("section.profile");
        if (section == null || section.size() == 0) {
            if (user.getRememberMe())
                logInAgain();
            else
                logOut();
            return;
        }
        if (section.first() != null) {
            setTitle(section.first().select("strong").first().html());

            //set registered values
            dataForm.setVisibility(View.VISIBLE);
            Elements registeredElements = section.first().select("ul");

            StringBuilder registeredWeek = new StringBuilder();
            for (Element el : registeredElements.get(0).select("li")) {
                registeredWeek.append(el.html());
            }
            Integer registeredWeekInteger = Integer.parseInt(registeredWeek.toString());
            this.registeredWeek.setText(String.format(getString(R.string.registered_week), registeredWeekInteger.toString()));

            StringBuilder registeredMonth = new StringBuilder();
            for (Element el : registeredElements.get(1).select("li")) {
                registeredMonth.append(el.html());
            }
            Integer registeredMonthInteger = Integer.parseInt(registeredMonth.toString());
            this.registeredMonth.setText(String.format(getString(R.string.registered_month), registeredMonthInteger.toString()));

            StringBuilder registeredYear = new StringBuilder();
            for (Element el : registeredElements.get(2).select("li")) {
                registeredYear.append(el.html());
            }
            Integer registeredYearInteger = Integer.parseInt(registeredYear.toString());
            this.registeredYear.setText(String.format(getString(R.string.registered_year), registeredYearInteger.toString()));
        }
        //error messages
        Elements error = doc.select("div.alert-danger");
        if (error.size() > 0) {
            DialogManager.errorDialog(this, getString(R.string.error), error.first().html(), new ErrorDialogClickListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    dialog.dismiss();
                }
            });
        }

        //Success messages
        Elements success = doc.select("div.alert-success");
        if (success.size() > 0 && !success.first().html().equals("")) {
            priceEdit.setText("");
            timeEdit.setText("");
            timePosEdit.setText("");
            posCheckBox.setChecked(false);
            Toast toast = Toast.makeText(getApplicationContext(), success.first().html().replaceAll("<br />", ""), Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void logInAgain() {
        progressDialog.show();

        if (authTask == null) {
            authTask = new UserLoginTask(user.getUserName(), user.getPassword(), loginListener);
            authTask.execute((Void) null);
        }
    }

    private void logOut() {
        preferences.setCookie("");
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
