package com.manolevski.kasovbon;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.gson.Gson;
import com.manolevski.kasovbon.AsyncTasks.GetDataTask;
import com.manolevski.kasovbon.Listeners.ResponseListener;
import com.manolevski.kasovbon.AsyncTasks.SendDataTask;
import com.manolevski.kasovbon.AsyncTasks.UserLoginTask;
import com.manolevski.kasovbon.Listeners.ErrorDialogClickListener;
import com.manolevski.kasovbon.Managers.DialogManager;
import com.manolevski.kasovbon.Utils.Constants;
import com.manolevski.kasovbon.Managers.SharedPreferencesManager;
import com.manolevski.kasovbon.Utils.ScannerResult;
import com.manolevski.kasovbon.Utils.User;
import com.google.android.material.snackbar.Snackbar;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {


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
    private ResponseListener sendDataListener;
    private ResponseListener getDataListener;
    private ResponseListener loginListener;

    private Calendar calendar;

    static final int SCANNER_REQUEST = 1;

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

        calendar = Calendar.getInstance();
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

        sendDataListener = new ResponseListener() {
            @Override
            public void onCompleted(String result) {
                sendDataTask = null;
                progressDialog.dismiss();

                if (Boolean.parseBoolean(result)) {
                    if (getDataTask == null) {
                        progressDialog.show();
                        getDataTask = new GetDataTask(cookie, getDataListener);
                        getDataTask.execute((Void) null);
                    }
                } else {
                    Snackbar.make(findViewById(android.R.id.content), R.string.send_fail, Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCanceled() {
                sendDataTask = null;
                progressDialog.dismiss();
            }
        };

        getDataListener = new ResponseListener() {
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

        loginListener = new ResponseListener() {
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
                } else {
                    Snackbar.make(findViewById(android.R.id.content), R.string.error_common, Snackbar.LENGTH_LONG).show();
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

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DialogManager.datePickerDialog(this, year, month, day, this);
    }

    public void showTimePickerDialog() {
        timeEdit.setError(null);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        DialogManager.timePickerDialog(this, hour, minute, this);
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

    public void scan(View v) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, Constants.CAMERA_PERMISSION);
        } else {
            Intent intent = new Intent(this, ScannerActivity.class);
            startActivityForResult(intent, SCANNER_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(this, ScannerActivity.class);
                    startActivityForResult(intent, SCANNER_REQUEST);
                } else {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.scan_permission), Snackbar.LENGTH_LONG).show();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCANNER_REQUEST) {
            if(resultCode == Activity.RESULT_OK){
                String json = data.getStringExtra(Constants.SCANNER_RESULT);
                if (json == null) {
                    return;
                }
                Gson gson = new Gson();
                ScannerResult scannerResult = gson.fromJson(json, ScannerResult.class);
                if (scannerResult == null) {
                    return;
                }
                if (scannerResult.getPrice() != null) {
                    priceEdit.setText(scannerResult.getPrice());
                }
                if (scannerResult.getDate() != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
                    dateEdit.setText(sdf.format(scannerResult.getDate()));
                }
                if (scannerResult.getTime() != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.GERMANY);
                    timeEdit.setText(sdf.format(scannerResult.getTime()));
                }
            }
            if (resultCode == Activity.RESULT_CANCELED && data != null) {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.scan_error), Snackbar.LENGTH_LONG).show();
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
            int registeredWeekInteger = Integer.parseInt(registeredWeek.toString());
            this.registeredWeek.setText(String.format(getString(R.string.registered_week), registeredWeekInteger));

            StringBuilder registeredMonth = new StringBuilder();
            for (Element el : registeredElements.get(1).select("li")) {
                registeredMonth.append(el.html());
            }
            int registeredMonthInteger = Integer.parseInt(registeredMonth.toString());
            this.registeredMonth.setText(String.format(getString(R.string.registered_month), registeredMonthInteger));

            StringBuilder registeredYear = new StringBuilder();
            for (Element el : registeredElements.get(2).select("li")) {
                registeredYear.append(el.html());
            }
            int registeredYearInteger = Integer.parseInt(registeredYear.toString());
            this.registeredYear.setText(String.format(getString(R.string.registered_year), registeredYearInteger));
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
            priceEdit.requestFocus();
            timeEdit.setText("");
            timePosEdit.setText("");
            posCheckBox.setChecked(false);
            Snackbar.make(findViewById(android.R.id.content), success.first().html().replaceAll("<br />", ""), Snackbar.LENGTH_LONG).show();
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

    @Override
    public void onDateSet(DatePicker datePicker, int selectedYear, int selectedMonth, int selectedDay) {
        calendar.set(Calendar.YEAR, selectedYear);
        calendar.set(Calendar.MONTH, selectedMonth);
        calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
        Date selectedDate = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
        dateEdit.setText(sdf.format(selectedDate));
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
        calendar.set(Calendar.MINUTE, selectedMinute);
        Date selectedTime = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.GERMANY);
        timeEdit.setText(sdf.format(selectedTime));
    }
}
